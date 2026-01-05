# Testing Rules

## 1. Strategia testowania

| Warstwa | Typ testu | Narzędzie |
|---------|-----------|-----------|
| Backend - Domain | Unit | JUnit 5 |
| Backend - Application | Unit + Integration | JUnit 5, Mockito |
| Backend - API | Integration | Spring Boot Test, MockMvc |
| Backend - E2E | End-to-end | TestContainers, REST Assured |
| Frontend | Unit | Jest, React Native Testing Library |
| Frontend - E2E | End-to-end | Detox / Maestro |

## 2. Backend (Java/Spring Boot)

### Struktura katalogów
```
src/test/java/pl/pzynis/footmatch/
├── domain/
│   └── model/           # Testy jednostkowe encji
├── application/
│   └── service/         # Testy serwisów (z mockami)
├── api/
│   └── controller/      # Testy integracyjne API
└── e2e/                 # Testy E2E
```

### Nazewnictwo testów
```java
// Używamy @DisplayName z opisem po polsku
// Metody testowe: krótkie, opisowe nazwy

@Nested
@DisplayName("register()")
class RegisterTests {

    @Test
    @DisplayName("powinien zarejestrować nowego użytkownika")
    void shouldRegisterNewUser() { }

    @Test
    @DisplayName("powinien rzucić wyjątek gdy email już istnieje")
    void shouldThrowWhenEmailAlreadyExists() { }
}

@Nested
@DisplayName("joinMatch()")
class JoinMatchTests {

    @Test
    @DisplayName("powinien pozwolić graczowi dołączyć do meczu")
    void shouldAllowPlayerToJoin() { }

    @Test
    @DisplayName("powinien rzucić wyjątek gdy mecz jest pełny")
    void shouldThrowWhenMatchIsFull() { }
}
```

### Unit test (Domain)
```java
@DisplayName("Match")
class MatchTest {

    @Test
    @DisplayName("nie powinien pozwolić na więcej graczy niż maksimum")
    void shouldNotAllowMorePlayersThanMaximum() {
        // given
        Match match = new Match("Test", LocalDateTime.now().plusDays(1), 2);
        match.addPlayer(new Player("Player 1"));
        match.addPlayer(new Player("Player 2"));

        // when & then
        assertThatThrownBy(() -> match.addPlayer(new Player("Player 3")))
            .isInstanceOf(MatchFullException.class);
    }
}
```

