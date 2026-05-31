package com.example.myimagelogapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myimagelogapp.R
import com.example.myimagelogapp.adapter.PraiseAdapter
import com.example.myimagelogapp.data.remote.RetrofitProvider
import com.example.myimagelogapp.data.repository.AppPraiseRepository
import com.example.myimagelogapp.databinding.BottomSheetWritePraiseBinding
import com.example.myimagelogapp.databinding.FragmentPraiseBinding
import com.example.myimagelogapp.viewModel.PraiseListUiState
import com.example.myimagelogapp.viewModel.PraiseSubmitUiState
import com.example.myimagelogapp.viewModel.PraiseViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PraiseFragment : Fragment() {

    private var _binding: FragmentPraiseBinding? = null
    private val binding get() = _binding!!

    private val praiseVm: PraiseViewModel by lazy {
        val api = RetrofitProvider.createAppPraiseApi(requireContext())
        PraiseViewModel(AppPraiseRepository(api))
    }

    private val adapter = PraiseAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPraiseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupActions()
        observeList()
        observeSubmit()
        praiseVm.refresh()
    }

    private fun setupRecyclerView() {
        binding.rvPraises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPraises.adapter = adapter
        binding.rvPraises.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lm = rv.layoutManager as? LinearLayoutManager ?: return
                val last = lm.findLastVisibleItemPosition()
                if (last >= adapter.itemCount - 3) {
                    praiseVm.loadMore()
                }
            }
        })
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.mint_primary)
        binding.swipeRefresh.setOnRefreshListener { praiseVm.refresh() }
        binding.fabWritePraise.setOnClickListener { showWriteBottomSheet() }
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            praiseVm.listState.collectLatest { state ->
                when (state) {
                    PraiseListUiState.Idle -> Unit
                    PraiseListUiState.Loading -> {
                        binding.progressLoading.visibility = View.VISIBLE
                        binding.tvPraiseEmpty.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is PraiseListUiState.Success -> {
                        binding.progressLoading.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        adapter.submitList(state.items)
                        binding.tvPraiseEmpty.visibility =
                            if (state.items.isEmpty()) View.VISIBLE else View.GONE
                    }
                    is PraiseListUiState.Error -> {
                        binding.progressLoading.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun observeSubmit() {
        viewLifecycleOwner.lifecycleScope.launch {
            praiseVm.submitState.collectLatest { state ->
                when (state) {
                    PraiseSubmitUiState.Idle -> Unit
                    PraiseSubmitUiState.Submitting -> {
                        writeSheetBinding?.btnSubmitPraise?.isEnabled = false
                    }
                    PraiseSubmitUiState.Success -> {
                        Toast.makeText(requireContext(), R.string.praise_submit_success, Toast.LENGTH_SHORT).show()
                        writeSheet?.dismiss()
                        praiseVm.resetSubmitState()
                    }
                    is PraiseSubmitUiState.Error -> {
                        writeSheetBinding?.btnSubmitPraise?.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        praiseVm.resetSubmitState()
                    }
                }
            }
        }
    }

    private var writeSheet: BottomSheetDialog? = null
    private var writeSheetBinding: BottomSheetWritePraiseBinding? = null

    private fun showWriteBottomSheet() {
        val sheetBinding = BottomSheetWritePraiseBinding.inflate(layoutInflater)
        writeSheetBinding = sheetBinding
        val dialog = BottomSheetDialog(requireContext()).apply {
            setContentView(sheetBinding.root)
        }
        writeSheet = dialog

        sheetBinding.btnSubmitPraise.setOnClickListener {
            submitFromSheet(sheetBinding, dialog)
        }

        dialog.setOnDismissListener {
            praiseVm.resetSubmitState()
            writeSheet = null
            writeSheetBinding = null
        }
        dialog.show()
    }

    private fun submitFromSheet(
        sheetBinding: BottomSheetWritePraiseBinding,
        dialog: BottomSheetDialog
    ) {
        val nickname = sheetBinding.etPraiseNickname.text?.toString()?.trim().orEmpty()
        val content = sheetBinding.etPraiseContent.text?.toString()?.trim().orEmpty()

        when {
            nickname.length < 2 -> {
                Toast.makeText(requireContext(), R.string.praise_invalid_nickname, Toast.LENGTH_SHORT).show()
                return
            }
            content.isBlank() -> {
                Toast.makeText(requireContext(), R.string.praise_invalid_content, Toast.LENGTH_SHORT).show()
                return
            }
        }

        praiseVm.submit(nickname, content)
    }

    override fun onDestroyView() {
        writeSheet?.dismiss()
        _binding = null
        super.onDestroyView()
    }
}
