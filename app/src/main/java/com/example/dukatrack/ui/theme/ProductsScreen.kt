package com.example.dukatrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dukatrack.data.Product

@Composable
fun ProductsScreen(vm: ShopViewModel) {
    val products by vm.products.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Product?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Product?>(null) }

    Box(Modifier.fillMaxSize()) {
        if (products.isEmpty()) {
            Text("No products yet. Tap + to add one.", Modifier.align(Alignment.Center))
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { p ->
                ProductCard(
                    p,
                    onEdit = { editing = p; showDialog = true },
                    onDelete = { toDelete = p }
                )
            }
        }
        FloatingActionButton(
            onClick = { editing = null; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Add product") }
    }

    if (showDialog) {
        ProductDialog(
            product = editing,
            onDismiss = { showDialog = false },
            onSave = { vm.saveProduct(it); showDialog = false }
        )
    }

    toDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete ${p.name}?") },
            text = { Text("Past sales will still appear in your reports.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteProduct(p); toDelete = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProductCard(p: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = if (p.isLowStock)
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    else CardDefaults.cardColors()

    Card(Modifier.fillMaxWidth(), colors = colors) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(p.name, fontWeight = FontWeight.Bold)
                Text(p.category, style = MaterialTheme.typography.bodySmall)
                Text("Buy ${money(p.buyPrice)}  •  Sell ${money(p.sellPrice)}")
                Text(
                    if (p.isLowStock) "LOW STOCK: ${p.stockQty} left" else "${p.stockQty} in stock",
                    fontWeight = if (p.isLowStock) FontWeight.Bold else FontWeight.Normal
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") }
        }
    }
}

@Composable
private fun ProductDialog(product: Product?, onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var buy by remember { mutableStateOf(product?.buyPrice?.toString() ?: "") }
    var sell by remember { mutableStateOf(product?.sellPrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stockQty?.toString() ?: "") }
    var low by remember { mutableStateOf((product?.lowStockLevel ?: 5).toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add product" else "Edit product") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Field("Name", name, { name = it })
                Field("Category", category, { category = it })
                Field("Buying price", buy, { buy = it }, KeyboardType.Decimal)
                Field("Selling price", sell, { sell = it }, KeyboardType.Decimal)
                Field("Stock quantity", stock, { stock = it }, KeyboardType.Number)
                Field("Low-stock alert level", low, { low = it }, KeyboardType.Number)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val b = buy.toDoubleOrNull()
                val s = sell.toDoubleOrNull()
                val q = stock.toIntOrNull()
                val l = low.toIntOrNull()
                if (name.isBlank()) { error = "Name is required"; return@TextButton }
                if (b == null || s == null || q == null || l == null || b < 0 || s < 0 || q < 0 || l < 0) {
                    error = "Enter valid, non-negative numbers"
                    return@TextButton
                }
                onSave(
                    Product(
                        id = product?.id ?: 0,
                        name = name.trim(),
                        category = category.trim().ifEmpty { "General" },
                        buyPrice = b,
                        sellPrice = s,
                        stockQty = q,
                        lowStockLevel = l
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth()
    )
}
