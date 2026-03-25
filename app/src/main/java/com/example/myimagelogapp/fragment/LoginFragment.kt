package com.example.myimagelogapp.fragment

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myimagelogapp.R
import com.example.myimagelogapp.auth.AuthSession
import com.example.myimagelogapp.databinding.FragmentLoginBinding

/**
 * OAuth 로그인 버튼을 제공하고 로그인 완료(딥링크 저장) 후 Home으로 이동시키는 프래그먼트.
 */
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    /**
     * 로그인 화면에서 OAuth URL을 열고, 이미 로그인 완료라면 Home으로 바로 이동한다.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        if (AuthSession.isLoggedIn(requireContext())) {
            navigateHome()
            return
        }

        binding.btnGoogleLogin.setOnClickListener { openOauthLoginPage(provider = "google") }
        binding.btnKakaoLogin.setOnClickListener { openOauthLoginPage(provider = "kakao") }
    }

    /**
     * 서버의 OAuth 시작 엔드포인트를 Custom Tab으로 연다.
     */
    private fun openOauthLoginPage(provider: String) {
        val baseUrl = requireContext().getString(R.string.base_url).trim().removeSurrounding("\"")
        val oauthUrl = baseUrl.trimEnd('/') + "/oauth2/authorization/$provider"

        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(requireContext(), Uri.parse(oauthUrl))
    }

    /**
     * 로그인 이후 Home으로 이동하면서 로그인 화면을 백스택에서 제거한다.
     */
    private fun navigateHome() {
        findNavController().navigate(
            R.id.action_login_to_home,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()
        )
    }

    /**
     * 딥링크로 토큰 저장이 끝났다면 화면 재개 시 Home으로 이동한다.
     */
    override fun onResume() {
        super.onResume()
        if (isAdded && AuthSession.isLoggedIn(requireContext())) {
            navigateHome()
        }
    }

    /**
     * 바인딩을 해제하여 메모리 누수를 방지한다.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

