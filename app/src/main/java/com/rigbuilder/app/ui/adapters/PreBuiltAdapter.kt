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

class PreBuiltAdapter(
    private val items: List<PreBuiltData>,
    private val onItemClick: (PreBuiltData) -> Unit
) : RecyclerView.Adapter<PreBuiltAdapter.ViewHolder>() {

    private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.prebuilt_name)
        val specSummary: TextView = view.findViewById(R.id.prebuilt_spec_summary)
        val tierBadge: TextView = view.findViewById(R.id.prebuilt_tier_badge)
        val price: TextView = view.findViewById(R.id.prebuilt_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prebuilt_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.specSummary.text = "${item.cpu} · ${item.gpu} · ${item.ram}"
        holder.tierBadge.text = item.tier
        holder.price.text = pesoFmt.format(item.price.toDouble())
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
