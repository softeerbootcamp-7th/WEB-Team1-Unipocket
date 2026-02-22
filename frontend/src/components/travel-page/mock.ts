import SeoulImage from '@/assets/images/travel/seoul.jpg';
import { COUNTRY_CODE } from '@/data/country/countryCode';

export const folderMap = [
  {
    travelId: 0,
    label: '뉴욕 보스턴',
    dateRange: '2026.01.21 - 2026.01.26',
    localCountryCode: COUNTRY_CODE.US,
    localCountryAmount: 1024,
    baseCountryCode: COUNTRY_CODE.KR,
    baseCountryAmount: 1702320,
    imageUrl: SeoulImage,
  },
  {
    travelId: 1,
    label: '뉴욕 보스턴',
    dateRange: '2026.01.21 - 2026.01.26',
    localCountryCode: COUNTRY_CODE.KR,
    localCountryAmount: 1702320,
    baseCountryCode: COUNTRY_CODE.US,
    baseCountryAmount: 1024,
    imageUrl: null,
  },
];
