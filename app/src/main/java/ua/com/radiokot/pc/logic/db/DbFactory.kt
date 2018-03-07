package ua.com.radiokot.pc.logic.db

import android.arch.persistence.room.Room
import ua.com.radiokot.pc.App

object DbFactory {
    private var mDatabase: PcDatabase? = null

    fun getAppDatabase(): PcDatabase {
        return mDatabase ?: Room.databaseBuilder(App.instance, PcDatabase::class.java, "pc-db")
                .build()
                .also { mDatabase = it }
    }
}