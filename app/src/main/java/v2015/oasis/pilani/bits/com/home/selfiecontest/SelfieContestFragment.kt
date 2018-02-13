package v2015.oasis.pilani.bits.com.home.selfiecontest

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.login.LoginResult
import com.google.firebase.database.FirebaseDatabase
import v2015.oasis.pilani.bits.com.home.GlobalData
import v2015.oasis.pilani.bits.com.home.GlobalData.TAG
import v2015.oasis.pilani.bits.com.home.GlobalData.tinyDb
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.databinding.FragmentSelfiecontestBinding




data class People(val name: String, val email: String, val gender: String)

class SelfieContestFragment : Fragment(){

    lateinit var callbackManager: CallbackManager
    lateinit var accessTokenTracker: AccessTokenTracker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding = FragmentSelfiecontestBinding.inflate(inflater, container, false)
        val mainActivity = GlobalData.mainActivity
        mainActivity?.headerText?.text = "CONTEST"
        mainActivity?.filter?.visibility = View.INVISIBLE

        if (isLoggedIn())
            mainActivity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, SelfieMainPageFragment())?.commit()


        binding.loginButton.setReadPermissions("public_profile", "email")
        binding.loginButton.fragment = this

        binding.about.text = tinyDb.getString("aboutSelfieContest")

        callbackManager = CallbackManager.Factory.create()

        Log.e(TAG, (AccessToken.getCurrentAccessToken() != null).toString())

        binding.loginButton.setOnClickListener {
            binding.progressbar.visibility = View.VISIBLE
        }

        binding.loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, error.message)
                for(i in error.stackTrace){
                    Log.e(TAG, i.toString())
                }
            }

            override fun onSuccess(result: LoginResult) {

                val request = GraphRequest.newMeRequest(result.accessToken) { jsonObject, response ->

                    var id = ""
                    var name = ""
                    var email = ""
                    var gender = ""

                    try {
                        id = jsonObject.getString("id") ?: ""
                    } catch (e: Exception) {
                    }
                    try {
                        name = jsonObject.getString("name") ?: ""
                    } catch (e: Exception) {
                    }
                    try {
                        email = jsonObject.getString("email") ?: ""
                    } catch (e: Exception) {
                    }
                    try {
                        gender = jsonObject.getString("gender") ?: ""
                    } catch (e: Exception) { }


                    val people = People(name, email, gender)
                        FirebaseDatabase.getInstance().getReference("SelfieContest").child("People").child(id).setValue(people) { o1, o2 ->
                            binding.progressbar.visibility = View.VISIBLE
                            Log.e(TAG, result.accessToken.userId)
                            tinyDb.putString("facebook_id", result.accessToken.userId)
                            tinyDb.putBoolean("facebook_logged", true)
                            GlobalData.mainActivity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, SelfieMainPageFragment())?.commit()
                        }
                }

                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,gender")
                request.parameters = parameters
                request.executeAsync()
            }
        })

        return binding.root
    }



    fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}