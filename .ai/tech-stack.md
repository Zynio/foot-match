# Foot Match - Tech Stack

## Przegląd

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND                                  │
│  React Native + Expo + TypeScript                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ REST API (JSON)
                              │ JWT Bearer Token
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        BACKEND                                   │
│  Java 25 + Spring Boot 4.0 + Spring Security                    │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
              ▼               ▼               ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   PostgreSQL    │ │  Supabase Auth  │ │    Liquibase    │
│   (Supabase)    │ │  (JWT tokens)   │ │   (migrations)  │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

---

## 1. Backend

### Język i runtime
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **Java** | 25 | Język programowania |
| **Maven** | 3.9+ | Build tool, dependency management |

### Framework
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **Spring Boot** | 4.0.0 | Framework aplikacyjny |
| **Spring Web** | - | REST API, kontrolery |
| **Spring Security** | - | Autentykacja, autoryzacja |
| **Spring Data JPA** | - | ORM, repozytoria |
| **Spring Validation** | - | Bean Validation |

### Baza danych
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **PostgreSQL** | 15+ | Baza danych (Supabase hosted) |
| **Liquibase** | 4.x | Migracje bazodanowe (YAML) |
| **HikariCP** | - | Connection pooling |

### Autentykacja
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **Supabase Auth** | - | Zarządzanie użytkownikami |
| **JWT (jjwt)** | 0.12.3 | Walidacja tokenów |
| **BCrypt** | - | Hashowanie haseł |

### Utilities
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **Lombok** | 1.18+ | Redukcja boilerplate |
| **SLF4J + Logback** | - | Logging |
| **Jackson** | - | JSON serialization |
| **WebFlux** | - | HTTP client (Supabase API) |

### Dokumentacja API
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **SpringDoc OpenAPI** | 2.8+ | Generowanie OpenAPI spec |
| **Swagger UI** | - | Interaktywna dokumentacja |
| **OpenAPI 3.0** | - | Specyfikacja API |

### Testowanie
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **JUnit 5** | - | Testy jednostkowe |
| **Mockito** | - | Mockowanie |
| **Spring Boot Test** | - | Testy integracyjne |
| **TestContainers** | - | Testy z prawdziwą bazą |
| **REST Assured** | - | Testy API |

---

## 2. Frontend

### Język i runtime
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **TypeScript** | 5.9+ | Język programowania |
| **Node.js** | 20+ | Runtime (development) |
| **npm** | 10+ | Package manager |

### Framework
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **React** | 19.1+ | UI library |
| **React Native** | 0.81+ | Mobile framework |
| **Expo** | 54+ | Development platform |
| **Expo Router** | 5+ | File-based navigation |

### State Management
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **React Context** | - | Globalny stan (auth) |
| **useState/useReducer** | - | Lokalny stan |

### Networking
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **Fetch API** | - | HTTP requests |

### Storage & Security
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **expo-secure-store** | - | Bezpieczne przechowywanie tokenów |

### UI Components
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **React Native core** | - | Podstawowe komponenty |
| **React Navigation** | - | Bottom tabs |
| **React Native Reanimated** | - | Animacje |

### Testowanie
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **Jest** | - | Test runner |
| **React Native Testing Library** | - | Component testing |
| **Detox / Maestro** | - | E2E testing (opcjonalnie) |

### Development Tools
| Technologia | Wersja | Opis |
|-------------|--------|------|
| **ESLint** | - | Linting |
| **Prettier** | - | Formatowanie kodu |
| **Expo Go** | - | Development preview |

---

## 3. Baza danych

### PostgreSQL (Supabase)
| Aspekt | Wartość |
|--------|---------|
| **Provider** | Supabase |
| **Wersja** | PostgreSQL 15 |
| **Region** | EU (Frankfurt) |
| **Schema** | public |

### Rozszerzenia
- `pgcrypto` - gen_random_uuid()
- `uuid-ossp` - UUID generation (backup)

### Typy danych
| Typ | Użycie |
|-----|--------|
| `UUID` | Primary keys |
| `VARCHAR(n)` | Stringi z limitem |
| `TEXT` | Długie teksty |
| `TIMESTAMPTZ` | Daty z timezone |
| `INTEGER` | Liczby całkowite |
| `BOOLEAN` | Flagi |

---

## 4. Autentykacja (Supabase Auth)

