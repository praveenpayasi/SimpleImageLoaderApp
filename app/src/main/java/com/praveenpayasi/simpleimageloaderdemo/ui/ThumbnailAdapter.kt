package com.praveenpayasi.simpleimageloaderdemo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.praveenpayasi.simpleimageloaderdemo.R
import com.praveenpayasi.simpleimageloaderdemo.util.centerCrop
import com.praveenpayasi.simpleimageloaderdemo.util.fetch
import com.praveenpayasi.simpleimageloaderdemo.util.whenError
import com.praveenpayasi.simpleimageloaderdemo.util.withPlaceholder

class ThumbnailAdapter(private val list: List<ThumbnailDataModel>) :
    RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.thumbnail_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val purple = ContextCompat.getColor(holder.imageView.context, R.color.purple_700)
        val itemViewModel = list[position]
        holder.imageView.fetch(itemViewModel.thumbnailUrl) {
            centerCrop()
            withPlaceholder(R.drawable.ic_image, purple)
            whenError(R.drawable.ic_image_remove, purple)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }
}
