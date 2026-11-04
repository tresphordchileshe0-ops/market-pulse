package marketplace.model;

/**
 * Represents a market trader / shop in Zambia.
 * Extends MarketRecord for shared identity behaviour.
 */
public class Seller extends MarketRecord {

    private final String name;
    private final String location;

    public Seller(String id, String name, String location) {
        super(id); // validates id and stores it
        this.name = requireText(name, "Seller name");
        this.location = requireText(location, "Location");
    }

    /** Same helper style as Product — private, defensive, trimmed. */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                fieldName + " cannot be empty."
            );
        }
        return value.trim();
    }

    public String getName() { return name; }
    public String getLocation() { return location; }

    /** Nice for debugging / console output. */
    @Override
    public String toString() {
        return "Seller[" + getId() + ", " + name + ", " + location + "]";
    }
}
