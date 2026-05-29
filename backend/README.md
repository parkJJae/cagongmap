# 카공맵 Backend

> Spring Boot 기반 카페 방문 기록 API 서버

<br>

##개요

카공맵 백엔드는 **비회원 기반 카페 방문 기록 서비스**의 API 서버입니다.
일반 사용자는 닉네임만으로 즉시 카페를 기록하고, 운영자는 별도 관리자 페이지에서 신고된 글을 검토합니다.

- **일반 사용자** — 로그인 없음. 닉네임 기반 비회원 모델로 진입 장벽 최소화
- **운영자** — Spring Security + JWT(AT/RT)로 관리자 API 보호

<br>

##기술 스택

| 영역 | 기술 |
|---|---|
| Framework | Spring Boot 4 |
| Language | Java 17 |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security 7, JJWT 0.12.6 |
| DB | H2 (dev) / MySQL (prod) |
| Build | Gradle |
| 기타 | Lombok, Bean Validation, JPA Auditing |

<br>

## 아키텍처

<p align="center">
  <img src="https://github.com/user-attachments/assets/91a245ef-330a-4cfb-ad2d-9bfdb7e17579" alt="카공맵 시스템 아키텍처" width="780"/>
</p>

백엔드는 **Controller → Service → Repository**의 전형적인 3-Layer 구조를 따르되 다음 보조 컴포넌트로 책임을 명확히 분리했습니다.

- **`ApiResponse<T>`** — 모든 API 응답 포맷 통일
- **`GlobalExceptionHandler`** — `ResponseStatusException` 일괄 처리
- **`BaseEntity` + JPA Auditing** — `createdAt`, `updatedAt` 자동 관리
- **`SecurityFilterChain` + `JwtAuthenticationFilter`** — 관리자 API 보호 (stateless)

<br>

## 패키지 구조

```
src/main/java/com/mysite/cafe
├── domain/
│   ├── cafevisit/           # 카페 방문 기록 도메인
│   ├── user/                # 닉네임 기반 비회원 도메인
│   ├── admin/               # 관리자 계정 (Spring Security UserDetails)
│   ├── auth/                # 로그인 / 토큰 갱신 / 로그아웃
│   └── report/              # 카페 방문 기록 신고
└── global/
    ├── config/              # SecurityConfig, JpaConfig, JwtProperties
    ├── entity/              # BaseEntity (createdAt/updatedAt)
    ├── exception/           # GlobalExceptionHandler
    ├── jwt/                 # JwtTokenProvider, JwtAuthenticationFilter
    ├── response/            # ApiResponse 공통 응답
    └── util/                # IpAddressUtil 등
```

**도메인 기반 패키지 구조**를 채택했습니다. 초기에는 계층별로 분리했는데 도메인이 커질수록 한 도메인의 변경이 여러 폴더를 건드려야 해서 응집도가 떨어졌습니다. 도메인 단위로 묶으니 하나의 기능을 수정할 때 한 폴더만 보면 되어 인지 부하가 줄었습니다. 신고 기능과 관리자 인증을 추가하면서 이 구조의 효과가 더 분명해졌습니다.

<br>

## 리팩토링 스토리

처음 프로젝트는 컨트롤러에 모든 로직이 몰려 있는 형태였습니다. 동작은 하지만 유지보수가 어려운 상태였고, 다음과 같이 단계적으로 리팩토링했습니다.

### 1. 엔티티 — Setter 제거 + Builder 패턴

**Before**
```java
CafeVisit cafe = new CafeVisit();
cafe.setName(name);
cafe.setLat(lat);
cafe.setLng(lng);
```

**After**
```java
CafeVisit cafe = CafeVisit.builder()
    .name(name)
    .lat(lat)
    .lng(lng)
    .build();
```

Setter는 객체의 변경 시점을 추적하기 어렵게 만들고 불완전한 상태의 객체를 생성할 수 있습니다. **`@Builder`로 생성 시점에 모든 필드를 명시**하도록 강제하고, 와이파이 속도처럼 정해진 값은 `WifiSpeed` Enum으로 관리해 타입 안전성을 확보했습니다.

### 2. Service 레이어 도입

비즈니스 로직이 컨트롤러에 있으면 트랜잭션 경계가 모호해지고 컨트롤러가 HTTP 책임 외의 일을 떠안게 됩니다.
**Controller는 요청/응답 변환만, Service는 비즈니스 로직만** 담당하도록 분리했습니다.

### 3. 공통 응답 + 전역 예외 처리

API 응답 형태가 엔드포인트마다 달라지는 문제를 `ApiResponse<T>`로 통일하고, `GlobalExceptionHandler`에서 `ResponseStatusException`을 일괄 처리해 서비스 레이어에서는 비즈니스 흐름에만 집중할 수 있게 했습니다.

### 4. JPA Auditing — BaseEntity

`createdAt`, `updatedAt` 같은 메타 필드는 모든 엔티티가 가지지만 매번 수동으로 관리하면 누락되기 쉽습니다. **BaseEntity를 만들고 `@MappedSuperclass`로 상속**, JPA Auditing으로 자동 관리하도록 했습니다.

