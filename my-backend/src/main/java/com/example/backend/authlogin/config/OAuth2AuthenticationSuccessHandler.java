package com.example.backend.authlogin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.backend.authlogin.service.CustomOAuth2User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            String token = jwtUtil.generateToken(oAuth2User.getUser());
            
            log.info("OAuth2 인증 성공 - 사용자: {}, 토큰 생성됨", oAuth2User.getUser().getEmail());
            
            String targetUrl = frontendUrl + "/oauth2/redirect?token=" + token;
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            log.error("OAuth2 인증 후 처리 중 오류 발생", e);
            String errorUrl = frontendUrl + "/login?error=oauth2_callback_failed";
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}