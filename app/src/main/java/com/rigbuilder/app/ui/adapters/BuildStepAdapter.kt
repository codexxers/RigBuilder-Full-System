package com.rigbuilder.app.ui.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.rigbuilder.app.R
import com.rigbuilder.app.model.ComponentCategory
import com.rigbuilder.app.viewmodel.BuildState
import java.text.NumberFormat
import java.util.Locale

data class BuildStepItem(
    val category: ComponentCategory,
    val stepNumber: Int,
    val isActive: Boolean,
    val isCompleted: Boolean,
    val isLocked: Boolean,
    val isOptional: Boolean,
    val selectedName: String?,
    val selectedPrice: Double?
)

class BuildStepAdapter(
    private val onCategoryClick: (ComponentCategory) -> Unit,
    private val onRemove: (ComponentCategory) -> Unit = {}
) : ListAdapter<BuildStepItem, BuildStepAdapter.ViewHolder>(DiffCallback()) {

    private val pesoFormat = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val stepIndicator: View = view.findViewById(R.id.step_indicator)
        val stepNumber: TextView = view.findViewById(R.id.step_number)
        val stepIcon: ImageView = view.findViewById(R.id.step_icon)
        val stepThumb: ImageView = view.findViewById(R.id.step_thumb)
        val categoryName: TextView = view.findViewById(R.id.category_name)
        val selectedName: TextView = view.findViewById(R.id.selected_name)
        val selectedPrice: TextView = view.findViewById(R.id.selected_price)
        val btnRemove: ImageView = view.findViewById(R.id.btn_remove)
        val arrowIcon: ImageView = view.findViewById(R.id.arrow_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_build_step, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bindStep(holder, getItem(position))
    }

    /** Public binding method — can be called from external multi-type adapters. */
    fun bindStep(holder: ViewHolder, item: BuildStepItem) {
        val ctx = holder.itemView.context

        // Card background and stroke
        val borderColor = when {
            item.isOptional -> ContextCompat.getColor(ctx, R.color.synergy_good)
            item.isActive -> ContextCompat.getColor(ctx, R.color.rig_red)
            item.isCompleted -> ContextCompat.getColor(ctx, R.color.rig_green)
            else -> ContextCompat.getColor(ctx, R.color.rig_surface_variant)
        }
        holder.card.strokeColor = borderColor
        holder.card.setCardBackgroundColor(
            if (item.isActive || item.isOptional) ContextCompat.getColor(ctx, R.color.rig_surface)
            else ContextCompat.getColor(ctx, R.color.rig_card)
        )

        // Step indicator circle
        val indicatorBg = holder.stepIndicator.background
        if (indicatorBg is GradientDrawable) {
            val bgColor = when {
                item.isCompleted -> ContextCompat.getColor(ctx, R.color.rig_green)
                item.isOptional -> ContextCompat.getColor(ctx, R.color.synergy_good)
                item.isActive -> ContextCompat.getColor(ctx, R.color.rig_red)
                else -> ContextCompat.getColor(ctx, R.color.rig_surface_variant)
            }
            indicatorBg.setColor(bgColor)
        } else {
            val bg = GradientDrawable()
            bg.shape = GradientDrawable.OVAL
            bg.setColor(when {
                item.isCompleted -> ContextCompat.getColor(ctx, R.color.rig_green)
                item.isOptional -> ContextCompat.getColor(ctx, R.color.synergy_good)
                item.isActive -> ContextCompat.getColor(ctx, R.color.rig_red)
                else -> ContextCompat.getColor(ctx, R.color.rig_surface_variant)
            })
            holder.stepIndicator.background = bg
        }

        // Thumbnail vs step indicator
        if (item.isCompleted || item.selectedName != null) {
            // Show category thumbnail, hide circle
            holder.stepIndicator.visibility = View.GONE
            holder.stepThumb.visibility = View.VISIBLE
            holder.stepThumb.setImageResource(getCategoryThumbRes(item.category))
        } else {
            // Show circle indicator, hide thumbnail
            holder.stepIndicator.visibility = View.VISIBLE
            holder.stepThumb.visibility = View.GONE

            // Step number vs icon
            when {
                item.isOptional -> {
                    holder.stepNumber.visibility = View.GONE
                    holder.stepIcon.visibility = View.VISIBLE
                    holder.stepIcon.setImageResource(android.R.drawable.ic_media_next)
                    holder.stepIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.rig_white))
                }
                item.isLocked -> {
                    holder.stepNumber.visibility = View.GONE
                    holder.stepIcon.visibility = View.VISIBLE
                    holder.stepIcon.setImageResource(android.R.drawable.ic_lock_idle_lock)
                    holder.stepIcon.setColorFilter(ContextCompat.getColor(ctx, R.color.rig_gray))
                }
                else -> {
                    holder.stepNumber.visibility = View.VISIBLE
                    holder.stepIcon.visibility = View.GONE
                    holder.stepNumber.text = "${item.stepNumber}"
                }
            }
        }

        // Category name
        holder.categoryName.text = item.category.displayName
        holder.categoryName.setTextColor(
            if (item.isLocked) ContextCompat.getColor(ctx, R.color.rig_gray)
            else ContextCompat.getColor(ctx, R.color.rig_white)
        )

        // Selected name
        if (item.selectedName != null) {
            holder.selectedName.visibility = View.VISIBLE
            holder.selectedName.text = item.selectedName
            holder.selectedName.setTextColor(
                if (item.isOptional) ContextCompat.getColor(ctx, R.color.synergy_good)
                else ContextCompat.getColor(ctx, R.color.rig_gray)
            )
        } else if (item.isActive) {
            holder.selectedName.visibility = View.VISIBLE
            holder.selectedName.text = "Tap to select"
            holder.selectedName.setTextColor(ContextCompat.getColor(ctx, R.color.rig_red))
        } else {
            holder.selectedName.visibility = View.GONE
        }

        // Price
        if (item.selectedPrice != null) {
            holder.selectedPrice.visibility = View.VISIBLE
            holder.selectedPrice.text = pesoFormat.format(item.selectedPrice)
        } else {
            holder.selectedPrice.visibility = View.GONE
        }

        // Arrow (for active step)
        if (!item.isLocked && !item.isCompleted && item.isActive && !item.isOptional) {
            holder.arrowIcon.visibility = View.VISIBLE
        } else {
            holder.arrowIcon.visibility = View.GONE
        }

        // Remove button (X) — shown only when part is selected
        if (item.selectedName != null && !item.isOptional) {
            holder.btnRemove.visibility = View.VISIBLE
            holder.btnRemove.setOnClickListener {
                onRemove(item.category)
            }
        } else {
            holder.btnRemove.visibility = View.GONE
            holder.btnRemove.setOnClickListener(null)
        }

        // Click
        holder.card.isClickable = !item.isLocked
        holder.card.setOnClickListener {
            if (!item.isLocked) onCategoryClick(item.category)
        }
    }

    private fun getCategoryThumbRes(cat: ComponentCategory): Int = when (cat) {
        ComponentCategory.CPU -> R.drawable.thumb_cpu
        ComponentCategory.MOTHERBOARD -> R.drawable.thumb_motherboard
        ComponentCategory.RAM -> R.drawable.thumb_ram
        ComponentCategory.GPU -> R.drawable.thumb_gpu
        ComponentCategory.STORAGE -> R.drawable.thumb_storage
        ComponentCategory.COOLER -> R.drawable.thumb_cooler
        ComponentCategory.CASE -> R.drawable.thumb_case
        ComponentCategory.PSU -> R.drawable.thumb_psu
        ComponentCategory.FAN -> R.drawable.thumb_cooler
    }

    class DiffCallback : DiffUtil.ItemCallback<BuildStepItem>() {
        override fun areItemsTheSame(a: BuildStepItem, b: BuildStepItem) =
            a.category == b.category

        override fun areContentsTheSame(a: BuildStepItem, b: BuildStepItem) = a == b
    }
}

