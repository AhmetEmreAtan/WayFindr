package com.example.wayfindr.places

import PlaceModel
import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.wayfindr.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private val locationPermissionCode = 123
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0


    private val firestore = FirebaseFirestore.getInstance()

     var filterResultListener: FilterResultListener? = null

    private var selectedCategory: Int = -1
    private var selectedDistance: Int = 0

    private var radioGroupPricing: RadioGroup? = view?.findViewById(R.id.radioGroupPricing)

    private val placesCollection: CollectionReference = firestore.collection("places")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =
            inflater.inflate(R.layout.fragment_filter_bottom_sheet_fragment, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return view

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val seekBarLocation: SeekBar = view.findViewById(R.id.seekBarLocation)
        val textViewSelectedDistance: TextView = view.findViewById(R.id.textViewSelectedDistance)
        val resetButton: Button? = view.findViewById(R.id.resetButton)
        val filterButton: Button? = view.findViewById(R.id.filterButton)


        //val categoryNames = arrayOf("Tarihi Müzeler", "Bilim ve Endüstri", "Antropoloji", "Sanat", "Özel")

        val cardViews = arrayOf(
            view.findViewById<CardView>(R.id.cardview1),
            view.findViewById<CardView>(R.id.cardview2),
            view.findViewById<CardView>(R.id.cardview3),
            view.findViewById<CardView>(R.id.cardview4),
            view.findViewById<CardView>(R.id.cardview5)
        )

        for (i in cardViews.indices) {
            val cardView = cardViews[i]

            cardView.setOnClickListener {
                if (selectedCategory != i) {
                    selectedCategory = i
                    //val selectedCategoryName = categoryNames[i]

                    // Seçilen kategori ismi
                    //Log.d("Filter", "Selected Category: $selectedCategoryName")

                } else {
                    selectedCategory = -1
                }

                for (j in cardViews.indices) {
                    val card = cardViews[j]
                    card.setCardBackgroundColor(
                        if (selectedCategory == j) resources.getColor(R.color.vintage)
                        else Color.WHITE
                    )
                }
            }
        }


        resetButton?.setOnClickListener {
            radioGroupPricing?.clearCheck()
            seekBarLocation.progress = 0
            textViewSelectedDistance.text = "Seçilen Mesafe: 0 km"
            if (selectedCategory != -1) {
                cardViews[selectedCategory].setCardBackgroundColor(Color.WHITE)
                selectedCategory = -1
            }

        }

        seekBarLocation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedDistance = progress
                val selectedDistanceText = "$selectedDistance km"
                textViewSelectedDistance.text = "Seçilen Mesafe: $selectedDistanceText"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        filterButton?.setOnClickListener {
            performFiltering()
        }


        val closeButton: ImageView = view.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.d(TAG, "Back button pressed ")
                    if (isEnabled) {
                        isEnabled = false
                        activity?.supportFragmentManager?.beginTransaction()
                            ?.remove(this@FilterBottomSheetFragment)?.commit()
                    }
                }
            })

        if (hasLocationPermission()) {
            getLocation()
        } else {
            requestLocationPermission()
        }
    }

    // Kullanıcının konum izni olduğunu kontrol edin
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Kullanıcının konum iznini isteyin
    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Konum İzni Gerekli")
                .setMessage("Uygulamamızın konum bilgilerine erişmesi gerekiyor. Bu, yakınınızdaki yerleri bulmamıza yardımcı olur.")
                .setPositiveButton("İzin Ver") { dialog, which ->
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        locationPermissionCode
                    )
                }
                .setNegativeButton("Reddet") { dialog, which ->
                    // İzin reddedildiğinde veya iptal edildiğinde gereken işlemler
                }
                .create()

            alertDialog.show()
        } else {

            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            locationPermissionCode
        )
    }

    // İzin isteği sonucu
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
                getLocation()
            } else {
                Snackbar.make(
                    requireView(),
                    "Konum izni reddedildi, bu nedenle belirli özellikler kullanılamayabilir.",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    // Kullanıcının konumunu alma
    private fun getLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        userLatitude = location.latitude
                        userLongitude = location.longitude
                        //Log.d("Konum", "Enlem: $latitude, Boylam: $longitude")

                    }
                }
                .addOnFailureListener { exception ->

                    Log.e("Konum", "Konum alınamadı: ${exception.message}")
                }
        } catch (securityException: SecurityException) {
            // İzin reddedildi
        }
    }

    fun parseLatLngFromGoogleMapsUrl(url: String): Pair<Double, Double>? {
        val regex = "(@[0-9.]+,[0-9.]+)".toRegex()
        val matchResult = regex.find(url)

        if (matchResult != null) {
            val matchValue = matchResult.value.substring(1)
            val parts = matchValue.split(",")
            if (parts.size == 2) {
                val latitude = parts[0].toDoubleOrNull()
                val longitude = parts[1].toDoubleOrNull()
                if (latitude != null && longitude != null) {
                    return Pair(latitude, longitude)
                }
            }
        }
        return null
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = R * c
        return distance
    }

    //Konuma göre filtreleme yap
    private fun fetchPlacesbyLocation(
        selectedDistance: Int,
        onComplete: (List<PlaceModel>) -> Unit
    ) {
        val placesList = mutableListOf<PlaceModel>()

        val query = placesCollection.orderBy("placeName")

        query.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val placeLocation = document.getString("placeLocation")

                        if (placeLocation != null) {
                            val latLng = parseLatLngFromGoogleMapsUrl(placeLocation)
                            if (latLng != null) {
                                val (latitude, longitude) = latLng

                                // İki konum arasındaki mesafe
                                val distanceInKilometers = calculateDistance(
                                    userLatitude,
                                    userLongitude,
                                    latitude,
                                    longitude
                                )

                                val distanceInKilometersInt = distanceInKilometers.toInt()

                                Log.d("Mesafe: ", "$distanceInKilometersInt km")

                                if (distanceInKilometersInt <= selectedDistance) {
                                    val placeId = document.getString("placeId") ?: ""
                                    val placeName = document.getString("placeName") ?: ""
                                    val placeDescription =
                                        document.getString("placeDescription") ?: ""
                                    val placeImage = document.getString("placeImage") ?: ""
                                    val placeCategories =
                                        document.getString("placeCategories") ?: ""
                                    val placeAddress = document.getString("placeAddress") ?: ""
                                    val placeOpeningHours =
                                        document.getString("placeOpeningHours") ?: ""
                                    val placeDetails = document.getString("placeDetails") ?: ""
                                    val placePrice = document.getString("placePrice") ?: ""
                                    val isFavorite = document.getBoolean("isFavorite") ?: false

                                    val place = PlaceModel(
                                        placeId,
                                        placeName,
                                        placeDescription,
                                        placeImage,
                                        placeCategories,
                                        placeAddress,
                                        placeOpeningHours,
                                        placeDetails,
                                        placePrice,
                                        isFavorite
                                    )
                                    placesList.add(place)
                                    for (place in placesList) {
                                        Log.d(
                                            "PlaceByLocation",
                                            "Name: ${place.placeName}, Category: ${place.placeCategories}, Pricing: ${place.placePrice}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.e("Veri Alınmadı: ", "${task.exception}")
                }
                onComplete(placesList)
            }
    }

    // Kategoriye göre filtreleme yap
    private fun fetchPlacesByCategory(
        selectedCategoryName: String?,
        onComplete: (List<PlaceModel>) -> Unit
    ) {
        val filteredPlacesList = mutableListOf<PlaceModel>()

        var query: Query = placesCollection

        // Seçili kategoriye göre filtreleme yap
        if (!selectedCategoryName.isNullOrBlank()) {
            query = query.whereEqualTo("placeCategories", selectedCategoryName)
        }

        query.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val placeId = document.getString("placeId") ?: ""
                        val placeName = document.getString("placeName") ?: ""
                        val placeDescription = document.getString("placeDescription") ?: ""
                        val placeImage = document.getString("placeImage") ?: ""
                        val placeCategories = document.getString("placeCategories") ?: ""
                        val placeAddress = document.getString("placeAddress") ?: ""
                        val placeOpeningHours = document.getString("placeOpeningHours") ?: ""
                        val placeDetails = document.getString("placeDetails") ?: ""
                        val placePrice = document.getString("placePrice") ?: ""
                        val isFavorite = document.getBoolean("isFavorite") ?: false

                        val place = PlaceModel(
                            placeId,
                            placeName,
                            placeDescription,
                            placeImage,
                            placeCategories,
                            placeAddress,
                            placeOpeningHours,
                            placeDetails,
                            placePrice,
                            isFavorite
                        )
                        filteredPlacesList.add(place)
                    }
                    onComplete(filteredPlacesList)

                    // Filtrelenmiş verilere eriş
                    for (place in filteredPlacesList) {
                        // Yapılacak işlemler
                        Log.d(
                            "PlaceCategory",
                            " Name: ${place.placeName}, Category: ${place.placeCategories}, Pricing: ${place.placePrice}"
                        )
                    }
                } else {
                    Log.e("Firestore", "Error getting documents: ${task.exception}")
                }
            }
    }


    // Ücretlendirmeye göre filtreleme yap
    private fun fetchPlacesByPricing(
        selectedPricing: String?,
        onComplete: (List<PlaceModel>) -> Unit
    ) {
        val filteredPlacesList = mutableListOf<PlaceModel>()

        var query: Query = placesCollection

        // Ücretlendirmeye göre filtreleme yap
        if (!selectedPricing.isNullOrBlank()) {
            query = query.whereEqualTo("placePrice", selectedPricing)
        }

        query.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val placeId = document.getString("placeId") ?: ""
                        val placeName = document.getString("placeName") ?: ""
                        val placeDescription = document.getString("placeDescription") ?: ""
                        val placeImage = document.getString("placeImage") ?: ""
                        val placeCategories = document.getString("placeCategories") ?: ""
                        val placeAddress = document.getString("placeAddress") ?: ""
                        val placeOpeningHours = document.getString("placeOpeningHours") ?: ""
                        val placeDetails = document.getString("placeDetails") ?: ""
                        val placePrice = document.getString("placePrice") ?: ""
                        val isFavorite = document.getBoolean("isFavorite") ?: false

                        val place = PlaceModel(
                            placeId,
                            placeName,
                            placeDescription,
                            placeImage,
                            placeCategories,
                            placeAddress,
                            placeOpeningHours,
                            placeDetails,
                            placePrice,
                            isFavorite
                        )
                        filteredPlacesList.add(place)
                    }
                    onComplete(filteredPlacesList)

                    // Filtrelenmiş verilere eriş
                    for (place in filteredPlacesList) {
                        // Yapılacak işlemler
                        Log.d(
                            "PlacePrice",
                            " Name: ${place.placeName}, Category: ${place.placeCategories}, Pricing: ${place.placePrice}"
                        )
                    }
                } else {
                    Log.e("Firestore", "Error getting documents: ${task.exception}")
                }
            }
    }

    private fun findCommonPlaces(
        list1: List<PlaceModel>,
        list2: List<PlaceModel>,
        list3: List<PlaceModel>
    ): List<PlaceModel> {
        val commonPlaces = mutableListOf<PlaceModel>()

        for (place1 in list1) {
            for (place2 in list2) {
                for (place3 in list3) {
                    // Üç listeye de aynı anda bulunan bir yer varsa, commonPlaces listesine ekle
                    if (place1.placeId == place2.placeId && place1.placeId == place3.placeId) {
                        commonPlaces.add(place1)
                    }
                }
            }
        }

        return commonPlaces
    }

    private fun performFiltering() {
        val selectedPricing = when (radioGroupPricing?.checkedRadioButtonId) {
            R.id.radioButtonPaid -> "Ücretli"
            R.id.radioButtonFree -> "Ücretsiz"
            else -> null
        }

        val selectedCategoryName = when (selectedCategory) {
            0 -> "Tarihi Müzeler"
            1 -> "Bilim ve Endüstri"
            2 -> "Antropoloji"
            3 -> "Sanat"
            4 -> "Özel"
            else -> null
        }

        fetchPlacesbyLocation(selectedDistance) { byLocationList ->
            Log.d("Filter", "byLocationList size: ${byLocationList.size}")

            if (!selectedPricing.isNullOrBlank()) {
                fetchPlacesByPricing(selectedPricing) { byPriceList ->
                    Log.d("Filter", "byPriceList size: ${byPriceList.size}")

                    if (selectedCategoryName != null) {
                        fetchPlacesByCategory(selectedCategoryName) { byCategoryList ->
                            Log.d("Filter", "byCategoryList size: ${byCategoryList.size}")

                            val filteredPlaces = findCommonPlaces(byCategoryList, byPriceList, byLocationList)

                            // Sonuçları FilterResultListener üzerinden Places fragment'ına iletiyoruz
                            notifyFilterResults(filteredPlaces)
                        }
                    } else {
                        // Eğer kategori seçilmemişse, sadece ücretlendirme filtresini kullan
                        notifyFilterResults(byPriceList)
                    }
                }
            } else {
                // Eğer ücretlendirme belirtilmemişse, sadece kategori filtresini kullan
                if (selectedCategoryName != null) {
                    fetchPlacesByCategory(selectedCategoryName) { byCategoryList ->
                        Log.d("Filter", "byCategoryList size: ${byCategoryList.size}")

                        // Sonuçları FilterResultListener üzerinden Places fragment'ına iletiyoruz
                        notifyFilterResults(byCategoryList)
                    }
                } else {
                    // Eğer hiçbir filtreleme yapılmamışsa, tüm yerleri görüntüle
                    notifyFilterResults(byLocationList)
                }
            }
        }

        // Alt sayfayı kapat
        dismiss()
    }


    private fun notifyFilterResults(places: List<PlaceModel>) {
        filterResultListener?.onFilterResult(places)
    }


}