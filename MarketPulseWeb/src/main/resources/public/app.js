"use strict";

const productForm = document.getElementById("product-form");
const productName = document.getElementById("product-name");
const productCategory = document.getElementById("product-category");
const productUnit = document.getElementById("product-unit");

const productsBody = document.getElementById("products-table-body");
const productsMessage = document.getElementById("products-message");
const statusMessage = document.getElementById("status-message");
const saveButton = productForm.querySelector('button[type="submit"]');

function showStatus(message) {
    statusMessage.textContent = message;
}

async function request(path, options = {}) {
    const response = await fetch(path, options);

    if (!response.ok) {
        throw new Error(`Server returned HTTP ${response.status}.`);
    }

    return response;
}

function displayProducts(products) {
    productsBody.replaceChildren();

    for (const product of products) {
        const row = document.createElement("tr");

        for (const value of [
            product.name,
            product.category,
            product.unit
        ]) {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        }

        productsBody.appendChild(row);
    }

    productsMessage.textContent = products.length === 0
        ? "No products recorded yet."
        : `${products.length} product(s) loaded.`;
}

async function loadProducts() {
    productsMessage.textContent = "Loading products...";

    const response = await request("/api/products");
    const products = await response.json();

    if (
        !Array.isArray(products) ||
        !products.every(product =>
            product !== null &&
            typeof product === "object" &&
            typeof product.name === "string" &&
            typeof product.category === "string" &&
            typeof product.unit === "string"
        )
    ) {
        throw new Error("The product response has an unexpected format.");
    }

    displayProducts(products);
}

productForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const product = {
        name: productName.value.trim(),
        category: productCategory.value.trim(),
        unit: productUnit.value.trim()
    };

    if (!product.name || !product.category || !product.unit) {
        showStatus("Please complete every product field.");
        return;
    }

    saveButton.disabled = true;
    saveButton.textContent = "Saving...";
    showStatus("Saving product...");

    try {
        await request("/api/products", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(product)
        });
    } catch (error) {
        showStatus(
            `Could not confirm the save. ${error.message} ` +
            "Check the backend records before retrying."
        );

        saveButton.disabled = false;
        saveButton.textContent = "Save product";
        return;
    }

    productForm.reset();

    try {
        await loadProducts();
        showStatus("Product saved successfully.");
    } catch (error) {
        showStatus(
            `Product saved, but the list could not refresh. ${error.message}`
        );
        productsMessage.textContent =
            "Reload the page to try loading products again.";
    } finally {
        saveButton.disabled = false;
        saveButton.textContent = "Save product";
    }
});

async function initialisePage() {
    if (window.location.protocol === "file:") {
        showStatus(
            "This is a visual preview. Open the page through the " +
            "Javalin server to load and save products."
        );

        productsMessage.textContent = "Backend connection required.";
        return;
    }

    saveButton.disabled = false;
    saveButton.textContent = "Save product";

    try {
        await loadProducts();
        showStatus("Products loaded from the backend.");
    } catch (error) {
        showStatus(
            `Could not load products. ${error.message} ` +
            "Check that the backend implements GET /api/products."
        );

        productsMessage.textContent = "Products could not be loaded.";
    }
}

initialisePage();