package ua.com.radiokot.pc.logic.db.dao

import android.arch.persistence.room.*
import io.reactivex.Single
import ua.com.radiokot.pc.logic.db.entities.BookEntity

@Dao
interface BookDao {
    @Query("SELECT * FROM book ORDER BY paging_token DESC")
    fun getAll(): Single<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg books: BookEntity)

    @Query("DELETE FROM book WHERE id NOT IN (:ids)")
    fun leaveOnlyIds(vararg ids: Long?)

    @Update()
    fun update(vararg books: BookEntity)

    @Delete()
    fun delete(vararg books: BookEntity)
}