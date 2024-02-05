package com.example.wayfindr.places

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.wayfindr.R

class CustomSpinnerAdapter(
    context: Context,
    resource: Int,
    objects: List<SpinnerItem>
) : ArrayAdapter<SpinnerItem>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = convertView ?: inflater.inflate(R.layout.custom_spinner_item, parent, false)

        val iconImageView: ImageView = itemView.findViewById(R.id.spinner_icon)
        val text1TextView: TextView = itemView.findViewById(android.R.id.text1)

        val item = getItem(position)
        if (item != null) {
            iconImageView.setImageResource(item.iconResId)
            text1TextView.text = item.text
        }

        return itemView
    }
}
