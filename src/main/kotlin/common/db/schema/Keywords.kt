package com.turnin.common.db.schema

import com.turnin.common.db.BaseEntity
import com.turnin.common.db.BaseEntityClass
import com.turnin.common.db.BaseLongIdTable
import com.turnin.common.db.DatabaseUtils.customPostgresEnum
import com.turnin.common.db.VectorColumnType
import com.turnin.common.ml.keywordCategory.KeywordCategory
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 키워드 엔티티 클래스 (Exposed DSL 방식) */
object Keywords : BaseLongIdTable("keyword") {
    val keyword = varchar("keyword", 100).uniqueIndex()
    val embedding = registerColumn("embedding", VectorColumnType(768))
    val category = customPostgresEnum<KeywordCategory>("category", "keyword_category").nullable()
    val categorySimilarity = double("category_similarity").nullable()
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.SET_NULL).nullable()
}

/** 키워드 엔티티 클래스 (Exposed DAO/ORM 방식) */
class KeywordEntity(id: EntityID<Long>) : BaseEntity(id, Keywords) {
    companion object : BaseEntityClass<KeywordEntity>(Keywords)

    var keyword by Keywords.keyword
    var embedding by Keywords.embedding
    var category by Keywords.category
    var categorySimilarity by Keywords.categorySimilarity
    var createdBy by Keywords.createdBy
}
