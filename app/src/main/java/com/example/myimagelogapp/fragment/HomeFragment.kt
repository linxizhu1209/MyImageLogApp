package com.example.myimagelogapp.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myimagelogapp.R
import com.example.myimagelogapp.data.remote.RetrofitProvider
import com.example.myimagelogapp.data.repository.ImageRepository
import com.example.myimagelogapp.databinding.FragmentHomeBinding
import com.example.myimagelogapp.viewModel.ImageViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: ImageViewModel by lazy {
        val api = RetrofitProvider.createImageApi(requireContext())
        ImageViewModel(
            repo = ImageRepository(api)
        )
    }
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)

        binding.btnCreate.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_create)
        }
        binding.btnUploads.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_uploadQueue)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}