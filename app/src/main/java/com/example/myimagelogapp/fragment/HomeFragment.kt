package com.example.myimagelogapp.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
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
import com.example.myimagelogapp.auth.AuthNavigator
import com.example.myimagelogapp.auth.AuthSession
import com.example.myimagelogapp.data.repository.StockReportRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val stockReportRepo: StockReportRepository by lazy {
        val api = RetrofitProvider.createStockReportApi(requireContext())
        StockReportRepository(api, requireContext())
    }

    private lateinit var adapter : HomeImageAdapter
    private var userId: Long = -1L

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
        if (AuthNavigator.redirectToLoginIfNeeded(this)) return

        userId = AuthSession.userId(requireContext())
        setupRecyclerView()
        setupButtons()
        observeImageState()
        observeNewsState()

        homeVm.loadThisWeek(userId = userId)
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

    /**
     * 버튼 세팅
     */
    private fun setupButtons() {
        binding.btnCreate.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_create)
        }
        binding.btnWeekSummary.setOnClickListener {
            openWeekSummaryStreamlit(userId = userId)
        }
        binding.btnLogout.setOnClickListener {
            AuthSession.clear(requireContext())
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build()
            )
        }
        binding.btnStockReportSubscribe.setOnClickListener {
            showStockReportSubscribeDialog()
        }
        binding.btnPraise.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_praise)
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
                        if (state.requiresLogin) {
                            AuthSession.clear(requireContext())
                            AuthNavigator.redirectToLoginIfNeeded(this@HomeFragment)
                        } else {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
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
                        binding.tvNewsLoading.text = "AI가 뉴스를 분석 중입니다...\n잠시만 기다려주세요 (최대 2분)"
                        binding.tvNewsLoading.visibility = View.VISIBLE
                        binding.llNewsContainer.visibility = View.GONE
                        binding.tvNewsEmpty.visibility = View.GONE
                        binding.llAiSummaryContainer.visibility = View.GONE
                    }
                    is NewsUiState.Success -> {
                        binding.tvNewsLoading.visibility = View.GONE

                        if (!state.aiSummary.isNullOrBlank()) {
                            binding.tvAiSummary.text = state.aiSummary
                            binding.llAiSummaryContainer.visibility = View.VISIBLE
                        } else {
                            binding.llAiSummaryContainer.visibility = View.GONE
                        }

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
                        binding.llAiSummaryContainer.visibility = View.GONE
                        if (state.requiresLogin) {
                            AuthSession.clear(requireContext())
                            AuthNavigator.redirectToLoginIfNeeded(this@HomeFragment)
                        } else {
                            binding.tvNewsEmpty.visibility = View.VISIBLE
                            binding.tvNewsEmpty.text = state.message
                        }
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

    /**
     * Streamlit 이번 주 요약 페이지를 브라우저/WebView로 여는 함수
     * userId를 쿼리 파라미터로 전달
     */
    private fun openWeekSummaryStreamlit(userId: Long) {
        val token = AuthSession.token(requireContext())
        if (token.isNullOrBlank() || userId < 0L) {
            Toast.makeText(requireContext(), "로그인이 필요합니다!", Toast.LENGTH_SHORT).show()
            return
        }

        val baseStreamlitUrl = requireContext().getString(R.string.streamlit_url).trim().removeSurrounding("\"")
        val url = baseStreamlitUrl.toUri().buildUpon()
            .appendQueryParameter("userId", userId.toString())
            .appendQueryParameter("token", token)
            .build()
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "요약 페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStockReportSubscribeDialog() {
        if (!AuthSession.isLoggedIn(requireContext())) {
            Toast.makeText(requireContext(), R.string.stock_report_login_required, Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_stock_report_subscription, null)
        val etSymbol = dialogView.findViewById<TextInputEditText>(R.id.et_stock_symbol)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.et_stock_email)
        val btnPublish = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_publish)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_cancel)

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { stockReportRepo.getSubscription() }
                .getOrNull()
                ?.let { sub ->
                    etEmail.setText(sub.email)
                    etSymbol.setText(sub.symbols.joinToString(", "))
                }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.stock_report_dialog_title)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnPublish.setOnClickListener {
            val symbolRaw = etSymbol.text?.toString()?.trim().orEmpty()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            if (symbolRaw.isBlank()) {
                Toast.makeText(requireContext(), R.string.stock_report_invalid_symbol, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), R.string.stock_report_invalid_email, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 쉼표로 여러 종목 입력 가능 (서버 최대 10개)
            val symbols = symbolRaw.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            btnPublish.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                runCatching {
                    stockReportRepo.subscribe(email, symbols)
                }.onSuccess {
                    Toast.makeText(requireContext(), R.string.stock_report_success, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }.onFailure { e ->
                    val msg = e.message?.takeIf { it.isNotBlank() }
                        ?: getString(R.string.stock_report_failed)
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                btnPublish.isEnabled = true
            }
        }
        dialog.show()
    }
}