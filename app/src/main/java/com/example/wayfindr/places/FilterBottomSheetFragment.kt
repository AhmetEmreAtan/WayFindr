package com.example.wayfindr.places

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
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

        val radioGroupPricing: RadioGroup? = view.findViewById(R.id.radioGroupPricing)
        val radioButtonFree: RadioButton? = view.findViewById(R.id.radioButtonFree)
        val radioButtonPaid: RadioButton? = view.findViewById(R.id.radioButtonPaid)

        val selectedPricing = when (radioGroupPricing?.checkedRadioButtonId) {
            R.id.radioButtonPaid -> "Ücretli"
            R.id.radioButtonFree -> "Ücretsiz"
            else -> null
        }

        val spinnerDistricts: Spinner? = view.findViewById(R.id.spinnerDistricts)
        val districtAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.istanbul_districts))
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistricts?.adapter = districtAdapter


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
            radioButtonFree?.isChecked = false
            radioButtonPaid?.isChecked = false
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
        val uri = Uri.parse(url)
        val latitude = uri.getQueryParameter("q")?.split(",")?.get(0)?.toDoubleOrNull()
        val longitude = uri.getQueryParameter("q")?.split(",")?.get(1)?.toDoubleOrNull()

        if (latitude != null && longitude != null && isValidLatitude(latitude) && isValidLongitude(longitude)) {
            return Pair(latitude, longitude)
        }
        return null
    }
    fun isValidLatitude(latitude: Double): Boolean {
        return latitude in -90.0..90.0
    }

    fun isValidLongitude(longitude: Double): Boolean {
        return longitude in -180.0..180.0
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

    private fun filterPlacesByDistance(
        places: List<PlaceModel>,
        selectedDistance: Int,
        userLatitude: Double,
        userLongitude: Double
    ): List<PlaceModel> {
        val filteredPlaces = mutableListOf<PlaceModel>()

        for (place in places) {
            val latLng = parseLatLngFromGoogleMapsUrl(place.placeLocation)
            if (latLng != null) {
                val (latitude, longitude) = latLng
                val distanceInKilometers = calculateDistance(
                    userLatitude,
                    userLongitude,
                    latitude,
                    longitude
                )

                if (distanceInKilometers <= selectedDistance) {
                    filteredPlaces.add(place)
                    Log.d("Mesafe: ", "$distanceInKilometers km")
                }
            }
        }
        return filteredPlaces
    }

    //Konuma göre filtreleme yap
    private fun fetchPlacesByLocation(
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
                                ).toInt()

                                if (distanceInKilometers <= selectedDistance) {

                                    Log.d("Mesafe: ", "$distanceInKilometers km")

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
                                    val placeLocationn = document.getString("placeLocation") ?: ""
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
                                        placeLocationn,
                                        placePrice,
                                        isFavorite
                                    )
                                    placesList.add(place)
                                }
                            }
                        }
                    }

                    val filteredList = filterPlacesByDistance(
                        placesList,
                        selectedDistance,
                        userLatitude,
                        userLongitude
                    )

                    // Loglama işlemini burada gerçekleştiriyoruz
                    if (filteredList.isNotEmpty()) {
                        for (place in filteredList) {
                            Log.d(
                                "PlaceByLocation",
                                "Name: ${place.placeName}, Category: ${place.placeCategories}, Pricing: ${place.placePrice}"
                            )
                        }
                    } else {
                        // Filtrelenmiş liste boşsa bir hata olabilir
                        Log.e("PlaceByLocation", "Filtrelenmiş liste boş.")
                    }

                    // Sonuçları onComplete ile iletiyoruz
                    onComplete(filteredList)
                } else {
                    Log.e("Veri Alınmadı: ", "${task.exception}")

                    // Hata durumunda onComplete ile boş bir liste gönderiyoruz
                    onComplete(emptyList())
                }
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
                        val placeLocation = document.getString("placeLocation") ?: ""
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
                            placeLocation,
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
                        val placeLocation = document.getString("placeLocation") ?: ""
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
                            placeLocation,
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
        // Her filtre tipi için özel bir küme oluştur
        val set1 = list1.map { it.placeId }.toSet()
        val set2 = list2.map { it.placeId }.toSet()
        val set3 = list3.map { it.placeId }.toSet()

        // Kümeleri kesiştirerek ortak olan yerleri bul
        val commonIds = set1.intersect(set2).intersect(set3)

        // Ortak olan yerleri filtrele
        return list1.filter { it.placeId in commonIds }
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

        fetchPlacesByCategory(selectedCategoryName) { byCategoryList ->
            Log.d("Filter", "byCategoryList size: ${byCategoryList.size}")

            if (!selectedPricing.isNullOrBlank()) {
                fetchPlacesByPricing(selectedPricing) { byPriceList ->
                    Log.d("Filter", "byPriceList size: ${byPriceList.size}")

                    fetchPlacesByLocation(selectedDistance) { byLocationList ->
                        Log.d("Filter", "byLocationList size: ${byLocationList.size}")

                        val filteredPlaces = findCommonPlaces(byCategoryList, byPriceList, byLocationList)

                        // Sonuçları FilterResultListener üzerinden Places fragment'ına iletiyoruz
                        notifyFilterResults(filteredPlaces)

                        // Alt sayfayı kapat
                        dismiss()
                    }
                }
            } else {
                // Eğer ücretlendirme belirtilmemişse, sadece kategori filtresini kullan
                notifyFilterResults(byCategoryList)

                // Alt sayfayı kapat
                dismiss()
            }
        }
    }

    private fun notifyFilterResults(places: List<PlaceModel>) {
        filterResultListener?.onFilterResult(places)
    }
}