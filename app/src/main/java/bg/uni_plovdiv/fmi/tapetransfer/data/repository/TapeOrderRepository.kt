package bg.uni_plovdiv.fmi.tapetransfer.data.repository

import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.TapeOrderDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

enum class RevenuePeriod {
    MONTH,
    YEAR,
    ALL,
}

data class MonthRevenue(
    val year: Int,
    val month: Int,
    val orderCount: Int,
    val total: Double,
)

data class OrderSummary(
    val activeCount: Int,
    val doneCount: Int,
    val totalRevenue: Double,
)

@Singleton
class TapeOrderRepository @Inject constructor(
    private val dao: TapeOrderDao,
) {

    val allOrders: Flow<List<TapeOrderEntity>> = dao.getAllOrders()

    fun orderById(id: Int): Flow<TapeOrderEntity?> = dao.getOrderById(id)

    suspend fun add(order: TapeOrderEntity): Long = dao.insert(order)

    suspend fun update(order: TapeOrderEntity) = dao.update(order)

    suspend fun delete(order: TapeOrderEntity) = dao.delete(order)

    suspend fun moveToNextStatus(order: TapeOrderEntity) {
        val next = nextStatus(order.status) ?: return
        val completedAt = if (next == OrderStatus.DONE) System.currentTimeMillis() else order.completedAt
        dao.update(order.copy(status = next, completedAt = completedAt))
    }

    companion object {

        fun nextStatus(status: OrderStatus): OrderStatus? {
            return when (status) {
                OrderStatus.PENDING -> OrderStatus.IN_PROGRESS
                OrderStatus.IN_PROGRESS -> OrderStatus.DONE
                OrderStatus.DONE -> null
            }
        }

        fun calculateSummary(
            orders: List<TapeOrderEntity>,
            period: RevenuePeriod = RevenuePeriod.ALL,
            now: Long = System.currentTimeMillis(),
        ): OrderSummary {
            val active = orders.count { it.status != OrderStatus.DONE }
            val done = orders.filter { it.status == OrderStatus.DONE && isInPeriod(it, period, now) }
            return OrderSummary(
                activeCount = active,
                doneCount = done.size,
                totalRevenue = done.sumOf { it.price },
            )
        }

        fun isInPeriod(order: TapeOrderEntity, period: RevenuePeriod, now: Long): Boolean {
            val orderDate = Calendar.getInstance()
            orderDate.timeInMillis = order.completedAt ?: order.createdAt
            val today = Calendar.getInstance()
            today.timeInMillis = now

            val sameYear = orderDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            val sameMonth = orderDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)

            return when (period) {
                RevenuePeriod.MONTH -> sameYear && sameMonth
                RevenuePeriod.YEAR -> sameYear
                RevenuePeriod.ALL -> true
            }
        }

        fun revenueByMonth(orders: List<TapeOrderEntity>): List<MonthRevenue> {
            val doneOrders = orders.filter { it.status == OrderStatus.DONE }
            val groups = doneOrders.groupBy { order ->
                val date = Calendar.getInstance()
                date.timeInMillis = order.completedAt ?: order.createdAt
                Pair(date.get(Calendar.YEAR), date.get(Calendar.MONTH))
            }
            return groups
                .map { (key, monthOrders) ->
                    MonthRevenue(
                        year = key.first,
                        month = key.second,
                        orderCount = monthOrders.size,
                        total = monthOrders.sumOf { it.price },
                    )
                }
                .sortedWith(compareByDescending<MonthRevenue> { it.year }.thenByDescending { it.month })
        }
    }
}
