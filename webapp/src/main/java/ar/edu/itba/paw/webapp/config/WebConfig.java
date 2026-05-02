package ar.edu.itba.paw.webapp.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.sql.DataSource;
import java.util.Properties;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.TimeZone;

import java.time.ZoneId;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebMvc
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@ComponentScan({ "ar.edu.itba.paw.webapp.controller", "ar.edu.itba.paw.webapp.validation", "ar.edu.itba.paw.services", "ar.edu.itba.paw.persistence", "ar.edu.itba.paw.webapp.config", "ar.edu.itba.paw.webapp.auth", "ar.edu.itba.paw.webapp.assembler", "ar.edu.itba.paw.webapp.controller.advice" })
@Configuration
@PropertySource("classpath:/env.properties")
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    static {
        // pgjdbc uses JVM default TimeZone in the startup packet; must be a name PostgreSQL accepts.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    /**
     * App display / business wall-clock zone (e.g. auction end date/time, commerce hours).
     */
    @Bean
    public ZoneId businessZone(@Value("${app.display-zone:}") final String displayZone) {
        return (displayZone == null || displayZone.trim().isEmpty())
                ? ZoneId.of("America/Argentina/Buenos_Aires")
                : ZoneId.of(displayZone.trim());
    }

    @Bean
    public LocaleResolver localeResolver(final DatabaseAwareLocaleResolver databaseAwareLocaleResolver) {
        return databaseAwareLocaleResolver;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        final LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        registry.addInterceptor(interceptor);
        registry.addInterceptor(requestLoggingInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("/css/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("/images/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("/js/");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(5 * 1024 * 1024); // 5 MB
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource());
        return validator;
    }

    @Override
    public Validator getValidator() {
        return localValidatorFactoryBean();
    }

    @Bean
    public DataSource dataSource(
            @Value("${db.url}") final String url,
            @Value("${db.username}") final String username,
            @Value("${db.password}") final String password) {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.postgresql.Driver.class);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JavaMailSender javaMailSender(@Value("${mail.username}") final String username,
            @Value("${mail.password}") final String password) {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        final Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        return mailSender;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(final DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    public ViewResolver viewResolver() {
        final InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/jsp/views/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Bean
    public MessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new
        ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        messageSource.setCacheSeconds(5);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

}
