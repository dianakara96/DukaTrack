package com.example.dukatrack.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val saleDao = db.saleDao()

    val products: Flow<List<Product>> = productDao.observeAll()

    suspend fun saveProduct(p: Product) {
        if (p.id == 0L) productDao.insert(p) else productDao.update(p)
    }

    suspend fun deleteProduct(p: Product) = productDao.delete(p)

    fun salesSince(from: Long): Flow<List<Sale>> = saleDao.observeSince(from)
    fun topItemsSince(from: Long): Flow<List<TopItem>> = saleDao.observeTopItems(from)

    /**
     * Saves the sale and reduces stock in ONE transaction.
     * If any product has too little stock, everything is rolled back.
     */
    suspend fun recordSale(cart: List<CartLine>): Result<Unit> = try {
        db.withTransaction {
            val total = cart.sumOf { it.product.sellPrice * it.qty }
            val profit = cart.sumOf { (it.product.sellPrice - it.product.buyPrice) * it.qty }
            val saleId = saleDao.insertSale(
                Sale(date = System.currentTimeMillis(), total = total, profit = profit)
            )
            cart.forEach { line ->
                val changed = productDao.reduceStock(line.product.id, line.qty)
                if (changed == 0) {
                    throw IllegalStateException("Not enough stock for ${line.product.name}")
                }
            }
            saleDao.insertItems(
                cart.map {
                    SaleItem(
                        saleId = saleId,
                        productId = it.product.id,
                        productName = it.product.name,
                        quantity = it.qty,
                        price = it.product.sellPrice
                    )
                }
            )
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
