package ru.yarsu.domain

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.util.UUID

class ProductStore(
    products: List<Product>,
) {
    private val products = products.associateBy(Product::id)

    companion object {
        fun fromCsv(): ProductStore {
            val rows = csvReader().readAllWithHeader(File("data/products.csv"))
            val products = rows.map { row ->
                Product(UUID.fromString(row["id"]), row["name"].orEmpty())
            }
            return ProductStore(products)
        }
    }

    fun list(): List<Product> = products.values.toList()

    fun fetch(id: UUID): Product? = products[id]
}
