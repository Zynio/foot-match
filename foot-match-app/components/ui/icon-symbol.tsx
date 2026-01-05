// Fallback for using MaterialIcons on Android and web.

import MaterialIcons from '@expo/vector-icons/MaterialIcons';
import { SymbolWeight, SymbolViewProps } from 'expo-symbols';
import { ComponentProps } from 'react';
import { OpaqueColorValue, type StyleProp, type TextStyle } from 'react-native';

type IconMapping = Record<SymbolViewProps['name'], ComponentProps<typeof MaterialIcons>['name']>;
type IconSymbolName = keyof typeof MAPPING;

/**
 * Add your SF Symbols to Material Icons mappings here.
 * - see Material Icons in the [Icons Directory](https://icons.expo.fyi).
 * - see SF Symbols in the [SF Symbols](https://developer.apple.com/sf-symbols/) app.
 */
const MAPPING = {
  // Navigation
  'house.fill': 'home',
  'paperplane.fill': 'send',
  'chevron.left.forwardslash.chevron.right': 'code',
  'chevron.right': 'chevron-right',

  // Tabs
  'sportscourt.fill': 'sports-soccer',
  'list.bullet': 'list',
  'person.fill': 'person',

  // Match info
  'location.fill': 'location-on',
  'calendar': 'event',
  'clock.fill': 'access-time',
  'person.2.fill': 'group',
  'person.badge.shield.checkmark.fill': 'admin-panel-settings',

  // Actions
  'plus.circle.fill': 'add-circle',
  'checkmark.circle.fill': 'check-circle',
  'xmark.circle.fill': 'cancel',
  'clock.badge.questionmark': 'schedule',
  'arrow.right.square': 'login',
  'rectangle.portrait.and.arrow.right': 'logout',

  // Form inputs
  'envelope.fill': 'email',
  'lock.fill': 'lock',
  'eye.fill': 'visibility',
  'eye.slash.fill': 'visibility-off',
  'textformat': 'title',
  'text.alignleft': 'notes',

  // Status & feedback
  'info.circle.fill': 'info',
  'exclamationmark.triangle.fill': 'warning',
  'star.fill': 'star',

  // Misc
  'magnifyingglass': 'search',
  'gearshape.fill': 'settings',
} as IconMapping;

/**
 * An icon component that uses native SF Symbols on iOS, and Material Icons on Android and web.
 * This ensures a consistent look across platforms, and optimal resource usage.
 * Icon `name`s are based on SF Symbols and require manual mapping to Material Icons.
 */
export function IconSymbol({
  name,
  size = 24,
  color,
  style,
}: {
  name: IconSymbolName;
  size?: number;
  color: string | OpaqueColorValue;
  style?: StyleProp<TextStyle>;
  weight?: SymbolWeight;
}) {
  return <MaterialIcons color={color} size={size} name={MAPPING[name]} style={style} />;
}
