package com.rigbuilder.app.ui.fragments

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.rigbuilder.app.R
import com.rigbuilder.app.RigBuilderApp
import com.rigbuilder.app.databinding.FragmentPerformanceBinding
import com.rigbuilder.app.data.repository.BottleneckResult
import com.rigbuilder.app.ui.adapters.GameAdapter
import com.rigbuilder.app.viewmodel.BuildViewModel
import com.rigbuilder.app.viewmodel.PerformanceViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PerformanceFragment : Fragment() {

    private var _binding: FragmentPerformanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var performanceViewModel: PerformanceViewModel
    private lateinit var buildViewModel: BuildViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as RigBuilderApp
        buildViewModel = ViewModelProvider(
            requireActivity(),
            BuildViewModel.Factory(app.repository)
        )[BuildViewModel::class.java]

        performanceViewModel = ViewModelProvider(
            this,
            PerformanceViewModel.Factory(app.repository, buildViewModel)
        )[PerformanceViewModel::class.java]

        setupToolbar()
        setupRecyclerView()
        observeData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private lateinit var performanceAdapter: PerformanceAdapter

    private fun setupRecyclerView() {
        performanceAdapter = PerformanceAdapter()
        binding.performanceRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.performanceRecycler.adapter = performanceAdapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                performanceViewModel.games,
                performanceViewModel.bottleneck
            ) { games, bottleneck ->
                Pair(games, bottleneck)
            }.collectLatest { (games, bottleneck) ->
                val items = mutableListOf<PerformanceItem>()

                // Bottleneck card
                items.add(PerformanceItem.BottleneckCard(bottleneck))

                // Section header
                items.add(PerformanceItem.Header("Game Compatibility"))

                // Game cards
                games.forEach { game ->
                    val perf = performanceViewModel.evaluateGame(game)
                    items.add(PerformanceItem.GameCard(perf))
                }

                performanceAdapter.submitList(items)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Multi-type RecyclerView adapter ──────────────────────────

    sealed class PerformanceItem {
        data class BottleneckCard(val bottleneck: BottleneckResult?) : PerformanceItem()
        data class Header(val title: String) : PerformanceItem()
        data class GameCard(val perf: com.rigbuilder.app.viewmodel.GamePerformance) : PerformanceItem()
    }

    inner class PerformanceAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items: List<PerformanceItem> = emptyList()

        fun submitList(newItems: List<PerformanceItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int) = when (items[position]) {
            is PerformanceItem.BottleneckCard -> 0
            is PerformanceItem.Header -> 1
            is PerformanceItem.GameCard -> 2
        }

        override fun getItemCount() = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                0 -> BottleneckViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bottleneck, parent, false))
                1 -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false))
                2 -> GameViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_game_card, parent, false))
                else -> throw IllegalArgumentException()
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is PerformanceItem.BottleneckCard -> bindBottleneck(holder as BottleneckViewHolder, item.bottleneck)
                is PerformanceItem.Header -> bindHeader(holder as HeaderViewHolder, item.title)
                is PerformanceItem.GameCard -> bindGame(holder as GameViewHolder, item.perf)
            }
        }
    }

    class BottleneckViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun bindBottleneck(holder: BottleneckViewHolder, bottleneck: BottleneckResult?) {
        val view = holder.itemView
        val ctx = view.context
        val percentTv = view.findViewById<TextView>(R.id.synergy_percentage)
        val labelTv = view.findViewById<TextView>(R.id.bottleneck_label)
        val progress = view.findViewById<LinearProgressIndicator>(R.id.synergy_progress)
        val adviceCard = view.findViewById<MaterialCardView>(R.id.advice_card)
        val adviceText = view.findViewById<TextView>(R.id.advice_text)
        val emptyText = view.findViewById<TextView>(R.id.empty_synergy_text)
        val circleBg = view.findViewById<View>(R.id.synergy_circle_bg)

        if (bottleneck != null) {
            val synergyColor = when {
                bottleneck.synergyPercentage >= 80 -> ContextCompat.getColor(ctx, R.color.synergy_excellent)
                bottleneck.synergyPercentage >= 60 -> ContextCompat.getColor(ctx, R.color.synergy_good)
                bottleneck.synergyPercentage >= 40 -> ContextCompat.getColor(ctx, R.color.synergy_adequate)
                else -> ContextCompat.getColor(ctx, R.color.synergy_not_advised)
            }

            percentTv.text = "${bottleneck.synergyPercentage}%"
            percentTv.setTextColor(synergyColor)
            percentTv.visibility = View.VISIBLE
            labelTv.text = bottleneck.bottleneck
            labelTv.visibility = View.VISIBLE

            // Circle background tint
            val bg = circleBg.background
            if (bg is GradientDrawable) {
                bg.setColor(synergyColor and 0x4DFFFFFF.toInt()) // 30% alpha
            }

            progress.visibility = View.VISIBLE
            progress.setProgress(bottleneck.synergyPercentage, true)
            progress.setIndicatorColor(synergyColor)

            adviceCard.visibility = View.VISIBLE
            adviceText.text = bottleneck.advice

            emptyText.visibility = View.GONE
        } else {
            percentTv.visibility = View.GONE
            labelTv.visibility = View.GONE
            progress.visibility = View.GONE
            adviceCard.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        }
    }

    private fun bindHeader(holder: HeaderViewHolder, title: String) {
        val tv = holder.itemView.findViewById<TextView>(android.R.id.text1)
        tv.text = title
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.rig_white))
        tv.textSize = 20f
        tv.paint.isFakeBoldText = true
        tv.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rig_black))
        tv.setPadding(0, (8 * resources.displayMetrics.density).toInt(), 0, 0)
    }

    private fun bindGame(holder: GameViewHolder, perf: com.rigbuilder.app.viewmodel.GamePerformance) {
        val view = holder.itemView
        val ctx = view.context

        view.findViewById<TextView>(R.id.game_name).text = perf.game.name

        // Load game cover by game name (no DB dependency — works even if imageUrl is empty)
        val coverMap = mapOf(
            "Apex Legends"         to R.drawable.game_apex,
            "Arc Raiders"          to R.drawable.game_arc_raiders,
            "Black Myth: Wukong"   to R.drawable.game_black_myth,
            "Counter-Strike 2"     to R.drawable.game_cs2,
            "Cyberpunk 2077"       to R.drawable.game_cyberpunk,
            "Elden Ring"           to R.drawable.game_elden_ring,
            "Fortnite"             to R.drawable.game_fortnite,
            "GTA V"                to R.drawable.game_gtav,
            "GTA VI"               to R.drawable.game_gtavi,
            "Hogwarts Legacy"      to R.drawable.game_hogwarts,
            "League of Legends"    to R.drawable.game_lol,
            "Red Dead Redemption 2" to R.drawable.game_rdr2,
            "Valorant"             to R.drawable.game_valorant
        )
        val coverView = view.findViewById<ImageView>(R.id.game_cover)
        val coverRes = coverMap[perf.game.name]
        if (coverRes != null) {
            coverView.setImageResource(coverRes)
        } else {
            coverView.setImageDrawable(null)
        }

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

        val statusIcon = view.findViewById<ImageView>(R.id.status_icon)
        statusIcon.setImageResource(statusIconRes)
        statusIcon.imageTintList = ColorStateList.valueOf(statusColor)

        val statusLabel = view.findViewById<TextView>(R.id.status_label)
        statusLabel.text = perf.statusLabel
        statusLabel.setTextColor(statusColor)

        // Set bars
        setBarWidth(view.findViewById(R.id.cpu_bar), perf.cpuMeetsMin, perf.cpuMeetsRec, ctx)
        setBarWidth(view.findViewById(R.id.gpu_bar), perf.gpuMeetsMin, perf.gpuMeetsRec, ctx)
        setBarWidth(view.findViewById(R.id.ram_bar), perf.ramMeetsMin, perf.ramMeetsRec, ctx)
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
}
