# Foot Match - Database Plan

## 1. Diagram ERD

```
┌─────────────────────┐       ┌─────────────────────┐
│      app_user       │       │        match        │
├─────────────────────┤       ├─────────────────────┤
│ id (PK, UUID)       │       │ id (PK, UUID)       │
│ email (UNIQUE)      │       │ organizer_id (FK)   │──┐
│ password_hash       │       │ title               │  │
│ name                │◀──────│ description         │  │
│ role                │       │ location            │  │
│ created_at          │       │ match_date          │  │
│ updated_at          │       │ max_players         │  │
└─────────────────────┘       │ status              │  │
         ▲                    │ created_at          │  │
         │                    │ updated_at          │  │
         │                    └─────────────────────┘  │
         │                             ▲               │
         │                             │               │
         │                             │               │
         │    ┌────────────────────────┴───────┐      │
         │    │       match_participant        │      │
         │    ├────────────────────────────────┤      │
         │    │ id (PK, UUID)                  │      │
         └────│ player_id (FK)                 │      │
              │ match_id (FK)   ───────────────┘      │
              │ status                                │
              │ joined_at                             │
              └───────────────────────────────────────┘
```

## 2. Tabele

### 2.1 app_user

Przechowuje dane użytkowników (gracze i organizatorzy).

| Kolumna | Typ | Constraints | Opis |
|---------|-----|-------------|------|
| `id` | UUID | PK, NOT NULL, DEFAULT gen_random_uuid() | Identyfikator |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Adres email |
| `password_hash` | VARCHAR(255) | NOT NULL | Hash hasła (z Supabase Auth) |
| `name` | VARCHAR(100) | NOT NULL | Imię/nazwa użytkownika |
| `role` | VARCHAR(20) | NOT NULL | Rola: PLAYER, ORGANIZER |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT now() | Data utworzenia |
| `updated_at` | TIMESTAMPTZ | | Data ostatniej aktualizacji |

**Indeksy:**
- `uidx_app_user_email` (UNIQUE) - szybkie wyszukiwanie po email

**Uwagi:**
- Tabela nazwana `app_user` (nie `user`) - unikamy słowa kluczowego SQL
- `password_hash` może być pusty jeśli używamy tylko Supabase Auth (auth.users)

---

### 2.2 match

Przechowuje informacje o meczach.

| Kolumna | Typ | Constraints | Opis |
|---------|-----|-------------|------|
| `id` | UUID | PK, NOT NULL, DEFAULT gen_random_uuid() | Identyfikator |
| `organizer_id` | UUID | FK → app_user(id), NOT NULL | Organizator meczu |
| `title` | VARCHAR(100) | NOT NULL | Tytuł meczu |
| `description` | TEXT | | Opis meczu |
| `location` | VARCHAR(255) | NOT NULL | Lokalizacja |
| `match_date` | TIMESTAMPTZ | NOT NULL | Data i czas meczu |
| `max_players` | INTEGER | NOT NULL, CHECK (2-50) | Max liczba graczy |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'OPEN' | Status meczu |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT now() | Data utworzenia |
| `updated_at` | TIMESTAMPTZ | | Data ostatniej aktualizacji |

**Statusy meczu:**
- `OPEN` - otwarty na zgłoszenia
- `CLOSED` - zamknięty (pełny lub ręcznie)
- `CANCELLED` - anulowany
- `COMPLETED` - zakończony

**Indeksy:**
- `idx_match_organizer_id` - mecze organizatora
- `idx_match_match_date` - sortowanie po dacie
- `idx_match_status` - filtrowanie po statusie

**Foreign Keys:**
- `fk_match_app_user` → app_user(id) ON DELETE CASCADE

---

### 2.3 match_participant

Tabela łącząca graczy z meczami (uczestnictwo).

| Kolumna | Typ | Constraints | Opis |
|---------|-----|-------------|------|
| `id` | UUID | PK, NOT NULL, DEFAULT gen_random_uuid() | Identyfikator |
| `match_id` | UUID | FK → match(id), NOT NULL | Mecz |
| `player_id` | UUID | FK → app_user(id), NOT NULL | Gracz |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | Status zgłoszenia |
| `joined_at` | TIMESTAMPTZ | NOT NULL, DEFAULT now() | Data zgłoszenia |

**Statusy uczestnictwa:**
- `PENDING` - oczekuje na akceptację
- `ACCEPTED` - zaakceptowany
- `REJECTED` - odrzucony

**Indeksy:**
- `idx_match_participant_match_id` - uczestnicy meczu
- `idx_match_participant_player_id` - mecze gracza

**Unique Constraints:**
- `uq_match_participant_match_player` (match_id, player_id) - gracz może dołączyć tylko raz