### 5. 환경별 DB 분리

- `application-dev.yml` → H2 인메모리 + 시드 데이터
- `application-prod.yml` → MySQL + 환경변수 주입 (`DB_*`, `JWT_SECRET`, `ADMIN_*`)

운영 DB 정보와 JWT 시크릿, 관리자 시드 자격증명은 코드에 박지 않고 배포 환경의 환경변수로만 주입되도록 분리했습니다.

<br>

## 인증/인가 (관리자 API)

일반 사용자는 로그인이 필요 없지만, **운영자가 신고 글을 검토하는 관리자 API는 보호되어야 합니다.** Spring Security + JWT로 이 영역을 구현했습니다.

- **Access Token** — JWT, 30분. self-contained라 매 요청마다 DB 조회 없이 검증
- **Refresh Token** — UUID 랜덤 문자열, 14일, DB 저장. 갱신 시마다 폐기·재발급(Rotation)
- **권한 통제** — `SecurityConfig` 한 곳에서 `/api/admin/**`을 `hasRole("ADMIN")`으로 일괄 처리

### 주요 의사결정

| 결정 | 근거 |
|---|---|
| AT 30분 + RT 14일 | 짧은 토큰의 안전(탈취 피해 최소) + 긴 세션의 편함(잦은 재로그인 방지) 동시 확보 |
| RT는 DB에 저장 | 운영자 1명 규모. Redis는 인프라 비용만 늘어남.|
| RT Rotation | 한 번 쓴 RT는 재사용 불가. 탈취돼도 정상 사용자가 한 번 갱신하면 무효화됨 |
| CSRF disable | JWT를 헤더로 전달 → CSRF 공격 대상이 아님 |
| 로그인 실패 메시지 통일 | 메시지가 달라지면 계정 열거 공격 가능 → "아이디 또는 비밀번호가 올바르지 않습니다"로 일원화 |
| 단일 ROLE_ADMIN | 다중 역할 불필요. Admin 엔티티에 roles 컬럼 없이 코드에서 고정 부여 |

<br>

## 신고 기능

비회원 모델이라 악의적인 글이 올라와도 작성자를 식별할 수 없습니다. 그래서 **사용자가 직접 신고하고, 누적되면 운영자가 검토**하는 흐름을 만들었습니다.

### 핵심 설계

- **신고 누적 → 검토 플래그 동작** — 장난성 신고로 정상 글이 사라지는 위험이 더 큼. 임계값(3) 도달 시 `flaggedForReview = true`만 켜고, 관리자가 사유를 보고 정상/삭제 판단
- **중복 신고 방지 — 신고자 IP를 키로 DB 유니크 제약** — 회원이라면 user_id로 막겠지만 비회원이라 IP 기반. 애플리케이션 레벨 검증만으론 race condition에 두 건이 통과할 수 있어 DB 제약으로 막음

```java
@Table(
    name = "cafe_visit_reports",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"visit_id", "reporter_ip"})
    }
)
```

### 신고자 IP는 `X-Forwarded-For` 우선

배포 환경에서 프록시(nginx, ALB)를 거치면 `request.getRemoteAddr()`은 항상 프록시 IP라 의미가 없습니다. `X-Forwarded-For`의 첫 번째 값을 우선 사용하고, 없을 때만 `RemoteAddr`로 폴백합니다.

> **알려진 한계**: `X-Forwarded-For`는 헤더라 위조 가능합니다. 운영 환경에서는 신뢰할 수 있는 프록시가 붙인 헤더만 받도록 처리해야 하지만, 단일 운영자 규모에선 검토 플래그 + 수동 검토가 1차 방어선이라 일단 이 수준으로 두고 향후 개선 항목으로 남겼습니다.

<br>

## 트러블슈팅

### #1 카페 목록 조회의 N+1 문제

> **증상**: 카페 5개 조회 시 SQL 6번 실행
> **원인**: `@ManyToOne(LAZY)` + DTO 변환 시 연관 엔티티 접근
> **해결**: `LEFT JOIN FETCH`로 1번으로 감소 (6배 ↓)

DTO 변환 시 `cafe.getUser().getNickname()`을 호출하는 순간 카페마다 LAZY 로딩이 발동해 추가 쿼리가 발생하고 있었습니다. 데이터가 적을 땐 체감이 없지만 카페 수에 비례해 선형 증가하는 구조라 미리 해결했습니다.

`INNER JOIN`이 아닌 **`LEFT JOIN FETCH`를 선택한 이유**는 비회원이 등록한 카페(`user = null`)도 조회 결과에 포함되어야 하기 때문입니다.

```java
@Query("SELECT c FROM CafeVisit c LEFT JOIN FETCH c.user")
List<CafeVisit> findAllWithUser();
```

상세한 발견 과정과 해결 흐름은 별도 글로 정리했습니다.

