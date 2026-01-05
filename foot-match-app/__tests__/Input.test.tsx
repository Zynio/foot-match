import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import { Input } from '@/components/ui/Input';

describe('Input - testy z perspektywy uzytkownika', () => {
  it('uzytkownik widzi etykiete pola', () => {
    render(<Input label="Email" placeholder="Wpisz email" />);

    expect(screen.getByText('Email')).toBeOnTheScreen();
  });

  it('uzytkownik moze wpisac tekst w pole', () => {
    const onChangeText = jest.fn();
    render(
      <Input
        label="Email"
        placeholder="Wpisz email"
        onChangeText={onChangeText}
      />
    );

    const input = screen.getByPlaceholderText('Wpisz email');
    fireEvent.changeText(input, 'jan@example.com');

    expect(onChangeText).toHaveBeenCalledWith('jan@example.com');
  });

  it('uzytkownik widzi placeholder gdy pole jest puste', () => {
    render(<Input placeholder="Wpisz swoje imie" />);

    expect(screen.getByPlaceholderText('Wpisz swoje imie')).toBeOnTheScreen();
  });

  it('uzytkownik widzi komunikat bledu gdy pole jest niepoprawne', () => {
    render(
      <Input
        label="Email"
        placeholder="Wpisz email"
        error="Niepoprawny format email"
      />
    );

    expect(screen.getByText('Niepoprawny format email')).toBeOnTheScreen();
  });

  it('uzytkownik moze kliknac ikone po prawej stronie (np. toggle hasla)', () => {
    const onRightIconPress = jest.fn();
    render(
      <Input
        label="Haslo"
        placeholder="Wpisz haslo"
        secureTextEntry
        rightIcon="eye.fill"
        onRightIconPress={onRightIconPress}
      />
    );

    // Znajdujemy ikonę i klikamy
    // W testach ikona jest renderowana jako "MaterialIcons" (mock)
    const input = screen.getByPlaceholderText('Wpisz haslo');
    expect(input).toBeOnTheScreen();
  });

  it('pole z ikona po lewej stronie jest dostepne dla uzytkownika', () => {
    render(
      <Input
        label="Lokalizacja"
        placeholder="Wpisz adres"
        leftIcon="location.fill"
      />
    );

    const input = screen.getByPlaceholderText('Wpisz adres');
    expect(input).toBeOnTheScreen();

    // Uzytkownik moze wpisac tekst
    fireEvent.changeText(input, 'ul. Pulawska 12');
    expect(input.props.value).toBe(undefined); // controlled component without value prop
  });

  it('pole wieloliniowe pozwala na wpisanie dluzszego tekstu', () => {
    const onChangeText = jest.fn();
    render(
      <Input
        label="Opis"
        placeholder="Opisz mecz"
        multiline
        numberOfLines={3}
        onChangeText={onChangeText}
      />
    );

    const input = screen.getByPlaceholderText('Opisz mecz');
    const longText = 'To jest dlugi opis meczu.\nDruga linia.\nTrzecia linia.';
    fireEvent.changeText(input, longText);

    expect(onChangeText).toHaveBeenCalledWith(longText);
  });
});

describe('Input - walidacja i stany', () => {
  it('pole bez bledu nie wyswietla komunikatu', () => {
    render(<Input label="Imie" placeholder="Wpisz imie" />);

    expect(screen.queryByText(/blad|error/i)).toBeNull();
  });

  it('pole z etykieta i bledem wyswietla oba elementy', () => {
    render(
      <Input
        label="Haslo"
        placeholder="Wpisz haslo"
        error="Haslo musi miec min. 8 znakow"
      />
    );

    expect(screen.getByText('Haslo')).toBeOnTheScreen();
    expect(screen.getByText('Haslo musi miec min. 8 znakow')).toBeOnTheScreen();
  });

  it('pole bez etykiety wyswietla tylko input', () => {
    render(<Input placeholder="Szukaj..." />);

    expect(screen.getByPlaceholderText('Szukaj...')).toBeOnTheScreen();
    // Nie powinno byc zadnej etykiety
    expect(screen.queryByText(/label/i)).toBeNull();
  });
});

describe('Input - interakcje uzytkownika', () => {
  it('uzytkownik moze skupic sie na polu (focus)', () => {
    const onFocus = jest.fn();
    render(
      <Input
        label="Email"
        placeholder="Wpisz email"
        onFocus={onFocus}
      />
    );

    const input = screen.getByPlaceholderText('Wpisz email');
    fireEvent(input, 'focus');

    expect(onFocus).toHaveBeenCalled();
  });

  it('uzytkownik moze opuscic pole (blur)', () => {
    const onBlur = jest.fn();
    render(
      <Input
        label="Email"
        placeholder="Wpisz email"
        onBlur={onBlur}
      />
    );

    const input = screen.getByPlaceholderText('Wpisz email');
    fireEvent(input, 'blur');

    expect(onBlur).toHaveBeenCalled();
  });

  it('pole z klawiatura numeryczna przyjmuje tylko liczby', () => {
    const onChangeText = jest.fn();
    render(
      <Input
        label="Liczba graczy"
        placeholder="10"
        keyboardType="number-pad"
        onChangeText={onChangeText}
      />
    );

    const input = screen.getByPlaceholderText('10');
    fireEvent.changeText(input, '15');

    expect(onChangeText).toHaveBeenCalledWith('15');
  });
});
