import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { router } from 'expo-router';
import { Card } from '@/components/ui/Card';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { colors, spacing } from '@/constants/theme';
import type { Match } from '@/types';

interface MatchCardProps {
  match: Match;
}

export function MatchCard({ match }: MatchCardProps) {
  const handlePress = () => {
    router.push(`/match/${match.id}`);
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('pl-PL', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusColor = () => {
    switch (match.status) {
      case 'OPEN':
        return colors.success;
      case 'CLOSED':
        return colors.warning;
      case 'CANCELLED':
        return colors.error;
      case 'COMPLETED':
        return colors.secondary;
      default:
        return colors.secondary;
    }
  };

  const getStatusLabel = () => {
    switch (match.status) {
      case 'OPEN':
        return 'Otwarty';
      case 'CLOSED':
        return 'Zamkniety';
      case 'CANCELLED':
        return 'Anulowany';
      case 'COMPLETED':
        return 'Zakonczony';
      default:
        return match.status;
    }
  };

  return (
    <TouchableOpacity onPress={handlePress} activeOpacity={0.7}>
      <Card style={styles.card}>
        <View style={styles.header}>
          <Text style={styles.title} numberOfLines={1}>
            {match.title}
          </Text>
          <View style={[styles.statusBadge, { backgroundColor: getStatusColor() }]}>
            <Text style={styles.statusText}>{getStatusLabel()}</Text>
          </View>
        </View>

        <View style={styles.info}>
          <View style={styles.infoRow}>
            <IconSymbol name="location.fill" size={16} color={colors.textSecondary} />
            <Text style={styles.infoText}>{match.location}</Text>
          </View>
          <View style={styles.infoRow}>
            <IconSymbol name="calendar" size={16} color={colors.textSecondary} />
            <Text style={styles.infoText}>{formatDate(match.matchDate)}</Text>
          </View>
        </View>

        <View style={styles.footer}>
          <View style={styles.playersContainer}>
            <IconSymbol name="person.2.fill" size={18} color={colors.primary} />
            <Text style={styles.players}>
              {match.currentPlayers}/{match.maxPlayers}
            </Text>
          </View>
          <View style={styles.organizerContainer}>
            <IconSymbol name="person.badge.shield.checkmark.fill" size={14} color={colors.textSecondary} />
            <Text style={styles.organizer}>{match.organizer.name}</Text>
          </View>
        </View>
      </Card>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    marginHorizontal: spacing.md,
    marginVertical: spacing.sm,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
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
  info: {
    marginBottom: spacing.sm,
    gap: spacing.xs,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  infoText: {
    fontSize: 14,
    color: colors.textSecondary,
    flex: 1,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: colors.border,
    paddingTop: spacing.sm,
  },
  playersContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  players: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.primary,
  },
  organizerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  organizer: {
    fontSize: 12,
    color: colors.textSecondary,
  },
});
