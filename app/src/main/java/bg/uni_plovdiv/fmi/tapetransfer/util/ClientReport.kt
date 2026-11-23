package bg.uni_plovdiv.fmi.tapetransfer.util

import android.content.Context
import bg.uni_plovdiv.fmi.tapetransfer.R
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity

object ClientReport {

    fun buildMessage(context: Context, order: TapeOrderEntity): String {
        val templateRes = when (order.status) {
            OrderStatus.PENDING -> R.string.report_pending
            OrderStatus.IN_PROGRESS -> R.string.report_in_progress
            OrderStatus.DONE -> R.string.report_done
        }
        return context.getString(
            templateRes,
            order.clientName,
            order.mediaType.displayName,
            order.outputFormat.displayName,
            Formatters.formatOrderNumber(order.id),
            order.durationMinutes,
            Formatters.formatPrice(order.price),
        )
    }
}
