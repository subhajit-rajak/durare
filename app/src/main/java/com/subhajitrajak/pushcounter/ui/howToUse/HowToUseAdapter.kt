package com.subhajitrajak.pushcounter.ui.howToUse

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.pushcounter.R

class HowToUseAdapter(
    private val images: IntArray
) : RecyclerView.Adapter<HowToUseAdapter.ImageViewHolder>() {

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_how_to_use, parent, false) as ImageView
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int = images.size
}
