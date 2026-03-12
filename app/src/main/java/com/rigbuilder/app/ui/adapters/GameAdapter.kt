package com.rigbuilder.app.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rigbuilder.app.R
import com.rigbuilder.app.viewmodel.GamePerformance

class GameAdapter : ListAdapter<GamePerformance, GameAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gameName: TextView = view.findViewById(R.id.game_name)
        val gameCover: ImageView = view.findViewById(R.id.game_cover)
        val cpuBar: View = view.findViewById(R.id.cpu_bar)
        val gpuBar: View = view.findViewById(R.id.gpu_bar)
        val ramBar: View = view.findViewById(R.id.ram_bar)
        val statusIcon: ImageView = view.findViewById(R.id.status_icon)
        val statusLabel: TextView = view.findViewById(R.id.status_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val perf = getItem(position)
        val ctx = holder.itemView.context

        holder.gameName.text = perf.game.name

        // Load game cover from the hardcoded drawable map
        val coverRes = COVER_MAP[perf.game.imageUrl]
        if (coverRes != null) {
            holder.gameCover.setImageResource(coverRes)
        } else {
            holder.gameCover.setImageDrawable(null)
        }

        // Status color and icon
        val statusColor = when (perf.statusLabel) {
            "Runs Great" -> ContextCompat.getColor(ctx, R.color.synergy_excellent)
            "Playable" -> ContextCompat.getColor(ctx, R.color.synergy_adequate)
            else -> ContextCompat.getColor(ctx, R.color.synergy_not_advised)
        }

        val statusIconRes = when (perf.statusLabel) {
            "Runs Great" -> android.R.drawable.ic_menu_manage
            "Playable" -> android.R.drawable.ic_dialog_alert
            else -> android.R.drawable.ic_delete
        }

        holder.statusIcon.setImageResource(statusIconRes)
        holder.statusIcon.imageTintList = ColorStateList.valueOf(statusColor)
        holder.statusLabel.text = perf.statusLabel
        holder.statusLabel.setTextColor(statusColor)

        // Requirement bars
        setBarWidth(holder.cpuBar, perf.cpuMeetsMin, perf.cpuMeetsRec, ctx)
        setBarWidth(holder.gpuBar, perf.gpuMeetsMin, perf.gpuMeetsRec, ctx)
        setBarWidth(holder.ramBar, perf.ramMeetsMin, perf.ramMeetsRec, ctx)
    }

    private fun setBarWidth(bar: View, meetsMin: Boolean, meetsRec: Boolean, ctx: android.content.Context) {
        val fraction = when {
            meetsRec -> 1f
            meetsMin -> 0.5f
            else -> 0.15f
        }
        val color = when {
            meetsRec -> ContextCompat.getColor(ctx, R.color.synergy_excellent)
            meetsMin -> ContextCompat.getColor(ctx, R.color.synergy_adequate)
            else -> ContextCompat.getColor(ctx, R.color.synergy_not_advised)
        }

        bar.post {
            val parent = bar.parent as? View ?: return@post
            val params = bar.layoutParams
            params.width = (parent.width * fraction).toInt()
            bar.layoutParams = params
            bar.setBackgroundColor(color)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GamePerformance>() {
        override fun areItemsTheSame(a: GamePerformance, b: GamePerformance) =
            a.game.id == b.game.id
        override fun areContentsTheSame(a: GamePerformance, b: GamePerformance) = a == b
    }

    companion object {
        /** Maps the imageUrl filename (from games.json) directly to a compiled drawable resource ID. */
        val COVER_MAP = mapOf(
            "game_apex.jpg"        to R.drawable.game_apex,
            "game_arc_raiders.jpg" to R.drawable.game_arc_raiders,
            "game_black_myth.jpg"  to R.drawable.game_black_myth,
            "game_cs2.jpg"         to R.drawable.game_cs2,
            "game_cyberpunk.jpg"   to R.drawable.game_cyberpunk,
            "game_elden_ring.jpg"  to R.drawable.game_elden_ring,
            "game_fortnite.jpg"    to R.drawable.game_fortnite,
            "game_gtav.jpg"        to R.drawable.game_gtav,
            "game_gtavi.jpg"       to R.drawable.game_gtavi,
            "game_hogwarts.jpg"    to R.drawable.game_hogwarts,
            "game_lol.jpg"         to R.drawable.game_lol,
            "game_rdr2.png"        to R.drawable.game_rdr2,
            "game_valorant.png"    to R.drawable.game_valorant
        )
    }
}
