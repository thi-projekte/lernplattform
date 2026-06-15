export const TOUR_STORAGE_KEY = 'onboardingTourPending';
export const TOUR_STEP_STORAGE_KEY = 'onboardingTourStep';

export const markOnboardingTourPending = () => {
  localStorage.setItem(TOUR_STORAGE_KEY, 'true');
  localStorage.setItem(TOUR_STEP_STORAGE_KEY, '0');
};
