import i18n from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import { initReactI18next } from 'react-i18next';

import deRes from './locales/de.json';
import enRes from './locales/en.json';

i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        resources: {
            de: { translation: deRes },
            en: { translation: enRes }
        },
        fallbackLng: 'en',
        debug: true,
    });

export default i18n;