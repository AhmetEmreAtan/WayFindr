package com.example.wayfindr.places

import PlaceModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class PlacesDetailFragment : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()

    private var placesName: TextView? = null
    private var placesImage: ImageView? = null
    private var placesDetails: TextView? = null
    private var placesOpeningHours: TextView? = null
    private var placesPrice: TextView? = null
    private var placesAddress: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_places_detail, container, false)

        // UI elemanlarını burada tanımla
        placesName = view.findViewById(R.id.placesName)
        placesImage = view.findViewById(R.id.placesImage)
        placesDetails = view.findViewById(R.id.placesDetails)
        placesOpeningHours = view.findViewById(R.id.placesOpeningHours)
        placesPrice = view.findViewById(R.id.placesPrice)
        placesAddress = view.findViewById(R.id.placesAddress)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Burada belge ID'sini al
        val selectedPlace = arguments?.getParcelable<PlaceModel>("selectedPlace")

        if (selectedPlace != null) {
            val placeId = selectedPlace.placeId

            if (placeId.isNotBlank()) {
                db.collection("places")
                    .document(placeId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Firestore'dan çekilen belgeyi PlacesDetailsModel'e dönüştür
                            val placeModel = document.toObject(PlaceModel::class.java)

                            // Ardından bu verileri UI elemanlarına yerleştir
                            placesName?.text = placeModel?.placeName
                            placesAddress?.text = placeModel?.placeAddress
                            placesOpeningHours?.text = placeModel?.placeOpeningHours
                            placesPrice?.text = placeModel?.placePrice
                            placesDetails?.text = placeModel?.placeDetails
                            Glide.with(view)
                                .load(placeModel?.placeImage)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .into(placesImage!!)

                            // İster logcat'e yazdırabilirsin
                            println(placeModel)
                        } else {
                            println("Belge bulunamadı")
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Hata durumunda işlemler
                        println("Firestore veri çekme hatası: $exception")
                    }
            } else {
                println("Belge ID'si boş")
            }
        } else {
            println("Belge ID'si null")
        }
    }
}



