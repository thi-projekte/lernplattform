import { useRef, useState } from 'react';
import { Badge, Box, Button, Group, Modal, Stack, Text, ThemeIcon, Title } from '@mantine/core';
import {
  IconBulb,
  IconChevronLeft,
  IconChevronRight,
  IconQuestionMark,
  IconSparkles,
} from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';
import type { IndexCardDto } from '../../schemas/topic.ts';
import classes from './quiz-view.module.css';

interface QuizViewProps {
  indexCards: IndexCardDto[];
}

const CARD_MAX_WIDTH = 580;

const QuizView = ({ indexCards }: QuizViewProps) => {
  const { t } = useTranslation();
  const [current, setCurrent] = useState(0);
  const [showAnswer, setShowAnswer] = useState(false);
  const [celebrationOpen, setCelebrationOpen] = useState(false);
  // The celebration is shown only once per quiz session.
  const celebrationShown = useRef(false);

  if (indexCards.length === 0) {
    return (
      <Text c="dimmed" size="sm" ta="center" py="xl">
        {t('topic.quiz.viewEmptyHint')}
      </Text>
    );
  }

  const total = indexCards.length;
  const index = Math.min(current, total - 1);
  const card = indexCards[index];
  const isLastCard = index === total - 1;

  const goTo = (next: number) => {
    setShowAnswer(false);
    setCurrent((next + total) % total);
  };

  // Clicking "Next" on the last card finishes the quiz and shows the celebration once.
  const handleNext = () => {
    if (isLastCard && !celebrationShown.current) {
      celebrationShown.current = true;
      setCelebrationOpen(true);
      return;
    }
    goTo(index + 1);
  };

  const flip = () => setShowAnswer((value) => !value);

  return (
    <Stack gap="lg" align="center">
      <Group justify="space-between" align="center" w="100%" maw={CARD_MAX_WIDTH}>
        <Title order={4}>{t('topic.quiz.viewTitle')}</Title>
        <Badge variant="light" color="blue" radius="sm" size="lg">
          {index + 1} / {total}
        </Badge>
      </Group>

      <Box
        role="button"
        tabIndex={0}
        onClick={flip}
        onKeyDown={(event) => {
          if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            flip();
          }
        }}
        className={`${classes.card} ${showAnswer ? classes.answer : classes.question}`}
      >
        {/* key forces the fade-in animation to replay on every flip / navigation */}
        <div className={classes.content} key={`${index}-${showAnswer}`}>
          <Group gap={8}>
            <ThemeIcon variant="light" color={showAnswer ? 'teal' : 'blue'} radius="xl" size="md">
              {showAnswer ? <IconBulb size={16} /> : <IconQuestionMark size={16} />}
            </ThemeIcon>
            <Text
              size="xs"
              fw={700}
              tt="uppercase"
              c={showAnswer ? 'teal.4' : 'blue.4'}
              style={{ letterSpacing: 1 }}
            >
              {showAnswer ? t('topic.quiz.answer') : t('topic.quiz.question')}
            </Text>
          </Group>

          <Box className={classes.body}>
            <Text
              size={showAnswer ? 'lg' : 'xl'}
              fw={showAnswer ? 400 : 600}
              ta="center"
              style={{ whiteSpace: 'pre-wrap', lineHeight: 1.5 }}
            >
              {showAnswer ? card.answer : card.question}
            </Text>
          </Box>

          <Text size="xs" c="dimmed" ta="center">
            {showAnswer ? t('topic.quiz.showQuestionHint') : t('topic.quiz.showAnswerHint')}
          </Text>
        </div>
      </Box>

      {total > 1 && total <= 12 && (
        <Group gap={6} justify="center">
          {indexCards.map((dot, i) => (
            <Box
              key={dot.id ?? i}
              onClick={() => goTo(i)}
              style={{
                width: i === index ? 24 : 8,
                height: 8,
                borderRadius: 999,
                cursor: 'pointer',
                background:
                  i === index ? 'var(--mantine-color-blue-5)' : 'rgba(34, 139, 230, 0.25)',
                transition: 'width 0.25s, background 0.25s',
              }}
            />
          ))}
        </Group>
      )}

      <Group justify="center" gap="sm" w="100%" maw={CARD_MAX_WIDTH}>
        <Button
          variant="light"
          color="gray"
          radius="xl"
          size="md"
          leftSection={<IconChevronLeft size={16} />}
          onClick={() => goTo(index - 1)}
          disabled={total <= 1}
        >
          {t('topic.quiz.previous')}
        </Button>
        <Button
          radius="xl"
          size="md"
          rightSection={<IconChevronRight size={16} />}
          onClick={handleNext}
          disabled={total <= 1}
        >
          {t('topic.quiz.next')}
        </Button>
      </Group>

      <Modal
        opened={celebrationOpen}
        onClose={() => setCelebrationOpen(false)}
        centered
        withCloseButton={false}
        radius="lg"
        size="sm"
        padding="xl"
        overlayProps={{ backgroundOpacity: 0.6, blur: 4 }}
      >
        <Stack align="center" justify="center" gap="md" w="100%" ta="center">
          <ThemeIcon variant="light" color="yellow" radius="xl" size={64}>
            <IconSparkles size={36} />
          </ThemeIcon>
          <Title order={3}>{t('topic.quiz.celebrationTitle')}</Title>
          <Text c="dimmed">{t('topic.quiz.celebrationMessage')}</Text>
          <Button radius="xl" size="md" mt="xs" onClick={() => setCelebrationOpen(false)}>
            {t('topic.quiz.celebrationClose')}
          </Button>
        </Stack>
      </Modal>
    </Stack>
  );
};

export default QuizView;
