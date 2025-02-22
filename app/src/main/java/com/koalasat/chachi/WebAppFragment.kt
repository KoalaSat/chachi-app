package com.koalasat.chachi

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.koalasat.chachi.databinding.FragmentWebAppBinding
import com.koalasat.chachi.interfaces.ExternalSignerInterface

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class WebAppFragment : Fragment() {
    private lateinit var webView: WebView

    private var _binding: FragmentWebAppBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        inflater.inflate(R.layout.fragment_web_app, container, false)

        _binding = FragmentWebAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        webView.addJavascriptInterface(ExternalSignerInterface(webView), "chachi")
        webView.webViewClient =
            object : WebViewClient() {
                override fun onPageFinished(
                    view: WebView?,
                    url: String?,
                ) {
                    super.onPageFinished(view, url)
                    injectNIP07()
                }
            }
        webView.loadUrl("https://chachi.chat")

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }
                }
            },
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun injectNIP07() {
        val jsCode =
            """
            window.nostr = {
                getPublicKey: function() {
                    return chachi.getPublicKey();
                },
                signEvent: function signEvent(event) {
                    return new Promise((resolve, reject) => {
                        const callbackName = 'callback_' + Date.now();
                        window[callbackName] = function(response) {
                            console.log(response);
                            delete window[callbackName];
                            resolve(response);
                        };
                        console.log(JSON.stringify(event));
                        console.log(callbackName);
                        chachi.signEvent(JSON.stringify(event), callbackName);
                    });
                }
            };
            """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }
}
