import {
  ActionIcon,
  Badge,
  Box,
  Button,
  ColorInput,
  Group,
  Loader,
  Modal,
  Paper,
  Select,
  Stack,
  Text,
  TextInput,
  Title,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { useDisclosure } from '@mantine/hooks';
import { notifications } from '@mantine/notifications';
import { IconEdit, IconPlus, IconTree } from '@tabler/icons-react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  useCreateCategoryMutation,
  useQueryCategoryTree,
  useUpdateCategoryMutation,
} from '../../api/category.ts';
import { Layout } from '../../components/layout.tsx';
import type { CategoryTreeDto } from '../../schemas/category.ts';

interface CategoryFormValues {
  title: string;
  color: string;
  parentId: string | null;
}

interface FlatCategory {
  id: string;
  title: string;
  depth: number;
}

const flattenTree = (nodes: CategoryTreeDto[], depth = 0): FlatCategory[] => {
  return nodes.flatMap((node) => [
    { id: node.id, title: node.title, depth },
    ...flattenTree(node.children, depth + 1),
  ]);
};

interface CategoryRowProps {
  node: CategoryTreeDto;
  depth: number;
  onEdit: (node: CategoryTreeDto) => void;
}

const CategoryRow = ({ node, depth, onEdit }: CategoryRowProps) => {
  return (
    <>
      <Group
        px="md"
        py="xs"
        style={{
          paddingLeft: `calc(var(--mantine-spacing-md) + ${depth * 24}px)`,
          borderBottom: '1px solid var(--mantine-color-default-border)',
        }}
        justify="space-between"
        wrap="nowrap"
      >
        <Group gap="sm" wrap="nowrap">
          {node.color && (
            <Box
              style={{
                width: 14,
                height: 14,
                borderRadius: 3,
                background: node.color,
                flexShrink: 0,
              }}
            />
          )}
          <Text size="sm" fw={depth === 0 ? 600 : 400}>
            {node.title}
          </Text>
          {node.color && (
            <Badge size="xs" variant="outline" color="gray">
              {node.color}
            </Badge>
          )}
        </Group>
        <ActionIcon variant="subtle" color="gray" size="sm" onClick={() => onEdit(node)}>
          <IconEdit size={14} />
        </ActionIcon>
      </Group>
      {node.children.map((child) => (
        <CategoryRow key={child.id} node={child} depth={depth + 1} onEdit={onEdit} />
      ))}
    </>
  );
};

const AdminCategoriesPage = () => {
  const { t } = useTranslation();
  const [opened, { open, close }] = useDisclosure(false);
  const [editingId, setEditingId] = useState<string | null>(null);

  const { data: tree, isLoading } = useQueryCategoryTree();
  const { mutate: create, isPending: isCreating } = useCreateCategoryMutation();
  const { mutate: update, isPending: isUpdating } = useUpdateCategoryMutation();

  const flat = tree ? flattenTree(tree) : [];

  const form = useForm<CategoryFormValues>({
    initialValues: { title: '', color: '#6366f1', parentId: null },
    validate: {
      title: (v) => (v.trim().length === 0 ? t('common.shouldNotBeEmpty') : null),
      color: (v) => (v.trim().length === 0 ? t('common.shouldNotBeEmpty') : null),
    },
  });

  const openCreate = () => {
    setEditingId(null);
    form.setValues({ title: '', color: '#6366f1', parentId: null });
    open();
  };

  const openEdit = (node: CategoryTreeDto) => {
    setEditingId(node.id);
    form.setValues({ title: node.title, color: node.color ?? '#6366f1', parentId: null });
    open();
  };

  const handleSubmit = (values: CategoryFormValues) => {
    const request = {
      title: values.title.trim(),
      color: values.color.trim(),
      parentId: values.parentId ?? undefined,
    };

    const onSuccess = () => {
      notifications.show({
        color: 'green',
        title: t('common.success'),
        message: editingId ? t('categoryAdmin.updateSuccess') : t('categoryAdmin.createSuccess'),
      });
      close();
    };

    const onError = () => {
      notifications.show({
        color: 'red',
        title: t('common.serverError'),
        message: t('categoryAdmin.saveError'),
      });
    };

    if (editingId) {
      update({ id: editingId, request }, { onSuccess, onError });
    } else {
      create(request, { onSuccess, onError });
    }
  };

  const parentOptions = flat
    .filter((c) => c.id !== editingId)
    .map((c) => ({
      value: c.id,
      label: ' '.repeat(c.depth * 2) + c.title,
    }));

  return (
    <Layout>
      <Stack gap="lg" maw={720}>
        <Group justify="space-between" align="center">
          <Title order={2}>{t('categoryAdmin.title')}</Title>
          <Button leftSection={<IconPlus size={16} />} onClick={openCreate}>
            {t('categoryAdmin.createCategory')}
          </Button>
        </Group>

        {isLoading ? (
          <Loader size="sm" />
        ) : !tree?.length ? (
          <Text c="dimmed" size="sm">
            {t('categoryAdmin.empty')}
          </Text>
        ) : (
          <Paper withBorder radius="md" style={{ overflow: 'hidden' }}>
            <Group
              px="md"
              py="xs"
              style={{ borderBottom: '1px solid var(--mantine-color-default-border)' }}
            >
              <IconTree size={16} />
              <Text size="sm" fw={600} c="dimmed">
                {t('categoryAdmin.treeTitle')}
              </Text>
            </Group>
            {tree.map((node) => (
              <CategoryRow key={node.id} node={node} depth={0} onEdit={openEdit} />
            ))}
          </Paper>
        )}
      </Stack>

      <Modal
        opened={opened}
        onClose={close}
        title={editingId ? t('categoryAdmin.editTitle') : t('categoryAdmin.createTitle')}
        centered
      >
        <form onSubmit={form.onSubmit(handleSubmit)}>
          <Stack gap="sm">
            <TextInput label={t('categoryAdmin.fields.title')} {...form.getInputProps('title')} />
            <ColorInput
              label={t('categoryAdmin.fields.color')}
              {...form.getInputProps('color')}
              format="hex"
            />
            {!editingId && (
              <Select
                label={t('categoryAdmin.fields.parentCategory')}
                placeholder={t('categoryAdmin.fields.parentPlaceholder')}
                data={parentOptions}
                clearable
                searchable
                {...form.getInputProps('parentId')}
              />
            )}
            <Group justify="flex-end" mt="xs">
              <Button variant="default" onClick={close}>
                {t('common.cancel')}
              </Button>
              <Button type="submit" loading={isCreating || isUpdating}>
                {t('common.save')}
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>
    </Layout>
  );
};

export default AdminCategoriesPage;