### Architektura: Backend-first
```
Mobile App → Spring Boot API → Supabase Auth API → PostgreSQL
```

### Funkcje
| Funkcja | Status |
|---------|--------|
| Email/Password | ✅ MVP |
| JWT Access Token | ✅ MVP |
| Refresh Token | ✅ MVP |
| Password Reset | ⏳ Post-MVP |
| Social Login (Google) | ⏳ Post-MVP |

### Token Configuration
| Parametr | Wartość |
|----------|---------|
| Access Token TTL | 1 godzina |
| Refresh Token TTL | 7 dni |
| Algorithm | HS256 |

---

## 5. DevOps & CI/CD

### Version Control
| Technologia | Opis |
|-------------|------|
| **Git** | Version control |
| **GitHub** | Repository hosting |

### CI/CD Pipeline
| Technologia | Opis |
|-------------|------|
| **GitHub Actions** | CI/CD automation |

### Pipeline Steps
```yaml
Backend:
  - Checkout
  - Setup Java 25
  - Maven build
  - Run tests
  - (Deploy to Heroku/Railway - opcjonalnie)

Frontend:
  - Checkout
  - Setup Node.js
  - npm install
  - npm test
  - Expo build (EAS)
```

### Environments
| Środowisko | Backend | Database |
|------------|---------|----------|
| **Development** | localhost:8080 | Supabase (dev project) |
| **Production** | TBD | Supabase (prod project) |

---

## 6. Architektura kodu

### Backend - Hexagonal Architecture
```
pl.pzynis.footmatch/
├── domain/           # Czysta Java, logika biznesowa
├── application/      # Use cases, serwisy
├── infrastructure/   # JPA, Supabase adapter
└── api/              # REST controllers, DTOs
```

### Frontend - Feature-based
```
foot-match-app/
├── app/              # Expo Router screens
├── components/       # Reużywalne komponenty
├── hooks/            # Custom hooks
├── services/         # API calls
├── stores/           # State management
└── types/            # TypeScript types
```

---

## 7. Zależności Maven (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-core</artifactId>
    </dependency>

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

    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.8.0</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 8. Zależności npm (package.json)

```json
{
  "dependencies": {
    "expo": "~54.0.0",
    "expo-router": "~5.0.0",
    "expo-secure-store": "~14.0.0",
    "react": "19.1.0",
    "react-native": "0.81.0",
    "@react-navigation/bottom-tabs": "^7.0.0",
    "react-native-reanimated": "~3.17.0",
    "react-native-safe-area-context": "~5.4.0",
    "react-native-screens": "~4.10.0"
  },
  "devDependencies": {
    "@types/react": "~19.1.0",
    "typescript": "~5.9.0",
    "eslint": "^9.0.0",
    "jest": "^29.0.0",
    "@testing-library/react-native": "^12.0.0"
  }
}
```

---

## 9. Wymagania systemowe

### Development
| Narzędzie | Wersja |
|-----------|--------|
| JDK | 25+ |
| Node.js | 20+ |
| npm | 10+ |
| Maven | 3.9+ |
| Git | 2.40+ |

### IDE (rekomendowane)
| IDE | Użycie |
|-----|--------|
| IntelliJ IDEA | Backend (Java) |
| VS Code | Frontend (TypeScript) |
| Android Studio | Android emulator |
| Xcode | iOS simulator (macOS) |

### Mobile Testing
| Platforma | Narzędzie |
|-----------|-----------|
| Android | Android Studio Emulator / Physical device |
| iOS | iOS Simulator (macOS) / Physical device |
| Both | Expo Go app |

---

## 10. Konfiguracja środowiskowa

### Backend (application.yaml)
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}

supabase:
  url: ${SUPABASE_URL}
  key: ${SUPABASE_ANON_KEY}
  service-key: ${SUPABASE_SERVICE_KEY}
  jwt-secret: ${SUPABASE_JWT_SECRET}
```

### Frontend (.env)
```
EXPO_PUBLIC_API_URL=http://localhost:8080
```

### Zmienne środowiskowe (wymagane)
```
# Database
DATABASE_URL=jdbc:postgresql://...
DATABASE_USER=postgres
DATABASE_PASSWORD=***

# Supabase
SUPABASE_URL=https://xxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
SUPABASE_SERVICE_KEY=eyJ...
SUPABASE_JWT_SECRET=***
```
