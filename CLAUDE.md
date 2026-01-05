# Foot Match - Instrukcje dla Claude

## O projekcie
Aplikacja mobilna do organizowania meczy piłkarskich dla amatorów w lokalnych społecznościach.

**Dokumentacja:** `.ai/prd.md`

## Struktura projektu
```
foot-match/
├── foot-match-api/    # Backend (Java, Spring Boot)
├── foot-match-app/    # Frontend (React Native, Expo)
├── rules/             # Reguły kodowania
└── .ai/               # Dokumentacja projektowa
```

## Stack technologiczny
- **Backend:** Java 25, Spring Boot 4.0, PostgreSQL (Supabase)
- **Frontend:** React Native, Expo, TypeScript
- **Autentykacja:** Supabase Auth (Backend-first) + JWT
- **Dokumentacja API:** OpenAPI 3.0 / Swagger UI
- **Migracje:** Liquibase (YAML)

## Komendy

### Backend (foot-match-api/)
```bash
mvn spring-boot:run      # Uruchom API
mvn test                 # Testy
mvn clean install        # Build
```

### Frontend (foot-match-app/)
```bash
npm install              # Instalacja zależności
npm start                # Uruchom Expo
npm run android          # Android
npm run ios              # iOS
npm test                 # Testy
```

## Reguły kodowania

### Backend
- Architektura: Hexagonal (Ports & Adapters)
- Pakiety: `domain`, `application`, `infrastructure`, `api`
- Kontrolery wywołują tylko serwisy aplikacyjne
- Dependency injection przez interfejsy
- Szczegóły: `rules/backend.md`

### Baza danych
- PostgreSQL (Supabase)
- Migracje: Liquibase YAML w `db/changelog/changes/`
- UUID jako klucze główne
- snake_case dla tabel i kolumn
- Szczegóły: `rules/liquibase.md`

### Frontend
- TypeScript (strict mode)
- Expo Router (file-based routing)
- Komponenty funkcyjne z hookami
- Szczegóły: `rules/frontend.md`

### Autentykacja
- Supabase Auth (Backend-first)
- Frontend → Spring Boot → Supabase Auth
- JWT tokens (access + refresh)
- Role: PLAYER, ORGANIZER
- Szczegóły: `rules/auth.md`

### API
- REST conventions
- Dokumentacja: Swagger UI (`/swagger-ui.html`)
- OpenAPI spec: `/v3/api-docs`
- Szczegóły: `rules/api.md`

### Testy
- Szczegóły: `rules/testing.md`

## Zasady pracy

### Rób
- Czytaj istniejący kod przed modyfikacją
- Twórz migracje Liquibase dla zmian w bazie
- Pisz testy dla logiki biznesowej
- Stosuj się do reguł w `rules/`

### Nie rób
- Nie commituj bez zgody użytkownika
- Nie modyfikuj `application.yaml` bez pytania
- Nie twórz kodu bez przeczytania PRD
- Nie używaj AI w logice biznesowej (projekt bez AI)

## Użytkownicy aplikacji
1. **Gracz** - szuka i dołącza do meczy
2. **Organizator** - tworzy i zarządza meczami
