package com.example.dukatrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukatrack.data.AppDatabase
import com.example.dukatrack.data.CartLine
import com.example.dukatrack.data.Product
import com.example.dukatrack.data.ShopRepository
import com.example.dukatrack.data.TopItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardState(
    val todayTotal: Double = 0.0,
    val todayProfit: Double = 0.0,
    val todayCount: Int = 0,
    val weekTotal: Double = 0.0,
    val weekProfit: Double = 0.0,
    val topItems: List<TopItem> = emptyList(),
    val lowStock: List<Product> = emptyList()
)

class ShopViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ShopRepository(AppDatabase.get(app))

    val products: StateFlow<List<Product>> = repo.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ----- Cart -----
    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun addToCart(p: Product) = _cart.update { list ->
        val line = list.find { it.product.id == p.id }
        when {
            line == null -> if (p.stockQty > 0) list + CartLine(p, 1) else list
            line.qty < p.stockQty ->
                list.map { if (it.product.id == p.id) it.copy(qty = it.qty + 1) else it }
            else -> list // cannot sell more than we have
        }
    }

    fun decrease(p: Product) = _cart.update { list ->
        list.mapNotNull {
            when {
                it.product.id != p.id -> it
                it.qty > 1 -> it.copy(qty = it.qty - 1)
                else -> null
            }
        }
    }

    fun checkout() {
        val lines = _cart.value
        if (lines.isEmpty()) return
        viewModelScope.launch {
            repo.recordSale(lines)
                .onSuccess {
                    _cart.value = emptyList()
                    _message.value = "Sale recorded"
                }
                .onFailure { _message.value = it.message ?: "Could not record sale" }
        }
    }

    fun consumeMessage() { _message.value = null }

    // ----- Products -----
    fun saveProduct(p: Product) = viewModelScope.launch { repo.saveProduct(p) }
    fun deleteProduct(p: Product) = viewModelScope.launch { repo.deleteProduct(p) }

    // ----- Dashboard -----
    private val startOfToday = startOfDay(0)
    private val startOfWeek = startOfDay(6) // today + previous 6 days

    val dashboard: StateFlow<DashboardState> = combine(
        repo.salesSince(startOfToday),
        repo.salesSince(startOfWeek),
        repo.topItemsSince(startOfWeek),
        repo.products
    ) { today, week, top, products ->
        DashboardState(
            todayTotal = today.sumOf { it.total },
            todayProfit = today.sumOf { it.profit },
            todayCount = today.size,
            weekTotal = week.sumOf { it.total },
            weekProfit = week.sumOf { it.profit },
            topItems = top,
            lowStock = products.filter { it.isLowStock }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    private fun startOfDay(daysAgo: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -daysAgo)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
