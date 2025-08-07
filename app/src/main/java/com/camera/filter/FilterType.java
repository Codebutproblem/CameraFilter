package com.camera.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum FilterType {
    // No Filter
    NONE(0, "No Filter"),

    // LOMO Filters (1-5)
    LOMO_CLASSIC(1, "Classic LOMO"),
    LOMO_BLUE(2, "Blue LOMO"),
    LOMO_WARM(3, "Warm LOMO"),
    LOMO_GREEN(4, "Green LOMO"),
    LOMO_PURPLE(5, "Purple LOMO"),

    // RETRO Filters (6-10)
    RETRO_SEVENTIES(6, "Classic 70s"),
    RETRO_SEPIA(7, "Sepia Retro"),
    RETRO_FADED_FILM(8, "Faded Film"),
    RETRO_VINTAGE_PINK(9, "Vintage Pink"),
    RETRO_ORANGE_CRUSH(10, "Orange Crush"),

    // CUBE Filters (11-15)
    CUBE_COLOR_ENHANCE(11, "Color Cube 1"),
    CUBE_HIGH_CONTRAST(12, "Color Cube 2"),
    CUBE_CYAN_MAGENTA(13, "Color Cube 3"),
    CUBE_COOL_TONE(14, "Color Cube 4"),
    CUBE_NEON(15, "Color Cube 5"),

    // BLACK & WHITE Filters (16-20)
    BW_CLASSIC(16, "Classic B&W"),
    BW_HIGH_CONTRAST(17, "High Contrast B&W"),
    BW_SOFT(18, "Soft B&W"),
    BW_RED_CHANNEL(19, "Red Channel B&W"),
    BW_BLUE_CHANNEL(20, "Blue Channel B&W"),

    // VIGNETTE Filters (21-25)
    VIGNETTE_CLASSIC(21, "Classic Vignette"),
    VIGNETTE_STRONG(22, "Strong Vignette"),
    VIGNETTE_OVAL(23, "Oval Vignette"),
    VIGNETTE_COLOR(24, "Color Vignette"),
    VIGNETTE_TUNNEL(25, "Tunnel Vignette");

    private final int filterType;
    private final String displayName;

    FilterType(int filterType, String displayName) {
        this.filterType = filterType;
        this.displayName = displayName;
    }

    public int getFilterType() {
        return filterType;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Utility methods
    public static FilterType fromFilterType(int filterType) {
        for (FilterType filter : FilterType.values()) {
            if (filter.getFilterType() == filterType) {
                return filter;
            }
        }
        return NONE;
    }

    public static FilterType[] getLomoFilters() {
        return new FilterType[]{
                LOMO_CLASSIC, LOMO_BLUE, LOMO_WARM, LOMO_GREEN, LOMO_PURPLE
        };
    }

    public static FilterType[] getRetroFilters() {
        return new FilterType[]{
                RETRO_SEVENTIES, RETRO_SEPIA, RETRO_FADED_FILM, RETRO_VINTAGE_PINK, RETRO_ORANGE_CRUSH
        };
    }

    public static FilterType[] getCubeFilters() {
        return new FilterType[]{
                CUBE_COLOR_ENHANCE, CUBE_HIGH_CONTRAST, CUBE_CYAN_MAGENTA, CUBE_COOL_TONE, CUBE_NEON
        };
    }

    public static FilterType[] getBWFilters() {
        return new FilterType[]{
                BW_CLASSIC, BW_HIGH_CONTRAST, BW_SOFT, BW_RED_CHANNEL, BW_BLUE_CHANNEL
        };
    }

    public static FilterType[] getVignetteFilters() {
        return new FilterType[]{
                VIGNETTE_CLASSIC, VIGNETTE_STRONG, VIGNETTE_OVAL, VIGNETTE_COLOR, VIGNETTE_TUNNEL
        };
    }

    public static List<FilterType> getAllFilters() {
        return Arrays.asList(FilterType.values());
    }

    public boolean isLomoFilter() {
        return filterType >= 1 && filterType <= 5;
    }

    public boolean isRetroFilter() {
        return filterType >= 6 && filterType <= 10;
    }

    public boolean isCubeFilter() {
        return filterType >= 11 && filterType <= 15;
    }

    public boolean isBWFilter() {
        return filterType >= 16 && filterType <= 20;
    }

    public boolean isVignetteFilter() {
        return filterType >= 21 && filterType <= 25;
    }

    public String getFilterCategory() {
        if (isLomoFilter()) return "LOMO";
        if (isRetroFilter()) return "RETRO";
        if (isCubeFilter()) return "CUBE";
        if (isBWFilter()) return "BLACK & WHITE";
        if (isVignetteFilter()) return "VIGNETTE";
        return "NONE";
    }

    @Override
    public String toString() {
        return displayName;
    }
}