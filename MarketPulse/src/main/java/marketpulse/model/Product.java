package marketpulse.model;

public class Product extends MarketRecord {

    private final String name;
    private final String category;
    private final String unit;

    public Product(String id, String name, String category, String unit) {
        super(id);

        this.name = requireText(name, "Product name");
        this.category = requireText(category, "Category");
        this.unit = requireText(unit, "Unit");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                fieldName + " cannot be empty."
            );
        }

        return value.trim();
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }
}