import { useEffect, useState } from 'react';
import { NavLink, Link, useLocation, useNavigate } from 'react-router-dom';
import { User, LogOut, Moon, Sun, Search, Menu, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import './Header.css';

const Header = () => {
  const { isAuthenticated, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth > 960) {
        setMobileMenuOpen(false);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    if (!mobileMenuOpen) {
      document.body.style.overflow = '';
      return;
    }

    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = '';
    };
  }, [mobileMenuOpen]);

  const handleLogout = async () => {
    await logout();
    setMobileMenuOpen(false);
    navigate('/');
  };

  return (
    <header className="header glass">
      <div className={`header-container ${mobileMenuOpen ? 'menu-open' : ''}`}>
        <div className="header-logo">
          <Link to="/">
            <span className="logo-tua text-gradient" style={{fontWeight: 800}}>SpaceNet</span>
          </Link>
        </div>

        <button
          type="button"
          className="icon-btn mobile-menu-toggle"
          aria-label={mobileMenuOpen ? 'Menüyü kapat' : 'Menüyü aç'}
          aria-expanded={mobileMenuOpen}
          onClick={() => setMobileMenuOpen((open) => !open)}
        >
          {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
        </button>

        {mobileMenuOpen ? <button type="button" className="header-mobile-backdrop" aria-label="Menüyü kapat" onClick={() => setMobileMenuOpen(false)} /> : null}

        <div className={`header-mobile-panel ${mobileMenuOpen ? 'open' : ''}`}>
          <nav className="header-nav">
            <NavLink to="/discover" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>REHBER</NavLink>
            <NavLink to="/projects" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>PROJELER</NavLink>
            <NavLink to="/kurumlar" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>KURUMLAR</NavLink>
            <NavLink to="/hakkimizda" className={({isActive}) => isActive ? 'nav-link active' : 'nav-link'}>HAKKIMIZDA</NavLink>
          </nav>

          <div className="header-actions">
          <div className="search-box">
            <Search size={16} />
            <input type="text" placeholder="Proje, kurum veya yetkinlik ara" />
          </div>
          <div className="header-utility-row">
            <button
              className={`icon-btn theme-toggle ${theme === 'dark' ? 'active' : ''}`}
              aria-label={theme === 'light' ? 'Koyu moda geç' : 'Açık moda geç'}
              title={theme === 'light' ? 'Koyu moda geç' : 'Açık moda geç'}
              onClick={toggleTheme}
            >
              {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            </button>
          </div>
          
          {isAuthenticated ? (
            <div className="header-user-actions">
              <Link to="/panel" className="icon-btn profile-btn" aria-label="Profile">
                <User size={20} />
              </Link>
              <button className="icon-btn logout-btn" onClick={handleLogout} aria-label="Logout">
                <LogOut size={20} />
              </button>
            </div>
          ) : (
            <div className="header-auth-actions">
              <Link to="/login" className="btn btn-secondary header-login-btn">Giriş Yap</Link>
              <Link to="/register" className="btn btn-primary header-register-btn">Kayıt Ol</Link>
            </div>
          )}
        </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
