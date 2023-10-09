import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.Places
import com.example.wayfindr.R
import com.example.wayfindr.places.ItemClickListener

class PlacesAdapter(
    private var placesList: List<PlaceModel>,
    private val itemClickListener: ItemClickListener,
    private val placesContext: Places
) : RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

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

        holder.favoriteCheckbox.isChecked = place.isFavorite
        holder.favoriteCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // Checkbox durumu değiştiğinde, PlaceModel'deki durumu güncelle
            place.isFavorite = isChecked

            // Eğer checkbox işaretlendi ise, Places sınıfındaki fonksiyonu çağır
            if (isChecked) {
                placesContext.updateFirestoreFavorites(place.placeId)
            }
            else {
                // Eğer checkbox işaretlenmedi ise, favorilerden kaldırma işlemi yap
                placesContext.removeFromFirestoreFavorites(place.placeId)
            }
        }
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    inner class PlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val placesImage: ImageView = itemView.findViewById(R.id.placesImage)
        private val placesName: TextView = itemView.findViewById(R.id.placesName)
        private val placesDescription: TextView = itemView.findViewById(R.id.placesDescription)
        val favoriteCheckbox: CheckBox = itemView.findViewById(R.id.favoriteAdd)

        fun bind(place: PlaceModel) {
            // Resmi URL'den yüklemek için Glide kütüphanesini kullan
            Glide.with(itemView)
                .load(place.placeImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(placesImage)

            placesName.text = place.placeName
            placesDescription.text = place.placeDescription
        }
    }

    // Yeni verileri set etmek için fonksiyon
    fun setPlacesList(newPlacesList: List<PlaceModel>) {
        placesList = newPlacesList
        notifyDataSetChanged()
    }

    fun getPlaceByPlaceId(placeId: String): PlaceModel? {
        return placesList.find { it.placeId == placeId }
    }

    fun updateFavorites(favorites: Set<String>) {
        placesList.forEach { place ->
            place.isFavorite = favorites.contains(place.placeId)
        }
        notifyDataSetChanged()
    }

}