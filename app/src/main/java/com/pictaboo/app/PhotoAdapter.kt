package com.pictaboo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Import Glide
import com.pictaboo.app.R // Pastikan R diimpor

class PhotoAdapter(private val photoList: List<PhotoModel>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_project, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photoList[position]

        // Menggunakan Glide untuk memuat gambar dari URL Firebase Storage
        Glide.with(holder.itemView.context)
            .load(photo.url)
            .placeholder(R.drawable.ic_gallery) // Placeholder saat loading
            .error(R.drawable.ic_gallery_add) // Gambar jika gagal load
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = photoList.size
}