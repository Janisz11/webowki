package pl.edu.pwr.ztw.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pwr.ztw.demo.model.AuthRequest;
import pl.edu.pwr.ztw.demo.model.AuthResponse;
import pl.edu.pwr.ztw.demo.services.JwtService;
import pl.edu.pwr.ztw.demo.services.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
            if (passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                String token = jwtService.generateToken(userDetails.getUsername());
                return ResponseEntity.ok(new AuthResponse(token));
            }
        } catch (UsernameNotFoundException ignored) {
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
