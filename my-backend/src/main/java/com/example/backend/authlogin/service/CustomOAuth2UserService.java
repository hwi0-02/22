package com.example.backend.authlogin.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import com.example.backend.authlogin.config.OAuth2Attributes;
import com.example.backend.authlogin.domain.User;
import com.example.backend.authlogin.repository.LoginRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final LoginRepository loginRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String userNameAttributeName = userRequest.getClientRegistration()
                    .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

            OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, userNameAttributeName, oauth2User.getAttributes());
            User user = saveOrUpdate(attributes, registrationId);
            
            log.info("OAuth2 사용자 로드 완료: {}", user.getEmail());
            return new CustomOAuth2User(user, oauth2User.getAttributes(), userNameAttributeName);
            
        } catch (Exception e) {
            log.error("OAuth2 사용자 로드 중 오류 발생: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("사용자 정보 로드 실패: " + e.getMessage());
        }
    }

    private User saveOrUpdate(OAuth2Attributes attributes, String registrationId) {
        List<User> existingUsers = loginRepository.findAllByEmail(attributes.getEmail());
        
        if (!existingUsers.isEmpty()) {
            // 기존 사용자 찾음 - 간소화된 업데이트
            User mainUser = existingUsers.get(0);
            
            // 중복 계정 제거 (첫 번째 계정만 유지)
            for (int i = 1; i < existingUsers.size(); i++) {
                loginRepository.delete(existingUsers.get(i));
                log.info("중복 계정 제거: {}", existingUsers.get(i).getId());
            }
            
            // 사용자 정보 업데이트 (간소화)
            mainUser.update(attributes.getName(), null, null);
            return loginRepository.save(mainUser);
            
        } else {
            // 새 사용자 생성 (소셜 로그인)
            // 소셜 로그인 시 기본 생년월일 설정
            User newUser = User.builder()
                    .name(attributes.getName())
                    .email(attributes.getEmail())
                    .password(null) // 소셜 로그인은 비밀번호 없음
                    .phone("010-0000-0000") // 기본 전화번호
                    .address("주소 미입력") // 기본 주소
                    .dateOfBirth(java.time.LocalDate.of(1990, 1, 1)) // 기본 생년월일
                    .provider(getProviderFromRegistrationId(registrationId))
                    .build();
                    
            User savedUser = loginRepository.save(newUser);
            log.info("새 소셜 사용자 생성: {}", savedUser.getEmail());
            return savedUser;
        }
    }

    private User.Provider getProviderFromRegistrationId(String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return User.Provider.GOOGLE;
            case "naver":
                return User.Provider.NAVER;
            case "kakao":
                return User.Provider.KAKAO;
            default:
                return User.Provider.LOCAL;
        }
    }
}