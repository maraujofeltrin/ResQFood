package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.auth.AuthUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import java.util.concurrent.TimeUnit;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class WebAuthConfig extends WebSecurityConfigurerAdapter {

    private final AuthUserDetailsService authUserDetailsService;
    private final String rememberMeKey;
    private final int rememberMeValidityDays;

    @Autowired
    public WebAuthConfig(
            final AuthUserDetailsService authUserDetailsService,
            @Value("${security.remember-me.key:resqfood-remember-me-secret}") final String rememberMeKey,
            @Value("${security.remember-me.validity-days:7}") final int rememberMeValidityDays) {
        this.authUserDetailsService = authUserDetailsService;
        this.rememberMeKey = rememberMeKey;
        this.rememberMeValidityDays = rememberMeValidityDays;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(antMatcher("/css/**"), antMatcher("/images/**"), antMatcher("/js/**"), antMatcher("/favicon.ico"));
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(authUserDetailsService);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.userDetailsService(authUserDetailsService)
            .authorizeHttpRequests()
                .requestMatchers(antMatcher("/login"), antMatcher("/register")).anonymous()
                .requestMatchers(antMatcher("/logout")).authenticated()
                .requestMatchers(antMatcher("/")).permitAll()
                .requestMatchers(antMatcher("/password-reset/request"), antMatcher("/password-reset/change")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET,  "/packs/**")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.POST, "/packs/*/bid")).hasRole("CLIENT")
                .requestMatchers(antMatcher(HttpMethod.POST, "/packs/**")).authenticated()
                .requestMatchers(antMatcher("/commerce"), antMatcher("/commerce/**")).hasRole("COMMERCE")
                .requestMatchers(antMatcher(HttpMethod.POST, "/reservations/**")).authenticated()
                .requestMatchers(antMatcher(HttpMethod.GET,  "/reservations/**")).authenticated()
                .anyRequest().authenticated()
            .and().formLogin()
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)
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
            .and().csrf()
                .ignoringRequestMatchers(antMatcher("/reservations/accept"), antMatcher("/reservations/reject"));
    }
}
