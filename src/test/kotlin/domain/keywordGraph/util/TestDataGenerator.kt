package com.peekr.domain.keywordGraph.util

import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.util.TestDatabaseFactory
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insertAndGetId

/**
 * 키워드 그래프 테스트 데이터 제너레이터
 */
object TestDataGenerator {
    /**
     * 사용자ID 및 키워드ID는 1부터 순서대로 개수만큼 부여된다.
     *
     * @param userCount 생성할 사용자 수
     * @param keywordCount 생성할 키워드 수
     * @param userKeywordRelation 사용자 키워드 관계 지정 (예: key(1L), value(listOf(1L, 2L, 3L)))
     */
    suspend fun generate(
        userCount: Int = 26,
        keywordCount: Int = 100,
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
                }
            }
        }

        // 2. 키워드 생성
        TestDatabaseFactory.dbQuery {
            (1..keywordCount).map { i ->
                Keywords
                    .insertAndGetId {
                        it[Keywords.keyword] = "Keyword$i"
                        it[Keywords.createdBy] = EntityID(1L, Users)
                    }.value
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
