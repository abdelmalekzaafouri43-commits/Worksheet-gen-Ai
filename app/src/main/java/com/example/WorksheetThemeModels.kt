package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import android.graphics.Color as AndroidColor
import android.graphics.Typeface

enum class WorksheetFont(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val fontFamily: FontFamily,
    val androidTypeface: Typeface,
    val sampleText: String
) {
    SANS_SERIF(
        id = "sans_serif",
        displayName = "Modern Clean",
        subtitle = "Crisp, contemporary & highly legible",
        fontFamily = FontFamily.SansSerif,
        androidTypeface = Typeface.SANS_SERIF,
        sampleText = "The quick brown fox jumps over the lazy dog"
    ),
    SERIF(
        id = "serif",
        displayName = "Academic Classic",
        subtitle = "Formal university & textbook typography",
        fontFamily = FontFamily.Serif,
        androidTypeface = Typeface.SERIF,
        sampleText = "Mastering English syntax and reading comprehension"
    ),
    MONOSPACE(
        id = "monospace",
        displayName = "Technical Typewriter",
        subtitle = "Fixed-width coding & grammar drills",
        fontFamily = FontFamily.Monospace,
        androidTypeface = Typeface.MONOSPACE,
        sampleText = "Fill in: [ subject + have/has + past participle ]"
    ),
    CURSIVE(
        id = "cursive",
        displayName = "Friendly Rounded",
        subtitle = "Inviting & engaging for young learners",
        fontFamily = FontFamily.Cursive,
        androidTypeface = Typeface.create("casual", Typeface.NORMAL) ?: Typeface.SANS_SERIF,
        sampleText = "Let's learn new vocabulary and creative idioms!"
    )
}

enum class WorksheetAccentColor(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tintBackground: Color,
    val borderTint: Color,
    val darkTextColor: Color,
    val primaryHex: String,
    val secondaryHex: String,
    val tintHex: String,
    val borderHex: String,
    val darkTextHex: String
) {
    OXFORD_NAVY(
        id = "oxford_navy",
        displayName = "Oxford Collegiate",
        primaryColor = Color(0xFF1E3A8A),
        secondaryColor = Color(0xFF2563EB),
        tintBackground = Color(0xFFF0F6FF),
        borderTint = Color(0xFFBFDBFE),
        darkTextColor = Color(0xFF0F172A),
        primaryHex = "#1E3A8A",
        secondaryHex = "#2563EB",
        tintHex = "#F0F6FF",
        borderHex = "#BFDBFE",
        darkTextHex = "#0F172A"
    ),
    SLATE_CHARCOAL(
        id = "slate_charcoal",
        displayName = "Obsidian Slate",
        primaryColor = Color(0xFF334155),
        secondaryColor = Color(0xFF475569),
        tintBackground = Color(0xFFF8FAFC),
        borderTint = Color(0xFFCBD5E1),
        darkTextColor = Color(0xFF0F172A),
        primaryHex = "#334155",
        secondaryHex = "#475569",
        tintHex = "#F8FAFC",
        borderHex = "#CBD5E1",
        darkTextHex = "#0F172A"
    ),
    EMERALD_ACADEMY(
        id = "emerald_academy",
        displayName = "Emerald Forest",
        primaryColor = Color(0xFF065F46),
        secondaryColor = Color(0xFF059669),
        tintBackground = Color(0xFFECFDF5),
        borderTint = Color(0xFFA7F3D0),
        darkTextColor = Color(0xFF064E3B),
        primaryHex = "#065F46",
        secondaryHex = "#059669",
        tintHex = "#ECFDF5",
        borderHex = "#A7F3D0",
        darkTextHex = "#064E3B"
    ),
    CAMBRIDGE_CRIMSON(
        id = "cambridge_crimson",
        displayName = "Cambridge Crimson",
        primaryColor = Color(0xFF991B1B),
        secondaryColor = Color(0xFFDC2626),
        tintBackground = Color(0xFFFEF2F2),
        borderTint = Color(0xFFFECACA),
        darkTextColor = Color(0xFF7F1D1D),
        primaryHex = "#991B1B",
        secondaryHex = "#DC2626",
        tintHex = "#FEF2F2",
        borderHex = "#FECACA",
        darkTextHex = "#7F1D1D"
    ),
    ROYAL_AMETHYST(
        id = "royal_amethyst",
        displayName = "Royal Amethyst",
        primaryColor = Color(0xFF581C87),
        secondaryColor = Color(0xFF7C3AED),
        tintBackground = Color(0xFFFAF5FF),
        borderTint = Color(0xFFDDD6FE),
        darkTextColor = Color(0xFF3B0764),
        primaryHex = "#581C87",
        secondaryHex = "#7C3AED",
        tintHex = "#FAF5FF",
        borderHex = "#DDD6FE",
        darkTextHex = "#3B0764"
    ),
    WARM_PARCHMENT(
        id = "warm_parchment",
        displayName = "Warm Amber",
        primaryColor = Color(0xFF92400E),
        secondaryColor = Color(0xFFD97706),
        tintBackground = Color(0xFFFFFBEB),
        borderTint = Color(0xFFFDE68A),
        darkTextColor = Color(0xFF78350F),
        primaryHex = "#92400E",
        secondaryHex = "#D97706",
        tintHex = "#FFFBEB",
        borderHex = "#FDE68A",
        darkTextHex = "#78350F"
    ),
    TEAL_OCEAN(
        id = "teal_ocean",
        displayName = "Oceanic Teal",
        primaryColor = Color(0xFF115E59),
        secondaryColor = Color(0xFF0D9488),
        tintBackground = Color(0xFFF0FDFA),
        borderTint = Color(0xFF99F6E4),
        darkTextColor = Color(0xFF134E4A),
        primaryHex = "#115E59",
        secondaryHex = "#0D9488",
        tintHex = "#F0FDFA",
        borderHex = "#99F6E4",
        darkTextHex = "#134E4A"
    )
}

