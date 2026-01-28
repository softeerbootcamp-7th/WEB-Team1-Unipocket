interface TextInputProps {
  placeholder?: string;
}

const TextInput = ({ placeholder="텍스트를 입력해 주세요." }: TextInputProps) => {
  return (
    <div className="flex flex-col gap-2">
        <p>주제</p>
        <input
            placeholder={placeholder}
        ></input>
        <p>메시지에 마침표를 찍어요.</p>
    </div>
  )
}

export default TextInput