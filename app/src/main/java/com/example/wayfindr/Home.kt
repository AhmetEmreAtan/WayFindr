package com.example.wayfindr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.home.ImageSliderAdapter


class Home : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageSliderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val imageList = listOf(R.drawable.galatakulesi, R.drawable.dolmabahce, R.drawable.arkeolojimuzesi, R.drawable.kizkulesi, R.drawable.topkapisarayi)
        val captionList = listOf("Galata Kulesi", "Dolmabahçe Sarayı", "İstanbul Arkeoloji Müzesi", "Kız Kulesi", "İstanbul Topkapı Sarayı")


        adapter = ImageSliderAdapter(imageList, captionList)
        recyclerView.adapter = adapter


        return rootView
    }
}