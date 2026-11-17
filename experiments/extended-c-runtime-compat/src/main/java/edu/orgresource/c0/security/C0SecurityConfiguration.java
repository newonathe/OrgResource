package edu.orgresource.c0.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class C0SecurityConfiguration {
    @Bean
    PasswordEncoder c0PasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityFilterChain c0SecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/assets/**", "/error", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ORG_EDITOR")
                .requestMatchers(HttpMethod.GET, "/api/v2/csrf", "/api/v2/departments/**")
                    .hasAnyRole("ORG_EDITOR", "ORG_VIEWER")
                .requestMatchers(HttpMethod.POST, "/api/v2/departments")
                    .hasRole("ORG_EDITOR")
                .requestMatchers("/api/**").hasAnyRole("ORG_EDITOR", "ORG_VIEWER")
                .anyRequest().permitAll())
            .exceptionHandling(exceptions -> exceptions
                    .defaultAuthenticationEntryPointFor(C0SecurityResponses::unauthenticated,
                            request -> request.getServletPath().startsWith("/api/"))
                    .accessDeniedHandler(C0SecurityResponses::forbidden))
            .csrf(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
            .formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Profile("c0-test")
    UserDetailsService c0TestUsers(PasswordEncoder encoder,
            @Value("${c0.test.editor-password}") String editorPassword,
            @Value("${c0.test.viewer-password}") String viewerPassword,
            @Value("${c0.test.organization-id}") Long organizationId,
            @Value("${c0.test.other-organization-id}") Long otherOrganizationId) {
        C0UserPrincipal editor = user("c0-editor", editorPassword, organizationId, "ROLE_ORG_EDITOR", encoder);
        C0UserPrincipal viewer = user("c0-viewer", viewerPassword, organizationId, "ROLE_ORG_VIEWER", encoder);
        C0UserPrincipal other = user("c0-other", editorPassword, otherOrganizationId, "ROLE_ORG_EDITOR", encoder);
        return username -> List.of(editor, viewer, other).stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Test account is unavailable."));
    }

    @Bean
    @Profile("!c0-test")
    UserDetailsService noAccountsConfigured() {
        return username -> { throw new UsernameNotFoundException("No account store is configured in this proof runtime."); };
    }

    private static C0UserPrincipal user(String username, String password, Long organizationId,
                                        String authority, PasswordEncoder encoder) {
        return new C0UserPrincipal(username, encoder.encode(password), organizationId,
                List.of(() -> authority));
    }
}
