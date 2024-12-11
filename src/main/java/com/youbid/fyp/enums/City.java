package com.youbid.fyp.enums;

public enum City {
    ISLAMABAD,
    KARACHI,
    LAHORE,
    FSD,
    MULTAN,
    PESHAWAR,
    QUETTA,
    RAWALPINDI,
    GILGIT,
    ABBOTTABAD,
    SIALKOT,
    HYDERABAD,
    GUJRANWALA,
    SARGODHA,
    JHELUM,
    LARKANA,
    MIRPUR,
    BAHAWALPUR,
    KOTLI;

    @Override
    public String toString() {
        return name().replace('_', ' ').toLowerCase();
    }
}
