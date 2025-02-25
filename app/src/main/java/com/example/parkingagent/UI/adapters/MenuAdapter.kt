package com.example.parkingagent.UI.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingagent.R

class MenuAdapter(private val menuList: List<String>, private val onItemClick: (String) -> Unit) :
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
        holder.menuText.text = menuItem
        holder.menuIcon.setImageResource(R.drawable.ic_launcher_foreground) // Change this if you have different icons
        holder.itemView.setOnClickListener { onItemClick(menuItem) }
    }

    override fun getItemCount() = menuList.size
}
