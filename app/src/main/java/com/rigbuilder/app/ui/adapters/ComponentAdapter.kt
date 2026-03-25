package com.rigbuilder.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.rigbuilder.app.R
import com.rigbuilder.app.data.entity.*
import com.rigbuilder.app.data.repository.CaseCompatibility
import com.rigbuilder.app.data.repository.MotherboardWithSynergy
import com.rigbuilder.app.model.*
import java.text.NumberFormat
import java.util.Locale

data class ComponentItem(
    val id: Long,
    val data: Any, // The actual entity or wrapper
    val recommendedPsuWatts: Int = 0,
    val availableFanSlots: Int = 0
)

class ComponentAdapter(
    private val onAddClick: (Any, String?) -> Unit,
    private val onFullSpec: (Any) -> Unit,
    private val isReadOnly: Boolean = false
) : ListAdapter<ComponentItem, ComponentAdapter.ViewHolder>(DiffCallback()) {

    private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumb: ImageView = view.findViewById(R.id.component_thumb)
        val name: TextView = view.findViewById(R.id.component_name)
        val brand: TextView = view.findViewById(R.id.component_brand)
        val specsContainer: LinearLayout = view.findViewById(R.id.specs_container)
        val warning: TextView = view.findViewById(R.id.warning_text)
        val price: TextView = view.findViewById(R.id.component_price)
        val variantDropdownLayout: TextInputLayout = view.findViewById(R.id.variant_dropdown_layout)
        val variantDropdown: android.widget.AutoCompleteTextView = view.findViewById(R.id.variant_dropdown)
        val variantContainer: View = view.findViewById(R.id.variant_container) // for fan qty only
        val btnFullSpec: MaterialButton = view.findViewById(R.id.btn_full_spec)
        val btnAdd: MaterialButton = view.findViewById(R.id.btn_add_to_build)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_component_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val data = item.data
        val ctx = holder.itemView.context

        // Clear previous specs
        holder.specsContainer.removeAllViews()

        // Reset both variant areas
        holder.variantDropdownLayout.visibility = View.GONE
        holder.variantDropdown.text = null
        holder.variantDropdown.onItemClickListener = null
        (holder.variantContainer as ViewGroup).removeAllViews()
        holder.variantContainer.visibility = View.GONE
        
        if (isReadOnly) {
            holder.btnAdd.visibility = View.GONE
        } else {
            holder.btnAdd.visibility = View.VISIBLE
        }

        // Set category thumbnail
        holder.thumb.imageTintList = null // Remove tint so images show in color
        val thumbRes = when (data) {
            is CpuEntity -> R.drawable.thumb_cpu
            is MotherboardWithSynergy -> R.drawable.thumb_motherboard
            is RamEntity -> R.drawable.thumb_ram
            is GpuEntity -> R.drawable.thumb_gpu
            is StorageEntity -> R.drawable.thumb_storage
            is CoolerEntity -> R.drawable.thumb_cooler
            is CaseCompatibility -> R.drawable.thumb_case
            is PsuEntity -> R.drawable.thumb_psu
            is FanEntity -> R.drawable.thumb_cooler // reuse cooler image for fans
            else -> 0
        }
        if (thumbRes != 0) {
            holder.thumb.setImageResource(thumbRes)
        }

        var selectedVariant: String? = null
        var addEnabled = true

        when (data) {
            is CpuEntity -> {
                holder.name.text = data.name
                holder.brand.text = data.brand
                holder.price.text = pesoFmt.format(data.price)
                addSpecs(holder, listOf(
                    "Cores/Threads" to "${data.cores}C/${data.threads}T",
                    "Clock" to "${data.baseClockGhz}–${data.boostClockGhz} GHz",
                    "Socket" to data.socket.name,
                    "TDP" to "${data.tdp}W"
                ))
                val hasGamingIgpu = data.integratedGraphics != null &&
                    data.name.endsWith("G") &&
                    data.integratedGraphics.contains("Radeon", ignoreCase = true)
                if (hasGamingIgpu) {
                    showWarning(holder, "✦ Has ${data.integratedGraphics} iGPU — can game without a discrete GPU!",
                        ContextCompat.getColor(ctx, R.color.synergy_good))
                } else {
                    holder.warning.visibility = View.GONE
                }
            }
            is MotherboardWithSynergy -> {
                holder.name.text = data.motherboard.name
                holder.brand.text = data.motherboard.brand
                holder.price.text = pesoFmt.format(data.motherboard.price)
                addSpecs(holder, listOf(
                    "Chipset" to data.motherboard.chipset.name,
                    "Form Factor" to data.motherboard.formFactor.name,
                    "RAM" to "${data.motherboard.ramGeneration.name} (${data.motherboard.ramSlots} slots)",
                    "VRM" to "${data.motherboard.vrmTier.name} — ${data.synergy.label}"
                ))
                val synergyColor = when (data.synergy) {
                    VrmSynergy.EXCELLENT -> ContextCompat.getColor(ctx, R.color.synergy_excellent)
                    VrmSynergy.GOOD -> ContextCompat.getColor(ctx, R.color.synergy_good)
                    VrmSynergy.ADEQUATE -> ContextCompat.getColor(ctx, R.color.synergy_adequate)
                    VrmSynergy.NOT_ADVISED -> ContextCompat.getColor(ctx, R.color.synergy_not_advised)
                }
                if (data.warning != null) {
                    showWarning(holder, data.warning, synergyColor)
                } else {
                    holder.warning.visibility = View.GONE
                }
            }
            is RamEntity -> {
                holder.name.text = data.name
                holder.brand.text = data.brand
                holder.price.text = pesoFmt.format(data.price)
                addSpecs(holder, listOf(
                    "Capacity" to "${data.capacityGb}GB (${data.modules}x${data.capacityGb / data.modules}GB)",
                    "Speed" to "${data.speedMhz} MHz ${data.latency}",
                    "Type" to data.generation.name
                ))
                holder.warning.visibility = View.GONE
                addEnabled = false
                addVariantDropdown(holder, "Color", data.colorVariants) { variant ->
                    selectedVariant = variant
                    holder.btnAdd.isEnabled = true
                }
            }
            is GpuEntity -> {
                holder.name.text = data.name
                holder.brand.text = data.brand
                holder.price.text = pesoFmt.format(data.price)
                addSpecs(holder, listOf(
                    "VRAM" to "${data.vramGb}GB",
                    "Length" to "${data.lengthMm}mm",
                    "TDP" to "${data.tdp}W",
                    "Rec. PSU" to "${data.recommendedPsuWatts}W"
                ))
                holder.warning.visibility = View.GONE
            }
            is StorageEntity -> {
                holder.name.text = data.name
                holder.brand.text = data.brand
                holder.price.text = pesoFmt.format(data.price)
                addSpecs(holder, listOf(
                    "Type" to data.type.name.replace("_", " "),
                    "Read/Write" to "${data.readSpeedMbps}/${data.writeSpeedMbps} MB/s",
                    "Form" to data.formFactor
                ))
                holder.warning.visibility = View.GONE
                addEnabled = false
                addVariantDropdown(holder, "Capacity", data.capacityVariants) { variant ->
                    selectedVariant = variant
                    holder.btnAdd.isEnabled = true
                }
            }
            is CoolerEntity -> {
                holder.name.text = data.name
                holder.brand.text = data.brand
                holder.price.text = pesoFmt.format(data.price)
                val sizeLabel = if (data.type == CoolerType.AIR) "Height" to "${data.heightMm}mm"
                else "Radiator" to "${data.radiatorSizeMm}mm"
                addSpecs(holder, listOf(
                    "Type" to data.type.name,
                    sizeLabel,
                    "Fan" to "${data.fanSizeMm}mm",
                    "TDP Rating" to "${data.tdpRating}W"
                ))
                holder.warning.visibility = View.GONE
            }
            is CaseCompatibility -> {
                holder.name.text = data.case.name
                holder.brand.text = data.case.brand
                holder.price.text = pesoFmt.format(data.case.price)
                addSpecs(holder, listOf(
                    "GPU Clearance" to "${data.case.maxGpuLengthMm}mm",
                    "Cooler Clear." to "${data.case.maxCpuCoolerHeightMm}mm",
                    "Fan Slots" to "${data.case.totalFanSlots} (${data.case.includedFans} included)"
                ))
                if (!data.isFullyCompatible) {
                    val warning = buildString {
                        if (!data.formFactorFits) append("⚠ Motherboard form factor not supported. ")
                        if (!data.gpuFits) append("⚠ GPU too long. ")
                        if (!data.coolerFits) append("⚠ Cooler won't fit. ")
                    }
                    showWarning(holder, warning, ContextCompat.getColor(ctx, R.color.synergy_not_advised))
                } else {
                    holder.warning.visibility = View.GONE
                }
                addEnabled = data.isFullyCompatible
                val initEnabled = addEnabled
                addEnabled = false // Need variant first
                addVariantDropdown(holder, "Color", data.case.colorVariants) { variant ->
                    selectedVariant = variant
                    holder.btnAdd.isEnabled = initEnabled
                }
            }
            is PsuEntity -> {
                holder.name.text = data.name
                holder.brand.text = data.brand
                holder.price.text = pesoFmt.format(data.price)
                val recommended = item.recommendedPsuWatts
                addSpecs(holder, listOf(
                    "Wattage" to "${data.wattage}W",
                    "Efficiency" to data.efficiency,
                    "Modularity" to data.modularity,
                    "Recommended" to "${recommended}W"
                ))
                val isUnderpowered = data.wattage < recommended
                if (isUnderpowered) {
                    showWarning(holder,
                        "⚠ Below recommended ${recommended}W! Your build needs at least ${recommended}W " +
                        "based on GPU manufacturer specs. This PSU is ${recommended - data.wattage}W short and " +
                        "may cause crashes, shutdowns, or instability under heavy load.",
                        ContextCompat.getColor(ctx, R.color.synergy_not_advised))
                } else {
                    holder.warning.visibility = View.GONE
                }
            }
            is FanEntity -> {
                holder.name.text = data.name
                holder.brand.text = data.brand
                holder.price.text = pesoFmt.format(data.price)
                addSpecs(holder, listOf(
                    "Size" to "${data.sizeMm}mm",
                    "Qty" to "${data.quantity}x per pack",
                    "Airflow" to "${data.airflowCfm} CFM",
                    "RGB" to (data.rgbType ?: "None")
                ))
                holder.warning.visibility = View.GONE
                val maxPacks = (item.availableFanSlots / data.quantity).coerceAtLeast(1)
                addFanQuantityPicker(holder, data, maxPacks) { packs ->
                    selectedVariant = packs.toString()
                }
            }
        }

        holder.btnAdd.isEnabled = addEnabled
        holder.btnFullSpec.setOnClickListener { onFullSpec(data) }
        holder.btnAdd.setOnClickListener {
            when (data) {
                is FanEntity -> {
                    val packs = (selectedVariant?.toIntOrNull() ?: 1)
                    onAddClick(data, packs.toString())
                }
                else -> onAddClick(data, selectedVariant)
            }
        }
    }

    private fun addSpecs(holder: ViewHolder, specs: List<Pair<String, String>>) {
        val ctx = holder.itemView.context
        specs.forEach { (label, value) ->
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            val labelTv = TextView(ctx).apply {
                text = "$label: "
                setTextColor(ContextCompat.getColor(ctx, R.color.rig_gray))
                textSize = 10f
            }
            val valueTv = TextView(ctx).apply {
                text = value
                setTextColor(ContextCompat.getColor(ctx, R.color.rig_white))
                textSize = 10f
                paint.isFakeBoldText = true
            }
            row.addView(labelTv)
            row.addView(valueTv)
            holder.specsContainer.addView(row)
        }
    }

    private fun showWarning(holder: ViewHolder, text: String, color: Int) {
        holder.warning.visibility = View.VISIBLE
        holder.warning.text = text
        holder.warning.setTextColor(color)
    }

    private fun addVariantDropdown(
        holder: ViewHolder,
        label: String,
        options: List<String>,
        onSelect: (String) -> Unit
    ) {
        val ctx = holder.itemView.context
        holder.variantDropdownLayout.visibility = View.VISIBLE
        holder.variantDropdownLayout.hint = label
        holder.variantDropdown.text = null
        holder.variantDropdown.setAdapter(ArrayAdapter(ctx, R.layout.item_dropdown, options))
        holder.variantDropdown.setOnItemClickListener { _, _, position, _ ->
            onSelect(options[position])
        }
    }

    private fun addFanQuantityPicker(
        holder: ViewHolder,
        fan: FanEntity,
        maxPacks: Int,
        onPacksChanged: (Int) -> Unit
    ) {
        val ctx = holder.itemView.context
        val variantGroup = holder.variantContainer as ViewGroup
        variantGroup.removeAllViews()
        variantGroup.visibility = View.VISIBLE
        var packs = 1

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val packsLabel = TextView(ctx).apply {
            text = "Piece/s: "
            setTextColor(ContextCompat.getColor(ctx, R.color.rig_gray))
            textSize = 12f
        }

        val btnMinus = TextView(ctx).apply {
            text = "−"
            setTextColor(ContextCompat.getColor(ctx, R.color.rig_white))
            textSize = 18f
            gravity = android.view.Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(28.dp(ctx), 28.dp(ctx))
            background = ContextCompat.getDrawable(ctx, R.drawable.circle_bg)
            (background as? android.graphics.drawable.GradientDrawable)?.setColor(
                ContextCompat.getColor(ctx, R.color.rig_surface_variant)
            )
        }

        val packsText = TextView(ctx).apply {
            text = "$packs"
            setTextColor(ContextCompat.getColor(ctx, R.color.rig_white))
            textSize = 16f
            paint.isFakeBoldText = true
            setPadding(8.dp(ctx), 0, 8.dp(ctx), 0)
        }

        val btnPlus = TextView(ctx).apply {
            text = "+"
            setTextColor(ContextCompat.getColor(ctx, R.color.rig_white))
            textSize = 18f
            gravity = android.view.Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(28.dp(ctx), 28.dp(ctx))
            background = ContextCompat.getDrawable(ctx, R.drawable.circle_bg)
            (background as? android.graphics.drawable.GradientDrawable)?.setColor(
                ContextCompat.getColor(ctx, R.color.rig_surface_variant)
            )
        }

        val totalFans = packs * fan.quantity
        val fansCount = TextView(ctx).apply {
            text = "($totalFans piece/s)"
            setTextColor(ContextCompat.getColor(ctx, R.color.rig_gray))
            textSize = 10f
            setPadding(4.dp(ctx), 0, 0, 0)
        }

        btnMinus.setOnClickListener {
            if (packs > 1) {
                packs--
                packsText.text = "$packs"
                fansCount.text = "(${packs * fan.quantity} piece/s)"
                onPacksChanged(packs)
            }
        }
        btnPlus.setOnClickListener {
            if (packs < maxPacks) {
                packs++
                packsText.text = "$packs"
                fansCount.text = "(${packs * fan.quantity} piece/s)"
                onPacksChanged(packs)
            }
        }

        row.addView(packsLabel)
        row.addView(btnMinus)
        row.addView(packsText)
        row.addView(btnPlus)
        row.addView(fansCount)
        variantGroup.addView(row)

        onPacksChanged(packs) // initial
    }

    private fun Int.dp(ctx: android.content.Context): Int =
        (this * ctx.resources.displayMetrics.density).toInt()

    class DiffCallback : DiffUtil.ItemCallback<ComponentItem>() {
        override fun areItemsTheSame(a: ComponentItem, b: ComponentItem) = a.id == b.id
        override fun areContentsTheSame(a: ComponentItem, b: ComponentItem) = a == b
    }
}
