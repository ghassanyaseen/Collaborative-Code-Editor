package com.example.collaborativecodeeditor.Config;

import com.example.collaborativecodeeditor.Services.CustomOAuth2UserService;
import com.example.collaborativecodeeditor.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserService userService;

    @Autowired
    public SecurityConfig(@Lazy CustomOAuth2UserService customOAuth2UserService, @Lazy UserService userService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.userService = userService;
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/","/execute", "/login", "/signup", "/oauth2/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/editor/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers("/viewer/**").hasAnyRole("ADMIN", "VIEWER")
                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "EDITOR","VIEWER")
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .successHandler(new CustomAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(new CustomAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll());

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProviders() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/login?error=true");
    }

}
