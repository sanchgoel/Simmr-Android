package com.example.simmr.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrRadius
import com.example.simmr.core.designsystem.theme.SimmrSpacing
import com.example.simmr.core.designsystem.theme.SimmrStroke

@Composable
fun SimmrPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    SimmrButtonSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = SimmrColors.Coral,
        contentColor = Color.White,
        pressedAlpha = 0.85f,
        content = content,
    )
}

@Composable
fun SimmrSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    SimmrButtonSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        containerColor = SimmrColors.CreamCard,
        contentColor = SimmrColors.TextDark,
        pressedAlpha = 0.7f,
        border = BorderStroke(SimmrStroke.Regular, SimmrColors.Border),
        content = content,
    )
}

@Composable
private fun SimmrButtonSurface(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    pressedAlpha: Float,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha = when {
        !enabled -> 0.45f
        isPressed -> pressedAlpha
        else -> 1f
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(SimmrRadius.Md),
        color = containerColor.copy(alpha = alpha),
        contentColor = contentColor,
        border = border,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = SimmrSpacing.Lg, vertical = SimmrSpacing.Sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun SimmrCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(SimmrRadius.Lg),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = SimmrColors.CreamCard,
        contentColor = SimmrColors.TextDark,
        border = BorderStroke(SimmrStroke.Hairline, SimmrColors.Border),
        content = content,
    )
}

@Composable
fun SimmrSelectionChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    role: Role = Role.RadioButton,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = role,
            ),
        shape = RoundedCornerShape(SimmrRadius.Md),
        color = if (selected) SimmrColors.Tint else Color.Transparent,
        contentColor = SimmrColors.TextDark,
        border = BorderStroke(
            SimmrStroke.Regular,
            if (selected) SimmrColors.Coral else SimmrColors.Border,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = SimmrSpacing.Sm,
                vertical = SimmrSpacing.Xs,
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}
