package mcjty.rftoolsstorage.modules.scanner.tools;

public enum SortingMode {
    AMOUNT_ASCENDING("Amount+", "Sort by amount (ascending)"),
    AMOUNT_DESCENDING("Amount-", "Sort by amount (descending)"),
    MOD("Mod", "Sort by mod"),
    TAG("Tag", "Sort by most common tag"),
    NAME("Name", "Sort by name");

    private final String description;
    private final String tooltip;

    SortingMode(String description, String tooltip) {
        this.description = description;
        this.tooltip = tooltip;
    }

    public static SortingMode byDescription(String s) {
        for (SortingMode mode : values()) {
            if (s.equals(mode.getDescription())) {
                return mode;
            }
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public String getTooltip() {
        return tooltip;
    }
}
