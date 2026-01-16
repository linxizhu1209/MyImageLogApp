package com.example.myimagelogapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myimagelogapp.R
import com.example.myimagelogapp.adapter.PhotoPreviewAdapter
import com.example.myimagelogapp.databinding.FragmentPostCreateBinding
import com.example.myimagelogapp.viewModel.PostCreateViewModel

class PostCreateFragment : Fragment(R.layout.fragment_post_create) {

    private var _binding: FragmentPostCreateBinding? = null
    private val binding get() = _binding!!

    private val vm: PostCreateViewModel by viewModels()

    private lateinit var adapter: PhotoPreviewAdapter

    private val pickPhotos =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)) { uris ->
            val uriStrings = uris.map { it.toString()}
            vm.addPhotos(uriStrings, max = 10)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPostCreateBinding.bind(view)

        adapter = PhotoPreviewAdapter(onRemove = { uriString ->
            vm.removePhoto(uriString)
        })

        binding.rvPhotos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPhotos.adapter = adapter

        binding.btnPickPhotos.setOnClickListener {
            pickPhotos.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        vm.photos.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
            binding.tvPhotoCount.text = "${list.size}/10"
        }

        binding.btnSubmit.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}