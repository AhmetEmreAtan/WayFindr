package com.example.wayfindr.places

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.UserData
import com.example.wayfindr.databinding.FragmentPlacesDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlacesDetailFragment : Fragment() {

    private var _binding: FragmentPlacesDetailBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val userId = currentUser?.uid

    private val placesRepository = PlacesRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlacesDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedPlace = arguments?.getSerializable("selectedPlace") as? PlaceModel

        selectedPlace?.let { place ->
            loadPlacesDetail(place)
            setupCommentsRecyclerView(place)
            setupFavoriteButton(place)
        }

        binding.commentSend.setOnClickListener {
            addComment(selectedPlace)
        }

        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupCommentsRecyclerView(selectedPlace: PlaceModel) {
        val commentList = mutableListOf<CommentPlacesModel>()
        val layoutManager = LinearLayoutManager(requireContext())
        binding.commentRecyclerView.layoutManager = layoutManager
        val commentAdapter = CommentPlacesAdapter(commentList)
        binding.commentRecyclerView.adapter = commentAdapter

        selectedPlace.placeId?.let { placeId ->
            loadComments(placeId, commentList, commentAdapter)
        }
    }

    private fun loadComments(
        placeId: String,
        commentList: MutableList<CommentPlacesModel>,
        commentAdapter: CommentPlacesAdapter
    ) {
        db.collection("places")
            .document(placeId)
            .collection("comments")
            .get()
            .addOnSuccessListener { commentsQuerySnapshot ->
                commentList.clear()

                for (commentDocument in commentsQuerySnapshot.documents) {
                    val commentText = commentDocument.getString("commentText")
                    val userMap = commentDocument.get("user") as? Map<String, Any>?
                    val userModel = userMap?.let { map ->
                        UserData(
                            userId = map["id"] as? String,
                            username = map["userName"] as? String,
                            firstName = map["name"] as? String,
                            profileImageUrl = map["profileImage"] as? String
                        )
                    }

                    val commentModel = CommentPlacesModel(
                        commentText = commentText,
                        user = userModel
                    )

                    commentList.add(commentModel)
                }

                commentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("PlacesDetailFragment", "Firestore'dan yorum verilerini alma hatası: $e")
            }
    }

    private fun setupFavoriteButton(selectedPlace: PlaceModel) {
        userId?.let { userId ->
            selectedPlace.placeId?.let { placeId ->
                checkFavoriteStatus(placeId) { isFavorite ->
                    updateFavoriteButton(isFavorite)
                }

                binding.favoriteButton.setOnClickListener {
                    toggleFavorite(placeId, selectedPlace)
                }
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val drawableRes =
            if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined
        binding.favoriteButton.setImageResource(drawableRes)
    }

    private fun loadPlacesDetail(selectedPlace: PlaceModel) {
        selectedPlace.placeId?.let { placeId ->
            db.collection("places")
                .document(placeId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val placeModel = document.toObject(PlaceModel::class.java)

                        binding.placesName.text = placeModel?.placeName
                        binding.placesAddress.text = placeModel?.placeAddress
                        binding.placesOpeningHours.text = placeModel?.placeOpeningHours
                        binding.placesPrice.text = placeModel?.placePrice
                        binding.placesDetails.text = placeModel?.placeDetails
                        binding.placeLocation.text = placeModel?.placeAddress

                        Glide.with(requireContext())
                            .load(placeModel?.placeImage)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .centerCrop()
                            .into(binding.placesImage)

                        Log.d(
                            "PlacesDetailFragment",
                            "Yer detayları başarıyla yüklendi: $placeModel"
                        )
                    } else {
                        Log.d("PlacesDetailFragment", "Belge bulunamadı")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PlacesDetailFragment", "Firestore veri çekme hatası: $exception")
                }
        }
    }

    private fun addComment(selectedPlace: PlaceModel?) {
        selectedPlace?.let { place ->
            val commentText = binding.commentDetail.text.toString()
            place.placeId?.let { placeId ->
                val user = auth.currentUser

                user?.let { currentUser ->
                    val userId = currentUser.uid
                    val userData = mapOf(
                        "id" to userId,
                        "userName" to currentUser.displayName,
                        "name" to currentUser.displayName,
                        "profileImage" to currentUser.photoUrl.toString()
                    )

                    val commentData = mapOf(
                        "commentText" to commentText,
                        "user" to userData
                    )

                    db.collection("places")
                        .document(placeId)
                        .collection("comments")
                        .add(commentData)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.success_comment),
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d(
                                "PlacesDetailFragment",
                                "Yorum başarıyla eklendi: ${documentReference.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.comment_warning),
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PlacesDetailFragment", "Yorum ekleme hatası: $e")
                        }
                } ?: Log.d("PlacesDetailFragment", "Kullanıcı girişi yapmamış")
            } ?: Log.d("PlacesDetailFragment", "Yer ID'si boş")
        }
    }

    private fun checkFavoriteStatus(placeId: String, callback: (Boolean) -> Unit) {
        placesRepository.isPlaceFavorite(userId!!, placeId) { isFavorite ->
            callback(isFavorite)
        }
    }

    private fun toggleFavorite(placeId: String, selectedPlace: PlaceModel) {
        val userId = auth.currentUser?.uid
        if (!userId.isNullOrBlank()) {
            placesRepository.isPlaceFavorite(userId, placeId) { isFavorite ->
                if (isFavorite) {
                    placesRepository.removeFromFavorites(userId, placeId)
                    updateFavoriteButton(false)
                } else {
                    placesRepository.addToFavorites(userId, selectedPlace)
                    updateFavoriteButton(true)
                }
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.signin_warning), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}