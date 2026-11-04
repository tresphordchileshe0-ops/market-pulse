package marketpulse.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A single, time-stamped price for a Product sold by a Seller.
 * Immutable: a new observation is created instead of editing a price.
 * This makes tracking price changes reliable.
 */
public class PriceObservation extends MarketRecord {

    private final Product product;      // which good
    private final Seller seller;        // who sells it
    private final BigDecimal price;     // amount in ZMW (never double for money)
    private final LocalDate dateRecorded;

    public PriceObservation(String id,
                           Product product,
                           Seller seller,
                           BigDecimal price,
                           LocalDate dateRecorded) {
        super(id);
        this.product = Objects.requireNonNull(product, "Product is required.");
        this.seller  = Objects.requireNonNull(seller,  "Seller is required.");
        this.price   = requirePositive(price);
        this.dateRecorded = Objects.requireNonNull(
                dateRecorded, "Date recorded is required.");
    }

    /** Money must be positive; BigDecimal keeps ZMW amounts exact. */
    private static BigDecimal requirePositive(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Price must be greater than zero.");
        }
        return value;
    }

    public Product getProduct()       { return product; }
    public Seller getSeller()         { return seller; }
    public BigDecimal getPrice()       { return price; }
    public LocalDate getDateRecorded() { return dateRecorded; }

    /** Convenience for UI / JSON-like output. */
    public String getCurrency() { return "ZMW"; }

    @Override
    public String toString() {
        return "PriceObservation[" + getId()
                + ", " + product.getName()
                + " @ " + seller.getName()
                + ", " + price + " ZMW on " + dateRecorded + "]";
    }
}
