package bg.uni_plovdiv.fmi.tapetransfer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey val mediaType: MediaType,
    val pricePerHour: Double,
)
