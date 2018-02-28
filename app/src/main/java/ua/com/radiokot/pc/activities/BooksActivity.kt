package ua.com.radiokot.pc.activities

import android.os.Bundle
import android.util.Log
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import kotlinx.android.synthetic.main.activity_books.*
import org.jetbrains.anko.onClick
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.logic.AuthManager
import ua.com.radiokot.pc.logic.repository.Repositories
import ua.com.radiokot.pc.logic.repository.UserRepository
import ua.com.radiokot.pc.util.Navigator
import ua.com.radiokot.pc.util.ObservableTransformers

class BooksActivity : BaseActivity() {
    private val userRepository: UserRepository
        get() = Repositories.user()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isAuthorized()) {
            Log.i("Oleg", "It was here1!!")
            Navigator.toLoginActivity(this)
            return
        }

        setContentView(R.layout.activity_books)

        userRepository.updateIfNotFresh()
        userRepository.itemSubject
                .compose(ObservableTransformers.defaultSchedulers())
                .bindUntilEvent(lifecycle(), ActivityEvent.DESTROY)
                .subscribe {
                    user_name_text_view.text = it.name
                }

        logout_button.onClick {
            AuthManager.logOut()
        }
    }
}
