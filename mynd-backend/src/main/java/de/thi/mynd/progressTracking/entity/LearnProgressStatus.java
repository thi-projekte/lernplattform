package de.thi.mynd.progressTracking.entity;

public enum LearnProgressStatus {
    STARTED("STARTED"),
    ALL_CONTENT_ELEMENTS_COMPLETED("ALL_CONTENT_ELEMENTS_COMPLETED"),
    COMPLETED_MANUALLY("COMPLETED_MANUALLY");

    public final String label;

    private LearnProgressStatus(String label) {
        this.label = label;
    }
}
