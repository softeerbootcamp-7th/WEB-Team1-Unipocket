interface HighlightTextProps {
  text: string;
  highlight: string;
}

const HighlightText = ({ text, highlight }: HighlightTextProps) => {
  if (!highlight.trim()) return <span className="caption1-medium">{text}</span>;
  const escapedHighlight = highlight.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const regex = new RegExp(`(${escapedHighlight})`, 'gi');
  return (
    <span className="caption1-medium">
      {text.split(regex).map((part, i) =>
        i % 2 === 1 ? (
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
