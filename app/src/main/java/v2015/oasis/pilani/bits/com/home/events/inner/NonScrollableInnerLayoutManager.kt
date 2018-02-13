package v2015.oasis.pilani.bits.com.home.events.inner

import com.ramotion.garlandview.inner.InnerLayoutManager

class NonScrollableInnerLayoutManager : InnerLayoutManager(){
    override fun canScrollVertically(): Boolean {
        return false
    }
}