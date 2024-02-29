package sn.youdev.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableWebSecurity
//pour proteger les methode
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig  {
    @Autowired
    private PasswordEncoder passwordEncoder;
    private ClientRegistrationRepository clientRegistrationRepository;
    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }
    /***pour personnaliser la security de spring security et base a une chaine de filtre
     * cest a nous de dire a spring security quelles sont les ressources a proteger
     * quelles sont les ressources  ne pas proteger
     * ***/

    /***#JDBC Authentication
     *  on cree deux table user et roles
     * ***/
    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {

        return new JdbcUserDetailsManager(dataSource);
    }

    /***#InMemory Authentication**/
    //@Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager(
                User.withUsername("user1").password(passwordEncoder.encode("1234")).roles("USER").build(),
                User.withUsername("user2").password(passwordEncoder.encode("1234")).roles("USER").build(),
                User.withUsername("admin").password(passwordEncoder.encode("1234")).roles("USER", "ADMIN").build()
        );
        return inMemoryUserDetailsManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //formulaire par defaut {httpSecurity.formLogin()}
        //Login personnaliser
        //httpSecurity.formLogin().loginPage("/login").defaultSuccessUrl("/").permitAll();
        httpSecurity.authorizeHttpRequests().requestMatchers("/webjars/**").permitAll();
        httpSecurity.rememberMe();
        //httpSecurity.authorizeHttpRequests().requestMatchers("/admin/**").hasRole("ADMIN");
        //httpSecurity.authorizeHttpRequests().requestMatchers("/user/**").hasRole("USER");
        httpSecurity.authorizeHttpRequests().anyRequest().authenticated();
        httpSecurity.exceptionHandling().accessDeniedPage("/notAuthorized");

        httpSecurity.oauth2Login(oauth->oauth.loginPage("/login").defaultSuccessUrl("/").permitAll() );
        httpSecurity.authorizeHttpRequests();
        httpSecurity.logout()
                    .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .logoutSuccessUrl("/").permitAll()
                           .clearAuthentication(true)
                        .deleteCookies("JSESSIONID","github_auth_token");
        httpSecurity.exceptionHandling(eh->eh.accessDeniedPage("/notAuthorized"));
        httpSecurity.csrf().disable();
        /****
         * tous les requetes demande une authentification
         * il exite plusieur strategie pour definir les user et roles avec spring security
         * #InMemory Authentication
         * #JDBC Authentication
         * #Authentication base sur userDataServices
         * ***/
        return httpSecurity.build();
    }
    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        final OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}?logoutsuccess=true");
        return oidcLogoutSuccessHandler;
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            final Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            authorities.forEach((authority) -> {
                if (authority instanceof OidcUserAuthority oidcAuth) {
                    mappedAuthorities.addAll(mapAuthorities(oidcAuth.getIdToken().getClaims()));
                    System.out.println(oidcAuth.getAttributes());
                } else if (authority instanceof OAuth2UserAuthority oauth2Auth) {
                    mappedAuthorities.addAll(mapAuthorities(oauth2Auth.getAttributes()));
                }
            });
            return mappedAuthorities;
        };
    }
    private List<SimpleGrantedAuthority> mapAuthorities(final Map<String, Object> attributes) {
        final Map<String, Object> realmAccess = ((Map<String, Object>)attributes.getOrDefault("realm_access", Collections.emptyMap()));
        final Collection<String> roles = ((Collection<String>)realmAccess.getOrDefault("roles", Collections.emptyList()));
        return roles.stream()
                .map((role) -> new SimpleGrantedAuthority(role))
                .toList();
    }

}
