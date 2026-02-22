import SeoulImage from '@/assets/images/travel/seoul.jpg';
import { COUNTRY_CODE, type CountryCode } from '@/data/country/countryCode';

export interface TravelPocket {
  travelId: number;
  travelPlaceName: string;
  startDate: string;
  endDate: string;
  localCountryCode: CountryCode;
  localCountryAmount: number;
  baseCountryCode: CountryCode;
  baseCountryAmount: number;
  imageKey: string | null;
}

export const mockTravelPockets: TravelPocket[] = [
  {
    travelId: 0,
    travelPlaceName: '뉴욕 보스턴',
    startDate: '2026.01.21',
    endDate: '2026.01.26',
    localCountryCode: COUNTRY_CODE.US,
    localCountryAmount: 1024,
    baseCountryCode: COUNTRY_CODE.KR,
    baseCountryAmount: 1702320,
    imageKey: SeoulImage,
  },
  {
    travelId: 1,
    travelPlaceName: '뉴욕 보스턴',
    startDate: '2026.01.21',
    endDate: '2026.01.26',
    localCountryCode: COUNTRY_CODE.KR,
    localCountryAmount: 1702320,
    baseCountryCode: COUNTRY_CODE.US,
    baseCountryAmount: 1024,
    imageKey: null,
  },
];
