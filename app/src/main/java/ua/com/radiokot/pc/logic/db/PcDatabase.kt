package ua.com.radiokot.pc.logic.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ua.com.radiokot.pc.logic.db.dao.BookDao
import ua.com.radiokot.pc.logic.db.dao.QuoteDao
import ua.com.radiokot.pc.logic.db.dao.UserDao
import ua.com.radiokot.pc.logic.db.entities.BookEntity
import ua.com.radiokot.pc.logic.db.entities.QuoteEntity
import ua.com.radiokot.pc.logic.db.entities.UserEntity

@Database(entities = [UserEntity::class, BookEntity::class, QuoteEntity::class],
        version = 1, exportSchema = false)
abstract class PcDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val bookDao: BookDao
    abstract val quoteDao: QuoteDao
}