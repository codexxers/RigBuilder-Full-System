# RigBuilder — Smart PC Configurator

An Android application that serves as a comprehensive PC building companion. It features a **Home Hub** with a **Featured Pre-Built PC carousel**, a global **Parts List catalog**, and a **Smart PC Configurator**. 

The Configurator guides users through building a custom PC step-by-step, enforcing hardware compatibility between components, calculating CPU–GPU synergy, and evaluating ultimate game performance.

---

## Version History

### v3.0 — Home Screen Redesign & Navigation
> *Transitioned from a single-screen config app to a full multi-section hub.*

- **Global Drawer Navigation** — Wrapped the app in a `DrawerLayout` with a slide-out hamburger menu accessing all sections.
- **Home Hub Screen** — New landing page with a "Featured Builds" auto-scrolling `ViewPager2` carousel (swipes infinitely) and big navigation buttons.
- **Pre-Built PCs Section** — Browse a list of pre-configured PCs with a detail screen showing tier badges, specs, and specific styling.
- **Parts List Viewer** — Repurposed the catalog into a global read-only library (hides "Add to Build" functionality when browsing outside of the configurator).
- **Navigation Graph Update** — Expanded from linear (Build→Catalog→Performance) to a hub-and-spoke model starting at `HomeFragment`.

---

### v1.0 — Initial Build (Jetpack Compose)
> *The foundation of the app, built with Jetpack Compose.*

