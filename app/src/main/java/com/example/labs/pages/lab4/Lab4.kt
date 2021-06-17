package com.example.labs.pages.lab4

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.labs.R
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE


class Lab4 : Fragment() {
    private lateinit var lab4ViewModel: Lab4ViewModel


    lateinit var mSpotifyAppRemote: SpotifyAppRemote
    private lateinit var tokenView: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        lab4ViewModel = ViewModelProvider(this).get(Lab4ViewModel::class.java)

        val root = inflater.inflate(R.layout.lab4, container, false)

        tokenView = root.findViewById(R.id.token)
        val signInBtn: Button = root.findViewById(R.id.sign_in_button)
        signInBtn.setOnClickListener {
            auth()
        }

        return root
    }


    private fun auth() {
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(context, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d("MainActivity", "Connected! Yay!")
                    // Now you can start interacting with App Remote
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MainActivity", throwable.message, throwable)

                    // Something went wrong when attempting to connect! Handle errors here
                }
            })
        val builder = AuthorizationRequest.Builder(CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI)

        builder.setScopes(SCOPES)

        val request = builder.build()

        val intent = AuthorizationClient.createLoginActivityIntent(activity, request)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun connected() {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        //super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    token = response.accessToken
                    tokenView.text = token
                }
                AuthorizationResponse.Type.ERROR -> {
                    //
                }
                AuthorizationResponse.Type.EMPTY -> {

                }
                else -> {

                }
            }
        }
    }


    companion object {
        const val BASE_URL = "https://api.spotify.com/v1/me/"
        private const val CLIENT_ID = "74a396e640414fafbab2a91422d04705"
        private const val REDIRECT_URI = "http://com.example.labs/callback"
        private val SCOPES = arrayOf("streaming", "playlist-read-private")
        var token : String = ""
    }
}

