import { type ReactNode, useState } from 'react';
import { Button, Group, Stepper } from '@mantine/core';
import { useMediaQuery } from '@mantine/hooks';
import { useTranslation } from 'react-i18next';

export interface StepperStep {
  step: ReactNode;
  label: string;
  description?: string;
  canProceed?: boolean;
  // When true, the bottom action / back buttons are hidden on this step.
  // The step content itself is responsible for any required actions.
  hideFooter?: boolean;
}

interface StepperProgressProps {
  steps: StepperStep[];
  onComplete: () => void;
  onBack?: () => void;
  isLoading?: boolean;
  lastStepLabel?: string;
}

const StepperProgress = ({
  steps,
  onComplete,
  onBack,
  isLoading,
  lastStepLabel,
}: StepperProgressProps) => {
  const [active, setActive] = useState(0);
  const isMobile = useMediaQuery('(max-width: 768px)');

  const nextStep = () =>
    setActive((current) => (current < steps.length - 1 ? current + 1 : current));
  const prevStep = () => setActive((current) => (current > 0 ? current - 1 : current));

  const { t } = useTranslation();

  const isLastStep = active === steps.length - 1;
  const currentStep = steps[active];

  const canProceed = (idx: number) => idx < steps.length && (idx === -1 || steps[idx].canProceed);

  return (
    <>
      <Stepper
        active={active}
        onStepClick={setActive}
        // Enforce sequential navigation — user must use the Weiter button to
        // move forward. Clicking on a future step does nothing; clicking on
        // an already-completed previous step is still allowed.
        allowNextStepsSelect={false}
        orientation={isMobile ? 'vertical' : 'horizontal'}
      >
        {steps.map((step, i) => (
          <Stepper.Step
            label={step.label}
            description={step.description}
            key={step.label + i}
          >
            {step.step}
          </Stepper.Step>
        ))}
      </Stepper>
      {!currentStep?.hideFooter && (
        <Group justify="space-between" mt="xl">
          <Button
            variant="default"
            onClick={active === 0 ? onBack : prevStep}
            disabled={active === 0 ? !onBack : false}
            loading={isLoading && active === 0}
          >
            {t('common.back')}
          </Button>
          <Button
            onClick={isLastStep ? onComplete : nextStep}
            disabled={!canProceed(active)}
            loading={isLoading}
          >
            {isLastStep && lastStepLabel ? lastStepLabel : t('common.next')}
          </Button>
        </Group>
      )}
    </>
  );
};

export default StepperProgress;
