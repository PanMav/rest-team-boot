package my.approach.team.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.approach.team.teaming.auth.*;
import my.approach.team.auth.*;
import my.approach.team.service.UserService;
import my.approach.team.idm.nx.s2s.client.S2STokenClient;
import my.approach.team.idm.nx.sso.sp.opensaml.saml.SamlHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@Profile("!test")
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final ObjectProvider<S2STokenClient> s2STokenClientProvider;
    private final SamlConfigurationProperties samlConfig;
    private final ObjectProvider<SamlHelper> samlHelperProvider;
    private final ObjectMapper objectMapper;
    private final UserService userService;
//    private final CorsConfigurationSource corsConfigurationSource;

    @Value("${idm.role-prefix}")
    private String rolePrefix;

    @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
    @ConditionalOnProperty(prefix = "idm.saml", name = "enabled", havingValue = "true")
    @RequiredArgsConstructor
    public static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/*.js", "/*.css", "/*.ico", "/**/*.png", "/assets/images/*.jpg", "/error", samlConfig.getSloRedirectPath());

        web.ignoring().antMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (samlConfig.isEnabled()) {
            IdmSsoAuthenticationEntryPoint idmSsoAuthenticationEntryPoint = new IdmSsoAuthenticationEntryPoint(samlConfig, samlHelperProvider.getIfAvailable());
            IdmAccessTokenAuthenticationFilter idmAccessTokenAuthenticationFilter = new IdmAccessTokenAuthenticationFilter();
            IdmVoucherTokenAuthenticationFilter idmVoucherTokenProcessingFilter = new IdmVoucherTokenAuthenticationFilter(samlConfig.getVoucherTokenAttr());
            idmAccessTokenAuthenticationFilter.setAuthenticationManager(authenticationManager());
            idmVoucherTokenProcessingFilter.setAuthenticationManager(authenticationManager());

            // @formatter:off
            http
                .authorizeRequests()
                    .antMatchers("/*.js", "/*.css", "/*.ico", "/**/*.png", "/assets/images/*.jpg", "/error", samlConfig.getSloRedirectPath())
                        .permitAll()
                    .anyRequest()
                        .authenticated()
            .and()
                .addFilterBefore(idmVoucherTokenProcessingFilter, LogoutFilter.class)
                .addFilterBefore(idmAccessTokenAuthenticationFilter, IdmVoucherTokenAuthenticationFilter.class)
                .addFilterBefore(new SamlResponseInterceptFilter(samlHelperProvider.getIfAvailable(), samlConfig), IdmAccessTokenAuthenticationFilter.class)
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .logout()
                    .clearAuthentication(true)
                    .logoutRequestMatcher(new AntPathRequestMatcher("/api/logout"))
                    .addLogoutHandler(new SamlLogoutHandler(samlConfig, samlHelperProvider.getIfAvailable()))
            .and()
                .exceptionHandling()
                    .defaultAuthenticationEntryPointFor(idmSsoAuthenticationEntryPoint, new AntPathRequestMatcher("/"))
                    .defaultAuthenticationEntryPointFor(idmSsoAuthenticationEntryPoint, new AntPathRequestMatcher("/ui/**"))
                    .defaultAuthenticationEntryPointFor(new ApiAuthenticationEntryPoint(objectMapper), new AntPathRequestMatcher("/api/**"));
            // @formatter:on
        }

        // @formatter:off
        http.httpBasic()
                .disable()
            .anonymous()
                .disable()
            .csrf()
                .disable()
            .cors()
                .disable();
//                .configurationSource(corsConfigurationSource);
        // @formatter:on
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
        if (samlConfig.isEnabled()) {
            final S2STokenClient s2STokenClient = s2STokenClientProvider.getIfAvailable();
            authenticationManagerBuilder.authenticationProvider(new TokenAuthenticationProvider(s2STokenClient, userService));
        }
    }

    @Bean
    @Primary
    public DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler() {
        final DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
        defaultWebSecurityExpressionHandler.setDefaultRolePrefix(rolePrefix);
        return defaultWebSecurityExpressionHandler;
    }
}
