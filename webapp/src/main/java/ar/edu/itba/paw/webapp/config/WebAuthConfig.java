package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class WebAuthConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;

    @Autowired
    public WebAuthConfig(final UserService userService) {
        this.userService = userService;
    }

    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
                final User user = userService.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                if (user.getRole() == null) {
                    throw new UsernameNotFoundException("User " + username + " has no role assigned");
                }
                final String roleName = "ROLE_" + user.getRole().name();
                final Collection<? extends GrantedAuthority> authorities =
                        Collections.singleton(new SimpleGrantedAuthority(roleName));
                String password = user.getPassword();
                if ("__RESERVATION_PENDING_PASSWORD__".equals(password)) {
                    // A sentinel value — user cannot log in. Substitute a valid-format hash
                    // that BCryptPasswordEncoder will reject without throwing an exception.
                    password = "$2a$10$00000000000000000000000000000000000000000000000000000";
                }
                return new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        password,
                        authorities);
            }
        };
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.userDetailsService(userDetailsServiceBean())
            .authorizeHttpRequests()
                .requestMatchers(antMatcher("/css/**"), antMatcher("/images/**")).permitAll()
                .requestMatchers(antMatcher("/login"), antMatcher("/register"), antMatcher("/create")).anonymous()
                .requestMatchers(antMatcher("/logout")).authenticated()
                .requestMatchers(antMatcher("/")).permitAll()
                .requestMatchers(antMatcher(HttpMethod.GET,  "/packs/**")).permitAll()
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
                .key("resqfood-remember-me-secret-2026")
                .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(7))
            .and().csrf()
                .ignoringRequestMatchers(antMatcher("/reservations/accept"), antMatcher("/reservations/reject"));
    }
}
