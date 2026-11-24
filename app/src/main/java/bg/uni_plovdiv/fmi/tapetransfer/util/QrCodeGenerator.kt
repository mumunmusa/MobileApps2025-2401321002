package bg.uni_plovdiv.fmi.tapetransfer.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {

    fun buildContent(order: TapeOrderEntity): String {
        return listOf(
            "TapeTransfer",
            Formatters.formatOrderNumber(order.id),
            order.clientName,
            order.mediaType.displayName + " -> " + order.outputFormat.displayName,
        ).joinToString("\n")
    }

    fun generate(text: String, sizePx: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1,
        )
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            for (x in 0 until sizePx) {
                pixels[y * sizePx + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = createBitmap(sizePx, sizePx)
        bitmap.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        return bitmap
    }
}
