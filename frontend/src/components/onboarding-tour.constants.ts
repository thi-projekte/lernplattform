export const TOUR_STORAGE_KEY = 'onboardingTourPending';

export const markOnboardingTourPending = () => {
  localStorage.setItem(TOUR_STORAGE_KEY, 'true');
};
