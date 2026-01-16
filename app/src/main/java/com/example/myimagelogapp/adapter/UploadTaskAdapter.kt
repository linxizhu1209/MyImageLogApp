package com.example.myimagelogapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myimagelogapp.databinding.ItemUploadTaskBinding
import com.example.myimagelogapp.entity.UploadTaskEntity

class UploadTaskAdapter : RecyclerView.Adapter<UploadTaskAdapter.VH>() {

    private val items = mutableListOf<UploadTaskEntity>()

    fun submit(list: List<UploadTaskEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadTaskAdapter.VH {
        val binding = ItemUploadTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: UploadTaskAdapter.VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(private val binding: ItemUploadTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UploadTaskEntity) {
            binding.tvTitle.text = item.title.ifBlank { "(제목 없음)" }
            binding.tvMeta.text = "photos=${item.photoCount}"
            binding.pb.progress = item.progress
            binding.tvStatus.text = "${item.status} (${item.progress}%)"
        }
    }
}