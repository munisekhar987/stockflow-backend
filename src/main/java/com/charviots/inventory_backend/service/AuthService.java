package com.charviots.inventory_backend.service;

import com.charviots.inventory_backend.dto.AuthResponse;
import com.charviots.inventory_backend.dto.LoginRequest;
import com.charviots.inventory_backend.dto.RegisterRequest;
import com.charviots.inventory_backend.entity.Store;
import com.charviots.inventory_backend.entity.User;
import com.charviots.inventory_backend.repository.StoreRepository;
import com.charviots.inventory_backend.repository.UserRepository;
import com.charviots.inventory_backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private static final String SUPER_ADMIN_EMAIL = "sekharmuni92@gmail.com";

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Regular users must select a store
        if (request.getStoreId() == null) {
            throw new RuntimeException("Store is required for registration");
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (!store.getEnabled()) {
            throw new RuntimeException("Selected store is disabled");
        }

        // All new registrations are USER role
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)  // Always USER
                .store(store)
                .enabled(true)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is enabled
        if (!user.getEnabled()) {
            throw new RuntimeException("Account is disabled. Please contact administrator.");
        }

        // Check if user's store is enabled (only for non-admin users)
        if (user.getRole() == User.Role.USER && user.getStore() != null) {
            if (!user.getStore().getEnabled()) {
                throw new RuntimeException("Your store is currently disabled. Please contact administrator.");
            }
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}