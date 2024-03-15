import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.places.PlaceModel

class PopularAdapter(
    private var placesList: List<PlaceModel>,
    private var itemClickListener: OnItemClickListener

) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {
    inner class PopularViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeImage: ImageView = itemView.findViewById(R.id.imageViewPopular)
        private val placeName: TextView = itemView.findViewById(R.id.popularPlaceName)

        fun bind(place: PlaceModel) {
            Glide.with(itemView)
                .load(place.placeImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(placeImage)

            placeName.text = place.placeName
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular, parent, false)

        return PopularViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        val place = placesList[position]
        holder.bind(place)

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(place.placeId)
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

    interface OnItemClickListener {
        fun onItemClick(placeId: String)
    }
}
