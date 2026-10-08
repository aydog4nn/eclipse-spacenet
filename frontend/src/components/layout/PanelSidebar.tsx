import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Home, Briefcase, FileText, Settings, LogOut, Shield, Layout } from 'lucide-react';
import './PanelSidebar.css';

const PanelSidebar = () => {
  const { user, logout } = useAuth();
  
  return (
    <aside className="panel-sidebar glass">
      <div className="sidebar-header">
        <h3 className="sidebar-title">SPACENET PANEL</h3>
        <p className="sidebar-subtitle">EKOSİSTEM MERKEZİ</p>
      </div>
      
      <nav className="sidebar-nav">
        <NavLink to="/panel" end className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
          <Home size={18} /> PANEL
        </NavLink>
        
        {user?.role === 'ADMIN' ? (
          <>
            <div className="sidebar-divider">ADMİN İŞLEMLERİ</div>
            <NavLink to="/panel/admin/approvals" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <Shield size={18} /> KURUM ONAYLARI
            </NavLink>
            <NavLink to="/panel/admin/capabilities" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <Layout size={18} /> YETKİNLİK YÖNETİMİ
            </NavLink>
          </>
        ) : (
          <>
            <NavLink to="/panel/projects" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <Briefcase size={18} /> İLANLAR
            </NavLink>
            <NavLink to="/panel/applications" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
              <FileText size={18} /> BAŞVURULAR
            </NavLink>
          </>
        )}
        
        <NavLink to="/panel/settings" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
          <Settings size={18} /> AYARLAR
        </NavLink>
      </nav>
      
      <div className="sidebar-footer">
        <div className="user-info">
          <div className="user-role">{user?.role}</div>
          <div className="user-email">{user?.email}</div>
        </div>
        <button onClick={logout} className="btn-logout" aria-label="Çıkış Yap">
          <LogOut size={20} />
        </button>
      </div>
    </aside>
  );
};

export default PanelSidebar;
