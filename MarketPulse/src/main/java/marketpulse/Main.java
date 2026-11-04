package marketpulse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import marketpulse.model.Product;
import marketpulse.service.ProductService;

import java.util.Map;

public class Main {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        ProductService productService = new ProductService();

        Javalin.create(config -> config.staticFiles.add("/public"))
            .get("/api/products", ctx -> ctx.json(productService.getProducts()))
            .post("/api/products", ctx -> {
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
                        request.name(),
                        request.category(),
                        request.unit()
                    );
                    ctx.status(201).json(product);
                } catch (IllegalArgumentException exception) {
                    ctx.status(400).json(Map.of("error", exception.getMessage()));
                }
            })
            .start(7070);
    }

    private record ProductRequest(String name, String category, String unit) {
    }
}