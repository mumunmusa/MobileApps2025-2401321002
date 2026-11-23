package bg.uni_plovdiv.fmi.tapetransfer.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.PriceDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.TapeOrderDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.UserDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.DiscountEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.PriceEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.TapeOrderEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TapeOrderEntity::class,
        PriceEntity::class,
        DiscountEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun tapeOrderDao(): TapeOrderDao

    abstract fun priceDao(): PriceDao

    companion object {
        const val NAME = "tapetransfer.db"
    }
}
