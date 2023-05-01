package com.project.questapp.controllers;

import com.project.questapp.entities.RefreshToken;
import com.project.questapp.entities.User;
import com.project.questapp.requests.RefreshRequest;
import com.project.questapp.requests.UserRequest;
import com.project.questapp.responses.AuthResponse;
import com.project.questapp.security.JwtTokenProvider;
import com.project.questapp.services.RefreshTokenService;
import com.project.questapp.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                          UserService userService, PasswordEncoder passwordEncoder,RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody UserRequest loginRequest){
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwtToken = jwtTokenProvider.generateJwtToken(auth);
        User user = userService.getOneUserByUserName(loginRequest.getUsername());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("Bearer " + jwtToken);
        authResponse.setRefreshToken(refreshTokenService.createRefreshToken(user));
        authResponse.setUserId(user.getId());
        return authResponse;
    }


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRequest registerRequest){
        AuthResponse authResponse = new AuthResponse();
        if (userService.getOneUserByUserName(registerRequest.getUsername()) != null){
            authResponse.setMessage("Username already in use");
            return new ResponseEntity<>(authResponse, HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userService.saveOneUser(user);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwtToken = jwtTokenProvider.generateJwtToken(auth);

        authResponse.setMessage("User successfully registered");
        authResponse.setAccessToken("Bearer " + jwtToken);
        authResponse.setRefreshToken(refreshTokenService.createRefreshToken(user));
        authResponse.setUserId(user.getId());
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);

    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest refreshRequest){
        AuthResponse response = new AuthResponse();
        RefreshToken token = refreshTokenService.getByUser(refreshRequest.getUserId());
        if (token.getToken().equals(refreshRequest.getRefreshToken()) && !refreshTokenService.isRefreshTokenExpired(token))
        {
            User user = token.getUser();

            String jwtToken = jwtTokenProvider.generateJwtTokenByUserName(user.getId());
            response.setMessage("Token successfully refreshed");
            response.setAccessToken("Bearer " + jwtToken);
            response.setRefreshToken(refreshTokenService.createRefreshToken(user));
            response.setUserId(user.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);

        }else {
            response.setMessage("Refresh token is not valid");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

    }


}

//Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwiaWF0IjoxNjgyOTQ5MDYyLCJleHAiOjE2ODI5NDkwNjZ9.o56voUEVmtEoA1b6rwNhyVR44b5okVD0lVSMma_0DucV8s5TfMdBfza09JAYm2wm9puCQ5SGjjOTLO--HS82NA
//Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwiaWF0IjoxNjgyOTQ5MzE4LCJleHAiOjE2ODI5NDkzMjJ9.c_-Wtpnj1Lb5dqiIrD6-weR_bXseccNLWVNOlPM9iCgPXEAU3kY_dSsrzwDva4wBMuIx-qAn9Fv_D5u3OEIa0Q