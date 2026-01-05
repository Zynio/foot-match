import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  Alert,
  TouchableOpacity,
} from 'react-native';
import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { useAuth } from '@/stores/AuthContext';
import { colors, spacing } from '@/constants/theme';
import { ApiException } from '@/services/api';
import type { UserRole } from '@/types';

export default function RegisterScreen() {
  const { register } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('PLAYER');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<{
    name?: string;
    email?: string;
    password?: string;
  }>({});

  const validate = () => {
    const newErrors: typeof errors = {};
    if (!name) newErrors.name = 'Imie jest wymagane';
    if (!email) newErrors.email = 'Email jest wymagany';
    else if (!/\S+@\S+\.\S+/.test(email)) newErrors.email = 'Nieprawidlowy format email';
    if (!password) newErrors.password = 'Haslo jest wymagane';
    else if (password.length < 8) newErrors.password = 'Haslo musi miec min. 8 znakow';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegister = async () => {
    if (!validate()) return;

    setLoading(true);
    try {
      await register({ name, email, password, role });
      router.replace('/(tabs)');
    } catch (error) {
      if (error instanceof ApiException) {
        Alert.alert('Blad rejestracji', error.message);
      } else {
        Alert.alert('Blad', 'Cos poszlo nie tak. Sprobuj ponownie.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
        >
          <View style={styles.header}>
            <Text style={styles.title}>Utworz konto</Text>
            <Text style={styles.subtitle}>Dolacz do spolecznosci pilkarzy</Text>
          </View>

          <View style={styles.form}>
            <Input
              label="Imie"
              value={name}
              onChangeText={setName}
              placeholder="Jan Kowalski"
              autoCapitalize="words"
              error={errors.name}
            />

            <Input
              label="Email"
              value={email}
              onChangeText={setEmail}
              placeholder="jan@example.com"
              keyboardType="email-address"
              autoCapitalize="none"
              autoComplete="email"
              error={errors.email}
            />

            <Input
              label="Haslo"
              value={password}
              onChangeText={setPassword}
              placeholder="Min. 8 znakow"
              secureTextEntry
              autoComplete="new-password"
              error={errors.password}
            />

            <Text style={styles.roleLabel}>Wybierz role</Text>
            <View style={styles.roleContainer}>
              <TouchableOpacity
                style={[styles.roleOption, role === 'PLAYER' && styles.roleSelected]}
                onPress={() => setRole('PLAYER')}
              >
                <Text style={[styles.roleText, role === 'PLAYER' && styles.roleTextSelected]}>
                  Gracz
                </Text>
                <Text style={styles.roleDescription}>Dolaczaj do meczy</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.roleOption, role === 'ORGANIZER' && styles.roleSelected]}
                onPress={() => setRole('ORGANIZER')}
              >
                <Text style={[styles.roleText, role === 'ORGANIZER' && styles.roleTextSelected]}>
                  Organizator
                </Text>
                <Text style={styles.roleDescription}>Twórz i zarzadzaj meczami</Text>
              </TouchableOpacity>
            </View>

            <Button
              title="Zarejestruj sie"
              onPress={handleRegister}
              loading={loading}
              style={styles.button}
            />
          </View>

          <View style={styles.footer}>
            <Text style={styles.footerText}>Masz juz konto?</Text>
            <Button
              title="Zaloguj sie"
              variant="outline"
              onPress={() => router.back()}
            />
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  keyboardView: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    padding: spacing.lg,
  },
  header: {
    alignItems: 'center',
    marginBottom: spacing.xl,
    marginTop: spacing.lg,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: spacing.sm,
  },
  subtitle: {
    fontSize: 16,
    color: colors.textSecondary,
  },
  form: {
    marginBottom: spacing.xl,
  },
  roleLabel: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.text,
    marginBottom: spacing.sm,
  },
  roleContainer: {
    flexDirection: 'row',
    gap: spacing.md,
    marginBottom: spacing.md,
  },
  roleOption: {
    flex: 1,
    padding: spacing.md,
    borderRadius: 8,
    borderWidth: 2,
    borderColor: colors.border,
    alignItems: 'center',
  },
  roleSelected: {
    borderColor: colors.primary,
    backgroundColor: `${colors.primary}10`,
  },
  roleText: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text,
    marginBottom: 4,
  },
  roleTextSelected: {
    color: colors.primary,
  },
  roleDescription: {
    fontSize: 12,
    color: colors.textSecondary,
    textAlign: 'center',
  },
  button: {
    marginTop: spacing.md,
  },
  footer: {
    alignItems: 'center',
    gap: spacing.md,
    paddingBottom: spacing.lg,
  },
  footerText: {
    color: colors.textSecondary,
    fontSize: 14,
  },
});
