package com.example.myimagelogapp.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myimagelogapp.R
import com.example.myimagelogapp.adapter.HomeImageAdapter
import com.example.myimagelogapp.data.remote.RetrofitProvider
import com.example.myimagelogapp.data.remote.StockNewsDto
import com.example.myimagelogapp.data.repository.ImageRepository
import com.example.myimagelogapp.databinding.FragmentHomeBinding
import com.example.myimagelogapp.viewModel.HomeUiState
import com.example.myimagelogapp.viewModel.HomeViewModel
import com.example.myimagelogapp.viewModel.NewsUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter : HomeImageAdapter

    private val homeVm: HomeViewModel by lazy {
        val api = RetrofitProvider.createImageApi(requireContext())
        HomeViewModel(repo = ImageRepository(api))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
        observeImageState()
        observeNewsState()

        homeVm.loadThisWeek(userId = 1L)
        homeVm.loadTodayNews()
    }

    private fun setupRecyclerView() {
        val baseUrl = requireContext().getString(R.string.base_url).trim().removeSurrounding("\"")
        // recyclerview 좌우 스크롤
        adapter = HomeImageAdapter(baseUrl = baseUrl) {
                item ->
            val bundle = Bundle().apply {
                putLong("imageId", item.id)
                putString("imageUrl", item.url)
                putString("dateText", item.dateText)
            }
            findNavController().navigate(R.id.postDetailFragment, bundle)
        }
        binding.rvHomePhotos.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHomePhotos.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnCreate.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_create)
        }
        binding.btnUploads.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_uploadQueue)
        }
    }

    private fun observeImageState() {
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
    }

    private fun observeNewsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeVm.newsState.collectLatest { state ->
                when(state) {
                    NewsUiState.Loading -> {
                        binding.tvNewsLoading.visibility = View.VISIBLE
                        binding.llNewsContainer.visibility = View.GONE
                        binding.tvNewsEmpty.visibility = View.GONE
                    }
                    is NewsUiState.Success -> {
                        binding.tvNewsLoading.visibility = View.GONE
                        if (state.news.isEmpty()) {
                            binding.llNewsContainer.visibility = View.GONE
                            binding.tvNewsEmpty.visibility = View.VISIBLE
                        } else {
                            binding.tvNewsEmpty.visibility = View.GONE
                            displayNews(state.news)
                        }
                    }
                    is NewsUiState.Error -> {
                        binding.tvNewsLoading.visibility = View.GONE
                        binding.llNewsContainer.visibility = View.GONE
                        binding.tvNewsEmpty.visibility = View.VISIBLE
                        binding.tvNewsEmpty.text = state.message
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * 뉴스목록을 동적으로 표시
     */
    private fun displayNews(newsList: List<StockNewsDto>) {
        binding.llNewsContainer.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        for (news in newsList) {
            val itemView = inflater.inflate(R.layout.item_news, binding.llNewsContainer, false)

            itemView.findViewById<TextView>(R.id.tv_news_title).text = news.title
            itemView.findViewById<TextView>(R.id.tv_news_source).text = news.source ?: "뉴스"

            // 클릭 시 브라우저로 기사 열기
            itemView.setOnClickListener {
                news.sourceUrl?.let { url ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "링크를 열 수 없습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            binding.llNewsContainer.addView(itemView)
        }
        binding.llNewsContainer.visibility = View.VISIBLE
    }
}