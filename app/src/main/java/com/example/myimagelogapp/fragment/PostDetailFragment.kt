package com.example.myimagelogapp.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.myimagelogapp.R
import com.example.myimagelogapp.data.remote.RetrofitProvider
import com.example.myimagelogapp.data.repository.ImageRepository
import com.example.myimagelogapp.databinding.FragmentPostDetailBinding
import com.example.myimagelogapp.viewModel.PostDetailViewModel
import kotlinx.coroutines.launch

/**
 * 메인에서 이미지를 클릭했을 때 열리는 화면으로, 클릭한 이미지와 함께 기록한 글을 보여주고 수정 가능
 */
class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private val args: PostDetailFragmentArgs by navArgs()

    private val vm: PostDetailViewModel by viewModels {
        val api = RetrofitProvider.createImageApi(requireContext())
        PostDetailViewModel.Factory(ImageRepository(api))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPostDetailBinding.bind(view)

        binding.tvDetailDate.text = args.dateText

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.loadPost(args.imageId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.postData.collect { data ->
                android.util.Log.d("PostDetail", "postData collected: $data")
                if (data != null) {
                    binding.etTitle.setText(data.title)
                    binding.etContent.setText(data.content)

                    binding.tvCreatedAt.text = "작성: ${formatDateTime(data.createdAt)}"
                    binding.tvUpdatedAt.text = "수정: ${formatDateTime(data.updatedAt)}"

                    val imageUrl = data.imageUrl
                    android.util.Log.d("PostDetail", "imageUrl: $imageUrl")
                    if (!imageUrl.isNullOrBlank()) {
                        binding.ivDetailPhoto.load(imageUrl) {
                            crossfade(true)
                            listener(
                                onError = { _, result ->
                                    android.util.Log.e("PostDetail", "이미지 로드 실패: $imageUrl", result.throwable)
                                },
                                onSuccess = { _, _ ->
                                    android.util.Log.d("PostDetail", "이미지 로드 성공: $imageUrl")
                                }
                            )
                        }
                    } else {
                        android.util.Log.e("PostDetail", "imageUrl이 null 또는 빈 문자열")
                    }
                }
            }
        }

        binding.btnUpdate.setOnClickListener {
            val title = binding.etTitle.text?.toString().orEmpty()
            val content = binding.etContent.text?.toString().orEmpty()
            viewLifecycleOwner.lifecycleScope.launch {
                vm.updatePost(args.imageId, title, content)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.events.collect { event ->
                when (event) {
                    is PostDetailViewModel.Event.Updated -> {
                        Toast.makeText(requireContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is PostDetailViewModel.Event.Error -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun resolveImageUrl(url: String, baseUrl: String): String {
        val u = url.toUri()
        val host = u.host ?: return url
        if (host != "localhost" && host != "127.0.0.1") return url
        val base = baseUrl.toUri()
        val baseOrigin = "${base.scheme}://${base.host}:${base.port}"
        return "$baseOrigin${u.path ?: ""}${u.query?.let { "?$it" } ?: ""}"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun formatDateTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "-"
        return try {
            val ldt = java.time.LocalDateTime.parse(isoString.substringBefore("."))
            ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
        } catch (e: Exception) {
            isoString.take(16).replace("T", " ")
        }
    }
}