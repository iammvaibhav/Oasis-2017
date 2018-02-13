package v2015.oasis.pilani.bits.com.home.navbar_items

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import v2015.oasis.pilani.bits.com.home.GlobalData
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.databinding.SponsorsItemBinding

data class SponsorsData(val imageUrl: String, val type: String, val name: String, val priority: String)

class SponsorsViewHolder(val binding: SponsorsItemBinding) : RecyclerView.ViewHolder(binding.root){
    fun bindData(data: SponsorsData, typeface: Typeface){
        binding.data = data
        binding.typeface = typeface
    }
}

class SponsorsAdapter(val typeface: Typeface, val context: Context) : RecyclerView.Adapter<SponsorsViewHolder>(){
    override fun onBindViewHolder(holder: SponsorsViewHolder, position: Int) {
        holder.bindData(GlobalData.sponsorsData[position], typeface)
        try {
            Picasso.with(context).load(GlobalData.sponsorsData[position].imageUrl).fit().centerInside().into(holder.binding.photo)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SponsorsViewHolder {
        return SponsorsViewHolder(SponsorsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = GlobalData.sponsorsData.size
}

class SponsorsFragment : Fragment(){

    lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_sponsors, container, false)
        (activity as MainActivity).headerText.text = " SPONSORS "
        (activity as MainActivity).filter.visibility = View.INVISIBLE
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        recyclerView.adapter = SponsorsAdapter((activity as MainActivity).typefaceBold, activity)
        return view
    }
}