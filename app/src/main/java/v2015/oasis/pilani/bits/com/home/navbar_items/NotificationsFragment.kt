package v2015.oasis.pilani.bits.com.home.navbar_items

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import v2015.oasis.pilani.bits.com.home.GlobalData
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.databinding.NotificationsItemBinding
import java.util.*

data class NotificationData(val title: String, val body: String, val timeRecieved: String, val date: Date)

class NotificationViewHolder(val binding: NotificationsItemBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(data: NotificationData, typeface: Typeface){
        binding.data = data
        binding.typeface = typeface
    }
}

class NotificationsAdapter(val typeface: Typeface) : RecyclerView.Adapter<NotificationViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(NotificationsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = GlobalData.notificationsData.size

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(GlobalData.notificationsData[position], typeface)
    }
}

class NotificationsFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        try {
            GlobalData.notificationsData = GlobalData.tinyDb.getObject("notificationsData", GlobalData.notificationsDataType)
        }catch (e: NullPointerException){
            e.printStackTrace()
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = NotificationsAdapter((activity as MainActivity).typefaceBold)
        (activity as MainActivity).filter.visibility = View.INVISIBLE
        (activity as MainActivity).headerText.text = " NOTIFY "
        return view
    }
}