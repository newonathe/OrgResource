package edu.orgresource.extended.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800)
public class C1SecurityConfiguration {
    @Bean
    CookieSerializer c1SessionCookieSerializer(
            @Value("${c1.session.cookie-name:__Host-ORGSESSION}") String cookieName,
            @Value("${c1.session.cookie-secure:true}") boolean secure) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(cookieName);
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(secure);
        serializer.setSameSite("Lax");
        serializer.setUseBase64Encoding(false);
        return serializer;
    }

    @Bean
    PasswordEncoder c1PasswordEncoder() { return new BCryptPasswordEncoder(11); }

    @Bean
    AuthenticationManager c1AuthenticationManager(C1DatabaseUserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    CsrfTokenRepository c1CsrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        repository.setParameterName("_csrf");
        return repository;
    }

    @Bean
    SecurityContextRepository c1SecurityContextRepository() { return new HttpSessionSecurityContextRepository(); }

    @Bean
    FilterRegistrationBean<C1AccountSessionFilter> c1AccountSessionFilterRegistration(C1AccountSessionFilter filter) {
        FilterRegistrationBean<C1AccountSessionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<C1SameOriginFilter> c1SameOriginFilterRegistration(C1SameOriginFilter filter) {
        FilterRegistrationBean<C1SameOriginFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SessionAuthenticationStrategy c1SessionAuthenticationStrategy() { return new ChangeSessionIdAuthenticationStrategy(); }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    SecurityFilterChain c1SecurityFilterChain(HttpSecurity http,
            CsrfTokenRepository csrfRepository,
            SecurityContextRepository contextRepository,
            C1SameOriginFilter sameOriginFilter,
            C1AccountSessionFilter accountSessionFilter) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/assets/**", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/api/v2/auth/csrf", "/api/v2/auth/login", "/api/v2/auth/recover",
                        "/api/v2/invitations/redeem").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(C1SecurityResponses::unauthenticated,
                        request -> C1RequestPaths.path(request).startsWith("/api/"))
                .accessDeniedHandler(C1SecurityResponses::forbidden))
            .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository)
                    .csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler()))
            .securityContext(context -> context.securityContextRepository(contextRepository).requireExplicitSave(true))
            .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
            .requestCache(cache -> cache.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .addFilterBefore(sameOriginFilter, CsrfFilter.class)
            .addFilterAfter(accountSessionFilter, SecurityContextHolderFilter.class);
        return http.build();
    }
}
