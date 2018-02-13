package v2015.oasis.pilani.bits.com.home.selfiecontest

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager, val userID: String) : FragmentPagerAdapter(fragmentManager){

    override fun getItem(position: Int): Fragment{
        when(position){
            0 -> {
                val fragment = RecentImagesView()
                val arguments = Bundle()
                arguments.putString("userID", userID)
                fragment.arguments = arguments
                return fragment
            }
            1 -> {
                val fragment = TopImageView()
                val arguments = Bundle()
                arguments.putString("userID", userID)
                fragment.arguments = arguments
                return fragment
            }
            2 -> {
                val fragment = UploadedImageView()
                val arguments = Bundle()
                arguments.putString("userID", userID)
                fragment.arguments = arguments
                return fragment
            }
            else -> return RecentImagesView()
        }
    }

    override fun getCount() = 3

    override fun getPageTitle(position: Int) = when(position){
        0 -> "Recent"
        1 -> "Top"
        2 -> "My Uploads"
        else -> ""
    }
}