package com.example.myimagelogapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myimagelogapp.databinding.ItemHomeImageBinding
import com.example.myimagelogapp.viewModel.HomeImageUiItem
import androidx.core.net.toUri

/**
 * 메인의 "이번 주 기록" 이미지 목록을 좌우 스크롤로 보여주는 Adapter
 */
class HomeImageAdapter(
    private val baseUrl: String,
    private val onClick: (HomeImageUiItem) -> Unit = {}
) : ListAdapter<HomeImageUiItem, HomeImageAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHomeImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, baseUrl, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemHomeImageBinding,
        private val baseUrl: String,
        private val onClick: (HomeImageUiItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * localhost/127.0.0.1 URL을 baseUrl 의 호스트로 바꿔 에뮬레이터에서 이미지가 로드되게함
         */
        private fun resolveImageUrl(url: String): String {
            val u = url.toUri()
            val host = u.host ?: return url
            if (host != "localhost" && host != "127.0.0.1") return url
            val base = baseUrl.trim().removeSurrounding("\"").toUri()
            val baseOrigin = "${base.scheme}://${base.host}:${base.port}"
            val path = u.path ?: ""
            val query = u.query?.let { "?$it" } ?: ""
            return "$baseOrigin$path$query"
        }
        fun bind(item: HomeImageUiItem) {
            val loadUrl = resolveImageUrl(item.url)
            android.util.Log.d("HomeImage", "loadUrl=$loadUrl")
            binding.ivPhoto.load(loadUrl) {
                crossfade(true)
                listener(
                    onError = { _, result ->
                        android.util.Log.e("HomeImage", "load failed: $loadUrl", result.throwable)
                    },
                    onSuccess = { _, _ ->
                        android.util.Log.d("HomeImage", "load ok: $loadUrl")
                    }
                )
            }
            binding.tvDate.text = item.dateText
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<HomeImageUiItem>() {
            override fun areItemsTheSame(
                oldItem: HomeImageUiItem,
                newItem: HomeImageUiItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: HomeImageUiItem,
                newItem: HomeImageUiItem
            ): Boolean = oldItem == newItem
        }
    }

}