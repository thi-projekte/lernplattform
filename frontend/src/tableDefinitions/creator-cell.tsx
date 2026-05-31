import { Avatar, Group } from '@mantine/core';
import { IconUser } from '@tabler/icons-react';
import CategoryBadge from '../components/category-badge.tsx';
import { useQueryProfilePicture } from '../api/profile-picture.ts';

const CreatorCell = ({
  creatorId,
  creatorFullName,
}: {
  creatorId?: string;
  creatorFullName?: string;
}) => {
  const { data: profilePicture } = useQueryProfilePicture(creatorId);
  return (
    <Group gap="xs" align="center">
      <Avatar src={profilePicture?.url ?? null} size={20} radius="xl">
        <IconUser size={12} />
      </Avatar>
      <CategoryBadge
        title={creatorFullName ?? ''}
        color="#4c6ef5"
        size="sm"
        style={{ textTransform: 'uppercase' }}
      />
    </Group>
  );
};

export default CreatorCell;
