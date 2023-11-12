package com.example.wayfindr

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
class ImageSelectionFragment : DialogFragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var userCommentEditText: EditText
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var addImageButton: FloatingActionButton
    private lateinit var addPhotoLocation: EditText
    private lateinit var textView2: View
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var back_btn: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_selection, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        imagePreview = view.findViewById(R.id.image_preview)
        uploadImageButton = view.findViewById(R.id.upload_image_btn)
        userCommentEditText = view.findViewById(R.id.user_comment_edittxt)
        addImageButton = view.findViewById(R.id.add_image_button)
        textView2 = view.findViewById(R.id.textView2)
        addPhotoLocation = view.findViewById(R.id.photo_location_text)
        back_btn = view.findViewById(R.id.back_btn)

        uploadImageButton.setOnClickListener {
            if (addPhotoLocation.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen konum ekleyin.", Toast.LENGTH_SHORT).show()
            } else {
                uploadImageWithComment()
            }
        }

        val storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        addImageButton.setOnClickListener {
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
    }

    private fun uploadDataToUserSpecificFirestore(imageUrl: String, comment: String, Identifier: String, photoLocation: String) {
        if (!isAdded) {
            return
        }

        val userPhotosCollection = db.collection("user_photos").document(Identifier).collection("memories")

        val photoData = hashMapOf(
            "imageUrl" to imageUrl,
            "userComment" to comment,
            "photoLocation" to photoLocation
        )

        userPhotosCollection
            .add(photoData)
            .addOnSuccessListener { documentReference ->
                if (isAdded) {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Yükleme başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun uploadImageWithComment() {
        selectedImageUri?.let { uri ->
            val imagesRef = storageReference.child("images/${System.currentTimeMillis()}")

            imagesRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    imagesRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (userId != null) {
                            val location = addPhotoLocation.text.toString()
                            uploadDataToUserSpecificFirestore(imageUrl, userCommentEditText.text.toString(), userId, location)
                        } else {
                            Toast.makeText(requireContext(), "Kullanıcı oturumu açılmamış.", Toast.LENGTH_SHORT).show()
                        }

                        imagePreview.visibility = View.VISIBLE
                        imagePreview.setImageURI(uri)
                        addImageButton.visibility = View.GONE
                        textView2.visibility = View.GONE
                        Toast.makeText(requireContext(), "Resim yüklendi.", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Yükleme başarısız: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
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
                textView2.visibility = View.GONE
            }
        }
    }
}