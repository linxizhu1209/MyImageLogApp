package com.example.myimagelogapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myimagelogapp.databinding.ItemPraiseBinding
import com.example.myimagelogapp.viewModel.PraiseUiItem

class PraiseAdapter : ListAdapter<PraiseUiItem, PraiseAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPraiseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(private val binding: ItemPraiseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PraiseUiItem) {
            binding.tvPraiseNickname.text = item.nickname
            binding.tvPraiseContent.text = item.content
            binding.tvPraiseDate.text = item.dateText
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PraiseUiItem>() {
            override fun areItemsTheSame(a: PraiseUiItem, b: PraiseUiItem) = a.id == b.id
            override fun areContentsTheSame(a: PraiseUiItem, b: PraiseUiItem) = a == b
        }
    }
}
