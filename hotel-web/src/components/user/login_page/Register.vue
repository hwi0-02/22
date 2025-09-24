<template>
  <div class="register-page">
        <div class="register-container">
            <!-- 왼쪽 : 로고 -->
            <div class="register-logo">
                <img src="/egodaLogo.png" alt="egoda" />
            </div>

            <!-- 오른쪽 : 회원가입 폼 -->
            <div class="register-form">
                <div class="back-link" @click=$router.go(-1)>← 돌아가기</div>
                
                <h1>회원가입</h1>
                <p class="sub-text">Register</p>

                <!-- 성 & 이름 -->
                <div class="name-row">
                <div class="input-group half">
                    <label for="lastName">성</label>
                    <input type="text" id="lastName" v-model="lastName" placeholder="성" />
                </div>
                <div class="input-group half">
                    <label for="firstName">이름</label>
                    <input type="text" id="firstName" v-model="firstName" placeholder="이름" />
                </div>
                </div>

                <!-- 이메일 -->
                <div class="input-group">
                <label for="email">이메일</label>
                <input type="email" id="email" v-model="email" placeholder="이메일" />
                </div>

                <!-- 전화번호 -->
                <div class="input-group">
                <label for="phone">전화번호</label>
                <input type="tel" id="phone" v-model="phone" placeholder="010-1234-5678" />
                </div>

                <!-- 생년월일 -->
                <div class="input-group">
                <label for="dateOfBirth">생년월일</label>
                <input type="date" id="dateOfBirth" v-model="dateOfBirth" />
                </div>

                <!-- 주소 -->
                <div class="input-group">
                <label for="address">주소</label>
                <input type="text" id="address" v-model="address" placeholder="주소를 입력해주세요" />
                </div>

                <!-- 비밀번호 -->
                <div class="input-group">
                    <label for="password">비밀번호</label>
                    <div class="password-box">
                        <input :type="showPassword ? 'text' : 'password'" id="password" v-model="password" placeholder="비밀번호"/>
                        <button type="button" class="toggle-btn" @click="togglePassword">
                        {{ showPassword ? '숨김' : '보기' }}
                        </button> 
                    </div>
                </div>

                <!-- 비밀번호 확인 -->
                <div class="input-group">
                <label for="confirmPassword">비밀번호 확인</label>
                    <div class="password-box">
                        <input :type="showConfirmPassword ? 'text' : 'password'" id="confirmPassword" v-model="confirmPassword" placeholder="비밀번호 확인"/>
                        <button type="button" class="toggle-btn" @click="toggleConfirmPassword">
                        {{ showConfirmPassword ? '숨김' : '보기' }}
                        </button>
                    </div>
                </div>

                <!-- 회원가입 버튼 -->
                <button class="register-btn" @click="register">계정 생성</button>
                
                <!-- 동의 체크박스 및 링크 -->
                <div class="options">
                    <input type="checkbox" id="terms" v-model="agree" />
                    <label for="terms" class="terms-label">
                        회원가입 시,
                        <router-link to="/terms" class="link">트립닷컴 이용약관</router-link> 및
                        <router-link to="/privacy" class="link">개인정보 정책</router-link>에 동의하시게 됩니다.
                        <br />
                        
                    </label>
                </div>

                <!-- 구분선 -->
                <div class="divider">----------------------------------------- 또는 -----------------------------------------<div>

                <!-- 소셜 로그인 -->
                <div class="social-login">
                    <div class="social-box">
                        <img src="/naverLogo.png" alt="네이버 로그인" />
                    </div>
                    <div class="social-box">
                        <img src="/googleLogo.png" alt="구글 로그인" />
                    </div>
                    <div class="social-box">
                        <img src="/kakaoLogo.png" alt="카카오 로그인" />
                    </div>
                </div>
            </div>
            </div>
        </div>
    </div>
  </div>
</template>

<style scoped src="@/assets/css/login/register.css"></style>

<script>
import http from "@/api/http";

export default {
  name: "RegisterPage",
  data() {
    return {
      firstName: "",
      lastName: "",
      email: "",
      phone: "",
      dateOfBirth: "",
      address: "",
      password: "",
      confirmPassword: "",
      agree: false,
      showPassword: false,
      showConfirmPassword: false,
    };
  },
  methods: {
    togglePassword() {
      this.showPassword = !this.showPassword;
    },
    toggleConfirmPassword() {
      this.showConfirmPassword = !this.showConfirmPassword;
    },
    async register() {
      // 필수 필드 검증
      if (!this.firstName || this.firstName.trim() === "") {
        alert("이름을 입력해주세요.");
        return;
      }
      
      if (!this.lastName || this.lastName.trim() === "") {
        alert("성을 입력해주세요.");
        return;
      }
      
      if (!this.email || this.email.trim() === "") {
        alert("이메일을 입력해주세요.");
        return;
      }
      
      if (!this.phone || this.phone.trim() === "") {
        alert("전화번호를 입력해주세요.");
        return;
      }
      
      if (!this.dateOfBirth || this.dateOfBirth.trim() === "") {
        alert("생년월일을 입력해주세요.");
        return;
      }
      
      if (!this.address || this.address.trim() === "") {
        alert("주소를 입력해주세요.");
        return;
      }
      
      if (!this.password || this.password.trim() === "") {
        alert("비밀번호를 입력해주세요.");
        return;
      }
      
      if (!this.confirmPassword || this.confirmPassword.trim() === "") {
        alert("비밀번호 확인을 입력해주세요.");
        return;
      }
      
      // 이메일 형식 검증
      const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
      if (!emailRegex.test(this.email)) {
        alert("올바른 이메일 형식을 입력해주세요.");
        return;
      }
      
      // 전화번호 형식 검증 (010-1234-5678 형태)
      const phoneRegex = /^010-\d{4}-\d{4}$/;
      if (!phoneRegex.test(this.phone)) {
        alert("올바른 전화번호 형식을 입력해주세요. (예: 010-1234-5678)");
        return;
      }
      
      // 생년월일 검증 (18세 이상)
      const birthDate = new Date(this.dateOfBirth);
      const today = new Date();
      const age = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();
      
      if (age < 18 || (age === 18 && monthDiff < 0) || (age === 18 && monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        alert("18세 이상만 회원가입이 가능합니다.");
        return;
      }
      
      // 비밀번호 최소 길이 검증
      if (this.password.length < 6) {
        alert("비밀번호는 최소 6자 이상이어야 합니다.");
        return;
      }
      
      // 약관 동의 확인
      if (!this.agree) {
        alert("약관에 동의해야 회원가입이 가능합니다.");
        return;
      }
      
      // 비밀번호 일치 확인
      if (this.password !== this.confirmPassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
      }

      try {
        const response = await http.post("/users/register", {
          name: this.lastName + this.firstName,
          email: this.email,
          password: this.password,
          phone: this.phone,
          dateOfBirth: this.dateOfBirth,
          address: this.address
        });
        alert("회원가입 성공!");
        this.$router.push("/login");
      } catch (error) {
        console.error("회원가입 실패:", error.response?.data || error.message);
        
        // 백엔드에서 온 에러 메시지가 있으면 그것을 사용, 없으면 기본 메시지
        const errorMessage = error.response?.data || "회원가입에 실패했습니다. 다시 시도해주세요.";
        alert(errorMessage);
      }
    },
  },
};
</script>
