package com.example.wayfindr.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.wayfindr.R
import com.example.wayfindr.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val moreInfoTxt = view.findViewById<TextView>(R.id.moreinfotxt)
        moreInfoTxt.setOnClickListener {
            showMoreInfoDialog()
        }

    }

    private fun showMoreInfoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Etkinlik Planlama")
        builder.setMessage("Etkinlik planlama özelliği sayesinde artık arkadaşlarınızla plan yapabilecek veya gitmeyi planladığınız bir yeri işaretleyerek arkadaşlarınıza planlarınızı gösterebilirsiniz.")
        builder.setPositiveButton("Kapat") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(userId: String): EventsFragment {
            val fragment = EventsFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }
}
