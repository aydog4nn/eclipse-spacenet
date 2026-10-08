import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import GlassCard from '../components/common/GlassCard';
import PasswordField from '../components/common/PasswordField';
import './Login.css';

const REMEMBERED_EMAIL_KEY = 'spacenet_remembered_email';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    const rememberedEmail = localStorage.getItem(REMEMBERED_EMAIL_KEY);
    if (rememberedEmail) {
      setEmail(rememberedEmail);
      setRememberMe(true);
    }
  }, []);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (rememberMe) {
        localStorage.setItem(REMEMBERED_EMAIL_KEY, email);
      } else {
        localStorage.removeItem(REMEMBERED_EMAIL_KEY);
      }
      await login(email, password);
      showToast('Başarıyla giriş yapıldı.', 'success');
      navigate('/panel');
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || 'Giriş yapılamadı. Lütfen bilgilerinizi kontrol edin.';
      showToast(errorMsg, 'error');
    }
  };

  return (
    <div className="login-page">
      <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
        <GlassCard className="login-card">
          <h2 className="login-title">Ekosisteme Giriş Yap</h2>
          <p className="login-desc">Hesabınıza giriş yapın.</p>
          
          <form className="login-form" onSubmit={handleLogin}>
            <div className="form-group">
              <label>E-POSTA ADRESİ</label>
              <input 
                type="email" 
                required 
                placeholder="Örn: firma@sirket.com" 
                value={email}
                onChange={e => setEmail(e.target.value)}
              />
            </div>
            
            <div className="form-group">
              <label>ŞİFRE</label>
              <PasswordField
                required 
                placeholder="••••••••" 
                value={password}
                onChange={e => setPassword(e.target.value)}
              />
            </div>
            
            <div className="login-support-row">
              <label className="remember-toggle">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                />
                <span className="remember-check" aria-hidden="true" />
                <span>Beni hatırla</span>
              </label>
              <span className="support-link-copy">
                Hesabınız yok mu? <Link to="/register">Hemen Kaydolun</Link>
              </span>
            </div>
            
            <button type="submit" className="btn btn-primary" style={{width: '100%', marginTop: '1rem', padding: '1rem'}}>
              GİRİŞ YAP
            </button>
          </form>
        </GlassCard>
      </div>
    </div>
  );
};

export default Login;
