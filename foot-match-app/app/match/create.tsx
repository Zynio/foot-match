import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  Alert,
} from 'react-native';
import { router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { matchService } from '@/services/matchService';
import { useAuth } from '@/stores/AuthContext';
import { colors, spacing } from '@/constants/theme';
import { ApiException } from '@/services/api';

export default function CreateMatchScreen() {
  const { user, isAuthenticated } = useAuth();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [maxPlayers, setMaxPlayers] = useState('10');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Redirect if not authenticated or not organizer
  if (!isAuthenticated || user?.role !== 'ORGANIZER') {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorText}>
          Tylko organizatorzy moga tworzyc mecze
        </Text>
        <Button title="Wróc" onPress={() => router.back()} />
      </View>
    );
  }

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!title.trim()) newErrors.title = 'Tytul jest wymagany';
    if (!location.trim()) newErrors.location = 'Lokalizacja jest wymagana';
    if (!date.trim()) newErrors.date = 'Data jest wymagana';
    if (!time.trim()) newErrors.time = 'Godzina jest wymagana';

    const players = parseInt(maxPlayers, 10);
    if (isNaN(players) || players < 2) {
      newErrors.maxPlayers = 'Min. 2 graczy';
    } else if (players > 50) {
      newErrors.maxPlayers = 'Max. 50 graczy';
    }

    // Validate date format (YYYY-MM-DD)
    if (date && !/^\d{4}-\d{2}-\d{2}$/.test(date)) {
      newErrors.date = 'Format: YYYY-MM-DD (np. 2024-12-20)';
    }

    // Validate time format (HH:MM)
    if (time && !/^\d{2}:\d{2}$/.test(time)) {
      newErrors.time = 'Format: HH:MM (np. 18:00)';
    }

    // Check if date is in the future
    if (date && time) {
      const matchDate = new Date(`${date}T${time}:00`);
      if (matchDate <= new Date()) {
        newErrors.date = 'Data meczu musi byc w przyszlosci';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleCreate = async () => {
    if (!validate()) return;

    setLoading(true);
    try {
      const matchDate = `${date}T${time}:00`;
      await matchService.create({
        title: title.trim(),
        description: description.trim() || undefined,
        location: location.trim(),
        matchDate,
        maxPlayers: parseInt(maxPlayers, 10),
      });

      Alert.alert('Sukces', 'Mecz zostal utworzony!', [
        { text: 'OK', onPress: () => router.back() },
      ]);
    } catch (err) {
      if (err instanceof ApiException) {
        Alert.alert('Blad', err.message);
      } else {
        Alert.alert('Blad', 'Nie udalo sie utworzyc meczu');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        <ScrollView
          contentContainerStyle={styles.content}
          keyboardShouldPersistTaps="handled"
        >
          <Input
            label="Tytul meczu"
            value={title}
            onChangeText={setTitle}
            placeholder="np. Mecz na orliku"
            error={errors.title}
            maxLength={100}
            leftIcon="textformat"
          />

          <Input
            label="Opis (opcjonalnie)"
            value={description}
            onChangeText={setDescription}
            placeholder="Dodatkowe informacje o meczu"
            multiline
            numberOfLines={3}
            maxLength={500}
            style={styles.textArea}
            leftIcon="text.alignleft"
          />

          <Input
            label="Lokalizacja"
            value={location}
            onChangeText={setLocation}
            placeholder="np. Orlik Mokotow, ul. Pulawska 12"
            error={errors.location}
            maxLength={255}
            leftIcon="location.fill"
          />

          <View style={styles.row}>
            <View style={styles.halfInput}>
              <Input
                label="Data"
                value={date}
                onChangeText={setDate}
                placeholder="YYYY-MM-DD"
                error={errors.date}
                keyboardType="numbers-and-punctuation"
                leftIcon="calendar"
              />
            </View>
            <View style={styles.halfInput}>
              <Input
                label="Godzina"
                value={time}
                onChangeText={setTime}
                placeholder="HH:MM"
                error={errors.time}
                keyboardType="numbers-and-punctuation"
                leftIcon="clock.fill"
              />
            </View>
          </View>

          <Input
            label="Maksymalna liczba graczy"
            value={maxPlayers}
            onChangeText={setMaxPlayers}
            placeholder="10"
            keyboardType="number-pad"
            error={errors.maxPlayers}
            leftIcon="person.2.fill"
          />

          <View style={styles.hint}>
            <IconSymbol name="info.circle.fill" size={20} color={colors.primary} style={styles.hintIcon} />
            <Text style={styles.hintText}>
              Po utworzeniu meczu gracze beda mogli do niego dolaczac.
              Jako organizator bedziesz moc akceptowac lub odrzucac zgłoszenia.
            </Text>
          </View>
        </ScrollView>

        <View style={styles.footer}>
          <Button
            title="Utworz mecz"
            onPress={handleCreate}
            loading={loading}
          />
        </View>
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
  content: {
    padding: spacing.lg,
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: spacing.lg,
  },
  row: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  halfInput: {
    flex: 1,
  },
  textArea: {
    height: 80,
    textAlignVertical: 'top',
  },
  hint: {
    flexDirection: 'row',
    backgroundColor: `${colors.primary}10`,
    padding: spacing.md,
    borderRadius: 8,
    marginTop: spacing.md,
    gap: spacing.sm,
  },
  hintIcon: {
    marginTop: 2,
  },
  hintText: {
    flex: 1,
    fontSize: 14,
    color: colors.textSecondary,
    lineHeight: 20,
  },
  footer: {
    padding: spacing.lg,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    backgroundColor: colors.white,
  },
  errorText: {
    fontSize: 16,
    color: colors.error,
    textAlign: 'center',
    marginBottom: spacing.md,
  },
});
