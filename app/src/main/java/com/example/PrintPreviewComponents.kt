package com.example

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Visual indicator placed between physical A4 sheets showing where a page break occurs.
 */
@Composable
fun PageBreakDivider(
    previousPage: Int,
    nextPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dashed cutting line with scissors
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            val dashColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .drawBehind {
                        drawLine(
                            color = dashColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
            )

            // Centered Scissors & Page Break Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "Cut / Page Break",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "PAGE BREAK • SHEET $previousPage OF $totalPages ENDS HERE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Explanatory Subtitle
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "⬇ Content below will print on Physical Sheet $nextPage (A4 210 × 297 mm)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * A single physical A4 paper sheet rendered with authentic paper margins, headers, footers,
 * and customizable theme styling (Fonts, Accent Colors, Layout Templates).
 */
@Composable
fun PhysicalA4Sheet(
    data: WorksheetData,
    pageContent: PageContent,
    headerLogoUri: Uri?,
    level: Level,
    category: Category,
    settings: WorksheetCustomizationSettings,
    showMarginGuides: Boolean = false,
    modifier: Modifier = Modifier
) {
    PhysicalA4Sheet(
        data = data,
        pageContent = pageContent,
        headerLogoUri = headerLogoUri,
        level = level,
        category = category,
        font = settings.font,
        accentColor = settings.accentColor,
        layoutTemplate = settings.layoutTemplate,
        showMarginGuides = showMarginGuides,
        showCandidateHeader = settings.showCandidateHeader,
        showScoreBox = settings.showScoreBox,
        showNotesWorkspace = settings.showNotesWorkspace,
        showQrBadge = settings.showQrBadge,
        showTeacherAttribution = settings.showTeacherAttribution,
        teacherName = settings.teacherName,
        modifier = modifier
    )
}

@Composable
fun PhysicalA4Sheet(
    data: WorksheetData,
    pageContent: PageContent,
    headerLogoUri: Uri?,
    level: Level,
    category: Category,
    font: WorksheetFont = WorksheetFont.SANS_SERIF,
    accentColor: WorksheetAccentColor = WorksheetAccentColor.OXFORD_NAVY,
    layoutTemplate: WorksheetLayoutTemplate = WorksheetLayoutTemplate.MODERN_CARDS,
    showMarginGuides: Boolean = false,
    showCandidateHeader: Boolean = false,
    showScoreBox: Boolean = false,
    showNotesWorkspace: Boolean = true,
    showQrBadge: Boolean = true,
    showTeacherAttribution: Boolean = true,
    teacherName: String = "Mr. Zaafouri Abdelmalek",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / 1.414f),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Optional 0.75-inch printable margin visual guideline
            if (showMarginGuides) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = accentColor.secondaryColor.copy(alpha = 0.4f),
                                style = Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                            )
                        }
                )
            }

            // Minimalist Air: Vertical Accent line along left edge
            if (layoutTemplate == WorksheetLayoutTemplate.MINIMALIST_AIR) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(accentColor.primaryColor)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                // Top Accent Stripe (if not minimalist air)
                if (layoutTemplate != WorksheetLayoutTemplate.MINIMALIST_AIR) {
                    Row(modifier = Modifier.padding(bottom = 10.dp)) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(4.dp)
                                .background(accentColor.primaryColor, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(4.dp)
                                .background(accentColor.borderTint, RoundedCornerShape(2.dp))
                        )
                    }
                }

                if (pageContent.isFirstPage) {
                    // Page 1 Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        if (headerLogoUri != null) {
                            AsyncImage(
                                model = headerLogoUri,
                                contentDescription = "School Logo",
                                modifier = Modifier
                                    .height(44.dp)
                                    .weight(1f, fill = false),
                                alignment = Alignment.TopStart,
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                "ENGLISH WORKSHEET STUDIO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor.primaryColor,
                                letterSpacing = 1.sp
                            )
                        }

                        // Digital access card / badge or Score Box
                        if (showQrBadge) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentColor.tintBackground,
                                border = BorderStroke(1.dp, accentColor.borderTint)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text(
                                        "A4 PHYSICAL HANDOUT",
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor.primaryColor
                                    )
                                    Text(
                                        "Sheet ${pageContent.pageIndex + 1} of ${pageContent.totalPages}",
                                        fontSize = 7.5.sp,
                                        color = accentColor.secondaryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Formal Exam Candidate Header Box
                    if (showCandidateHeader || layoutTemplate == WorksheetLayoutTemplate.FORMAL_EXAM) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.tintBackground.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, accentColor.borderTint)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "CANDIDATE NAME: ____________________________",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor.darkTextColor
                                    )
                                    Text(
                                        "DATE: ____________",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor.darkTextColor
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "CANDIDATE NO: ______________  CLASS: _________",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor.darkTextColor
                                    )
                                    if (showScoreBox || layoutTemplate == WorksheetLayoutTemplate.FORMAL_EXAM) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, accentColor.primaryColor)
                                        ) {
                                            Text(
                                                "SCORE: ___ / 100",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor.primaryColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Metadata tag
                    Text(
                        text = "ENGLISH • ${level.displayName.uppercase(Locale.ROOT)} • ${category.displayName.uppercase(Locale.ROOT)}",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor.secondaryColor,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Title
                    Text(
                        text = data.title,
                        color = accentColor.darkTextColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = font.fontFamily,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // Teacher Attribution
                    if (showTeacherAttribution && teacherName.isNotBlank()) {
                        Text(
                            text = "Prepared by: $teacherName",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor.secondaryColor,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Intro
                    if (data.intro.isNotBlank()) {
                        Text(
                            text = data.intro,
                            color = Color(0xFF475569),
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp,
                            fontFamily = font.fontFamily,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Divider rule
                    Divider(color = accentColor.borderTint.copy(alpha = 0.7f), thickness = 1.dp, modifier = Modifier.padding(bottom = 10.dp))
                } else {
                    // Continuation Header on Page 2+
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${data.title} (Continued)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor.darkTextColor,
                            fontFamily = font.fontFamily,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.tintBackground,
                            border = BorderStroke(1.dp, accentColor.borderTint)
                        ) {
                            Text(
                                text = "${level.code} • SHEET ${pageContent.pageIndex + 1} OF ${pageContent.totalPages}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor.primaryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Divider(color = accentColor.borderTint.copy(alpha = 0.7f), thickness = 1.dp, modifier = Modifier.padding(bottom = 10.dp))
                }

                // Section items on this page (Formatted according to Layout Template)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (layoutTemplate) {
                        WorksheetLayoutTemplate.MODERN_CARDS -> {
                            pageContent.sections.forEachIndexed { sIdx, section ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentColor.tintBackground.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, accentColor.borderTint)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = accentColor.primaryColor
                                                ) {
                                                    Text(
                                                        String.format(Locale.ROOT, "%02d", sIdx + 1),
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = section.title,
                                                    color = accentColor.darkTextColor,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = font.fontFamily
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = section.content,
                                            color = Color(0xFF334155),
                                            fontSize = 9.5.sp,
                                            lineHeight = 14.sp,
                                            fontFamily = font.fontFamily
                                        )
                                    }
                                }
                            }
                        }

                        WorksheetLayoutTemplate.TWO_COLUMN -> {
                            val pairs = pageContent.sections.chunked(2)
                            pairs.forEach { rowSections ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowSections.forEach { section ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(accentColor.primaryColor, CircleShape)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = section.title,
                                                    color = accentColor.darkTextColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = font.fontFamily
                                                )
                                            }
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                text = section.content,
                                                color = Color(0xFF334155),
                                                fontSize = 9.sp,
                                                lineHeight = 13.sp,
                                                fontFamily = font.fontFamily
                                            )
                                        }
                                    }
                                    if (rowSections.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        WorksheetLayoutTemplate.FORMAL_EXAM -> {
                            pageContent.sections.forEachIndexed { sIdx, section ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SECTION ${sIdx + 1}: ${section.title.uppercase(Locale.ROOT)}",
                                            color = accentColor.darkTextColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = font.fontFamily
                                        )
                                        Text(
                                            "[ 20 MARKS ]",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor.primaryColor
                                        )
                                    }
                                    Divider(color = accentColor.borderTint, thickness = 0.75.dp, modifier = Modifier.padding(vertical = 3.dp))
                                    Text(
                                        text = section.content,
                                        color = Color(0xFF1E293B),
                                        fontSize = 9.5.sp,
                                        lineHeight = 14.5.sp,
                                        fontFamily = font.fontFamily
                                    )
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }

                        else -> {
                            // CLASSIC_STANDARD and MINIMALIST_AIR
                            pageContent.sections.forEach { section ->
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(accentColor.primaryColor, CircleShape)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = section.title,
                                            color = accentColor.darkTextColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = font.fontFamily
                                        )
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = section.content,
                                        color = Color(0xFF334155),
                                        fontSize = 9.5.sp,
                                        lineHeight = 14.5.sp,
                                        fontFamily = font.fontFamily,
                                        modifier = Modifier.padding(start = 14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Student Notes & Workspace
                    if (showNotesWorkspace && pageContent.hasNotesBox) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(accentColor.tintBackground.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .border(
                                    1.dp,
                                    accentColor.borderTint,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(6.dp)
                        ) {
                            Column {
                                Text(
                                    "STUDENT NOTES & WORKSPACE",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor.secondaryColor,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Divider(color = accentColor.borderTint, thickness = 0.75.dp)
                                Spacer(Modifier.height(12.dp))
                                Divider(color = accentColor.borderTint, thickness = 0.75.dp)
                                Spacer(Modifier.height(12.dp))
                                Divider(color = accentColor.borderTint, thickness = 0.75.dp)
                            }
                        }
                    }
                }

                // Page Footer
                Divider(color = accentColor.borderTint.copy(alpha = 0.7f), thickness = 0.75.dp, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                    val teacherFooter = if (showTeacherAttribution && teacherName.isNotBlank()) "Teacher: $teacherName • " else ""
                    Text(
                        "English Worksheet Studio • ${teacherFooter}$dateStr",
                        fontSize = 7.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        "Page ${pageContent.pageIndex + 1} of ${pageContent.totalPages}",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor.secondaryColor
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Fullscreen Print Preview Dialog showing exact A4 page breaks,
 * margin guides, and direct printing controls.
 */
@Composable
fun PrintPreviewModal(
    data: WorksheetData,
    headerLogoUri: Uri?,
    level: Level,
    category: Category,
    settings: WorksheetCustomizationSettings = WorksheetCustomizationSettings(),
    customizationSettings: WorksheetCustomizationSettings = settings,
    onOpenThemeCustomizer: () -> Unit = {},
    onOpenCustomizer: () -> Unit = onOpenThemeCustomizer,
    onPrint: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    var showMarginGuides by remember { mutableStateOf(true) }
    val pages = remember(data) { WorksheetPaginator.paginate(data) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = "Print Preview",
                            tint = customizationSettings.accentColor.primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Physical Print Preview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = customizationSettings.accentColor.primaryColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "A4 Handout",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = customizationSettings.accentColor.primaryColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        "${pages.size} Physical ${if (pages.size == 1) "Sheet" else "Sheets"}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Theme Customizer Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = customizationSettings.accentColor.tintBackground,
                            border = BorderStroke(1.dp, customizationSettings.accentColor.borderTint),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenCustomizer() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = "Theme Studio",
                                    modifier = Modifier.size(16.dp),
                                    tint = customizationSettings.accentColor.primaryColor
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Theme: ${customizationSettings.accentColor.displayName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = customizationSettings.accentColor.darkTextColor
                                )
                            }
                        }

                        // Margin Guides Toggle
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (showMarginGuides) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showMarginGuides = !showMarginGuides }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Straighten,
                                    contentDescription = "Margin Guides",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (showMarginGuides) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (showMarginGuides) "Margins: On" else "Margins: Off",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (showMarginGuides) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Educational Banner
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "💡 Proofing with '${customizationSettings.layoutTemplate.displayName}' layout & '${customizationSettings.font.displayName}' typography.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Scrollable Draft Table Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0).copy(alpha = 0.7f))
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        pages.forEachIndexed { index, page ->
                            if (index > 0) {
                                PageBreakDivider(
                                    previousPage = index,
                                    nextPage = index + 1,
                                    totalPages = pages.size
                                )
                            }

                            PhysicalA4Sheet(
                                data = data,
                                pageContent = page,
                                headerLogoUri = headerLogoUri,
                                level = level,
                                category = category,
                                font = customizationSettings.font,
                                accentColor = customizationSettings.accentColor,
                                layoutTemplate = customizationSettings.layoutTemplate,
                                showMarginGuides = showMarginGuides,
                                showCandidateHeader = customizationSettings.showCandidateHeader,
                                showScoreBox = customizationSettings.showScoreBox,
                                showNotesWorkspace = customizationSettings.showNotesWorkspace,
                                showQrBadge = customizationSettings.showQrBadge,
                                showTeacherAttribution = customizationSettings.showTeacherAttribution,
                                teacherName = customizationSettings.teacherName
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Bottom Action Bar for Direct Physical Printing & Export
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPrint,
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = customizationSettings.accentColor.primaryColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Print to Physical Printer", fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = onDownload,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Download PDF")
                    }

                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }
        }
    }
}
