# MarketPulse

MarketPulse is a Java project for tracking prices of everyday goods in the market. The goal is to help compare product prices across sellers and keep a record of price history over time.

## Current status

This project is still in the early development phase. The core Java model layer exists, and the Maven build is configured, but the app does not yet include the full persistence, comparison logic, or web interface described in the original project plan.

## Implemented so far

The project currently contains:

- `marketpulse.Main`: the main application entry point
- `marketpulse.model.MarketRecord`: an abstract base class with validation for record IDs
- `marketpulse.model.Product`: a concrete model that stores a product name, category, and unit

## Project structure

```text
Market Pulse/
├── .gitignore
├── README.md
├── MarketPulse/
│   ├── pom.xml
│   └── src/
│       └── main/
│           └── java/
│               └── marketpulse/
│                   ├── Main.java
│                   └── model/
│                       ├── MarketRecord.java
│                       └── Product.java
```

## Technology stack

- Java 17
- Maven
- Git and GitHub

## Prerequisites

Before running the project, make sure you have:

- Java 17 or newer installed
- Maven installed and available in your PATH

## Running the project

From the repository root:

```powershell
cd "Market Pulse"
cd MarketPulse
mvn compile
java -cp target/classes marketpulse.Main
```

This currently starts the Java entry point and prints the application startup message from `Main`.

## Planned features

- Record products and sellers
- Save price observations over time
- Compare prices for the same product across sellers
- View price history for a product at a seller
- Persist records between application runs
- Add a web or desktop interface later in the project

## Notes

The project is intentionally focused on the domain model at this stage. More features and storage logic will be added as the application evolves.

## Contributing

Contributions are welcome. The project is expected to be developed collaboratively using GitHub branches and pull requests.
