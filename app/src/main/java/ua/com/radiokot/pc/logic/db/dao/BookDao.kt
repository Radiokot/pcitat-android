package ua.com.radiokot.pc.logic.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Single
import ua.com.radiokot.pc.logic.db.entities.BookEntity

@Dao
interface BookDao {
    @Query("SELECT * FROM book ORDER BY display_order DESC")
    fun getAll(): Single<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg books: BookEntity)

    @Query("DELETE FROM book WHERE id NOT IN (:ids)")
    fun leaveOnlyIds(vararg ids: Long?)

    @Query("DELETE FROM book")
    fun clear()
}