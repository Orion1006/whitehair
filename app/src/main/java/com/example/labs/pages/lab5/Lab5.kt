package com.example.labs.pages.lab5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.labs.R
import com.example.labs.pages.lab4.Lab4
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.squareup.picasso.Picasso

data class Playlist(var name: String, var songs: Int, var imgUrl: String)

class Lab5 : Fragment() {

    private lateinit var lab5ViewModel: Lab5ViewModel

    lateinit var playlistsRV: RecyclerView
    lateinit var mSpotifyAppRemote: SpotifyAppRemote
    lateinit var token: String
    var playlistsMain = mutableListOf<Playlist>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        lab5ViewModel = ViewModelProvider(this).get(Lab5ViewModel::class.java)

        val root = inflater.inflate(R.layout.lab5, container, false)



        playlistsRV = root.findViewById(R.id.albums)

        playlistsRV.layoutManager = LinearLayoutManager(context)
        playlistsRV.adapter = PlaylistsAdapter(playlistsMain)
        val reloadBtn: Button = root.findViewById(R.id.reload_btn)
        reloadBtn.setOnClickListener {
            token = Lab4.token
            val queue: RequestQueue = Volley.newRequestQueue(context);
            queue.add(playlistsRequest())
        }
        return root
    }

    class PlaylistsAdapter(private val playlists: MutableList<Playlist>) :
        RecyclerView.Adapter<PlaylistsAdapter.PlaylistsViewHolder>() {
        inner class PlaylistsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var name: TextView = itemView.findViewById(R.id.name)
            var songs: TextView = itemView.findViewById(R.id.num_songs)
            var img: ImageView = itemView.findViewById(R.id.playlist_cover)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.spotify_playlist, parent, false)
            return PlaylistsViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
            val item = playlists[position]
            holder.apply {
                songs.text = item.songs.toString()
                name.text = item.name
                Picasso.get()
                    .load(item.imgUrl)
                    .into(img)
            }
        }

        override fun getItemCount() = playlists.size

        fun updateData(newAlbums: MutableList<Playlist>){
            this.apply {
                playlists.clear()
                playlists.addAll(newAlbums)
            }
            notifyDataSetChanged()
        }
    }

    private fun playlistsRequest(): JsonObjectRequest {

        val jsonObjectRequest = object : JsonObjectRequest(Method.GET,
            Lab4.BASE_URL+"playlists",
            null,
            { response ->
                var playLists = response.getJSONArray("items")
                playlistsMain.clear()
                for (i in 0 until playLists.length()) {
                    val name = playLists.getJSONObject(i).getString("name")
                    val songs = playLists.getJSONObject(i).getJSONObject("tracks").getInt("total")
                    val img = playLists.getJSONObject(i).getJSONArray("images").getJSONObject(0).getString("url")
                    this.playlistsMain.add(Playlist(name, songs ,img))
                    playlistsRV.adapter!!.notifyDataSetChanged()
                }
            },
            { error ->
                // TODO: Handle error
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val token = token
                val auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers
            }

        }
        return jsonObjectRequest
    }
}
