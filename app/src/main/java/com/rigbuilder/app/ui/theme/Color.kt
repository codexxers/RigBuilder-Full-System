@file:Suppress("unused")
package com.rigbuilder.app.ui.theme

/**
 * Color constants for the RigBuilder theme.
 * These are plain ARGB integer values for use in Kotlin code.
 * XML equivalents are in res/values/colors.xml.
 */
object RigColors {
    // ── Primary Brand Colors ────────────────────────────────────
    const val RigRed            = 0xFF880808.toInt()   // Blood Red
    const val RigRedDark        = 0xFF5C0404.toInt()   // Deeper blood red
    const val RigRedLight       = 0xFFAA2020.toInt()   // Lighter blood red accent
    const val RigRedGlow        = 0x33880808       // Subtle glow overlay

    // ── Backgrounds ─────────────────────────────────────────────
    const val RigBlack          = 0xFF000000.toInt()   // Pure black
    const val RigSurface        = 0xFF0D0D0D.toInt()   // Near-black surface
    const val RigSurfaceVariant = 0xFF1A1A1A.toInt()   // Elevated surface
    const val RigCard           = 0xFF111111.toInt()   // Card background
    const val RigCardElevated   = 0xFF181818.toInt()   // Elevated card

    // ── Text ────────────────────────────────────────────────────
    const val RigWhite          = 0xFFFAF9F6.toInt()   // Off White
    const val RigGray           = 0xFFA0A0A0.toInt()   // Secondary text
    const val RigGrayDark       = 0xFF606060.toInt()   // Tertiary text
    const val RigGrayLight      = 0xFFD0D0D0.toInt()   // Bright secondary

    // ── Status Colors ───────────────────────────────────────────
    const val RigGreen          = 0xFF2E7D32.toInt()   // Deeper green
    const val RigYellow         = 0xFFE6B800.toInt()   // Gold accent
    const val RigOrange         = 0xFFE65100.toInt()   // Warning

    // ── Synergy Colors ──────────────────────────────────────────
    const val SynergyExcellent  = 0xFF2E7D32.toInt()
    const val SynergyGood       = 0xFF558B2F.toInt()
    const val SynergyAdequate   = 0xFFE6B800.toInt()
    const val SynergyNotAdvised = 0xFF880808.toInt()

    // ── Gradients ───────────────────────────────────────────────
    const val GradientRedStart  = 0xFF880808.toInt()
    const val GradientRedEnd    = 0xFF5C0404.toInt()
}
