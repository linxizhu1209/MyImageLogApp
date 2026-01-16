package com.example.myimagelogapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myimagelogapp.databinding.ItemPhotoPreviewBinding

class PhotoPreviewAdapter(
    private val onRemove: (String) -> Unit
): RecyclerView.Adapter<PhotoPreviewAdapter.VH>() {

    private val items = mutableListOf<String>()

    fun submit(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoPreviewAdapter.VH {
        val binding = ItemPhotoPreviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding, onRemove)
    }

    override fun onBindViewHolder(holder: PhotoPreviewAdapter.VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(
        private val binding: ItemPhotoPreviewBinding,
        private val onRemove: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uriString: String) {
            binding.ivThumb.load(uriString) {
                crossfade(true)
            }
            binding.btnRemove.setOnClickListener {
                onRemove(uriString)
            }
        }
    }
}