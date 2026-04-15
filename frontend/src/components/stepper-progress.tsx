import  { type ReactNode, useState } from 'react';
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
}

const StepperProgress = ({steps}: StepperProgressProps) => {

  const [active, setActive] = useState(0);

  const nextStep = () => setActive((current) => (current < 3 ? current + 1 : current));
  const prevStep = () => setActive((current) => (current > 0 ? current - 1 : current));

  const {t} = useTranslation();

  const canProceed = steps[active].canProceed;


  return (
    <>
      <Stepper active={active} onStepClick={setActive}>
        {steps.map((step) => (
          <Stepper.Step label={step.label} description={step.description}>
            {step.step}
          </Stepper.Step>
        ))}
      </Stepper>
      <Group justify="space-between" mt="xl">
        <Button variant="default" onClick={prevStep} disabled={active === 1}>
          {t("common.back")}
        </Button>
        <Button onClick={nextStep} disabled={!canProceed}>{t("common.next")}</Button>
      </Group>
    </>
  );
}

export default StepperProgress;