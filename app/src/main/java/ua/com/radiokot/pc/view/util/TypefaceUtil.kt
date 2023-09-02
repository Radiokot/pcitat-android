package ua.com.radiokot.pc.view.util

import android.graphics.Typeface
import ua.com.radiokot.pc.App

object TypefaceUtil {
    private var robotoSlabRegular: Typeface? = null
    fun getRobotoSlabRegular(): Typeface {
        return robotoSlabRegular
            ?: Typeface.createFromAsset(App.instance.assets, "fonts/RobotoSlab-Regular.ttf")
                .also { robotoSlabRegular = it }
    }

    private var condensedBold: Typeface? = null
    fun getCondensedBold(): Typeface {
        return condensedBold
            ?: (Typeface.create("sans-serif-condensed", Typeface.BOLD)
                ?: Typeface.DEFAULT)
                .also { condensedBold = it }
    }
}
