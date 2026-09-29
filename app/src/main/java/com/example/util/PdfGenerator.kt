package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.AccountEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {
    fun generateAccountsPdf(context: Context, accounts: List<AccountEntity>): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E1E2C")
            textSize = 20f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#3F51B5")
            textSize = 12f
            isFakeBoldText = true
        }

        var yPosition = 50f
        val leftMargin = 40f

        // Title
        canvas.drawText("Multi-Screen Gmail Lab - Account Report", leftMargin, yPosition, titlePaint)
        yPosition += 20f

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        canvas.drawText("Generated on: $dateStr | Total Accounts: ${accounts.size}", leftMargin, yPosition, subtitlePaint)
        yPosition += 35f

        // Table Header
        canvas.drawText("Screen", leftMargin, yPosition, headerPaint)
        canvas.drawText("Email Address", leftMargin + 60f, yPosition, headerPaint)
        canvas.drawText("Password", leftMargin + 260f, yPosition, headerPaint)
        canvas.drawText("IP / Proxy", leftMargin + 390f, yPosition, headerPaint)
        yPosition += 15f

        // Divider line
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawLine(leftMargin, yPosition, 555f, yPosition, linePaint)
        yPosition += 20f

        paint.textSize = 10f
        for (acc in accounts) {
            if (yPosition > 780f) {
                break
            }

            canvas.drawText("#${acc.screenId}", leftMargin, yPosition, paint)
            canvas.drawText(acc.email, leftMargin + 60f, yPosition, paint)
            canvas.drawText(acc.password, leftMargin + 260f, yPosition, paint)
            canvas.drawText(acc.ipAddress, leftMargin + 390f, yPosition, paint)

            yPosition += 22f
        }

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Gmail_Accounts_Report_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()

        return file
    }
}
