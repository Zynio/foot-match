# Liquibase Rules - PostgreSQL (Supabase)

## 1. Format plików

- **Format:** YAML only
- **Jeden changeSet per plik**
- **changeSet id:** `YYYYMMDDHHMM_<short_description>`
- **author:** `claude-code`
- **Lokalizacja:** `src/main/resources/db/changelog/changes/`
- **Nazwa pliku:** `YYYYMMDDHHMM_<short_description>.yaml`

## 2. Przykład kompletnego pliku

```yaml
databaseChangeLog:
  - changeSet:
      id: 202412151030_create_user_table
      author: claude-code
      changes:
        - createTable:
            tableName: app_user
            columns:
              - column:
                  name: id
                  type: uuid
                  constraints:
                    primaryKey: true
                    nullable: false
                  defaultValueComputed: gen_random_uuid()
              - column:
                  name: email
                  type: varchar(255)
                  constraints:
                    nullable: false
                    unique: true
              - column:
                  name: password_hash
                  type: varchar(255)
                  constraints:
                    nullable: false
              - column:
                  name: name
                  type: varchar(100)
                  constraints:
                    nullable: false
              - column:
                  name: role
                  type: varchar(20)
                  constraints:
                    nullable: false
              - column:
                  name: created_at
                  type: timestamptz
                  constraints:
                    nullable: false
                  defaultValueComputed: now()
      rollback:
        - dropTable:
            tableName: app_user
```

## 3. Typy danych PostgreSQL

| Użyj | Zamiast |
|------|---------|
| `uuid` | `bigint`, `serial` |
| `varchar(n)` | `text` (dla ograniczonej długości) |
| `text` | `clob` |
| `timestamptz` | `timestamp`, `datetime` |
| `jsonb` | `json`, `text` |
| `boolean` | `tinyint`, `bit` |
| `integer` | `int` |

**NIGDY nie używaj:** `auto_increment`, `serial` (dla PK), typów MySQL

## 4. Klucze główne (Primary Keys)

```yaml
- column:
    name: id
    type: uuid
    constraints:
      primaryKey: true
      nullable: false
    defaultValueComputed: gen_random_uuid()
```

## 5. Timestamps

```yaml
# created_at - automatyczna data utworzenia
- column:
    name: created_at
    type: timestamptz
    constraints:
      nullable: false
    defaultValueComputed: now()

# updated_at - opcjonalne, do aktualizacji
- column:
    name: updated_at
    type: timestamptz
```

## 6. Klucze obce (Foreign Keys)

### Konwencja nazewnictwa
- Format: `fk_<tabela_źródłowa>_<tabela_docelowa>`
- Przykład: `fk_match_participant_match`

### Przykład
```yaml
- column:
    name: organizer_id
    type: uuid
    constraints:
      nullable: false
      foreignKeyName: fk_match_user
      references: app_user(id)
```

### Z ON DELETE
```yaml
- addForeignKeyConstraint:
    baseTableName: match_participant
    baseColumnNames: match_id
    referencedTableName: match
    referencedColumnNames: id
    constraintName: fk_match_participant_match
    onDelete: CASCADE
```

## 7. Indeksy

### Konwencja nazewnictwa
- Zwykły indeks: `idx_<tabela>_<kolumna>`
- Unikalny indeks: `uidx_<tabela>_<kolumna>`
- Złożony indeks: `idx_<tabela>_<kolumna1>_<kolumna2>`

### Przykład
```yaml
- createIndex:
    indexName: idx_match_match_date
    tableName: match
    columns:
      - column:
          name: match_date

- createIndex:
    indexName: uidx_user_email
    tableName: app_user
    unique: true
    columns:
      - column:
          name: email
```

## 8. Enumy

### Jako VARCHAR (rekomendowane)
```yaml
- column:
    name: status
    type: varchar(20)
    constraints:
      nullable: false
    defaultValue: 'OPEN'
```

Wartości enum waliduj w aplikacji Java.

### Jako PostgreSQL ENUM (opcjonalnie)
```yaml
- sql:
    sql: CREATE TYPE match_status AS ENUM ('OPEN', 'CLOSED', 'CANCELLED', 'COMPLETED');
- column:
    name: status
    type: match_status
    constraints:
      nullable: false
    defaultValue: 'OPEN'
```

## 9. Constraints

### Unique
```yaml
- addUniqueConstraint:
    tableName: match_participant
    columnNames: match_id, player_id
    constraintName: uq_match_participant_match_player
```

### Check (opcjonalne)
```yaml
- sql:
    sql: ALTER TABLE match ADD CONSTRAINT chk_match_max_players CHECK (max_players >= 2 AND max_players <= 50);
```

## 10. Tabele łączące (Join Tables)

- Prefiks: `rel_`
- Przykład: `rel_match_participant`

```yaml
- createTable:
    tableName: rel_match_participant
    columns:
      - column:
          name: id
          type: uuid
          constraints:
            primaryKey: true
            nullable: false
          defaultValueComputed: gen_random_uuid()
      - column:
          name: match_id
          type: uuid
          constraints:
            nullable: false
            foreignKeyName: fk_rel_match_participant_match
            references: match(id)
      - column:
          name: player_id
          type: uuid
          constraints:
            nullable: false
            foreignKeyName: fk_rel_match_participant_user
            references: app_user(id)
```

## 11. Rollback

**KAŻDY changeSet MUSI mieć rollback:**

```yaml
# Dla createTable
rollback:
  - dropTable:
      tableName: nazwa_tabeli

# Dla addColumn
rollback:
  - dropColumn:
      tableName: nazwa_tabeli
      columnName: nazwa_kolumny

# Dla createIndex
rollback:
  - dropIndex:
      indexName: idx_nazwa
      tableName: nazwa_tabeli

# Dla addForeignKeyConstraint
rollback:
  - dropForeignKeyConstraint:
      baseTableName: nazwa_tabeli
      constraintName: fk_nazwa
```

## 12. Supabase specifics

- Schema: `public`
- Rozszerzenie `pgcrypto` jest dostępne (dla `gen_random_uuid()`)
- NIE zarządzaj: roles, policies, RLS (chyba że proszone)
- Unikaj reserved keywords jako nazw tabel/kolumn

## 13. Konwencje nazewnictwa

| Element | Konwencja | Przykład |
|---------|-----------|----------|
| Tabela | snake_case | `match_participant` |
| Kolumna | snake_case | `created_at` |
| Primary Key | `id` | `id` |
| Foreign Key | `<tabela>_id` | `match_id` |
| FK constraint | `fk_<src>_<dest>` | `fk_match_user` |
| Index | `idx_<tabela>_<kolumna>` | `idx_match_date` |
| Unique index | `uidx_<tabela>_<kolumna>` | `uidx_user_email` |
| Join table | `rel_<tabela1>_<tabela2>` | `rel_match_participant` |

## 14. Bezpieczeństwo

- NIGDY nie usuwaj tabel/kolumn bez wyraźnej prośby
- Zawsze definiuj NOT NULL gdzie to możliwe
- Dodawaj unique constraints dla pól biznesowo unikalnych
- Używaj ON DELETE CASCADE ostrożnie
