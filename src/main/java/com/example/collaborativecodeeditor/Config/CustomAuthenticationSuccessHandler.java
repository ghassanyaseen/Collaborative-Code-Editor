package com.example.collaborativecodeeditor.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String roleWithPrefix = authentication.getAuthorities().iterator().next().getAuthority();

        switch (roleWithPrefix) {
            case "ROLE_ADMIN":
                response.sendRedirect("/admin");
                break;
            case "ROLE_EDITOR":
                response.sendRedirect("/editor/branch-editor?");
                break;
            case "ROLE_VIEWER":
                response.sendRedirect("/viewer/branch-viewer");
                break;
            default:
                response.sendRedirect("/login");
        }
    }
}
