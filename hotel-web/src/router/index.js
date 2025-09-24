// src/router/index.js
import { createRouter, createWebHistory } from "vue-router"
import http from "@/api/http.js";

// Auth & static
import Login from "@/components/user/login_page/Login.vue"
import Register from "@/components/user/login_page/Register.vue"
import ForgotPassword from "@/components/user/login_page/ForgotPassword.vue"
import LoginVerify from "@/components/user/login_page/LoginVerify.vue"
import PasswordReset from "@/components/user/login_page/PasswordReset.vue"
import OAuth2Redirect from "@/components/user/login_page/OAuth2Redirect.vue"
import MainPage from "@/components/user/main_page/MainPage.vue"
import TermsPage from "@/components/user/main_page/Terms.vue"
import PrivacyPage from "@/components/user/main_page/Privacy.vue"

// Hotel search/detail (네가 쓰던 경로 유지)
import Search from "@/components/user/hotel_page/Search.vue"
// 상세는 지연 로딩 권장
const HotelDetailView = () => import("@/components/user/hotel_page/HotelDetailView.vue")

// Checkout pages (요청한 디렉토리)
const ReservationCheckout = () => import("@/components/user/hotel_checkout/ReservationCheckout.vue")
const ReservationResult   = () => import("@/components/user/hotel_checkout/ReservationResult.vue")

const routes = [
  { path: "/", component: MainPage },

  // 검색/상세
  { path: "/search", name: "Search", component: Search },
  { path: "/hotels/:id", component: HotelDetailView, props: true },
  { path: "/hotels", redirect: "/hotels/1" },

  // 체크아웃/결과
  { path: "/reservations/:id", name: "ReservationCheckout", component: ReservationCheckout },
  { path: "/reservations/:id/result", name: "ReservationResult", component: ReservationResult },

  // Auth / 정책
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/terms", component: TermsPage },
  { path: "/privacy", component: PrivacyPage },
  { path: "/forgotPassword", component: ForgotPassword },
  { path: "/forgot-password", component: ForgotPassword },
  { path: "/verify", component: LoginVerify },
  { path: "/passwordReset", component: PasswordReset },
  { path: "/password-reset", component: PasswordReset },
  { path: "/oauth2/redirect", component: OAuth2Redirect },
]

// 관리자 라우트 추가
routes.push({
  path: "/admin",
  component: () => import("@/components/admin/AdminLayout.vue"),
  meta: { requiresAdmin: true },
  children: [
    { path: "", redirect: "/admin/dashboard" },
    { path: "dashboard", component: () => import("@/components/admin/AdminDashboard.vue") },
    { path: "users", component: () => import("@/components/admin/UserManagement.vue") },
    { path: "businesses", component: () => import("@/components/admin/HotelManagement.vue") },
    { path: "hotels", component: () => import("@/components/admin/AdminHotelManagement.vue") },
    { path: "reservations", component: () => import("@/components/admin/ReservationMonitoring.vue") },
    { path: "sales", component: () => import("@/components/admin/SalesManagement.vue") },
    { path: "payments", component: () => import("@/components/admin/PaymentManagement.vue") },
    { path: "reviews", component: () => import("@/components/admin/ReviewManagement.vue") },
    { path: "coupons", component: () => import("@/components/admin/CouponManagement.vue") }
  ]
});

async function checkAdminRole() {
  try {
    const resp = await http.get('/user/info');
    const serverRole = resp?.data?.role;
    if (serverRole) {
      localStorage.setItem('userRole', serverRole);
      return serverRole === 'ADMIN';
    }
  } catch (e) {
    if (e?.response?.status === 401) {
      return false;
    }
  }
  const userRole = localStorage.getItem('userRole');
  if (userRole) return userRole === 'ADMIN';
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      if (user?.role) {
        localStorage.setItem('userRole', user.role);
        return user.role === 'ADMIN';
      }
    } catch (e) {}
  }
  return false;
}

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
});

router.beforeEach(async (to, from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('로그인이 필요합니다.');
      next('/login');
      return;
    }
  }
  if (to.meta.requiresAdmin) {
    const isAdmin = await checkAdminRole();
    if (isAdmin) next();
    else { alert('관리자 권한이 필요합니다.'); next('/'); }
  } else {
    next();
  }
});

export default router;
