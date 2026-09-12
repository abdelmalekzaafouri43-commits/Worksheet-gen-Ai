package com.example

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

data class PdfDownloadResult(
    val uri: Uri,
    val fileName: String,
    val displayPath: String
)

object PdfGenerator {

    // Standard ISO 216 A4 size in PostScript points (72 points per inch)
    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = A4_WIDTH - (MARGIN * 2)

    fun generatePdf(
        context: Context,
        data: WorksheetData,
        headerLogoUri: Uri?,
        level: String,
        category: String,
        isSerif: Boolean = false,
        teacherName: String = "Mr. Zaafouri Abdelmalek",
        settings: WorksheetCustomizationSettings = WorksheetCustomizationSettings(
            font = if (isSerif) WorksheetFont.SERIF else WorksheetFont.SANS_SERIF,
            teacherName = teacherName
        )
    ): File {
        val paginatedPages = WorksheetPaginator.paginate(data)
        val pdfDocument = PdfDocument()

        paginatedPages.forEachIndexed { index, pageData ->
            val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            drawWorksheetPage(
                context = context,
                canvas = page.canvas,
                data = data,
                pageData = pageData,
                headerLogoUri = headerLogoUri,
                level = level,
                category = category,
                settings = settings
            )
            pdfDocument.finishPage(page)
        }

        val outputDir = File(context.cacheDir, "worksheets").apply { mkdirs() }
        val sanitizedTitle = data.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30)
        val outputFile = File(outputDir, "${sanitizedTitle}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawWorksheetPage(
        context: Context,
        canvas: Canvas,
        data: WorksheetData,
        pageData: PageContent,
        headerLogoUri: Uri?,
        level: String,
        category: String,
        settings: WorksheetCustomizationSettings
    ) {
        val baseTypeface = settings.font.androidTypeface
        val accent = settings.accentColor
        val layoutTemplate = settings.layoutTemplate

        // Background - crisp white A4
        canvas.drawColor(Color.WHITE)

        var currentY = MARGIN

        // Minimalist Air: Vertical accent bar on left edge
        if (layoutTemplate == WorksheetLayoutTemplate.MINIMALIST_AIR) {
            val leftBarPaint = Paint().apply {
                color = Color.parseColor(accent.primaryHex)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(16f, MARGIN, 20f, A4_HEIGHT - MARGIN), 2f, 2f, leftBarPaint)
        } else {
            // Top Decorative Accent Stripe
            val accentPaint = Paint().apply {
                color = Color.parseColor(accent.primaryHex)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + 40f, currentY + 4f), 2f, 2f, accentPaint)

            val accentLightPaint = Paint().apply {
                color = Color.parseColor(accent.borderHex)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(MARGIN + 46f, currentY, MARGIN + 70f, currentY + 4f), 2f, 2f, accentLightPaint)
        }

        currentY += 14f