- **Smart PC Configurator** — Step-by-step guided build flow: CPU → Motherboard → RAM → GPU → Storage → Cooler → Case → PSU → Case Fans
- **Room Database** — Seeded from JSON assets with 10 component categories (CPU, Motherboard, RAM, GPU, Storage, Cooler, Case, PSU, Fan, Games)
- **Compatibility Engine** — Automatic filtering based on socket, chipset, RAM type, form factor, GPU length, cooler clearance, and power requirements
- **VRM Synergy Ratings** — Motherboard cards display Excellent / Good / Adequate / Not Advised synergy labels based on CPU–motherboard VRM pairing
- **Variant Selection** — RAM color variants, Storage capacity variants, Case color variants via dropdowns
- **Fan Quantity Picker** — +/− stepper for fan packs with max count based on available fan slots
- **Full Spec Bottom Sheet** — Detailed spec view for every component with categorized sections (General, Performance, Compatibility, Pricing)
- **Filter & Sort Panel** — Brand checkboxes, price range slider (₱0–₱200,000), ascending/descending sort
- **Search** — Per-category text search filtering
- **Performance Screen ("Can I Run It?")** — CPU ↔ GPU synergy percentage circle, bottleneck detection with advice, game compatibility cards with CPU/GPU/RAM requirement bars
- **GPU Optional Step** — CPUs with integrated gaming GPUs (G-suffix) make the GPU step optional
- **PSU Warnings** — Underpowered PSU warnings when wattage < recommended
- **Case Compatibility Warnings** — Form factor, GPU length, and cooler clearance warnings
- **Splash & Error Screens** — Loading spinner with retry on database seed failure
- **Material3 Dark Theme** — Full dark theme with red accent (#880808)

---

### v1.1 — Database Expansion & Stability
> *Expanded the component database significantly and performed a stability check.*

- **GPU Database Expansion** — Added a wider range of graphics cards with proper specs, pricing, and gaming tiers
- **RAM Database Expansion** — More RAM options with color variants, speeds, and capacities
- **SSD/Storage Expansion** — Additional NVMe and SATA storage options with capacity variants
- **Stability Audit** — System-wide check to resolve potential issues from the database expansions
- **Data Integrity Fixes** — Ensured all entries have correct specs, pricing, and compatibility flags

---

### v2.0 — Views Conversion (Compose → Views Activity)
> *Complete UI rewrite from Jetpack Compose to Android Views while preserving all functionality.*

#### Architecture Changes
- **Removed** all Jetpack Compose dependencies (BOM, compose-ui, material3, navigation-compose, coil-compose)
- **Added** Views dependencies (Material Components, Navigation Fragment, RecyclerView, ViewBinding, AppCompat, ConstraintLayout, Fragment KTX)
- **Changed** `ComponentActivity` → `AppCompatActivity` with ViewBinding
- **Changed** Compose Navigation → Android Navigation Component with Fragment destinations

#### New XML Layouts (14 files)
| Layout | Purpose |
|--------|---------|
| `activity_main.xml` | NavHostFragment + splash/error overlays |
| `layout_splash.xml` | Loading spinner + title |
| `layout_error.xml` | Error message + retry |
| `fragment_build.xml` | Build stepper RecyclerView + bottom bar |
| `item_build_step.xml` | Step card (indicator + name + price) |
| `item_build_header.xml` | Gradient header card |
| `item_build_total.xml` | Running total card |
| `fragment_catalog.xml` | Search + filter + component list |
| `item_component_card.xml` | Component card (image + specs + buttons) |
| `layout_filter_panel.xml` | Sort + brands + price range |
| `fragment_performance.xml` | Synergy + game compatibility list |
| `item_bottleneck.xml` | Synergy circle + progress + advice |
| `item_game_card.xml` | Game card with requirement bars |
| `bottom_sheet_full_spec.xml` | Full spec bottom sheet |

#### New Kotlin UI Files
| File | Replaces |
|------|----------|
| `BuildFragment.kt` | `BuildScreen.kt` composable |
| `CatalogFragment.kt` | `CatalogScreen.kt` composable |
| `PerformanceFragment.kt` | `PerformanceScreen.kt` composable |
| `FullSpecBottomSheet.kt` | Compose `ModalBottomSheet` |
| `BuildStepAdapter.kt` | Compose `LazyColumn` items |
| `ComponentAdapter.kt` | Compose grid items |
| `GameAdapter.kt` | Compose game cards |

#### New Resource Files
- `nav_graph.xml` — 3 destinations: Build → Catalog → Performance
- `circle_bg.xml`, `gradient_red_horizontal.xml`, `rounded_bg_surface_variant.xml`, `rounded_bar_fill.xml`
- `menu_build.xml` (reset), `menu_catalog.xml` (filter toggle)
- Full Material3 dark theme in `themes.xml`, all colors ported to `colors.xml`, spacing in `dimens.xml`

#### Deleted (Compose-only files)
- `BuildScreen.kt`, `CatalogScreen.kt`, `PerformanceScreen.kt`
- `AppNavigation.kt`, `Theme.kt`, `Type.kt`

#### Untouched (Data Layer & Business Logic)
- `RigBuilderApp.kt`, `BuildViewModel.kt`, `PerformanceViewModel.kt`, `ComponentRepository.kt`
- All 10 Room entities, all 10 DAOs, `AppDatabase`, `DatabaseSeeder`, `Converters`
- All JSON seed files, `Enums.kt`, `AndroidManifest.xml`

---

### v2.1 — Critical Bug Fixes
> *Resolved crashes and missing UI elements after the Views conversion.*

#### RAM Selection Crash Fix
- **Root Cause**: `ClassCastException: ViewGroup.LayoutParams cannot be cast to LinearLayout.LayoutParams` in `TextInputLayout.updateInputLayoutMargins()`
- **Fix**: Changed `MaterialAutoCompleteTextView` child LayoutParams from `ViewGroup.LayoutParams` to `LinearLayout.LayoutParams` — `TextInputLayout` extends `LinearLayout` and internally casts child params
- **Files Fixed**: `ComponentAdapter.kt`, `FullSpecBottomSheet.kt`

#### Missing "RigBuilder" Header
- **Root Cause**: `BuildFragment` was not including the header item in its adapter
- **Fix**: Rewrote `BuildFragment` to use a multi-type `BuildListAdapter` that correctly includes the header

---

### v2.2 — UI Polish & Part Images
> *Visual improvements to search bar, dropdowns, thumbnails, and layout.*

#### Search Bar Overlap Fix
- Set `app:hintEnabled="false"` on the search `TextInputLayout` to prevent the floating label from overlapping the toolbar
- Increased top margin from `8dp` to `12dp`

#### Dropdown Improvements
- **Pure Black Background**: Changed dropdown popup to `@color/rig_black` with white text
- **Custom Dropdown Layout**: Created `item_dropdown.xml` replacing `android.R.layout.simple_dropdown_item_1line`
- **XML-Defined Dropdown**: Moved variant dropdown from programmatic creation to an XML-defined `TextInputLayout` + `AutoCompleteTextView` with `ExposedDropdownMenu` style — fixes inconsistent sizing during RecyclerView scroll
- **Proper LayoutParams**: TextInputLayout uses `FrameLayout.LayoutParams` matching the parent container

#### Part Thumbnail Images
- Added 8 category thumbnail images to `res/drawable-nodpi/`:

| Image | Component |
|-------|-----------|
| `thumb_cpu.png` | AMD Ryzen 5 7600 |
| `thumb_motherboard.png` | MSI PRO A620M-E |
| `thumb_ram.png` | Lexar Ares RGB 16GB |
| `thumb_gpu.png` | ASRock RX 7600 Challenger |
| `thumb_storage.png` | Lexar MP44L 500GB |
| `thumb_cooler.png` | DeepCool AK620 |
| `thumb_case.png` | NZXT H510 Flow |
| `thumb_psu.png` | Corsair RM750e |

- **Component Cards**: Replaced placeholder gallery icon with category thumbnails in `item_component_card.xml` + `ComponentAdapter.kt`
- **Build Steps**: When a part is selected, the step indicator circle is replaced with the category's thumbnail image (`item_build_step.xml` + `BuildStepAdapter.kt`)

#### Layout Cropping Fixes
- **Build Screen**: Added `paddingBottom="160dp"` to RecyclerView so content doesn't hide behind the overlay bottom bar
- **Performance Screen**: Changed RecyclerView from `match_parent` height to `0dp` with `layout_weight="1"` + `paddingBottom="32dp"`

#### Fan Quantity Picker Improvements
- Replaced oversized `ImageButton` icons (`btn_minus`/`btn_plus`) with compact 28dp circular `TextView` buttons showing simple **−** and **+** characters
- Changed label from **"Packs:"** to **"Piece/s:"**
- Changed count text from **"(X fans)"** to **"(X piece/s)"**

---

### v2.3 — Header Redesign & Game Covers
> *Stylish app header and game cover thumbnails on the Can I Run It page.*

#### Header Redesign
- **Gradient background** — dark-to-deep-red gradient (`header_gradient.xml`) with a red glow at the bottom edge
- **App logo** — `ic_launcher` icon displayed at 34dp on the left
- **Styled title** — Bold condensed "RigBuilder" + red "PC BUILD CONFIGURATOR" subtitle in all-caps with wide letter spacing
- **Red accent line** — Fading horizontal red line at the bottom of the header (`header_accent_line.xml`)
- **Fixed overlap bug** — Removed old programmatic title `TextView` injection from `BuildFragment.setupToolbar()` that was causing double/ghost text

#### Game Cover Thumbnails
- Added 13 game cover images to `res/drawable-nodpi/` (named `game_*.jpg/.png`)
- `item_game_card.xml` — replaced compass icon placeholder with a proper 72×72dp `ImageView` (`game_cover`)
- `PerformanceFragment.bindGame()` — loads the correct cover for each game by name using a hardcoded drawable map (no DB dependency)

| Game | File |
|------|------|
| Apex Legends | `game_apex.jpg` |
| Arc Raiders | `game_arc_raiders.jpg` |
| Black Myth: Wukong | `game_black_myth.jpg` |
| Counter-Strike 2 | `game_cs2.jpg` |
| Cyberpunk 2077 | `game_cyberpunk.jpg` |
| Elden Ring | `game_elden_ring.jpg` |
| Fortnite | `game_fortnite.jpg` |
| GTA V | `game_gtav.jpg` |
| GTA VI | `game_gtavi.jpg` |
| Hogwarts Legacy | `game_hogwarts.jpg` |
| League of Legends | `game_lol.jpg` |
| Red Dead Redemption 2 | `game_rdr2.png` |
| Valorant | `game_valorant.png` |

- **Database version** bumped from 8 → 9 to force a clean re-seed on install
- `games.json` — `imageUrl` fields populated for all 13 games

---

## Project Structure

```
RigBuilder/
├── app/src/main/
│   ├── assets/                  # JSON seed files for all components & games
│   ├── java/com/rigbuilder/app/
│   │   ├── MainActivity.kt
│   │   ├── RigBuilderApp.kt     # Application class + DB seeding
│   │   ├── data/
│   │   │   ├── entities/        # 10 Room entities
│   │   │   ├── dao/             # 10 DAOs
│   │   │   ├── AppDatabase.kt
│   │   │   ├── DatabaseSeeder.kt
│   │   │   └── Converters.kt
│   │   ├── model/
│   │   │   └── Enums.kt         # SocketType, Chipset, ComponentCategory, etc.
│   │   ├── repository/
│   │   │   └── ComponentRepository.kt
│   │   ├── viewmodel/
│   │   │   ├── BuildViewModel.kt
│   │   │   └── PerformanceViewModel.kt
│   │   └── ui/
│   │       ├── fragments/       # BuildFragment, CatalogFragment, PerformanceFragment, FullSpecBottomSheet
│   │       ├── adapters/        # BuildStepAdapter, ComponentAdapter, GameAdapter
│   │       └── theme/Color.kt
│   └── res/
│       ├── layout/              # 14+ XML layouts
│       ├── drawable/            # Shape drawables + component thumbnails
│       ├── drawable-nodpi/      # Part thumbnail images (8 images)
│       ├── navigation/          # nav_graph.xml
│       ├── menu/                # Toolbar menus
│       └── values/              # colors.xml, themes.xml, dimens.xml
└── Part_Images/                 # Source thumbnail images
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Android Views (XML Layouts, Fragments, RecyclerView, ViewBinding)
- **Navigation**: Android Navigation Component
- **Database**: Room (SQLite) with JSON seed data
- **Architecture**: MVVM (ViewModel + StateFlow + Repository)
- **Styling**: Material Design 3 (Material Components for Android)
- **Min SDK**: 26 (Android 8.0)
