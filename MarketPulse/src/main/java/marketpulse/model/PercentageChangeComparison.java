package marketplace.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Compares prices by percentage change, e.g. "12.5% cheaper". */
public class PercentageChangeComparison implements PriceComparison {

    @Override
    public String compare(PriceObservation a, PriceObservation b) {
        BigDecimal priceA = a.getPrice();
        BigDecimal priceB = b.getPrice();

        if (priceA.compareTo(priceB) == 0) {
            return "Prices are equal (0% difference).";
        }

        // percentage relative to the more expensive observation
        BigDecimal higher = priceA.max(priceB);
        BigDecimal lower  = priceA.min(priceB);
        BigDecimal pct = higher.subtract(lower)
                .divide(higher, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        String cheaperSeller = priceA.compareTo(priceB) < 0
                ? a.getSeller().getName()
                : b.getSeller().getName();

        return cheaperSeller + " is cheaper by " + pct + "%";
    }
}
