-- 알림 딥링크 부가 데이터 (FCM data와 동일한 key-value 맵을 JSON 문자열로 저장)
-- 예: {"ref_owner_id":"34"} (refType/refId로 표현할 수 없는 딥링크 화면 파라미터)
ALTER TABLE notification ADD COLUMN ref_data TEXT;
