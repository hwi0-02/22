<template>
  <div class="reservation-monitoring">
    <div class="page-header">
      <h1>예약 관리</h1>
      <p class="page-description">호텔 예약 및 결제 상태를 모니터링하고 관리합니다.</p>
    </div>

    <!-- 검색 및 필터 -->
    <div class="search-section mb-16">
      <form class="search-form" @submit.prevent="searchReservations">
        <div class="search-group">
          <label>호텔명</label>
          <input v-model="filters.hotelName" placeholder="호텔명 검색" @keyup.enter="searchReservations" class="search-input" />
        </div>
        <div class="search-group">
          <label>예약자명</label>
          <input v-model="filters.userName" placeholder="예약자명 검색" @keyup.enter="searchReservations" class="search-input" />
        </div>
        <div class="search-group">
          <label>예약 상태</label>
          <select v-model="filters.reservationStatus" class="search-select">
            <option value="">전체</option>
            <option value="PENDING">대기중</option>
            <option value="CONFIRMED">확정</option>
            <option value="CHECKED_IN">체크인</option>
            <option value="CHECKED_OUT">체크아웃</option>
            <option value="CANCELLED">취소</option>
            <option value="NO_SHOW">노쇼</option>
          </select>
        </div>
        <div class="search-group">
          <label>결제 상태</label>
          <select v-model="filters.paymentStatus" class="search-select">
            <option value="">전체</option>
            <option value="PENDING">결제대기</option>
            <option value="COMPLETED">결제완료</option>
            <option value="FAILED">결제실패</option>
            <option value="CANCELLED">결제취소</option>
            <option value="REFUNDED">환불완료</option>
          </select>
        </div>
        <div class="search-buttons">
          <button class="btn btn-primary" type="submit">검색</button>
          <button class="btn btn-secondary" type="button" @click="resetFilters">초기화</button>
        </div>
      </form>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-row mb-16">
      <div class="stat-card"><div>전체 예약</div><div class="stat-number">{{ totalReservations }}</div></div>
      <div class="stat-card"><div>확정 예약</div><div class="stat-number confirmed">{{ confirmedReservations }}</div></div>
      <div class="stat-card"><div>취소 예약</div><div class="stat-number cancelled">{{ cancelledReservations }}</div></div>
      <div class="stat-card"><div>결제 완료</div><div class="stat-number paid">{{ paidReservations }}</div></div>
    </div>

    <!-- 예약 목록 테이블 -->
    <div class="table-section">
      <table class="reservations-table">
        <thead>
          <tr>
            <th>예약번호</th>
            <th>호텔명</th>
            <th>객실</th>
            <th>예약자</th>
            <th>체크인</th>
            <th>체크아웃</th>
            <th class="text-right">예약금액</th>
            <th>예약상태</th>
            <th>결제상태</th>
            <th>예약일시</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in reservations" :key="row.reservationId">
            <td>{{ row.reservationId }}</td>
            <td>{{ row.hotelName }}</td>
            <td>{{ row.roomName }} ({{ row.roomType }})</td>
            <td>
              <div>{{ row.userName }}</div>
              <small class="muted">{{ row.userEmail }}</small>
            </td>
            <td>{{ formatDate(row.checkInDate) }}</td>
            <td>{{ formatDate(row.checkOutDate) }}</td>
            <td class="text-right">{{ formatCurrency(row.totalAmount) }}</td>
            <td>{{ getReservationStatusText(row.reservationStatus) }}</td>
            <td>{{ getPaymentStatusText(row.paymentStatus) }}</td>
            <td>{{ formatDateTime(row.reservationCreatedAt) }}</td>
            <td><button class="btn btn-secondary btn-sm" @click="viewReservationDetail(row)">상세보기</button></td>
          </tr>
          <tr v-if="reservations.length === 0">
            <td colspan="11" class="muted center">조건에 맞는 예약이 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination" v-if="pagination.totalPages > 1">
      <button 
        @click="changePage(pagination.currentPage - 1)"
        :disabled="pagination.currentPage === 0"
        class="page-btn"
      >
        이전
      </button>
      
      <span class="page-info">
        {{ pagination.currentPage + 1 }} / {{ pagination.totalPages }}
      </span>
      
      <button 
        @click="changePage(pagination.currentPage + 1)"
        :disabled="pagination.currentPage >= pagination.totalPages - 1"
        class="page-btn"
      >
        다음
      </button>
    </div>

    <!-- 예약 상세 다이얼로그 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>예약 상세 정보</h3>
          <button class="close" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body" v-if="selectedReservation">
          <div class="detail-grid">
            <section class="detail-card">
              <h4>예약 정보</h4>
              <div class="desc-grid">
                <div><label>예약번호</label><span>{{ selectedReservation.reservationId }}</span></div>
                <div><label>예약상태</label><span>{{ getReservationStatusText(selectedReservation.reservationStatus) }}</span></div>
                <div><label>체크인</label><span>{{ formatDate(selectedReservation.checkInDate) }}</span></div>
                <div><label>체크아웃</label><span>{{ formatDate(selectedReservation.checkOutDate) }}</span></div>
                <div><label>투숙인원</label><span>{{ selectedReservation.guestCount }}명</span></div>
                <div><label>총 금액</label><span>{{ formatCurrency(selectedReservation.totalAmount) }}</span></div>
                <div v-if="selectedReservation.specialRequests" class="span-2"><label>특별요청</label><span>{{ selectedReservation.specialRequests }}</span></div>
                <div class="span-2"><label>예약일시</label><span>{{ formatDateTime(selectedReservation.reservationCreatedAt) }}</span></div>
              </div>
            </section>

            <section class="detail-card">
              <h4>결제 정보</h4>
              <div class="desc-grid">
                <div v-if="selectedReservation.paymentId"><label>결제번호</label><span>{{ selectedReservation.paymentId }}</span></div>
                <div><label>결제상태</label><span>{{ getPaymentStatusText(selectedReservation.paymentStatus) }}</span></div>
                <div v-if="selectedReservation.paymentMethod"><label>결제수단</label><span>{{ getPaymentMethodText(selectedReservation.paymentMethod) }}</span></div>
                <div v-if="selectedReservation.paidAmount"><label>결제금액</label><span>{{ formatCurrency(selectedReservation.paidAmount) }}</span></div>
                <div v-if="selectedReservation.paymentCreatedAt" class="span-2"><label>결제일시</label><span>{{ formatDateTime(selectedReservation.paymentCreatedAt) }}</span></div>
              </div>
            </section>

            <section class="detail-card">
              <h4>고객 정보</h4>
              <div class="desc-grid">
                <div><label>고객번호</label><span>{{ selectedReservation.userId }}</span></div>
                <div><label>고객명</label><span>{{ selectedReservation.userName }}</span></div>
                <div class="span-2"><label>이메일</label><span>{{ selectedReservation.userEmail }}</span></div>
              </div>
            </section>

            <section class="detail-card">
              <h4>호텔/객실 정보</h4>
              <div class="desc-grid">
                <div><label>호텔번호</label><span>{{ selectedReservation.hotelId }}</span></div>
                <div><label>호텔명</label><span>{{ selectedReservation.hotelName }}</span></div>
                <div><label>객실번호</label><span>{{ selectedReservation.roomId }}</span></div>
                <div><label>객실명</label><span>{{ selectedReservation.roomName }}</span></div>
                <div class="span-2"><label>객실타입</label><span>{{ selectedReservation.roomType }}</span></div>
              </div>
            </section>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn" @click="closeDetailModal">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'

