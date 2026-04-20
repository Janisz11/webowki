package pl.edu.pwr.ztw.demo.services;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.pwr.ztw.demo.model.User;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final List<User> users;

    public UserService() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        this.users = List.of(
                new User("1", "admin", encoder.encode("admin123"), "ROLE_ADMIN")
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .map(u -> org.springframework.security.core.userdetails.User.builder()
                        .username(u.getUsername())
                        .password(u.getPassword())
                        .authorities(u.getRole())
                        .build())
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
