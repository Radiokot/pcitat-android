package ua.com.radiokot.pc.logic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Maybe
import ua.com.radiokot.pc.logic.db.entities.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM user LIMIT 1")
    fun getFirst(): Maybe<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)
}
