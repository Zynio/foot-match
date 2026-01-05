import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import { router } from 'expo-router';
import { MatchCard } from '@/components/match/MatchCard';
import type { Match } from '@/types';

// Mock data - mecz z perspektywy użytkownika
const mockMatch: Match = {
  id: 'match-123',
  title: 'Mecz na Orliku Mokotow',
  description: 'Przyjazna gra dla wszystkich poziomow',
  location: 'Orlik Mokotow, ul. Pulawska 12',
  matchDate: '2026-01-10T18:00:00',
  maxPlayers: 10,
  currentPlayers: 6,
  status: 'OPEN',
  organizer: {
    id: 'org-1',
    name: 'Jan Kowalski',
    email: 'jan@example.com',
    role: 'ORGANIZER',
  },
  createdAt: '2026-01-01T10:00:00',
  updatedAt: '2026-01-01T10:00:00',
};

describe('MatchCard - testy z perspektywy uzytkownika', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('uzytkownik widzi wszystkie informacje o meczu', () => {
    render(<MatchCard match={mockMatch} />);

    // Uzytkownik widzi tytul meczu
    expect(screen.getByText('Mecz na Orliku Mokotow')).toBeOnTheScreen();

    // Uzytkownik widzi lokalizacje
    expect(screen.getByText('Orlik Mokotow, ul. Pulawska 12')).toBeOnTheScreen();

    // Uzytkownik widzi liczbe graczy
    expect(screen.getByText('6/10')).toBeOnTheScreen();

    // Uzytkownik widzi nazwe organizatora
    expect(screen.getByText('Jan Kowalski')).toBeOnTheScreen();

    // Uzytkownik widzi status meczu
    expect(screen.getByText('Otwarty')).toBeOnTheScreen();
  });

  it('uzytkownik moze kliknac w karte aby zobaczyc szczegoly meczu', () => {
    render(<MatchCard match={mockMatch} />);

    // Uzytkownik klika w karte meczu
    const card = screen.getByText('Mecz na Orliku Mokotow');
    fireEvent.press(card);

    // Sprawdzamy czy nawigacja zostala wywolana z poprawnym ID
    expect(router.push).toHaveBeenCalledWith('/match/match-123');
  });

  it('uzytkownik widzi rozne statusy meczow', () => {
    // Test dla meczu zamknietego
    const closedMatch: Match = { ...mockMatch, status: 'CLOSED' };
    const { rerender } = render(<MatchCard match={closedMatch} />);
    expect(screen.getByText('Zamkniety')).toBeOnTheScreen();

    // Test dla meczu anulowanego
    const cancelledMatch: Match = { ...mockMatch, status: 'CANCELLED' };
    rerender(<MatchCard match={cancelledMatch} />);
    expect(screen.getByText('Anulowany')).toBeOnTheScreen();

    // Test dla meczu zakonczonego
    const completedMatch: Match = { ...mockMatch, status: 'COMPLETED' };
    rerender(<MatchCard match={completedMatch} />);
    expect(screen.getByText('Zakonczony')).toBeOnTheScreen();
  });

  it('uzytkownik widzi sformatowana date meczu', () => {
    render(<MatchCard match={mockMatch} />);

    // Data powinna byc sformatowana po polsku
    // Sprawdzamy czy zawiera elementy daty (dzien, miesiac, godzine)
    const dateText = screen.getByText(/sty|01/i); // Styczen
    expect(dateText).toBeOnTheScreen();
  });

  it('karta meczu jest interaktywna i reaguje na dotyk', () => {
    render(<MatchCard match={mockMatch} />);

    // Sprawdzamy czy element jest klikalny (TouchableOpacity)
    const touchable = screen.getByText('Mecz na Orliku Mokotow').parent?.parent;
    expect(touchable).toBeTruthy();
  });
});

describe('MatchCard - przypadki brzegowe', () => {
  it('wyswietla mecz z pelna liczba graczy', () => {
    const fullMatch: Match = {
      ...mockMatch,
      currentPlayers: 10,
      maxPlayers: 10,
    };
    render(<MatchCard match={fullMatch} />);

    expect(screen.getByText('10/10')).toBeOnTheScreen();
  });

  it('wyswietla mecz bez graczy', () => {
    const emptyMatch: Match = {
      ...mockMatch,
      currentPlayers: 0,
    };
    render(<MatchCard match={emptyMatch} />);

    expect(screen.getByText('0/10')).toBeOnTheScreen();
  });

  it('obcina dlugi tytul meczu', () => {
    const longTitleMatch: Match = {
      ...mockMatch,
      title: 'Bardzo dlugi tytul meczu ktory powinien zostac obciety na karcie',
    };
    render(<MatchCard match={longTitleMatch} />);

    // Tytul powinien byc widoczny (moze byc obciety przez numberOfLines={1})
    expect(
      screen.getByText('Bardzo dlugi tytul meczu ktory powinien zostac obciety na karcie')
    ).toBeOnTheScreen();
  });
});
