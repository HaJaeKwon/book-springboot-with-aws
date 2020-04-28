package com.jaekwon.springbootwithaws.config.auth;

import com.jaekwon.springbootwithaws.config.auth.dto.OAuthAttributes;
import com.jaekwon.springbootwithaws.config.auth.dto.SessionUser;
import com.jaekwon.springbootwithaws.domain.user.User;
import com.jaekwon.springbootwithaws.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 진행 중인 서비스를 구분하는 코
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // 로그인 진행 시에 키가 되는 필드값. primary key
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 사용자의 이름, 프로필 이미지가 변경되었다면 update
        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));

        /* SessionUser 변환하는 이유
        세션에 저장하기 위해서는 직렬화를 구현해야 한다
        Entity class는 언제든지 다른 엔티티와 관계가 형성될 수 있다
        @OneTyMany, @ManyToMany
        직렬화를 구현하면 자식 class들까지 모두 직렬화 대상이 되기에 성능이슈, 사이드 이펙이 발생 할 수 있다
        따라서 직렬화 기능을 가진 Dto를 추가로 만드는 것이 좋다
         */

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                // email 기준으로 찾아보고 없으면 entity 생성
                .orElse(attributes.toEntity());
        return userRepository.save(user);
    }
}
