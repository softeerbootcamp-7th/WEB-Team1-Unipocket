import Icons from '@/assets';

interface ToastProps {
    type: 'success' | 'error';
    message: string;
}


const Toast = ({ type, message }: ToastProps) => {
  return (
    <div className="w-105">
        {message}
    </div>
  )
}

export default Toast