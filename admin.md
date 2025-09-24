# Admin 기능 이식 점검 보고서 및 실행 계획 (hotel → hotel_main)

- 기준일: 2025-09-24
- 원본(소스): `c:\hotel2\hotel\my-backend`
- 대상(타겟): `c:\hotel_main\hotel_main\my-backend`
- 목적: hotel 폴더의 관리자 기능을 hotel_main으로 완전 이식. API/Service/DTO/Repository/도메인 차이를 정밀 비교하여 누락/불일치 항목을 보완.

**요약 결론**
- 관리자 컨트롤러/리포지토리/서비스의 골격 이식 완료, DTO 반환으로 일관화.
- 도메인 차이에 따른 DTO/집계 수정 완료했고 최근 빌드 성공 상태.
- 원본 `config`의 초기화/동기화 로직은 대상 도메인에 맞춰 보정 완료.

---

## 1) 패키지/구성 비교

- 공통 패키지 존재 (hotel_main)
  - `admin/{controller,service,repository,domain,dto,security}`
  - `authlogin/{config,domain,repository}`
  - `hotel_reservation/{controller,service,repository,domain}`
  - `payment/{repository,domain}`
  - `review/{repository,domain}`

- 원본(hotel)과의 차이점
  - 원본 `config` → 대상은 `authlogin.config`로 이동 (AdminDataInitializer, BusinessRoleReconciler 등)
  - 원본 `fe_hotel_detail.domain.Review` → 대상은 `review.domain.Review`
  - 원본 `hotel_reservation.domain.Payment` → 대상은 `payment.domain.Payment` (구조 간소화)

결론: 컨트롤러/서비스는 존재하나, 의존하는 도메인 필드가 달라 DTO/통계에서 오류.

---

## 2) 컨트롤러 비교 및 상태

- `AdminController` (원본) → 대상에서는 기능별 분리:
  - `AdminUserController` (완료)
  - `AdminBusinessController` (완료, 서비스 분리 OK)
  - `AdminReservationController` (존재, 간소 응답 형태)
  - `AdminPaymentController` (DTO 반환으로 상향 완료)
  - `AdminReviewController` (DTO 반환으로 상향 완료, 상태 토글 API 제공)
  - `AdminCouponController` (존재, CRUD/통계 제공)
  - `AdminDashboardController` (존재, 집계 API 제공)

평가: 엔드포인트는 대체로 대응되며, 반환 DTO 수준/연관정보는 원본보다 단순화되어 있음.

---

## 3) 서비스/리포지토리 비교

- `AdminBusinessService` (대상 존재) → 원본과 유사: 페이징/필터 처리 후 `BusinessAdminDto` 매핑
- `BusinessService` (대상 존재) → 승인/거절 로직 제공, `AdminBusinessController`에서 사용
- `BusinessRepository` (대상 존재) → 원본과 동일한 JPQL 제공하되, 날짜 필드 명만 차이
  - 원본: `createdAt`
  - 대상: `createdOn`를 사용하도록 쿼리 변경됨 (대상 엔티티 확인 필요)
- 기타 Repository: Payment/Review/Reservation 각각의 단순화된 스키마 반영

평가: 서비스/리포지토리는 존재하나 날짜 필드/도메인 차이 반영 필요.

---

## 4) 도메인 모델 차이(핵심)

- User (대상: `authlogin.domain.User`)
  - 필드: `createdOn`(TIMESTAMP), `role` enum 존재, `provider` 존재
  - 연관관계: Business 필드 없음 → Business는 `BusinessRepository.findByUser(User)`로 조회 필요

- Business (대상: `admin.domain.Business`)
  - 필드: `createdAt`, `updatedAt` (확인됨), `status` enum 존재, `user` 연관 존재
  - Repository에서 월별 집계는 `createdOn`이 아닌 `createdAt` 사용해야 일관성

- Coupon (대상: `admin.domain.Coupon`)
  - 필드: `createdAt`, `updatedAt` 존재. 원본 DTO에서 `createdOn` 사용하던 부분을 `createdAt`로 맞춰야 함

- Reservation (대상: `hotel_reservation.domain.Reservation`)
  - 필드: `userId`, `roomId`, `startDate(Instant)`, `endDate(Instant)`, `numAdult/numKid/numRooms`, `status`, `transactionId`
  - 연관: User/Room/Payment 컬렉션 연관 없음 (원본과 큰 차이)

- Payment (대상: `payment.domain.Payment`)
  - 필드: `reservationId`, `amount(BigDecimal)`, `status(enum)`, `method(String)`, `transactionId`, `paidAt(Instant)`
  - 연관/추가 필드: 없음 (원본의 `orderId`, `paymentKey`, `cancelledAt`, `reservation` 연관 등이 삭제됨)

