package ua.com.radiokot.pc.logic.db.dao

import androidx.room.*
import io.reactivex.Single
import ua.com.radiokot.pc.logic.db.entities.QuoteEntity

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quote ORDER BY id DESC")
    fun getAll(): Single<List<QuoteEntity>>

    @Query("SELECT * FROM quote WHERE book_id = :bookId ORDER BY id DESC")
    fun getByBookId(bookId: Long): Single<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg quotes: QuoteEntity)

    @Query("DELETE FROM quote WHERE id NOT IN (:ids)")
    fun leaveOnlyIds(vararg ids: Long?)

    @Query("DELETE FROM quote WHERE book_id = :bookId AND id NOT IN (:ids)")
    fun leaveOnlyIdsForBook(bookId: Long, ids: List<Long?>)

    @Query("DELETE FROM quote WHERE book_id = :bookId")
    fun deleteFromBook(bookId: Long)

    @Update()
    fun update(vararg quotes: QuoteEntity)

    @Delete()
    fun delete(vararg quotes: QuoteEntity)

    @Query("DELETE FROM quote")
    fun deleteAll()
}