@file:Suppress("TooManyFunctions")

package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import za.co.dope.ballistics.engine.BearingReference
import za.co.dope.ballistics.engine.ResolvedWind
import za.co.dope.ballistics.engine.WindConvention
import za.co.dope.ballistics.engine.WindSpeedSelection
import za.co.dope.ballistics.ui.components.DopeCard
import za.co.dope.ballistics.ui.components.DopeField
import za.co.dope.ballistics.ui.components.DopeFieldConfig
import za.co.dope.ballistics.ui.components.DopePrimaryButton
import za.co.dope.ballistics.ui.components.DopeSecondaryButton
import za.co.dope.ballistics.ui.components.DopeStatus
import za.co.dope.ballistics.ui.components.LabelValue
import za.co.dope.ballistics.ui.components.StatusChip
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
@Suppress("CyclomaticComplexMethod", "LongMethod")
fun WindScreen(state: WindFormState) {
    val resolved =
        remember(state.observation(), state.windFromDegrees, state.directionOfFireDegrees) {
            state.observation()?.let { runCatching { WindConvention.resolve(it) }.getOrNull() }
        }
    ReferencePanelShell(title = "Wind") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Info, contentDescription = "Wind convention information")
            }
            TextButton(
                onClick = {
                    if (!state.locked) {
                        state.windFromDegrees = "0"
                        state.clockDirection = "12"
                    }
                },
            ) { Text("Reset") }
        }
        WindWheel(
            windFromDegrees = state.windFromDegrees.toDoubleOrNull() ?: 0.0,
            directionOfFireDegrees = state.directionOfFireDegrees.toDoubleOrNull() ?: 0.0,
            locked = state.locked,
            onWindFromChanged = { degrees ->
                state.windFromDegrees = formatWhole(degrees)
                state.clockDirection = clockFromDegrees(degrees).toString()
                state.timestampEpochMillis = System.currentTimeMillis()
            },
        )
        resolved?.let { WindResultTiles(it) }
            ?: StatusChip("Complete valid wind values", DopeStatus.BLOCKED)
        SpeedSelectionTiles(state)
        WindInputEditor(state)
        DopePrimaryButton(
            if (state.locked) "Unlock wind" else "Lock wind",
            { state.locked = !state.locked },
            Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SpeedSelectionTiles(state: WindFormState) {
    val values =
        listOf(
            Triple(WindSpeedSelection.MINIMUM, "Min", state.minimumSpeedMps),
            Triple(WindSpeedSelection.AVERAGE, "Avg", state.averageSpeedMps),
            Triple(WindSpeedSelection.MAXIMUM, "Max", state.maximumSpeedMps),
            Triple(WindSpeedSelection.GUST, "Gust", state.gustSpeedMps.ifBlank { "—" }),
        )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        values.forEach { (selection, label, value) ->
            ReferenceMetricTile(
                label = "$label · m/s",
                value = value,
                modifier = Modifier.weight(1f),
                selected = state.selectedSpeed == selection,
                onClick = { if (!state.locked) state.selectedSpeed = selection },
            )
        }
    }
}

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod")
private fun WindInputEditor(state: WindFormState) {
    DopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("WIND INPUTS", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = state.reference == BearingReference.TRUE,
                    onClick = { if (!state.locked) state.reference = BearingReference.TRUE },
                    label = { Text("True") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.reference == BearingReference.MAGNETIC,
                    onClick = { if (!state.locked) state.reference = BearingReference.MAGNETIC },
                    label = { Text("Magnetic") },
                    modifier = Modifier.weight(1f),
                )
            }
            DopeField(
                "Wind from",
                state.windFromDegrees,
                { value -> if (!state.locked) state.windFromDegrees = value },
                config = DopeFieldConfig(suffix = "°"),
            )
            DopeField(
                "Clock direction",
                state.clockDirection,
                { value ->
                    if (!state.locked) {
                        state.clockDirection = value
                        value.toIntOrNull()?.takeIf { it in 1..12 }?.let { clock ->
                            state.windFromDegrees = formatWhole((clock % 12) * 30.0)
                        }
                    }
                },
                config = DopeFieldConfig(suffix = "o’clock"),
            )
            DopeField(
                "Direction of fire",
                state.directionOfFireDegrees,
                { if (!state.locked) state.directionOfFireDegrees = it },
                config = DopeFieldConfig(suffix = "°"),
            )
            if (state.reference == BearingReference.MAGNETIC) {
                DopeField(
                    "Magnetic declination",
                    state.magneticDeclinationDegrees,
                    { if (!state.locked) state.magneticDeclinationDegrees = it },
                    config = DopeFieldConfig(suffix = "° east +"),
                )
            }
            DopeField(
                "Minimum",
                state.minimumSpeedMps,
                { if (!state.locked) state.minimumSpeedMps = it },
                config = DopeFieldConfig(suffix = "m/s"),
            )
            DopeField(
                "Average",
                state.averageSpeedMps,
                { if (!state.locked) state.averageSpeedMps = it },
                config = DopeFieldConfig(suffix = "m/s"),
            )
            DopeField(
                "Maximum",
                state.maximumSpeedMps,
                { if (!state.locked) state.maximumSpeedMps = it },
                config = DopeFieldConfig(suffix = "m/s"),
            )
            DopeField(
                "Gust",
                state.gustSpeedMps,
                { if (!state.locked) state.gustSpeedMps = it },
                config = DopeFieldConfig(suffix = "m/s"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DopeSecondaryButton("−1°", { adjustWind(state, -1.0) }, Modifier.weight(1f))
                DopeSecondaryButton("+1°", { adjustWind(state, 1.0) }, Modifier.weight(1f))
            }
            DopeField("Source", state.source, { if (!state.locked) state.source = it })
            DopeField("Notes", state.notes, { if (!state.locked) state.notes = it })
        }
    }
}

