"use strict";

const statusMessage = document.getElementById("status-message");
const productsMessage = document.getElementById("products-message");
const productsBody = document.getElementById("products-table-body");
const sellersBody = document.getElementById("sellers-body");
const historyBody = document.getElementById("history-body");
const historyMessage = document.getElementById("history-message");
const comparisonResult = document.getElementById("comparison-result");
const productSelectors = [
    document.getElementById("price-product"),
    document.getElementById("history-product"),
    document.getElementById("comparison-product")
];
let products = [];
let sellers = [];

function showStatus(message) {
    statusMessage.textContent = message;
}

async function request(path, options = {}) {
    const response = await fetch(path, options);
    if (!response.ok) {
        let message = `Server returned HTTP ${response.status}.`;
        if ((response.headers.get("content-type") || "").includes("application/json")) {
            const body = await response.json();
            if (body && typeof body.error === "string") {
                message = body.error;
            }
        }
        throw new Error(message);
    }
    return response;
}

function addCell(row, value) {
    const cell = document.createElement("td");
    cell.textContent = value;
    row.appendChild(cell);
}

function fillProductSelectors() {
    for (const select of productSelectors) {
        const selected = select.value;
        select.replaceChildren();
        for (const product of products) {
            const option = document.createElement("option");
            option.value = product.id;
            option.textContent = `${product.name} (${product.unit})`;
            select.appendChild(option);
        }
        if (products.some(product => product.id === selected)) {
            select.value = selected;
        }
    }
}

async function loadProducts() {
    const response = await request("/api/products");
    products = await response.json();
    productsBody.replaceChildren();

    for (const product of products) {
        const row = document.createElement("tr");
        addCell(row, product.name);
        addCell(row, product.category);
        addCell(row, product.unit);

        const action = document.createElement("td");
        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.textContent = "Delete";
        deleteButton.addEventListener("click", async () => {
            try {
                await request(`/api/products/${encodeURIComponent(product.id)}`, {
                    method: "DELETE"
                });
                await loadProducts();
                await loadHistory();
                showStatus("Product deleted.");
            } catch (error) {
                showStatus(`Could not delete product. ${error.message}`);
            }
        });
        action.appendChild(deleteButton);
        row.appendChild(action);
        productsBody.appendChild(row);
    }

    productsMessage.textContent = products.length === 0
        ? "No products recorded yet."
        : `${products.length} product(s) loaded.`;
    fillProductSelectors();
}

async function loadSellers() {
    const response = await request("/api/sellers");
    sellers = await response.json();
    sellersBody.replaceChildren();

    for (const seller of sellers) {
        const row = document.createElement("tr");
        addCell(row, seller.name);
        addCell(row, seller.location);
        sellersBody.appendChild(row);
    }
}

async function loadHistory() {
    historyBody.replaceChildren();
    const productId = document.getElementById("history-product").value;
    if (!productId) {
        historyMessage.textContent = "Add a product to view its history.";
        return;
    }

    const response = await request(`/api/history/${encodeURIComponent(productId)}`);
    const history = await response.json();
    for (const observation of history) {
        const row = document.createElement("tr");
        addCell(row, observation.recordedAt);
        addCell(row, observation.sellerName);
        addCell(row, observation.price);
        historyBody.appendChild(row);
    }
    historyMessage.textContent = history.length === 0
        ? "No prices recorded for this product yet."
        : `${history.length} observation(s), newest first.`;
}

document.getElementById("product-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
        await request("/api/products", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name: data.get("name"),
                category: data.get("category"),
                unit: data.get("unit")
            })
        });
        form.reset();
        await loadProducts();
        showStatus("Product saved.");
    } catch (error) {
        showStatus(`Could not save product. ${error.message}`);
    }
});

document.getElementById("seller-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
        await request("/api/sellers", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name: data.get("name"),
                location: data.get("location")
            })
        });
        form.reset();
        await loadSellers();
        showStatus("Seller saved.");
    } catch (error) {
        showStatus(`Could not save seller. ${error.message}`);
    }
});

document.getElementById("price-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
        await request("/api/prices", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                productId: data.get("productId"),
                sellerId: data.get("sellerId"),
                price: data.get("price"),
                recordedAt: data.get("recordedAt")
            })
        });
        await loadHistory();
        showStatus("Price recorded.");
    } catch (error) {
        showStatus(`Could not record price. ${error.message}`);
    }
});

document.getElementById("compare-button").addEventListener("click", async () => {
    const productId = document.getElementById("comparison-product").value;
    if (!productId) {
        comparisonResult.textContent = "Add a product before comparing prices.";
        return;
    }
    try {
        const response = await request(
            `/api/comparison/${encodeURIComponent(productId)}`);
        const comparison = await response.json();
        comparisonResult.textContent =
            `Price ${comparison.direction}: ${comparison.previousPrice} → ` +
            `${comparison.latestPrice} ZMW (difference ${comparison.difference} ZMW).`;
    } catch (error) {
        comparisonResult.textContent = error.message;
    }
});

document.getElementById("history-product").addEventListener("change", async () => {
    try {
        await loadHistory();
    } catch (error) {
        historyMessage.textContent = `Could not load history. ${error.message}`;
    }
});

document.getElementById("refresh-button").addEventListener("click", async () => {
    try {
        await Promise.all([loadProducts(), loadSellers()]);
        await loadHistory();
        showStatus("Products and sellers refreshed.");
    } catch (error) {
        showStatus(`Could not refresh records. ${error.message}`);
    }
});

async function initialisePage() {
    if (window.location.protocol === "file:") {
        showStatus("Open this page through the Javalin server to use the API.");
        return;
    }

    const date = new Date();
    const localToday = [
        date.getFullYear(),
        String(date.getMonth() + 1).padStart(2, "0"),
        String(date.getDate()).padStart(2, "0")
    ].join("-");
    document.getElementById("price-date").value = localToday;

    try {
        await Promise.all([loadProducts(), loadSellers()]);
        await loadHistory();
        showStatus("MarketPulse records loaded.");
    } catch (error) {
        showStatus(`Could not load records. ${error.message}`);
    }
}

initialisePage();
