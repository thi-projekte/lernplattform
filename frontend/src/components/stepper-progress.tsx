import { type ReactNode, useState } from 'react';
import { Button, Group, Stepper } from '@mantine/core';
import { useMediaQuery } from '@mantine/hooks';
import { useTranslation } from 'react-i18next';

export interface StepperStep {
  step: ReactNode;
  label: string;
  description?: string;
  canProceed?: boolean;
}

interface StepperProgressProps {
  steps: StepperStep[];
  onComplete: () => void;
  isLoading?: boolean;
  lastStepLabel?: string;
}

const StepperProgress = ({ steps, onComplete, isLoading, lastStepLabel }: StepperProgressProps) => {
  const [active, setActive] = useState(0);
  const isMobile = useMediaQuery('(max-width: 768px)');

  const nextStep = () => setActive((current) => (current < 3 ? current + 1 : current));
  const prevStep = () => setActive((current) => (current > 0 ? current - 1 : current));

  const { t } = useTranslation();

  const isLastStep = active === steps.length - 1;

  const canProceed = (idx: number) => idx < steps.length && (idx === -1 || steps[idx].canProceed);

  return (
    <>
      <Stepper active={active} onStepClick={setActive} orientation={isMobile ? 'vertical' : 'horizontal'}>
        {steps.map((step, i) => (
          <Stepper.Step
            label={step.label}
            description={step.description}
            allowStepSelect={canProceed(i - 1)}
            key={step.label + i}
          >
            {step.step}
          </Stepper.Step>
        ))}
      </Stepper>
      <Group justify="space-between" mt="xl">
        <Button variant="default" onClick={prevStep} disabled={active === 0 || isLoading}>
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
    </>
  );
};

export default StepperProgress;
