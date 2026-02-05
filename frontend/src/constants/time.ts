import type { CountryCode } from '@/data/countryCode';
import timeData from '@/data/countryTime.json';

export type TimeRegion =
  | 'KOREA'
  | 'JAPAN'
  | 'CHINA'
  | 'EUROPE'
  | 'US_EAST'
  | 'OCEANIA'
  | 'MIDDLE_EAST'
  | 'DEFAULT';

export const COUNTRY_TIME_REGION = timeData.countryTimeRegion as Partial<
  Record<CountryCode, TimeRegion>
>;

export const TIME_REGION_CONFIG: Record<
  TimeRegion,
  {
    locale: string;
    timeZone: string;
    hour12: boolean;
  }
> = {
  KOREA: {
    locale: 'ko-KR',
    timeZone: 'Asia/Seoul',
    hour12: true,
  },
  JAPAN: {
    locale: 'ja-JP',
    timeZone: 'Asia/Tokyo',
    hour12: false,
  },
  CHINA: {
    locale: 'zh-CN',
    timeZone: 'Asia/Shanghai',
    hour12: false,
  },
  US_EAST: {
    locale: 'en-US',
    timeZone: 'America/New_York',
    hour12: true,
  },
  EUROPE: {
    locale: 'en-GB',
    timeZone: 'Europe/Paris',
    hour12: false,
  },
  OCEANIA: {
    locale: 'en-AU',
    timeZone: 'Australia/Sydney',
    hour12: true,
  },
  MIDDLE_EAST: {
    locale: 'ar-AE',
    timeZone: 'Asia/Dubai',
    hour12: true,
  },
  DEFAULT: {
    locale: 'en-US',
    timeZone: 'UTC',
    hour12: true,
  },
};
