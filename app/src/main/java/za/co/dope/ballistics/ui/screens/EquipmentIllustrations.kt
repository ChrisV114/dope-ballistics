@file:Suppress("MatchingDeclarationName")

package za.co.dope.ballistics.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

internal enum class EquipmentIllustrationType(
    val description: String,
) {
    RIFLE("Rifle illustration"),
    AMMUNITION("Ammunition illustration"),
    SCOPE("Scope illustration"),
}

/** Neutral local line art used until the owner attaches a photo to a specific profile. */
@Composable
internal fun EquipmentIllustration(
    type: EquipmentIllustrationType,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.height(58.dp).semantics { contentDescription = type.description }) {
        when (type) {
            EquipmentIllustrationType.RIFLE -> drawRifleIllustration()
            EquipmentIllustrationType.AMMUNITION -> drawAmmunitionIllustration()
            EquipmentIllustrationType.SCOPE -> drawScopeIllustration()
        }
    }
}

private fun DrawScope.drawRifleIllustration() {
    val line = Color(0xFF94A3B8)
    val accent = Color(0xFF60A5FA)
    val y = size.height * 0.48f
    drawLine(
        line,
        Offset(size.width * 0.12f, y),
        Offset(size.width * 0.88f, y),
        5.dp.toPx(),
        StrokeCap.Round,
    )
    drawLine(
        accent,
        Offset(size.width * 0.56f, y - 8.dp.toPx()),
        Offset(size.width * 0.76f, y - 8.dp.toPx()),
        3.dp.toPx(),
    )
    drawRoundRect(
        line,
        topLeft = Offset(size.width * 0.32f, y - 7.dp.toPx()),
        size = Size(size.width * 0.27f, 15.dp.toPx()),
        cornerRadius =
            androidx.compose.ui.geometry
                .CornerRadius(4.dp.toPx()),
        style = Stroke(3.dp.toPx()),
    )
    drawLine(
        line,
        Offset(size.width * 0.42f, y + 6.dp.toPx()),
        Offset(size.width * 0.38f, y + 23.dp.toPx()),
        5.dp.toPx(),
    )
    val stock =
        Path().apply {
            moveTo(size.width * 0.12f, y)
            lineTo(size.width * 0.05f, y + 18.dp.toPx())
            lineTo(size.width * 0.23f, y + 14.dp.toPx())
            lineTo(size.width * 0.32f, y)
            close()
        }
    drawPath(stock, line.copy(alpha = 0.72f))
}

private fun DrawScope.drawAmmunitionIllustration() {
    val brass = Color(0xFFD6A85F)
    val copper = Color(0xFFE8793E)
    val centre = size.width / 2f
    val bodyWidth = 17.dp.toPx()
    drawRoundRect(
        brass,
        topLeft = Offset(centre - bodyWidth / 2f, size.height * 0.34f),
        size = Size(bodyWidth, size.height * 0.55f),
        cornerRadius =
            androidx.compose.ui.geometry
                .CornerRadius(3.dp.toPx()),
    )
    val projectile =
        Path().apply {
            moveTo(centre - bodyWidth * 0.38f, size.height * 0.38f)
            quadraticTo(centre, size.height * 0.02f, centre + bodyWidth * 0.38f, size.height * 0.38f)
            close()
        }
    drawPath(projectile, copper)
    drawLine(
        Color(0xFF6B4E2E),
        Offset(centre - bodyWidth / 2f, size.height * 0.82f),
        Offset(centre + bodyWidth / 2f, size.height * 0.82f),
        2.dp.toPx(),
    )
}

private fun DrawScope.drawScopeIllustration() {
    val line = Color(0xFF94A3B8)
    val accent = Color(0xFF60A5FA)
    val y = size.height / 2f
    drawLine(line, Offset(size.width * 0.2f, y), Offset(size.width * 0.8f, y), 9.dp.toPx(), StrokeCap.Round)
    drawCircle(line, 17.dp.toPx(), Offset(size.width * 0.18f, y), style = Stroke(5.dp.toPx()))
    drawCircle(line, 13.dp.toPx(), Offset(size.width * 0.82f, y), style = Stroke(5.dp.toPx()))
    drawRoundRect(
        accent,
        topLeft = Offset(size.width * 0.45f, y - 12.dp.toPx()),
        size = Size(size.width * 0.1f, 24.dp.toPx()),
        cornerRadius =
            androidx.compose.ui.geometry
                .CornerRadius(5.dp.toPx()),
        style = Stroke(3.dp.toPx()),
    )
    drawLine(
        accent,
        Offset(size.width * 0.5f, y - 12.dp.toPx()),
        Offset(size.width * 0.5f, y - 25.dp.toPx()),
        5.dp.toPx(),
        StrokeCap.Round,
    )
}
