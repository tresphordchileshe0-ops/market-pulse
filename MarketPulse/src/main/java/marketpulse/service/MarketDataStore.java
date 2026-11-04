package marketpulse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import marketpulse.model.PriceObservation;
import marketpulse.model.Product;
import marketpulse.model.Seller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MarketDataStore {

    private final Path file;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MarketDataStore(Path file) {
        this.file = file;
    }

    public synchronized LoadedData load() {
        if (!Files.exists(file)) {
            return new LoadedData(List.of(), List.of(), List.of());
        }

        try {
            DataFile saved = objectMapper.readValue(file.toFile(), DataFile.class);
            List<ProductData> productData = saved.products() == null
                ? List.of() : saved.products();
            List<SellerData> sellerData = saved.sellers() == null
                ? List.of() : saved.sellers();
            List<PriceData> priceData = saved.prices() == null
                ? List.of() : saved.prices();

            List<Product> products = productData.stream()
                .map(item -> new Product(item.id(), item.name(), item.category(), item.unit()))
                .toList();
            List<Seller> sellers = sellerData.stream()
                .map(item -> new Seller(item.id(), item.name(), item.location()))
                .toList();
            Map<String, Product> productsById = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
            Map<String, Seller> sellersById = sellers.stream()
                .collect(Collectors.toMap(Seller::getId, Function.identity()));
            List<PriceObservation> observations = new ArrayList<>();

            for (PriceData item : priceData) {
                Product product = productsById.get(item.productId());
                Seller seller = sellersById.get(item.sellerId());
                if (product == null || seller == null) {
                    throw new IllegalStateException(
                        "Saved price observation refers to a missing product or seller.");
                }
                observations.add(new PriceObservation(
                    item.id(),
                    product,
                    seller,
                    item.price(),
                    LocalDate.parse(item.recordedAt())
                ));
            }

            return new LoadedData(products, sellers, observations);
        } catch (IOException exception) {
            throw new PersistenceException("Could not load market data.", exception);
        }
    }

    public synchronized void save(
        List<Product> products,
        List<Seller> sellers,
        List<PriceObservation> observations
    ) {
        DataFile saved = new DataFile(
            products.stream()
                .map(product -> new ProductData(
                    product.getId(), product.getName(), product.getCategory(), product.getUnit()))
                .toList(),
            sellers.stream()
                .map(seller -> new SellerData(
                    seller.getId(), seller.getName(), seller.getLocation()))
                .toList(),
            observations.stream()
                .map(observation -> new PriceData(
                    observation.getId(),
                    observation.getProductId(),
                    observation.getSellerId(),
                    observation.getPrice(),
                    observation.getRecordedAt().toString()))
                .toList()
        );

        try {
            Files.createDirectories(file.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), saved);
        } catch (IOException exception) {
            throw new PersistenceException("Could not save market data.", exception);
        }
    }

    public record LoadedData(
        List<Product> products,
        List<Seller> sellers,
        List<PriceObservation> observations
    ) {
    }

    public record ProductData(String id, String name, String category, String unit) {
    }

    public record SellerData(String id, String name, String location) {
    }

    public record PriceData(
        String id,
        String productId,
        String sellerId,
        java.math.BigDecimal price,
        String recordedAt
    ) {
    }

    public record DataFile(
        List<ProductData> products,
        List<SellerData> sellers,
        List<PriceData> prices
    ) {
    }
}
