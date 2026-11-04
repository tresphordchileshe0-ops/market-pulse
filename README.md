# MarketPulse

MarketPulse helps people record everyday product prices from local sellers, review price history, and see whether a product's price has increased or decreased.

## Run the application

From the repository root:

```text
cd MarketPulse
mvn exec:java
```

Open [http://localhost:7070/](http://localhost:7070/). Products, sellers, and price observations are saved in `MarketPulse/data/marketpulse-data.json` and loaded on startup.

## Main classes

- `Main`: Javalin application and REST endpoint setup
- `MarketRecord`: shared base class for `Product`, `Seller`, and `PriceObservation`
- `ProductService`, `SellerService`, `PriceService`: in-memory collections and domain operations
- `MarketDataStore`: Jackson JSON loading and saving
- `PriceComparison`, `PriceDifferenceComparison`, `PercentageChangeComparison`: comparison strategy types

## Main endpoints

- `GET /api/products`, `POST /api/products`, `DELETE /api/products/{productId}`
- `GET /api/sellers`, `POST /api/sellers`
- `POST /api/prices`
- `GET /api/history/{productId}`
- `GET /api/comparison/{productId}`

The application uses Java 17, Maven, and Javalin. Run it from `MarketPulse` with Maven installed and Java 17 or newer.
