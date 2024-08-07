package com.example.wayfindr.places

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.wayfindr.R

class AddPlacesFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var add_places_placesname: EditText
    private lateinit var add_places_categories_spinner: Spinner
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var addImageButton: FloatingActionButton
    private lateinit var add_places_placeslocationedittext: EditText
    private lateinit var add_image_text: View
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var back_btn: ImageButton
    private var isUploading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_places_fragment, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        imagePreview = view.findViewById(R.id.placesimage_preview)
        uploadImageButton = view.findViewById(R.id.add_places_placesbtn)
        add_places_placesname = view.findViewById(R.id.add_places_placesnameedittext)
        add_places_categories_spinner = view.findViewById(R.id.category_spinner)
        addImageButton = view.findViewById(R.id.add_places_image_btn)
        add_image_text = view.findViewById(R.id.add_image_text)
        add_places_placeslocationedittext = view.findViewById(R.id.add_places_placeslocationedittext)
        back_btn = view.findViewById(R.id.addPlaces_back_btn)

        val storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        setupCategorySpinner()

        uploadImageButton.setOnClickListener {
            if (add_places_placeslocationedittext.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen konum ekleyin.", Toast.LENGTH_SHORT).show()
            } else {
                if (!isUploading) {
                    isUploading = true
                    uploadDataToFirestore()
                } else {
                    Toast.makeText(requireContext(), "Yükleme zaten devam ediyor.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        addImageButton.setOnClickListener {
            openGallery()
        }

        imagePreview.setOnClickListener {
            openGallery()
        }

        back_btn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                .remove(this)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun setupCategorySpinner() {
        val categoryNames = resources.getStringArray(R.array.array_categories)
        val categoryDrawablesResIds = resources.obtainTypedArray(R.array.array_category_images)

        val spinnerItems = mutableListOf<SpinnerItem>()

        for (i in categoryNames.indices) {
            val name = categoryNames[i]
            val drawableResId = categoryDrawablesResIds.getResourceId(i, -1)
            spinnerItems.add(SpinnerItem(drawableResId, name))
        }

        categoryDrawablesResIds.recycle()

        val adapter = CustomSpinnerAdapter(requireContext(), R.layout.custom_spinner_item, spinnerItems)
        add_places_categories_spinner.adapter = adapter
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
    }

    private fun uploadDataToFirestore() {
        val placesName = add_places_placesname.text.toString().trim()
        val placesCategories = add_places_categories_spinner.selectedItem.toString()
        val placesLocation = add_places_placeslocationedittext.text.toString().trim()

        if (placesName.isNotEmpty() && placesCategories.isNotEmpty() && placesLocation.isNotEmpty() && selectedImageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}")
            storageReference.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        Glide.with(requireContext())
                            .load(imageUrl)
                            .into(imagePreview)

                        val firestore = FirebaseFirestore.getInstance()
                        val document = firestore.collection("user_location_recommendation").document()
                        val data = hashMapOf(
                            "name" to placesName,
                            "categories" to placesCategories,
                            "location" to placesLocation,
                            "image" to imageUrl
                        )

                        document.set(data)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Data uploaded successfully", Toast.LENGTH_SHORT).show()
                                requireActivity().supportFragmentManager.popBackStack()
                                isUploading = false
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error uploading data: $e", Toast.LENGTH_SHORT).show()
                                isUploading = false
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error uploading image: $e", Toast.LENGTH_SHORT).show()
                    isUploading = false
                }
        } else {
            Toast.makeText(context, "Please fill in all fields and select an image", Toast.LENGTH_SHORT).show()
            isUploading = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            if (selectedImageUri != null) {
                imagePreview.visibility = View.VISIBLE
                imagePreview.setImageURI(selectedImageUri)

                addImageButton.visibility = View.GONE
                add_image_text.visibility = View.GONE
            }
        }
    }
}
