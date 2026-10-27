package com.example.dukatrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SellScreen(vm: ShopViewModel) {
    val products by vm.products.collectAsStateWithLifecycle()
    val cart by vm.cart.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val total = cart.sumOf { it.product.sellPrice * it.qty }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search products") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            Modifier.weight(1f).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(products.filter { it.name.contains(query, ignoreCase = true) }, key = { it.id }) { p ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.Bold)
                        Text("${money(p.sellPrice)}  •  ${p.stockQty} in stock")
                    }
                    Button(onClick = { vm.addToCart(p) }, enabled = p.stockQty > 0) { Text("Add") }
                }
                HorizontalDivider()
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Cart", style = MaterialTheme.typography.titleMedium)
        if (cart.isEmpty()) Text("Nothing added yet")

        LazyColumn(Modifier.heightIn(max = 180.dp)) {
            items(cart, key = { it.product.id }) { line ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(line.product.name, Modifier.weight(1f))
                    TextButton(onClick = { vm.decrease(line.product) }) { Text("−") }
                    Text("${line.qty}")
                    TextButton(onClick = { vm.addToCart(line.product) }) { Text("+") }
                    Text(money(line.product.sellPrice * line.qty))
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total: ${money(total)}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Button(onClick = { vm.checkout() }, enabled = cart.isNotEmpty()) { Text("Complete sale") }
        }
    }
}
