package za.co.dope.ballistics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import za.co.dope.ballistics.ui.theme.LocalDopeColors

@Composable
fun TopographicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val lineColor = LocalDopeColors.current.topographicLine
    Box(modifier = modifier.background(androidx.compose.material3.MaterialTheme.colorScheme.background)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawContourLines(lineColor)
            drawContourRings(lineColor)
        }
        content()
    }
}

private fun DrawScope.drawContourLines(lineColor: Color) {
    val strokeWidth = 1.dp.toPx()
    repeat(8) { index ->
        val y = size.height * (0.12f + index * 0.12f)
        val amplitude = size.height * (0.025f + (index % 3) * 0.009f)
        val path =
            Path().apply {
                moveTo(-size.width * 0.08f, y)
                cubicTo(
                    size.width * 0.18f,
                    y - amplitude,
                    size.width * 0.26f,
                    y + amplitude * 1.7f,
                    size.width * 0.52f,
                    y,
                )
                cubicTo(
                    size.width * 0.72f,
                    y - amplitude * 1.4f,
                    size.width * 0.88f,
                    y + amplitude,
                    size.width * 1.08f,
                    y - amplitude * 0.2f,
                )
            }
        drawPath(
            path = path,
            color = lineColor.copy(alpha = if (index % 2 == 0) 0.25f else 0.14f),
            style = Stroke(width = strokeWidth),
        )
    }
}

private fun DrawScope.drawContourRings(lineColor: Color) {
    repeat(4) { index ->
        val center =
            Offset(
                x = size.width * (0.18f + index * 0.24f),
                y = size.height * (0.28f + (index % 2) * 0.37f),
            )
        repeat(3) { ring ->
            drawOval(
                color = lineColor.copy(alpha = 0.14f),
                topLeft =
                    Offset(
                        center.x - (48 + ring * 22).dp.toPx(),
                        center.y - (20 + ring * 12).dp.toPx(),
                    ),
                size =
                    Size(
                        width = (96 + ring * 44).dp.toPx(),
                        height = (40 + ring * 24).dp.toPx(),
                    ),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}
