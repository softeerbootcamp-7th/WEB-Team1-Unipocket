import { useWidgetManagerCore } from '@/components/chart/widget/hook/useWidgetManager';
import { WIDGET_TYPES } from '@/components/chart/widget/type';

import {
  useTravelWidgetLayoutQuery,
  useUpdateTravelWidgetLayoutMutation,
} from '@/api/travels/query';

const TRAVEL_ALLOWED_WIDGETS = WIDGET_TYPES.filter(
  (type) => type !== 'COMPARISON',
);

export const useTravelWidgetManager = (travelId: string | number) => {
  const { data: layoutData } = useTravelWidgetLayoutQuery(travelId);
  const { mutate: saveLayout } = useUpdateTravelWidgetLayoutMutation(travelId);

  return {
    travelId,
    ...useWidgetManagerCore({
      layoutData,
      saveLayout,
      allowedWidgetTypes: TRAVEL_ALLOWED_WIDGETS,
    }),
  };
};
