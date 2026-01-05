package pl.pzynis.footmatch.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "super-secret-key-for-testing-purposes-minimum-256-bits-required";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000L; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "test@example.com";
    private static final String ROLE = "PLAYER";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    }

    @Nested
    @DisplayName("generateAccessToken()")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("powinien wygenerować poprawny access token")
        void shouldGenerateValidAccessToken() {
            // when
            String token = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, ROLE);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT format: header.payload.signature
        }

        @Test
        @DisplayName("powinien wygenerować różne tokeny dla różnych użytkowników")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            // given
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            // when
            String token1 = jwtTokenProvider.generateAccessToken(userId1, "user1@example.com", ROLE);
            String token2 = jwtTokenProvider.generateAccessToken(userId2, "user2@example.com", ROLE);

            // then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("generateRefreshToken()")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("powinien wygenerować poprawny refresh token")
        void shouldGenerateValidRefreshToken() {
            // when
            String token = jwtTokenProvider.generateRefreshToken(USER_ID, EMAIL, ROLE);

            // then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("access i refresh token powinny być różne")
        void shouldGenerateDifferentAccessAndRefreshTokens() {
            // when
            String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, ROLE);
            String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, EMAIL, ROLE);

            // then
            assertThat(accessToken).isNotEqualTo(refreshToken);
        }
    }

    @Nested
    @DisplayName("validateToken()")
    class ValidateTokenTests {

        @Test
        @DisplayName("powinien zwrócić true dla poprawnego tokena")
        void shouldReturnTrueForValidToken() {
            // given
            String token = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, ROLE);

            // when
            boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("powinien zwrócić false dla niepoprawnego tokena")
        void shouldReturnFalseForInvalidToken() {
            // when
            boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("powinien zwrócić false dla pustego tokena")
        void shouldReturnFalseForEmptyToken() {
            // when
            boolean isValid = jwtTokenProvider.validateToken("");

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("powinien zwrócić false dla null tokena")
        void shouldReturnFalseForNullToken() {
            // when
            boolean isValid = jwtTokenProvider.validateToken(null);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("powinien zwrócić false dla tokena z innym sekretem")
        void shouldReturnFalseForTokenWithDifferentSecret() {
            // given
            JwtTokenProvider otherProvider = new JwtTokenProvider(
                    "different-secret-key-for-testing-purposes-minimum-256-bits",
                    ACCESS_TOKEN_EXPIRATION,
                    REFRESH_TOKEN_EXPIRATION
            );
            String token = otherProvider.generateAccessToken(USER_ID, EMAIL, ROLE);

            // when
            boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("powinien zwrócić false dla wygasłego tokena")
        void shouldReturnFalseForExpiredToken() {
            // given
            JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1000L, -1000L);
            String expiredToken = expiredProvider.generateAccessToken(USER_ID, EMAIL, ROLE);

            // when
            boolean isValid = jwtTokenProvider.validateToken(expiredToken);

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("getUserIdFromToken()")
    class GetUserIdFromTokenTests {

        @Test
        @DisplayName("powinien zwrócić poprawne userId z tokena")
        void shouldReturnCorrectUserId() {
            // given
            String token = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, ROLE);

            // when
            UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

            // then
            assertThat(extractedUserId).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("powinien zwrócić różne userId dla różnych tokenów")
        void shouldReturnDifferentUserIdsForDifferentTokens() {
            // given
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            String token1 = jwtTokenProvider.generateAccessToken(userId1, "user1@example.com", ROLE);
            String token2 = jwtTokenProvider.generateAccessToken(userId2, "user2@example.com", ROLE);

            // when
            UUID extracted1 = jwtTokenProvider.getUserIdFromToken(token1);
            UUID extracted2 = jwtTokenProvider.getUserIdFromToken(token2);

            // then
            assertThat(extracted1).isEqualTo(userId1);
            assertThat(extracted2).isEqualTo(userId2);
            assertThat(extracted1).isNotEqualTo(extracted2);
        }
    }

    @Nested
    @DisplayName("getRoleFromToken()")
    class GetRoleFromTokenTests {

        @Test
        @DisplayName("powinien zwrócić poprawną rolę PLAYER z tokena")
        void shouldReturnCorrectPlayerRole() {
            // given
            String token = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, "PLAYER");

            // when
            String role = jwtTokenProvider.getRoleFromToken(token);

            // then
            assertThat(role).isEqualTo("PLAYER");
        }

        @Test
        @DisplayName("powinien zwrócić poprawną rolę ORGANIZER z tokena")
        void shouldReturnCorrectOrganizerRole() {
            // given
            String token = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, "ORGANIZER");

            // when
            String role = jwtTokenProvider.getRoleFromToken(token);

            // then
            assertThat(role).isEqualTo("ORGANIZER");
        }
    }

    @Nested
    @DisplayName("getAccessTokenExpiration()")
    class GetAccessTokenExpirationTests {

        @Test
        @DisplayName("powinien zwrócić skonfigurowany czas wygaśnięcia")
        void shouldReturnConfiguredExpiration() {
            // when
            long expiration = jwtTokenProvider.getAccessTokenExpiration();

            // then
            assertThat(expiration).isEqualTo(ACCESS_TOKEN_EXPIRATION);
        }
    }

    @Nested
    @DisplayName("Token consistency")
    class TokenConsistencyTests {

        @Test
        @DisplayName("refresh token powinien zawierać te same dane co access token")
        void shouldContainSameDataInAccessAndRefreshToken() {
            // given
            String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, ROLE);
            String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, EMAIL, ROLE);

            // when
            UUID accessUserId = jwtTokenProvider.getUserIdFromToken(accessToken);
            UUID refreshUserId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            String accessRole = jwtTokenProvider.getRoleFromToken(accessToken);
            String refreshRole = jwtTokenProvider.getRoleFromToken(refreshToken);

            // then
            assertThat(accessUserId).isEqualTo(refreshUserId);
            assertThat(accessRole).isEqualTo(refreshRole);
        }

        @Test
        @DisplayName("wygenerowany token powinien być walidowalny")
        void generatedTokenShouldBeValidatable() {
            // given
            String accessToken = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, ROLE);
            String refreshToken = jwtTokenProvider.generateRefreshToken(USER_ID, EMAIL, ROLE);

            // then
            assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
            assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        }
    }
}
