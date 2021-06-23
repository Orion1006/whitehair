package com.example.labs.pages.lab8

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.labs.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import org.json.JSONObject


class Lab8 : Fragment() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var loginI : TextInputLayout
    lateinit var passwordI : TextInputLayout
    var token = ""

    lateinit var imgV : ImageView

    private lateinit var queue: RequestQueue
    var data = mutableMapOf<String, String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.lab8, container, false)
        queue = Volley.newRequestQueue(context)
        val loginBtn : Button = root.findViewById(R.id.login_btn)

        loginI = root.findViewById(R.id.login_input)
        passwordI =root.findViewById(R.id.password_input)

        imgV = root.findViewById(R.id.response_img)

        loginBtn.setOnClickListener{
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_NAME, "Button")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "Lab 8")}

            getToken(object : VolleyCallback {
                override fun onSuccess(result: String) {
                    getData(token)

                }
            })
        }

        return root
    }

    interface VolleyCallback {
        fun onSuccess(result: String)
        fun onFail(context: Context){
            Toast.makeText(context,"Login Failed", Toast.LENGTH_LONG).show()
        }
    }
    fun getToken(callback: VolleyCallback){
        val params = JSONObject(mapOf(
            "username" to loginI.editText!!.text.toString(),
            "password" to passwordI.editText!!.text.toString()
        ))
        val req = object : JsonObjectRequest(
            Method.POST,
            JWT_URL + AUTH,
            params,
            { response ->

                token = response.getString("access_token")
                Log.d("JWT_TOKEN", token)
                callback.onSuccess(token)
            },
            { error ->
                callback.onFail(requireContext())
                Log.d("ErrorResponseJWT", error.toString())
            }
        ){

        }
        req.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        queue.add(req)

    }



    fun getData(jwtToken: String){
        val req = object : JsonObjectRequest(
            Method.GET,
            JWT_URL + PROTECTED,
            null,
            { response ->
                data = Gson().fromJson(response.toString(), data.javaClass)
                Log.d("PROTECTED_DATA", data.toString())
                data["img"]?.let { setImg(it) }
            },
            { error ->
                Log.d("ErrorResponseJWT", error.toString())
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val auth = TOKEN_PREFIX + jwtToken
                val headers = mutableMapOf(
                    "Authorization" to auth
                )
                return headers
            }
        }
        Log.d("HEADERS", req.headers.toString())
        queue.add(req)
    }

    fun setImg(imgBase64: String){
        val decodedByte = Base64.decode(imgBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
        imgV.setImageBitmap(bitmap)
    }

    companion object{
        const val JWT_URL = "https://ildar-jwt.herokuapp.com/"
        const val TOKEN_PREFIX = "JWT "
        const val PROTECTED = "/protected"
        const val FREE = "/"
        const val AUTH = "/auth"

    }
}