enum class WorksheetLayoutTemplate(
    val id: String,
    val displayName: String,
    val description: String,
    val iconName: String
) {
    MODERN_CARDS(
        id = "modern_cards",
        displayName = "Modern Block Cards",
        description = "Card blocks with accent badge pills & exercise borders",
        iconName = "view_agenda"
    ),
    CLASSIC_STANDARD(
        id = "classic_standard",
        displayName = "Classic Standard",
        description = "Balanced academic handout with top metadata bar & rules",
        iconName = "article"
    ),
    FORMAL_EXAM(
        id = "formal_exam",
        displayName = "Cambridge Exam Style",
        description = "Candidate info header, score tally box & mark weights",
        iconName = "assignment"
    ),
    MINIMALIST_AIR(
        id = "minimalist_air",
        displayName = "Minimalist Editorial",
        description = "Clean vertical margin accent bar, airy whitespace & dividers",
        iconName = "space_dashboard"
    ),
    TWO_COLUMN(
        id = "two_column",
        displayName = "Two-Column Compact",
        description = "High-density side-by-side exercise grid for paper efficiency",
        iconName = "view_column"
    )
}

data class WorksheetCustomizationSettings(
    val font: WorksheetFont = WorksheetFont.SANS_SERIF,
    val accentColor: WorksheetAccentColor = WorksheetAccentColor.OXFORD_NAVY,
    val layoutTemplate: WorksheetLayoutTemplate = WorksheetLayoutTemplate.MODERN_CARDS,
    val showCandidateHeader: Boolean = false,
    val showScoreBox: Boolean = false,
    val showNotesWorkspace: Boolean = true,
    val showQrBadge: Boolean = true,
    val showTeacherAttribution: Boolean = true,
    val teacherName: String = "Mr. Zaafouri Abdelmalek"
)

data class StylePreset(
    val name: String,
    val subtitle: String,
    val font: WorksheetFont,
    val accentColor: WorksheetAccentColor,
    val layoutTemplate: WorksheetLayoutTemplate,
    val showCandidateHeader: Boolean = false,
    val showScoreBox: Boolean = false
) {
    companion object {
        val PRESETS = listOf(
            StylePreset(
                name = "Cambridge Exam",
                subtitle = "Official test layout with candidate header & score box",
                font = WorksheetFont.SERIF,
                accentColor = WorksheetAccentColor.CAMBRIDGE_CRIMSON,
                layoutTemplate = WorksheetLayoutTemplate.FORMAL_EXAM,
                showCandidateHeader = true,
                showScoreBox = true
            ),
            StylePreset(
                name = "Oxford Collegiate",
                subtitle = "Elegant academic cards in classic collegiate navy",
                font = WorksheetFont.SERIF,
                accentColor = WorksheetAccentColor.OXFORD_NAVY,
                layoutTemplate = WorksheetLayoutTemplate.MODERN_CARDS
            ),
            StylePreset(
                name = "Modern Minimal",
                subtitle = "Sleek, high-contrast charcoal with vertical accent bar",
                font = WorksheetFont.SANS_SERIF,
                accentColor = WorksheetAccentColor.SLATE_CHARCOAL,
                layoutTemplate = WorksheetLayoutTemplate.MINIMALIST_AIR
            ),
            StylePreset(
                name = "Emerald Academy",
                subtitle = "Fresh forest green styling for grammar & vocabulary",
                font = WorksheetFont.SANS_SERIF,
                accentColor = WorksheetAccentColor.EMERALD_ACADEMY,
                layoutTemplate = WorksheetLayoutTemplate.CLASSIC_STANDARD
            ),
            StylePreset(
                name = "Two-Column Drill",
                subtitle = "Space-saving dual column layout for dense exercises",
                font = WorksheetFont.MONOSPACE,
                accentColor = WorksheetAccentColor.TEAL_OCEAN,
                layoutTemplate = WorksheetLayoutTemplate.TWO_COLUMN
            ),
            StylePreset(
                name = "Young Learners",
                subtitle = "Friendly rounded font with warm amber palette",
                font = WorksheetFont.CURSIVE,
                accentColor = WorksheetAccentColor.WARM_PARCHMENT,
                layoutTemplate = WorksheetLayoutTemplate.MODERN_CARDS
            )
        )
    }
}
