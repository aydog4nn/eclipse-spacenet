import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import './PasswordField.css';

type PasswordFieldProps = React.InputHTMLAttributes<HTMLInputElement>;

const PasswordField = ({ className = '', ...props }: PasswordFieldProps) => {
  const [visible, setVisible] = useState(false);

  return (
    <div className={`password-field ${className}`.trim()}>
      <input {...props} type={visible ? 'text' : 'password'} />
      <button
        type="button"
        className="password-toggle"
        onClick={() => setVisible((current) => !current)}
        aria-label={visible ? 'Şifreyi gizle' : 'Şifreyi göster'}
        title={visible ? 'Şifreyi gizle' : 'Şifreyi göster'}
      >
        {visible ? <Eye size={18} /> : <EyeOff size={18} />}
      </button>
    </div>
  );
};

export default PasswordField;
