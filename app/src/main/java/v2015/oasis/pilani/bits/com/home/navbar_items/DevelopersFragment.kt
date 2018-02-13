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
import android.widget.TextView
import com.squareup.picasso.Picasso
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.databinding.DeveloperItemBinding

data class DevelopersData(val image: Int, val name: String, val type: String)

class DevelopersViewHolder(val binding: DeveloperItemBinding) : RecyclerView.ViewHolder(binding.root){
    fun bindData(data: DevelopersData, typeface: Typeface){
        binding.data = data
        binding.typeface = typeface
    }
}

class DevelopersAdapter(val data: Array<DevelopersData>, val typeface: Typeface, val context: Context) : RecyclerView.Adapter<DevelopersViewHolder>(){
    override fun onBindViewHolder(holder: DevelopersViewHolder, position: Int) {
        holder.bindData(data[position], typeface)
        Picasso.with(context).load(data[position].image).fit().centerInside().into(holder.binding.photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevelopersViewHolder {
        return DevelopersViewHolder(DeveloperItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount() = data.size
}

class DevelopersFragment : Fragment(){

    lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_developers, container, false)
        view.findViewById<TextView>(R.id.departmentText).typeface = (activity as MainActivity).typefaceBold
        (activity as MainActivity).filter.visibility = View.INVISIBLE
        (activity as MainActivity).headerText.text = " DEVS "
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val data = arrayOf(DevelopersData(R.drawable.vaibhav, "Vaibhav Maheshwari", "UI/UX | Backend Developer"),
                DevelopersData(R.drawable.sombuddha, "Sammy", "Backend Developer"),
                DevelopersData(R.drawable.madhur, "Madhur Wadhwa", "UI Designer"),
                DevelopersData(R.drawable.tushy, "Tushar Goel", "REST API Developer"),
                DevelopersData(R.drawable.megh, "Megh Thakkar", "REST API Developer"),
                DevelopersData(R.drawable.laddha, "Laddha", "App Developer"),
                DevelopersData(R.drawable.annie, "Annie Rawat", "App Developer"))

        recyclerView.adapter = DevelopersAdapter(data, (activity as MainActivity).typefaceBold, activity)
        return view
    }
}