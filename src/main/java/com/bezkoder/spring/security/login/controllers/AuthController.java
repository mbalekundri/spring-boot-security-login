package com.bezkoder.spring.security.login.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring.security.login.dto.UserDTO;
import com.bezkoder.spring.security.login.models.ERole;
import com.bezkoder.spring.security.login.models.Role;
import com.bezkoder.spring.security.login.models.User;
import com.bezkoder.spring.security.login.payload.request.LoginRequest;
import com.bezkoder.spring.security.login.payload.request.SignupRequest;
import com.bezkoder.spring.security.login.payload.response.MessageResponse;
import com.bezkoder.spring.security.login.payload.response.ResponseFactory;
import com.bezkoder.spring.security.login.payload.response.UserInfoResponse;
import com.bezkoder.spring.security.login.repository.RoleRepository;
import com.bezkoder.spring.security.login.repository.UserRepository;
import com.bezkoder.spring.security.login.security.jwt.JwtUtils;
import com.bezkoder.spring.security.login.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
//for Angular Client (withCredentials)
//@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseFactory.build(new UserInfoResponse(userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles), jwtCookie, HttpStatus.OK, "User authenticated successfully!");
  }
  
  @PostMapping("/signupUsers")
  public ResponseEntity<?> registerMultipleUsers(@Valid @RequestBody SignupRequest[] signUpRequests) {
	  
	 Arrays.asList(signUpRequests).forEach(req -> {
		 this.registerUser(req);
	 });
	  
	  return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
                         signUpRequest.getEmail(),
                         encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "admin":
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(adminRole);

          break;
        case "manager":
          Role manRole = roleRepository.findByName(ERole.ROLE_MANAGER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(manRole);

          break;
        default:
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
        }
      });
    }

    user.setRoles(roles);
    user = userRepository.save(user);
    UserDTO dto = new UserDTO();
    BeanUtils.copyProperties(user, dto);
    return ResponseFactory.build(dto, HttpStatus.CREATED, "User registered successfully!");
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseFactory.build(cookie, HttpStatus.OK, "You've been signed out!");
  }
}
