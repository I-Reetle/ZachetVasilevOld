package ru.yarsu.domain

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

class ProductMovementStore(
    movementLog: List<ProductMovement>,
) {
    private val movementLog = movementLog.associateBy(ProductMovement::id)

    companion object {
        fun fromCsv(): ProductMovementStore {
            val rows = csvReader().readAllWithHeader(File("data/movement.csv"))
            val movements = rows.map { row ->
                ProductMovement(
                    UUID.fromString(row["id"]),
                    LocalDateTime.parse(row["moved_at"]),
                    UUID.fromString(row["product_id"]),
                    MovementType.valueOf(row["type"].orEmpty()),
                    row["amount"]?.toDoubleOrNull() ?: 0.0,
                )
            }
            return ProductMovementStore(movements)
        }
    }

    fun list(): List<ProductMovement> = movementLog.values.toList()

    fun fetch(id: UUID): ProductMovement? = movementLog[id]
}
