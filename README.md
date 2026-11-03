# MarketPulse

A Java project for manually recording essential-goods prices from
Zambian sellers, comparing recorded prices, and viewing price history.

## Project status

Under development. The Javalin backend and web interface are not
complete yet.

## Current model

- MarketRecord: abstract parent holding a record ID.
- Product: describes a product and its package unit.

## Planned additions

- Seller
- PriceObservation
- Comparison strategies
- PriceTrackerService
- File persistence
- Javalin REST endpoints
- HTML and JavaScript front end