package com.example.myimagelogapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myimagelogapp.R
import com.example.myimagelogapp.adapter.UploadTaskAdapter
import com.example.myimagelogapp.databinding.FragmentUploadQueueBinding
import com.example.myimagelogapp.viewModel.UploadQueueViewModel

class UploadQueueFragment : Fragment(R.layout.fragment_upload_queue) {
    private var _binding: FragmentUploadQueueBinding? = null
    private val binding get() = _binding!!

    private val vm: UploadQueueViewModel by viewModels()
    private val adapter = UploadTaskAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentUploadQueueBinding.bind(view)

        binding.rvUploads.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUploads.adapter = adapter

        vm.tasks.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}