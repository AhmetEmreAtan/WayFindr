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
import com.example.wayfindr.R
import com.example.wayfindr.UserData
import com.example.wayfindr.databinding.FragmentPlacesDetailBinding
import com.example.wayfindr.home.Navigation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlacesDetailFragment : Fragment() {

    private var _binding: FragmentPlacesDetailBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val userId = currentUser?.uid

    private val favoriteRepository = FavoriteRepository()
    private lateinit var commentList: MutableList<CommentPlacesModel>

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

        closeFragment(view)

        goToNavigationButtonAction(view)

        val selectedPlace = arguments?.getSerializable("selectedPlace") as? PlaceModel
        val selectedPlaceId = selectedPlace?.placeId

        setupUI(view, selectedPlace)
        setupRecyclerView()
        fetchPlaceDetails(selectedPlaceId)
        fetchComments(selectedPlaceId)

        binding.commentSend.setOnClickListener {
            addComment(selectedPlaceId)
        }

        addRemoveFavorites(selectedPlaceId, selectedPlace)
    }

    private fun setupUI(view: View, selectedPlace: PlaceModel?) {
        binding.apply {
            placesName.text = selectedPlace?.placeName ?: ""
            placesAddress.text = selectedPlace?.placeAddress ?: ""
            placesOpeningHours.text = selectedPlace?.placeOpeningHours ?: ""
            placesPrice.text = selectedPlace?.placePrice ?: ""
            placesDetails.text = selectedPlace?.placeDetails ?: ""
            Glide.with(view)
                .load(selectedPlace?.placeImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop()
                .into(placesImage)
        }
    }

    private fun setupRecyclerView() {
        commentList = mutableListOf()
        binding.commentRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CommentPlacesAdapter(commentList)
        }
    }

    private fun fetchPlaceDetails(placeId: String?) {
        if (placeId.isNullOrBlank()) {
            return
        }

        FirebaseFirestore.getInstance().collection("places")
            .document(placeId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val placeModel = document.toObject(PlaceModel::class.java)
                    setupUI(requireView(), placeModel)
                }
            }
    }

    private fun fetchComments(placeId: String?) {
        if (placeId.isNullOrBlank()) {
            return
        }

        FirebaseFirestore.getInstance().collection("places")
            .document(placeId)
            .collection("comments")
            .get()
            .addOnSuccessListener { commentsQuerySnapshot ->
                commentList.clear()
                for (commentDocument in commentsQuerySnapshot.documents) {
                    val commentText = commentDocument.getString("commentText")
                    val userMap = commentDocument.get("user") as? Map<String, String>
                    val userId = userMap?.get("userId")
                    if (!userId.isNullOrBlank()) {
                        fetchUserDetails(userId, commentText)
                    }
                }
            }
    }

    private fun fetchUserDetails(userId: String, commentText: String?) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument != null && userDocument.exists()) {
                    val userModel = userDocument.toObject(UserData::class.java)
                    if (userModel != null) {
                        val commentModel = CommentPlacesModel(
                            commentText = commentText,
                            user = userModel
                        )
                        commentList.add(commentModel)
                        binding.commentRecyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            }
    }

    private fun addComment(placeId: String?) {
        val commentText = binding.commentDetail.text.toString()
        val user = auth.currentUser

        if (user != null && !placeId.isNullOrBlank()) {
            val userId = user.uid
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userFirestoreData = documentSnapshot.toObject(UserData::class.java)
                        if (userFirestoreData != null) {
                            val userData = mapOf(
                                "userId" to userId,
                                "username" to userFirestoreData.username.orEmpty(),
                                "firstName" to userFirestoreData.firstName.orEmpty(),
                                "profileImageUrl" to userFirestoreData.profileImageUrl.orEmpty()
                            )
                            val commentData = mapOf(
                                "commentText" to commentText,
                                "user" to userData
                            )

                            FirebaseFirestore.getInstance().collection("places")
                                .document(placeId)
                                .collection("comments")
                                .add(commentData)
                                .addOnSuccessListener { documentReference ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Your comment was added successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val newComment = CommentPlacesModel(
                                        commentText,
                                        UserData(
                                            userId = userId,
                                            firstName = userFirestoreData.firstName,
                                            username = userFirestoreData.username,
                                            profileImageUrl = userFirestoreData.profileImageUrl
                                        )
                                    )

                                    commentList.add(newComment)
                                    binding.commentRecyclerView.adapter?.notifyDataSetChanged()
                                    binding.commentRecyclerView.layoutManager?.scrollToPosition(0)
                                }

                        }
                    }
                }
        }
    }

    private fun addRemoveFavorites(
        selectedPlaceId: String?,
        selectedPlace: PlaceModel?
    ) {
        if (userId != null && selectedPlaceId != null) {
            favoriteRepository.isPlaceFavorite(userId, selectedPlaceId) { isFavorite ->
                updateFavoriteButtonUI(isFavorite)
            }
        }

        binding.placesdetailSavefavorite.setOnClickListener {
            if (userId != null && selectedPlace != null) {
                favoriteRepository.isPlaceFavorite(userId, selectedPlace.placeId) { isFavorite ->
                    if (isFavorite) {
                        favoriteRepository.removeFromFavorites(userId, selectedPlace.placeId)
                        Toast.makeText(
                            requireContext(),
                            "remove from favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        favoriteRepository.addToFavorites(userId, selectedPlace)
                        Toast.makeText(requireContext(), "add to favorites", Toast.LENGTH_SHORT)
                            .show()
                    }
                    updateFavoriteButtonUI(!isFavorite)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please login",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateFavoriteButtonUI(isFavorite: Boolean) {
        if (isFavorite) {
            binding.placesdetailSavefavorite.setImageResource(R.drawable.ic_heart_filled)
        } else {
            binding.placesdetailSavefavorite.setImageResource(R.drawable.ic_heart_outlined)
        }
    }

    private fun goToNavigationButtonAction(view: View) {
        val gitBtn = view.findViewById<Button>(R.id.gitbtn)
        gitBtn.setOnClickListener {
            getSelectedPlaceLocation { placeLocation ->
                if (placeLocation != null) {
                    val intent = Intent(requireContext(), Navigation::class.java)
                    intent.putExtra("latitude", placeLocation.latitude)
                    intent.putExtra("longitude", placeLocation.longitude)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Konum bilgileri bulunamadı",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun closeFragment(view: View) {
        val closeButton = view.findViewById<ImageButton>(R.id.placesdetail_closebtn)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
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