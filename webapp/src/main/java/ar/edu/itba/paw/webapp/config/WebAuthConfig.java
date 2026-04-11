package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import java.util.Collection;
import java.util.Collections;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class WebAuthConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userService.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("No user found with email " + username));
                
                String roleName = (user.getRole() == null) ? "ROLE_CLIENT" : "ROLE_" + user.getRole().name();
                Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(roleName));
                
                String password = user.getPassword();
                if ("__RESERVATION_PENDING_PASSWORD__".equals(password)) {
                    // Provide a valid format fake bcrypt hash so BCryptPasswordEncoder doesn't warn/crash, 
                    // but it will certainly fail to match any normal input.
                    password = "$2a$10$00000000000000000000000000000000000000000000000000000";
                }
                
                return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    password,
                    authorities
                );
            }
        };
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.userDetailsService(userDetailsServiceBean())
            .authorizeHttpRequests()
                .requestMatchers(antMatcher("/css/**"), antMatcher("/images/**")).permitAll()
                .requestMatchers(antMatcher("/login"), antMatcher("/register"), antMatcher("/create")).anonymous()
                .requestMatchers(antMatcher("/logout")).authenticated()
                .requestMatchers(antMatcher("/")).permitAll()
                .requestMatchers(antMatcher("/packs/**")).permitAll()
                .requestMatchers(antMatcher("/commerce/**")).hasRole("COMMERCE")
                .requestMatchers(antMatcher(org.springframework.http.HttpMethod.POST, "/reservations/**")).authenticated()
                .requestMatchers(antMatcher(org.springframework.http.HttpMethod.GET, "/reservations/**")).authenticated()
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
                .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(7))
            .and().csrf()
                .ignoringRequestMatchers(antMatcher("/reservations/accept"), antMatcher("/reservations/reject"));
    }
}
