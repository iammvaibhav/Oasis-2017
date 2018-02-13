package v2015.oasis.pilani.bits.com.home.navbar_items

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.databinding.ContactItemBinding
import v2015.oasis.pilani.bits.com.home.databinding.ContactItemFirstBinding

interface phoneOnClickListener{
    fun onPhoneClick(phoneNo: String)
}

interface emailOnClickListener{
    fun onEmailClick(email: String)
}
data class ContactData(val photo: Int, val name: String, val department: String, val email: String, val phoneNo: String, val emailListener: View.OnClickListener, val phoneListener: View.OnClickListener)

class ContactViewHolder(val binding: ContactItemBinding) : RecyclerView.ViewHolder(binding.root){
    fun bindData(data: ContactData, typeface: Typeface){
        binding.dataItem = data
        binding.typeface = typeface
    }
}

class ContactFirstViewHolder(val binding: ContactItemFirstBinding) : RecyclerView.ViewHolder(binding.root){
    fun bindData(data: ContactData, typeface: Typeface){
        binding.dataItem = data
        binding.typeface = typeface
    }
}

class ContactAdapter(val data: Array<ContactData>, val typeface: Typeface, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ContactFirstViewHolder){
            holder.bindData(data[position], typeface)
            Picasso.with(context).load(data[position].photo).into(holder.binding.photo)
        }else if (holder is ContactViewHolder){
            holder.bindData(data[position], typeface)
            Picasso.with(context).load(data[position].photo).into(holder.binding.photo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1)
            return ContactFirstViewHolder(ContactItemFirstBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        return ContactViewHolder(ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = data.size

    override fun getItemViewType(position: Int) = if (position == 0) 1 else 2
}

class ContactFragment : Fragment(){

    lateinit var recycler_view: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_contact, container, false)

        (activity as MainActivity).filter.visibility = View.INVISIBLE
        (activity as MainActivity).headerText.text = "CONTACTS"

        recycler_view = view.findViewById(R.id.recycler_view)

        val emailListenerHelper = object : emailOnClickListener{
            override fun onEmailClick(email: String) {
                val TO = arrayOf(email)
                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.data = Uri.parse("mailto:")
                emailIntent.type = "text/plain"


                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO)
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here")

                try {
                    context.startActivity(Intent.createChooser(emailIntent, "Send mail..."))
                    Log.i("Finished email", "")
                } catch (ex: android.content.ActivityNotFoundException) {
                    Toast.makeText(context,
                            "There is no email client installed.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val phoneListenerHelper = object : phoneOnClickListener {
            override fun onPhoneClick(phoneNo: String) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$phoneNo")
                activity.startActivity(intent)
            }
        }

        val phoneListener = View.OnClickListener { v ->
            phoneListenerHelper.onPhoneClick("${(v as TextView).text}")
        }

        val emailListener = View.OnClickListener { v ->
            emailListenerHelper.onEmailClick("${(v as TextView).text}")
        }

        val data = arrayOf(ContactData(R.drawable.pcr, "Asim Shah", "Registration & Other Enquiries", "pcr@bits-oasis.org", "", emailListener, phoneListener),
                ContactData(R.drawable.controls, "Nisanth Varma", "Events, Competitions and operations", "controls@bits-oasis.org", "+91 80588 77118", emailListener, phoneListener),
                ContactData(R.drawable.sponz, "Siddhant Narula", "Sponsorship and Marketing", "sponsorship@bits-oasis.org", "+91 99822 00768", emailListener, phoneListener),
                ContactData(R.drawable.dvm, "Arjun Tyagi", "Website, App & Online Payments", "webmaster@bits-oasis.org", "+91 88750 52545", emailListener, phoneListener),
                ContactData(R.drawable.adp, "Gowtam Chandrahasa", "Art, Design & Publicity", "adp@bits-oasis.org", "+91 99820 84940", emailListener, phoneListener),
                ContactData(R.drawable.recnacc, "Arnav Kundra", "Reception and Accommodation", "recnacc@bits-oasis.org", "+91 99280 26633", emailListener, phoneListener),
                ContactData(R.drawable.prez, "Bharatha Ratna Puli", "President, Students Union", "president@bits-oasis.org", "+91 82970 39977", emailListener, phoneListener),
                ContactData(R.drawable.gensec, "Shivam Jindal", "General Secretary, Students Union", "gensec@bits-oasis.org", "+91 97170 24281", emailListener, phoneListener))

        recycler_view.layoutManager = LinearLayoutManager(activity)
        recycler_view.adapter = ContactAdapter(data, (activity as MainActivity).typefaceBold, activity)

        return view
    }
}