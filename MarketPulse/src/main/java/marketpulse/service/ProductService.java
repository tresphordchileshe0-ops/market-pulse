package marketpulse.service;

import marketpulse.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public ProductService(List<Product> products) {
        this.products.addAll(products);
    }

    public synchronized List<Product> getProducts() {
        return List.copyOf(products);
    }

    public synchronized Optional<Product> findById(String id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst();
    }

    public synchronized Product createProduct(String name, String category, String unit) {
        Product product = new Product(UUID.randomUUID().toString(), name, category, unit);
        products.add(product);
        return product;
    }

    public synchronized boolean deleteProduct(String id) {
        return products.removeIf(product -> product.getId().equals(id));
    }
}
