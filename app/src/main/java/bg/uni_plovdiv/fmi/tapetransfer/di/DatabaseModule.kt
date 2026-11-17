package bg.uni_plovdiv.fmi.tapetransfer.di

import android.content.Context
import androidx.room.Room
import bg.uni_plovdiv.fmi.tapetransfer.data.local.AppDatabase
import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.TapeOrderDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideTapeOrderDao(db: AppDatabase): TapeOrderDao = db.tapeOrderDao()
}
