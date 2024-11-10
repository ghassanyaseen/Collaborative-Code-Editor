package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.Provider;
import com.example.collaborativecodeeditor.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        // Extract username based on the OAuth provider (GitHub, Google, Facebook)
        String username = extractUsername(attributes, userRequest.getClientRegistration().getRegistrationId());

        if (username == null) {
            username = (String) attributes.get("email");
        }

        String providerID = String.valueOf(attributes.get("id"));
        Optional<User> userOptional = userService.findByUsername(username);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setProviderID(providerID);
            userService.save(user);
        } else {
            user = userService.saveNewUser(username, null,
                    mapRegistrationIdToProvider(userRequest.getClientRegistration().getRegistrationId()), providerID);
        }

        // Assign role to the user, defaulting to "VIEWER"
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + (user.getRole() != null ? user.getRole().name() : "VIEWER"));

        String primaryAttribute = getPrimaryUserAttribute(userRequest.getClientRegistration().getRegistrationId());

        return new DefaultOAuth2User(
                List.of(authority),
                oauth2User.getAttributes(),
                primaryAttribute
        );
    }

    private String extractUsername(Map<String, Object> attributes, String registrationId) {
        // Extract username depending on the OAuth provider (GitHub, Google, Facebook)
        return switch (registrationId) {
            case "github" -> (String) attributes.get("login");
            case "google" -> (String) attributes.get("email");
            case "facebook" -> (String) attributes.get("id");
            default -> null;
        };
    }

    private Provider mapRegistrationIdToProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "github" -> Provider.GITHUB;
            case "google" -> Provider.GOOGLE;
            case "facebook" -> Provider.FACEBOOK;
            default -> Provider.NORMAL;
        };
    }

    private String getPrimaryUserAttribute(String registrationId) {
        return switch (registrationId) {
            case "github" -> "login"; // GitHub uses "login"
            case "google" -> "email"; // Google uses "email"
            case "facebook" -> "id"; // Facebook uses "id"
            default -> null;
        };
    }
}
