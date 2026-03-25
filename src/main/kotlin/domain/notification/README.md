# Notification Module

## 의존성 규칙

알림 모듈은 단방향 의존성 규칙을 따른다.

- 알림 모듈은 어떠한 기능 모듈에도 의존하지 않는다.
- 다른 기능 모듈은 알림 모듈을 자유롭게 의존할 수 있다.
```
Friend   ──→ Notification ✅
Keyword  ──→ Notification ✅
User     ──→ Notification ✅

Notification ──→ Friend   ❌
Notification ──→ Keyword  ❌
Notification ──→ User     ❌
```

## 알림 유형

| 유형 | 설명 | 발송 주체 | 브로드캐스트 |
|---|---|---|---|
| `FRIEND_REQUEST` | 친구 요청 | 시스템 자동 | ❌ |
| `FRIEND_ACCEPT` | 친구 수락 | 시스템 자동 | ❌ |
| `NEW_KEYWORD` | 친구의 새 키워드 | 시스템 자동 | ❌ |
| `NOTICE` | 공지사항 | 관리자 수동 | ✅ |
| `EVENT` | 이벤트 | 관리자 수동 | ✅ |

## 딥링크

| 알림 유형 | 이동 화면 | URI                                                |
|---|---|----------------------------------------------------|
| `FRIEND_REQUEST`, `FRIEND_ACCEPT` | 프로필 화면 | `SCHEME://profile/{userId}`                        |
| `NEW_KEYWORD` | 키워드 상세 화면 | `SCHEME://keyword_detail/{userKeywordId}/{userId}` |
| `NOTICE`, `EVENT` | 알림 목록 화면 | `SCHEME://notifications`                           |