### Unit test (Application Service)
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchService matchService;

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("powinien utworzyć nowy mecz")
        void shouldCreateMatch() {
            // given
            CreateMatchRequest request = new CreateMatchRequest(
                "Test Match",
                "Opis",
                "Lokalizacja",
                LocalDateTime.now().plusDays(1),
                10
            );
            when(userRepository.findById(any())).thenReturn(Optional.of(organizer));
            when(matchRepository.save(any(MatchEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            MatchResponse result = matchService.create(request, ORGANIZER_ID);

            // then
            assertThat(result.title()).isEqualTo("Test Match");
            verify(matchRepository).save(any(MatchEntity.class));
        }
    }
}
```

### Integration test (API)
```java
@SpringBootTest
@AutoConfigureMockMvc
class MatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMatch_withValidRequest_shouldReturn201() throws Exception {
        // given
        CreateMatchRequest request = new CreateMatchRequest(
            "Integration Test Match",
            LocalDateTime.now().plusDays(1),
            10
        );

        // when & then
        mockMvc.perform(post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Integration Test Match"))
            .andExpect(jsonPath("$.id").isNotEmpty());
    }
}
```

### E2E test (z bazą danych)
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MatchE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullMatchLifecycle_shouldWorkCorrectly() {
        // 1. Rejestracja organizatora
        // 2. Logowanie
        // 3. Utworzenie meczu
        // 4. Rejestracja gracza
        // 5. Dołączenie do meczu
        // 6. Weryfikacja uczestnictwa
    }
}
```

## 3. Frontend (React Native)

### Struktura testów
```
__tests__/
├── components/
│   └── MatchCard.test.tsx
├── hooks/
│   └── useMatches.test.ts
└── services/
    └── matchService.test.ts
```

### Nazewnictwo
```typescript
// Format: describe (po polsku) -> it/test (po polsku)
describe('MatchCard - testy z perspektywy użytkownika', () => {
  it('użytkownik widzi wszystkie informacje o meczu', () => { });
  it('użytkownik może kliknąć w kartę aby zobaczyć szczegóły', () => { });
});

describe('MatchCard - przypadki brzegowe', () => {
  it('wyświetla mecz z pełną liczbą graczy', () => { });
});
```

### Component test
```tsx
import { render, screen, fireEvent } from '@testing-library/react-native';
import { router } from 'expo-router';
import { MatchCard } from '@/components/match/MatchCard';

describe('MatchCard - testy z perspektywy użytkownika', () => {
  const mockMatch = {
    id: 'match-123',
    title: 'Mecz na Orliku',
    location: 'Orlik Mokotow',
    matchDate: '2026-01-10T18:00:00',
    maxPlayers: 10,
    currentPlayers: 6,
    status: 'OPEN',
    organizer: { id: 'org-1', name: 'Jan Kowalski' },
  };

  it('użytkownik widzi wszystkie informacje o meczu', () => {
    render(<MatchCard match={mockMatch} />);

    expect(screen.getByText('Mecz na Orliku')).toBeOnTheScreen();
    expect(screen.getByText('Orlik Mokotow')).toBeOnTheScreen();
    expect(screen.getByText('6/10')).toBeOnTheScreen();
    expect(screen.getByText('Jan Kowalski')).toBeOnTheScreen();
  });

  it('użytkownik może kliknąć w kartę aby zobaczyć szczegóły', () => {
    render(<MatchCard match={mockMatch} />);

    const card = screen.getByText('Mecz na Orliku');
    fireEvent.press(card);

    expect(router.push).toHaveBeenCalledWith('/match/match-123');
  });
});
```

### Hook test
```tsx
import { renderHook, waitFor } from '@testing-library/react-native';
import { useMatches } from '../hooks/useMatches';

jest.mock('../services/matchService');

describe('useMatches', () => {
  it('should fetch and return matches', async () => {
    const mockMatches = [{ id: '1', title: 'Match 1' }];
    matchService.getAll.mockResolvedValue(mockMatches);

    const { result } = renderHook(() => useMatches());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.matches).toEqual(mockMatches);
  });
});
```

## 4. Reguły ogólne

### Struktura testu (AAA)
```
Arrange - przygotuj dane
Act - wykonaj akcję
Assert - sprawdź rezultat
```

### Given-When-Then (BDD)
```java
// given - warunki początkowe
Match match = createOpenMatch();

// when - akcja
match.close();

// then - weryfikacja
assertThat(match.getStatus()).isEqualTo(MatchStatus.CLOSED);
```

### Co testować
- **Zawsze:** Logikę biznesową (domain)
- **Zawsze:** Edge cases i błędy
- **Zawsze:** API endpoints (happy path + errors)
- **Opcjonalnie:** Proste gettery/settery (nie)
- **Minimum MVP:** 1 test E2E na główny flow

### Co mockować
- **Mockuj:** Zewnętrzne serwisy (API, baza danych)
- **Mockuj:** Time-sensitive operacje
- **Nie mockuj:** Testowaną klasę
- **Nie mockuj:** Proste value objects

## 5. Test Coverage

### Minimum dla MVP
- Domain layer: 80%+
- Application layer: 70%+
- API layer: główne endpoints
- Frontend: kluczowe komponenty

### Komendy
```bash
# Backend
mvn test                      # Wszystkie testy
mvn test -Dtest=MatchTest     # Konkretna klasa
mvn verify                    # + integration tests

# Frontend
npm test                      # Wszystkie testy
npm test -- --coverage        # Z coverage
npm test -- MatchCard         # Konkretny plik
```

## 6. CI/CD Integration

### GitHub Actions example
```yaml
test:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4

    # Backend tests
    - name: Run backend tests
      run: cd foot-match-api && mvn test

    # Frontend tests
    - name: Run frontend tests
      run: cd foot-match-app && npm test -- --coverage
```

## 7. Best Practices

1. **Izolacja** - każdy test niezależny od innych
2. **Powtarzalność** - ten sam wynik przy każdym uruchomieniu
3. **Szybkość** - unit testy < 100ms, integration < 5s
4. **Czytelność** - `@DisplayName` po polsku = dokumentacja
5. **Grupowanie** - używaj `@Nested` do grupowania testów per metoda
6. **Jeden assert per test** (w miarę możliwości)
7. **Nie testuj frameworka** - tylko swój kod
8. **Test-first dla bugów** - najpierw test reprodukujący
9. **Perspektywa użytkownika** - testy frontend opisują co użytkownik widzi/robi
