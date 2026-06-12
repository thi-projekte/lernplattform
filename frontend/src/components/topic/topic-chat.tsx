import { useEffect, useRef, useState } from 'react';
import {
  ActionIcon,
  Avatar,
  Badge,
  Box,
  Group,
  Loader,
  Paper,
  Stack,
  Text,
  Textarea,
  Title,
  Tooltip,
} from '@mantine/core';
import { IconFlame, IconSend } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import { useUserService } from '../../provider/user-provider.tsx';
import { useQueryChatHistory } from '../../api/chat.ts';
import { useTopicChat } from '../../hooks/use-topic-chat.ts';

interface TopicChatProps {
  topicId: string;
}

const TopicChat = ({ topicId }: TopicChatProps) => {
  const { t } = useTranslation();
  const userService = useUserService();
  const currentUsername = userService.account.username?.toLowerCase();

  const { data: history, isLoading } = useQueryChatHistory(topicId);
  const { messages, status, sendMessage, seedHistory } = useTopicChat(topicId);
  const [draft, setDraft] = useState('');
  const viewportRef = useRef<HTMLDivElement>(null);

  // History comes newest-first from the backend — reverse to oldest-first.
  useEffect(() => {
    if (history?.results) {
      seedHistory([...history.results].reverse());
    }
  }, [history, seedHistory]);

  // Keep the newest message in view by scrolling the message container itself
  // (scrollIntoView would also scroll the surrounding page).
  useEffect(() => {
    const viewport = viewportRef.current;
    if (viewport) viewport.scrollTop = viewport.scrollHeight;
  }, [messages]);

  const handleSend = () => {
    if (sendMessage(draft)) {
      setDraft('');
    }
  };

  const statusColor = status === 'open' ? 'green' : status === 'connecting' ? 'yellow' : 'red';
  const statusLabel =
    status === 'open'
      ? t('topic.chat.online')
      : status === 'connecting'
        ? t('topic.chat.connecting')
        : t('topic.chat.offline');

  const canSend = status === 'open' && draft.trim().length > 0;

  return (
    <Stack gap="sm" style={{ height: '100%', minHeight: 420 }}>
      <Group justify="space-between" align="center">
        <Title order={4}>{t('topic.tabs.chat')}</Title>
        <Badge size="sm" variant="dot" color={statusColor}>
          {statusLabel}
        </Badge>
      </Group>

      <Box ref={viewportRef} style={{ flex: 1, minHeight: 0, overflowY: 'auto', paddingRight: 8 }}>
        {isLoading ? (
          <Group justify="center" py="xl">
            <Loader size="sm" />
          </Group>
        ) : messages.length === 0 ? (
          <Text c="dimmed" size="sm" ta="center" py="xl">
            {t('topic.chat.empty')}
          </Text>
        ) : (
          <Stack gap="sm">
            {messages.map((m) => {
              const isOwn =
                !!currentUsername && m.sender.creatorId?.toLowerCase() === currentUsername;
              const streak = m.sender.streakToDisplay;

              return (
                <Group
                  key={m.id}
                  justify={isOwn ? 'flex-end' : 'flex-start'}
                  align="flex-start"
                  gap="xs"
                  wrap="nowrap"
                >
                  {!isOwn && (
                    <Avatar
                      size="sm"
                      radius="xl"
                      color="initials"
                      name={m.sender.creatorFullName ?? undefined}
                    />
                  )}
                  <Box style={{ maxWidth: '78%' }}>
                    {!isOwn && (
                      <Group gap={6} mb={4} align="center">
                        <Text size="xs" c="dimmed" fw={600}>
                          {m.sender.creatorFullName}
                        </Text>
                        {streak && streak.streakCount > 0 && (
                          <Tooltip
                            label={t('topic.chat.streakTooltip', { count: streak.streakCount })}
                            withArrow
                          >
                            <Badge
                              size="xs"
                              color="orange"
                              variant="light"
                              leftSection={<IconFlame size={10} />}
                            >
                              {streak.streakCount}
                            </Badge>
                          </Tooltip>
                        )}
                      </Group>
                    )}
                    <Paper
                      px="md"
                      py={8}
                      radius="lg"
                      style={
                        isOwn
                          ? { background: 'var(--mantine-primary-color-filled)' }
                          : { background: 'var(--card-bg)', border: '1px solid var(--card-border)' }
                      }
                    >
                      <Text
                        size="sm"
                        c={isOwn ? 'white' : undefined}
                        style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}
                      >
                        {m.message}
                      </Text>
                    </Paper>
                  </Box>
                </Group>
              );
            })}
          </Stack>
        )}
      </Box>

      <Group gap="xs" wrap="nowrap" align="flex-end">
        <Textarea
          flex={1}
          autosize
          minRows={1}
          maxRows={4}
          radius="md"
          placeholder={t('topic.chat.inputPlaceholder')}
          value={draft}
          onChange={(event) => setDraft(event.currentTarget.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter' && !event.shiftKey) {
              event.preventDefault();
              handleSend();
            }
          }}
          disabled={status !== 'open'}
        />
        <ActionIcon
          size={42}
          radius="md"
          onClick={handleSend}
          disabled={!canSend}
          aria-label={t('topic.chat.send')}
        >
          <IconSend size={18} />
        </ActionIcon>
      </Group>
    </Stack>
  );
};

export default TopicChat;
