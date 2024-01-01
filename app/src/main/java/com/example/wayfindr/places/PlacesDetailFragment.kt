package com.example.wayfindr.places

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.databinding.FragmentPlacesDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class PlacesDetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPlacesDetailBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val userId = currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPlacesDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var placesName = binding.placesName
        var placesImage = binding.placesImage
        var placesDetails = binding.placesDetails
        var placesOpeningHours = binding.placesOpeningHours
        var placesPrice = binding.placesPrice
        var placesAddress = binding.placesAddress
        var commentDetail = binding.commentDetail
        var addedComment = binding.commentSend

        val selectedPlace = arguments?.getSerializable("selectedPlace") as? PlaceModel

        if (selectedPlace != null) {
            val placeId = selectedPlace.placeId

            if (placeId.isNotBlank()) {
                db.collection("places")
                    .document(placeId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {

                            val placeModel = document.toObject(PlaceModel::class.java)

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

                            println(placeModel)
                        } else {
                            println("Belge bulunamadı")
                        }
                    }
                    .addOnFailureListener { exception ->
                        println("Firestore veri çekme hatası: $exception")
                    }
            } else {
                println("Belge ID'si boş")
            }
        } else {
            println("Belge ID'si null")
        }

        addedComment.setOnClickListener {
            val commentText = commentDetail.text.toString()
            val placeId = selectedPlace?.placeId
            val userId = auth.currentUser?.uid

            if (!userId.isNullOrBlank() && !placeId.isNullOrBlank()) {
                val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val userSubDoc = dataSnapshot.value as? Map<String, Any>

                            val placeSubDoc = hashMapOf(
                                "placeName" to selectedPlace?.placeName,
                                "placeDescription" to selectedPlace?.placeDescription,
                                "placeImage" to selectedPlace?.placeImage,
                                "placeCategories" to selectedPlace?.placeCategories,
                                "placeAddress" to selectedPlace?.placeAddress,
                                "placeLocation" to selectedPlace?.placeLocation,
                                "placeOpeningHours" to selectedPlace?.placeOpeningHours,
                                "placeDetails" to selectedPlace?.placeDetails,
                                "placePrice" to selectedPlace?.placePrice
                            )

                            val commentData = hashMapOf(
                                "commentText" to commentText,
                                "user" to userSubDoc,
                                "place" to placeSubDoc
                            )

                            db.collection("commentsPlaces")
                                .add(commentData)
                                .addOnSuccessListener { documentReference ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Yorumunuz başarıyla eklendi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.i("asas", "Yorum eklendi ${documentReference.id}")
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Yorumunuz eklenmedi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e("sa", "yorum ekleme hatası $e")
                                }
                        } else {
                            println("Kullanıcı belgesi bulunamadı")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        println("Realtime Database'den kullanıcı bilgilerini alma hatası: $databaseError")
                    }
                })
            } else {
                // Hata durumunda işlemler
                println("Kullanıcı ID'si veya Yer ID'si boş veya null")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



