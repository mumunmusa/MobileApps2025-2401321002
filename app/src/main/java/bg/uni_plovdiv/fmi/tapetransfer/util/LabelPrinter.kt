package bg.uni_plovdiv.fmi.tapetransfer.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import androidx.print.PrintHelper
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity

object LabelPrinter {

    private const val LABEL_WIDTH = 600
    private const val LABEL_HEIGHT = 820
    private const val QR_SIZE = 520

    fun print(activity: Activity, order: TapeOrderEntity) {
        val printHelper = PrintHelper(activity)
        printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
        val jobName = "TapeTransfer " + Formatters.formatOrderNumber(order.id)
        printHelper.printBitmap(jobName, createLabel(order))
    }

    fun createLabel(order: TapeOrderEntity): Bitmap {
        val label = createBitmap(LABEL_WIDTH, LABEL_HEIGHT)
        val canvas = Canvas(label)
        canvas.drawColor(Color.WHITE)

        val qr = QrCodeGenerator.generate(QrCodeGenerator.buildContent(order), QR_SIZE)
        val qrLeft = (LABEL_WIDTH - QR_SIZE) / 2f
        canvas.drawBitmap(qr, qrLeft, 30f, null)

        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        numberPaint.color = Color.BLACK
        numberPaint.textSize = 64f
        numberPaint.typeface = Typeface.DEFAULT_BOLD
        numberPaint.textAlign = Paint.Align.CENTER

        val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        infoPaint.color = Color.DKGRAY
        infoPaint.textSize = 36f
        infoPaint.textAlign = Paint.Align.CENTER

        val centerX = LABEL_WIDTH / 2f
        canvas.drawText(Formatters.formatOrderNumber(order.id), centerX, 640f, numberPaint)
        canvas.drawText(order.clientName, centerX, 700f, infoPaint)
        canvas.drawText(
            order.mediaType.displayName + " → " + order.outputFormat.displayName,
            centerX,
            750f,
            infoPaint,
        )
        return label
    }
}