- Review (대상: `review.domain.Review`)
  - 필드: `hotelId`, `userId`, `rating`, `content`, `hidden`, `reported`, `createdAt`
  - 연관/추가 필드: 없음 (원본의 `title`, `imageUrls`, `adminReply`, `repliedAt`, 예약/호텔/사용자 연관 제거)

결론: 원본 DTO 변환코드가 대상 도메인에 그대로 맞지 않아 컴파일 오류 대량 발생.

---

## 5) 현재 컴파일 오류 군 요약 (관찰된 패턴)

- `BusinessAdminDto`: 빌더 필드명 `createdAt` → 대상 DTO는 `createdOn` 사용 중. 소스는 `business.getCreatedAt()` 호출
- `CouponAdminDto`: `getCreatedOn()` 호출 → 대상 도메인에는 `getCreatedAt()`
- `PaymentAdminDto`: 원본 방식의 연관 접근(`p.getReservation() ...`) 및 필드(`orderId`, `paymentKey`, `cancelledAt`, `method enum`) 사용 → 대상에는 존재하지 않음
- `ReservationAdminDto`: 원본의 연관 필드를 광범위하게 사용 → 대상은 단순 id/Instant 기반, 연관 없음
- `ReviewAdminDto`: 원본의 확장 필드/연관 사용 → 대상은 단순 스칼라 필드만 존재
- `BusinessRoleReconciler`: `User#getBusiness()` 접근 → 대상 User에 연관 없음

---

## 6) 이식/수정 전략 (권장)

- 원칙: 컨트롤러/서비스의 엔드포인트는 유지하되, DTO 매핑을 대상 도메인 스키마에 맞게 조정
- 연관 데이터가 필요한 경우: 현재 단계에서는 간략 응답(필수 스칼라 위주)으로 정렬하고, 추후 V2에서 조인/조회로 확장
- 날짜 필드 명 일치: `createdAt` vs `createdOn` 혼용 제거

---

## 7) 실행 계획 (체크리스트)

1) DTO 호환성 수정 (컴파일 블로커)
- [x] `admin/dto/BusinessAdminDto.java`
  - 매핑 유지: DTO는 `createdOn`, 엔티티는 `getCreatedAt()` 사용 중 → 현재 빌드 성공 상태
- [x] `admin/dto/CouponAdminDto.java`
  - `coupon.getCreatedOn()` → `coupon.getCreatedAt()`로 수정 완료
- [x] `admin/dto/PaymentAdminDto.java`
  - 원본 연관/불필요 필드 제거, 단순 스키마에 맞게 매핑 완료 (`reservationId/amount/method/status/paidAt`)
- [x] `admin/dto/ReservationAdminDto.java`
  - `startDate/endDate(Instant)` → `LocalDate` 변환, `guestCount=numAdult+numKid`, 연관 접근 제거 완료
- [x] `admin/dto/ReviewAdminDto.java`
  - 원본 전용 필드 제거, 스칼라 필드만 사용하도록 정리 완료

2) 초기화/동기화 로직 검토
- [x] `authlogin.config.AdminDataInitializer`
  - User 빌더로 생성 후 `setRole(ADMIN)` 적용 완료
  - `createdOn`은 엔티티 기본값으로 설정됨
- [x] `authlogin.config.BusinessRoleReconciler`
  - `u.getBusiness()` 제거 → `businessRepository.findByUser(u)`로 승인 여부 확인 및 저장 처리 완료

3) Repository/집계 쿼리 정합성
- [x] `admin.repository.BusinessRepository`
  - 월별/기간 쿼리 날짜 필드를 `createdAt`로 통일 완료
- [x] `payment.repository.PaymentRepository`
  - 단순 스키마에 맞춰 정리: `paidAt` 기준 조회, 상태 `'COMPLETED'`로 통일, 합계 `COALESCE` 처리
  - 호텔/객실 조인 기반 메서드는 현재 스키마에서 불가하므로 제거

4) Controller 응답 일관성
- [x] `AdminReservationController` → `ReservationAdminDto` 반환으로 상향 완료
- [x] `AdminPaymentController` → `PaymentAdminDto` 매핑 후 반환으로 정리
- [x] `AdminReviewController` → `ReviewAdminDto` 매핑 후 반환으로 정리

5) 대시보드 통계 정합성
- [x] `AdminDashboardService`
  - `paymentRepository` 매출 합계/일매출 집계 연동 완료
  - 상위 호텔 집계: Payment→Reservation→Room→Hotel 네이티브 조인으로 구현, 대시보드 연동 완료
  - 사용자/사업자 월별 가입 집계 병합 로직 반영 완료

