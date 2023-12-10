package dev.wjteo;

import dev.wjteo.progressindicator.helper.ProgressStage;

public enum TProgressStage implements ProgressStage {
    ONE("Prepare", "Preparing a heartfelt speech"),
    TWO("Target", "Finding a target to deliver the speech"),
    THREE("Deliver", "Delivering heartfelt speech to fortunate target"),
    FOUR("Thank", "Expressing gratitude to target"),
    FIVE("Harass", "Followup harassment via phone call");

    private final String shortDescription;
    private final String longDescription;

    TProgressStage(final String shortDescription, final String longDescription) {
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }

    @Override
    public String getShortDescription() {
        return shortDescription;
    }

    @Override
    public String getLongDescription() {
        return longDescription;
    }

    @Override
    public int getIndex() {
        return ordinal();
    }
}
