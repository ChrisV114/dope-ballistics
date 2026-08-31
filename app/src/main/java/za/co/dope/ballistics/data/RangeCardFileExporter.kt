package za.co.dope.ballistics.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import za.co.dope.ballistics.domain.DistanceDisplayUnit
import za.co.dope.ballistics.domain.RangeCardColumnSet
import za.co.dope.ballistics.domain.RangeCardCsvExporter
import za.co.dope.ballistics.domain.RangeCardDocument
import za.co.dope.ballistics.domain.RangeCardLayout
import za.co.dope.ballistics.domain.RangeCardRow
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

enum class RangeCardExportFormat(
    val extension: String,
    val mimeType: String,
) {
    CSV("csv", "text/csv"),
    PDF("pdf", "application/pdf"),
    PNG("png", "image/png"),
}

class RangeCardFileExporter(
    private val context: Context,
) {
    fun export(
        document: RangeCardDocument,
        format: RangeCardExportFormat,
        fileStem: String = "dope-range-card",
    ): File {
        val exportDirectory = File(context.cacheDir, EXPORT_DIRECTORY).apply { mkdirs() }
        val safeStem = fileStem.replace(Regex("[^A-Za-z0-9._-]"), "-").trim('-').ifBlank { "dope-range-card" }
        val output = File(exportDirectory, "$safeStem.${format.extension}")
        when (format) {
            RangeCardExportFormat.CSV -> output.writeText(RangeCardCsvExporter.export(document), Charsets.UTF_8)
            RangeCardExportFormat.PDF -> writePdf(document, output)
            RangeCardExportFormat.PNG -> writePng(document, output)
        }
        return output
    }

    fun shareIntent(
        file: File,
        format: RangeCardExportFormat,
    ): Intent {
        val exportDirectory = File(context.cacheDir, EXPORT_DIRECTORY).canonicalFile
        require(file.canonicalFile.parentFile == exportDirectory) {
            "Only generated range-card exports can be shared"
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun writePdf(
        document: RangeCardDocument,
        output: File,
    ) {
        val pdf = PdfDocument()
        try {
            val rowsPerPage = 24
            document.rows.chunked(rowsPerPage).forEachIndexed { pageIndex, rows ->
                val page =
                    pdf.startPage(
                        PdfDocument.PageInfo.Builder(PDF_WIDTH, PDF_HEIGHT, pageIndex + 1).create(),
                    )
                drawPage(page.canvas, document, rows, pageIndex + 1)
                pdf.finishPage(page)
            }
            if (document.rows.isEmpty()) {
                val page = pdf.startPage(PdfDocument.PageInfo.Builder(PDF_WIDTH, PDF_HEIGHT, 1).create())
                drawPage(page.canvas, document, emptyList(), 1)
                pdf.finishPage(page)
            }
            FileOutputStream(output).use(pdf::writeTo)
        } finally {
            pdf.close()
        }
    }

    private fun writePng(
        document: RangeCardDocument,
        output: File,
    ) {
        val height =
            (HEADER_HEIGHT + (document.rows.size.coerceAtLeast(1) + 1) * ROW_HEIGHT + FOOTER_HEIGHT).toInt()
        val bitmap = createBitmap(PNG_WIDTH, height, Bitmap.Config.ARGB_8888)
        try {
            drawPage(Canvas(bitmap), document, document.rows, 1)
            FileOutputStream(output).use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)) { "PNG encoding failed" }
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun drawPage(
        canvas: Canvas,
        document: RangeCardDocument,
        rows: List<RangeCardRow>,
        pageNumber: Int,
    ) {
        val palette = palette(RangeCardLayout.valueOf(document.metadata.layout))
        canvas.drawColor(palette.background)
        val titlePaint = paint(palette.text, 30f, Typeface.BOLD)
        val headings = headings(document)
        val tableTextSize = if (headings.size > 8) 7f else 12f
        val accentPaint = paint(palette.accent, tableTextSize, Typeface.BOLD)
        val textPaint = paint(palette.text, tableTextSize, Typeface.NORMAL)
        val mutedPaint = paint(palette.muted, 12f, Typeface.NORMAL)
        canvas.drawText("DOPE RANGE CARD", MARGIN, 42f, titlePaint)
        canvas.drawText(document.metadata.profileLabel, MARGIN, 66f, accentPaint)
        canvas.drawText(
            "${document.metadata.units} • engine ${document.metadata.engineVersion} • page $pageNumber",
            MARGIN,
            88f,
            mutedPaint,
        )
        var y = HEADER_HEIGHT.toFloat()
        val columnWidth = (PDF_WIDTH - 2 * MARGIN) / headings.size
        headings.forEachIndexed { index, heading ->
            canvas.drawText(heading, MARGIN + index * columnWidth, y, accentPaint)
        }
        rows.forEach { row ->
            y += ROW_HEIGHT
            canvas.drawLine(MARGIN, y - 20f, PDF_WIDTH - MARGIN, y - 20f, paint(palette.rule, 1f))
            values(document, row).forEachIndexed { index, value ->
                canvas.drawText(value, MARGIN + index * columnWidth, y, textPaint)
            }
        }
        if (rows.isEmpty()) canvas.drawText("No valid trajectory rows", MARGIN, y + ROW_HEIGHT, textPaint)
        val footerY = canvas.height - 24f
        canvas.drawText(
            "Created ${document.metadata.createdAtEpochMillis} • " +
                "precise location: ${document.metadata.preciseLocationIncluded}",
            MARGIN,
            footerY,
            mutedPaint,
        )
    }

    private fun paint(
        color: Int,
        size: Float,
        style: Int = Typeface.NORMAL,
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = Typeface.create(Typeface.MONOSPACE, style)
    }

    private fun format(
        value: Double,
        suffix: String,
    ): String = String.format(Locale.ROOT, "%.2f%s", value, suffix)

    private fun headings(document: RangeCardDocument): List<String> =
        when (RangeCardColumnSet.valueOf(document.metadata.columnSet)) {
            RangeCardColumnSet.ESSENTIAL -> {
                listOf("RANGE", "DIAL", "CLICKS", "WIND")
            }

            RangeCardColumnSet.FIELD -> {
                listOf("RANGE", "RAW", "DIAL", "CLICKS", "WIND", "MIN", "MAX", "VEL")
            }

            RangeCardColumnSet.FULL -> {
                listOf(
                    "RANGE",
                    "RAW",
                    "DIAL",
                    "CLICK",
                    "RET",
                    "ENV",
                    "WIND",
                    "MIN",
                    "MAX",
                    "TOF",
                    "VEL",
                    "ENERGY",
                    "MACH",
                    "STATE",
                    "UNC",
                    "WARN",
                )
            }
        }

    private fun values(
        document: RangeCardDocument,
        row: RangeCardRow,
    ): List<String> {
        val range =
            if (document.metadata.distanceUnits == DistanceDisplayUnit.YARDS.name) {
                format(row.distanceMetres / 0.9144, "yd")
            } else {
                format(row.distanceMetres, "m")
            }
        val essential =
            listOf(range, format(row.elevationDial, ""), row.elevationClicks.toString(), format(row.windSelected, ""))
        return when (RangeCardColumnSet.valueOf(document.metadata.columnSet)) {
            RangeCardColumnSet.ESSENTIAL -> {
                essential
            }

            RangeCardColumnSet.FIELD -> {
                listOf(
                    range,
                    format(row.elevationRaw, ""),
                    essential[1],
                    essential[2],
                    essential[3],
                    format(row.windMinimum, ""),
                    format(row.windMaximum, ""),
                    format(row.remainingVelocityMps, ""),
                )
            }

            RangeCardColumnSet.FULL -> {
                listOf(
                    range,
                    format(row.elevationRaw, ""),
                    essential[1],
                    essential[2],
                    row.reticleHold?.let { format(it, "") } ?: "N/A",
                    format(row.environmentalDeviation, ""),
                    essential[3],
                    format(row.windMinimum, ""),
                    format(row.windMaximum, ""),
                    format(row.timeOfFlightSeconds, ""),
                    format(row.remainingVelocityMps, ""),
                    format(row.remainingEnergyJoules, ""),
                    format(row.mach, ""),
                    row.flightState,
                    format(row.elevationUncertainty, ""),
                    row.warningState,
                )
            }
        }
    }

    private fun palette(layout: RangeCardLayout): Palette =
        when (layout) {
            RangeCardLayout.OUTDOOR -> {
                Palette(
                    Color.rgb(8, 18, 32),
                    Color.rgb(230, 236, 244),
                    Color.rgb(34, 197, 94),
                    Color.rgb(160, 176, 196),
                    Color.rgb(37, 52, 73),
                )
            }

            RangeCardLayout.HIGH_CONTRAST -> {
                Palette(Color.BLACK, Color.WHITE, Color.YELLOW, Color.LTGRAY, Color.WHITE)
            }

            RangeCardLayout.RED_LIGHT -> {
                Palette(Color.BLACK, Color.rgb(255, 96, 96), Color.RED, Color.rgb(190, 64, 64), Color.rgb(110, 24, 24))
            }
        }

    private data class Palette(
        val background: Int,
        val text: Int,
        val accent: Int,
        val muted: Int,
        val rule: Int,
    )

    private companion object {
        const val EXPORT_DIRECTORY = "range-card-exports"
        const val PDF_WIDTH = 842
        const val PDF_HEIGHT = 595
        const val PNG_WIDTH = PDF_WIDTH
        const val HEADER_HEIGHT = 120
        const val ROW_HEIGHT = 28f
        const val FOOTER_HEIGHT = 50
        const val MARGIN = 36f
        const val PNG_QUALITY = 100
    }
}
