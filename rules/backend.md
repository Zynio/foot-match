
# Backend Rules - Hexagonal Architecture (Spring Boot)

## 1. Architektura

### Warstwy i pakiety
```
pl.pzynis.footmatch/
├── domain/                    # Logika biznesowa (czysta Java)
│   ├── model/                 # Encje domenowe
│   ├── port/                  # Interfejsy (in/out)
│   │   ├── in/               # Use case interfaces
│   │   └── out/              # Repository interfaces
│   └── exception/            # Wyjątki domenowe
├── application/              # Koordynacja use cases
│   └── service/              # Implementacje use cases
├── infrastructure/           # Implementacje zewnętrzne
│   ├── persistence/          # JPA entities, repositories
│   │   ├── entity/          # JPA @Entity classes
│   │   ├── repository/      # Spring Data repositories
│   │   └── adapter/         # Port adapters
│   └── config/              # Konfiguracja Spring
└── api/                      # Warstwa HTTP
    ├── controller/          # REST controllers
    ├── dto/                 # Request/Response DTOs
    └── exception/           # Exception handlers
```

### Zasady warstw
1. **Domain** - czysta Java, zero zależności Spring, zawiera całą logikę biznesową
2. **Application** - implementuje porty `in`, wywołuje porty `out`
3. **Infrastructure** - implementuje porty `out`, używa Spring/JPA
4. **API** - kontrolery wywołują TYLKO serwisy aplikacyjne

## 2. Spring Boot

### Dependency Injection
```java
// DOBRZE - constructor injection
@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
}

// ŹLE - field injection
@Service
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;
}
```

### DTOs jako rekordy
```java
public record CreateMatchRequest(
    @NotBlank String title,
    @NotNull LocalDateTime matchDate,
    @Min(2) @Max(30) int maxPlayers
) {}

public record MatchResponse(
    UUID id,
    String title,
    LocalDateTime matchDate,
    MatchStatus status
) {}
```

### Walidacja
```java
@PostMapping
public ResponseEntity<MatchResponse> createMatch(
    @Valid @RequestBody CreateMatchRequest request
) {
    // ...
}
```

## 3. Obsługa błędów

### Error DTO
```java
public record ErrorResponse(
    String code,
    String message,
    LocalDateTime timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now());
    }
}
```

### Custom exceptions
```java
// Domain exception
public class MatchFullException extends RuntimeException {
    public MatchFullException(UUID matchId) {
        super("Match " + matchId + " is already full");
    }
}
```

### Global exception handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MatchFullException.class)
    public ResponseEntity<ErrorResponse> handleMatchFull(MatchFullException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("MATCH_FULL", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_ERROR", message));
    }
}
```

## 4. Konwencje nazewnictwa

| Typ | Wzorzec | Przykład |
|-----|---------|----------|
| Use case port | `*UseCase` | `CreateMatchUseCase` |
| Repository port | `*Repository` | `MatchRepository` |
| Application service | `*Service` | `MatchService` |
| JPA entity | `*Entity` | `MatchEntity` |
| JPA repository | `*JpaRepository` | `MatchJpaRepository` |
| Port adapter | `*Adapter` | `MatchRepositoryAdapter` |
| Controller | `*Controller` | `MatchController` |
| Request DTO | `*Request` | `CreateMatchRequest` |
| Response DTO | `*Response` | `MatchResponse` |

## 5. Bezpieczeństwo

### Hasła
```java
// Używaj BCrypt przez PasswordEncoder
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// Nigdy nie przechowuj plain text
user.setPasswordHash(passwordEncoder.encode(rawPassword));
```

### JWT
- Przechowuj secret w konfiguracji (nie w kodzie)
- Ustawiaj rozsądny czas wygaśnięcia (np. 24h)
- Waliduj token przy każdym żądaniu

### Walidacja wejścia
- Zawsze używaj `@Valid` dla `@RequestBody`
- Sanitizuj dane przed zapisem do bazy
- Nie ufaj danym od klienta

## 6. Dobre praktyki

### Logging
```java
// DOBRZE - SLF4J
@Slf4j
public class MatchService {
    public void createMatch(...) {
        log.info("Creating match: {}", request.title());
    }
}

// ŹLE
System.out.println("Creating match");
```

### Optional
```java
// DOBRZE
return matchRepository.findById(id)
    .orElseThrow(() -> new MatchNotFoundException(id));

// ŹLE
Match match = matchRepository.findById(id).get();
```

### Streams
```java
// DOBRZE
List<MatchResponse> responses = matches.stream()
    .map(this::toResponse)
    .toList();

// ŹLE
List<MatchResponse> responses = new ArrayList<>();
for (Match match : matches) {
    responses.add(toResponse(match));
}
```

## 7. Reguły obowiązkowe

1. Logika biznesowa TYLKO w warstwie Domain
2. Application layer koordynuje use cases
3. Infrastructure implementuje porty (adaptery)
4. API layer wywołuje TYLKO serwisy aplikacyjne
5. Zmiany w bazie = migracja Liquibase w `db/changelog/changes/`
6. Dependency injection przez interfejsy/porty
7. Każdy plik w odpowiednim pakiecie
