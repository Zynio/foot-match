# Foot Match - Product Requirements Document (PRD)

## 1. Przegląd produktu

**Foot Match** to aplikacja mobilna umożliwiająca organizowanie meczy piłkarskich dla amatorów w lokalnych społecznościach. Aplikacja łączy graczy szukających rozgrywek z organizatorami tworzącymi mecze.

### Cel
Ułatwienie organizacji spotkań sportowych w lokalnych społecznościach poprzez cyfrową platformę łączącą graczy i organizatorów.

### Główne założenia
- Prostota użytkowania
- Skupienie na lokalnych społecznościach
- Brak sztucznej inteligencji - prosty, przewidywalny system

## 2. Użytkownicy

### 2.1 Gracz
- Szuka meczy w swojej okolicy
- Dołącza do istniejących meczy
- Przegląda historię swoich rozgrywek
- Zarządza swoim profilem

### 2.2 Organizator
- Tworzy nowe mecze
- Określa szczegóły: miejsce, data, liczba graczy
- Zarządza listą uczestników (akceptacja/odrzucenie)
- Może anulować lub edytować mecz

## 3. Funkcjonalności

### 3.1 Kontrola dostępu (Autentykacja)

**Technologia:** Supabase Auth (Backend-first)

**Architektura:**
```
React Native → Spring Boot API → Supabase Auth → PostgreSQL
```

| Funkcja | Opis |
|---------|------|
| Rejestracja | Email + hasło, wybór roli (gracz/organizator) |
| Logowanie | Email + hasło → JWT token |
| Wylogowanie | Unieważnienie sesji |
| Odświeżenie tokena | Refresh token → nowy access token |

**Role (RBAC):**
- `PLAYER` - przeglądanie meczy, dołączanie
- `ORGANIZER` - wszystko co PLAYER + tworzenie/zarządzanie meczami

**Szczegóły:** `rules/auth.md`

### 3.2 Zarządzanie danymi (CRUD)

#### Mecze
| Operacja | Gracz | Organizator |
|----------|-------|-------------|
| Create | - | Tworzy mecz |
| Read | Przegląda dostępne mecze | Przegląda swoje mecze |
| Update | - | Edytuje szczegóły meczu |
| Delete | - | Usuwa/anuluje mecz |

#### Uczestnictwo w meczach
| Operacja | Gracz | Organizator |
|----------|-------|-------------|
| Create | Zgłasza się do meczu | - |
| Read | Lista swoich meczy | Lista uczestników |
| Update | - | Akceptuje/odrzuca graczy |
| Delete | Wycofuje się z meczu | Usuwa gracza |

#### Profil użytkownika
| Operacja | Opis |
|----------|------|
| Create | Przy rejestracji |
| Read | Podgląd profilu |
| Update | Edycja danych osobowych |
| Delete | Usunięcie konta |

### 3.3 Logika biznesowa

1. **Limity uczestników** - mecz ma określoną max. liczbę graczy
2. **Status meczu** - otwarty/zamknięty/anulowany/zakończony
3. **Walidacja terminów** - nie można utworzyć meczu w przeszłości
4. **Automatyczne zamykanie** - mecz zamyka się po osiągnięciu limitu graczy
5. **Powiadomienia** - informowanie o zmianach w meczu (opcjonalnie)

## 4. Struktura danych

### 4.1 User (Użytkownik)
```
- id: UUID
- email: string (unique)
- password_hash: string
- name: string
- role: enum (PLAYER, ORGANIZER)
- created_at: timestamp
```

### 4.2 Match (Mecz)
```
- id: UUID
- organizer_id: UUID (FK -> User)
- title: string
- description: string (optional)
- location: string
- match_date: timestamp
- max_players: integer
- status: enum (OPEN, CLOSED, CANCELLED, COMPLETED)
- created_at: timestamp
```

### 4.3 MatchParticipant (Uczestnictwo)
```
- id: UUID
- match_id: UUID (FK -> Match)
- player_id: UUID (FK -> User)
- status: enum (PENDING, ACCEPTED, REJECTED)
- joined_at: timestamp
```

## 5. Wymagania techniczne

### 5.1 Stack technologiczny
- **Backend**: Java 25, Spring Boot 4.0, PostgreSQL (Supabase)
- **Frontend**: React Native, Expo, TypeScript
- **Autentykacja**: Supabase Auth (Backend-first) + JWT
- **Dokumentacja API**: OpenAPI 3.0 / Swagger UI
- **Architektura**: Hexagonal Architecture (Ports & Adapters)

### 5.2 Testy
- Minimum jeden test E2E weryfikujący flow użytkownika
- Testy jednostkowe dla logiki biznesowej
- Testy integracyjne dla API

### 5.3 CI/CD Pipeline
- Automatyczne budowanie aplikacji
- Uruchamianie testów przy każdym uchu
- Linting i statyczna analiza kodu

## 6. API Endpoints (planowane)

### Autentykacja (Supabase Auth via Backend)
```
POST /api/auth/register - rejestracja (email, hasło, imię, rola)
POST /api/auth/login - logowanie → JWT tokens
POST /api/auth/logout - wylogowanie
POST /api/auth/refresh - odświeżenie access tokena
```

### Użytkownicy
```
GET /api/users/me - profil zalogowanego użytkownika
PUT /api/users/me - aktualizacja profilu
DELETE /api/users/me - usunięcie konta
```

### Mecze
```
GET /api/matches - lista meczy (z filtrami)
GET /api/matches/{id} - szczegóły meczu
POST /api/matches - utworzenie meczu (organizator)
PUT /api/matches/{id} - edycja meczu (organizator)
DELETE /api/matches/{id} - usunięcie meczu (organizator)
```

### Uczestnictwo
```
POST /api/matches/{id}/join - dołączenie do meczu
DELETE /api/matches/{id}/leave - opuszczenie meczu
GET /api/matches/{id}/participants - lista uczestników
PUT /api/matches/{id}/participants/{playerId} - zmiana statusu (organizator)
```

## 7. Ekrany aplikacji mobilnej (planowane)

1. **Ekran logowania** - email/hasło, link do rejestracji
2. **Ekran rejestracji** - formularz, wybór roli
3. **Lista meczy** - dostępne mecze w okolicy
4. **Szczegóły meczu** - info, lista graczy, przycisk dołącz
5. **Tworzenie meczu** - formularz (tylko organizator)
6. **Moje mecze** - lista meczy użytkownika
7. **Profil** - dane użytkownika, wylogowanie

## 8. Kryteria sukcesu MVP

- [ ] Użytkownik może się zarejestrować i zalogować
- [ ] Organizator może utworzyć mecz
- [ ] Gracz może przeglądać i dołączać do meczy
- [ ] Organizator może zarządzać uczestnikami
- [ ] Działa co najmniej jeden test E2E
- [ ] Pipeline CI/CD buduje i testuje aplikację
- [ ] Dokumentacja API dostępna przez Swagger UI

## 9. Przyszłe rozszerzenia (poza MVP)

- Geolokalizacja i mapa meczy
- System ocen graczy
- Chat między uczestnikami
- Powiadomienia push
- Integracja z kalendarzem
- Cykliczne mecze
