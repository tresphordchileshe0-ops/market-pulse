package marketpulse.service;

import marketpulse.model.Seller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SellerService {

    private final List<Seller> sellers = new ArrayList<>();

    public List<Seller> getSellers() {
        return new ArrayList<>(sellers);
    }

    public Seller createSeller(String name, String location) {
        String id = UUID.randomUUID().toString();
        Seller seller = new Seller(id, name, location);
        sellers.add(seller);
        return seller;
    }
}