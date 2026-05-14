package com.turnin.domain.discover.util

import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.domain.discover.util.TestVectorFixture.toPgVectorString
import com.turnin.util.db.TestDatabaseFactory
import java.time.Instant
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insertAndGetId

/**
 * '탐색' 테스트 데이터 생성기
 */
object DiscoverTestDataGenerator {
    /**
     * 사용자, 키워드, 사용자 키워드 데이터를 생성해서 DB에 삽입한다.
     *
     * 사용자ID 및 키워드ID는 1부터 순서대로 개수만큼 부여된다.
     *
     * @param userCount 생성할 사용자 수
     * @param keywordsWithVectors 생성할 키워드 개수만큼 (키워드 명, 벡터 값) 입력
     * @param userKeywordRelation 사용자 키워드 관계 지정 (사용자 ID to 키워드 ID)
     */
    suspend fun setupKeywordRelations(
        userCount: Int,
        keywordsWithVectors: List<Pair<String, FloatArray>>,
        userKeywordRelation: Map<Long, List<Long>>,
    ) {
        // 1. 사용자 생성
        (1..userCount).chunked(100).forEach { chunk ->
            TestDatabaseFactory.dbQuery {
                Users.batchInsert(chunk) { i ->
                    this[Users.role] = Role.USER
                    this[Users.provider] = SocialLoginProvider.KAKAO
                    this[Users.providerId] = "pid$i"
                    this[Users.displayId] = "display$i"
                    this[Users.name] = "사용자$i"
                    this[Users.introduce] = "introduce$i"
                    this[Users.lastLoginAt] = Instant.now()
                }
            }
        }

        // 2. 키워드 생성
        TestDatabaseFactory.dbQuery {
            keywordsWithVectors.map { (name, vec) ->
                Keywords.insertAndGetId {
                    it[keyword] = name
                    it[embedding] = vec.toPgVectorString()
                    it[createdBy] = EntityID(1L, Users)
                }
            }
        }

        // 3. 관계 생성 (수정된 로직)
        val ukRelation = userKeywordRelation.flatMap { (userId, keywordIds) ->
            keywordIds.map { userId to it }
        }
        TestDatabaseFactory.dbQuery {
            UserKeywords.batchInsert(ukRelation) { (uid, kid) ->
                this[UserKeywords.userId] = uid
                this[UserKeywords.keywordId] = kid
            }
        }
    }
}
