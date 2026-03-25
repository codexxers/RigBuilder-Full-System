package com.rigbuilder.app.model

/**
 * Central source of truth for all Pre-Built PC configurations.
 * ALL parts referenced here MUST exist in the JSON seed database.
 * Ordered lowest → highest price. Tiers: Entry → Mid-Range → High-End → Extreme.
 */
object PreBuiltRepository {

    val allPreBuilts: List<PreBuiltData> = listOf(
        // ── Entry Tier ──────────────────────────────────────────────
        PreBuiltData(
            id = "pb_001",
            name = "Starter Build",
            tier = "Entry",
            description = "Your gateway into PC gaming. Handles popular titles like Valorant, CS2, and Fortnite at smooth framerates without breaking the bank. Perfect for first-time builders ready to ditch console gaming.",
            price = 43825,
            cpu = "Intel Core i5-12400F",
            gpu = "Sapphire Pulse AMD Radeon RX 7600 8GB GDDR6",
            ram = "TeamGroup T-Force Vulcan 16GB (1x16GB) DDR4 3200MHz Desktop Memory",
            storage = "Kingston NV2 500GB PCIe Gen4 NVMe M.2 2280 SSD",
            psu = "Cooler Master MWE V2 650 80+ Bronze",
            case_ = "Cooler Master MasterBox Q300L",
            motherboard = "MSI PRO B760-P WIFI DDR4",
            cooler = "Cooler Master Hyper 212",
            fans = "Arctic P12 PWM PST",
            cpuTier = 5, gpuTier = 4, vrmTier = 2, psuTier = 2
        ),
        PreBuiltData(
            id = "pb_002",
            name = "Entry Performer",
            tier = "Entry",
            description = "Step up your game with a proven AMD platform. Crush 1080p gaming in AAA titles like Cyberpunk 2077 and Hogwarts Legacy with room to upgrade later. The sweet spot for gamers who want performance without compromise.",
            price = 47625,
            cpu = "AMD Ryzen 5 5600X",
            gpu = "MSI Radeon RX 7600 MECH 2X 8GB OC GDDR6",
            ram = "G.Skill Ripjaws V 16GB (1x16GB) DDR4 3200MHz Desktop Memory",
            storage = "Lexar NM710 500GB PCIe Gen4 NVMe M.2 2280 SSD",
            psu = "Seasonic Focus GM-550 80+ Gold",
            case_ = "Fractal Design Pop Mini Air",
            motherboard = "MSI MAG B550 TOMAHAWK",
            cooler = "ID-COOLING SE-226-XT",
            fans = "Arctic P12 PWM PST",
            cpuTier = 6, gpuTier = 4, vrmTier = 3, psuTier = 3
        ),

        // ── Mid-Range Tier ──────────────────────────────────────────
        PreBuiltData(
            id = "pb_003",
            name = "Solid Performance Build",
            tier = "Mid-Range",
            description = "Dominate 1080p and push into 1440p territory with RTX ray-tracing and DLSS. Stream, game, and multitask without missing a beat. Built for gamers who refuse to lower their settings.",
            price = 67425,
            cpu = "AMD Ryzen 5 7600X",
            gpu = "MSI GeForce RTX 4060 Ti Ventus 2X Black 8GB OC GDDR6",
            ram = "Kingston Fury Beast 16GB (1x16GB) DDR5 5600MHz Desktop Memory",
            storage = "WD Blue SN5000 1TB PCIe Gen4 NVMe M.2 2280 SSD",
            psu = "Corsair RM750e 80+ Gold",
            case_ = "NZXT H510 Flow",
            motherboard = "Gigabyte B650M AORUS ELITE AX",
            cooler = "DeepCool AK620",
            fans = "Corsair iCUE SP120 RGB ELITE 3-Pack",
            cpuTier = 7, gpuTier = 6, vrmTier = 3, psuTier = 3
        ),
        PreBuiltData(
            id = "pb_004",
            name = "Power House",
            tier = "Mid-Range",
            description = "1440p ultra settings? Done. This rig tears through demanding titles with an RTX 4070 Super and a beastly Ryzen 7. Competitive multiplayer at 144+ FPS or cinematic single-player at max quality \u2014 your call.",
            price = 106830,
            cpu = "AMD Ryzen 7 7700X",
            gpu = "MSI GeForce RTX 4070 Super Ventus 3X 12GB OC GDDR6X",
            ram = "Corsair Vengeance RGB 32GB (2x16GB) DDR5 6000MHz Desktop Memory",
            storage = "Samsung 990 PRO 2TB PCIe Gen4 NVMe M.2 2280 SSD",
            psu = "Seasonic Focus GX-850 80+ Gold",
            case_ = "Corsair 4000D Airflow",
            motherboard = "MSI MAG B650 TOMAHAWK WIFI",
            cooler = "Arctic Liquid Freezer II 240mm AIO",
            fans = "Corsair iCUE SP120 RGB ELITE 3-Pack",
            cpuTier = 8, gpuTier = 8, vrmTier = 4, psuTier = 4
        ),

        // ── High-End Tier ───────────────────────────────────────────
        PreBuiltData(
            id = "pb_005",
            name = "Creator Pro",
            tier = "High-End",
            description = "Where gaming meets content creation. 4K gaming, buttery-smooth video editing, and live streaming all at once. Powered by a Ryzen 9 and RTX 4070 Ti Super \u2014 built for those who play hard and create harder.",
            price = 150725,
            cpu = "AMD Ryzen 9 7900X",
            gpu = "MSI GeForce RTX 4070 Ti Super Ventus 3X 16GB OC GDDR6X",
            ram = "Corsair Vengeance RGB 32GB (2x16GB) DDR5 6000MHz Desktop Memory",
            storage = "WD BLACK SN850X 2TB PCIe Gen4 NVMe M.2 2280 SSD",
            psu = "Corsair RM1000x 80+ Gold",
            case_ = "Lian Li Lancool III",
            motherboard = "ASUS ROG Strix X670E-E Gaming",
            cooler = "Corsair iCUE H150i Elite 360mm AIO",
            fans = "Lian Li UNI FAN SL120 V2 3-Pack",
            cpuTier = 9, gpuTier = 9, vrmTier = 5, psuTier = 4
        ),
        PreBuiltData(
            id = "pb_006",
            name = "Ultimate Rig",
            tier = "High-End",
            description = "No compromises, no excuses. Max out every game at 4K with the RTX 4080 Super while running anything else in the background. This is the rig that makes other setups jealous.",
            price = 179525,
            cpu = "AMD Ryzen 9 7950X",
            gpu = "MSI GeForce RTX 4080 Super Ventus 3X 16GB OC GDDR6X",
            ram = "Corsair Vengeance RGB 64GB (2x32GB) DDR5 6000MHz Desktop Memory",
            storage = "Samsung 990 PRO 2TB PCIe Gen4 NVMe M.2 2280 SSD",
            psu = "EVGA SuperNOVA 1000 G7 80+ Gold",
            case_ = "NZXT H9 Flow",
            motherboard = "ASUS ROG Strix X670E-E Gaming",
            cooler = "NZXT Kraken X63 280mm AIO",
            fans = "Lian Li UNI FAN SL120 V2 3-Pack",
            cpuTier = 10, gpuTier = 10, vrmTier = 5, psuTier = 5
        ),

        // ── Extreme Tier ────────────────────────────────────────────
        PreBuiltData(
            id = "pb_007",
            name = "Dream Machine",
            tier = "Extreme",
            description = "The absolute pinnacle of PC gaming. RTX 4090 power, 96GB of RAM, and 4TB of blazing storage \u2014 nothing on earth runs games better than this. If you want the best that exists, this is it.",
            price = 268080,
            cpu = "AMD Ryzen 9 9950X",
            gpu = "ASUS ROG Strix GeForce RTX 4090 OC 24GB GDDR6X",
            ram = "Corsair Vengeance RGB 96GB (2x48GB) DDR5 6000MHz Desktop Memory",
            storage = "Samsung 990 PRO 4TB PCIe Gen4 NVMe M.2 2280 SSD",
            psu = "EVGA SuperNOVA 1000 G7 80+ Gold",
            case_ = "NZXT H9 Flow",
            motherboard = "ASUS ROG Strix X670E-E Gaming",
            cooler = "Corsair iCUE H150i Elite 360mm AIO",
            fans = "Lian Li UNI FAN SL120 V2 3-Pack",
            cpuTier = 10, gpuTier = 10, vrmTier = 5, psuTier = 5
        )
    )

    fun findById(id: String): PreBuiltData? = allPreBuilts.find { it.id == id }

    /** The featured subset shown on the Home carousel */
    val featuredBuilds: List<PreBuiltData> get() = allPreBuilts
}
