// components/category-badge.tsx

import { Badge, type BadgeProps } from '@mantine/core';

interface CategoryBadgeProps extends Omit<BadgeProps, 'color'> {
  title: string;
  color: string;
}

const CategoryBadge = ({ title, color, style, ...props }: CategoryBadgeProps) => {
  const categoryColor = color.startsWith('#') ? color : `#${color}`;

  return (
    <Badge
      radius="xl"
      variant="light"
      style={{
        color: `color-mix(in srgb, ${categoryColor} 85%, black)`,
        background: `color-mix(in srgb, ${categoryColor} 22%, white)`,
        border: `1px solid color-mix(in srgb, ${categoryColor} 45%, white)`,
        textTransform: 'none',
        fontWeight: 700,
        ...style,
      }}
      {...props}
    >
      {title}
    </Badge>
  );
};

export default CategoryBadge;
