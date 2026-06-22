import { useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Code,
  CopyButton,
  Divider,
  Modal,
  ScrollArea,
  Stack,
  Stepper,
  Text,
  Textarea,
  TextInput,
} from '@mantine/core';
import {
  IconCheck,
  IconCopy,
  IconExternalLink,
  IconRobot,
  IconSparkles,
} from '@tabler/icons-react';
import { notifications } from '@mantine/notifications';
import { useTranslation } from 'react-i18next';
import { buildFullImportSchema } from '../../schemas/topic.ts';
import { buildFullImportSchemaText } from '../../constants/full-import-schema.ts';
import { useImportTopicsMutation, useQueryAllCategories } from '../../api/topic.ts';

interface AiAgentModalProps {
  opened: boolean;
  onClose: () => void;
}

const GEMINI_URL = 'https://gemini.google.com/app';

const sanitizeAiJson = (raw: string): string => {
  let text = raw.trim();
  text = text.replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/i, '');
  const first = text.indexOf('{');
  const last = text.lastIndexOf('}');
  if (first !== -1 && last !== -1 && last > first) {
    text = text.slice(first, last + 1);
  }
  text = text.replace(
    /\[\s*https?:\/\/[^\]]*\]\s*\(\s*(https?:\/\/[\s\S]*?)\s*\)/g,
    (_match, url: string) => url.replace(/\s+/g, '')
  );
  return text;
};

const normalizeCategories = (raw: unknown, validTitles: string[], fallback: string): unknown => {
  if (!raw || typeof raw !== 'object') return raw;
  const obj = raw as { topics?: unknown };
  if (!Array.isArray(obj.topics)) return raw;

  const validSet = new Set(validTitles);
  obj.topics = obj.topics.map((topic) => {
    if (!topic || typeof topic !== 'object') return topic;
    const typed = topic as { categories?: unknown };
    const valid = Array.isArray(typed.categories)
      ? typed.categories.filter((c): c is string => typeof c === 'string' && validSet.has(c))
      : [];
    return { ...typed, categories: valid.length > 0 ? valid.slice(0, 2) : [fallback] };
  });
  return obj;
};

const buildPrompt = (schema: string, categoryTitles: string[], subject: string) => {
  const categoryList =
    categoryTitles.length > 0
      ? categoryTitles.map((title) => `- ${title}`).join('\n')
      : '(keine Kategorien verfügbar)';

  const subjectLine = subject.trim()
    ? `Erstelle Lernthemen zum folgenden Thema/Fachgebiet: "${subject.trim()}".`
    : 'Erstelle sinnvolle Lernthemen.';

  return `Du bist ein Experte für die Erstellung von Lernthemen. ${subjectLine} Generiere ein valides JSON-Objekt, das exakt dem unten stehenden JSON-Schema entspricht. Das JSON soll Lernthemen mit Inhaltselementen und Quiz-Karteikarten enthalten.

WICHTIGE REGELN:
1. Antworte ausschließlich mit dem JSON – kein Markdown, keine Code-Blöcke, keine Erklärungen.
2. JEDES Thema MUSS ein Feld "categories" enthalten – ein Array mit 1 bis 2 Werten. Wähle die thematisch am besten passende(n) Kategorie(n) AUSSCHLIESSLICH aus dieser Liste (exakte Schreibweise). Erfinde keine neuen Kategorien:
${categoryList}
3. Gib alle URLs als reine Strings an, niemals als Markdown-Links. Richtig: "uri": "https://example.com". Falsch: "uri": "[https://example.com](https://example.com)".
4. Jedes Thema muss mindestens ein Inhaltselement haben.

JSON-Schema:
${schema}`;
};