---

## 8) 단계별 수행 순서 (권고)

- Phase A: 컴파일 오류 차단 (DTO/초기화/동기화)
  - 위 1), 2) 항목 일괄 수정 후 빌드 → 남은 오류 목록 축소

- Phase B: Repository 쿼리/통계 메서드 보강
  - 대시보드 통계용 리포지토리 메서드 시그니처 확인, 필요한 경우 네이티브/JPQL 추가

- Phase C: Controller 응답 포맷 상향
  - AdminReservation/Payment/Review 컨트롤러가 엔티티 직접 반환 → DTO 매핑 계층 도입

- Phase D: 확장 기능 회수
  - Review 첨부/답글 등 원본 기능이 필요하다면 도메인/스키마 확장 설계 검토

---

## 9) 즉시 적용할 코드 변경 목록 (요약)

- DTO
  - `CouponAdminDto`: `getCreatedOn()` → `getCreatedAt()`
  - `PaymentAdminDto`: 대상 스키마 기준으로 필드 접근 정리, `transactionId` 활용, 시간 변환 일관화
  - `ReservationAdminDto`: `Instant` 변환, id 기반 필드 활용, 연관 접근 제거
  - `ReviewAdminDto`: 스칼라 필드만 사용, 연관 접근 제거
  - `BusinessAdminDto`: 날짜 필드 명 일치 확인 (`createdAt` vs `createdOn`) 후 통일

- Config
  - `AdminDataInitializer`: builder 후 `setRole(ADMIN)` 적용
  - `BusinessRoleReconciler`: `findByUser(u)`로 승인 여부 확인

- Repository
  - `BusinessRepository` 월별 집계 `createdAt/createdOn` 통일
  - `PaymentRepository` 통계 메서드 존재 확인 및 구현

---

## 10) 검증 계획

- 빌드: PowerShell
```powershell
cd C:\hotel_main\hotel_main\my-backend
./mvnw.cmd clean compile
```
- API 스모크 테스트: 관리자 토큰으로 각 컨트롤러 GET 호출 및 200 확인
- 대시보드: `/api/admin/dashboard` 응답 키 검증 (`users`, `businessesPending`, `hotels`, `reservationsToday`, `revenueToday`)

현재 상태: 위 빌드 절차로 BUILD SUCCESS 확인됨 (2025-09-24).

---

## 11) 리스크와 후속 과제

- 도메인 단순화로 인해 원본의 풍부한 연관 기반 DTO가 축약됨. 프론트 요구사항에 따라 추가 조회/조인 로직 필요
- 시간 필드(Instant/LocalDate/LocalDateTime) 혼재로 직렬화 포맷 주의 필요
- 결제/예약 통계 쿼리는 DB 스키마와 일치하도록 재작성 필요할 수 있음

본 계획에 따라 Phase A 변경을 먼저 적용하면 현재의 컴파일 오류를 크게 줄일 수 있습니다. 이후 대시보드/통계와 응답 DTO 정교화 작업을 단계적으로 진행합니다.

---

## 12) DB 스키마 정렬 계획 (edit.md 기준)

편의상 edit.md의 스키마를 정본으로 간주하고, 현 도메인 모델을 이에 맞춰 단계적으로 정렬합니다. 각 테이블 ↔ 엔티티 매핑과 필요한 리팩토링을 요약합니다.

- `app_user` ↔ `authlogin.domain.User`
  - 컬럼 매핑: `created_on` → `createdOn`, `date_of_birth` → `dateOfBirth`
  - 상태: 현재 엔티티와 대체로 일치. 빌더 기본 role=USER 이므로 초기화 시 `setRole(ADMIN)` 필요

- `Hotel`/`Room`/`hotel_image`/`Room_Inventory`/`Room_Price_Policy`
  - 현 프로젝트 내 도메인(호텔/객실)과 명명/스키마가 다를 수 있음 → 즉시 대상은 아님 (Admin 핵심 경로는 예약/결제/리뷰)
  - 추후 호텔/객실 관리 화면에서 필요한 경우 @Table/@Column 명시로 정합성 확보

