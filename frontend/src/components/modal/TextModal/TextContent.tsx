import type React from 'react';

interface ContentProps {
  title: string;
  description?: string;
  subDescription?: React.ReactNode;
}

const TextContent = ({ title, description, subDescription }: ContentProps) => {
  return (
    <div className="flex h-fit w-83.75 flex-col gap-13">
      <div className="flex flex-col items-center gap-2.5">
        <h2 className="text-label-normal headline1-bold">{title}</h2>
        {description && (
          <span className="text-label-alternative body1-normal-medium text-center whitespace-pre-line">
            {description}
          </span>
        )}
        <p className="body1-normal-medium text-label-neutral min-h-32 p-10 text-center whitespace-pre-line">
          {subDescription}
        </p>
      </div>
    </div>
  );
};

export default TextContent;
