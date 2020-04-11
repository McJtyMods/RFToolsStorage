package mcjty.rftoolsstorage.modules.scanner.tools;

public enum SortingMode {
    AMOUNT_ASCENDING("Amount+"),
    AMOUNT_DESCENDING("Amount-"),
    NAME("Name");

    private final String description;

    SortingMode(String description) {
        this.description = description;
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
}