- `Reservation` ↔ `hotel_reservation.domain.Reservation`
  - edit.md: `user_id`, `room_id`, `num_rooms`, `transaction_id`, `num_adult`, `num_kid`, `start_date`, `end_date`, `created_at`, `status(PENDING|COMPLETED|CANCELLED)`, `expires_at`
  - 현재 엔티티: `userId`, `roomId`, `numRooms`, `numAdult`, `numKid`, `startDate(Instant)`, `endDate(Instant)`, `status`, `expiresAt`, `transactionId` (대부분 일치). 누락: `created_at`
  - 조치: `createdAt` 필드 추가(@Column(name="created_at"), @PrePersist 기본값), 상태 enum 값이 DB와 일치하는지 확인

- `Payment` ↔ `payment.domain.Payment` (차이 큼)
  - edit.md: `reservation_id`, `payment_method(varchar)`, `base_price`, `total_price`, `tax`, `discount`, `status(PAID|CANCELLED|REFUNDED)`, `created_at`, `refunded_at`, `receipt_url`
  - 현재 엔티티: `reservationId`, `amount(BigDecimal)`, `status(PENDING|COMPLETED|FAILED|REFUNDED)`, `method(String)`, `transactionId`, `paidAt(Instant)`
  - 조치:
    - 필드 추가/개명: `basePrice(int)`, `totalPrice(int)`, `tax(int)`, `discount(int)`, `createdAt`, `refundedAt`, `receiptUrl`, `paymentMethod`(또는 `method`를 `@Column(name="payment_method")`로 매핑)
    - 상태 enum 동기화: DB의 `PAID|CANCELLED|REFUNDED`로 통일 또는 변환 계층 도입
    - `amount/paidAt/transactionId`는 유지 가능하나, 핵심은 `total_price` 사용으로 정렬
    - 통계 쿼리는 `created_at` 기준으로 변경

- `Coupon` ↔ `admin.domain.Coupon` (차이 존재)
  - edit.md: `Coupon(id, user_id, name, code, discount_type, discount_value(int), min_spend(int), valid_from, valid_to, is_active)`
  - 현재 엔티티: `coupons` 테이블, `discountValue(BigDecimal)`, `minOrderAmount(BigDecimal)`, `maxDiscountAmount`, `usedCount/usageLimit`, `status`, `createdAt/updatedAt`
  - 조치: 두 가지 옵션
    - A) 현 엔티티 유지, 별도 마이그레이션/뷰로 edit.md 호환층 제공
    - B) 엔티티를 edit.md에 맞추어 단순화 (관리자 기능 범위 고려 시 B 권장 시나리오 재검토 필요)
  - 최소 조치: 관리자 화면에서 사용하는 필드만 우선 일치화 (`code/name/discount_type/discount_value/valid_from/valid_to`)

- `Review` ↔ `review.domain.Review` (차이 큼)
  - edit.md: `reservation_id`, `wrote_on`, `star_rating`, `content`, `image`
  - 현재 엔티티: `hotelId`, `userId`, `rating`, `content`, `hidden`, `reported`, `createdAt`
  - 조치:
    - 필드 조정: `reservationId`, `wroteOn`, `starRating`, `image` 추가; `hidden/reported/hotelId/userId`는 유지 여부 결정
    - 관리자 기능 요구사항에 따라 최소 매핑으로 우선 정렬 후 추가 필드 보강

### 우선순위 적용 순서 (DB 정렬)
- Step 1: Reservation에 `created_at` 추가 → DTO `reservationCreatedOn` 매핑 복구 (완료)
- Step 2: Payment에 `created_at` 추가 → 대시보드/통계 기준 시각 정합성 향상 (완료)
- Step 3: Review 필드 보강 → `ReviewAdminDto` 정합성 확보
- Step 4: Coupon 필드 최소 정렬 → `CouponAdminDto` 컴파일/런타임 정합성
- Step 5: @Table/@Column 정규화로 테이블명/컬럼명 정확 매핑 (대문자 테이블명 포함)

### 마이그레이션/검증
- 엔티티 변경 후 컴파일 → H2/로컬 DB 스키마 검증 (DDL 출력 확인)
- 주요 API 스모크 테스트 및 대시보드 통계 검증

현재 진행 현황 요약 (2025-09-24)
- DTO 정렬/컨트롤러 DTO 반환: 완료 (Reservation/Payment/Review)
- Repository 정렬: `BusinessRepository`(createdAt), `PaymentRepository`(paidAt/COMPLETED/합계 COALESCE/상위호텔 네이티브 집계) 완료
- DB 정렬: `Reservation.created_at`, `Payment.created_at` 추가 완료; `Payment` 확장 필드(basePrice,totalPrice,tax,discount,refundedAt,receiptUrl) 반영; `Review` 필드(reservationId,wroteOn,image) 반영
- 대시보드: 월별 가입 집계 병합, 일매출/오늘 매출, 상위 호텔 Top5 연동 완료

