package com.dsatm.ayurvi

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Context

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : Fragment() {

    private lateinit var webView: WebView
    private val sharedPrefKey = "CHATBOX_HTML"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)

        // Initialize WebView
        webView = view.findViewById(R.id.webView)
        webView.webViewClient = WebViewClient() // Ensure links open within the WebView
        webView.settings.javaScriptEnabled = true // Enable JavaScript (optional)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                // Inject previously stored HTML into the chatbox class
                val storedHtml = getStoredChatHtml()
                if (storedHtml.isNotEmpty()) {
                    view.evaluateJavascript(
                        "document.querySelector('.chatbox').innerHTML = `$storedHtml`;",
                        null
                    )
                }

                // Save the chatbox HTML content when WebView is closed
            }
        }
        webView.loadUrl("https://adithyavardhan-b.github.io/ChatBot-for-Ayurvi/")

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Register a back press callback
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    saveChatHtml()
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        saveChatHtml()
    }

    override fun onStop() {
        super.onStop()
        saveChatHtml()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Clear WebView resources when fragment is destroyed
        saveChatHtml()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Example: Clean up WebView or other components
        saveChatHtml()
    }
    override fun onPause() {
        super.onPause()
        saveChatHtml() // Save the chatbox content when the fragment is paused
    }

    fun saveChatHtml() {
        webView.evaluateJavascript(
            "document.querySelector('.chatbox')?.innerHTML;",
            { html ->
                if (isAdded && !html.isNullOrEmpty()) { // Check if fragment is still attached
                    val cleanedHtml = html.trim('"').replace("\\n", "").replace("\\\"", "\"")

                    android.util.Log.d("WebViewFragment", "Extracted HTML: $cleanedHtml")

                    val prefs = requireActivity()
                        .getSharedPreferences("AppData", Context.MODE_PRIVATE)
                    prefs.edit().putString(sharedPrefKey, cleanedHtml).apply()
                }
            }
        )
    }

    // Retrieves stored chatbox content
    private fun getStoredChatHtml(): String {
        if (!isAdded) return ""
        val prefs = requireContext().getSharedPreferences("AppData", Context.MODE_PRIVATE)
        val storedHtml = prefs.getString(sharedPrefKey, "") ?: ""
        android.util.Log.d("WebViewFragment", "Retrieved HTML: $storedHtml")
        return storedHtml
    }
}