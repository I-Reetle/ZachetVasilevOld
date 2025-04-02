package ru.yarsu.domain

import java.time.LocalDateTime
import java.util.UUID

data class ProductMovement(
    val id: UUID,
    val movedAt: LocalDateTime,
    val productId: UUID,
    val type: MovementType,
    val amount: Double,
)
