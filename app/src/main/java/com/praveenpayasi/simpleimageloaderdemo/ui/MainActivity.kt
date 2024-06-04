package com.praveenpayasi.simpleimageloaderdemo.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.praveenpayasi.simpleimageloaderdemo.util.networkhelper.NetworkHelper
import com.praveenpayasi.simpleimageloaderdemo.R
import com.praveenpayasi.simpleimageloaderdemo.SimpleImageLoader.imageLoader
import org.json.JSONArray
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageLoader()

        val recyclerview = findViewById<RecyclerView>(R.id.recycler)
        recyclerview.layoutManager = GridLayoutManager(this, 3)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)


        val data = ArrayList<ThumbnailDataModel>()
        val adapter = ThumbnailAdapter(data)

        val networkHelper = NetworkHelper(this)

        if (networkHelper.isNetworkConnected()) {
            thread {
                try {
                    val json =
                        URL("https://acharyaprashant.org/api/v2/content/misc/media-coverages?limit=100").readText()
                    val array = JSONArray(json)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val thumbnailObj = obj.optJSONObject("thumbnail")
                        val domain = thumbnailObj?.optString("domain")
                        val basePath = thumbnailObj?.optString("basePath")
                        val key = thumbnailObj?.optString("key")
                        val imageURL = "$domain/$basePath/0/$key"
                        data.add(ThumbnailDataModel(imageURL))
                    }
                } catch (ignored: Throwable) {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Oops!! Error is ${ignored.message} please try to relaunch app",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    recyclerview.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show()
        }

        recyclerview.adapter = adapter
    }

}