@Composable
private fun WindResultTiles(result: ResolvedWind) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ReferenceMetricTile(
            "Direction of fire",
            "${format(result.directionOfFireInputDegrees, "°")} (${cardinal(result.directionOfFireInputDegrees)})",
            Modifier.weight(1f),
        )
        ReferenceMetricTile(
            "Relative angle",
            "${format(result.relativeWindFromDegrees, "°")} (${result.effect.name.lowercase()})",
            Modifier.weight(1f),
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ReferenceMetricTile(
            "Crosswind",
            format(result.selected.crosswindMps, " m/s (${result.effect.name.first()})"),
            Modifier.weight(1f),
        )
        ReferenceMetricTile(
            "Headwind",
            format(result.selected.headwindMps, " m/s"),
            Modifier.weight(1f),
        )
    }
    if (result.bearingReference == BearingReference.MAGNETIC && result.windFromTrueDegrees == null) {
        StatusChip("True bearing unavailable · declination missing", DopeStatus.WARNING)
    }
}

@Composable
@Suppress("LongMethod")
internal fun WindWheel(
    windFromDegrees: Double,
    directionOfFireDegrees: Double,
    locked: Boolean,
    onWindFromChanged: (Double) -> Unit,
    height: androidx.compose.ui.unit.Dp = 330.dp,
) {
    val updateFromPosition: (Offset, Offset) -> Unit = { position, centre ->
        val bearing = WindConvention.normalizeDegrees(atan2(position.x - centre.x, centre.y - position.y) * 180.0 / PI)
        onWindFromChanged(bearing)
    }
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(18.dp)
                .pointerInput(locked) {
                    if (!locked) {
                        detectTapGestures { updateFromPosition(it, Offset(size.width / 2f, size.height / 2f)) }
                    }
                }.pointerInput(locked) {
                    if (!locked) {
                        detectDragGestures { change, _ ->
                            updateFromPosition(change.position, Offset(size.width / 2f, size.height / 2f))
                        }
                    }
                },
    ) {
        val radius = size.minDimension * 0.4f
        val centre = center
        drawCircle(Color(0xFF253B4E), radius, centre, style = Stroke(width = 3.dp.toPx()))
        drawCircle(Color(0xFF253B4E), radius * 0.47f, centre, style = Stroke(width = 2.dp.toPx()))
        repeat(36) { index ->
            val angle = index * 10.0 * PI / 180.0
            val outer = Offset(centre.x + sin(angle).toFloat() * radius, centre.y - cos(angle).toFloat() * radius)
            val innerRadius = radius - if (index % 3 == 0) 14.dp.toPx() else 7.dp.toPx()
            val inner =
                Offset(
                    centre.x + sin(angle).toFloat() * innerRadius,
                    centre.y - cos(angle).toFloat() * innerRadius,
                )
            drawLine(Color(0xFF9FB1C4), inner, outer, 2.dp.toPx(), StrokeCap.Round)
        }
        drawDirectionArrow(centre, radius * 0.82f, windFromDegrees, Color(0xFF2CC5E8), true)
        drawDirectionArrow(centre, radius * 0.62f, directionOfFireDegrees, Color(0xFF22C55E), false)
        drawContext.canvas.nativeCanvas.apply {
            val paint =
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textSize = 13.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            drawText("N", centre.x, centre.y - radius - 8.dp.toPx(), paint)
            drawText("E", centre.x + radius + 12.dp.toPx(), centre.y + 5.dp.toPx(), paint)
            drawText("S", centre.x, centre.y + radius + 18.dp.toPx(), paint)
            drawText("W", centre.x - radius - 12.dp.toPx(), centre.y + 5.dp.toPx(), paint)
            repeat(12) { index ->
                val degrees = index * 30
                if (degrees % 90 == 0) return@repeat
                val angle = degrees * PI / 180.0
                val labelRadius = radius + 22.dp.toPx()
                paint.textSize = 10.dp.toPx()
                drawText(
                    degrees.toString(),
                    centre.x + sin(angle).toFloat() * labelRadius,
                    centre.y - cos(angle).toFloat() * labelRadius + 4.dp.toPx(),
                    paint,
                )
            }
            paint.textSize = 12.dp.toPx()
            drawText("Wind From", centre.x, centre.y - 18.dp.toPx(), paint)
            paint.textSize = 28.dp.toPx()
            drawText("${formatWhole(windFromDegrees)}°", centre.x, centre.y + 12.dp.toPx(), paint)
            paint.textSize = 14.dp.toPx()
            drawText(cardinal(windFromDegrees), centre.x, centre.y + 34.dp.toPx(), paint)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDirectionArrow(
    centre: Offset,
    length: Float,
    bearingDegrees: Double,
    color: Color,
    pointsTowardCentre: Boolean,
) {
    val radians = bearingDegrees * PI / 180.0
    val edge = Offset(centre.x + sin(radians).toFloat() * length, centre.y - cos(radians).toFloat() * length)
    val start = if (pointsTowardCentre) edge else centre
    val end = if (pointsTowardCentre) centre else edge
    drawLine(color, start, end, 5.dp.toPx(), StrokeCap.Round)
    val direction = if (pointsTowardCentre) bearingDegrees + 180.0 else bearingDegrees
    val left = direction - 150.0
    val right = direction + 150.0
    val tip = end
    val path =
        Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(
                tip.x + sin(left * PI / 180.0).toFloat() * 18.dp.toPx(),
                tip.y - cos(left * PI / 180.0).toFloat() * 18.dp.toPx(),
            )
            lineTo(
                tip.x + sin(right * PI / 180.0).toFloat() * 18.dp.toPx(),
                tip.y - cos(right * PI / 180.0).toFloat() * 18.dp.toPx(),
            )
            close()
        }
    drawPath(path, color)
}

private fun adjustWind(
    state: WindFormState,
    delta: Double,
) {
    if (!state.locked) {
        val value = WindConvention.normalizeDegrees((state.windFromDegrees.toDoubleOrNull() ?: 0.0) + delta)
        state.windFromDegrees = formatWhole(value)
        state.clockDirection = clockFromDegrees(value).toString()
    }
}

private fun clockFromDegrees(value: Double): Int {
    val clock = ((value + 15.0) / 30.0).toInt() % 12
    return if (clock == 0) 12 else clock
}

private fun format(
    value: Double,
    suffix: String,
): String = String.format(Locale.ROOT, "%.1f%s", value, suffix)

private fun formatWhole(value: Double): String {
    val normalized = WindConvention.normalizeDegrees(value)
    return String.format(Locale.ROOT, "%.0f", normalized)
}

private fun cardinal(value: Double): String {
    val points = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = ((WindConvention.normalizeDegrees(value) + 22.5) / 45.0).toInt() % points.size
    return points[index]
}
