import android.os.Parcel
import android.os.Parcelable
import com.example.wayfindr.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class PlaceModel(
    var placeId: String = "",
    val placeName: String = "",
    val placeDescription: String = "",
    val placeImage: String = "",
    val placeCategories: String? = "",
    val placeAddress: String,
    val placeOpeningHours: String,
    val placeDetails: String,
    val placePrice: String? = "",
    var isFavorite: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )

    constructor() : this("", "", "", "", "", "", "", "", "", false)

    fun isFavoritePlace(userId: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val userFavoritesReference = database.getReference("users").child(userId).child("favorites")

        // Mekanın belirtilen kullanıcı için favori olup olmadığını kontrol et
        userFavoritesReference.child(placeId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    fun addToFavorites(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userFavoritesReference = database.getReference("users").child(userId).child("favorites")

        val favoritePlace = mapOf(
            "placeId" to placeId,
            "placeName" to placeName,
            "placeImage" to placeImage,
            "placeDescription" to placeDescription
        )

        userFavoritesReference.child(placeId).setValue(favoritePlace)
    }

    fun removeFromFavorites(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userFavoritesReference = database.getReference("users").child(userId).child("favorites")

        userFavoritesReference.child(placeId).removeValue()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(placeId)
        parcel.writeString(placeName)
        parcel.writeString(placeDescription)
        parcel.writeString(placeImage)
        parcel.writeByte(if (isFavorite) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PlaceModel> = object : Parcelable.Creator<PlaceModel> {
            override fun createFromParcel(parcel: Parcel): PlaceModel {
                return PlaceModel(parcel)
            }

            override fun newArray(size: Int): Array<PlaceModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}
