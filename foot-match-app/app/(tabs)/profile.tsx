import React from 'react';
import { View, Text, StyleSheet, Alert } from 'react-native';
import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { useAuth } from '@/stores/AuthContext';
import { colors, spacing } from '@/constants/theme';

export default function ProfileScreen() {
  const { user, isAuthenticated, logout, isLoading } = useAuth();

  const handleLogout = () => {
    Alert.alert(
      'Wylogowanie',
      'Czy na pewno chcesz sie wylogowac?',
      [
        { text: 'Anuluj', style: 'cancel' },
        {
          text: 'Wyloguj',
          style: 'destructive',
          onPress: async () => {
            await logout();
          },
        },
      ]
    );
  };

  const getRoleLabel = () => {
    switch (user?.role) {
      case 'ORGANIZER':
        return 'Organizator';
      case 'PLAYER':
        return 'Gracz';
      default:
        return user?.role;
    }
  };

  if (isLoading) {
    return (
      <View style={styles.centered}>
        <Text style={styles.loadingText}>Ladowanie...</Text>
      </View>
    );
  }

  if (!isAuthenticated) {
    return (
      <SafeAreaView style={styles.container} edges={['bottom']}>
        <View style={styles.centered}>
          <View style={styles.welcomeIcon}>
            <IconSymbol name="sportscourt.fill" size={64} color={colors.primary} />
          </View>
          <Text style={styles.title}>Witaj w Foot Match!</Text>
          <Text style={styles.subtitle}>Zaloguj sie, aby korzystac z pelnych funkcji aplikacji</Text>
          <Button
            title="Zaloguj sie"
            onPress={() => router.push('/(auth)/login')}
            style={styles.button}
          />
          <Button
            title="Zarejestruj sie"
            variant="outline"
            onPress={() => router.push('/(auth)/register')}
            style={styles.button}
          />
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <View style={styles.content}>
        <Card style={styles.profileCard}>
          <View style={styles.avatarContainer}>
            <View style={styles.avatar}>
              <Text style={styles.avatarText}>
                {user?.name?.charAt(0).toUpperCase() || '?'}
              </Text>
            </View>
          </View>

          <Text style={styles.name}>{user?.name}</Text>
          <View style={styles.emailRow}>
            <IconSymbol name="envelope.fill" size={14} color={colors.textSecondary} />
            <Text style={styles.email}>{user?.email}</Text>
          </View>

          <View style={styles.roleBadge}>
            <IconSymbol
              name={user?.role === 'ORGANIZER' ? 'person.badge.shield.checkmark.fill' : 'person.fill'}
              size={16}
              color={colors.primary}
            />
            <Text style={styles.roleText}>{getRoleLabel()}</Text>
          </View>
        </Card>

        <View style={styles.actions}>
          <Button
            title="Wyloguj sie"
            variant="outline"
            onPress={handleLogout}
          />
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: spacing.lg,
  },
  content: {
    flex: 1,
    padding: spacing.lg,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: spacing.sm,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 16,
    color: colors.textSecondary,
    textAlign: 'center',
    marginBottom: spacing.xl,
  },
  welcomeIcon: {
    marginBottom: spacing.lg,
  },
  button: {
    minWidth: 200,
    marginBottom: spacing.md,
  },
  profileCard: {
    alignItems: 'center',
    padding: spacing.xl,
  },
  avatarContainer: {
    marginBottom: spacing.md,
  },
  avatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
  },
  avatarText: {
    fontSize: 32,
    fontWeight: 'bold',
    color: colors.white,
  },
  name: {
    fontSize: 22,
    fontWeight: '600',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  emailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    marginBottom: spacing.md,
  },
  email: {
    fontSize: 14,
    color: colors.textSecondary,
  },
  roleBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
    backgroundColor: `${colors.primary}15`,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: 16,
  },
  roleText: {
    color: colors.primary,
    fontWeight: '600',
    fontSize: 14,
  },
  actions: {
    marginTop: spacing.xl,
  },
  loadingText: {
    fontSize: 16,
    color: colors.textSecondary,
  },
});
