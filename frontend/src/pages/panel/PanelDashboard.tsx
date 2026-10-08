import { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useData } from '../../context/DataContext';
import { Shield, AlertCircle } from 'lucide-react';
import ApplicationService from '../../services/applicationService';
import type { Application } from '../../types';

const PanelDashboard = () => {
  const { user } = useAuth();
  const { projects, organizations } = useData();
  const [applications, setApplications] = useState<Application[]>([]);

  const firmProjects = projects.filter(p => p.organizationId === user?.organizationId);
  const supplierOrg = organizations.find(o => o.id === user?.organizationId);
  const supplierCount = organizations.filter(o => o.role === 'TEDARIKCI').length;
  const profileCompletion = useMemo(() => {
    if (!supplierOrg) return 0;

    const checks = [
      supplierOrg.description,
      supplierOrg.location,
      supplierOrg.website,
      supplierOrg.capabilities?.length ? 'capabilities' : '',
      supplierOrg.certifications?.length ? 'certifications' : '',
      supplierOrg.references?.length ? 'references' : '',
    ];

    return Math.round((checks.filter(Boolean).length / checks.length) * 100);
  }, [supplierOrg]);

  useEffect(() => {
    if (!user) return;
    const loadApplications = async () => {
      try {
        const response = await ApplicationService.getMyApplications();
        if (response.success) {
          setApplications(response.data);
        }
      } catch (error) {
        console.error('Dashboard applications could not be loaded:', error);
      }
    };

    loadApplications();
  }, [user]);

  if (!user) return null;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '2rem', color: 'var(--color-text-main)', margin: 0 }}>Hoş Geldiniz, {supplierOrg?.name || user.email}</h2>
        
        <div className={`status-badge-container ${user.approved ? 'approved' : 'pending'}`} style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.6rem',
          padding: '0.6rem 1.2rem',
          borderRadius: '12px',
          background: user.approved ? 'rgba(0, 255, 136, 0.1)' : 'rgba(255, 171, 0, 0.1)',
          border: `1px solid ${user.approved ? 'rgba(0, 255, 136, 0.3)' : 'rgba(255, 171, 0, 0.3)'}`,
          color: user.approved ? '#00ff88' : '#ffab00',
          fontWeight: 700,
          fontSize: '0.85rem'
        }}>
          {user.approved ? <Shield size={18} /> : <AlertCircle size={18} />}
          {user.approved ? 'HESAP ONAYLI' : 'ADMİN ONAYI BEKLİYOR'}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1.5rem', marginBottom: '3rem' }}>
        {user.role === 'FIRMA' ? (
          <>
            <div className="glass-card-inner" style={{ background: 'var(--color-bg-card)', borderRadius: 'var(--radius-md)', padding: '1.5rem', border: '1px solid var(--color-border)' }}>
              <h3 style={{ fontSize: '1rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>Aktif İlanlarınız</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-text-main)' }}>{firmProjects.length}</div>
            </div>
            <div className="glass-card-inner" style={{ background: 'var(--color-bg-card)', borderRadius: 'var(--radius-md)', padding: '1.5rem', border: '1px solid var(--color-border)' }}>
              <h3 style={{ fontSize: '1rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>Gelen Başvurular</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-text-main)' }}>{applications.length}</div>
            </div>
            <div className="glass-card-inner" style={{ background: 'var(--color-bg-card)', borderRadius: 'var(--radius-md)', padding: '1.5rem', border: '1px solid var(--color-border)' }}>
              <h3 style={{ fontSize: '1rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>Tedarikçiler</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-text-main)' }}>{supplierCount}</div>
            </div>
          </>
        ) : (
          <>
            <div className="glass-card-inner" style={{ background: 'var(--color-bg-card)', borderRadius: 'var(--radius-md)', padding: '1.5rem', border: '1px solid var(--color-border)' }}>
              <h3 style={{ fontSize: '1rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>Yeni İlan Önerileri</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-text-main)' }}>{projects.length}</div>
            </div>
            <div className="glass-card-inner" style={{ background: 'var(--color-bg-card)', borderRadius: 'var(--radius-md)', padding: '1.5rem', border: '1px solid var(--color-border)' }}>
              <h3 style={{ fontSize: '1rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>Yaptığınız Başvurular</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-text-main)' }}>{applications.length}</div>
            </div>
            <div className="glass-card-inner" style={{ background: 'var(--color-bg-card)', borderRadius: 'var(--radius-md)', padding: '1.5rem', border: '1px solid var(--color-border)' }}>
              <h3 style={{ fontSize: '1rem', color: 'var(--color-text-muted)', marginBottom: '1rem' }}>Profil Tamamlama</h3>
              <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--color-cyan)' }}>%{profileCompletion}</div>
            </div>
          </>
        )}
      </div>
      
      <div style={{ background: 'var(--color-bg-card)', padding: '2rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border)' }}>
        <h3 style={{ marginBottom: '1.5rem', color: 'var(--color-text-main)' }}>Son Aktiviteler</h3>
        <p style={{ color: 'var(--color-text-muted)' }}>
          {applications.length > 0
            ? `${applications.length} başvuru kaydı sistemden canlı olarak yüklendi.`
            : 'Henüz görüntülenecek canlı aktivite bulunmuyor.'}
        </p>
      </div>
    </div>
  );
};

export default PanelDashboard;
