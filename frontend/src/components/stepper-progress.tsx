import { type ReactNode, useState } from 'react';
import { Button, Group, Stepper } from '@mantine/core';
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

  const nextStep = () => setActive((current) => (current < 3 ? current + 1 : current));
  const prevStep = () => setActive((current) => (current > 0 ? current - 1 : current));

  const { t } = useTranslation();

  const isLastStep = active === steps.length -1;

  const canProceed = (idx: number) => idx < steps.length && (idx === -1 || steps[idx].canProceed);

  return (
    <>
      <Stepper active={active} onStepClick={setActive}>
        {steps.map((step, i) => (
          <Stepper.Step
            label={step.label}
            description={step.description}
            allowStepSelect={canProceed(i - 1)}
          >
            {step.step}
          </Stepper.Step>
        ))}
      </Stepper>
      <Group justify="space-between" mt="xl">
        <Button variant="default" onClick={prevStep} disabled={active === 1 || isLoading}>
          {t('common.back')}
        </Button>
        <Button onClick={isLastStep ? onComplete : nextStep} disabled={!canProceed(active)} loading={isLoading}>
          {isLastStep && lastStepLabel ? lastStepLabel : t('common.next')}
        </Button>
      </Group>
    </>
  );
};

export default StepperProgress;
