# API Rules - REST Conventions

## 1. Struktura URL

### Konwencje
- Wszystkie endpointy pod `/api`
- Rzeczowniki w liczbie mnogiej: `/matches`, `/users`
- Lowercase, kebab-case dla wielu słów: `/match-participants`
- ID w ścieżce dla konkretnego zasobu: `/matches/{id}`
- Zagnieżdżanie max 2 poziomy: `/matches/{id}/participants`

### Przykłady
```
GET    /api/matches                    # Lista meczy
GET    /api/matches/{id}               # Szczegóły meczu
POST   /api/matches                    # Utwórz mecz
PUT    /api/matches/{id}               # Aktualizuj mecz
DELETE /api/matches/{id}               # Usuń mecz
GET    /api/matches/{id}/participants  # Uczestnicy meczu
POST   /api/matches/{id}/participants  # Dołącz do meczu
```

## 2. Metody HTTP

| Metoda | Użycie | Idempotentna |
|--------|--------|--------------|
| GET | Pobieranie zasobów | Tak |
| POST | Tworzenie zasobów | Nie |
| PUT | Pełna aktualizacja | Tak |
| PATCH | Częściowa aktualizacja | Tak |
| DELETE | Usuwanie zasobów | Tak |

## 3. Kody HTTP

### Success (2xx)
| Kod | Użycie |
|-----|--------|
| 200 OK | GET, PUT, PATCH - sukces |
| 201 Created | POST - zasób utworzony |
| 204 No Content | DELETE - sukces bez body |

### Client Error (4xx)
| Kod | Użycie |
|-----|--------|
| 400 Bad Request | Błędne dane wejściowe |
| 401 Unauthorized | Brak/nieprawidłowy token |
| 403 Forbidden | Brak uprawnień |
| 404 Not Found | Zasób nie istnieje |
| 409 Conflict | Konflikt (np. mecz pełny) |
| 422 Unprocessable Entity | Walidacja nie przeszła |

### Server Error (5xx)
| Kod | Użycie |
|-----|--------|
| 500 Internal Server Error | Nieoczekiwany błąd serwera |

## 4. Format odpowiedzi

### Success response
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Mecz na orliku",
  "matchDate": "2024-12-20T18:00:00Z",
  "location": "Orlik Mokotów",
  "maxPlayers": 10,
  "currentPlayers": 6,
  "status": "OPEN",
  "organizer": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Jan Kowalski"
  },
  "createdAt": "2024-12-15T10:30:00Z"
}
```

### Error response
```json
{
  "code": "MATCH_FULL",
  "message": "Match has reached maximum number of players",
  "timestamp": "2024-12-15T10:30:00Z"
}
```

### Validation error response
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2024-12-15T10:30:00Z",
  "errors": [
    {
      "field": "title",
      "message": "must not be blank"
    },
    {
      "field": "maxPlayers",
      "message": "must be at least 2"
    }
  ]
}
```

## 5. Paginacja

### Request
```
GET /api/matches?page=0&size=20&sort=matchDate,desc
```

### Response
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### Spring Boot implementation
```java
@GetMapping
public Page<MatchResponse> getMatches(
    @PageableDefault(size = 20, sort = "matchDate", direction = Sort.Direction.DESC)
    Pageable pageable
) {
    return matchService.findAll(pageable).map(this::toResponse);
}
```

## 6. Filtrowanie

### Request
```
GET /api/matches?status=OPEN&location=Warszawa&dateFrom=2024-12-15
```

### Spring Boot implementation
```java
@GetMapping
public List<MatchResponse> getMatches(
    @RequestParam(required = false) MatchStatus status,
    @RequestParam(required = false) String location,
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateFrom
) {
    return matchService.findAll(status, location, dateFrom);
}
```

## 7. Autentykacja (Supabase Auth - Backend-first)

**Architektura:** Frontend → Spring Boot → Supabase Auth

**Szczegółowa dokumentacja:** `rules/auth.md`

### JWT Bearer Token
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### Endpointy auth
```
POST /api/auth/register     # Rejestracja → Supabase Auth
POST /api/auth/login        # Logowanie → JWT tokens
POST /api/auth/refresh      # Odświeżenie access tokena
POST /api/auth/logout       # Wylogowanie
```

### Register request
```json
{
  "email": "jan@example.com",
  "password": "securePassword123",
  "name": "Jan Kowalski",
  "role": "PLAYER"
}
```

### Login request
```json
{
  "email": "jan@example.com",
  "password": "securePassword123"
}
```

