package ar.edu.itba.paw.models;

public enum PackTag {
    VEGAN("Vegano"),
    VEGETARIAN("Vegetariano"),
    GLUTEN_FREE("Sin TACC"),
    SWEET("Dulce"),
    SAVORY("Salado");

    private final String displayName;

    PackTag(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
