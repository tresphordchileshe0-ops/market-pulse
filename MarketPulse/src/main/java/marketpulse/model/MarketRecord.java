package marketpulse.model;

public abstract class MarketRecord {

    private final String id;

    protected MarketRecord(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty.");
        }

        this.id = id.trim();
    }

    public String getId() {
        return id;
    }
}