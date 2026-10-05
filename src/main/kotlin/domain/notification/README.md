# Notification Module

## 의존성 규칙

알림 모듈은 단방향 의존성 규칙을 따른다.

- 알림 모듈은 어떠한 기능 모듈에도 의존하지 않는다.
- 다른 기능 모듈은 알림 모듈을 자유롭게 의존할 수 있다.
```
Friend   ──→ Notification ✅
Keyword  ──→ Notification ✅
User     ──→ Notification ✅
PingPong ──→ Notification ✅

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
| `PING_PONG_QUESTION` | 내 게시물에 핑퐁 질문 등록 | 시스템 자동 | ❌ |
| `PING_PONG_ANSWER` | 내 핑퐁 질문에 답변 등록 | 시스템 자동 | ❌ |
| `NOTICE` | 공지사항 | 관리자 수동 | ✅ |
| `EVENT` | 이벤트 | 관리자 수동 | ✅ |

## 딥링크

| 알림 유형 | 이동 화면 | URI                                                |
|---|---|----------------------------------------------------|
| `FRIEND_REQUEST`, `FRIEND_ACCEPT` | 프로필 화면 | `SCHEME://profile/{userId}`                        |
| `NEW_KEYWORD`, `PING_PONG_QUESTION`, `PING_PONG_ANSWER` | 키워드 상세 화면 | `SCHEME://keyword_detail/{userKeywordId}/{userId}` |
| `NOTICE`, `EVENT` | 알림 목록 화면 | `SCHEME://notifications`                           |

### 딥링크 데이터 구성

> 클라이언트와의 계약은 **알림 목록 조회 API 문서**(`NotificationRoute.getNotificationsDocs`)가 기준이다. 아래 표를 수정할 때 API 문서도 함께 수정한다.

- 이동할 화면의 핵심 ID는 `refType` + `refId`에 담는다.
- 그 외 화면 이동에 필요한 부가 값은 `refData`(key-value 맵)에 담는다. (FCM data 키와 동일)
- `refData`는 FCM data와 알림 내역(`notification.ref_data`)에 함께 저장되므로, 푸시와 알림 목록에서 같은 값으로 딥링크를 구성할 수 있다.
- 알림의 `userId`는 **수신자** ID이므로 딥링크 파라미터로 사용하지 않는다.

| 알림 유형 | refType | refId | refData |
|---|---|---|---|
| `FRIEND_REQUEST`, `FRIEND_ACCEPT` | `USER` | 상대 사용자 ID | - |
| `NEW_KEYWORD` (FCM만) | `KEYWORD` | 사용자 키워드 ID | `ref_owner_id`: 게시물 작성자 ID, `user_id`: 동일 값 (레거시) |
| `PING_PONG_QUESTION`, `PING_PONG_ANSWER` | `KEYWORD` | 사용자 키워드 ID | `ref_owner_id`: 게시물 작성자 ID |

### 레거시 키

| 키 | 대체 키 | 유지 이유 | 제거 조건 |
|---|---|---|---|
| `user_id` (`NEW_KEYWORD`) | `ref_owner_id` | 구버전 앱이 `user_id`로 딥링크를 처리함 | 최소 지원 앱 버전이 `ref_owner_id`를 읽는 버전 이상으로 올라간 후 |
