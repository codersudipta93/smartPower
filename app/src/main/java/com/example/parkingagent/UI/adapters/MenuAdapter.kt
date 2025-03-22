package com.example.parkingagent.UI.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parkingagent.R
import com.example.parkingagent.data.remote.models.Menu.MenuDataItem

class MenuAdapter(private val menuList: List<MenuDataItem>, private val onItemClick: (MenuDataItem) -> Unit) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuText: TextView = itemView.findViewById(R.id.menuText)
        val menuIcon: ImageView = itemView.findViewById(R.id.menuIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuList[position]
        holder.menuText.text = menuItem.menuName

        holder.menuIcon.setImageResource(R.drawable.icon_car)  // Change this if you have different icons
        // Use Glide to load the image from the URL into the ImageView
        Glide.with(holder.itemView.context)
            .load(menuItem.icon)
            .error(R.drawable.icon_car)
            .into(holder.menuIcon)
        holder.itemView.setOnClickListener { onItemClick(menuItem) }
    }

    override fun getItemCount() = menuList.size
}
