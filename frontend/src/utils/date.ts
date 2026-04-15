import dayjs from 'dayjs';

const DEFAULT_FORMAT = 'DD.MM.YYYY HH:mm:ss';


export const formatDate = (date: Date) => dayjs(date).format(DEFAULT_FORMAT)