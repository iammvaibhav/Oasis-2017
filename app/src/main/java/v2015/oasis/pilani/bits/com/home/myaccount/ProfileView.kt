package v2015.oasis.pilani.bits.com.home.myaccount

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import v2015.oasis.pilani.bits.com.home.R

class ProfileView : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.profile_view, container, false)
        val webview = view as WebView
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("https://bits-oasis.org/id/")
        return view
    }
}