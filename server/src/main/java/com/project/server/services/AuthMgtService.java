package com.project.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.server.db.models.AppUser;
import com.project.server.db.models.Department;
import com.project.server.db.models.Level;
import com.project.server.db.models.Otp;
import com.project.server.db.services.appUser.AppUserService;
import com.project.server.db.services.department.DepartmentService;
import com.project.server.db.services.level.LevelService;
import com.project.server.db.services.otp.OtpService;
import com.project.server.dtos.request.auth.*;
import com.project.server.dtos.response.AuthResponse;
import com.project.server.dtos.response.MessageResponse;
import com.project.server.enums.Role;
import com.project.server.enums.Status;
import com.project.server.exceptions.*;
import com.project.server.util.VariableUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthMgtService implements LogoutHandler {
    private final AppUserService userService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final LevelService levelService;
    private final DepartmentService departmentService;

    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;
    private final MailingMgtService mailingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${project.security.access-token.expiration}")
    private long accessTokenExpiration;
    @Value("${project.security.refresh-token.expiration}")
    private long refreshTokenExpiration;
    @Value("${project.security.reset-token.expiration}")
    private long resetTokenExpiration;
    @Value("${project.security.secret-key}")
    private String secretKey;


    public MessageResponse createUser(StaffSignUpRequest request) throws DuplicateEntryException, NotFoundException {
        if(userService.isExistingUser(request)) throw new DuplicateEntryException("This user already exists.");

        userService.save(toUserDbModel(request));

        return new MessageResponse("Registration was successful.");
    }
    public MessageResponse createUser(StudentSignUpRequest request) throws DuplicateEntryException, NotFoundException{
        if(userService.isExistingUser(request)) throw new DuplicateEntryException("This user already exists.");
        else if (userService.hasExistingCourseRepresentative(request)) throw new DuplicateEntryException(String.format("%s already has a class representative for %s level.", request.getDepartment(), request.getLevel()));

        Level level = levelService.getByLevel(request.getLevel());
        Department department = departmentService.getByName(request.getDepartment());

        userService.save(toUserDbModel(request, department, level));

        return new MessageResponse("Registration was successful.");
    }

    public MessageResponse updateCourseRepresentative(String userId, String level) throws NotFoundException {
        AppUser courseRepresentative = userService.getByUserIdOrEmail(userId, null);
        Level newLevel = levelService.getByLevel(level);

        courseRepresentative.setLevel(newLevel);

        userService.save(courseRepresentative);

        return new MessageResponse("Student info was updated successfully");
    }

    public MessageResponse deactivateCourseRepresentative(String userId) throws NotFoundException {
        AppUser courseRepresentative = userService.getByUserIdOrEmail(userId, null);
        courseRepresentative.setStatus(Status.DEACTIVATED);
        userService.save(courseRepresentative);

        return new MessageResponse("Student was successfully deactivated");
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse httpResponse) throws ForbiddenException, NotFoundException {
        AppUser user = userService.getByUserIdOrEmail(request.getUserId(), null);
        if((user.getRole() == Role.STUDENT) && user.getStatus() == Status.DEACTIVATED) throw new ForbiddenException("You are not authorized to use this application anymore.");

        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserId(),
                            request.getPassword()
                    )
            );
        }
        catch(AuthenticationException e){
            log.warn(e.getMessage());
            throw new ForbiddenException("Invalid credentials");
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        setTokenCookie(httpResponse, refreshToken, VariableUtil.REFRESH_TOKEN, refreshTokenExpiration);
        return toAuthResponse(user, accessToken);
    }

    public MessageResponse verifyEmail(VerifyEmailRequest request, HttpServletResponse response) throws NotFoundException, MailingException {
        AppUser user = userService.getByUserIdOrEmail(null, request.getUserEmail());
        String otpValue = createOtp();

        mailingService.sendOtp(user.getUserEmail(), otpValue);

        Otp otp = Otp.builder().otp(otpValue).build();
        otpService.save(otp);

        user.setOtp(otp);
        userService.save(user);

        String resetToken = generateResetToken(user);
        setTokenCookie(response, resetToken, VariableUtil.RESET_TOKEN, resetTokenExpiration);

        return new MessageResponse("Check your mail for a one time password.");
    }

    public MessageResponse verifyOtp(VerifyOtpRequest request, HttpServletRequest httpRequest) throws Exception {
        String token = getTokenFromCookie(httpRequest.getCookies(), VariableUtil.RESET_TOKEN, "Unable to verify, please try again.");
        String username;

        try{
            username = extractUsername(token);
        }
        catch(ExpiredJwtException e){
            throw new BadRequestException("You have exceeded the verification time frame, please try again.");
        }
        catch(Exception e){
            log.warn(e.getMessage());
            throw new Exception("Something went wrong, please try again");
        }

        userService.loadUserByUsername(username);

        if(!isValidOtp(request.getOtp())){
            throw new BadRequestException("OTP has expired, please try again.");
        }

        return new MessageResponse("OTP was successfully verified");
    }

    public MessageResponse resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        String token = getTokenFromCookie(httpRequest.getCookies(), VariableUtil.RESET_TOKEN, "Unable to verify, please try again.");
        String username;

        try{
            username = extractUsername(token);
        }
        catch(ExpiredJwtException e){
            throw new BadRequestException("You have exceeded the verification time frame, please try again.");
        }
        catch(Exception e){
            log.warn(e.getMessage());
            throw new Exception("Something went wrong, please try again");
        }

        AppUser user = userService.getByUserIdOrEmail(username, null);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userService.save(user);

        clearCookies(httpRequest, httpResponse);

        return new MessageResponse("Password change was successful.");
    }

    public AuthResponse refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ForbiddenException {
        String token = getTokenFromCookie(httpRequest.getCookies(), VariableUtil.REFRESH_TOKEN, "Session Expired, Please Log in.");

        String username;
        AppUser user;
        String newAccessToken;

        try{
            if(isExpiredToken(token)) throw new ForbiddenException("Session has expired, please log back in.");

            username = extractUsername(token);
            user = userService.getByUserIdOrEmail(username, null);
            newAccessToken = generateAccessToken(user);
        }
        catch(Exception e){
            clearCookies(httpRequest, httpResponse);
            throw new ForbiddenException("Session has expired, please log back in.");
        }

        return toAuthResponse(user, newAccessToken);
    }

    @SneakyThrows
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader == null || !authHeader.startsWith("Bearer ")) throw new UnauthorizedException("You are unauthorized.");
    }

    // OTP
    private String createOtp(){
        Random random = new Random();

        int otp = random.nextInt(9999) + 1000;

        return String.valueOf(otp);
    }

    private boolean isValidOtp(String otpValue) throws NotFoundException {
        Otp otp = otpService.findByOtp(otpValue);

        return Duration.between(otp.getCreatedAt(), LocalDateTime.now()).getSeconds() < otp.getExpiration();
    }


    // JWT
    public String generateAccessToken(UserDetails user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return buildToken(user, claims, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return buildToken(user, claims, refreshTokenExpiration);
    }

    public String generateResetToken(UserDetails user){
        return buildToken(user, new HashMap<>(), resetTokenExpiration);
    }

    @SuppressWarnings("unchecked")
    public List<GrantedAuthority> getAuthorities(String token){
        Claims claims = extractClaims(token);

        List<String> roles = (List<String>) claims.get("roles");

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isExpiredToken(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean userHasValidToken(UserDetails user, String token){
        return extractUsername(token).equals(user.getUsername()) && !isExpiredToken(token);
    }

    private String buildToken(
            UserDetails user,
            Map<String, Object> claims,
            long expiration
    ){
        return Jwts
                .builder()
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setSubject(user.getUsername())
                .addClaims(claims)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    private String getTokenFromCookie(Cookie[] cookies, String tokenType, String message) throws ForbiddenException {
        if(cookies == null){
            throw new ForbiddenException(message);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(tokenType))
                .findFirst()
                .map(Cookie::getValue).orElse(null);
    }

    private void setTokenCookie(HttpServletResponse response, String token, String tokenType, long expiration){
        ResponseCookie cookie = ResponseCookie.from(tokenType, token)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .maxAge(expiration)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookies(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return;

        for(Cookie cookie : request.getCookies()){
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setValue(null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    private AppUser toUserDbModel(StaffSignUpRequest request){
        return AppUser.builder()
                .userId(request.getStaffId().toUpperCase())
                .userEmail(request.getStaffEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.LECTURER)
                .phone(request.getPhone())
                .name(request.getName())
                .build();
    }
    private AppUser toUserDbModel(StudentSignUpRequest request, Department department, Level level){
        return AppUser.builder()
                .userId(request.getStudentId().toUpperCase())
                .userEmail(request.getStudentEmail())
                .department(department)
                .level(level)
                .status(Status.ACTIVATED)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();
    }

    private AuthResponse toAuthResponse(AppUser user, String token){
        String department = user.getDepartment() == null ? null : user.getDepartment().getName();
        String level = user.getLevel() == null ? null : user.getLevel().getLevel();
        return AuthResponse.builder()
                .userId(user.getUserId())
                .userEmail(user.getUserEmail())
                .department(department)
                .level(level)
                .role(user.getRole().name())
                .token(token)
                .build();
    }
}
