package com.example.myimagelogapp.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myimagelogapp.R
import com.example.myimagelogapp.adapter.HomeImageAdapter
import com.example.myimagelogapp.data.remote.RetrofitProvider
import com.example.myimagelogapp.data.repository.ImageRepository
import com.example.myimagelogapp.databinding.FragmentHomeBinding
import com.example.myimagelogapp.viewModel.HomeUiState
import com.example.myimagelogapp.viewModel.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter : HomeImageAdapter

    private val homeVm: HomeViewModel by lazy {
        val api = RetrofitProvider.createImageApi(requireContext())
        HomeViewModel(repo = ImageRepository(api))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)

        // recyclerview 좌우 스크롤
        adapter = HomeImageAdapter(baseUrl = requireContext().getString(R.string.base_url).trim().removeSurrounding("\""))
        binding.rvHomePhotos.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHomePhotos.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            homeVm.state.collect { state ->
                when (state) {
                    HomeUiState.Loading -> {
                        binding.tvEmptyThisWeek.visibility = View.GONE
                    }
                    is HomeUiState.Success -> {
                        adapter.submitList(state.items)
                        binding.tvEmptyThisWeek.visibility =
                            if (state.items.isEmpty()) View.VISIBLE else View.GONE
                    }
                    is HomeUiState.Error -> {
                        binding.tvEmptyThisWeek.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }

        homeVm.loadThisWeek(userId = 1L)

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