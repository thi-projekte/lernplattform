import { useState } from 'react';
import Joyride, { type CallBackProps, type Step, STATUS } from 'react-joyride';
import { useTranslation } from 'react-i18next';
import { useUserService } from '../provider/user-provider.tsx';
import { TOUR_STORAGE_KEY } from './onboarding-tour.constants.ts';

export const OnboardingTour = () => {
  const { t } = useTranslation();
  const userService = useUserService();
  const [run, setRun] = useState(() => localStorage.getItem(TOUR_STORAGE_KEY) === 'true');

  const isBuilder = userService.roles.includes('builder');

  const commonSteps: Step[] = [
    {
      target: '[data-tour="nav-dashboard"]',
      content: t('onboardingTour.common.dashboard'),
    },
    {
      target: '[data-tour="nav-search"]',
      content: t('onboardingTour.common.search'),
    },
    {
      target: '[data-tour="nav-challenges"]',
      content: t('onboardingTour.common.challenges'),
    },
    {
      target: '[data-tour="nav-streaks"]',
      content: t('onboardingTour.common.streaks'),
    },
    {
      target: '[data-tour="nav-invitations"]',
      content: t('onboardingTour.common.invitations'),
    },
    {
      target: '[data-tour="nav-subscription"]',
      content: t('onboardingTour.common.subscription'),
    },
  ];

  const learnerSteps: Step[] = [
    {
      target: 'body',
      placement: 'center',
      content: t('onboardingTour.learner.welcome'),
    },
    ...commonSteps,
  ];

  const builderSteps: Step[] = [
    {
      target: 'body',
      placement: 'center',
      content: t('onboardingTour.builder.welcome'),
    },
    ...commonSteps,
    {
      target: '[data-tour="nav-builder-mode"]',
      content: t('onboardingTour.builder.builderMode'),
    },
  ];

  const handleCallback = (data: CallBackProps) => {
    const { status } = data;
    if (status === STATUS.FINISHED || status === STATUS.SKIPPED) {
      localStorage.removeItem(TOUR_STORAGE_KEY);
      setRun(false);
    }
  };

  if (!run) return null;

  return (
    <Joyride
      steps={isBuilder ? builderSteps : learnerSteps}
      run={run}
      continuous
      showSkipButton
      showProgress
      disableScrolling
      callback={handleCallback}
      locale={{
        back: t('onboardingTour.controls.back'),
        close: t('onboardingTour.controls.close'),
        last: t('onboardingTour.controls.last'),
        next: t('onboardingTour.controls.next'),
        skip: t('onboardingTour.controls.skip'),
      }}
      styles={{
        options: {
          zIndex: 10000,
          primaryColor: '#3aa3d8',
        },
      }}
    />
  );
};
