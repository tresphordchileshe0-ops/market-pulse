package marketpulse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import marketpulse.model.PriceComparison;
import marketpulse.model.PriceDifferenceComparison;
import marketpulse.model.PriceObservation;
import marketpulse.model.Product;
import marketpulse.model.Seller;
import marketpulse.service.MarketDataStore;
import marketpulse.service.PersistenceException;
import marketpulse.service.PriceService;
import marketpulse.service.ProductService;
import marketpulse.service.ResourceNotFoundException;
import marketpulse.service.SellerService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        MarketDataStore dataStore = new MarketDataStore(
            Path.of("data", "marketpulse-data.json"));
        MarketDataStore.LoadedData savedData = dataStore.load();
        ProductService productService = new ProductService(savedData.products());
        SellerService sellerService = new SellerService(savedData.sellers());
        PriceService priceService = new PriceService(
            savedData.observations(), productService, sellerService);

        Javalin app = Javalin.create(config -> config.staticFiles.add("/public"));
        app.exception(ResourceNotFoundException.class, (exception, ctx) ->
            ctx.status(400).json(Map.of("error", exception.getMessage())));
        app.exception(PersistenceException.class, (exception, ctx) ->
            ctx.status(500).json(Map.of("error", exception.getMessage())));

        app.get("/api/products", ctx -> ctx.json(productService.getProducts()));
        app.post("/api/products", ctx -> {
            ProductRequest request;
            try {
                request = JSON_MAPPER.readValue(ctx.body(), ProductRequest.class);
            } catch (JsonProcessingException exception) {
                ctx.status(400).json(Map.of("error", "Invalid JSON request body."));
                return;
            }
            if (request == null) {
                ctx.status(400).json(Map.of("error", "A product request is required."));
                return;
            }

            try {
                Product product = productService.createProduct(
                    request.name(), request.category(), request.unit());
                saveData(dataStore, productService, sellerService, priceService);
                ctx.status(201).json(product);
            } catch (IllegalArgumentException exception) {
                ctx.status(400).json(Map.of("error", exception.getMessage()));
            }
        });
        app.delete("/api/products/{productId}", ctx -> {
            String productId = ctx.pathParam("productId");
            if (!productService.deleteProduct(productId)) {
                throw new ResourceNotFoundException("Product not found.");
            }
            priceService.deleteProductObservations(productId);
            saveData(dataStore, productService, sellerService, priceService);
            ctx.status(200).json(Map.of("message", "Product deleted."));
        });

        app.get("/api/sellers", ctx -> ctx.json(sellerService.getSellers()));
        app.post("/api/sellers", ctx -> {
            SellerRequest request;
            try {
                request = JSON_MAPPER.readValue(ctx.body(), SellerRequest.class);
            } catch (JsonProcessingException exception) {
                ctx.status(400).json(Map.of("error", "Invalid JSON request body."));
                return;
            }
            if (request == null) {
                ctx.status(400).json(Map.of("error", "A seller request is required."));
                return;
            }

            try {
                Seller seller = sellerService.createSeller(
                    request.name(), request.location());
                saveData(dataStore, productService, sellerService, priceService);
                ctx.status(201).json(seller);
            } catch (IllegalArgumentException exception) {
                ctx.status(400).json(Map.of("error", exception.getMessage()));
            }
        });

        app.post("/api/prices", ctx -> {
            PriceRequest request;
            try {
                request = JSON_MAPPER.readValue(ctx.body(), PriceRequest.class);
            } catch (JsonProcessingException exception) {
                ctx.status(400).json(Map.of("error", "Invalid JSON request body."));
                return;
            }
            if (request == null) {
                ctx.status(400).json(Map.of("error", "A price request is required."));
                return;
            }

            try {
                PriceObservation observation = priceService.createObservation(
                    request.productId(),
                    request.sellerId(),
                    request.price(),
                    request.recordedAt()
                );
                saveData(dataStore, productService, sellerService, priceService);
                ctx.status(201).json(toPriceResponse(observation));
            } catch (IllegalArgumentException exception) {
                ctx.status(400).json(Map.of("error", exception.getMessage()));
            }
        });
        app.get("/api/history/{productId}", ctx -> {
            String productId = ctx.pathParam("productId");
            if (productService.findById(productId).isEmpty()) {
                throw new ResourceNotFoundException("Product not found.");
            }
            ctx.json(priceService.getHistory(productId).stream()
                .map(Main::toPriceResponse)
                .toList());
        });
        app.get("/api/comparison/{productId}", ctx -> {
            String productId = ctx.pathParam("productId");
            if (productService.findById(productId).isEmpty()) {
                throw new ResourceNotFoundException("Product not found.");
            }
            List<PriceObservation> history = priceService.getHistory(productId);
            if (history.size() < 2) {
                ctx.status(400).json(Map.of(
                    "error", "At least two price observations are required to compare."));
                return;
            }

            PriceObservation latest = history.get(0);
            PriceObservation previous = history.get(1);
            BigDecimal difference = latest.getPrice().subtract(previous.getPrice());
            String direction = switch (difference.signum()) {
                case 1 -> "increased";
                case -1 -> "decreased";
                default -> "stayed the same";
            };
            PriceComparison comparison = new PriceDifferenceComparison();
            ctx.json(new ComparisonResponse(
                direction,
                previous.getPrice(),
                latest.getPrice(),
                difference,
                comparison.compare(previous, latest)
            ));
        });

        app.start(7070);
    }

    private static PriceResponse toPriceResponse(PriceObservation observation) {
        return new PriceResponse(
            observation.getId(),
            observation.getProductId(),
            observation.getSellerId(),
            observation.getSeller().getName(),
            observation.getPrice(),
            observation.getRecordedAt().toString()
        );
    }

    private static void saveData(
        MarketDataStore dataStore,
        ProductService productService,
        SellerService sellerService,
        PriceService priceService
    ) {
        dataStore.save(
            productService.getProducts(),
            sellerService.getSellers(),
            priceService.getObservations()
        );
    }

    private record ProductRequest(String name, String category, String unit) {
    }

    private record SellerRequest(String name, String location) {
    }

    private record PriceRequest(
        String productId,
        String sellerId,
        BigDecimal price,
        String recordedAt
    ) {
    }

    private record PriceResponse(
        String id,
        String productId,
        String sellerId,
        String sellerName,
        BigDecimal price,
        String recordedAt
    ) {
    }

    private record ComparisonResponse(
        String direction,
        BigDecimal previousPrice,
        BigDecimal latestPrice,
        BigDecimal difference,
        String summary
    ) {
    }
}
