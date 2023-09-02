package ua.com.radiokot.pc.view.util

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Observes RecyclerView scroll to hide Floating Action Button
 * when scrolling down. If [fab] is disabled it won't be shown.
 */
class HideFabOnScrollListener(
    private val fab: FloatingActionButton
) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 2) {
            fab.hide()
        } else if (dy < -2 && fab.isEnabled) {
            fab.show()
        }
    }
}
