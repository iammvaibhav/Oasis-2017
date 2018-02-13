package v2015.oasis.pilani.bits.com.home.selfiecontest

import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import v2015.oasis.pilani.bits.com.home.GlobalData
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.utils.StyleToasts

class EnterDataFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_enter_data, container, false)
        val caption = view.findViewById<TextInputEditText>(R.id.caption)
        val description = view.findViewById<TextInputEditText>(R.id.description)
        val done = view.findViewById<Button>(R.id.done)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressbar)

        val imageID = arguments.getString("imageID")
        val userID = arguments.getString("userID")
        val thumbnailUrl = arguments.getString("thumbnailUrl")
        val fullSizeImageUrl = arguments.getString("fullSizeImageUrl")

        done.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            if (caption.text.isBlank()) {
                caption.error = "Caption cannot be empty."
                progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }

            if (description.text.isBlank()) {
                description.error = "Description cannot be empty"
                progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }

            if (caption.text.length > 20){
                caption.error = "Caption too long"
                progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }

            if (description.text.length > 60) {
                description.error = "Description too long"
                progressBar.visibility = View.INVISIBLE
                return@setOnClickListener
            }


            val selfie = Selfie("${caption.text}", "${description.text}",
                    thumbnailUrl, fullSizeImageUrl, 0L, ArrayList<String>(), imageID)


            FirebaseDatabase.getInstance().getReference("SelfieContest").child("People").child(userID).child("selfies").child(imageID).setValue(selfie){ _, _ ->
                FirebaseDatabase.getInstance().getReference("SelfieContest").child("Selfies").child(imageID).setValue(selfie){ _, _ ->
                    if (activity != null) {
                        val st = StyleToasts.successToast("Successfully Uploaded", activity)
                        st.setDuration(Toast.LENGTH_SHORT)
                        st.show()
                    }

                    progressBar.visibility = View.INVISIBLE
                    GlobalData.mainActivity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, SelfieMainPageFragment())?.commit()
                }
            }
        }
        return view
    }
}