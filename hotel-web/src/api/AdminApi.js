import http from './http.js';

export default {
  // 사용자 관리
  getUsers: (params) => http.get('/admin/users', { params }),
  updateUserRole: (userId, role) => http.put(`/admin/users/${userId}/role`, { role }),

  // 사업자/호텔 관리
  getBusinesses: (params) => http.get('/admin/businesses', { params }),
  approveBusiness: (id) => http.put(`/admin/businesses/${id}/approve`),
  rejectBusiness: (id, reason) => http.put(`/admin/businesses/${id}/reject`, { reason }),

  getHotels: (params) => http.get('/admin/hotels', { params }),
  getHotelDetail: (hotelId) => http.get(`/admin/hotels/${hotelId}`),
  updateHotelStatus: (hotelId, status, reason) => http.put(`/admin/hotels/${hotelId}/status`, { status, reason }),
  getHotelStats: (hotelId) => http.get(`/admin/hotels/${hotelId}/stats`),

  // 객실 관리
  getRooms: (params) => http.get('/admin/rooms', { params }),
  getRoomDetail: (roomId) => http.get(`/admin/rooms/${roomId}`),
  updateRoomStatus: (roomId, status) => http.put(`/admin/rooms/${roomId}/status`, { status }),
  deleteRoom: (roomId) => http.delete(`/admin/rooms/${roomId}`),
  getRoomInventory: (roomId) => http.get(`/admin/rooms/${roomId}/inventory`),

  // 예약/결제/리뷰/쿠폰
  getReservations: (params) => http.get('/admin/reservations', { params }),
  getReservationDetail: (id) => http.get(`/admin/reservations/${id}`),
  getPayments: (params) => http.get('/admin/payments', { params }),
  getReviews: (params) => http.get('/admin/reviews', { params }),
  getReviewDetail: (id) => http.get(`/admin/reviews/${id}`),
  hideReview: (id) => http.put(`/admin/reviews/${id}/hide`),
  showReview: (id) => http.put(`/admin/reviews/${id}/show`),
  reportReview: (id) => http.put(`/admin/reviews/${id}/report`),

  getCoupons: (params) => http.get('/admin/coupons', { params }),
  createCoupon: (data) => http.post('/admin/coupons', data),
  updateCoupon: (id, data) => http.put(`/admin/coupons/${id}`, data),
  updateCouponStatus: (id, status) => http.put(`/admin/coupons/${id}/status`, { status }),
  deleteCoupon: (id) => http.delete(`/admin/coupons/${id}`),
  getCouponStats: () => http.get('/admin/coupons/stats')
};