        if (pageData.isFirstPage) {
            // Header Area: Logo (left) & Digital Badge (right)
            val headerTop = currentY
            var headerHeight = 0f

            // Draw Logo if available
            if (headerLogoUri != null) {
                try {
                    context.contentResolver.openInputStream(headerLogoUri)?.use { input ->
                        val bitmap = BitmapFactory.decodeStream(input)
                        if (bitmap != null) {
                            val maxW = 120f
                            val maxH = 45f
                            val scale = minOf(maxW / bitmap.width, maxH / bitmap.height)
                            val scaledW = bitmap.width * scale
                            val scaledH = bitmap.height * scale
                            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                            canvas.drawBitmap(bitmap, null, RectF(MARGIN, currentY, MARGIN + scaledW, currentY + scaledH), paint)
                            headerHeight = maxOf(headerHeight, scaledH)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val brandPaint = TextPaint().apply {
                    color = Color.parseColor(accent.primaryHex)
                    textSize = 9f
                    isFakeBoldText = true
                    letterSpacing = 0.08f
                    isAntiAlias = true
                }
                canvas.drawText("ENGLISH WORKSHEET STUDIO", MARGIN, currentY + 12f, brandPaint)
                headerHeight = maxOf(headerHeight, 18f)
            }

            // Draw Digital Access Card / QR placeholder on the right
            if (settings.showQrBadge) {
                val qrBoxW = 125f
                val qrBoxH = 40f
                val qrBoxX = A4_WIDTH - MARGIN - qrBoxW
                val qrBoxY = headerTop

                val qrBgPaint = Paint().apply {
                    color = Color.parseColor(accent.tintHex)
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawRoundRect(RectF(qrBoxX, qrBoxY, qrBoxX + qrBoxW, qrBoxY + qrBoxH), 5f, 5f, qrBgPaint)

                val qrBorderPaint = Paint().apply {
                    color = Color.parseColor(accent.borderHex)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                    isAntiAlias = true
                }
                canvas.drawRoundRect(RectF(qrBoxX, qrBoxY, qrBoxX + qrBoxW, qrBoxY + qrBoxH), 5f, 5f, qrBorderPaint)

                val qrLabelPaint = TextPaint().apply {
                    color = Color.parseColor(accent.primaryHex)
                    textSize = 7f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                canvas.drawText("ENGLISH WORKSHEET", qrBoxX + 8f, qrBoxY + 14f, qrLabelPaint)

                val qrUrlPaint = TextPaint().apply {
                    color = Color.parseColor(accent.secondaryHex)
                    textSize = 6.5f
                    isAntiAlias = true
                }
                val shortHash = Integer.toHexString(data.title.hashCode()).take(6)
                canvas.drawText("english.studio/s/$shortHash", qrBoxX + 8f, qrBoxY + 25f, qrUrlPaint)

                val qrInstructionPaint = TextPaint().apply {
                    color = Color.parseColor("#94A3B8")
                    textSize = 5.5f
                    isAntiAlias = true
                }
                canvas.drawText("Scan for interactive practice", qrBoxX + 8f, qrBoxY + 34f, qrInstructionPaint)

                headerHeight = maxOf(headerHeight, qrBoxH)
            }

            currentY = headerTop + headerHeight + 12f

            // Candidate Name & Info Header (for Formal Exam or if enabled)
            if (settings.showCandidateHeader || layoutTemplate == WorksheetLayoutTemplate.FORMAL_EXAM) {
                val boxH = 46f
                val candBg = Paint().apply {
                    color = Color.parseColor(accent.tintHex)
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + boxH), 5f, 5f, candBg)

                val candBorder = Paint().apply {
                    color = Color.parseColor(accent.borderHex)
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                    isAntiAlias = true
                }
                canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + boxH), 5f, 5f, candBorder)

                val candTextPaint = TextPaint().apply {
                    color = Color.parseColor(accent.darkTextHex)
                    textSize = 8f
                    typeface = Typeface.create(baseTypeface, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("CANDIDATE NAME: ___________________________________", MARGIN + 10f, currentY + 16f, candTextPaint)
                canvas.drawText("DATE: ________________", MARGIN + CONTENT_WIDTH - 140f, currentY + 16f, candTextPaint)

                canvas.drawText("CANDIDATE NO: __________________   CLASS: _________", MARGIN + 10f, currentY + 34f, candTextPaint)

                if (settings.showScoreBox || layoutTemplate == WorksheetLayoutTemplate.FORMAL_EXAM) {
                    val scoreBoxPaint = Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val sX = MARGIN + CONTENT_WIDTH - 110f
                    val sY = currentY + 22f
                    canvas.drawRoundRect(RectF(sX, sY, sX + 100f, sY + 18f), 3f, 3f, scoreBoxPaint)

                    val sBorder = Paint().apply {
                        color = Color.parseColor(accent.primaryHex)
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(RectF(sX, sY, sX + 100f, sY + 18f), 3f, 3f, sBorder)

                    val scoreTextPaint = TextPaint().apply {
                        color = Color.parseColor(accent.primaryHex)
                        textSize = 8f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    canvas.drawText("SCORE: ___ / 100", sX + 12f, sY + 12f, scoreTextPaint)
                }

                currentY += boxH + 12f
            }

            // Metadata Tag (e.g. ENGLISH • B1 INTERMEDIATE • GRAMMAR & SYNTAX)
            val metaPaint = TextPaint().apply {
                color = Color.parseColor(accent.secondaryHex)
                textSize = 8f
                isFakeBoldText = true
                letterSpacing = 0.08f
                isAntiAlias = true
            }
            val metaText = "ENGLISH • ${level.uppercase(Locale.ROOT)} • ${category.uppercase(Locale.ROOT)}"
            canvas.drawText(metaText, MARGIN, currentY, metaPaint)
            currentY += 14f

            // Worksheet Title
            val titlePaint = TextPaint().apply {
                color = Color.parseColor(accent.darkTextHex)
                textSize = 19f
                typeface = Typeface.create(baseTypeface, Typeface.BOLD)
                isAntiAlias = true
            }
            val titleLayout = StaticLayout.Builder.obtain(data.title, 0, data.title.length, titlePaint, CONTENT_WIDTH.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.1f)
                .build()
            canvas.save()
            canvas.translate(MARGIN, currentY)
            titleLayout.draw(canvas)
            canvas.restore()
            currentY += titleLayout.height + 4f

            // Teacher / Instructor Name
            if (settings.showTeacherAttribution && settings.teacherName.isNotBlank()) {
                val teacherPaint = TextPaint().apply {
                    color = Color.parseColor(accent.secondaryHex)
                    textSize = 9f
                    typeface = Typeface.create(baseTypeface, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("Prepared by: ${settings.teacherName}", MARGIN, currentY + 6f, teacherPaint)
                currentY += 12f
            }

            // Intro text
            if (data.intro.isNotBlank()) {
                val introPaint = TextPaint().apply {
                    color = Color.parseColor("#475569")
                    textSize = 9.5f
                    typeface = Typeface.create(baseTypeface, Typeface.NORMAL)
                    isAntiAlias = true
                }
                val introLayout = StaticLayout.Builder.obtain(data.intro, 0, data.intro.length, introPaint, CONTENT_WIDTH.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.2f)
                    .build()
                canvas.save()
                canvas.translate(MARGIN, currentY)
                introLayout.draw(canvas)
                canvas.restore()
                currentY += introLayout.height + 10f
            }

            // Horizontal dividing rule
            val rulePaint = Paint().apply {
                color = Color.parseColor(accent.borderHex)
                strokeWidth = 1f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawLine(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY, rulePaint)
            currentY += 14f
        } else {
            // Continuation Header on Page 2+
            val continuationPaint = TextPaint().apply {
                color = Color.parseColor(accent.darkTextHex)
                textSize = 12f
                typeface = Typeface.create(baseTypeface, Typeface.BOLD)
                isAntiAlias = true
            }
            val contTitle = "${data.title} (Continued)"
            canvas.drawText(contTitle.take(45), MARGIN, currentY + 10f, continuationPaint)

            val tagPaint = TextPaint().apply {
                color = Color.parseColor(accent.secondaryHex)
                textSize = 8f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }
            canvas.drawText("${level.uppercase(Locale.ROOT)} • SHEET ${pageData.pageIndex + 1} OF ${pageData.totalPages}", A4_WIDTH - MARGIN, currentY + 10f, tagPaint)

            currentY += 18f
            val rulePaint = Paint().apply {
                color = Color.parseColor(accent.borderHex)
                strokeWidth = 1f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawLine(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY, rulePaint)
            currentY += 14f
        }

        // Section Title & Content Paints
        val sectionTitlePaint = TextPaint().apply {
            color = Color.parseColor(accent.darkTextHex)
            textSize = 11.5f
            typeface = Typeface.create(baseTypeface, Typeface.BOLD)
            isAntiAlias = true
        }

        val sectionContentPaint = TextPaint().apply {
            color = Color.parseColor("#334155")
            textSize = 9f
            typeface = Typeface.create(baseTypeface, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionIndicatorPaint = Paint().apply {
            color = Color.parseColor(accent.primaryHex)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        when (layoutTemplate) {
            WorksheetLayoutTemplate.MODERN_CARDS -> {
                pageData.sections.forEachIndexed { sIdx, section ->
                    val secTitleLayout = StaticLayout.Builder.obtain(section.title, 0, section.title.length, sectionTitlePaint, (CONTENT_WIDTH - 44f).toInt())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .build()

                    val contentLayout = StaticLayout.Builder.obtain(section.content, 0, section.content.length, sectionContentPaint, (CONTENT_WIDTH - 24f).toInt())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1.2f)
                        .build()

                    val cardH = secTitleLayout.height + contentLayout.height + 24f

                    val cardBgPaint = Paint().apply {
                        color = Color.parseColor(accent.tintHex)
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + cardH), 6f, 6f, cardBgPaint)

                    val cardBorderPaint = Paint().apply {
                        color = Color.parseColor(accent.borderHex)
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + cardH), 6f, 6f, cardBorderPaint)

                    // Pill Badge for number
                    val badgePaint = Paint().apply {
                        color = Color.parseColor(accent.primaryHex)
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(RectF(MARGIN + 8f, currentY + 8f, MARGIN + 28f, currentY + 22f), 3f, 3f, badgePaint)

                    val numPaint = TextPaint().apply {
                        color = Color.WHITE
                        textSize = 7.5f
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    canvas.drawText(String.format(Locale.ROOT, "%02d", sIdx + 1), MARGIN + 18f, currentY + 18f, numPaint)

                    // Title
                    canvas.save()
                    canvas.translate(MARGIN + 34f, currentY + 8f)
                    secTitleLayout.draw(canvas)
                    canvas.restore()

                    // Content
                    canvas.save()
                    canvas.translate(MARGIN + 12f, currentY + 8f + secTitleLayout.height + 6f)
                    contentLayout.draw(canvas)
                    canvas.restore()

                    currentY += cardH + 8f
                }
            }

            WorksheetLayoutTemplate.TWO_COLUMN -> {
                val colW = (CONTENT_WIDTH - 14f) / 2f
                val colTitlePaint = TextPaint().apply {
                    color = Color.parseColor(accent.darkTextHex)
                    textSize = 10.5f
                    typeface = Typeface.create(baseTypeface, Typeface.BOLD)
                    isAntiAlias = true
                }
                val colContentPaint = TextPaint().apply {
                    color = Color.parseColor("#334155")
                    textSize = 8.5f
                    typeface = Typeface.create(baseTypeface, Typeface.NORMAL)
                    isAntiAlias = true
                }

                val pairs = pageData.sections.chunked(2)
                pairs.forEach { rowSections ->
                    var maxRowH = 0f
                    val layouts = rowSections.map { sec ->
                        val titleL = StaticLayout.Builder.obtain(sec.title, 0, sec.title.length, colTitlePaint, (colW - 14f).toInt()).build()
                        val contentL = StaticLayout.Builder.obtain(sec.content, 0, sec.content.length, colContentPaint, colW.toInt()).setLineSpacing(0f, 1.2f).build()
                        val h = titleL.height + contentL.height + 14f
                        maxRowH = maxOf(maxRowH, h)
                        Triple(titleL, contentL, h)
                    }

                    rowSections.forEachIndexed { idx, _ ->
                        val xOffset = MARGIN + idx * (colW + 14f)
                        val (titleL, contentL, _) = layouts[idx]

                        canvas.drawCircle(xOffset + 4f, currentY + 6f, 2.5f, sectionIndicatorPaint)

                        canvas.save()
                        canvas.translate(xOffset + 12f, currentY)
                        titleL.draw(canvas)
                        canvas.restore()

                        canvas.save()
                        canvas.translate(xOffset, currentY + titleL.height + 4f)
                        contentL.draw(canvas)
                        canvas.restore()
                    }

                    currentY += maxRowH + 10f
                }
            }

            WorksheetLayoutTemplate.FORMAL_EXAM -> {
                pageData.sections.forEachIndexed { sIdx, section ->
                    val secTitle = "SECTION ${sIdx + 1}: ${section.title.uppercase(Locale.ROOT)}"
                    val examTitlePaint = TextPaint().apply {
                        color = Color.parseColor(accent.darkTextHex)
                        textSize = 10.5f
                        typeface = Typeface.create(baseTypeface, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    canvas.drawText(secTitle, MARGIN, currentY + 10f, examTitlePaint)

                    val marksPaint = TextPaint().apply {
                        color = Color.parseColor(accent.primaryHex)
                        textSize = 8.5f
                        isFakeBoldText = true
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    canvas.drawText("[ 20 MARKS ]", MARGIN + CONTENT_WIDTH, currentY + 10f, marksPaint)

                    currentY += 14f
                    val linePaint = Paint().apply {
                        color = Color.parseColor(accent.borderHex)
                        strokeWidth = 0.75f
                        isAntiAlias = true
                    }
                    canvas.drawLine(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY, linePaint)
                    currentY += 6f

                    val contentLayout = StaticLayout.Builder.obtain(section.content, 0, section.content.length, sectionContentPaint, CONTENT_WIDTH.toInt())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1.25f)
                        .build()
                    canvas.save()
                    canvas.translate(MARGIN, currentY)
                    contentLayout.draw(canvas)
                    canvas.restore()
                    currentY += contentLayout.height + 12f
                }
            }

            else -> {
                // CLASSIC_STANDARD & MINIMALIST_AIR
                pageData.sections.forEach { section ->
                    canvas.drawCircle(MARGIN + 4f, currentY + 6f, 3f, sectionIndicatorPaint)

                    val secTitle = section.title
                    val secTitleLayout = StaticLayout.Builder.obtain(secTitle, 0, secTitle.length, sectionTitlePaint, (CONTENT_WIDTH - 16f).toInt())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .build()
                    canvas.save()
                    canvas.translate(MARGIN + 16f, currentY)
                    secTitleLayout.draw(canvas)
                    canvas.restore()
                    currentY += secTitleLayout.height + 5f

                    val contentLayout = StaticLayout.Builder.obtain(section.content, 0, section.content.length, sectionContentPaint, (CONTENT_WIDTH - 16f).toInt())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1.25f)
                        .build()
                    canvas.save()
                    canvas.translate(MARGIN + 16f, currentY)
                    contentLayout.draw(canvas)
                    canvas.restore()
                    currentY += contentLayout.height + 12f
                }
            }
        }

        // Student Notes / Answer Workspace (if room permits on this page)
        val remainingSpace = (A4_HEIGHT - MARGIN - 30f) - currentY
        if (settings.showNotesWorkspace && pageData.hasNotesBox && remainingSpace > 50f) {
            val boxHeight = minOf(remainingSpace - 10f, 80f)
            val notesBoxPaint = Paint().apply {
                color = Color.parseColor(accent.tintHex)
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + boxHeight), 5f, 5f, notesBoxPaint)

            val notesBorderPaint = Paint().apply {
                color = Color.parseColor(accent.borderHex)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(MARGIN, currentY, MARGIN + CONTENT_WIDTH, currentY + boxHeight), 5f, 5f, notesBorderPaint)

            val notesTitlePaint = TextPaint().apply {
                color = Color.parseColor(accent.secondaryHex)
                textSize = 7.5f
                isFakeBoldText = true
                letterSpacing = 0.05f
                isAntiAlias = true
            }
            canvas.drawText("STUDENT NOTES & WORKSPACE", MARGIN + 10f, currentY + 14f, notesTitlePaint)

            // Draw handwriting guide lines
            val linePaint = Paint().apply {
                color = Color.parseColor(accent.borderHex)
                strokeWidth = 0.75f
                isAntiAlias = true
            }
            var lineY = currentY + 28f
            while (lineY < currentY + boxHeight - 8f) {
                canvas.drawLine(MARGIN + 10f, lineY, MARGIN + CONTENT_WIDTH - 10f, lineY, linePaint)
                lineY += 16f
            }
        }

        // Running Footer
        val footerY = A4_HEIGHT - MARGIN + 12f

        val footerRulePaint = Paint().apply {
            color = Color.parseColor(accent.borderHex)
            strokeWidth = 0.75f
            isAntiAlias = true
        }
        canvas.drawLine(MARGIN, footerY - 14f, MARGIN + CONTENT_WIDTH, footerY - 14f, footerRulePaint)

        val footerPaint = TextPaint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            isAntiAlias = true
        }
        val dateStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        val teacherFooter = if (settings.showTeacherAttribution && settings.teacherName.isNotBlank()) "Teacher: ${settings.teacherName} • " else ""
        canvas.drawText("English Worksheet Studio • ${teacherFooter}$dateStr", MARGIN, footerY, footerPaint)

        val pagePaint = TextPaint().apply {
            color = Color.parseColor(accent.secondaryHex)
            textSize = 7.5f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("Sheet ${pageData.pageIndex + 1} of ${pageData.totalPages}", A4_WIDTH - MARGIN, footerY, pagePaint)
    }

    fun downloadPdfToStorage(context: Context, pdfFile: File, title: String): PdfDownloadResult? {
        return try {
            val sanitized = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(28)
            val fileName = "${sanitized}_A4.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/EnglishWorksheets")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(pdfFile).use { input ->
                        input.copyTo(out)
                    }
                }
                PdfDownloadResult(uri, fileName, "Downloads/EnglishWorksheets")
            } else {
                val targetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "EnglishWorksheets").apply { mkdirs() }
                val targetFile = File(targetDir, fileName)
                pdfFile.copyTo(targetFile, overwrite = true)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
                PdfDownloadResult(uri, fileName, "Downloads/EnglishWorksheets")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun writePdfToUri(context: Context, sourceFile: File, targetUri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(targetUri)?.use { outStream ->
                FileInputStream(sourceFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun openPdf(context: Context, uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Worksheet PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sharePdf(context: Context, pdfFile: File, title: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "English Worksheet: $title")
                putExtra(Intent.EXTRA_TEXT, "English educational worksheet prepared with English Worksheet Studio by Mr. Zaafouri Abdelmalek.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Worksheet PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printPdf(context: Context, pdfFile: File, jobName: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = android.print.PrintDocumentInfo.Builder("${jobName}_A4.pdf")
                        .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build()
                    callback?.onLayoutFinished(info, newAttributes != oldAttributes)
                }

                override fun onWrite(
                    pages: Array<out android.print.PageRange>?,
                    destination: android.os.ParcelFileDescriptor?,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onWriteCancelled()
                        return
                    }
                    try {
                        FileInputStream(pdfFile).use { input ->
                            FileOutputStream(destination?.fileDescriptor).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }
            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()
            printManager.print(jobName, adapter, printAttributes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