### Auth response (register/login)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "jan@example.com",
    "name": "Jan Kowalski",
    "role": "PLAYER"
  }
}
```

### Refresh request
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

### Autoryzacja endpointów

| Endpoint | Public | PLAYER | ORGANIZER |
|----------|--------|--------|-----------|
| `POST /api/auth/*` | ✅ | - | - |
| `GET /api/matches` | ✅ | ✅ | ✅ |
| `GET /api/matches/{id}` | ✅ | ✅ | ✅ |
| `POST /api/matches` | ❌ | ❌ | ✅ |
| `PUT /api/matches/{id}` | ❌ | ❌ | ✅ (własne) |
| `DELETE /api/matches/{id}` | ❌ | ❌ | ✅ (własne) |
| `POST /api/matches/{id}/join` | ❌ | ✅ | ✅ |
| `DELETE /api/matches/{id}/leave` | ❌ | ✅ | ✅ |
| `GET /api/users/me` | ❌ | ✅ | ✅ |

## 8. Request DTOs

### Konwencje
- Suffix `Request` dla inputów
- Używaj Java records
- Bean Validation annotations

```java
public record CreateMatchRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    String title,

    @Size(max = 500)
    String description,

    @NotBlank(message = "Location is required")
    String location,

    @NotNull(message = "Match date is required")
    @Future(message = "Match date must be in the future")
    LocalDateTime matchDate,

    @Min(value = 2, message = "Minimum 2 players required")
    @Max(value = 50, message = "Maximum 50 players allowed")
    int maxPlayers
) {}
```

## 9. Response DTOs

### Konwencje
- Suffix `Response` dla outputów
- Nigdy nie zwracaj entity bezpośrednio
- Nie zwracaj wrażliwych danych (hasła, tokeny wewnętrzne)

```java
public record MatchResponse(
    UUID id,
    String title,
    String description,
    String location,
    LocalDateTime matchDate,
    int maxPlayers,
    int currentPlayers,
    MatchStatus status,
    UserSummaryResponse organizer,
    LocalDateTime createdAt
) {}

public record UserSummaryResponse(
    UUID id,
    String name
) {}
```

## 10. Wersjonowanie API

### Opcja 1: URL (rekomendowane dla prostoty)
```
/api/v1/matches
/api/v2/matches
```

### Opcja 2: Header
```
Accept: application/vnd.footmatch.v1+json
```

### Dla MVP
- Bez wersjonowania
- Dodaj wersjonowanie gdy pojawi się potrzeba breaking changes

## 11. CORS

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:8081") // Expo
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

## 12. Dokumentacja API (OpenAPI / Swagger)

### Zależność Maven
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.0</version>
</dependency>
```

### URL-e
| URL | Opis |
|-----|------|
| `/swagger-ui.html` | Interaktywna dokumentacja |
| `/v3/api-docs` | OpenAPI JSON spec |
| `/v3/api-docs.yaml` | OpenAPI YAML spec |

### Konfiguracja (application.yaml)
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
```

### Adnotacje kontrolera
```java
@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Match management API")
public class MatchController {

    @Operation(
        summary = "Create a new match",
        description = "Creates a new match. Only organizers can create matches."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Match created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not an organizer")
    })
    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(
        @Valid @RequestBody CreateMatchRequest request
    ) {
        // ...
    }
}
```

### Adnotacje DTO
```java
@Schema(description = "Request to create a new match")
public record CreateMatchRequest(
    @Schema(description = "Match title", example = "Mecz na orliku")
    @NotBlank String title,

    @Schema(description = "Match location", example = "Orlik Mokotów")
    @NotBlank String location,

    @Schema(description = "Match date and time", example = "2024-12-20T18:00:00")
    @NotNull @Future LocalDateTime matchDate,

    @Schema(description = "Maximum number of players", example = "10", minimum = "2", maximum = "50")
    @Min(2) @Max(50) int maxPlayers
) {}
```

### Security w Swagger
```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Foot Match API")
                .version("1.0")
                .description("API for organizing football matches"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### Reguły dokumentacji
1. **Każdy kontroler** musi mieć `@Tag` z nazwą i opisem
2. **Każdy endpoint** musi mieć `@Operation` z summary i description
3. **Każdy endpoint** musi mieć `@ApiResponses` z możliwymi kodami
4. **Każde DTO** musi mieć `@Schema` z opisami i przykładami
5. **Wrażliwe endpointy** muszą być oznaczone `@SecurityRequirement`

## 13. Rate Limiting (opcjonalnie)

### Headers
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1702648800
```

### Response 429
```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Try again in 60 seconds.",
  "timestamp": "2024-12-15T10:30:00Z"
}
```

## 14. Best Practices

1. **Konsystencja** - te same konwencje w całym API
2. **Idempotentność** - PUT i DELETE zawsze idempotentne
3. **Stateless** - nie przechowuj stanu sesji na serwerze
4. **HATEOAS** - rozważ linki do powiązanych zasobów (opcjonalne)
5. **Walidacja** - na wejściu, przed logiką biznesową
6. **Logowanie** - loguj requesty/responses (bez wrażliwych danych)
7. **Timeout** - ustawiaj sensowne timeouty
8. **Kompresja** - włącz gzip dla dużych odpowiedzi