**[N+1 문제를 발견하고 해결한 이야기 (Velog)](https://velog.io/@qwg2825/N1-%EB%AC%B8%EC%A0%9C%EB%A5%BC-%EB%B0%9C%EA%B2%AC%ED%95%98%EA%B3%A0-%ED%95%B4%EA%B2%B0%ED%95%9C-%EC%9D%B4%EC%95%BC%EA%B8%B0)**

### #2 무효 토큰이 401 대신 403을 던지던 문제

> **증상**: 관리자 페이지에서 토큰이 만료되거나 무효해진 상태로 API 호출 시 401이 아닌 403이 반환되어, 프론트의 토큰 자동 갱신 인터셉터(401 트리거)가 동작하지 않음
> **원인**: `JwtAuthenticationFilter`가 토큰이 무효일 때 인증을 붙이지 않고 그대로 통과시킴 → Spring Security가 익명 사용자로 보고 `hasRole("ADMIN")` 검사에서 403 반환
> **해결**: 프론트 인터셉터가 401과 403을 함께 잡도록 수정

관리자 API 영역은 정당한 권한 부족으로 403이 나올 케이스가 사실상 없습니다(관리자 페이지에서 관리자 API만 호출). 따라서 403은 토큰 문제로 간주하고 동일하게 refresh를 시도, 실패 시 강제 로그아웃하는 흐름으로 일원화했습니다.

<br>

## API 명세

모든 응답은 `ApiResponse<T>` 형태로 통일됩니다.

```json
{
  "success": true,
  "message": null,
  "data": [ ... ]
}
```

### 인증

| Method | Endpoint | 설명 |
|---|---|---|
| `POST` | `/api/auth/login` | 로그인 → AT + RT 발급 |
| `POST` | `/api/auth/refresh` | RT로 새 AT + RT 발급 (Rotation) |
| `POST` | `/api/auth/logout` | RT 폐기 (멱등) |

### 카페 (공개)

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/api/cafes` | 전체 카페 방문 기록 조회 |
| `GET` | `/api/cafes/{id}` | 단일 카페 방문 기록 조회 |
| `POST` | `/api/cafes` | 카페 방문 기록 등록 |
| `PUT` | `/api/cafes/{id}` | 카페 방문 기록 수정 |
| `DELETE` | `/api/cafes/{id}` | 카페 방문 기록 삭제 |

### 신고 (공개)

| Method | Endpoint | 설명 |
|---|---|---|
| `POST` | `/api/cafes/{id}/reports` | 카페 신고 접수 (IP 기반 중복 방지, 임계값 3) |

### 관리자 (ROLE_ADMIN 필요)

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/api/admin/reports` | 검토 필요 카페 목록 (신고 수 내림차순) |
| `GET` | `/api/admin/reports/{id}` | 사유별 신고 통계 |
| `POST` | `/api/admin/reports/{id}/review` | 검토 완료 (정상 글 판정, flag 해제) |
| `DELETE` | `/api/admin/cafes/{id}` | 악성 글 삭제 (신고 내역 포함) |

### PUT/DELETE는 왜 만들었는데 프론트에서 안 쓰는가?

> 카공맵은 비회원 모델이라 "본인이 작성한 기록"을 식별할 방법이 없습니다.
> 누구나 다른 사람의 기록을 수정/삭제할 수 있게 되면 데이터 신뢰성이 무너지므로 **프론트에서는 의도적으로 수정/삭제 UI를 노출하지 않았습니다.**
>
> 다만 PUT/DELETE API는 **RESTful 표준에 맞춰 미리 구현**해 두었습니다.

<br>

## 의사결정 기록

프로젝트를 진행하며 **하지 않기로 결정한 것들**입니다. 모든 기능을 다 구현하기보다, 프로젝트의 핵심 가치(접근성)에 집중하기 위한 의도적 선택입니다.

| 항목 | 결정 | 이유 |
|---|---|---|
| 일반 사용자 회원/OAuth | 도입 안 함 | 핵심 가치인 "접근성" 고려. 닉네임 기반 비회원 모델로 진입 장벽 제거 |
| Bounding Box 쿼리 | 도입 안 함 | 현재 데이터 규모(수십~수백 건)에서는 전체 조회로 충분. 과도한 최적화 지양 |
| 프론트 수정/삭제 UI | 도입 안 함 | 비회원 모델에서 작성자 식별 불가 |
| 회원가입 API (관리자) | 도입 안 함 | 운영자 1명. `CommandLineRunner` 시드로 충분 |
| 비밀번호 변경/찾기 | 도입 안 함 | 운영자 1명 환경에선 불필요하다 생각하여 필요 시 환경변수 교체 + 재시드로 처리 |
| RT 저장소 Redis | 도입 안 함 | 단일 운영자 규모라 DB로 충분하다 판단하였다. |
| 토큰 블랙리스트 | 도입 안 함 | AT 30분 + RT Rotation으로 위험 시간 창이 짧음. |
| 자동 글 숨김 (신고 누적) | 도입 안 함 | 장난성 신고로 정상 글이 사라지는 위험이 더 큼. 검토 플래그 + 운영자 판단 방식 채택 |

<br>
