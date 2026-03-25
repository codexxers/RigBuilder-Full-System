package com.rigbuilder.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.rigbuilder.app.R
import com.rigbuilder.app.data.entity.*
import com.rigbuilder.app.data.repository.CaseCompatibility
import com.rigbuilder.app.data.repository.MotherboardWithSynergy
import com.rigbuilder.app.model.*
import com.rigbuilder.app.viewmodel.BuildState
import com.rigbuilder.app.viewmodel.BuildViewModel
import java.text.NumberFormat
import java.util.Locale

class FullSpecBottomSheet : BottomSheetDialogFragment() {

    var onAddCallback: ((String?) -> Unit)? = null

    private var item: Any? = null
    private var category: ComponentCategory? = null
    private var buildViewModel: BuildViewModel? = null
    private var selectedVariant: String? = null
    private val pesoFmt = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    override fun getTheme(): Int = com.google.android.material.R.style.Theme_Material3_DayNight_BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_full_spec, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = item ?: return

        val btnClose = view.findViewById<View>(R.id.btn_close)
        val btnAdd = view.findViewById<MaterialButton>(R.id.btn_sheet_add)
        val imageGallery = view.findViewById<LinearLayout>(R.id.image_gallery)
        val variantContainer = view.findViewById<FrameLayout>(R.id.sheet_variant_container)
        val specsContainer = view.findViewById<LinearLayout>(R.id.specs_sections_container)

        // Close
        btnClose.setOnClickListener { dismiss() }

