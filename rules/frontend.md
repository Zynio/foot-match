# Frontend Rules - React Native (Expo)

## 1. Stack technologiczny

- **Framework:** React Native 0.81+ z Expo 54+
- **Język:** TypeScript (strict mode)
- **Nawigacja:** Expo Router (file-based routing)
- **Stan:** React Context / Zustand (jeśli potrzebne)
- **Styling:** StyleSheet / NativeWind (opcjonalnie)

## 2. Struktura projektu

```
foot-match-app/
├── app/                      # Expo Router - ekrany
│   ├── (tabs)/              # Tab navigation
│   │   ├── _layout.tsx      # Tab layout
│   │   ├── index.tsx        # Home (lista meczy)
│   │   ├── my-matches.tsx   # Moje mecze
│   │   └── profile.tsx      # Profil
│   ├── (auth)/              # Auth screens (bez tabs)
│   │   ├── login.tsx
│   │   └── register.tsx
│   ├── match/
│   │   ├── [id].tsx         # Szczegóły meczu
│   │   └── create.tsx       # Tworzenie meczu
│   └── _layout.tsx          # Root layout
├── components/              # Reużywalne komponenty
│   ├── ui/                  # Podstawowe UI (Button, Input)
│   └── match/               # Komponenty domenowe
├── hooks/                   # Custom hooks
├── services/                # API calls
├── stores/                  # State management
├── constants/               # Stałe (theme, config)
├── types/                   # TypeScript types
└── utils/                   # Helpers
```

## 3. Komponenty

### Funkcyjne komponenty
```tsx
// DOBRZE
export function MatchCard({ match }: MatchCardProps) {
  return (
    <View style={styles.container}>
      <Text>{match.title}</Text>
    </View>
  );
}

// ŹLE - class components
class MatchCard extends React.Component { }
```

### Props typing
```tsx
interface MatchCardProps {
  match: Match;
  onPress?: () => void;
}

export function MatchCard({ match, onPress }: MatchCardProps) {
  // ...
}
```

### Konwencje nazewnictwa

| Typ | Konwencja | Przykład |
|-----|-----------|----------|
| Komponent | PascalCase | `MatchCard.tsx` |
| Hook | camelCase, `use` prefix | `useAuth.ts` |
| Utility | camelCase | `formatDate.ts` |
| Typ/Interface | PascalCase | `Match`, `MatchCardProps` |
| Stała | SCREAMING_SNAKE | `API_URL` |

## 4. Hooks

### Custom hooks
```tsx
// hooks/useMatches.ts
export function useMatches() {
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchMatches()
      .then(setMatches)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return { matches, loading, error, refetch: fetchMatches };
}
```

### Hook rules
- Prefix `use` dla wszystkich hooków
- Jeden hook = jedna odpowiedzialność
- Zwracaj obiekt z nazwanymi wartościami

## 5. API Services

### Struktura
```tsx
// services/api.ts
const API_URL = process.env.EXPO_PUBLIC_API_URL;

async function request<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_URL}${endpoint}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`);
  }

  return response.json();
}

// services/matchService.ts
export const matchService = {
  getAll: () => request<Match[]>('/api/matches'),
  getById: (id: string) => request<Match>(`/api/matches/${id}`),
  create: (data: CreateMatchRequest) =>
    request<Match>('/api/matches', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
};
```

## 6. State Management

### React Context (dla prostych przypadków)
```tsx
// stores/AuthContext.tsx
interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  const login = async (email: string, password: string) => {
    const user = await authService.login(email, password);
    setUser(user);
  };

  const logout = () => setUser(null);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
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

## 7. Styling

### StyleSheet (preferowane)
```tsx
const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
  },
});
```

### Theme
```tsx
// constants/theme.ts
export const colors = {
  primary: '#2563eb',
  secondary: '#64748b',
  background: '#ffffff',
  text: '#1e293b',
  error: '#ef4444',
  success: '#22c55e',
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
} as const;
```

## 8. Nawigacja (Expo Router)

### File-based routing
```
app/
├── index.tsx           -> /
├── match/
│   ├── [id].tsx       -> /match/123
│   └── create.tsx     -> /match/create
└── (tabs)/
    ├── index.tsx      -> / (tab)
    └── profile.tsx    -> /profile (tab)
```

### Nawigacja programowa
```tsx
import { router } from 'expo-router';

// Przejście
router.push('/match/123');

// Zamiana (bez historii)
router.replace('/login');

// Powrót
router.back();
```

### Parametry
```tsx
// app/match/[id].tsx
import { useLocalSearchParams } from 'expo-router';

export default function MatchDetails() {
  const { id } = useLocalSearchParams<{ id: string }>();
  // ...
}
```

## 9. Formularze

### Kontrolowane inputy
```tsx
function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = () => {
    // walidacja i submit
  };

  return (
    <View>
      <TextInput
        value={email}
        onChangeText={setEmail}
        placeholder="Email"
        keyboardType="email-address"
        autoCapitalize="none"
      />
      <TextInput
        value={password}
        onChangeText={setPassword}
        placeholder="Hasło"
        secureTextEntry
      />
      <Button title="Zaloguj" onPress={handleSubmit} />
    </View>
  );
}
```

## 10. Error Handling

```tsx
function MatchList() {
  const { matches, loading, error } = useMatches();

  if (loading) return <ActivityIndicator />;
  if (error) return <Text style={styles.error}>{error}</Text>;
  if (matches.length === 0) return <Text>Brak meczy</Text>;

  return (
    <FlatList
      data={matches}
      renderItem={({ item }) => <MatchCard match={item} />}
      keyExtractor={(item) => item.id}
    />
  );
}
```

## 11. Bezpieczeństwo

### Przechowywanie tokenów
```tsx
import * as SecureStore from 'expo-secure-store';

// Zapis
await SecureStore.setItemAsync('authToken', token);

// Odczyt
const token = await SecureStore.getItemAsync('authToken');

// Usunięcie
await SecureStore.deleteItemAsync('authToken');
```

### Nigdy nie przechowuj wrażliwych danych w:
- AsyncStorage (nieszyfrowane)
- State (widoczne w devtools)
- console.log w produkcji

## 12. Dobre praktyki

1. **TypeScript strict mode** - włącz w tsconfig.json
2. **Komponenty małe** - max 150-200 linii
3. **Jeden plik = jeden komponent** (eksportowany)
4. **Unikaj inline styles** - używaj StyleSheet
5. **Memoizacja** - React.memo, useMemo, useCallback gdzie potrzebne
6. **Keys w listach** - zawsze unikalne, stabilne ID
7. **Accessibility** - accessibilityLabel dla interaktywnych elementów
