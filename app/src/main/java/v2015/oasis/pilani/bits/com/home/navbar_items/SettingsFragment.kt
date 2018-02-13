package v2015.oasis.pilani.bits.com.home.navbar_items

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import v2015.oasis.pilani.bits.com.home.GlobalData.changeTimeFormatTo
import v2015.oasis.pilani.bits.com.home.GlobalData.tinyDb
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        (activity as MainActivity).filter.visibility = View.INVISIBLE
        (activity as MainActivity).headerText.text = " SETTINGS "
        binding.typeface = (activity as MainActivity).typefaceBold
        val currTimeFormat = tinyDb.getInt("timeFormat")
        if (currTimeFormat == 12){
            binding.twelve.setChecked(true, false)
        }else if (currTimeFormat == 24){
            binding.twentyFour.setChecked(true, false)
        }


        binding.twelve.setOnCheckedChangeListener { checkBox, isChecked ->
            if (isChecked) {
                binding.twentyFour.setChecked(false, true)
                changeTimeFormatTo(12)
                tinyDb.putInt("timeFormat", 12)
                binding.twelve.isClickable = false
                binding.twentyFour.isClickable = true
            }
        }

        binding.twentyFour.setOnCheckedChangeListener { checkBox, isChecked ->
            if (isChecked) {
                binding.twelve.setChecked(false, true)
                changeTimeFormatTo(24)
                tinyDb.putInt("timeFormat", 24)
                binding.twelve.isClickable = true
                binding.twentyFour.isClickable = false
            }
        }

        binding.enableNotifications.isChecked = tinyDb.getBoolean("enableNotifications")
        binding.enableNotifications.setOnCheckedChangeListener { checkBox, isChecked ->
            if (isChecked)
                tinyDb.putBoolean("enableNotifications", true)
            else
                tinyDb.putBoolean("enableNotifications", false)
        }

        binding.info.setOnClickListener {
            Toast.makeText(activity, "Get notifications regarding important notices & announcements. If you turn it off, you can still get them from Notifications page", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    //TODO("If time")
    /*
    Enable notifications for favourites
    Get favourites notification before x minutes of the event
    Fetch latest data
     * Clear All Favourites
     */

}