        // Image gallery (4 placeholders)
        imageGallery.removeAllViews()
        repeat(4) { idx ->
            val frame = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (200 * resources.displayMetrics.density).toInt(),
                    (150 * resources.displayMetrics.density).toInt()
                ).apply { marginEnd = (12 * resources.displayMetrics.density).toInt() }
                setBackgroundResource(R.drawable.rounded_bg_surface_variant)
            }
            val label = TextView(requireContext()).apply {
                text = "Image ${idx + 1}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.rig_gray))
                textSize = 10f
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply { gravity = android.view.Gravity.CENTER }
            }
            frame.addView(label)
            imageGallery.addView(frame)
        }

        // Variants
        val variants = getVariants(data)
        val hasVariants = variants.isNotEmpty()
        if (hasVariants && !isReadOnly) {
            variantContainer.visibility = View.VISIBLE
            variantContainer.removeAllViews()
            val variantLabel = getVariantLabel(data)
            addDropdown(variantContainer, variantLabel, variants) { v ->
                selectedVariant = v
                btnAdd.isEnabled = true
            }
            btnAdd.isEnabled = false
        } else {
            variantContainer.visibility = View.GONE
            btnAdd.isEnabled = true
        }
        
        if (isReadOnly) {
            btnAdd.visibility = View.GONE
        }

        // Full specs
        specsContainer.removeAllViews()
        val specs = getFullSpecs(data)
        specs.forEach { (section, items) ->
            val sectionTitle = TextView(requireContext()).apply {
                text = section
                setTextColor(ContextCompat.getColor(requireContext(), R.color.rig_red))
                textSize = 14f
                paint.isFakeBoldText = true
                setPadding(dp(16), dp(8), dp(16), dp(8))
            }
            specsContainer.addView(sectionTitle)

            items.forEach { (label, value) ->
                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(dp(16), dp(6), dp(16), dp(6))
                }
                val labelTv = TextView(requireContext()).apply {
                    text = label
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.rig_gray))
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(dp(120), LinearLayout.LayoutParams.WRAP_CONTENT)
                }
                val valueTv = TextView(requireContext()).apply {
                    text = value
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.rig_white))
                    textSize = 14f
                    paint.isFakeBoldText = true
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                row.addView(labelTv)
                row.addView(valueTv)
                specsContainer.addView(row)

                // Divider
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
                    ).apply { marginStart = dp(16); marginEnd = dp(16) }
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rig_surface_variant))
                    alpha = 0.3f
                }
                specsContainer.addView(divider)
            }
        }

        // Add button
        btnAdd.setOnClickListener {
            onAddCallback?.invoke(selectedVariant)
            dismiss()
        }
    }

    private fun addDropdown(container: ViewGroup, label: String, options: List<String>, onSelect: (String) -> Unit) {
        val ctx = requireContext()
        val themedCtx = android.view.ContextThemeWrapper(ctx,
            com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox)

        val layout = TextInputLayout(themedCtx).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.MATCH_PARENT, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT)
            hint = label
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            val r = 8f * ctx.resources.displayMetrics.density
            setBoxCornerRadii(r, r, r, r)
            try { defaultHintTextColor = ContextCompat.getColorStateList(ctx, R.color.rig_gray) } catch (_: Exception) {}
            endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
        }
        val autoComplete = com.google.android.material.textfield.MaterialAutoCompleteTextView(themedCtx).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            setTextColor(ContextCompat.getColor(ctx, R.color.rig_white))
            textSize = 14f
            inputType = 0
            setAdapter(ArrayAdapter(ctx, R.layout.item_dropdown, options))
            setOnItemClickListener { _, _, position, _ -> onSelect(options[position]) }
        }
        layout.addView(autoComplete)
        container.addView(layout)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // ── Spec Extraction (same logic as Compose) ─────────────────

    private fun getVariants(item: Any): List<String> = when (item) {
        is RamEntity -> item.colorVariants
        is StorageEntity -> item.capacityVariants
        is CaseCompatibility -> item.case.colorVariants
        is CaseEntity -> item.colorVariants
        else -> emptyList()
    }

    private fun getVariantLabel(item: Any): String = when (item) {
        is RamEntity -> "Color"
        is StorageEntity -> "Capacity"
        is CaseCompatibility, is CaseEntity -> "Color"
        else -> "Variant"
    }

    private fun getFullSpecs(item: Any): List<Pair<String, List<Pair<String, String>>>> = when (item) {
        is CpuEntity -> listOf(
            "General" to listOf("Brand" to item.brand, "Name" to item.name, "Socket" to item.socket.name, "Architecture" to item.architecture.name),
            "Performance" to listOf("Cores" to "${item.cores}", "Threads" to "${item.threads}", "Base Clock" to "${item.baseClockGhz} GHz", "Boost Clock" to "${item.boostClockGhz} GHz", "TDP" to "${item.tdp}W", "Power Tier" to item.powerTier.name),
            "Compatibility" to listOf("Supported Chipsets" to item.supportedChipsets.joinToString(", ") { it.name }, "Integrated Graphics" to (item.integratedGraphics ?: "None")),
            "Pricing" to listOf("Price" to pesoFmt.format(item.price))
        )
        is MotherboardWithSynergy -> listOf(
            "General" to listOf("Brand" to item.motherboard.brand, "Name" to item.motherboard.name, "Socket" to item.motherboard.socket.name, "Chipset" to item.motherboard.chipset.name, "Form Factor" to item.motherboard.formFactor.name),
            "Memory" to listOf("RAM Generation" to item.motherboard.ramGeneration.name, "RAM Slots" to "${item.motherboard.ramSlots}", "Max RAM Speed" to "${item.motherboard.maxRamSpeedMhz} MHz"),
            "Storage" to listOf("M.2 Slots" to "${item.motherboard.m2Slots}", "SATA Slots" to "${item.motherboard.sataSlots}"),
            "Power" to listOf("VRM Tier" to item.motherboard.vrmTier.name, "VRM Synergy" to item.synergy.label),
            "Pricing" to listOf("Price" to pesoFmt.format(item.motherboard.price))
        )
        is RamEntity -> listOf(
            "General" to listOf("Brand" to item.brand, "Name" to item.name, "Generation" to item.generation.name),
            "Performance" to listOf("Capacity" to "${item.capacityGb}GB", "Modules" to "${item.modules}", "Speed" to "${item.speedMhz} MHz", "Latency" to item.latency),
            "Options" to listOf("Color Variants" to item.colorVariants.joinToString(", ")),
            "Pricing" to listOf("Price" to pesoFmt.format(item.price))
        )
        is GpuEntity -> listOf(
            "General" to listOf("Brand" to item.brand, "Name" to item.name),
            "Performance" to listOf("VRAM" to "${item.vramGb}GB", "TDP" to "${item.tdp}W", "Gaming Tier" to "${item.gamingTier}/10"),
            "Physical" to listOf("Length" to "${item.lengthMm}mm", "Thickness" to "${item.thicknessSlots} slots"),
            "Power" to listOf("Recommended PSU" to "${item.recommendedPsuWatts}W"),
            "Pricing" to listOf("Price" to pesoFmt.format(item.price))
        )
        is StorageEntity -> listOf(
            "General" to listOf("Brand" to item.brand, "Name" to item.name, "Type" to item.type.name.replace("_", " ")),
            "Performance" to listOf("Read" to "${item.readSpeedMbps} MB/s", "Write" to "${item.writeSpeedMbps} MB/s"),
            "Physical" to listOf("Form Factor" to item.formFactor, "Capacity" to "${item.capacityGb}GB"),
            "Options" to listOf("Variants" to item.capacityVariants.joinToString(", ")),
            "Pricing" to listOf("Price" to pesoFmt.format(item.price))
        )
        is CoolerEntity -> listOf(
            "General" to listOf("Brand" to item.brand, "Name" to item.name, "Type" to item.type.name),
            "Specs" to listOf(
                if (item.type == CoolerType.AIR) "Height" to "${item.heightMm}mm" else "Radiator" to "${item.radiatorSizeMm}mm",
                "Fan Size" to "${item.fanSizeMm}mm", "TDP Rating" to "${item.tdpRating}W"
            ),
            "Compatibility" to listOf("Sockets" to item.supportedSockets.joinToString(", ") { it.name }),
            "Pricing" to listOf("Price" to pesoFmt.format(item.price))
        )
        is CaseCompatibility -> listOf(
            "General" to listOf("Brand" to item.case.brand, "Name" to item.case.name),
            "Clearance" to listOf("Max GPU Length" to "${item.case.maxGpuLengthMm}mm", "Max Cooler Height" to "${item.case.maxCpuCoolerHeightMm}mm", "Max PSU Length" to "${item.case.maxPsuLengthMm}mm", "Max Radiator" to "${item.case.maxRadiatorSizeMm}mm"),
            "Fans" to listOf("Total Slots" to "${item.case.totalFanSlots}", "Included" to "${item.case.includedFans}"),
            "Form Factors" to listOf("Supported" to item.case.supportedFormFactors.joinToString(", ") { it.name }),
            "Options" to listOf("Colors" to item.case.colorVariants.joinToString(", ")),
            "Pricing" to listOf("Price" to pesoFmt.format(item.case.price))
        )
        is PsuEntity -> listOf(
            "General" to listOf("Brand" to item.brand, "Name" to item.name),
            "Specs" to listOf("Wattage" to "${item.wattage}W", "Efficiency" to item.efficiency, "Modularity" to item.modularity, "Length" to "${item.lengthMm}mm"),
            "Pricing" to listOf("Price" to pesoFmt.format(item.price))
        )
        is FanEntity -> listOf(
            "General" to listOf("Brand" to item.brand, "Name" to item.name),
            "Specs" to listOf("Size" to "${item.sizeMm}mm", "Quantity" to "${item.quantity} per pack", "Airflow" to "${item.airflowCfm} CFM", "Noise" to "${item.noiseLevelDba} dBA", "RGB" to (item.rgbType ?: "None")),
            "Pricing" to listOf("Price" to pesoFmt.format(item.price))
        )
        else -> emptyList()
    }

    private var isReadOnly: Boolean = false

    companion object {
        fun newInstance(
            item: Any,
            category: ComponentCategory,
            buildViewModel: BuildViewModel
        ): FullSpecBottomSheet {
            return FullSpecBottomSheet().apply {
                this.item = item
                this.category = category
                this.buildViewModel = buildViewModel
                this.isReadOnly = false
            }
        }

        fun newInstanceReadOnly(
            item: Any,
            category: ComponentCategory
        ): FullSpecBottomSheet {
            return FullSpecBottomSheet().apply {
                this.item = item
                this.category = category
                this.isReadOnly = true
            }
        }
    }
}
