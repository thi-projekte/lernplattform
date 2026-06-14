import { useMemo, useState } from 'react';
import Joyride, { ACTIONS, EVENTS, type CallBackProps, type Step, STATUS } from 'react-joyride';
import { useTranslation } from 'react-i18next';
import { useMantineColorScheme, useMantineTheme } from '@mantine/core';
import { useUserService } from '../provider/user-provider.tsx';
import { TOUR_STORAGE_KEY, TOUR_STEP_STORAGE_KEY } from './onboarding-tour.constants.ts';

export const OnboardingTour = () => {
    const { t } = useTranslation();
    const userService = useUserService();
    const theme = useMantineTheme();
    const { colorScheme } = useMantineColorScheme();
    const [run, setRun] = useState(() => localStorage.getItem(TOUR_STORAGE_KEY) === 'true');
    const [stepIndex, setStepIndex] = useState(() =>
        Number(localStorage.getItem(TOUR_STEP_STORAGE_KEY) ?? 0)
    );

    const isBuilder = userService.roles.includes('builder');
    const rolesLoaded = userService.roles.length > 0;

    const commonSteps: Step[] = useMemo(
        () => [
            {
                target: '[data-tour="nav-dashboard"]',
                content: t('onboardingTour.common.dashboard') as string,
                disableBeacon: true,
            },
            {
                target: '[data-tour="nav-search"]',
                content: t('onboardingTour.common.search') as string,
                disableBeacon: true,
            },
            {
                target: '[data-tour="nav-challenges"]',
                content: t('onboardingTour.common.challenges') as string,
                disableBeacon: true,
            },
            {
                target: '[data-tour="nav-streaks"]',
                content: t('onboardingTour.common.streaks') as string,
                disableBeacon: true,
            },
            {
                target: '[data-tour="nav-invitations"]',
                content: t('onboardingTour.common.invitations') as string,
                disableBeacon: true,
            },
            {
                target: '[data-tour="nav-subscription"]',
                content: t('onboardingTour.common.subscription') as string,
                disableBeacon: true,
            },
        ],
        [t]
    );

    const learnerSteps: Step[] = useMemo(
        () => [
            {
                target: 'body',
                placement: 'center',
                content: t('onboardingTour.learner.welcome') as string,
                disableBeacon: true,
            },
            ...commonSteps,
        ],
        [t, commonSteps]
    );

    const builderSteps: Step[] = useMemo(
        () => [
            {
                target: 'body',
                placement: 'center',
                content: t('onboardingTour.builder.welcome') as string,
                disableBeacon: true,
            },
            ...commonSteps,
            {
                target: '[data-tour="nav-builder-mode"]',
                content: t('onboardingTour.builder.builderMode') as string,
                disableBeacon: true,
            },
        ],
        [t, commonSteps]
    );

    const steps = isBuilder ? builderSteps : learnerSteps;

    const handleCallback = (data: CallBackProps) => {
        const { status, type, index, action } = data;

        if (status === STATUS.FINISHED || status === STATUS.SKIPPED) {
            localStorage.removeItem(TOUR_STORAGE_KEY);
            localStorage.removeItem(TOUR_STEP_STORAGE_KEY);
            setRun(false);
            setStepIndex(0);
            return;
        }

        if (type === EVENTS.STEP_AFTER || type === EVENTS.TARGET_NOT_FOUND) {
            const nextIndex = index + (action === ACTIONS.PREV ? -1 : 1);

            if (nextIndex >= steps.length) {
                localStorage.removeItem(TOUR_STORAGE_KEY);
                localStorage.removeItem(TOUR_STEP_STORAGE_KEY);
                setRun(false);
                setStepIndex(0);
                return;
            }

            setStepIndex(nextIndex);
            localStorage.setItem(TOUR_STEP_STORAGE_KEY, String(nextIndex));
        }
    };

    if (!run || !rolesLoaded) return null;

    const isDark = colorScheme === 'dark';
    const safeStepIndex = Math.min(stepIndex, steps.length - 1);

    return (
        <Joyride
            steps={steps}
            run={run}
            stepIndex={safeStepIndex}
            continuous
            showSkipButton
            showProgress
            disableScrolling
            spotlightClicks
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
                    primaryColor: theme.colors.brand[6],
                    arrowColor: isDark ? '#1e2533' : '#fff',
                    backgroundColor: isDark ? '#1e2533' : '#fff',
                    textColor: isDark ? theme.colors.brandGray[1] : '#1a1a1a',
                    overlayColor: 'rgba(10, 14, 24, 0.6)',
                },
                buttonNext: {
                    backgroundColor: theme.colors.brand[6],
                    color: '#fff',
                    borderRadius: 6,
                },
                buttonBack: {
                    color: theme.colors.brand[6],
                },
                buttonSkip: {
                    color: theme.colors.brandGray[5],
                },
                tooltip: {
                    borderRadius: 8,
                },
            }}
        />
    );
};