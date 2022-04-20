package com.example.ecommercenashtechbackend.contronller.customer;

import com.example.ecommercenashtechbackend.common.RoleName;
import com.example.ecommercenashtechbackend.dto.request.RefreshTokenRequestDto;
import com.example.ecommercenashtechbackend.dto.request.UserLoginRequestDto;
import com.example.ecommercenashtechbackend.dto.request.UserRequestDto;
import com.example.ecommercenashtechbackend.dto.response.JwtResponse;
import com.example.ecommercenashtechbackend.dto.response.UserLoginResponseDto;
import com.example.ecommercenashtechbackend.dto.response.UserResponseDto;
import com.example.ecommercenashtechbackend.entity.Role;
import com.example.ecommercenashtechbackend.entity.User;
import com.example.ecommercenashtechbackend.exception.ExceptionResponse;
import com.example.ecommercenashtechbackend.exception.custom.ForbiddenException;
import com.example.ecommercenashtechbackend.security.UserDetail;
import com.example.ecommercenashtechbackend.security.jwt.JwtUtil;
import com.example.ecommercenashtechbackend.service.RoleService;
import com.example.ecommercenashtechbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final UserService userService;
    private final RoleService roleService;
    private final JwtUtil jwtUtil;


    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success, user registered",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "409", description = "Email is already in use",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Validated @RequestBody UserRequestDto userRequestDto) {
        ModelMapper modelMapper = new ModelMapper();
        Role roleUser = roleService.getRoleByName(RoleName.ROLE_USER);
        User userSave = modelMapper.map(userRequestDto, User.class);
        userSave.setRoles(Set.of(roleUser));
        userSave = userService.save(userSave);
        UserResponseDto responseDto = modelMapper.map(userSave, UserResponseDto.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Login user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success, user logged in",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Invalid email or password",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody UserLoginRequestDto userLoginDto) {
        User user = userService.login(userLoginDto.getEmail(), userLoginDto.getPassword());
        UserDetail userDetail = new UserDetail(user);
        String accessToken = jwtUtil.generateAccessToken(userDetail);
        String refreshToken = jwtUtil.generateRefreshToken(userDetail);
        ModelMapper modelMapper = new ModelMapper();
        UserLoginResponseDto userLoginResponseDto = modelMapper.map(user, UserLoginResponseDto.class);
        userLoginResponseDto.setAccessToken(accessToken);
        userLoginResponseDto.setRefreshToken(refreshToken);
        return ResponseEntity.ok(userLoginResponseDto);
    }

    @Operation(summary = "Refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success, token refreshed",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Invalid refresh token",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Validated @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        try {
            jwtUtil.validateToken(refreshTokenRequestDto.getRefreshToken());
        } catch(Exception e){
            throw new ForbiddenException(e.getMessage());
        }
        String refreshToken = refreshTokenRequestDto.getRefreshToken();
        String email = jwtUtil.getUserNameFromJwtToken(refreshToken);
        User user = userService.getUserByEmail(email);
        UserDetail userDetail = new UserDetail(user);
        String accessToken = jwtUtil.generateAccessToken(userDetail);
        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
    }
}