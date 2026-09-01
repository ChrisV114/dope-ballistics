package za.co.dope.ballistics.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import za.co.dope.ballistics.R
import za.co.dope.ballistics.ui.theme.DopeDesignTokens
import za.co.dope.ballistics.ui.theme.LocalDopeColors

@Composable
fun DopeWordmark(
    modifier: Modifier = Modifier,
    @DrawableRes resource: Int = R.drawable.dope_wordmark_reference,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = "DOPE — Data On Previous Engagements",
        modifier = modifier,
    )
}

@Composable
fun DopeCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(DopeDesignTokens.Sizing.CardCorner),
                ).border(
                    width = DopeDesignTokens.Sizing.Border,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(DopeDesignTokens.Sizing.CardCorner),
                ).padding(DopeDesignTokens.Spacing.Card),
    ) {
        content()
    }
}

@Composable
fun StatusChip(
    label: String,
    status: DopeStatus,
    modifier: Modifier = Modifier,
) {
    val (color, icon) =
        when (status) {
            DopeStatus.READY -> DopeDesignTokens.Colors.Success to Icons.Outlined.CheckCircle
            DopeStatus.WARNING -> DopeDesignTokens.Colors.Warning to Icons.Outlined.WarningAmber
            DopeStatus.BLOCKED -> DopeDesignTokens.Colors.Error to Icons.Outlined.ErrorOutline
            DopeStatus.INFO -> LocalDopeColors.current.info to Icons.Outlined.Info
        }
    Row(
        modifier =
            modifier
                .background(color.copy(alpha = 0.13f), RoundedCornerShape(50))
                .border(1.dp, color.copy(alpha = 0.75f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = DopeDesignTokens.Spacing.Chip)
                .semantics { contentDescription = "$label status" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
fun DopePrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .heightIn(min = DopeDesignTokens.Sizing.PrimaryControlHeight)
                .semantics {
                    role = Role.Button
                    contentDescription = label
                },
        shape = RoundedCornerShape(DopeDesignTokens.Sizing.ControlCorner),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = LocalDopeColors.current.textMuted,
            ),
    ) {
        ButtonContents(label = label, icon = icon)
    }
}

@Composable
fun DopeSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            modifier
                .heightIn(min = DopeDesignTokens.Sizing.PrimaryControlHeight)
                .semantics {
                    role = Role.Button
                    contentDescription = label
                },
        shape = RoundedCornerShape(DopeDesignTokens.Sizing.ControlCorner),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        ButtonContents(label = label, icon = icon)
    }
}

@Composable
private fun RowScope.ButtonContents(
    label: String,
    icon: ImageVector?,
) {
    if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        androidx.compose.foundation.layout
            .Spacer(modifier = Modifier.size(8.dp))
    }
    Text(label.uppercase(), style = MaterialTheme.typography.labelLarge)
}

@Composable
fun DopeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    config: DopeFieldConfig = DopeFieldConfig(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().heightIn(min = DopeDesignTokens.Sizing.PrimaryControlHeight),
        label = { Text(label) },
        suffix = config.suffix?.let { { Text(it, color = LocalDopeColors.current.textMuted) } },
        readOnly = config.readOnly,
        keyboardOptions =
            if (config.numeric) {
                KeyboardOptions(keyboardType = KeyboardType.Decimal)
            } else {
                KeyboardOptions.Default
            },
        singleLine = true,
        shape = RoundedCornerShape(DopeDesignTokens.Sizing.ControlCorner),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
    )
}

@Composable
fun ResultPanel(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
    status: DopeStatus = DopeStatus.INFO,
) {
    val accent =
        when (status) {
            DopeStatus.READY -> LocalDopeColors.current.lime
            DopeStatus.WARNING -> DopeDesignTokens.Colors.Warning
            DopeStatus.BLOCKED -> DopeDesignTokens.Colors.Error
            DopeStatus.INFO -> MaterialTheme.colorScheme.secondary
        }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .border(1.dp, accent.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = LocalDopeColors.current.textMuted)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                unit,
                modifier = Modifier.padding(start = 6.dp, bottom = 7.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun LabelValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LocalDopeColors.current.textMuted)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}
