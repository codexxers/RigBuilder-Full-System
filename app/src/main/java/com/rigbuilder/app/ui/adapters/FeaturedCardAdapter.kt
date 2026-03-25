package com.rigbuilder.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rigbuilder.app.R
import com.rigbuilder.app.model.PreBuiltData
import java.text.NumberFormat
import java.util.Locale

class FeaturedCardAdapter(
    private val items: List<PreBuiltData>,
    private val onItemClick: (PreBuiltData) -> Unit
) : RecyclerView.Adapter<FeaturedCardAdapter.ViewHolder>() {

    private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.card_pc_name)
        val tierBadge: TextView = view.findViewById(R.id.card_tier_badge)
        val specBlurb: TextView = view.findViewById(R.id.card_spec_blurb)
        val price: TextView = view.findViewById(R.id.card_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_featured_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position % items.size]
        holder.name.text = item.name
        holder.tierBadge.text = item.tier
        holder.specBlurb.text = "${item.cpu} · ${item.gpu} · ${item.ram}"
        holder.price.text = pesoFmt.format(item.price.toDouble())
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    fun getRealPosition(position: Int) = position % items.size

    override fun getItemCount() = if (items.isEmpty()) 0 else Int.MAX_VALUE
}
