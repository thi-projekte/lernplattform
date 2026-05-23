import { createTheme, type MantineColorsTuple } from '@mantine/core';

const brand: MantineColorsTuple = [
  '#f0f8fd',
  '#ddf1fb',
  '#b8e3f6',
  '#92d3f0',
  '#7CC6E8',
  '#5ab5e0',
  '#3aa3d8',
  '#2d8ab8',
  '#236e92',
  '#1a5270',
];

const brandGreen: MantineColorsTuple = [
  '#f0fbf4',
  '#d9f5e3',
  '#b5ecc8',
  '#90e0ac',
  '#7DD49B',
  '#5ec882',
  '#43b86b',
  '#349856',
  '#287543',
  '#1d5631',
];

const brandRed: MantineColorsTuple = [
  '#fdf2f2',
  '#fae0e0',
  '#f5bebe',
  '#ef9b9b',
  '#E86A6A',
  '#e04040',
  '#d12828',
  '#b01f1f',
  '#8c1818',
  '#671212',
];

const brandYellow: MantineColorsTuple = [
  '#fef9ed',
  '#fdf0cf',
  '#fae3a0',
  '#f8d57a',
  '#F4C95D',
  '#f0b830',
  '#e6a510',
  '#c48b0d',
  '#9e700a',
  '#775408',
];

const brandGray: MantineColorsTuple = [
  '#ffffff',
  '#eef0f3',
  '#e4e8ec',
  '#D6DCE2',
  '#c2cad3',
  '#adb8c3',
  '#8e9fae',
  '#6f8698',
  '#516679',
  '#35475a',
];

const appFontFamily =
  "Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif";

export const theme = createTheme({
  primaryColor: 'brand',
  primaryShade: 6,
  defaultRadius: 'md',
  colors: {
    brand,
    brandGreen,
    brandRed,
    brandYellow,
    brandGray,
  },
  other: {
    layoutHeaderBg: '#f1f7fb',
    layoutNavbarBg: '#f1f7fb',
    layoutMainBg: 'linear-gradient(180deg, #eef5fa 0%, #f5fafd 60%, #f1f8fc 100%)',
    layoutBorder: 'rgba(176, 210, 232, 0.5)',
    graphBg: '#fafbfc',
    graphDots: '#d1d5db',
    nodeOwnBg: 'linear-gradient(135deg, #f3f8fc 0%, #fbf0d8 100%)',
    nodeOwnSelectedBg: 'linear-gradient(135deg, #eaf2f8 0%, #f6e5be 100%)',
    nodeForeignBg: 'linear-gradient(135deg, #f4f9fe 0%, #e8f1f9 100%)',
    nodeForeignSelectedBg: 'linear-gradient(135deg, #e6f0fa 0%, #d4e4f3 100%)',
    nodeJourneyBg: 'linear-gradient(135deg, #e7f2ff 0%, #fff9e6 100%)',
  },
  fontFamily: appFontFamily,
  fontFamilyMonospace:
    "ui-monospace, 'Cascadia Code', 'Source Code Pro', Menlo, Consolas, monospace",
  headings: {
    fontFamily: appFontFamily,
    fontWeight: '750',
    sizes: {
      h1: { fontSize: '1.875rem', lineHeight: '1.2' },
      h2: { fontSize: '1.375rem', lineHeight: '1.3' },
      h3: { fontSize: '1.125rem', lineHeight: '1.35' },
    },
  },
});
