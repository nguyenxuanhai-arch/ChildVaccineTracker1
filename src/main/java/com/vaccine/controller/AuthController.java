package com.vaccine.controller;

import com.vaccine.config.JwtTokenProvider;
import com.vaccine.dto.JwtResponseDTO;
import com.vaccine.dto.LoginDTO;
import com.vaccine.dto.RegisterDTO;
import com.vaccine.entity.User;
import com.vaccine.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    // UI controllers
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/process-register")
    public String processRegistration(@Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        if (userService.existsByUsername(registerDTO.getUsername())) {
            result.rejectValue("username", "error.username", "Username is already taken");
            return "auth/register";
        }

        if (userService.existsByEmail(registerDTO.getEmail())) {
            result.rejectValue("email", "error.email", "Email is already in use");
            return "auth/register";
        }

        try {
            userService.registerUser(
                registerDTO.getUsername(),
                registerDTO.getEmail(),
                registerDTO.getPassword(),
                registerDTO.getFullName(),
                registerDTO.getPhoneNumber(),
                registerDTO.getAddress()
            );
            
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // REST API endpoints for authentication
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        
        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        return ResponseEntity.ok(new JwtResponseDTO(
            jwt,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            roles
        ));
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDTO registerDTO) {
        if (userService.existsByUsername(registerDTO.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }

        if (userService.existsByEmail(registerDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }

        User user = userService.registerUser(
            registerDTO.getUsername(),
            registerDTO.getEmail(),
            registerDTO.getPassword(),
            registerDTO.getFullName(),
            registerDTO.getPhoneNumber(),
            registerDTO.getAddress()
        );

        return ResponseEntity.ok("User registered successfully");
    }
}
