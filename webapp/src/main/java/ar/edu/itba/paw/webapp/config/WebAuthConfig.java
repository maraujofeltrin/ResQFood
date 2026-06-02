package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.auth.AuthUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebAuthConfig extends WebSecurityConfigurerAdapter {

    private static final int REMEMBER_ME_KEY_MIN_LENGTH = 32;
    private static final Set<String> WEAK_REMEMBER_ME_KEYS = Set.of(
            "resqfood-remember-me-secret",
            "PAW_REMEMBER_ME_TOKEN_KEY_FOR_PRODUCTION",
            "CHANGE_ME_WITH_A_LONG_RANDOM_SECRET");

    private final AuthUserDetailsService authUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final String rememberMeKey;
    private final int rememberMeValidityDays;
    private final String appBaseUrl;

    @Autowired
    public WebAuthConfig(
            final AuthUserDetailsService authUserDetailsService,
            final PasswordEncoder passwordEncoder,
            @Value("${security.remember-me.key}") final String rememberMeKey,
            @Value("${security.remember-me.validity-days:7}") final int rememberMeValidityDays,
            @Value("${app.base-url:}") final String appBaseUrl) {
        this.authUserDetailsService = authUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.rememberMeKey = rememberMeKey;
        this.rememberMeValidityDays = rememberMeValidityDays;
        this.appBaseUrl = appBaseUrl;
        validateRememberMeKey();
    }

    private void validateRememberMeKey() {
        final String key = rememberMeKey == null ? "" : rememberMeKey.trim();
        if (key.isEmpty() || key.length() < REMEMBER_ME_KEY_MIN_LENGTH) {
            throw new IllegalStateException(
                    "security.remember-me.key must be set to a long, random secret (min length: "
                            + REMEMBER_ME_KEY_MIN_LENGTH + ")");
        }

        if (WEAK_REMEMBER_ME_KEYS.contains(key) && !isLocalBaseUrl(appBaseUrl)) {
            throw new IllegalStateException(
                    "security.remember-me.key is set to a placeholder value; configure a strong secret for deployment");
        }
    }

    private boolean isLocalBaseUrl(final String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return false;
        }

        final String normalized = baseUrl.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("localhost") || normalized.contains("127.0.0.1");
    }


    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(authUserDetailsService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.userDetailsService(authUserDetailsService)
                .authorizeHttpRequests()
                .requestMatchers(antMatcher("/css/**"), antMatcher("/images/**"), antMatcher("/js/**"), antMatcher("/favicon.ico")).permitAll()
                .requestMatchers(antMatcher("/login"), antMatcher("/register")).anonymous()
                .requestMatchers(antMatcher("/logout")).authenticated()
                .requestMatchers(antMatcher("/")).permitAll()
                .requestMatchers(antMatcher("/password-reset/request"), antMatcher("/password-reset/change")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET, "/verify-email"), antMatcher(HttpMethod.GET, "/verify-email/resend")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.POST, "/verify-email/resend")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET, "/packs/**")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET, "/commerces/**")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.POST, "/packs/*/bid")).hasRole("CLIENT")
                .requestMatchers(antMatcher(HttpMethod.POST, "/packs/*/reserve")).hasRole("CLIENT")
                .requestMatchers(antMatcher(HttpMethod.POST, "/packs/*/commerce-review")).hasRole("CLIENT")
                .requestMatchers(antMatcher(HttpMethod.POST, "/commerces/*/commerce-review")).hasRole("CLIENT")
                .requestMatchers(antMatcher(HttpMethod.POST, "/commerces/*/favorite")).hasRole("CLIENT")
                .requestMatchers(antMatcher(HttpMethod.POST, "/packs/*/favorite")).hasRole("CLIENT")
                .requestMatchers(antMatcher(HttpMethod.POST, "/packs/**")).authenticated()
                .requestMatchers(antMatcher(HttpMethod.GET, "/favorites"), antMatcher(HttpMethod.GET, "/favorites/**")).hasRole("CLIENT")
                .requestMatchers(antMatcher("/commerce"), antMatcher("/commerce/**")).hasRole("COMMERCE")
                .requestMatchers(antMatcher("/reservations/accept"), antMatcher("/reservations/reject")).hasRole("COMMERCE")
                .requestMatchers(antMatcher(HttpMethod.POST, "/reservations/*/reject")).hasRole("COMMERCE")
                .requestMatchers(antMatcher(HttpMethod.POST, "/reservations/**")).authenticated()
                .requestMatchers(antMatcher(HttpMethod.GET, "/reservations/**")).authenticated()
                .requestMatchers(antMatcher(HttpMethod.GET, "/profile"), antMatcher(HttpMethod.GET, "/profile/settings"),
                        antMatcher(HttpMethod.GET, "/profile/change-password"), antMatcher(HttpMethod.POST, "/profile/change-password"),
                        antMatcher(HttpMethod.POST, "/profile/account"), antMatcher(HttpMethod.POST, "/profile/settings/locale")).authenticated()
                .anyRequest().authenticated()
                .and().sessionManagement()
                .invalidSessionUrl("/login?sessionExpired=true")
                .and().formLogin()
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .failureUrl("/login?error=true")
                .and().logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .and().rememberMe()
                .rememberMeParameter("rememberMe")
                .userDetailsService(authUserDetailsService)
                .key(rememberMeKey)
                .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(rememberMeValidityDays))
                .and().csrf().disable();
    }
}