const AiAgentModal = ({ opened, onClose }: AiAgentModalProps) => {
  const { t } = useTranslation();
  const [step, setStep] = useState(0);
  const [subject, setSubject] = useState('');
  const [jsonInput, setJsonInput] = useState('');
  const [importError, setImportError] = useState<string | null>(null);
  const { mutate: importTopics, isPending: isImporting } = useImportTopicsMutation();
  const { data: categories } = useQueryAllCategories();

  const categoryTitles = useMemo(() => (categories ?? []).map((c) => c.title), [categories]);
  const prompt = useMemo(
    () => buildPrompt(buildFullImportSchemaText(categoryTitles), categoryTitles, subject),
    [categoryTitles, subject]
  );
  const hasSubject = subject.trim().length > 0;

  const handleClose = () => {
    setStep(0);
    setSubject('');
    setJsonInput('');
    setImportError(null);
    onClose();
  };

  const handleImport = () => {
    setImportError(null);
    const fallbackCategory = categoryTitles[0];
    if (!fallbackCategory) {
      setImportError(t('topic.aiAgent.noCategory'));
      return;
    }
    try {
      const parsed = JSON.parse(sanitizeAiJson(jsonInput));
      const normalized = normalizeCategories(parsed, categoryTitles, fallbackCategory);
      const result = buildFullImportSchema(categoryTitles).safeParse(normalized);
      if (!result.success) {
        setImportError(result.error.issues[0]?.message ?? t('topic.actions.importJsonError'));
        return;
      }
      importTopics(result.data, {
        onSuccess: () => {
          notifications.show({ color: 'green', message: t('topic.actions.importJsonSuccess') });
          handleClose();
        },
        onError: () => {
          setImportError(t('topic.actions.importJsonError'));
        },
      });
    } catch {
      setImportError(t('topic.aiAgent.invalidJson'));
    }
  };

  return (
    <Modal
      opened={opened}
      onClose={handleClose}
      title={t('topic.aiAgent.title')}
      centered
      size="lg"
    >
      <Stepper active={step} onStepClick={setStep} size="sm">
        <Stepper.Step label={t('topic.aiAgent.stepPrompt')} icon={<IconSparkles size={18} />}>
          <Stack gap="md" mt="md">
            <Text size="sm">{t('topic.aiAgent.promptHint')}</Text>
            <TextInput
              label={t('topic.aiAgent.subjectLabel')}
              description={t('topic.aiAgent.subjectRequired')}
              placeholder={t('topic.aiAgent.subjectPlaceholder')}
              value={subject}
              onChange={(e) => setSubject(e.currentTarget.value)}
              withAsterisk
              data-autofocus
            />
            <ScrollArea h={200} type="auto">
              <Code block>{prompt}</Code>
            </ScrollArea>
            <CopyButton value={prompt} timeout={2000}>
              {({ copied, copy }) => (
                <Button
                  variant="light"
                  color={copied ? 'teal' : 'blue'}
                  leftSection={copied ? <IconCheck size={16} /> : <IconCopy size={16} />}
                  onClick={copy}
                  disabled={!hasSubject}
                  fullWidth
                >
                  {copied ? t('common.copied') : t('topic.aiAgent.copyPrompt')}
                </Button>
              )}
            </CopyButton>
            <Button
              component="a"
              href={GEMINI_URL}
              target="_blank"
              rel="noopener noreferrer"
              variant="filled"
              leftSection={<IconExternalLink size={16} />}
              fullWidth
            >
              {t('topic.aiAgent.openGemini')}
            </Button>
            <Divider />
            <Button variant="default" onClick={() => setStep(1)} disabled={!hasSubject} fullWidth>
              {t('topic.aiAgent.nextStep')}
            </Button>
          </Stack>
        </Stepper.Step>

        <Stepper.Step label={t('topic.aiAgent.stepImport')} icon={<IconRobot size={18} />}>
          <Stack gap="md" mt="md">
            <Text size="sm">{t('topic.aiAgent.pasteHint')}</Text>
            <Textarea
              value={jsonInput}
              onChange={(e) => {
                setJsonInput(e.currentTarget.value);
                setImportError(null);
              }}
              placeholder='{"topics": [...], "associations": {...}}'
              autosize
              minRows={6}
              maxRows={14}
            />
            {importError && (
              <Alert color="red" variant="light">
                {importError}
              </Alert>
            )}
            <Button
              fullWidth
              disabled={!jsonInput.trim()}
              loading={isImporting}
              onClick={handleImport}
            >
              {t('topic.actions.importJsonSubmit')}
            </Button>
          </Stack>
        </Stepper.Step>
      </Stepper>
    </Modal>
  );
};

export default AiAgentModal;
