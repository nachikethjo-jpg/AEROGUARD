package com.example.data.model

import com.example.data.local.SavedLocationEntity

data class CityHazardPin(
    val id: Long,
    val title: String,
    val location: String,
    val sourceType: String,
    val severity: String,
    val description: String,
    val timeAgo: String,
    val reporter: String,
    val upvotes: Int,
    val normalizedX: Float,
    val normalizedY: Float,
    val lat: Double,
    val lon: Double
)

data class CityAirProfile(
    val id: String,
    val cityName: String,
    val state: String,
    val zone: String, // North, South, East, West, Central, North-East
    val subText: String,
    val stationName: String,
    val baseAqi: Int,
    val latitude: Double,
    val longitude: Double,
    val landmarkWater: String,
    val majorHighway: String,
    val hazardPins: List<CityHazardPin>
)

object IndiaLocations {

    val allCities: List<CityAirProfile> = listOf(
        // --- Central India (including Durg / Bhilai / Raipur) ---
        CityAirProfile(
            id = "durg_core",
            cityName = "Durg",
            state = "Chhattisgarh",
            zone = "Central",
            subText = "Padmanabhpur & G.E. Road Corridor",
            stationName = "CECB Regional Monitoring Station, Durg",
            baseAqi = 142,
            latitude = 21.1904,
            longitude = 81.2849,
            landmarkWater = "Shivnath River Basin",
            majorHighway = "NH-53 Expressway to Bhilai",
            hazardPins = listOf(
                CityHazardPin(1L, "Open Garbage & Leaf Burning", "Padmanabhpur Bypass Ring Road", "Open Burning", "High", "Dense smoke plume billowing from waste piles near residential colonies.", "35 mins ago", "Aman K. (Local Resident)", 14, 0.32f, 0.68f, 21.1824, 81.2750),
                CityHazardPin(2L, "Industrial Smelter Emission", "G.E. Road near Bhilai Border", "Industrial", "Severe", "Dark particulate exhaust drifting south-west with wind across National Highway.", "1.5 hours ago", "Pooja S.", 28, 0.78f, 0.38f, 21.1985, 81.3020),
                CityHazardPin(3L, "Fly-ash & Construction Dust", "Mohan Nagar Overbridge Junction", "Construction Dust", "Moderate", "Heavy dump trucks carrying uncovered aggregate, kicking up dense dust.", "2.5 hours ago", "Vikram R.", 9, 0.45f, 0.32f, 21.1915, 81.2810),
                CityHazardPin(4L, "Heavy Transit Exhaust & Soot", "Durg Railway Station Bus Stand", "Vehicle Exhaust", "Moderate", "Idling diesel buses and interstate freight creating concentrated black carbon pocket.", "4 hours ago", "Rajesh N.", 7, 0.52f, 0.52f, 21.1920, 81.2840),
                CityHazardPin(5L, "Market Waste Incineration", "Indira Market Outer Perimeter", "Biomass Burning", "High", "Vegetable and cardboard cartons being burnt in alleys after evening market closure.", "5 hours ago", "Neha T.", 11, 0.62f, 0.62f, 21.1890, 81.2800)
            )
        ),
        CityAirProfile(
            id = "bhilai_city",
            cityName = "Bhilai Steel City",
            state = "Chhattisgarh",
            zone = "Central",
            subText = "Civic Centre, Sector 6 & Industrial Estate",
            stationName = "CPCB Industrial Air Station, Bhilai Sector 1",
            baseAqi = 168,
            latitude = 21.1938,
            longitude = 81.3509,
            landmarkWater = "Tandula Canal Network",
            majorHighway = "G.E. Road Corridor to Raipur",
            hazardPins = listOf(
                CityHazardPin(11L, "Blast Furnace Sintering Emission", "Heavy Industrial Area Sector 1", "Industrial", "Severe", "Particulate emissions during furnace shift transition.", "20 mins ago", "Devendra P.", 32, 0.72f, 0.45f, 21.2050, 81.3650),
                CityHazardPin(12L, "Slag Haulage Road Dust", "Nandini Road Heavy Vehicle Route", "Road Dust", "High", "Unpaved road shoulders generating heavy dust plumes.", "1 hour ago", "Kavita M.", 16, 0.40f, 0.65f, 21.1980, 81.3350)
            )
        ),
        CityAirProfile(
            id = "raipur_cap",
            cityName = "Raipur",
            state = "Chhattisgarh",
            zone = "Central",
            subText = "Telibandha, Urla Industrial & Jaistambh",
            stationName = "CECB Regional Lab, Raipur AIIMS Station",
            baseAqi = 155,
            latitude = 21.2514,
            longitude = 81.6296,
            landmarkWater = "Kharun River Basin",
            majorHighway = "NH-30 & Ring Road 1",
            hazardPins = listOf(
                CityHazardPin(21L, "Rolling Mill Chimney Plume", "Urla Industrial Zone Phase 2", "Industrial", "Severe", "Thick black particulate plume blowing towards Ring Road.", "40 mins ago", "Sanjay G.", 22, 0.28f, 0.35f, 21.3120, 81.5950),
                CityHazardPin(22L, "Expressway Transit Congestion", "Telibandha Marine Drive Junction", "Vehicle Exhaust", "Moderate", "Peak evening gridlock emitting elevated nitrogen oxides.", "1 hour ago", "Anita C.", 15, 0.65f, 0.55f, 21.2420, 81.6680)
            )
        ),
        CityAirProfile(
            id = "bilaspur_cg",
            cityName = "Bilaspur",
            state = "Chhattisgarh",
            zone = "Central",
            subText = "Vyapar Vihar & Link Road",
            stationName = "CPCB High Court Road Station, Bilaspur",
            baseAqi = 136,
            latitude = 22.0797,
            longitude = 82.1409,
            landmarkWater = "Arpa River Basin",
            majorHighway = "NH-130 Raipur-Bilaspur Expressway",
            hazardPins = listOf(
                CityHazardPin(31L, "Dry Riverbed Silt Dust", "Arpa Bridge Causeway", "Fugitive Dust", "Moderate", "Dry winds carrying fine silt into downtown commercial core.", "2 hours ago", "Manish B.", 10, 0.50f, 0.50f, 22.0850, 82.1450)
            )
        ),
        CityAirProfile(
            id = "korba_cg",
            cityName = "Korba",
            state = "Chhattisgarh",
            zone = "Central",
            subText = "TP Nagar & Coal Thermal Hub",
            stationName = "CPCB Korba Power Belt Station",
            baseAqi = 185,
            latitude = 22.3595,
            longitude = 82.7501,
            landmarkWater = "Hasdeo River Basin",
            majorHighway = "Korba-Champa Industrial Corridor",
            hazardPins = listOf(
                CityHazardPin(41L, "Thermal Fly-Ash Dykes Dispersion", "NTPC Jamnipali Vicinity", "Fly Ash", "Severe", "Dry fly-ash particles whipped up by thermal draft breezes.", "50 mins ago", "Sunil K.", 29, 0.60f, 0.40f, 22.3750, 82.7250)
            )
        ),
        CityAirProfile(
            id = "bhopal_mp",
            cityName = "Bhopal",
            state = "Madhya Pradesh",
            zone = "Central",
            subText = "TT Nagar, Paryavaran Parisar & MP Nagar",
            stationName = "MPPCB Paryavaran Parisar, Bhopal",
            baseAqi = 124,
            latitude = 23.2599,
            longitude = 77.4126,
            landmarkWater = "Bada Talaab (Upper Lake)",
            majorHighway = "Hoshangabad Road Corridor",
            hazardPins = listOf(
                CityHazardPin(51L, "Municipal Waste Burning", "Bhanpur Dumpsite Perimeter", "Open Burning", "High", "Periodic spontaneous refuse combustion near northern outskirts.", "2 hours ago", "Rohit J.", 19, 0.70f, 0.30f, 23.3100, 77.4350)
            )
        ),
        CityAirProfile(
            id = "indore_mp",
            cityName = "Indore",
            state = "Madhya Pradesh",
            zone = "Central",
            subText = "Vijay Nagar, Chhotigwaltoli & Pithampur",
            stationName = "MPPCB Sanwer Road Station, Indore",
            baseAqi = 138,
            latitude = 22.7196,
            longitude = 75.8577,
            landmarkWater = "Kanh River Basin",
            majorHighway = "AB Road (NH-52) & Bypass",
            hazardPins = listOf(
                CityHazardPin(61L, "BRTS Corridor Congestion Exhaust", "Palasia Square Traffic Hub", "Vehicle Exhaust", "Moderate", "Heavy commuter traffic idling at multi-phase signal.", "1 hour ago", "Kunal V.", 12, 0.55f, 0.48f, 22.7250, 75.8850)
            )
        ),
        CityAirProfile(
            id = "gwalior_mp",
            cityName = "Gwalior",
            state = "Madhya Pradesh",
            zone = "Central",
            subText = "Phoolbagh, Maharaj Bada & Morar",
            stationName = "MPPCB City Centre Station, Gwalior",
            baseAqi = 172,
            latitude = 26.2183,
            longitude = 78.1828,
            landmarkWater = "Swarn Rekha River Canal",
            majorHighway = "Agra-Mumbai NH-44",
            hazardPins = listOf(
                CityHazardPin(71L, "Stone Crusher Dust Dispersion", "Gwalior Bypass Outskirts", "Quarry Dust", "Severe", "Unscreened aggregate crushers emitting dense silica dust.", "3 hours ago", "Deepak S.", 21, 0.35f, 0.70f, 26.1950, 78.1550)
            )
        ),

        // --- Northern India (Delhi NCR, UP, Punjab, Haryana, J&K) ---
        CityAirProfile(
            id = "delhi_anand_vihar",
            cityName = "New Delhi",
            state = "Delhi NCR",
            zone = "North",
            subText = "Anand Vihar, ITO & Connaught Place",
            stationName = "DPCC Anand Vihar Continuous Ambient Station",
            baseAqi = 245,
            latitude = 28.6139,
            longitude = 77.2090,
            landmarkWater = "Yamuna River Corridor",
            majorHighway = "Delhi Ring Road & Delhi-Meerut Expressway",
            hazardPins = listOf(
                CityHazardPin(101L, "Interstate Bus Terminal Diesel Plume", "Anand Vihar ISBT Entry", "Heavy Diesel", "Severe", "Concentrated diesel particulates from incoming interstate state transport fleets.", "15 mins ago", "Naveen K.", 48, 0.82f, 0.42f, 28.6502, 77.3150),
                CityHazardPin(102L, "Waste Landfill Smoldering", "Ghazipur Landfill Border", "Toxic Smoke", "Hazardous", "Deep-seated refuse combustion emitting dense chemical haze across east Delhi.", "45 mins ago", "Harish M.", 64, 0.88f, 0.48f, 28.6250, 77.3300),
                CityHazardPin(103L, "Urban Traffic Congestion", "ITO Junction & Vikas Marg", "Vehicle Exhaust", "High", "Peak hour vehicle gridlock causing high NO2 and CO spikes.", "1.5 hours ago", "Simran B.", 31, 0.58f, 0.52f, 28.6310, 77.2450)
            )
        ),
        CityAirProfile(
            id = "noida_up",
            cityName = "Noida",
            state = "Uttar Pradesh (NCR)",
            zone = "North",
            subText = "Sector 62, Sector 125 & Expressway",
            stationName = "UPPCB Sector 62 Continuous Station, Noida",
            baseAqi = 232,
            latitude = 28.5355,
            longitude = 77.3910,
            landmarkWater = "Hindon River Canal",
            majorHighway = "Noida-Greater Noida Expressway",
            hazardPins = listOf(
                CityHazardPin(111L, "High-Rise Construction Dust", "Sector 150 Real Estate Corridor", "Construction Dust", "Severe", "Multiple mega towers undergoing excavation without anti-smog water guns.", "1 hour ago", "Vikas P.", 27, 0.65f, 0.72f, 28.4550, 77.4750)
            )
        ),
        CityAirProfile(
            id = "gurugram_hr",
            cityName = "Gurugram",
            state = "Haryana (NCR)",
            zone = "North",
            subText = "Cyber City, Golf Course Road & Manesar",
            stationName = "HSPCB Vikas Sadan Station, Gurugram",
            baseAqi = 218,
            latitude = 28.4595,
            longitude = 77.0266,
            landmarkWater = "Najafgarh Drain Perimeter",
            majorHighway = "Delhi-Jaipur Expressway (NH-48)",
            hazardPins = listOf(
                CityHazardPin(121L, "Highway Freight Diesel Idling", "Kherki Daula Toll Plaza", "Diesel Soot", "High", "Truck congestion along commercial transit bottleneck.", "2 hours ago", "Abhishek R.", 23, 0.45f, 0.60f, 28.3950, 76.9850)
            )
        ),
        CityAirProfile(
            id = "lucknow_up",
            cityName = "Lucknow",
            state = "Uttar Pradesh",
            zone = "North",
            subText = "Lalbagh, Gomti Nagar & Alambagh",
            stationName = "UPPCB Central Lalbagh Air Observatory",
            baseAqi = 204,
            latitude = 26.8467,
            longitude = 80.9462,
            landmarkWater = "Gomti River Basin",
            majorHighway = "Shaheed Path & Kanpur Road",
            hazardPins = listOf(
                CityHazardPin(131L, "Biomass & Leaf Burning in Enclaves", "Old City Chowk Alleys", "Open Burning", "High", "Winter evening heating fires and leaf sweeps.", "45 mins ago", "Mohd Rizwan", 19, 0.38f, 0.45f, 26.8650, 80.9050)
            )
        ),
        CityAirProfile(
            id = "kanpur_up",
            cityName = "Kanpur",
            state = "Uttar Pradesh",
            zone = "North",
            subText = "Nehru Nagar, Panki & Jajmau",
            stationName = "CPCB IIT Kanpur / Nehru Nagar Station",
            baseAqi = 215,
            latitude = 26.4499,
            longitude = 80.3319,
            landmarkWater = "Ganga River Basin",
            majorHighway = "Grand Trunk Road (NH-19)",
            hazardPins = listOf(
                CityHazardPin(141L, "Industrial Boiler Exhaust", "Panki Industrial Area Site 1", "Industrial", "Severe", "Unscrubbed solid fuel emissions from small chemical units.", "2 hours ago", "Pradeep S.", 26, 0.32f, 0.40f, 26.4850, 80.2550)
            )
        ),
        CityAirProfile(
            id = "varanasi_up",
            cityName = "Varanasi",
            state = "Uttar Pradesh",
            zone = "North",
            subText = "Ardhali Bazar, BHU & Godowlia",
            stationName = "UPPCB BHU Air Monitoring Station",
            baseAqi = 186,
            latitude = 25.3176,
            longitude = 82.9739,
            landmarkWater = "Ganga River Ghats",
            majorHighway = "NH-19 Varanasi Ring Road",
            hazardPins = listOf(
                CityHazardPin(151L, "Road Resurfacing Asphalt Fumes", "Lahartara Ring Road Junction", "Fumes", "High", "Hot bitumen boiler operating in proximity to market shops.", "1.5 hours ago", "Anand T.", 17, 0.50f, 0.55f, 25.3200, 82.9600)
            )
        ),
        CityAirProfile(
            id = "chandigarh_ut",
            cityName = "Chandigarh",
            state = "Punjab & Haryana",
            zone = "North",
            subText = "Sector 17, Sector 22 & Industrial Area",
            stationName = "CPCC Sector 22 Continuous Air Station",
            baseAqi = 112,
            latitude = 30.7333,
            longitude = 76.7794,
            landmarkWater = "Sukhna Lake Basin",
            majorHighway = "Madhya Marg & Himalayan Expressway",
            hazardPins = listOf(
                CityHazardPin(161L, "Agri-Residue Drift Incursion", "Sector 39 Grain Market Fringe", "Crop Smoke", "Moderate", "Paddy stubble smoke drifting from surrounding Mohali/Kharar fields.", "3 hours ago", "Gurpreet S.", 18, 0.28f, 0.52f, 30.7450, 76.7350)
            )
        ),
        CityAirProfile(
            id = "ludhiana_pb",
            cityName = "Ludhiana",
            state = "Punjab",
            zone = "North",
            subText = "Industrial Area A, Focal Point & Dholewal",
            stationName = "PPCB Focal Point Station, Ludhiana",
            baseAqi = 168,
            latitude = 30.9010,
            longitude = 75.8573,
            landmarkWater = "Budha Nullah Corridor",
            majorHighway = "GT Road (NH-44)",
            hazardPins = listOf(
                CityHazardPin(171L, "Dyeing Unit Boiler Stack Smoke", "Industrial Area B Textile Cluster", "Industrial", "Severe", "Black particulate smoke settling in evening thermal inversion.", "1 hour ago", "Harbinder P.", 25, 0.65f, 0.45f, 30.8850, 75.8750)
            )
        ),
        CityAirProfile(
            id = "amritsar_pb",
            cityName = "Amritsar",
            state = "Punjab",
            zone = "North",
            subText = "Golden Temple Area, Civil Lines & GT Road",
            stationName = "PPCB Golden Temple Heritage Station",
            baseAqi = 144,
            latitude = 31.6340,
            longitude = 74.8723,
            landmarkWater = "Tung Dhab Drain",
            majorHighway = "Grand Trunk Road",
            hazardPins = listOf(
                CityHazardPin(181L, "Auto-Rickshaw 2-Stroke Emissions", "Hall Gate Entry Corridor", "Vehicle Exhaust", "Moderate", "Dense localized exhaust accumulation in walled city gates.", "2 hours ago", "Manpreet K.", 14, 0.50f, 0.48f, 31.6310, 74.8750)
            )
        ),
        CityAirProfile(
            id = "jaipur_rj",
            cityName = "Jaipur",
            state = "Rajasthan",
            zone = "North",
            subText = "Mansarovar, Ajmer Road & MI Road",
            stationName = "RSPCB Police Commissionerate Station, Jaipur",
            baseAqi = 174,
            latitude = 26.9124,
            longitude = 75.7873,
            landmarkWater = "Dravyavati River Basin",
            majorHighway = "Jaipur-Delhi Highway (NH-48)",
            hazardPins = listOf(
                CityHazardPin(191L, "Dry Arid Desert Road Dust", "Mansarovar Bypass Crossing", "Mineral Dust", "High", "High wind carrying loose soil and stone dust across thoroughfares.", "1 hour ago", "Gaurav S.", 16, 0.40f, 0.65f, 26.8650, 75.7650)
            )
        ),
        CityAirProfile(
            id = "dehradun_uk",
            cityName = "Dehradun",
            state = "Uttarakhand",
            zone = "North",
            subText = "Clock Tower, ISBT & Rajpur Road",
            stationName = "UKPCB Clock Tower Observatory",
            baseAqi = 86,
            latitude = 30.3165,
            longitude = 78.0322,
            landmarkWater = "Bindal River Stream",
            majorHighway = "Haridwar-Dehradun Highway",
            hazardPins = listOf(
                CityHazardPin(201L, "Valley Inversion Transit Haze", "Saharanpur Road Junction", "Transit", "Moderate", "Winter evening haze trapped in Doon valley floor.", "2 hours ago", "Tarun N.", 9, 0.48f, 0.55f, 30.3050, 78.0150)
            )
        ),
        CityAirProfile(
            id = "srinagar_jk",
            cityName = "Srinagar",
            state = "Jammu & Kashmir",
            zone = "North",
            subText = "Rajbagh, Dal Lake & Lal Chowk",
            stationName = "JKPCC Lal Chowk Station, Srinagar",
            baseAqi = 52,
            latitude = 34.0837,
            longitude = 74.7973,
            landmarkWater = "Jhelum River & Dal Lake",
            majorHighway = "NH-44 Jammu-Srinagar Highway",
            hazardPins = listOf(
                CityHazardPin(211L, "Domestic Charcoal Heating Smoke", "Old Downtown Zaina Kadal", "Wood Burning", "Moderate", "Traditional Kangri and Bukhari heating smoke in dense lanes.", "3 hours ago", "Farooq A.", 12, 0.52f, 0.42f, 34.0920, 74.8050)
            )
        ),

        // --- Western India (Maharashtra, Gujarat, Goa) ---
        CityAirProfile(
            id = "mumbai_metro",
            cityName = "Mumbai",
            state = "Maharashtra",
            zone = "West",
            subText = "Bandra-Kurla Complex (BKC), Colaba & Andheri",
            stationName = "MPCB BKC Continuous Ambient Monitoring Station",
            baseAqi = 118,
            latitude = 19.0760,
            longitude = 72.8777,
            landmarkWater = "Arabian Sea Coast & Mithi River",
            majorHighway = "Western Express Highway & Coastal Road",
            hazardPins = listOf(
                CityHazardPin(301L, "Refinery & Petrochemical Flare", "Mahul Chembur Corridor", "Industrial", "Severe", "Sulfur and VOC emissions trapped under coastal marine inversion.", "40 mins ago", "Aditya K.", 34, 0.72f, 0.62f, 19.0150, 72.8950),
                CityHazardPin(302L, "Metro Underground Haulage Dust", "WEH Andheri East Subway Line", "Construction Dust", "High", "Continuous dumpers stirring up crushed basalt dust.", "1.5 hours ago", "Rhea S.", 21, 0.42f, 0.38f, 19.1150, 72.8650),
                CityHazardPin(303L, "Coastal Highway Vehicular Drift", "Bandra Reclamation Flyover", "Traffic Soot", "Moderate", "Heavy bumper-to-bumper sea-link approach queue.", "2 hours ago", "Faisal Q.", 17, 0.35f, 0.55f, 19.0450, 72.8350)
            )
        ),
        CityAirProfile(
            id = "pune_mh",
            cityName = "Pune",
            state = "Maharashtra",
            zone = "West",
            subText = "Shivajinagar, Hinjawadi IT Park & Hadapsar",
            stationName = "IITM SAFAR Shivajinagar Station, Pune",
            baseAqi = 92,
            latitude = 18.5204,
            longitude = 73.8567,
            landmarkWater = "Mula-Mutha River Basin",
            majorHighway = "Mumbai-Pune Expressway & Katraj Bypass",
            hazardPins = listOf(
                CityHazardPin(311L, "IT Corridor Commuter Exhaust", "Hinjawadi Phase 1 Flyover", "Vehicle Exhaust", "Moderate", "Peak software shift transit gridlock along ridge road.", "1 hour ago", "Pranav D.", 16, 0.28f, 0.35f, 18.5950, 73.7350)
            )
        ),
        CityAirProfile(
            id = "nagpur_mh",
            cityName = "Nagpur",
            state = "Maharashtra",
            zone = "West",
            subText = "Civil Lines, Sitabuldi & MIHAN",
            stationName = "MPCB Civil Lines Continuous Station, Nagpur",
            baseAqi = 88,
            latitude = 21.1458,
            longitude = 79.0882,
            landmarkWater = "Nag River Stream & Futala Lake",
            majorHighway = "Samruddhi Mahamarg & NH-44",
            hazardPins = listOf(
                CityHazardPin(321L, "Freight Terminal Diesel Shunting", "Ajni Railway Goods Yard", "Diesel Soot", "Moderate", "Heavy locomotives and multi-axle freight movement.", "2.5 hours ago", "Sachin W.", 11, 0.55f, 0.58f, 21.1250, 79.0950)
            )
        ),
        CityAirProfile(
            id = "ahmedabad_gj",
            cityName = "Ahmedabad",
            state = "Gujarat",
            zone = "West",
            subText = "Maninagar, SG Highway & Vatva GIDC",
            stationName = "GPCB Vatva Industrial Continuous Station",
            baseAqi = 162,
            latitude = 23.0225,
            longitude = 72.5714,
            landmarkWater = "Sabarmati Riverfront",
            majorHighway = "S.G. Highway & SP Ring Road",
            hazardPins = listOf(
                CityHazardPin(331L, "Chemical Waste Thermal Incinerator", "Vatva GIDC Phase 3", "Chemical Smoke", "Severe", "Pungent chemical odor blowing towards eastern residential suburbs.", "30 mins ago", "Chirag P.", 28, 0.75f, 0.68f, 22.9650, 72.6350),
                CityHazardPin(332L, "Commercial Ring Road Transit Dust", "SG Highway Iskcon Flyover", "Fugitive Dust", "High", "Heavy truck and bus traffic kicking up shoulder dust.", "1.5 hours ago", "Hetvi M.", 18, 0.32f, 0.42f, 23.0300, 72.5050)
            )
        ),
        CityAirProfile(
            id = "surat_gj",
            cityName = "Surat",
            state = "Gujarat",
            zone = "West",
            subText = "Athwa, Sachin GIDC & Varachha",
            stationName = "GPCB Sachin Industrial Station, Surat",
            baseAqi = 128,
            latitude = 21.1702,
            longitude = 72.8311,
            landmarkWater = "Tapi River Basin",
            majorHighway = "Surat-Dumas Road & NH-48",
            hazardPins = listOf(
                CityHazardPin(341L, "Textile Processing Steam Boiler", "Pandesara GIDC Cluster", "Industrial", "High", "Lignite boiler particulate stack discharge.", "1 hour ago", "Bhavik S.", 20, 0.65f, 0.55f, 21.1450, 72.8450)
            )
        ),

        // --- Southern India (Karnataka, Tamil Nadu, Telangana, AP, Kerala) ---
        CityAirProfile(
            id = "bengaluru_tech",
            cityName = "Bengaluru",
            state = "Karnataka",
            zone = "South",
            subText = "BTM Layout, Whitefield & Silk Board Junction",
            stationName = "KSPCB BTM Layout Continuous Station",
            baseAqi = 68,
            latitude = 12.9716,
            longitude = 77.5946,
            landmarkWater = "Vrishabhavathi River Basin & Bellandur Lake",
            majorHighway = "Outer Ring Road (ORR) & Hosur Road (NH-44)",
            hazardPins = listOf(
                CityHazardPin(401L, "Transit Bottleneck Black Carbon", "Central Silk Board Junction", "Vehicle Exhaust", "Moderate", "Dense intersection idling creating elevated fine particulate pocket.", "25 mins ago", "Ganesh K.", 26, 0.52f, 0.68f, 12.9175, 77.6235),
                CityHazardPin(402L, "Tech Corridor Metro Line Dust", "Marathahalli-Kadubeesanahalli ORR", "Construction Dust", "Moderate", "Under-construction flyover pillar and barricade road dust.", "1.5 hours ago", "Deepa R.", 15, 0.78f, 0.52f, 12.9450, 77.6950),
                CityHazardPin(403L, "Industrial Metal Finishing", "Peenya Industrial Area Stage 2", "Industrial", "High", "Metal fumes drifting into north-west residential colonies.", "2.5 hours ago", "Suresh B.", 19, 0.25f, 0.32f, 13.0350, 77.5150)
            )
        ),
        CityAirProfile(
            id = "chennai_tn",
            cityName = "Chennai",
            state = "Tamil Nadu",
            zone = "South",
            subText = "Alandur, Marina Beach & Manali Petrochemical",
            stationName = "TNPCB Alandur Bus Depot Station, Chennai",
            baseAqi = 76,
            latitude = 13.0827,
            longitude = 80.2707,
            landmarkWater = "Cooum & Adyar River Estuaries",
            majorHighway = "Mount Road (Anna Salai) & GST Road",
            hazardPins = listOf(
                CityHazardPin(411L, "Petrochemical Flare Stack", "Manali Industrial Belt", "Industrial", "High", "North Chennai refinery emission drift with sea breeze.", "1 hour ago", "Karthik V.", 27, 0.70f, 0.25f, 13.1650, 80.2550),
                CityHazardPin(412L, "Wholesale Market Truck Emissions", "Koyambedu Perishable Market Hub", "Diesel Exhaust", "Moderate", "Heavy midnight truck freight unloading soot.", "3 hours ago", "Meenakshi S.", 14, 0.38f, 0.52f, 13.0700, 80.1900)
            )
        ),
        CityAirProfile(
            id = "hyderabad_tg",
            cityName = "Hyderabad",
            state = "Telangana",
            zone = "South",
            subText = "Gachibowli, Charminar & Sanathnagar",
            stationName = "TSPCB Sanathnagar Continuous Ambient Station",
            baseAqi = 134,
            latitude = 17.3850,
            longitude = 78.4867,
            landmarkWater = "Musi River Basin & Hussain Sagar",
            majorHighway = "Outer Ring Road (Nehru ORR) & PVNR Expressway",
            hazardPins = listOf(
                CityHazardPin(421L, "Bulk Drug Manufacturing Odor", "Jeedimetla Industrial Estate", "Chemical Solvents", "Severe", "Volatile organic compound emissions drifting south.", "45 mins ago", "Venkatesh M.", 30, 0.45f, 0.28f, 17.5150, 78.4650),
                CityHazardPin(422L, "Old City Walled Traffic Congestion", "Charminar-Nayapul Corridor", "Vehicle Exhaust", "Moderate", "Heavy two-wheeler exhaust trapped in heritage bazaar corridors.", "2 hours ago", "Syed Zafar", 13, 0.52f, 0.65f, 17.3620, 78.4750)
            )
        ),
        CityAirProfile(
            id = "kochi_kl",
            cityName = "Kochi",
            state = "Kerala",
            zone = "South",
            subText = "Kadavanthra, Vytilla Hub & Eloor",
            stationName = "KSPCB Eloor Industrial Air Station",
            baseAqi = 48,
            latitude = 9.9312,
            longitude = 76.2673,
            landmarkWater = "Vembanad Backwaters & Periyar River",
            majorHighway = "Edappally-Aroor Bypass (NH-66)",
            hazardPins = listOf(
                CityHazardPin(431L, "Fertilizer Plant Flue Gas", "Udyogamandal Industrial Island", "Industrial", "Moderate", "Moist marine air carrying localized ammonia scent.", "2 hours ago", "Thomas J.", 11, 0.60f, 0.35f, 10.0650, 76.2950)
            )
        ),
        CityAirProfile(
            id = "visakhapatnam_ap",
            cityName = "Visakhapatnam",
            state = "Andhra Pradesh",
            zone = "South",
            subText = "Gajuwaka, Beach Road & Steel Plant Zone",
            stationName = "APPCB Gajuwaka Continuous Station, Vizag",
            baseAqi = 102,
            latitude = 17.6868,
            longitude = 83.2185,
            landmarkWater = "Bay of Bengal Sea Coast",
            majorHighway = "NH-16 Kolkata-Chennai Highway",
            hazardPins = listOf(
                CityHazardPin(441L, "Coke Oven Coal Dust", "Autonagar Industrial Belt", "Coal Dust", "High", "Fugitive carbon dust carried by coastal gusts.", "1.5 hours ago", "Ravi Teja", 18, 0.35f, 0.62f, 17.6950, 83.1850)
            )
        ),

        // --- Eastern & North-Eastern India (West Bengal, Bihar, Odisha, Jharkhand, Assam) ---
        CityAirProfile(
            id = "kolkata_wb",
            cityName = "Kolkata",
            state = "West Bengal",
            zone = "East",
            subText = "Victoria Memorial, Salt Lake Sector V & Ballygunge",
            stationName = "WBPCB Victoria Memorial Observatory",
            baseAqi = 156,
            latitude = 22.5726,
            longitude = 88.3639,
            landmarkWater = "Hooghly River Basin",
            majorHighway = "EM Bypass & Kona Expressway",
            hazardPins = listOf(
                CityHazardPin(501L, "Foundry Cupola Plume", "Howrah Ghusuri Industrial Belt", "Industrial", "Severe", "Dense grey particulate stack discharge crossing Hooghly river.", "50 mins ago", "Debashis R.", 38, 0.28f, 0.42f, 22.6050, 88.3450),
                CityHazardPin(502L, "Commercial Diesel Freight Shunting", "Chitpur Railway Freight Yards", "Diesel Soot", "High", "Old commercial lorries and freight locos emitting black carbon.", "2 hours ago", "Sourav M.", 22, 0.55f, 0.32f, 22.5950, 88.3850),
                CityHazardPin(503L, "Wetlands Perimeter Refuse Fire", "East Kolkata Wetlands Dhapa Dumpsite", "Open Burning", "Severe", "Smoldering garbage mounds sending smoke across EM Bypass.", "1 hour ago", "Priyanka G.", 45, 0.78f, 0.58f, 22.5450, 88.4250)
            )
        ),
        CityAirProfile(
            id = "patna_br",
            cityName = "Patna",
            state = "Bihar",
            zone = "East",
            subText = "Danapur, Eco Park & Gandhi Maidan",
            stationName = "BSPCB Samanpura Continuous Station, Patna",
            baseAqi = 228,
            latitude = 25.5941,
            longitude = 85.1376,
            landmarkWater = "Ganga River Basin",
            majorHighway = "Bailey Road & Patna Ring Road",
            hazardPins = listOf(
                CityHazardPin(511L, "Brick Kiln Cluster Chimney Smoke", "Digha-Ganga Diara Sandbank", "Brick Smoke", "Hazardous", "Dozens of bull trench brick kilns firing unrefined coal on river islands.", "30 mins ago", "Alok K.", 52, 0.40f, 0.25f, 25.6550, 85.0950),
                CityHazardPin(512L, "Alluvium Silt Road Dust", "Boring Canal Road Concourse", "Road Dust", "Severe", "Passing heavy transit kicking up loose unpaved river basin silt.", "1 hour ago", "Shweta N.", 24, 0.55f, 0.55f, 25.6150, 85.1150)
            )
        ),
        CityAirProfile(
            id = "bhubaneswar_od",
            cityName = "Bhubaneswar",
            state = "Odisha",
            zone = "East",
            subText = "Patia, Chandrasekharpur & Master Canteen",
            stationName = "OSPCB Secretariat Road Station, Bhubaneswar",
            baseAqi = 98,
            latitude = 20.2961,
            longitude = 85.8245,
            landmarkWater = "Kuakhai River Basin",
            majorHighway = "NH-16 Kolkata-Chennai Highway",
            hazardPins = listOf(
                CityHazardPin(521L, "IT Corridor Traffic Congestion", "Patia Big Bazaar Square", "Vehicle Exhaust", "Moderate", "Office peak traffic along Nandankanan Road.", "1.5 hours ago", "Subhashree P.", 14, 0.60f, 0.38f, 20.3550, 85.8200)
            )
        ),
        CityAirProfile(
            id = "ranchi_jh",
            cityName = "Ranchi",
            state = "Jharkhand",
            zone = "East",
            subText = "Doranda, Albert Ekka Chowk & Tupudana",
            stationName = "JSPCB Kutchery Road Station, Ranchi",
            baseAqi = 122,
            latitude = 23.3441,
            longitude = 85.3096,
            landmarkWater = "Subarnarekha River Basin",
            majorHighway = "Ranchi Ring Road & NH-33",
            hazardPins = listOf(
                CityHazardPin(531L, "Industrial Stone Crushing", "Tupudana Industrial Area", "Stone Dust", "High", "Dense silica dust drifting towards Hatia residential sectors.", "2 hours ago", "Amitabh T.", 19, 0.48f, 0.68f, 23.2850, 85.3150)
            )
        ),
        CityAirProfile(
            id = "guwahati_as",
            cityName = "Guwahati",
            state = "Assam",
            zone = "North-East",
            subText = "Panbazar, Railway Colony & Dispur",
            stationName = "PCBA Bamunimaidam Central Station, Guwahati",
            baseAqi = 82,
            latitude = 26.1445,
            longitude = 91.7362,
            landmarkWater = "Brahmaputra River Basin",
            majorHighway = "GS Road & Guwahati Bypass (NH-27)",
            hazardPins = listOf(
                CityHazardPin(541L, "Hill Cutting Earth Dust", "Khanapara Foothills Development", "Earth Silt", "Moderate", "Unprotected hillside earth movement creating localized dust plumes.", "2.5 hours ago", "Bikram D.", 16, 0.75f, 0.62f, 26.1150, 91.8150)
            )
        )
    )

    fun getCityById(id: String): CityAirProfile? {
        return allCities.find { it.id == id }
    }

    fun findCityByName(name: String): CityAirProfile {
        return allCities.find {
            name.contains(it.cityName, ignoreCase = true) || it.cityName.contains(name, ignoreCase = true)
        } ?: allCities.first()
    }
}
