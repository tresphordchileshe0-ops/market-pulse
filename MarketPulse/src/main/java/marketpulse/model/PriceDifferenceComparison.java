package marketpulse.model;

import java.math.BigDecimal;

/** Compares prices by absolute difference, e.g. "10.00 ZMW cheaper". */
public class PriceDifferenceComparison implements PriceComparison {

    @Override
    public String compare(PriceObservation a, PriceObservation b) {
        BigDecimal diff = a.getPrice().subtract(b.getPrice());
        BigDecimal abs = diff.abs();

        if (diff.signum() == 0) {
            return "Both sellers charge the same: " + abs + " ZMW";
        }

        String cheaperSeller = diff.signum() < 0
                ? a.getSeller().getName()
                : b.getSeller().getName();

        return cheaperSeller + " is cheaper by " + abs + " ZMW";
    }
}
