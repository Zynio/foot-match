import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useLocalSearchParams, router } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { matchService } from '@/services/matchService';
import { useAuth } from '@/stores/AuthContext';
import { colors, spacing } from '@/constants/theme';
import { ApiException } from '@/services/api';
import type { Match, Participant } from '@/types';

export default function MatchDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { user, isAuthenticated } = useAuth();
  const [match, setMatch] = useState<Match | null>(null);
  const [participants, setParticipants] = useState<Participant[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchMatch();
  }, [id]);

  const fetchMatch = async () => {
    if (!id) return;
    try {
      setError(null);
      const [matchData, participantsData] = await Promise.all([
        matchService.getById(id),
        matchService.getParticipants(id),
      ]);
      setMatch(matchData);
      setParticipants(participantsData);
    } catch (err) {
      setError('Nie udalo sie pobrac danych meczu');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('pl-PL', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusColor = () => {
    switch (match?.status) {
      case 'OPEN': return colors.success;
      case 'CLOSED': return colors.warning;
      case 'CANCELLED': return colors.error;
      case 'COMPLETED': return colors.secondary;
      default: return colors.secondary;
    }
  };

  const getStatusLabel = () => {
    switch (match?.status) {
      case 'OPEN': return 'Otwarty';
      case 'CLOSED': return 'Zamkniety';
      case 'CANCELLED': return 'Anulowany';
      case 'COMPLETED': return 'Zakonczony';
      default: return match?.status;
    }
  };

  const isOrganizer = user?.id === match?.organizer.id;
  const hasJoined = participants.some(p => p.player.id === user?.id);
  const canJoin = isAuthenticated && !isOrganizer && !hasJoined && match?.status === 'OPEN';

  const handleJoin = async () => {
    if (!id || !isAuthenticated) {
      router.push('/(auth)/login');
      return;
    }

    setActionLoading(true);
    try {
      await matchService.join(id);
      await fetchMatch();
      Alert.alert('Sukces', 'Dolaczyles do meczu!');
    } catch (err) {
      if (err instanceof ApiException) {
        Alert.alert('Blad', err.message);
      } else {
        Alert.alert('Blad', 'Nie udalo sie dolaczyc do meczu');
      }
    } finally {
      setActionLoading(false);
    }
  };

  const handleLeave = async () => {
    if (!id) return;

    Alert.alert(
      'Opusc mecz',
      'Czy na pewno chcesz opuscic ten mecz?',
      [
        { text: 'Anuluj', style: 'cancel' },
        {
          text: 'Opusc',
          style: 'destructive',
          onPress: async () => {
            setActionLoading(true);
            try {
              await matchService.leave(id);
              await fetchMatch();
              Alert.alert('Sukces', 'Opusciles mecz');
            } catch (err) {
              Alert.alert('Blad', 'Nie udalo sie opuscic meczu');
            } finally {
              setActionLoading(false);
            }
          },
        },
      ]
    );
  };

  const handleAcceptParticipant = async (playerId: string) => {
    if (!id) return;
    setActionLoading(true);
    try {
      await matchService.updateParticipantStatus(id, playerId, 'ACCEPTED');
      await fetchMatch();
    } catch (err) {
      if (err instanceof ApiException) {
        Alert.alert('Blad', err.message);
      }
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejectParticipant = async (playerId: string) => {
    if (!id) return;
    setActionLoading(true);
    try {
      await matchService.updateParticipantStatus(id, playerId, 'REJECTED');
      await fetchMatch();
    } catch (err) {
      Alert.alert('Blad', 'Nie udalo sie odrzucic gracza');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  if (error || !match) {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorText}>{error || 'Mecz nie zostal znaleziony'}</Text>
        <Button title="Wróc" onPress={() => router.back()} />
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <ScrollView contentContainerStyle={styles.content}>
        <Card style={styles.mainCard}>
          <View style={styles.header}>
            <Text style={styles.title}>{match.title}</Text>
            <View style={[styles.statusBadge, { backgroundColor: getStatusColor() }]}>
              <Text style={styles.statusText}>{getStatusLabel()}</Text>
            </View>
          </View>

          {match.description && (
            <Text style={styles.description}>{match.description}</Text>
          )}

          <View style={styles.infoRow}>
            <View style={styles.infoLabelContainer}>
              <IconSymbol name="location.fill" size={18} color={colors.primary} />
              <Text style={styles.infoLabel}>Lokalizacja</Text>
            </View>
            <Text style={styles.infoValue}>{match.location}</Text>
          </View>

          <View style={styles.infoRow}>
            <View style={styles.infoLabelContainer}>
              <IconSymbol name="calendar" size={18} color={colors.primary} />
              <Text style={styles.infoLabel}>Data</Text>
            </View>
            <Text style={styles.infoValue}>{formatDate(match.matchDate)}</Text>
          </View>

          <View style={styles.infoRow}>
            <View style={styles.infoLabelContainer}>
              <IconSymbol name="person.2.fill" size={18} color={colors.primary} />
              <Text style={styles.infoLabel}>Gracze</Text>
            </View>
            <Text style={styles.infoValue}>
              {match.currentPlayers} / {match.maxPlayers}
            </Text>
          </View>

          <View style={styles.infoRow}>
            <View style={styles.infoLabelContainer}>
              <IconSymbol name="person.badge.shield.checkmark.fill" size={18} color={colors.primary} />
              <Text style={styles.infoLabel}>Organizator</Text>
            </View>
            <Text style={styles.infoValue}>{match.organizer.name}</Text>
          </View>
        </Card>

        <Card style={styles.participantsCard}>
          <View style={styles.sectionHeader}>
            <IconSymbol name="person.2.fill" size={20} color={colors.text} />
            <Text style={styles.sectionTitle}>Uczestnicy</Text>
          </View>
          {participants.length === 0 ? (
            <Text style={styles.emptyText}>Brak uczestnikow</Text>
          ) : (
            participants.map((p) => (
              <View key={p.id} style={styles.participantRow}>
                <View style={styles.participantInfo}>
                  <View style={styles.participantNameRow}>
                    <IconSymbol name="person.fill" size={16} color={colors.textSecondary} />
                    <Text style={styles.participantName}>{p.player.name}</Text>
                  </View>
                  <View style={styles.participantStatusRow}>
                    {p.status === 'PENDING' && (
                      <IconSymbol name="clock.badge.questionmark" size={14} color={colors.warning} />
                    )}
                    {p.status === 'ACCEPTED' && (
                      <IconSymbol name="checkmark.circle.fill" size={14} color={colors.success} />
                    )}
                    {p.status === 'REJECTED' && (
                      <IconSymbol name="xmark.circle.fill" size={14} color={colors.error} />
                    )}
                    <Text style={[
                      styles.participantStatus,
                      p.status === 'ACCEPTED' && styles.statusAccepted,
                      p.status === 'REJECTED' && styles.statusRejected,
                    ]}>
                      {p.status === 'PENDING' && 'Oczekuje'}
                      {p.status === 'ACCEPTED' && 'Zaakceptowany'}
                      {p.status === 'REJECTED' && 'Odrzucony'}
                    </Text>
                  </View>
                </View>
                {isOrganizer && p.status === 'PENDING' && (
                  <View style={styles.participantActions}>
                    <Button
                      title="Akceptuj"
                      onPress={() => handleAcceptParticipant(p.player.id)}
                      style={styles.actionButton}
                    />
                    <Button
                      title="Odrzuc"
                      variant="outline"
                      onPress={() => handleRejectParticipant(p.player.id)}
                      style={styles.actionButton}
                    />
                  </View>
                )}
              </View>
            ))
          )}
        </Card>
      </ScrollView>

      <View style={styles.footer}>
        {canJoin && (
          <Button
            title="Dolacz do meczu"
            onPress={handleJoin}
            loading={actionLoading}
          />
        )}
        {hasJoined && !isOrganizer && (
          <Button
            title="Opusc mecz"
            variant="outline"
            onPress={handleLeave}
            loading={actionLoading}
          />
        )}
        {!isAuthenticated && match.status === 'OPEN' && (
          <Button
            title="Zaloguj sie, aby dolaczyc"
            onPress={() => router.push('/(auth)/login')}
          />
        )}
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
    padding: spacing.md,
  },
  mainCard: {
    marginBottom: spacing.md,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: spacing.md,
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    color: colors.text,
    flex: 1,
    marginRight: spacing.sm,
  },
  statusBadge: {
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: 12,
  },
  statusText: {
    color: colors.white,
    fontSize: 12,
    fontWeight: '600',
  },
  description: {
    fontSize: 14,
    color: colors.textSecondary,
    marginBottom: spacing.md,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: spacing.sm,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
  infoLabelContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  infoLabel: {
    fontSize: 14,
    color: colors.textSecondary,
  },
  infoValue: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.text,
    flex: 1,
    textAlign: 'right',
  },
  participantsCard: {
    marginBottom: spacing.lg,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    marginBottom: spacing.md,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.text,
  },
  emptyText: {
    fontSize: 14,
    color: colors.textSecondary,
    textAlign: 'center',
  },
  participantRow: {
    paddingVertical: spacing.sm,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
  participantInfo: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  participantNameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  participantName: {
    fontSize: 14,
    fontWeight: '500',
    color: colors.text,
  },
  participantStatusRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  participantStatus: {
    fontSize: 12,
    color: colors.warning,
  },
  statusAccepted: {
    color: colors.success,
  },
  statusRejected: {
    color: colors.error,
  },
  participantActions: {
    flexDirection: 'row',
    gap: spacing.sm,
    marginTop: spacing.sm,
  },
  actionButton: {
    flex: 1,
    paddingVertical: spacing.sm,
  },
  footer: {
    padding: spacing.md,
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
