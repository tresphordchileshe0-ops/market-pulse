package marketpulse.model;

/**
 * Strategy interface for comparing two price observations.
 * Different implementations give different perspectives
 * (absolute difference vs percentage change) via the SAME method call.
 */
public interface PriceComparison {

    /** A human-readable result of the comparison. */
    String compare(PriceObservation a, PriceObservation b);
}
