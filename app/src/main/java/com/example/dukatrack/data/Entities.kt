package com.example.dukatrack.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val stockQty: Int,
    val lowStockLevel: Int = 5
) {
    val isLowStock: Boolean get() = stockQty <= lowStockLevel
}

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,      // epoch millis
    val total: Double,
    val profit: Double
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String, // kept so reports survive product deletion
    val quantity: Int,
    val price: Double
)

/** Result row for the "top sellers" query. */
data class TopItem(val name: String, val qty: Int)

/** One line in the cart (not stored in the database). */
data class CartLine(val product: Product, val qty: Int)
