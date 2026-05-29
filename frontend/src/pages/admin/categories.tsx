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
import { IconChevronDown, IconChevronRight, IconEdit, IconPlus, IconTrash, IconTree } from '@tabler/icons-react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  useCreateCategoryMutation,
  useDeleteCategoryMutation,
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
  parentId: string | null;
}

const toHex = (color: string) => (color.startsWith('#') ? color : `#${color}`);

const flattenTree = (
  nodes: CategoryTreeDto[],
  depth = 0,
  parentId: string | null = null,
): FlatCategory[] =>
  nodes.flatMap((node) => [
    { id: node.id, title: node.title, depth, parentId },
    ...flattenTree(node.children, depth + 1, node.id),
  ]);

const BORDER = '1.5px solid var(--mantine-color-default-border)';
const INDENT = 20;

const TreeConnector = ({ lineage, isLast }: { lineage: boolean[]; isLast: boolean }) => {
  if (lineage.length === 0) return null;

  return (
    <div style={{ display: 'flex', alignSelf: 'stretch', flexShrink: 0 }}>
      {lineage.map((wasLast, i) => (
        <div
          key={i}
          style={{
            width: INDENT,
            borderLeft: wasLast ? 'none' : BORDER,
          }}
        />
      ))}
      <div style={{ width: INDENT, position: 'relative', flexShrink: 0 }}>
        <div
          style={{
            position: 'absolute',
            left: 0,
            top: 0,
            bottom: isLast ? '50%' : 0,
            borderLeft: BORDER,
          }}
        />
        <div
          style={{
            position: 'absolute',
            left: 0,
            top: '50%',
            right: 0,
            borderBottom: BORDER,
          }}
        />
      </div>
    </div>
  );
};

interface CategoryRowProps {
  node: CategoryTreeDto;
  isLast: boolean;
  lineage: boolean[];
  parentId: string | null;
  expanded: Set<string>;
  onToggle: (id: string) => void;
  onEdit: (node: CategoryTreeDto, parentId: string | null) => void;
  onDelete: (id: string) => void;
}

const CategoryRow = ({ node, isLast, lineage, parentId, expanded, onToggle, onEdit, onDelete }: CategoryRowProps) => {
  const hasChildren = node.children.length > 0;
  const isExpanded = expanded.has(node.id);

  return (
    <>
      <Group
        px="md"
        py={8}
        style={{
          borderBottom: '1px solid var(--mantine-color-default-border)',
          minHeight: 44,
        }}
        justify="space-between"
        wrap="nowrap"
      >
        <Group gap="xs" wrap="nowrap" style={{ minWidth: 0, flex: 1 }}>
          <TreeConnector lineage={lineage} isLast={isLast} />
          {hasChildren ? (
            <ActionIcon
              variant="subtle"
              color="gray"
              size="xs"
              style={{ flexShrink: 0 }}
              onClick={() => onToggle(node.id)}
            >
              {isExpanded ? <IconChevronDown size={13} /> : <IconChevronRight size={13} />}
            </ActionIcon>
          ) : (
            <Box style={{ width: 22, flexShrink: 0 }} />
          )}
          {node.color && (
            <Box
              style={{
                width: 12,
                height: 12,
                borderRadius: 3,
                background: toHex(node.color),
                flexShrink: 0,
              }}
            />
          )}
          <Text size="sm" fw={lineage.length === 0 ? 600 : 400} truncate>
            {node.title}
          </Text>
          {hasChildren && (
            <Badge size="xs" variant="outline" color="dimmed" style={{ flexShrink: 0 }}>
              {node.children.length}
            </Badge>
          )}
        </Group>
        <Group gap={4} wrap="nowrap">
          <ActionIcon variant="subtle" color="gray" size="sm" onClick={() => onEdit(node, parentId)}>
            <IconEdit size={14} />
          </ActionIcon>
          <ActionIcon variant="subtle" color="gray" size="sm" className="delete-hover-red" onClick={() => onDelete(node.id)}>
            <IconTrash size={14} />
          </ActionIcon>
        </Group>
      </Group>
      {isExpanded && node.children.map((child, i) => (
        <CategoryRow
          key={child.id}
          node={child}
          isLast={i === node.children.length - 1}
          lineage={[...lineage, isLast]}
          parentId={node.id}
          expanded={expanded}
          onToggle={onToggle}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      ))}
    </>
  );
};

const AdminCategoriesPage = () => {
  const { t } = useTranslation();
  const [opened, { open, close }] = useDisclosure(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [expanded, setExpanded] = useState<Set<string>>(new Set());

  const toggleExpanded = (id: string) =>
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });

  const { data: tree, isLoading } = useQueryCategoryTree();
  const { mutate: create, isPending: isCreating } = useCreateCategoryMutation();
  const { mutate: update, isPending: isUpdating } = useUpdateCategoryMutation();
  const { mutate: deleteCategory } = useDeleteCategoryMutation();

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

  const handleDelete = (id: string) => {
    deleteCategory(id, {
      onSuccess: () =>
        notifications.show({
          color: 'green',
          title: t('common.success'),
          message: t('categoryAdmin.deleteSuccess'),
        }),
      onError: () =>
        notifications.show({
          color: 'red',
          title: t('common.serverError'),
          message: t('categoryAdmin.deleteError'),
        }),
    });
  };

  const openEdit = (node: CategoryTreeDto, pid: string | null) => {
    setEditingId(node.id);
    form.setValues({ title: node.title, color: node.color ?? '#6366f1', parentId: pid });
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

    const onError = () =>
      notifications.show({
        color: 'red',
        title: t('common.serverError'),
        message: t('categoryAdmin.saveError'),
      });

    if (editingId) {
      update({ id: editingId, request }, { onSuccess, onError });
    } else {
      create(request, { onSuccess, onError });
    }
  };

  const parentOptions = flat
    .filter((c) => c.id !== editingId)
    .map((c) => ({ value: c.id, label: ' '.repeat(c.depth * 4) + c.title }));

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
            {tree.map((node, i) => (
              <CategoryRow
                key={node.id}
                node={node}
                isLast={i === tree.length - 1}
                lineage={[]}
                parentId={null}
                expanded={expanded}
                onToggle={toggleExpanded}
                onEdit={openEdit}
                onDelete={handleDelete}
              />
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
              withEyeDropper={false}
            />
            <Select
              label={t('categoryAdmin.fields.parentCategory')}
              placeholder={t('categoryAdmin.fields.parentPlaceholder')}
              data={parentOptions}
              clearable
              searchable
              {...form.getInputProps('parentId')}
            />
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
