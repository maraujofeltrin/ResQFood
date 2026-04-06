package ar.edu.itba.paw.models;

public enum PackTag {
    VEGAN("Vegano"),
    VEGETARIAN("Vegetariano"),
    GLUTEN_FREE("Sin TACC"),
    SWEET("Dulce"),
    SAVORY("Salado"),
    ORGANIC("Orgánico"),
    DAIRY_FREE("Sin Lácteos"),
    SUGAR_FREE("Sin Azúcar"),
    SPICY("Picante"),
    FRESH("Fresco"),
    HIGH_PROTEIN("Alto en Proteína"),
    LOW_CALORIE("Bajo en Calorías");

    private final String displayName;

    PackTag(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
