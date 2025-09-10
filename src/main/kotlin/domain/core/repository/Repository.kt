package com.peekr.domain.core.repository

interface Repository<Model : Any, ID : Any> {
    suspend fun findById(id: ID): Model?

    suspend fun findAll(): List<Model>

    suspend fun findAll(pagination: Pagination): PaginationResult<Model>

    suspend fun create(model: Model): Model

    suspend fun update(model: Model): Model

    suspend fun delete(id: ID): ID

    suspend fun count(pagination: Pagination? = null): Long
}
