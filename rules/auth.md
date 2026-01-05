# Authentication & Authorization Rules - Supabase Auth (Backend-first)

## 1. Architektura

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  React Native   │────▶│  Spring Boot    │────▶│  Supabase Auth  │
│  (Expo)         │     │  (API)          │     │  (Admin API)    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        │                       ▼                       │
        │               ┌─────────────────┐             │
        │               │   PostgreSQL    │◀────────────┘
        │               │   (Supabase)    │
        │               └─────────────────┘
        │                       │
        └───────────────────────┘
              JWT Token
```

### Przepływ danych
1. Frontend wywołuje Spring Boot API (`/api/auth/*`)
2. Spring Boot komunikuje się z Supabase Auth Admin API
3. Supabase tworzy/weryfikuje użytkownika i zwraca JWT
4. Spring Boot zwraca JWT do frontendu
5. Frontend przechowuje JWT i dołącza do każdego requestu

## 2. Supabase Setup

### Wymagane dane w konfiguracji
```yaml
# application.yaml
supabase:
  url: ${SUPABASE_URL}           # https://xxx.supabase.co
  key: ${SUPABASE_ANON_KEY}      # Public anon key
  service-key: ${SUPABASE_SERVICE_KEY}  # Service role key (backend only!)
  jwt-secret: ${SUPABASE_JWT_SECRET}    # JWT secret for validation
```

### Zmienne środowiskowe
```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=eyJhbGci...
SUPABASE_SERVICE_KEY=eyJhbGci...  # NIGDY nie udostępniaj!
SUPABASE_JWT_SECRET=your-jwt-secret
```

## 3. Role użytkowników

| Rola | Opis | Uprawnienia |
|------|------|-------------|
| `PLAYER` | Gracz | Przeglądanie meczy, dołączanie |
| `ORGANIZER` | Organizator | Wszystko co PLAYER + tworzenie/zarządzanie meczami |

### Przechowywanie roli
- Rola zapisana w tabeli `app_user.role`
- Rola dodawana do JWT claims (opcjonalnie przez Supabase hooks)

## 4. Backend Implementation

### Struktura pakietów
```
pl.pzynis.footmatch/
├── domain/
│   └── model/
│       └── User.java
├── application/
│   └── service/
│       └── AuthService.java
├── infrastructure/
│   ├── auth/
│   │   ├── SupabaseAuthAdapter.java
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthenticationFilter.java
│   └── config/
│       └── SecurityConfig.java
└── api/
    ├── controller/
    │   └── AuthController.java
    └── dto/
        ├── RegisterRequest.java
        ├── LoginRequest.java
        └── AuthResponse.java
```

### AuthController
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}
```

### DTOs
```java
public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String name,
    @NotNull UserRole role  // PLAYER or ORGANIZER
) {}

public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserResponse user
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}

public record UserResponse(
    UUID id,
    String email,
    String name,
    UserRole role
) {}
```

### Supabase Auth Adapter
```java
@Component
@RequiredArgsConstructor
public class SupabaseAuthAdapter implements AuthPort {

    private final SupabaseClient supabaseClient;
    private final UserRepository userRepository;

    @Override
    public AuthResult register(String email, String password, String name, UserRole role) {
        // 1. Utwórz użytkownika w Supabase Auth
        SignUpResponse supabaseResponse = supabaseClient.auth()
            .signUp(email, password);

        if (supabaseResponse.getError() != null) {
            throw new AuthenticationException(supabaseResponse.getError().getMessage());
        }

        // 2. Utwórz rekord w app_user
        User user = new User(
            UUID.fromString(supabaseResponse.getUser().getId()),
            email,
            name,
            role
        );
        userRepository.save(user);

        // 3. Zwróć tokeny
        return new AuthResult(
            supabaseResponse.getAccessToken(),
            supabaseResponse.getRefreshToken(),
            supabaseResponse.getExpiresIn(),
            user
        );
    }

    @Override
    public AuthResult login(String email, String password) {
        SignInResponse response = supabaseClient.auth()
            .signInWithPassword(email, password);

        if (response.getError() != null) {
            throw new AuthenticationException("Invalid credentials");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));

        return new AuthResult(
            response.getAccessToken(),
            response.getRefreshToken(),
            response.getExpiresIn(),
            user
        );
    }
}
```

### JWT Validation Filter
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException(userId));

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

### Security Config
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/matches/**").permitAll()
                // Organizer only
                .requestMatchers(HttpMethod.POST, "/api/matches").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.PUT, "/api/matches/**").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.DELETE, "/api/matches/**").hasRole("ORGANIZER")
                // Authenticated users
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## 5. Frontend Implementation

### Przechowywanie tokenów
```typescript
// services/tokenStorage.ts
import * as SecureStore from 'expo-secure-store';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

export const tokenStorage = {
  async setTokens(accessToken: string, refreshToken: string) {
    await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, accessToken);
    await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, refreshToken);
  },

  async getAccessToken() {
    return SecureStore.getItemAsync(ACCESS_TOKEN_KEY);
  },

  async getRefreshToken() {
    return SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
  },

  async clearTokens() {
    await SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY);
    await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
  },
};
```

### Auth Service
```typescript
// services/authService.ts
import { api } from './api';
import { tokenStorage } from './tokenStorage';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  role: 'PLAYER' | 'ORGANIZER';
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/login', data);
    await tokenStorage.setTokens(response.accessToken, response.refreshToken);
    return response;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/register', data);
    await tokenStorage.setTokens(response.accessToken, response.refreshToken);
    return response;
  },

  async logout(): Promise<void> {
    await api.post('/api/auth/logout');
    await tokenStorage.clearTokens();
  },

  async refreshToken(): Promise<AuthResponse> {
    const refreshToken = await tokenStorage.getRefreshToken();
    const response = await api.post<AuthResponse>('/api/auth/refresh', { refreshToken });
    await tokenStorage.setTokens(response.accessToken, response.refreshToken);
    return response;
  },
};
```

### API Client z auto-refresh
```typescript
// services/api.ts
import { tokenStorage } from './tokenStorage';

const API_URL = process.env.EXPO_PUBLIC_API_URL;

export const api = {
  async request<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const accessToken = await tokenStorage.getAccessToken();

    const response = await fetch(`${API_URL}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        ...options?.headers,
      },
    });

    // Token expired - try refresh
    if (response.status === 401) {
      const newTokens = await this.refreshToken();
      if (newTokens) {
        return this.request(endpoint, options); // Retry
      }
      throw new Error('Session expired');
    }

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'API Error');
    }

    return response.json();
  },

  get: <T>(endpoint: string) => api.request<T>(endpoint),

  post: <T>(endpoint: string, data?: unknown) =>
    api.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    }),

  // ... put, delete
};
```

### Auth Context
```typescript
// stores/AuthContext.tsx
import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService, AuthResponse, LoginRequest, RegisterRequest } from '../services/authService';
import { tokenStorage } from '../services/tokenStorage';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      const token = await tokenStorage.getAccessToken();
      if (token) {
        // Validate token / fetch user
        const response = await api.get<User>('/api/users/me');
        setUser(response);
      }
    } catch {
      await tokenStorage.clearTokens();
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (data: LoginRequest) => {
    const response = await authService.login(data);
    setUser(response.user);
  };

  const register = async (data: RegisterRequest) => {
    const response = await authService.register(data);
    setUser(response.user);
  };

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{
      user,
      isLoading,
      isAuthenticated: !!user,
      login,
      register,
      logout,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
```

## 6. Endpointy Auth

| Endpoint | Metoda | Opis | Auth |
|----------|--------|------|------|
| `/api/auth/register` | POST | Rejestracja | Public |
| `/api/auth/login` | POST | Logowanie | Public |
| `/api/auth/logout` | POST | Wylogowanie | Required |
| `/api/auth/refresh` | POST | Odświeżenie tokena | Public (refresh token) |
| `/api/users/me` | GET | Dane zalogowanego usera | Required |

## 7. Autoryzacja endpointów

| Endpoint | PLAYER | ORGANIZER |
|----------|--------|-----------|
| `GET /api/matches` | ✅ | ✅ |
| `GET /api/matches/{id}` | ✅ | ✅ |
| `POST /api/matches` | ❌ | ✅ |
| `PUT /api/matches/{id}` | ❌ | ✅ (własne) |
| `DELETE /api/matches/{id}` | ❌ | ✅ (własne) |
| `POST /api/matches/{id}/join` | ✅ | ✅ |
| `DELETE /api/matches/{id}/leave` | ✅ | ✅ |

## 8. Bezpieczeństwo

### Wymagania
- Hasła min. 8 znaków (walidacja przez Supabase)
- JWT expiration: 1 godzina (access), 7 dni (refresh)
- HTTPS w produkcji
- Service key TYLKO na backendzie
- Nie loguj tokenów ani haseł

### Walidacja JWT
- Weryfikuj sygnaturę z Supabase JWT secret
- Sprawdzaj expiration
- Sprawdzaj issuer (Supabase URL)

## 9. Zależności Maven

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- HTTP Client for Supabase -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## 10. Zależności npm (Expo)

```bash
npx expo install expo-secure-store
```