**Foreign Keys:**
- `fk_match_participant_match` → match(id) ON DELETE CASCADE
- `fk_match_participant_app_user` → app_user(id) ON DELETE CASCADE

---

## 3. Enumy (jako VARCHAR)

Przechowujemy jako VARCHAR, walidacja w aplikacji Java.

### UserRole
```
PLAYER
ORGANIZER
```

### MatchStatus
```
OPEN
CLOSED
CANCELLED
COMPLETED
```

### ParticipantStatus
```
PENDING
ACCEPTED
REJECTED
```

---

## 4. Relacje

| Relacja | Typ | Opis |
|---------|-----|------|
| app_user → match | 1:N | Organizator ma wiele meczy |
| match → match_participant | 1:N | Mecz ma wielu uczestników |
| app_user → match_participant | 1:N | Gracz uczestniczy w wielu meczach |

---

## 5. Pliki migracji Liquibase

Kolejność tworzenia:

```
db/changelog/changes/
├── 202412151000_create_app_user_table.yaml
├── 202412151001_create_match_table.yaml
├── 202412151002_create_match_participant_table.yaml
└── 202412151003_create_indexes.yaml
```

---

## 6. Przykładowe zapytania

### Otwarte mecze (posortowane po dacie)
```sql
SELECT m.*, u.name as organizer_name
FROM match m
JOIN app_user u ON m.organizer_id = u.id
WHERE m.status = 'OPEN'
  AND m.match_date > NOW()
ORDER BY m.match_date ASC;
```

### Mecze gracza
```sql
SELECT m.*
FROM match m
JOIN match_participant mp ON m.id = mp.match_id
WHERE mp.player_id = :playerId
  AND mp.status = 'ACCEPTED'
ORDER BY m.match_date DESC;
```

### Liczba uczestników meczu
```sql
SELECT m.id, m.title, m.max_players,
       COUNT(mp.id) FILTER (WHERE mp.status = 'ACCEPTED') as current_players
FROM match m
LEFT JOIN match_participant mp ON m.id = mp.match_id
WHERE m.id = :matchId
GROUP BY m.id;
```

### Mecze organizatora
```sql
SELECT m.*
FROM match m
WHERE m.organizer_id = :organizerId
ORDER BY m.created_at DESC;
```

### Uczestnicy meczu
```sql
SELECT u.id, u.name, u.email, mp.status, mp.joined_at
FROM match_participant mp
JOIN app_user u ON mp.player_id = u.id
WHERE mp.match_id = :matchId
ORDER BY mp.joined_at ASC;
```

---

## 7. Supabase Auth Integration

Opcjonalnie można zintegrować z `auth.users` Supabase:

```sql
-- Synchronizacja z Supabase auth.users
-- Trigger po utworzeniu użytkownika w Supabase Auth
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.app_user (id, email, name, role)
  VALUES (
    NEW.id,
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'name', 'User'),
    COALESCE(NEW.raw_user_meta_data->>'role', 'PLAYER')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger (opcjonalnie - jeśli chcemy automatyczną synchronizację)
-- CREATE TRIGGER on_auth_user_created
--   AFTER INSERT ON auth.users
--   FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
```

**Uwaga:** W podejściu Backend-first Spring Boot sam tworzy rekordy w `app_user`.

---

## 8. Constraints i walidacje

### Na poziomie bazy danych
- `max_players` BETWEEN 2 AND 50
- `email` format (walidacja w aplikacji)
- `match_date` > NOW() przy tworzeniu (walidacja w aplikacji)

### Na poziomie aplikacji (Java)
- Gracz nie może dołączyć do własnego meczu (organizator)
- Gracz nie może dołączyć do pełnego meczu
- Tylko organizator może edytować/usuwać swój mecz
- Tylko organizator może zmieniać status uczestników

---

## 9. Przyszłe rozszerzenia (poza MVP)

```sql
-- Lokalizacja geograficzna
ALTER TABLE match ADD COLUMN coordinates POINT;
CREATE INDEX idx_match_coordinates ON match USING GIST (coordinates);

-- Oceny graczy
CREATE TABLE player_rating (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  rater_id UUID REFERENCES app_user(id),
  rated_id UUID REFERENCES app_user(id),
  match_id UUID REFERENCES match(id),
  rating INTEGER CHECK (rating BETWEEN 1 AND 5),
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE (rater_id, rated_id, match_id)
);

-- Cykliczne mecze
ALTER TABLE match ADD COLUMN recurrence_rule VARCHAR(255);
ALTER TABLE match ADD COLUMN parent_match_id UUID REFERENCES match(id);
```
