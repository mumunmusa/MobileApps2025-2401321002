package bg.uni_plovdiv.fmi.tapetransfer.util

import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.DiscountEntity
import kotlin.math.ceil
import kotlin.math.roundToLong

data class PriceQuote(
    val pricePerHour: Double,
    val hours: Int,
    val discountPercent: Int,
    val total: Double,
)

object PriceCalculator {

    fun startedHours(minutes: Int): Int = ceil(minutes / 60.0).toInt()

    fun discountFor(quantity: Int, discounts: List<DiscountEntity>): Int {
        val tier = discounts
            .filter { quantity >= it.minQuantity }
            .maxByOrNull { it.minQuantity }
        return tier?.percent ?: 0
    }

    fun calculate(
        pricePerHour: Double,
        minutes: Int,
        quantity: Int,
        discounts: List<DiscountEntity>,
    ): PriceQuote {
        val hours = startedHours(minutes)
        val percent = discountFor(quantity, discounts)
        val base = hours * pricePerHour
        val total = roundToCents(base * (100 - percent) / 100)
        return PriceQuote(pricePerHour, hours, percent, total)
    }

    private fun roundToCents(amount: Double): Double = (amount * 100).roundToLong() / 100.0
}
