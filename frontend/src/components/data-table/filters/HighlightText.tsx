interface HighlightTextProps {
  text: string;
  highlight: string;
}

const HighlightText = ({ text, highlight }: HighlightTextProps) => {
  if (!highlight.trim()) return <span className="caption1-medium">{text}</span>;
  const regex = new RegExp(`(${highlight})`, 'gi');
  return (
    <span className="caption1-medium">
      {text.split(regex).map((part, i) =>
        regex.test(part) ? (
          <span key={i} className="underline">
            {part}
          </span>
        ) : (
          <span key={i}>{part}</span>
        ),
      )}
    </span>
  );
};

export default HighlightText;
