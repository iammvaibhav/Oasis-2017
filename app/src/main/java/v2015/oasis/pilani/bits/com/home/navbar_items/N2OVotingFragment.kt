package v2015.oasis.pilani.bits.com.home.navbar_items

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import v2015.oasis.pilani.bits.com.home.GlobalData.N2OVotingData
import v2015.oasis.pilani.bits.com.home.GlobalData.tinyDb
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.databinding.FragmentN2oVotingBinding
import v2015.oasis.pilani.bits.com.home.databinding.N2oItemBinding
import v2015.oasis.pilani.bits.com.home.events.IfOpenThenCloseListener
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

interface onItemClicked{
    fun onItemClicked(data: N2OData)
}

data class N2OData(val name: String, val imageURL: String, val description: String, val votes: Long, val key: String)

class N2OVotingViewHolder(val binding: N2oItemBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(data: N2OData, listener: onItemClicked, iVoted: String){
        binding.textString = data.name
        binding.favourite.isChecked = iVoted == data.key
        binding.favourite.setOnCheckStateChangeListener { view, checked ->
            binding.favourite.isChecked = !checked
            listener.onItemClicked(data)
        }
        binding.root.setOnClickListener { listener.onItemClicked(data) }
    }
}

class N2OVotingAdapter(val listener: onItemClicked, val iVoted: String) : RecyclerView.Adapter<N2OVotingViewHolder>(){
    override fun onBindViewHolder(holder: N2OVotingViewHolder, position: Int) {
        holder.bind(N2OVotingData[position], listener, iVoted)
    }

    override fun getItemCount() = N2OVotingData.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = N2OVotingViewHolder(N2oItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

}

class N2OVotingFragment : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentN2oVotingBinding.inflate(inflater, container, false)
        (activity as MainActivity).filter.visibility = View.INVISIBLE
        (activity as MainActivity).headerText.text = " VOTING "

        val bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheetParent)
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

        binding.recyclerView.visibility = if (tinyDb.getBoolean("isN2OAvailable")) View.VISIBLE else View.INVISIBLE
        binding.status.visibility = if (tinyDb.getBoolean("isN2OAvailable")) View.INVISIBLE else View.VISIBLE
        binding.status.text = tinyDb.getString("N2OStatus")
        val listener = object : onItemClicked {
            override fun onItemClicked(data: N2OData) {
                //Show bottom sheet
                binding.data = data
                binding.favourite.isChecked = (data.key == tinyDb.getString("iVoted"))
                Picasso.with(activity).load(data.imageURL).fit().centerInside().into(binding.image)
                bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        (activity as MainActivity).closeListenerSelfie = object : IfOpenThenCloseListener {
            override fun ifOpenThenClose(): Boolean {
                if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
                    return true
                }
                return false
            }
        }

        binding.favourite.setOnCheckStateChangeListener { view, checked ->
            Thread{
                if (isOnline()){
                    if (binding.data != null){
                            val reference = FirebaseDatabase.getInstance().getReference("N2O")
                            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError?) {}

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (checked){
                                        try {
                                            val prevKey = tinyDb.getString("iVoted")
                                            if (prevKey != ""){
                                                val prevKeyVotes = snapshot.child(prevKey).child("votes").value as Long
                                                reference.child(prevKey).child("votes").setValue(prevKeyVotes - 1)
                                                val currKey = binding.data?.key
                                                val currKeyVotes = snapshot.child(currKey).child("votes").value as Long
                                                reference.child(currKey).child("votes").setValue(currKeyVotes + 1)
                                                tinyDb.putString("iVoted", currKey)
                                                binding.recyclerView.adapter = N2OVotingAdapter(listener, currKey ?: "")
                                            }else{
                                                val currKey = binding.data?.key
                                                val currKeyVotes = snapshot.child(currKey).child("votes").value as Long
                                                reference.child(currKey).child("votes").setValue(currKeyVotes + 1)
                                                tinyDb.putString("iVoted", currKey)
                                                binding.recyclerView.adapter = N2OVotingAdapter(listener, currKey ?: "")
                                            }
                                        }catch (e: Exception){
                                            view.post { Toast.makeText(activity, "Error! Please try again", Toast.LENGTH_LONG).show() }
                                        }
                                    }else{
                                        try{
                                            val prevKey = tinyDb.getString("iVoted")
                                            val prevKeyVotes = snapshot.child(prevKey).child("votes").value as Long
                                            reference.child(prevKey).child("votes").setValue(prevKeyVotes - 1)
                                            tinyDb.putString("iVoted", "")
                                            binding.recyclerView.adapter = N2OVotingAdapter(listener, "")
                                        }catch (e: Exception){
                                            view.post { Toast.makeText(activity, "Error! Please try again", Toast.LENGTH_LONG).show() }
                                        }
                                    }
                                }
                            })
                    }else{
                        view.post { Toast.makeText(activity, "Error! Please try again", Toast.LENGTH_LONG).show() }
                    }
                }else{
                    view.post { Toast.makeText(activity, "Cannot Vote! Please make sure that you are connected to internet", Toast.LENGTH_LONG).show() }
                }
            }.start()
        }

        binding.close.setOnClickListener{
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = N2OVotingAdapter(listener, tinyDb.getString("iVoted"))


        return binding.root
    }

    fun isOnline(): Boolean {
        try {
            val timeoutMs = 1500
            val sock = Socket()
            val sockaddr = InetSocketAddress("8.8.8.8", 53)
            sock.connect(sockaddr, timeoutMs)
            sock.close()

        } catch (e: IOException) {
            return false
        }
        return true
    }
}