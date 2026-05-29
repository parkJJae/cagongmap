# ☕ 카공맵 (CagongMap)

> **카공족을 위한 카페 방문 기록 서비스** <br>
> 콘센트, 와이파이 속도, 분위기까지 — 카공하기 좋은 카페를 직접 다녀온 사람의 기록으로 확인하세요.

<br>

## 미리보기

<img width="900" alt="카공맵 메인 페이지" src="https://github.com/user-attachments/assets/6fb8c583-e1ce-4932-a1a5-fdadaa9d6430" /> <br>
*▲ 메인 페이지*

<br>

<img width="900" alt="카공맵 지도 페이지" src="https://github.com/user-attachments/assets/fce4ee30-8a10-432f-8bd1-599a4589de4b" /> <br>
*▲ 지도 페이지*

<br>

## 📌 프로젝트 소개

카공맵은 로그인 없이 누구나 빠르게 카페 정보를 기록하고 공유할 수 있는 지도 기반 서비스입니다.

저는 평소에 카페에서 작업하거나 공부하는 것을 좋아하는데 새로운 지역의 카페에 가기 전에 항상 고민합니다.

> "여기 콘센트는 있나?", "와이파이 빠른가?", "공부하기엔 너무 시끄럽지 않을까?"

이런 정보를 실제로 다녀온 사람들이 직접 기록해서 시간을 낭비하지 않도록 하는 게 카공맵의 목표입니다.

### 핵심 가치

- **접근성** — 회원가입/로그인 없이 누구나 즉시 이용
- **신뢰성** — 직접 다녀온 사람의 기록 (별점, 콘센트 유무, 와이파이 속도, 메모)
- **시각화** — 지도에서 한눈에 카공 카페 발견

<br>

## 시스템 아키텍처

<p align="center">
  <img src="https://github.com/user-attachments/assets/91a245ef-330a-4cfb-ad2d-9bfdb7e17579" alt="카공맵 시스템 아키텍처" width="780"/>
</p>

브라우저 → React(Vite) + 카카오맵 → Spring Boot REST API → JPA → H2/MySQL 으로 이어지는 단순한 4-tier 구조입니다.
프론트는 axios 인터셉터로 공통 응답 포맷(ApiResponse)을 자동 처리하고, 백엔드는 환경 프로파일에 따라 H2(dev) / MySQL(prod) DB를 자동으로 선택합니다.

<br>

## 🛠 기술 스택

### Backend
- **Spring Boot 3** (4.0), Java 17
- **Spring Data JPA** + JPA Auditing
- **H2** (개발) / **MySQL** (운영) — 환경별 분리
- **Lombok**, Bean Validation

### Frontend
- **React 19** + Vite 7
- **react-leaflet** + **Kakao Maps SDK** — 지도 시각화
- **axios** + 커스텀 인터셉터 (공통 응답 자동 처리)

### Infra
- Oracle Cloud Infrastructure VM에 직접 배포 (운영자 1명 규모 기준 비용 최적화)

<br>

## 프로젝트 구조 (모노레포)

```
cagongmap/
├── backend/    # Spring Boot API 서버
└── frontend/   # React 클라이언트
```

각 폴더의 상세 문서는 아래 링크를 참고해 주세요.

- **[Backend README](./backend/README.md)** — 아키텍처, 리팩토링 스토리, 트러블슈팅
- (Frontend는 백엔드 중심 포트폴리오라 별도 문서 없음)

<br>



## 주요 기능

| 기능 | 설명 |
|---|---|
| 카페 기록 등록 | 카카오맵 검색 → 위치 선택 → 별점/콘센트/와이파이/메모 작성 |
| 지도 시각화 | 등록된 카페를 지도 위 마커로 표시 (만족도별 색상) |
| 검색 | 카페명/지점으로 빠른 검색 |
| 비회원 모델 | 로그인 없이 닉네임만으로 즉시 기록 가능 |

<br>

## 학습 포인트 (기술 블로그)

프로젝트 진행 중 마주친 문제와 해결 과정을 글로 정리했습니다.

- **[N+1 문제를 발견하고 해결한 이야기](https://velog.io/@qwg2825/N1-%EB%AC%B8%EC%A0%9C%EB%A5%BC-%EB%B0%9C%EA%B2%AC%ED%95%98%EA%B3%A0-%ED%95%B4%EA%B2%B0%ED%95%9C-%EC%9D%B4%EC%95%BC%EA%B8%B0)** — Fetch Join으로 6번 쿼리를 1번으로

<br>

