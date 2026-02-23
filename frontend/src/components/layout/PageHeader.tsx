interface PageHeaderProps {
  title: string;
  subtitle: string;
}

const PageHeader = ({ title, subtitle }: PageHeaderProps) => {
  return (
    <div className="flex flex-col justify-start gap-3">
      <h1 className="title2-semibold text-label-normal">{title}</h1>
      <h2 className="headline1-medium text-label-alternative">{subtitle}</h2>
    </div>
  );
};

export default PageHeader;
