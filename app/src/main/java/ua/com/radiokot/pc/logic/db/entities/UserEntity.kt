package ua.com.radiokot.pc.logic.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import ua.com.radiokot.pc.logic.model.User

@Entity(tableName = "user")
data class UserEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Long? = null,
        @ColumnInfo(name = "email")
        var email: String? = null,
        @ColumnInfo(name = "name")
        var name: String? = null,
        @ColumnInfo(name = "twitter_book_id")
        var twitterBookId: Long? = null,
        @ColumnInfo(name = "twitter_account")
        var twitterAccount: String? = null,
        @ColumnInfo(name = "key")
        var authKey: String? = null
) {
    companion object {
        fun fromUser(user: User): UserEntity {
            return user.let {
                UserEntity(it.id, it.email, it.name,
                        it.twitterIntegration?.bookId, it.twitterIntegration?.username,
                        it.authKey)
            }
        }
    }

    fun toUser(): User {
        return User(id, email, name,
                User.TwitterIntegration(twitterAccount, twitterBookId), authKey)
    }
}