package v2015.oasis.pilani.bits.com.home.navbar_items

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.databinding.FragmentEmergencyBinding

class EmergencyFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        (activity as MainActivity).filter.visibility = View.INVISIBLE
        (activity as MainActivity).headerText.text = "EMERGENCY"

        val listener = View.OnClickListener{ v ->
            val phoneNo = (v as TextView).text
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNo")
            activity.startActivity(intent)

        }


        val binding = FragmentEmergencyBinding.inflate(inflater, container, false)
        binding.typeface = (activity as MainActivity).typefaceBold
        binding.listener = listener

        return binding.root
    }
}