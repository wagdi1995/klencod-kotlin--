package com.example.domain.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.domain.model.AccountModel
import com.example.domain.model.TransactionModel
import com.example.domain.model.VoucherModel
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points

    fun generateAccountStatementPdf(
        context: Context,
        account: AccountModel,
        transactions: List<TransactionModel>
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        // Header background banner
        paint.color = Color.parseColor("#2160E8")
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 100f, paint)

        // App & Report Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("حساباتي - كشف حساب مالي", PAGE_WIDTH - 30f, 45f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("تاريخ التقرير: ${DateFormatter.formatFullDateArabic(System.currentTimeMillis())}", PAGE_WIDTH - 30f, 75f, paint)

        // Account Details Card
        val cardPaint = Paint().apply {
            color = Color.parseColor("#F5F7FC")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#EDF1F7")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val cardRect = RectF(30f, 120f, PAGE_WIDTH - 30f, 210f)
        canvas.drawRoundRect(cardRect, 12f, 12f, cardPaint)
        canvas.drawRoundRect(cardRect, 12f, 12f, borderPaint)

        // Account info text
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#142D52")
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("الحساب: ${account.name}", PAGE_WIDTH - 50f, 150f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("التصنيف: ${account.category}  |  الهاتف: ${account.phone.ifBlank { "غير مسجل" }}", PAGE_WIDTH - 50f, 175f, paint)
        canvas.drawText("الرصيد الافتتاحي: ${CurrencyFormatter.format(account.openingBalance, account.currencyCode)} (${account.openingBalanceType.labelArabic})", PAGE_WIDTH - 50f, 195f, paint)

        // Current Balance Badge on left of card
        paint.textAlign = Paint.Align.LEFT
        paint.color = if (account.isPositive) Color.parseColor("#16A58B") else Color.parseColor("#E65B63")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("الرصيد الحالي: ${CurrencyFormatter.format(account.currentBalance, account.currencyCode)}", 50f, 165f, paint)
        paint.textSize = 12f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("حالة الرصيد: ${account.balanceStatusLabel}", 50f, 190f, paint)

        // Table Header
        var currentY = 240f
        paint.color = Color.parseColor("#142D52")
        canvas.drawRoundRect(RectF(30f, currentY, PAGE_WIDTH - 30f, currentY + 30f), 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT

        canvas.drawText("التاريخ", PAGE_WIDTH - 40f, currentY + 20f, paint)
        canvas.drawText("البيان والنوع", PAGE_WIDTH - 140f, currentY + 20f, paint)
        canvas.drawText("المبلغ", PAGE_WIDTH - 320f, currentY + 20f, paint)
        canvas.drawText("الرصيد المتراكم", 50f, currentY + 20f, paint)

        currentY += 40f

        // Table Rows
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f

        val linePaint = Paint().apply {
            color = Color.parseColor("#EDF1F7")
            strokeWidth = 1f
        }

        val rowLimit = 25
        transactions.take(rowLimit).forEachIndexed { index, trans ->
            if (index % 2 == 1) {
                paint.color = Color.parseColor("#FAFCFF")
                canvas.drawRect(30f, currentY - 14f, PAGE_WIDTH - 30f, currentY + 12f, paint)
            }

            paint.textAlign = Paint.Align.RIGHT
            paint.color = Color.parseColor("#64748B")
            canvas.drawText(DateFormatter.formatShortDate(trans.date), PAGE_WIDTH - 40f, currentY, paint)

            paint.color = Color.parseColor("#1E293B")
            canvas.drawText("${trans.title} (${trans.type.labelArabic})", PAGE_WIDTH - 140f, currentY, paint)

            paint.color = if (trans.type.isIncoming) Color.parseColor("#16A58B") else Color.parseColor("#E65B63")
            val sign = if (trans.type.isIncoming) "+" else "-"
            canvas.drawText("$sign${CurrencyFormatter.format(trans.amount, trans.currencyCode)}", PAGE_WIDTH - 320f, currentY, paint)

            paint.color = Color.parseColor("#142D52")
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(CurrencyFormatter.format(trans.runningBalance, trans.currencyCode), 50f, currentY, paint)

            canvas.drawLine(30f, currentY + 14f, PAGE_WIDTH - 30f, currentY + 14f, linePaint)
            currentY += 24f
        }

        // Footer
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 9f
        canvas.drawText("تم استخراج هذا التقرير آلياً عبر تطبيق حساباتي - جميع الحقوق محفوظة", PAGE_WIDTH / 2f, PAGE_HEIGHT - 30f, paint)

        document.finishPage(page)

        return savePdfToFile(context, document, "statement_${account.id}_${System.currentTimeMillis()}.pdf")
    }

    fun generateVoucherPdf(context: Context, voucher: VoucherModel): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Top Header
        val isReceipt = voucher.type.name == "RECEIPT"
        val headerColor = if (isReceipt) Color.parseColor("#16A58B") else Color.parseColor("#E65B63")
        paint.color = headerColor
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, paint)

        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(voucher.type.labelArabic, PAGE_WIDTH / 2f, 50f, paint)

        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("رقم السند: ${voucher.voucherNumber}", PAGE_WIDTH / 2f, 85f, paint)

        // Voucher Body Card
        val bodyRect = RectF(30f, 140f, PAGE_WIDTH - 30f, 480f)
        val fillPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(bodyRect, 16f, 16f, fillPaint)
        canvas.drawRoundRect(bodyRect, 16f, 16f, borderPaint)

        var y = 190f
        val rightX = PAGE_WIDTH - 60f
        paint.textAlign = Paint.Align.RIGHT

        fun drawField(label: String, value: String) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#64748B")
            paint.textSize = 12f
            canvas.drawText(label, rightX, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 14f
            canvas.drawText(value, rightX - 120f, y, paint)

            val lineP = Paint().apply {
                color = Color.parseColor("#F1F5F9")
                strokeWidth = 1f
            }
            canvas.drawLine(50f, y + 15f, PAGE_WIDTH - 50f, y + 15f, lineP)
            y += 45f
        }

        drawField("التاريخ:", DateFormatter.formatFullDateArabic(voucher.date))
        drawField("الحساب:", voucher.accountName)
        drawField(if (isReceipt) "استلمنا من:" else "صرفنا إلى:", voucher.personName)
        drawField("المبلغ:", CurrencyFormatter.format(voucher.amount, voucher.currencyCode))
        drawField("البيان:", voucher.description)
        if (voucher.notes.isNotBlank()) {
            drawField("ملاحظات:", voucher.notes)
        }

        // Signature Sections
        y = 560f
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("توقيع المحاسب / أمين الصندوق", PAGE_WIDTH - 120f, y, paint)
        canvas.drawText("توقيع المستلم / العميل", 120f, y, paint)

        val signLinePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.5f
        }
        canvas.drawLine(PAGE_WIDTH - 190f, y + 50f, PAGE_WIDTH - 50f, y + 50f, signLinePaint)
        canvas.drawLine(50f, y + 50f, 190f, y + 50f, signLinePaint)

        // Footer
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 9f
        canvas.drawText("تم إصدار هذا السند عبر تطبيق حساباتي", PAGE_WIDTH / 2f, PAGE_HEIGHT - 30f, paint)

        document.finishPage(page)
        return savePdfToFile(context, document, "voucher_${voucher.voucherNumber}_${System.currentTimeMillis()}.pdf")
    }

    private fun savePdfToFile(context: Context, document: PdfDocument, fileName: String): File? {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        return try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            document.close()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    fun sharePdf(context: Context, file: File, title: String = "مشاركة المستند") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر مشاركة الملف: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
