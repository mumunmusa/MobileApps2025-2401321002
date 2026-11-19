package bg.uni_plovdiv.fmi.tapetransfer.data.repository

import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.TapeOrderDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

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

    val summary: Flow<OrderSummary> = allOrders.map { orders -> calculateSummary(orders) }

    fun ordersByStatus(status: OrderStatus): Flow<List<TapeOrderEntity>> = dao.getOrdersByStatus(status)

    fun orderById(id: Int): Flow<TapeOrderEntity?> = dao.getOrderById(id)

    suspend fun add(order: TapeOrderEntity): Long = dao.insert(order)

    suspend fun update(order: TapeOrderEntity) = dao.update(order)

    suspend fun delete(order: TapeOrderEntity) = dao.delete(order)

    companion object {

        fun calculateSummary(orders: List<TapeOrderEntity>): OrderSummary {
            val active = orders.count { it.status != OrderStatus.DONE }
            val done = orders.filter { it.status == OrderStatus.DONE }
            return OrderSummary(
                activeCount = active,
                doneCount = done.size,
                totalRevenue = done.sumOf { it.price },
            )
        }

        fun filterByQuery(orders: List<TapeOrderEntity>, query: String): List<TapeOrderEntity> {
            val text = query.trim()
            if (text.isEmpty()) return orders
            return orders.filter {
                it.clientName.contains(text, ignoreCase = true) || it.clientPhone.contains(text)
            }
        }
    }
}
