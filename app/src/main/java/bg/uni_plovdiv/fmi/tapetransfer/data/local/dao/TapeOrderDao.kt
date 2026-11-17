package bg.uni_plovdiv.fmi.tapetransfer.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.OrderStatus
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TapeOrderDao {
    @Insert
    suspend fun insert(order: TapeOrderEntity): Long

    @Update
    suspend fun update(order: TapeOrderEntity)

    @Delete
    suspend fun delete(order: TapeOrderEntity)

    @Query("SELECT * FROM tape_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<TapeOrderEntity>>

    @Query("SELECT * FROM tape_orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<TapeOrderEntity>>

    @Query("SELECT * FROM tape_orders WHERE id = :id")
    fun getOrderById(id: Int): Flow<TapeOrderEntity?>
}