export default {
  name: 'ReservationMonitoring',
  setup() {
    const reservations = ref([])
    const selectedReservation = ref(null)
    const showDetailModal = ref(false)
    
    // 필터 상태
    const filters = reactive({
      hotelName: '',
      userName: '',
      reservationStatus: '',
      paymentStatus: ''
    })

    // 페이지네이션 상태
    const pagination = reactive({
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      size: 20
    })

    // 통계 데이터
    const totalReservations = ref(0)
    const confirmedReservations = ref(0)
    const cancelledReservations = ref(0)
    const paidReservations = ref(0)

    // 예약 목록 조회
    const searchReservations = async () => {
      try {
        const params = new URLSearchParams({
          page: pagination.currentPage,
          size: pagination.size
        })

        if (filters.hotelName) params.append('hotelName', filters.hotelName)
        if (filters.userName) params.append('userName', filters.userName)
        if (filters.reservationStatus) params.append('reservationStatus', filters.reservationStatus)
        if (filters.paymentStatus) params.append('paymentStatus', filters.paymentStatus)

  const response = await fetch(`/api/admin/reservations?${params}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('예약 목록 조회 실패')

        const data = await response.json()
        reservations.value = data.content || []
        
        pagination.totalPages = data.totalPages || 0
        pagination.totalElements = data.totalElements || 0

        // 통계 업데이트 (실제로는 별도 API 호출)
        updateStatistics()
        
      } catch (error) {
        alert('예약 목록을 불러오는데 실패했습니다.')
      }
    }

    // 통계 업데이트
    const updateStatistics = () => {
      totalReservations.value = reservations.value.length
      confirmedReservations.value = reservations.value.filter(r => r.reservationStatus === 'CONFIRMED').length
      cancelledReservations.value = reservations.value.filter(r => r.reservationStatus === 'CANCELLED').length
      paidReservations.value = reservations.value.filter(r => r.paymentStatus === 'COMPLETED').length
    }

    // 페이지 변경
    const changePage = (page) => {
      pagination.currentPage = page
      searchReservations()
    }

    // 필터 초기화
    const resetFilters = () => {
      filters.hotelName = ''
      filters.userName = ''
      filters.reservationStatus = ''
      filters.paymentStatus = ''
      pagination.currentPage = 0
      searchReservations()
    }

    // 예약 상세 보기
    const viewReservationDetail = async (reservation) => {
      try {
  const response = await fetch(`/api/admin/reservations/${reservation.reservationId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        if (!response.ok) throw new Error('예약 상세 조회 실패')

        selectedReservation.value = await response.json()
        showDetailModal.value = true
        
      } catch (error) {
        alert('예약 상세 정보를 불러오는데 실패했습니다.')
      }
    }

    // 상세 모달 닫기
    const closeDetailModal = () => {
      showDetailModal.value = false
      selectedReservation.value = null
    }

    // 예약 상태 텍스트
    const getReservationStatusText = (status) => {
      const statusMap = {
        'PENDING': '대기중',
        'CONFIRMED': '확정',
        'CHECKED_IN': '체크인',
        'CHECKED_OUT': '체크아웃',
        'CANCELLED': '취소',
        'NO_SHOW': '노쇼'
      }
      return statusMap[status] || status
    }

    const reservationTagType = (status) => {
      const map = { PENDING: 'warning', CONFIRMED: 'success', CHECKED_IN: 'info', CHECKED_OUT: 'info', CANCELLED: 'danger', NO_SHOW: 'danger' }
      return map[status] || 'info'
    }
    const paymentTagType = (status) => {
      const map = { PENDING: 'warning', COMPLETED: 'success', FAILED: 'danger', CANCELLED: 'info', REFUNDED: 'danger' }
      return map[status] || 'info'
    }

    // 예약 상태 클래스
    const getReservationStatusClass = (status) => {
      const classMap = {
        'PENDING': 'status-pending',
        'CONFIRMED': 'status-confirmed',
        'CHECKED_IN': 'status-checked-in',
        'CHECKED_OUT': 'status-checked-out',
        'CANCELLED': 'status-cancelled',
        'NO_SHOW': 'status-no-show'
      }
      return classMap[status] || ''
    }

    // 결제 상태 텍스트
    const getPaymentStatusText = (status) => {
      if (!status) return '결제정보없음'
      const statusMap = {
        'PENDING': '결제대기',
        'COMPLETED': '결제완료',
        'FAILED': '결제실패',
        'CANCELLED': '결제취소',
        'REFUNDED': '환불완료'
      }
      return statusMap[status] || status
    }

    // 결제 상태 클래스
    const getPaymentStatusClass = (status) => {
      if (!status) return 'status-no-payment'
      const classMap = {
        'PENDING': 'status-pending',
        'COMPLETED': 'status-completed',
        'FAILED': 'status-failed',
        'CANCELLED': 'status-cancelled',
        'REFUNDED': 'status-refunded'
      }
      return classMap[status] || ''
    }

    // 결제 수단 텍스트
    const getPaymentMethodText = (method) => {
      if (!method) return '미지정'
      const methodMap = {
        'CREDIT_CARD': '신용카드',
        'BANK_TRANSFER': '계좌이체',
        'VIRTUAL_ACCOUNT': '가상계좌',
        'MOBILE': '휴대폰결제',
        'KAKAO_PAY': '카카오페이',
        'NAVER_PAY': '네이버페이'
      }
      return methodMap[method] || method
    }

    // 날짜 포맷팅
    const formatDate = (dateString) => {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleDateString('ko-KR')
    }

    // 날짜시간 포맷팅
    const formatDateTime = (dateString) => {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('ko-KR')
    }

    // 통화 포맷팅
    const formatCurrency = (amount) => {
      if (!amount) return '-'
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
      }).format(amount)
    }

    // 컴포넌트 마운트 시 데이터 로드
    onMounted(() => {
      searchReservations()
    })

    return {
      reservations,
      selectedReservation,
      showDetailModal,
      filters,
      pagination,
      totalReservations,
      confirmedReservations,
      cancelledReservations,
      paidReservations,
      searchReservations,
      changePage,
      resetFilters,
      viewReservationDetail,
      closeDetailModal,
      getReservationStatusText,
      getReservationStatusClass,
      getPaymentStatusText,
      getPaymentStatusClass,
      getPaymentMethodText,
      formatDate,
      formatDateTime,
      formatCurrency
    }
  }
}
</script>

<style scoped src="@/assets/css/admin/reservation-monitoring.css"></style>