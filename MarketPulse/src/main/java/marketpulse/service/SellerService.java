package marketpulse.service;

import marketpulse.model.Seller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SellerService {

    private final List<Seller> sellers = new ArrayList<>();

    public SellerService(List<Seller> sellers) {
        this.sellers.addAll(sellers);
    }

    public synchronized List<Seller> getSellers() {
        return List.copyOf(sellers);
    }

    public synchronized Optional<Seller> findById(String id) {
        return sellers.stream().filter(seller -> seller.getId().equals(id)).findFirst();
    }

    public synchronized Seller createSeller(String name, String location) {
        String id = UUID.randomUUID().toString();
        Seller seller = new Seller(id, name, location);
        sellers.add(seller);
        return seller;
    }
}