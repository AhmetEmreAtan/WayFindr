import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.places.ItemClickListener
import com.example.wayfindr.places.PlaceModel
import com.example.wayfindr.places.PlacesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PlacesAdapter(
    private var placesList: List<PlaceModel>,
    private val itemClickListener: ItemClickListener,
    private val firebaseAuth: FirebaseAuth,
    private val placesRepository: PlacesRepository
) : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    inner class PlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placesImage: ImageView = itemView.findViewById(R.id.placesImage)
        private val placesName: TextView = itemView.findViewById(R.id.placesName)
        private val placesDescription: TextView = itemView.findViewById(R.id.placesDescription)
        val favoriteAddButton: ImageView = itemView.findViewById(R.id.favoriteAdd)

        fun bind(place: PlaceModel) {
            Glide.with(itemView)
                .load(place.placeImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(placesImage)

            placesName.text = place.placeName
            placesDescription.text = place.placeDescription
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_places, parent, false)

        return PlacesViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val place = placesList[position]
        holder.bind(place)

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(place.placeId)
        }

        holder.favoriteAddButton.setOnClickListener {
            handleFavoriteButtonClick(holder, place)
        }

        updateFavoriteButton(holder, place)
    }

    private fun handleFavoriteButtonClick(holder: PlacesViewHolder, place: PlaceModel) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            placesRepository.isPlaceFavorite(userId, place.placeId) { isFavorite ->
                if (isFavorite) {
                    placesRepository.removeFromFavorites(userId, place.placeId)
                } else {
                    placesRepository.addToFavorites(userId, place)
                }

                updateFavoriteButton(holder, place)
            }
        } else {
            Log.d("AuthState", "Geçerli kullanıcı null")
            Toast.makeText(holder.itemView.context, "Lütfen giriş yapınız!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFavoriteButton(holder: PlacesViewHolder, place: PlaceModel) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()
            val userFavoritesReference = database.getReference("users").child(userId).child("favorites")

            userFavoritesReference.child(place.placeId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isFavorite = snapshot.exists()

                    if (isFavorite) {
                        holder.favoriteAddButton.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        holder.favoriteAddButton.setImageResource(R.drawable.ic_heart_outlined)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RealtimeDatabase", "Veritabanına erişim hatası: $error")
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    fun setPlacesList(newPlacesList: List<PlaceModel>) {
        Log.d("Adapter", "Current list size: ${placesList.size}, New list size: ${newPlacesList.size}")
        placesList = newPlacesList
        notifyDataSetChanged()
    }

    fun getPlaceByPlaceId(placeId: String): PlaceModel? {
        return placesList.find { it.placeId == placeId }
    }
}