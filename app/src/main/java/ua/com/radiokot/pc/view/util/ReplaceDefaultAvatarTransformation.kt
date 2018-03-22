package ua.com.radiokot.pc.view.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.squareup.picasso.Transformation
import ua.com.radiokot.pc.App
import ua.com.radiokot.pc.R
import ua.com.radiokot.pc.util.BitmapUtil

/**
 * Since the logo is being used as a default avatar,
 * Twitter default avatar should be replaced with it.
 * This transformation compares loaded bitmap with the default Twitter avatar
 * and if they seem equal returns logo.
 */
class ReplaceDefaultAvatarTransformation : Transformation {
    override fun key(): String = "replace_default_avatar"

    override fun transform(source: Bitmap?): Bitmap? {
        val width = source?.width ?: 0
        val height = source?.height ?: 0

        val defaultTwitterAvatar =
                Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(App.instance.resources,
                                R.drawable.default_twitter_avatar),
                        width, height, false)

        val controlPixels = arrayOf(
                0 to 0, width - 1 to height - 1,
                width / 2 to height / 2
        )

        return if (source == null
                || controlPixels.all { (x, y) ->
                    source.getPixel(x, y) ==
                            defaultTwitterAvatar.getPixel(x, y)
                }) {
            source?.recycle()
            return BitmapUtil
                    .getBitmapFromVectorDrawable(App.instance, R.drawable.default_profile_image)
        } else {
            source
        }
    }
}