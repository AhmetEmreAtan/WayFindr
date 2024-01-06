package com.example.wayfindr.places

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.databinding.FragmentPlacesDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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

        val commentList = mutableListOf<CommentPlacesModel>()
        val layoutManager = LinearLayoutManager(requireContext())
        binding.commentRecyclerView.layoutManager = layoutManager
        val commentAdapter = CommentPlacesAdapter(commentList)
        binding.commentRecyclerView.adapter = commentAdapter

        if (selectedPlace != null) {
            val placeId = selectedPlace.placeId

            if (!placeId.isNullOrBlank()) {
                val placeRef =
                    FirebaseFirestore.getInstance().collection("places").document(placeId)

                placeRef.collection("comments")
                    .get()
                    .addOnSuccessListener { commentsQuerySnapshot ->
                        for (commentDocument in commentsQuerySnapshot.documents) {
                            val commentModel =
                                commentDocument.toObject(CommentPlacesModel::class.java)
                            commentModel?.let {
                                commentList.add(it)
                            }
                        }

                        commentAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Log.e("asas", "Firestore'dan comment verilerini alma hatası: $e")
                    }
            } else {
                Log.i("asas", "Yer ID'si boş")
            }
        } else {
            Log.i("asas", "Belge ID'si null")
        }

        addedComment.setOnClickListener {
            val commentText = commentDetail.text.toString()
            val placeId = selectedPlace?.placeId
            val user = auth.currentUser

            if (user != null && !placeId.isNullOrBlank()) {
                val userId = user.uid
                val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
                val placeRef =
                    FirebaseFirestore.getInstance().collection("places").document(placeId)

                val userData = hashMapOf(
                    "userId" to userId,
                    "userName" to user.displayName,
                    "userEmail" to user.email,
                    "userImage" to user.photoUrl.toString()
                )

                userRef.set(userData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.i("asas", "Kullanıcı belgesi güncellendi: $userData")

                        val commentData = hashMapOf(
                            "commentText" to commentText,
                            "user" to userData
                        )

                        placeRef.collection("comments").add(commentData)
                            .addOnSuccessListener { documentReference ->
                                Toast.makeText(
                                    requireContext(),
                                    "Yorumunuz başarıyla eklendi",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.i("asas", "Yorum eklendi ${documentReference.id}")

                                val newComment = CommentPlacesModel(
                                    commentText,
                                    user.displayName,
                                    user.email,
                                    user.photoUrl.toString()
                                )
                                commentList.add(newComment)

                                commentAdapter.notifyDataSetChanged()

                                layoutManager.scrollToPositionWithOffset(0, 0)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Yorumunuz eklenmedi",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("sa", "yorum ekleme hatası $e")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("asas", "Kullanıcı belgesini güncelleme hatası: $e")
                    }

            } else {
                Log.i("asas", "Kullanıcı girişi yapmamış veya Yer ID'si boş veya null")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



