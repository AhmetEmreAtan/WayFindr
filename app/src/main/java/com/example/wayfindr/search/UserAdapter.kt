package com.example.wayfindr.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class UserAdapter(options: FirestoreRecyclerOptions<User>, private val currentUserId: String) :
    FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder>(options) {

    private var filteredList: List<User> = mutableListOf()

    init {
        filteredList = snapshots.toList().filter { it.userId != currentUserId }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun filter(queryText: String) {
        val query = queryText.lowercase()

        filteredList = if (query.isEmpty()) {
            snapshots.toList().filter { it.userId != currentUserId }
        } else {
            snapshots.filter { it.username?.lowercase()?.contains(query) == true && it.userId != currentUserId }
        }

        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            val userNameTextView = itemView.findViewById<TextView>(R.id.userNameTextView)
            userNameTextView.text = user.username

            val firstNameTextView = itemView.findViewById<TextView>(R.id.firstNameTextView)
            firstNameTextView.text = user.firstName

            val profileImageView = itemView.findViewById<ImageView>(R.id.profilepicture_search)
            Glide.with(itemView)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.profilephotoicon)
                .into(profileImageView)

            itemView.setOnClickListener {
                val userId = user.userId
                val profileFragment = UserProfile.newInstance(userId)
                val transaction = (itemView.context as AppCompatActivity)
                    .supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, profileFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }
}
