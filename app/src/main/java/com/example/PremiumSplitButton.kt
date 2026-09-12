package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium Split Button UI component adhering to Material 3 principles.
 * Features a primary execution block on the left and an options/modifier toggle on the right,
 * separated by an elegant hairline divider.
 */
@Composable
fun PremiumSplitButton(
    mainText: String,
    modifier: Modifier = Modifier,
    mainIcon: ImageVector? = null,
    subText: String? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    onMainClick: () -> Unit,
    dropdownContent: @Composable (closeMenu: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "ChevronRotation")

    val shape = RoundedCornerShape(14.dp)
    val effectiveContainerColor = if (enabled) containerColor else containerColor.copy(alpha = 0.38f)
    val effectiveContentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.38f)

    Surface(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(shape)
            .border(
                width = 1.dp,
                color = effectiveContentColor.copy(alpha = 0.15f),
                shape = shape
            ),
        shape = shape,
        color = effectiveContainerColor,
        contentColor = effectiveContentColor,
        shadowElevation = if (enabled) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Action segment
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable(
                        enabled = enabled && !isLoading,
                        role = Role.Button,
                        onClick = onMainClick
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = effectiveContentColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Generating...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = effectiveContentColor
                    )
                } else {
                    if (mainIcon != null) {
                        Icon(
                            imageVector = mainIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = effectiveContentColor
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = mainText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = effectiveContentColor,
                            letterSpacing = 0.2.sp
                        )
                        if (subText != null) {
                            Text(
                                text = subText,
                                fontSize = 10.5.sp,
                                color = effectiveContentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Hairline vertical separator
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .background(effectiveContentColor.copy(alpha = 0.25f))
            )

            // Trailing options segment
            Box {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(52.dp)
                        .clickable(
                            enabled = enabled && !isLoading,
                            role = Role.Button,
                            onClick = { expanded = !expanded }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Generation Options",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(chevronRotation),
                        tint = effectiveContentColor
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    dropdownContent { expanded = false }
                }
            }
        }
    }
}

/**
 * Compact split button designed for toolbar / header actions like PDF Export & Print.
 */
@Composable
fun CompactSplitButton(
    text: String,
    icon: ImageVector,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dropdownItems: @Composable (closeMenu: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(10.dp)

    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                shape = shape
            ),
        shape = shape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Main Button
            Row(
                modifier = Modifier
                    .clickable(enabled = enabled, onClick = onPrimaryClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(text, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }

            // Hairline separator
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = 6.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            )

            // Trailing Chevron
            Box {
                Box(
                    modifier = Modifier
                        .clickable(enabled = enabled) { expanded = !expanded }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "More",
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    dropdownItems { expanded = false }
                }
            }
        }
    }
}
