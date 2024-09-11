package app.sanmer.message.proxy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.sanmer.message.proxy.database.dao.LogDao
import app.sanmer.message.proxy.database.entity.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(version = 1, entities = [Log::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun log(): LogDao

    companion object Default {
        fun build(context: Context) =
            Room.databaseBuilder(
                context, AppDatabase::class.java, "mp"
            ).build()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object Provider {
        @Provides
        @Singleton
        fun AppDatabase(
            @ApplicationContext context: Context
        ) = build(context)

        @Provides
        @Singleton
        fun LogDao(db: AppDatabase) = db.log()
    }
}