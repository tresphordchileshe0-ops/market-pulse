package marketpulse.service;

import marketpulse.model.PriceObservation;
import marketpulse.model.Product;
import marketpulse.model.Seller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PriceService {

    private final List<PriceObservation> observations = new ArrayList<>();
    private final ProductService productService;
    private final SellerService sellerService;

    public PriceService(
        List<PriceObservation> observations,
        ProductService productService,
        SellerService sellerService
    ) {
        this.observations.addAll(observations);
        this.productService = productService;
        this.sellerService = sellerService;
    }

    public synchronized List<PriceObservation> getHistory(String productId) {
        return observations.stream()
            .filter(observation -> observation.getProductId().equals(productId))
            .sorted(Comparator.comparing(PriceObservation::getRecordedAt).reversed()
                .thenComparing(Comparator.comparingInt(observations::indexOf).reversed()))
            .toList();
    }

    public synchronized PriceObservation createObservation(
        String productId,
        String sellerId,
        BigDecimal price,
        String recordedAt
    ) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be empty.");
        }
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Seller ID cannot be empty.");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price is required.");
        }
        if (recordedAt == null || recordedAt.isBlank()) {
            throw new IllegalArgumentException("Recorded date is required.");
        }

        Product product = productService.findById(productId.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        Seller seller = sellerService.findById(sellerId.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found."));

        LocalDate date;
        try {
            date = LocalDate.parse(recordedAt.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Recorded date must use YYYY-MM-DD format.");
        }

        PriceObservation observation = new PriceObservation(
            UUID.randomUUID().toString(), product, seller, price, date);
        observations.add(observation);
        return observation;
    }

    public synchronized List<PriceObservation> getObservations() {
        return List.copyOf(observations);
    }

    public synchronized void deleteProductObservations(String productId) {
        observations.removeIf(observation -> observation.getProductId().equals(productId));
    }
}
