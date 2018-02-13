package v2015.oasis.pilani.bits.com.home.navbar_items

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.R

class AboutFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        (activity as MainActivity).headerText.text = " ABOUT "
        (activity as MainActivity).filter.visibility = View.INVISIBLE
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        view.findViewById<TextView>(R.id.aboutText).typeface = (activity as MainActivity).typefaceBold
        return view
    }
}