// ── Helpers to extract data from BuildState ──────────────────────

fun getSelectedName(cat: ComponentCategory, state: BuildState): String? = when (cat) {
    ComponentCategory.CPU -> state.selectedCpu?.name
    ComponentCategory.MOTHERBOARD -> state.selectedMotherboard?.name
    ComponentCategory.RAM -> state.selectedRam?.let { "${it.name}${state.selectedRamVariant?.let { v -> " ($v)" } ?: ""}" }
    ComponentCategory.GPU -> state.selectedGpu?.name
    ComponentCategory.STORAGE -> state.selectedStorage?.let { "${it.name}${state.selectedStorageVariant?.let { v -> " ($v)" } ?: ""}" }
    ComponentCategory.COOLER -> state.selectedCooler?.name
    ComponentCategory.CASE -> state.selectedCase?.let { "${it.name}${state.selectedCaseVariant?.let { v -> " ($v)" } ?: ""}" }
    ComponentCategory.PSU -> state.selectedPsu?.name
    ComponentCategory.FAN -> state.selectedFan?.let { "${it.name} ×${state.selectedFanQuantity}" }
}

fun getSelectedPrice(cat: ComponentCategory, state: BuildState): Double? = when (cat) {
    ComponentCategory.CPU -> state.selectedCpu?.price
    ComponentCategory.MOTHERBOARD -> state.selectedMotherboard?.price
    ComponentCategory.RAM -> state.selectedRam?.price
    ComponentCategory.GPU -> state.selectedGpu?.price
    ComponentCategory.STORAGE -> state.selectedStorage?.price
    ComponentCategory.COOLER -> state.selectedCooler?.price
    ComponentCategory.CASE -> state.selectedCase?.price
    ComponentCategory.PSU -> state.selectedPsu?.price
    ComponentCategory.FAN -> state.selectedFan?.let { it.price * state.selectedFanQuantity / it.quantity }
}
