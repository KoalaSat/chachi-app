package com.koalasat.nestr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.koalasat.nestr.databinding.FragmentHomeBinding
import com.koalasat.nestr.models.ExternalSigner

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.amber.setOnClickListener {
            ExternalSigner.savePubKey()
        }

        binding.buttonChachi.setOnClickListener {
            openWebApp("https://chachi.chat", getString(R.string.chachi))
        }
        binding.buttonZapStream.setOnClickListener {
            openWebApp("https://zap.stream", getString(R.string.zapstream))
        }
        binding.buttonHablanews.setOnClickListener {
            openWebApp("https://habla.news", getString(R.string.hablanews))
        }
        binding.buttonNostrkiwi.setOnClickListener {
            openWebApp("https://nostr.kiwi/app", getString(R.string.nostrkiwi))
        }
        binding.buttonAnimalsunset.setOnClickListener {
            openWebApp("https://www.animalsunset.com", getString(R.string.animalsunset))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openWebApp(
        url: String,
        title: String,
    ) {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setTitle("${getString(R.string.open)} $title")
        builder?.setMessage(getString(R.string.openWebsiteCheck))

        builder?.setPositiveButton(getString(R.string.browse)) { dialog, which ->
            val bundle =
                Bundle().apply {
                    putString("url", url)
                }
            findNavController().navigate(R.id.action_HomeFragment_to_WebAppFragment, bundle)
            val mainActivity = requireActivity() as MainActivity
            mainActivity.setActionBarTitle(title)
        }

        builder?.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = builder?.create()
        alertDialog?.show()
    }
}
