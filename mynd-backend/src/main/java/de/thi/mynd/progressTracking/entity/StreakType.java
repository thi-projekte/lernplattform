package de.thi.mynd.progressTracking.entity;

public enum StreakType {
    Daily("DAILY"),
    Weekly("WEEKLY"),
    Monthly("MONTHLY");

    private String label;

    StreakType(String label) {
        this.label = label;
    }
}
