package com.example.wayfindr

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageSelectionFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var userCommentEditText: EditText
    private lateinit var addPhotoLocation: EditText
    private lateinit var addImageButton: ImageButton
    private lateinit var storageReference: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var backBtn: ImageButton

    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private var isUploading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_selection, container, false)
        initializeViews(view)
        setViewVisibilityInitially()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        addImageButton.setOnClickListener {
            openGallery()
        }

        uploadImageButton.setOnClickListener {
            if (!isUploading) {
                uploadImageWithComment()
            } else {
                Toast.makeText(context, "Yükleme devam ediyor, lütfen bekleyiniz.", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn = view.findViewById(R.id.back_btn)
        backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun initializeViews(view: View) {
        imagePreview = view.findViewById(R.id.image_preview)
        uploadImageButton = view.findViewById(R.id.upload_image_btn)
        userCommentEditText = view.findViewById(R.id.user_comment_edittxt)
        addPhotoLocation = view.findViewById(R.id.photo_location_text)
        addImageButton = view.findViewById(R.id.add_image_button)
    }

    private fun setViewVisibilityInitially() {
        imagePreview.visibility = View.GONE
        addImageButton.visibility = View.VISIBLE
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun showSelectedImage(uri: Uri) {
        imagePreview.setImageURI(uri)
        imagePreview.visibility = View.VISIBLE
        addImageButton.visibility = View.GONE
    }

    private fun uploadImageWithComment() {
        selectedImageUri?.let { uri ->
            val photoLocation = addPhotoLocation.text.toString()
            if (photoLocation.isBlank()) {
                Toast.makeText(context, "Lütfen konum bilgisi giriniz.", Toast.LENGTH_SHORT).show()
                return
            }

            isUploading = true
            val imageRef = storageReference.child("images/${System.currentTimeMillis()}")
            imageRef.putFile(uri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val userComment = userCommentEditText.text.toString()
                    val imageData = hashMapOf(
                        "imageUrl" to downloadUri.toString(),
                        "userComment" to userComment,
                        "photoLocation" to photoLocation
                    )
                    db.collection("user_photos").document(auth.currentUser!!.uid)
                        .collection("memories").add(imageData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                            requireActivity().supportFragmentManager.popBackStack() // Fragment'ı kapat
                            isUploading = false
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            isUploading = false
                        }
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    isUploading = false
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                isUploading = false
            }
        } ?: Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
    }


    private fun resetViewsAfterUpload() {
        imagePreview.visibility = View.GONE
        addImageButton.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                data?.data?.let { uri ->
                    selectedImageUri = uri
                    showSelectedImage(uri)
                }
            }
        }
    }
}