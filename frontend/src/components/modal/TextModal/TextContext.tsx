interface ContentProps {
  title: string;
  description?: string;
}

const TextContext = ({ title, description }: ContentProps) => {
  return (
    <div className="flex h-60.5 w-83.75 flex-col gap-13">
      <div className="flex flex-col items-center gap-2.5">
        <h2 className="text-label-normal headline1-bold">{title}</h2>
        {description && (
          <span className="text-label-alternative body1-normal-medium text-center">
            {description}
          </span>
        )}
      </div>
    </div>
  );
};

export default TextContext;
