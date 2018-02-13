package v2015.oasis.pilani.bits.com.home.events

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import v2015.oasis.pilani.bits.com.home.databinding.BsLayoutBinding
import java.util.*

class bsViewHolder(val binding: BsLayoutBinding, val callback: () -> Unit) : RecyclerView.ViewHolder(binding.root) {
    fun bindData(list: HashMap<String, Boolean>, textString: String, isChecked: Boolean){
        binding.textString = textString
        binding.isChecked = isChecked
        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            list[textString] = isChecked
            callback()
        }
    }
}

class bsAdapter(var list: HashMap<String, Boolean>, var callback: () -> Unit) : RecyclerView.Adapter<bsViewHolder>(){

    val listToUse = ArrayList<Pair<String, Boolean>>()

    init {
        for ((i, j) in list)
            listToUse.add(Pair(i, j))
        Collections.sort(listToUse){o1, o2 -> o1.first.compareTo(o2.first) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): bsViewHolder {
        val binding = BsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return bsViewHolder(binding, callback)
    }

    override fun getItemCount() = listToUse.size

    override fun onBindViewHolder(holder: bsViewHolder, position: Int) {
        holder.bindData(list, listToUse[position].first, listToUse[position].second)
    }

    fun changeDatasetTo(list: HashMap<String, Boolean>, callback: () -> Unit){
        this.list = list
        this.callback = callback
        notifyDataSetChanged()
    }
}

