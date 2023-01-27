package ua.com.radiokot.pc.logic.db

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ua.com.radiokot.pc.App

object DbFactory {
    private var mDatabase: PcDatabase? = null

    fun getAppDatabase(): PcDatabase {
        return mDatabase ?: Room.databaseBuilder(App.instance, PcDatabase::class.java, "pc-db")
                .addMigrations(object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.apply {
                            execSQL("DELETE FROM `quote`")
                            execSQL("ALTER TABLE `quote` ADD COLUMN `is_public` INTEGER NOT NULL")
                        }
                    }
                })
                .build()
                .also { mDatabase = it }
    }
}