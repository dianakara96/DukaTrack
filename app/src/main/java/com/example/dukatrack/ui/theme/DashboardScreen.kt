package com.example.dukatrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardScreen(vm: ShopViewModel) {
    val d by vm.dashboard.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatCard(
                "Today",
                listOf(
                    "Sales" to money(d.todayTotal),
                    "Profit" to money(d.todayProfit),
                    "Transactions" to d.todayCount.toString()
                )
            )
        }
        item {
            StatCard(
                "Last 7 days",
                listOf("Sales" to money(d.weekTotal), "Profit" to money(d.weekProfit))
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Top sellers (7 days)", style = MaterialTheme.typography.titleMedium)
                    if (d.topItems.isEmpty()) Text("No sales yet")
                    val max = (d.topItems.firstOrNull()?.qty ?: 1).toFloat()
                    d.topItems.forEach {
                        Text("${it.name} — ${it.qty} sold", Modifier.padding(top = 8.dp))
                        LinearProgressIndicator(
                            progress = { it.qty / max },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Low stock", style = MaterialTheme.typography.titleMedium)
                    if (d.lowStock.isEmpty()) Text("All items are well stocked")
                    d.lowStock.forEach {
                        Text("${it.name}: ${it.stockQty} left", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, rows: List<Pair<String, String>>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            rows.forEach { (label, value) -> Text("$label: $value", Modifier.padding(top = 4.dp)) }
        }
    }
}
