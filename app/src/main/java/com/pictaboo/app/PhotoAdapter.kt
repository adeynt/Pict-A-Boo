package com.pictaboo.app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotoAdapter(private var photoList: List<PhotoModel>) :
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

        // Menggunakan Glide untuk memuat gambar dari URI lokal (yang disimpan di Room)
        Glide.with(holder.itemView.context)
            .load(Uri.parse(photo.localUri))
            .placeholder(R.drawable.ic_gallery)
            .error(R.drawable.ic_gallery_add)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = photoList.size

    /** Fungsi utilitas untuk memperbarui data dari Flow Room */
    fun updateData(newPhotoList: List<PhotoModel>) {
        this.photoList = newPhotoList
        notifyDataSetChanged()
    }
}
