package com.sathish.thodar.data.repository;

import com.sathish.thodar.data.dto.response.Station;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StationMaster {

    private static List<Station> createRoute(String... stations) {
        return Arrays.stream(stations)
                .map(Station::fromString)
                .collect(Collectors.toList());
    }
    // ==========================================
    // 1. CHENNAI - MADURAI (Chord & Main Lines)
    // ==========================================
    public static final List<Station> SR_CHORD_LINE = createRoute(
            "MS/Chennai Egmore", "TBM/Tambaram", "CGL/Chengalpattu", "MLMR/Melmaruvathur",
            "VM/Villupuram", "VRI/Vriddhachalam", "ALU/Ariyalur", "SRGM/Srirangam",
            "TPJ/Tiruchirappalli", "DG/Dindigul", "KQN/Kodaikanal Road", "MDU/Madurai"
    );

    public static final List<Station> SR_MAIN_LINE = createRoute(
            "MS/Chennai Egmore", "TBM/Tambaram", "CGL/Chengalpattu", "VM/Villupuram",
            "CUPJ/Cuddalore Port", "CDM/Chidambaram", "MV/Mayiladuthurai", "KMU/Kumbakonam",
            "TJ/Thanjavur", "TPJ/Tiruchirappalli", "DG/Dindigul", "MDU/Madurai"
    );

    // ==========================================
    // 2. WEST COAST & KERALA LINES
    // ==========================================
    public static final List<Station> SR_WEST_COAST_LINE = createRoute(
            "MAS/Chennai Central", "TRL/Tiruvallur", "AJJ/Arakkonam", "KPD/Katpadi",
            "JTJ/Jolarpettai", "SA/Salem", "ED/Erode", "TUP/Tiruppur", "CBE/Coimbatore"
    );

    public static final List<Station> SR_KERALA_SOUTH_LINE = createRoute(
            "CBE/Coimbatore", "PGT/Palakkad", "TCR/Thrissur", "AWY/Aluva",
            "ERN/Ernakulam Town", "KTYM/Kottayam", "CNGR/Chengannur", "QLN/Kollam",
            "TVC/Thiruvananthapuram", "NCJ/Nagercoil"
    );

    public static final List<Station> SR_MALABAR_LINE = createRoute(
            "SRR/Shoranur", "TIR/Tirur", "CLT/Kozhikode", "BDJ/Vadakara",
            "TLY/Thalassery", "CAN/Kannur", "KGQ/Kasaragod", "MAQ/Mangaluru Central"
    );

    // ==========================================
    // 3. CROSS ROUTES
    // ==========================================
    public static final List<Station> SR_CBE_MDU_LINE = createRoute(
            "CBE/Coimbatore", "POY/Pollachi", "UDT/Udumalappettai", "PLNI/Palani",
            "ODC/Oddanchatram", "DG/Dindigul", "MDU/Madurai"
    );

    public static final List<Station> SR_TPJ_RMM_LINE = createRoute(
            "TPJ/Tiruchirappalli", "PDKT/Pudukkottai", "KKDI/Karaikkudi",
            "SVGA/Sivaganga", "MNM/Manamadurai", "PMK/Paramakkudi", "RMD/Ramanathapuram",
            "RMM/Rameswaram"
    );

    // ==========================================
    // 4. DEEP SOUTH EXTENSIONS (From Madurai)
    // ==========================================
    public static final List<Station> SR_EXT_TIRUNELVELI = createRoute(
            "MDU/Madurai", "VPT/Virudhunagar", "SRT/Satur", "CVP/Kovilpatti", "TEN/Tirunelveli"
    );

    public static final List<Station> SR_EXT_KANYAKUMARI = createRoute(
            "MDU/Madurai", "VPT/Virudhunagar", "SRT/Satur", "CVP/Kovilpatti",
            "TEN/Tirunelveli", "VLY/Valliyur", "NCJ/Nagercoil", "CAPE/Kanyakumari"
    );

    public static final List<Station> SR_EXT_TUTICORIN = createRoute(
            "MDU/Madurai", "VPT/Virudhunagar", "SRT/Satur", "CVP/Kovilpatti",
            "MEJ/Vanchi Maniyachchi", "TN/Tuticorin"
    );

    public static final List<Station> SR_EXT_SENGOTTAI = createRoute(
            "MDU/Madurai", "VPT/Virudhunagar", "SVKS/Sivakasi", "RJPM/Rajapalayam",
            "SNKL/Sankarankovil", "TSI/Tenkasi", "SCT/Sengottai"
    );

    public static final List<Station> SR_EXT_RAMESWARAM = createRoute(
            "MDU/Madurai", "MNM/Manamadurai", "PMK/Paramakkudi", "RMD/Ramanathapuram",
            "MMM/Mandapam", "RMM/Rameswaram"
    );
}