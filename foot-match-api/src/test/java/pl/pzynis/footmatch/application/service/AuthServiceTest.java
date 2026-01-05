package pl.pzynis.footmatch.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.pzynis.footmatch.api.dto.AuthResponse;
import pl.pzynis.footmatch.api.dto.LoginRequest;
import pl.pzynis.footmatch.api.dto.RegisterRequest;
import pl.pzynis.footmatch.domain.exception.EmailAlreadyExistsException;
import pl.pzynis.footmatch.domain.exception.InvalidCredentialsException;
import pl.pzynis.footmatch.domain.model.UserRole;
import pl.pzynis.footmatch.infrastructure.persistence.entity.UserEntity;
import pl.pzynis.footmatch.infrastructure.persistence.repository.UserRepository;
import pl.pzynis.footmatch.infrastructure.security.JwtTokenProvider;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "jan@example.com";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String NAME = "Jan Kowalski";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash(ENCODED_PASSWORD)
                .name(NAME)
                .role(UserRole.PLAYER)
                .build();
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("powinien zarejestrować nowego użytkownika")
        void shouldRegisterNewUser() {
            // given
            RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, NAME, UserRole.PLAYER);

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.generateRefreshToken(any(), anyString(), anyString())).thenReturn(REFRESH_TOKEN);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

            // when
            AuthResponse response = authService.register(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.user().email()).isEqualTo(EMAIL);
            assertThat(response.user().name()).isEqualTo(NAME);
            assertThat(response.user().role()).isEqualTo(UserRole.PLAYER);

            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository).save(any(UserEntity.class));
            verify(passwordEncoder).encode(PASSWORD);
        }

        @Test
        @DisplayName("powinien zarejestrować organizatora")
        void shouldRegisterOrganizer() {
            // given
            RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, NAME, UserRole.ORGANIZER);
            UserEntity organizer = UserEntity.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .passwordHash(ENCODED_PASSWORD)
                    .name(NAME)
                    .role(UserRole.ORGANIZER)
                    .build();

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(UserEntity.class))).thenReturn(organizer);
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.generateRefreshToken(any(), anyString(), anyString())).thenReturn(REFRESH_TOKEN);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

            // when
            AuthResponse response = authService.register(request);

            // then
            assertThat(response.user().role()).isEqualTo(UserRole.ORGANIZER);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy email już istnieje")
        void shouldThrowWhenEmailAlreadyExists() {
            // given
            RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, NAME, UserRole.PLAYER);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("powinien zalogować użytkownika z poprawnymi danymi")
        void shouldLoginWithValidCredentials() {
            // given
            LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn(ACCESS_TOKEN);
            when(jwtTokenProvider.generateRefreshToken(any(), anyString(), anyString())).thenReturn(REFRESH_TOKEN);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

            // when
            AuthResponse response = authService.login(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.user().email()).isEqualTo(EMAIL);

            verify(userRepository).findByEmail(EMAIL);
            verify(passwordEncoder).matches(PASSWORD, ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy użytkownik nie jest zarejestowany")
        void shouldThrowWhenUserNotFound() {
            // given
            LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepository).findByEmail(EMAIL);
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy hasło jest niepoprawne")
        void shouldThrowWhenPasswordInvalid() {
            // given
            LoginRequest request = new LoginRequest(EMAIL, "wrong_password");

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrong_password", ENCODED_PASSWORD)).thenReturn(false);

            // when/then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(passwordEncoder).matches("wrong_password", ENCODED_PASSWORD);
        }
    }

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTests {

        @Test
        @DisplayName("powinien odświeżyć token dla poprawnego refresh tokena")
        void shouldRefreshValidToken() {
            // given
            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.getUserIdFromToken(REFRESH_TOKEN)).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("new_access_token");
            when(jwtTokenProvider.generateRefreshToken(any(), anyString(), anyString())).thenReturn("new_refresh_token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

            // when
            AuthResponse response = authService.refreshToken(REFRESH_TOKEN);

            // then
            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo("new_access_token");
            assertThat(response.refreshToken()).isEqualTo("new_refresh_token");

            verify(jwtTokenProvider).validateToken(REFRESH_TOKEN);
            verify(jwtTokenProvider).getUserIdFromToken(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("powinien rzucić wyjątek dla niepoprawnego tokena")
        void shouldThrowForInvalidToken() {
            // given
            when(jwtTokenProvider.validateToken("invalid_token")).thenReturn(false);

            // when/then
            assertThatThrownBy(() -> authService.refreshToken("invalid_token"))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(jwtTokenProvider).validateToken("invalid_token");
            verify(userRepository, never()).findById(any());
        }

        @Test
        @DisplayName("powinien rzucić wyjątek gdy użytkownik nie istnieje")
        void shouldThrowWhenUserNotFoundForRefresh() {
            // given
            when(jwtTokenProvider.validateToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtTokenProvider.getUserIdFromToken(REFRESH_TOKEN)).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
