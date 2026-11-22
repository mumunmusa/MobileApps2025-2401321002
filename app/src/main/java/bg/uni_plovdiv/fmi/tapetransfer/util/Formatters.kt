package bg.uni_plovdiv.fmi.tapetransfer.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

object Formatters {

    private val bulgarian: Locale = Locale.forLanguageTag("bg-BG")

    fun formatPrice(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(bulgarian)
        format.currency = Currency.getInstance("EUR")
        return format.format(amount)
    }

    fun formatOrderNumber(id: Int): String = "№%04d".format(id)

    fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("dd.MM.yyyy, HH:mm", bulgarian)
        return format.format(Date(timestamp))
    }
}
