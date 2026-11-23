package bg.uni_plovdiv.fmi.tapetransfer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.DiscountEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {
    @Query("SELECT * FROM prices")
    fun observePrices(): Flow<List<PriceEntity>>

    @Query("SELECT * FROM discounts ORDER BY minQuantity")
    fun observeDiscounts(): Flow<List<DiscountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<PriceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscounts(discounts: List<DiscountEntity>)

    @Update
    suspend fun updatePrice(price: PriceEntity)

    @Update
    suspend fun updateDiscount(discount: DiscountEntity)

    @Query("SELECT COUNT(*) FROM prices")
    suspend fun countPrices(): Int
}
