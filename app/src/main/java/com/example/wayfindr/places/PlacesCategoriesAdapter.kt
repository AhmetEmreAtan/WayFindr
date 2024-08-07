package com.example.wayfindr.places

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R

class PlacesCategoriesAdapter(
    private val categoriesList: List<PlacesCategories>
) : RecyclerView.Adapter<PlacesCategoriesAdapter.CategoryViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryCardView: CardView = itemView.findViewById(R.id.categoryCardView)
        val categoryImage: ImageView = itemView.findViewById(R.id.categoryImage)
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val category = categoriesList[position]
                    onItemClickListener?.invoke(category)
                    updateSelectedPosition(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categories, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoriesList[position]

        holder.categoryName.text = category.name
        holder.categoryImage.setImageResource(category.imageResId)

        if (selectedPosition == position) {
            holder.categoryCardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.green)
            )
        } else {
            holder.categoryCardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.begie)
            )
        }
    }

    override fun getItemCount() = categoriesList.size

    private var onItemClickListener: ((PlacesCategories) -> Unit)? = null

    fun setOnItemClickListener(listener: (PlacesCategories) -> Unit) {
        onItemClickListener = listener
    }

    private fun updateSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }
}