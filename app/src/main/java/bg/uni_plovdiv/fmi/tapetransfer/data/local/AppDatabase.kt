package bg.uni_plovdiv.fmi.tapetransfer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.TapeOrderDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.UserDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, TapeOrderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun tapeOrderDao(): TapeOrderDao

    companion object {
        const val NAME = "tapetransfer.db"
    }
}
