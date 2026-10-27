package com.example.dukatrack.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Product>>

    @Insert
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    /** Returns the number of rows changed: 0 means there was not enough stock. */
    @Query("UPDATE products SET stockQty = stockQty - :qty WHERE id = :id AND stockQty >= :qty")
    suspend fun reduceStock(id: Long, qty: Int): Int
}

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertItems(items: List<SaleItem>)

    @Query("SELECT * FROM sales WHERE date >= :from ORDER BY date DESC")
    fun observeSince(from: Long): Flow<List<Sale>>

    @Query(
        """SELECT si.productName AS name, SUM(si.quantity) AS qty
           FROM sale_items si JOIN sales s ON s.id = si.saleId
           WHERE s.date >= :from
           GROUP BY si.productName
           ORDER BY qty DESC LIMIT 5"""
    )
    fun observeTopItems(from: Long): Flow<List<TopItem>>
}
