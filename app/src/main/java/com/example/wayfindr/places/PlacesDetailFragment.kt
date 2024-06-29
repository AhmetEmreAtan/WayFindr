package com.example.wayfindr.places

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wayfindr.Places
import com.example.wayfindr.R
import com.example.wayfindr.UserData
import com.example.wayfindr.databinding.FragmentPlacesDetailBinding
import com.example.wayfindr.home.Navigation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PlacesDetailFragment : Fragment() {

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
        _binding = FragmentPlacesDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Close Fragment
        val closeButton = view.findViewById<ImageButton>(R.id.placesdetail_closebtn)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val selectedPlace = arguments?.getSerializable("selectedPlace") as? PlaceModel
        val selectedPlaceId = selectedPlace?.placeId


        val gitBtn = view.findViewById<Button>(R.id.gitbtn)
        gitBtn.setOnClickListener {
            getSelectedPlaceLocation { placeLocation ->
                if (placeLocation != null) {
                    val intent = Intent(requireContext(), Navigation::class.java)
                    intent.putExtra("latitude", placeLocation.latitude)
                    intent.putExtra("longitude", placeLocation.longitude)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Konum bilgileri bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }
        }


        var placesName = binding.placesName
        var placesImage = binding.placesImage
        var placesDetails = binding.placesDetails
        var placesOpeningHours = binding.placesOpeningHours
        var placesPrice = binding.placesPrice
        var placesAddress = binding.placesAddress
        var commentDetail = binding.commentDetail
        var addedComment = binding.commentSend


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
                                .centerCrop()
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
                val placeRef = FirebaseFirestore.getInstance().collection("places").document(placeId)

                placeRef.collection("comments")
                    .get()
                    .addOnSuccessListener { commentsQuerySnapshot ->
                        commentList.clear()

                        for (commentDocument in commentsQuerySnapshot.documents) {
                            val commentText = commentDocument.getString("commentText")

                            val userMap = commentDocument.get("user") as Map<String,Any>?
                            val userModel = UserData(
                                userId = userMap?.get("id") as String?,
                                username = userMap?.get("userName") as String?,
                                firstName = userMap?.get("name") as String?,
                                profileImageUrl = userMap?.get("profileImage") as String?
                            )

                            val commentModel = CommentPlacesModel(
                                commentText = commentText,
                                user = userModel
                            )

                            commentList.add(commentModel)
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
                val realtimeDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                val userData = hashMapOf<String, Any>()

                realtimeDatabaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val realtimeUserData = dataSnapshot.value as Map<String,Any>?

                        if (realtimeUserData != null) {
                            userData.putAll(realtimeUserData as Map<out String, Any>)
                        }

                        userRef.set(userData, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.i("asas", "Kullanıcı belgesi güncellendi: $userData")

                                val commentData = hashMapOf(
                                    "commentText" to commentText,
                                    "user" to userData
                                )

                                val placeRef = FirebaseFirestore.getInstance().collection("places").document(placeId)
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
                                            UserData(
                                                userId = user.uid,
                                                firstName = user.displayName,
                                                email = user.email,
                                                username = user.displayName,
                                                profileImageUrl = user.photoUrl.toString()
                                            )
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
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("asas", "Firebase Realtime Database hata: $databaseError")
                    }
                })
            } else {
                Log.i("asas", "Kullanıcı girişi yapmamış veya Yer ID'si boş veya null")
            }
        }
    }

    private val itemClickListener = object : ItemClickListener {
        override fun onItemClick(placeId: String) {
            val fragment = PlacesDetailFragment()
            val bundle = Bundle()
            bundle.putString("selectedPlaceId", placeId)
            fragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getSelectedPlaceLocation(callback: (LatLng?) -> Unit) {
        val selectedPlaceId = arguments?.getString("selectedPlaceId")

        if (selectedPlaceId != null) {
            val db = FirebaseFirestore.getInstance()
            val placeRef = db.collection("places").document(selectedPlaceId)

            placeRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val placeLocationData = document.get("placeLocation") as? Map<String, Any>
                        val latitude = placeLocationData?.get("latitude") as? Double
                        val longitude = placeLocationData?.get("longitude") as? Double

                        if (latitude != null && longitude != null) {
                            val placeLocation = LatLng(latitude, longitude)
                            Log.d("PlacesDetailFragment", "Konum başarıyla alındı: $placeLocation")
                            callback(placeLocation)
                        } else {
                            Log.e("PlacesDetailFragment", "Latitude veya longitude null")
                            callback(null)
                        }
                    } else {
                        Log.e("PlacesDetailFragment", "Belge bulunamadı veya yok")
                        callback(null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PlacesDetailFragment", "Firestore veri çekme hatası: $exception")
                    callback(null)
                }
        } else {
            Log.e("PlacesDetailFragment", "Seçilen mekan ID'si null")
            callback(null)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}