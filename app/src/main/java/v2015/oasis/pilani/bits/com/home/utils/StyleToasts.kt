package v2015.oasis.pilani.bits.com.home.utils

import android.content.Context
import android.graphics.Color
import com.muddzdev.styleabletoastlibrary.StyleableToast
import v2015.oasis.pilani.bits.com.home.R

object StyleToasts{

    fun errorToast(error: String = "Error", context: Context) = StyleableToast
                .Builder(context)
                .text(error)
                .textColor(Color.WHITE)
                .backgroundColor(Color.RED)
                .icon(R.drawable.ic_error)
                .build()

    fun successToast(success: String = "Success", context: Context) = StyleableToast
            .Builder(context)
            .text(success)
            .textColor(Color.WHITE)
            .backgroundColor(Color.GREEN)
            .icon(R.drawable.ic_done)
            .build()

    fun loadingToast(loading: String = "Loading", context: Context) = StyleableToast
            .Builder(context)
            .text(loading)
            .textColor(Color.BLACK)
            .backgroundColor(Color.YELLOW)
            .icon(R.drawable.ic_sync)
            .spinIcon()
            .build()

    fun likedToast(loading: String = "Liked", context: Context) = StyleableToast
            .Builder(context)
            .text(loading)
            .textColor(Color.WHITE)
            .backgroundColor(Color.parseColor("DA4336"))
            .build()

}