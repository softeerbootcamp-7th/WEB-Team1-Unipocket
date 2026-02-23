import {
  TabContent,
  TabList,
  TabProvider,
  TabTrigger,
} from '@/components/common/Tab';
import { DataTable } from '@/components/data-table/DataTable';
import DataTableProvider from '@/components/data-table/DataTableProvider';
import { columns } from '@/components/home-page/columns';
import { getData } from '@/components/landing-page/dummy';
import TabImage from '@/components/upload/image-upload/TabImage';

import type { Expense } from '@/api/expenses/type';
import { LandingImages } from '@/assets';

const ImageResultContent = () => {
  const data = getData();
  return (
    <div>
      <div className="px-4.25">
        <TabProvider variant="underline" defaultValue="sample1">
          <TabList>
            <TabTrigger value="sample1">
              <TabImage
                thumbnailUrl={LandingImages.DemoReceipt1}
                fileName={'IMG_1234dfsd.jpg'}
                successCount={3}
                warningCount={4}
              />
            </TabTrigger>
            <TabTrigger value="sample2">
              <TabImage
                thumbnailUrl={LandingImages.DemoReceipt1}
                fileName={'IMG_1234dfsd.jpg'}
                successCount={1}
                warningCount={0}
              />
            </TabTrigger>
          </TabList>
          <div className="h-10" />
          <TabContent value="sample1">
            <div className="flex flex-col gap-4.5 lg:flex-row">
              <div className="bg-background-alternative flex items-center justify-center rounded-2xl border border-gray-200 p-2.5">
                <img
                  src={LandingImages.DemoReceipt1}
                  className="h-120 rounded-lg"
                />
              </div>
              <div className="shadow-semantic-subtle h-fit min-w-0 flex-1 rounded-2xl px-2 py-4">
                <DataTableProvider columns={columns} data={data}>
                  <DataTable
                    enableGroupSelection={false}
                    groupBy={(row: Expense) =>
                      new Date(row.occurredAt).toLocaleDateString('ko-KR', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                      })
                    }
                  />
                </DataTableProvider>
              </div>
            </div>
          </TabContent>
          <TabContent value="sample2">
            <div className="flex flex-col gap-4.5 lg:flex-row">
              <div className="bg-background-alternative flex items-center justify-center rounded-2xl border border-gray-200 p-2.5">
                <img
                  src={LandingImages.DemoReceipt1}
                  className="h-120 rounded-lg"
                />
              </div>
              <div className="shadow-semantic-subtle h-fit min-w-0 flex-1 rounded-2xl px-2 py-4">
                <DataTableProvider columns={columns} data={data}>
                  <DataTable
                    enableGroupSelection={false}
                    groupBy={(row: Expense) =>
                      new Date(row.occurredAt).toLocaleDateString('ko-KR', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit',
                      })
                    }
                  />
                </DataTableProvider>
              </div>
            </div>
          </TabContent>
        </TabProvider>
      </div>
    </div>
  );
};

export default ImageResultContent;
