# peekr-server

## Project Structure (Layered)
```
project/
├── config/                  # 설정 관련 파일들
│
├── domain/                  # 핵심 도메인 계층
│   ├── model/               # 도메인 모델
│   ├── repository/          # 리포지토리 인터페이스
│   ├── service/             # 서비스 인터페이스
│   └── event/               # 이벤트 (optional)
│
├── application/             # 애플리케이션 계층
│   ├── dto/                 # 데이터 전송 객체
│   ├── usecase/             # 유스케이스 구현 (service와 유사)
│
├── presentation/            # 프레젠테이션 계층 (라우팅)
│
├── infrastructure/          # 인프라스트럭처 계층
│   ├── entity/              # DB 엔티티
│   ├── repository-impl/     # 리포지토리 구현체
│   ├── service-impl/        # 서비스 구현체
│   ├── mapper/              # 엔티티-도메인 모델 매퍼
│   └── external/            # 외부 API 클라이언트
│
├── di/                      # 의존성 주입 설정
```
## Dependency Direction
```mermaid
flowchart LR
    p(presentation) --> a(application)
    a --> d(domain)
    d --> i(infrastructure)
    di(di) --> p
    di(di) --> a
    di(di) --> d
    di(di) --> i
```