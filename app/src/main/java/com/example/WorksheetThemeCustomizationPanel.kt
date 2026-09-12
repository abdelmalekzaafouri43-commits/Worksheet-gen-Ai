package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact Quick Theme Bar placed directly in the Left Dashboard for immediate access & feedback.
 */
@Composable
fun WorksheetThemeDashboardBar(
    settings: WorksheetCustomizationSettings,
    onOpenCustomizer: () -> Unit,
    onApplyPreset: (StylePreset) -> Unit,
    onSelectFont: (WorksheetFont) -> Unit,
    onSelectColor: (WorksheetAccentColor) -> Unit,
    onSelectTemplate: (WorksheetLayoutTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, settings.accentColor.primaryColor.copy(alpha = 0.35f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = settings.accentColor.primaryColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "Style Studio",
                                tint = settings.accentColor.secondaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Worksheet Visual Style",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${settings.layoutTemplate.displayName} • ${settings.accentColor.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = settings.accentColor.secondaryColor
                        )
                    }
                }

                Button(
                    onClick = onOpenCustomizer,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = settings.accentColor.primaryColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Customize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Active Style Summary Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Layout Chip
                AssistChip(
                    onClick = onOpenCustomizer,
                    label = { Text(settings.layoutTemplate.displayName, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ViewAgenda,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = settings.accentColor.secondaryColor
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = settings.accentColor.tintBackground.copy(alpha = 0.8f),
                        labelColor = settings.accentColor.darkTextColor
                    ),
                    border = BorderStroke(1.dp, settings.accentColor.borderTint)
                )

                // Color Swatch Chip
                AssistChip(
                    onClick = onOpenCustomizer,
                    label = { Text(settings.accentColor.displayName, fontSize = 11.sp) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(settings.accentColor.primaryColor)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = settings.accentColor.tintBackground.copy(alpha = 0.8f),
                        labelColor = settings.accentColor.darkTextColor
                    ),
                    border = BorderStroke(1.dp, settings.accentColor.borderTint)
                )

                // Font Chip
                AssistChip(
                    onClick = onOpenCustomizer,
                    label = { Text(settings.font.displayName, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = settings.accentColor.secondaryColor
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = settings.accentColor.tintBackground.copy(alpha = 0.8f),
                        labelColor = settings.accentColor.darkTextColor
                    ),
                    border = BorderStroke(1.dp, settings.accentColor.borderTint)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Quick Presets Row
            Text(
                "Quick Style Presets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StylePreset.PRESETS.take(4).forEach { preset ->
                    val isSelected = settings.font == preset.font &&
                            settings.accentColor == preset.accentColor &&
                            settings.layoutTemplate == preset.layoutTemplate

                    FilterChip(
                        selected = isSelected,
                        onClick = { onApplyPreset(preset) },
                        label = { Text(preset.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(preset.accentColor.primaryColor)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = preset.accentColor.primaryColor.copy(alpha = 0.25f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            selectedLeadingIconColor = preset.accentColor.primaryColor
                        )
                    )
                }
            }
        }
    }
}

/**
 * Full Theme Customization Bottom Sheet / Dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetThemeCustomizationModal(
    settings: WorksheetCustomizationSettings,
    onDismiss: () -> Unit,
    onUpdateSettings: (WorksheetCustomizationSettings) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Presets", "Layouts", "Colors", "Typography", "Elements")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = settings.accentColor.primaryColor,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Worksheet Visual Styling",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Customize fonts, accent palettes & layout structure",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(14.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = { Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                1 -> Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp))
                                2 -> Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                                3 -> Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                                4 -> Icon(Icons.Default.DashboardCustomize, contentDescription = null, modifier = Modifier.size(16.dp))
                                else -> null
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> PresetsTab(settings, onUpdateSettings)
                    1 -> LayoutsTab(settings, onUpdateSettings)
                    2 -> ColorsTab(settings, onUpdateSettings)
                    3 -> TypographyTab(settings, onUpdateSettings)
                    4 -> ElementsTab(settings, onUpdateSettings)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Done Button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = settings.accentColor.primaryColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Apply & Close Studio", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PresetsTab(
    settings: WorksheetCustomizationSettings,
    onUpdateSettings: (WorksheetCustomizationSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Curated Professional Handout Styles",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        StylePreset.PRESETS.forEach { preset ->
            val isSelected = settings.font == preset.font &&
                    settings.accentColor == preset.accentColor &&
                    settings.layoutTemplate == preset.layoutTemplate

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onUpdateSettings(
                            settings.copy(
                                font = preset.font,
                                accentColor = preset.accentColor,
                                layoutTemplate = preset.layoutTemplate,
                                showCandidateHeader = preset.showCandidateHeader,
                                showScoreBox = preset.showScoreBox
                            )
                        )
                    },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) preset.accentColor.tintBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) preset.accentColor.primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color swatch visual
                    Surface(
                        shape = CircleShape,
                        color = preset.accentColor.primaryColor,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                preset.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) preset.accentColor.darkTextColor else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = preset.accentColor.primaryColor
                                ) {
                                    Text(
                                        "ACTIVE",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            preset.subtitle,
                            fontSize = 12.sp,
                            color = if (isSelected) preset.accentColor.secondaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "• ${preset.layoutTemplate.displayName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "• ${preset.font.displayName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayoutsTab(
    settings: WorksheetCustomizationSettings,
    onUpdateSettings: (WorksheetCustomizationSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Select Worksheet Structure & Layout",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        WorksheetLayoutTemplate.values().forEach { template ->
            val isSelected = settings.layoutTemplate == template

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onUpdateSettings(
                            settings.copy(
                                layoutTemplate = template,
                                showCandidateHeader = if (template == WorksheetLayoutTemplate.FORMAL_EXAM) true else settings.showCandidateHeader,
                                showScoreBox = if (template == WorksheetLayoutTemplate.FORMAL_EXAM) true else settings.showScoreBox
                            )
                        )
                    },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) settings.accentColor.tintBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) settings.accentColor.primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) settings.accentColor.primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                when (template) {
                                    WorksheetLayoutTemplate.MODERN_CARDS -> Icons.Default.ViewAgenda
                                    WorksheetLayoutTemplate.CLASSIC_STANDARD -> Icons.Default.Article
                                    WorksheetLayoutTemplate.FORMAL_EXAM -> Icons.Default.Assignment
                                    WorksheetLayoutTemplate.MINIMALIST_AIR -> Icons.Default.SpaceDashboard
                                    WorksheetLayoutTemplate.TWO_COLUMN -> Icons.Default.ViewColumn
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                template.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) settings.accentColor.darkTextColor else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = settings.accentColor.primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            template.description,
                            fontSize = 12.sp,
                            color = if (isSelected) settings.accentColor.secondaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorsTab(
    settings: WorksheetCustomizationSettings,
    onUpdateSettings: (WorksheetCustomizationSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Handout Accent Color Palette",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        WorksheetAccentColor.values().forEach { accent ->
            val isSelected = settings.accentColor == accent

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdateSettings(settings.copy(accentColor = accent)) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) accent.tintBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) accent.primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Swatch stack
                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(accent.primaryColor)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(accent.secondaryColor)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(accent.borderTint)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column {
                            Text(
                                accent.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) accent.darkTextColor else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Primary: ${accent.primaryHex} • Accent: ${accent.secondaryHex}",
                                fontSize = 11.sp,
                                color = if (isSelected) accent.secondaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (isSelected) {
                        Surface(
                            shape = CircleShape,
                            color = accent.primaryColor,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypographyTab(
    settings: WorksheetCustomizationSettings,
    onUpdateSettings: (WorksheetCustomizationSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Select Worksheet Typography",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        WorksheetFont.values().forEach { font ->
            val isSelected = settings.font == font

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdateSettings(settings.copy(font = font)) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) settings.accentColor.tintBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) settings.accentColor.primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                font.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSelected) settings.accentColor.darkTextColor else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                font.subtitle,
                                fontSize = 12.sp,
                                color = if (isSelected) settings.accentColor.secondaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = settings.accentColor.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Live preview box with this font
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "Exercise 1: Present Perfect Mastery",
                                fontFamily = font.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                font.sampleText,
                                fontFamily = font.fontFamily,
                                fontSize = 11.5.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElementsTab(
    settings: WorksheetCustomizationSettings,
    onUpdateSettings: (WorksheetCustomizationSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Handout Sections & Page Elements",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        // Candidate Name Box Switch
        ElementToggleRow(
            title = "Candidate / Student Name Header",
            subtitle = "Draw student name, class, date & ID boxes at top",
            icon = Icons.Default.Badge,
            isChecked = settings.showCandidateHeader,
            accentColor = settings.accentColor,
            onCheckedChange = { onUpdateSettings(settings.copy(showCandidateHeader = it)) }
        )

        // Score Tally Box Switch
        ElementToggleRow(
            title = "Grading & Score Box",
            subtitle = "Draw [ Score: ___ / 100 ] evaluation badge",
            icon = Icons.Default.Grade,
            isChecked = settings.showScoreBox,
            accentColor = settings.accentColor,
            onCheckedChange = { onUpdateSettings(settings.copy(showScoreBox = it)) }
        )

        // Notes Workspace Switch
        ElementToggleRow(
            title = "Student Notes & Workspace",
            subtitle = "Include faint handwriting lines for student draft work",
            icon = Icons.Default.EditNote,
            isChecked = settings.showNotesWorkspace,
            accentColor = settings.accentColor,
            onCheckedChange = { onUpdateSettings(settings.copy(showNotesWorkspace = it)) }
        )

        // QR Code Access Badge Switch
        ElementToggleRow(
            title = "Interactive Digital Badge & QR",
            subtitle = "Draw quick-access link for interactive practice",
            icon = Icons.Default.QrCode,
            isChecked = settings.showQrBadge,
            accentColor = settings.accentColor,
            onCheckedChange = { onUpdateSettings(settings.copy(showQrBadge = it)) }
        )

        // Teacher Attribution Switch
        ElementToggleRow(
            title = "Teacher Attribution",
            subtitle = "Show '${settings.teacherName}' in header & footer",
            icon = Icons.Default.School,
            isChecked = settings.showTeacherAttribution,
            accentColor = settings.accentColor,
            onCheckedChange = { onUpdateSettings(settings.copy(showTeacherAttribution = it)) }
        )
    }
}

@Composable
private fun ElementToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    accentColor: WorksheetAccentColor,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor.primaryColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        subtitle,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor.primaryColor
                )
            )
        }
    }
}
