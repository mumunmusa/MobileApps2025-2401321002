package bg.uni_plovdiv.fmi.tapetransfer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discounts")
data class DiscountEntity(
    @PrimaryKey val minQuantity: Int,
    val percent: Int,
)
