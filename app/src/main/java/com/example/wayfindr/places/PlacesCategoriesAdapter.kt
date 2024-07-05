package com.example.wayfindr.places

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R
import com.example.wayfindr.databinding.ItemCategoriesBinding

class PlacesCategoriesAdapter(private val categoriesList: List<PlacesCategories>) :
    RecyclerView.Adapter<PlacesCategoriesAdapter.PlacesCategoriesViewHolder>() {

    private var onItemClickListener: ((PlacesCategories) -> Unit)? = null
    private val selectedItems = BooleanArray(categoriesList.size)

    inner class PlacesCategoriesViewHolder(binding: ItemCategoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val categoryImage = binding.categoryImage
        val categoryName = binding.categoryName
        val categoryCardView = binding.categoryCardView

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    selectedItems[position] = !selectedItems[position]
                    notifyItemChanged(position)
                    onItemClickListener?.let { click ->
                        click(categoriesList[position])
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesCategoriesViewHolder {
        val binding =
            ItemCategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlacesCategoriesViewHolder(binding)
    }

    override fun getItemCount(): Int = categoriesList.size

    override fun onBindViewHolder(holder: PlacesCategoriesViewHolder, position: Int) {
        val category = categoriesList[position]
        holder.categoryImage.setImageResource(category.imageResId)
        holder.categoryName.text = category.name

        val context = holder.itemView.context
        val selectedColor = ContextCompat.getColor(context, R.color.green)
        val defaultColor = ContextCompat.getColor(context, R.color.begie)

        holder.categoryCardView.setCardBackgroundColor(
            if (selectedItems[position]) selectedColor else defaultColor
        )
    }

    fun setOnItemClickListener(listener: (PlacesCategories) -> Unit) {
        onItemClickListener = listener
    }
}