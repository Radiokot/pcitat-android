package ua.com.radiokot.pc.logic.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Maybe
import ua.com.radiokot.pc.logic.db.entities.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM user LIMIT 1")
    fun getFirst(): Maybe<UserEntity>

    @Insert(onConflict = REPLACE)
    fun insert(user: UserEntity)
}