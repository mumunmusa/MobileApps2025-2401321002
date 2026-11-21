package bg.uni_plovdiv.fmi.tapetransfer.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Formatters {

    private val bulgarian: Locale = Locale.forLanguageTag("bg-BG")

    fun formatPrice(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(bulgarian)
        format.currency = Currency.getInstance("EUR")
        return format.format(amount)
    }

    fun formatOrderNumber(id: Int): String = "№%04d".format(id)
}
