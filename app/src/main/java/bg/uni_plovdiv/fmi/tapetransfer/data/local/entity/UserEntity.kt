package bg.uni_plovdiv.fmi.tapetransfer.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val fullName: String,
    val passwordHash: String,
    val salt: String,
    val role: UserRole,
    val isApproved: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
)
