package bg.uni_plovdiv.fmi.tapetransfer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tape_orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByUserId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("createdByUserId"), Index("status")],
)
data class TapeOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val clientPhone: String,
    val mediaType: MediaType,
    val outputFormat: OutputFormat,
    val durationMinutes: Int,
    val price: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val notes: String = "",
    val createdByUserId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
