import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import GlassCard from '../../components/common/GlassCard';
import ApplicationService from '../../services/applicationService';
import { useToast } from '../../context/ToastContext';
import type { Application, ApplicationStatus } from '../../types';
import { Clock, CheckCircle2, XCircle, Eye, CornerUpLeft } from 'lucide-react';
import './PanelApplications.css';

const PanelApplications = () => {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [loading, setLoading] = useState(true);
  const [applications, setApplications] = useState<Application[]>([]);
  const [selectedApp, setSelectedApp] = useState<Application | null>(null);

  const isFirm = user?.role === 'FIRMA';

  const fetchApplications = useCallback(async () => {
    try {
      setLoading(true);
      const res = await ApplicationService.getMyApplications();
      if (res.success) {
        setApplications(res.data);
      }
    } catch {
      console.error('Başvurular yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

  const handleStatusUpdate = async (id: string, status: ApplicationStatus) => {
    try {
      await ApplicationService.updateApplicationStatus(id, status);
      showToast('Başvuru durumu güncellendi.', 'success');
      fetchApplications();
      setSelectedApp(null);
    } catch {
      showToast('Güncelleme sırasında hata oluştu.', 'error');
    }
  };

  const handleWithdraw = async (id: string) => {
    if (!window.confirm('Bu başvuruyu geri çekmek istediğinize emin misiniz?')) return;
    try {
      await ApplicationService.withdrawApplication(id);
      showToast('Başvuru geri çekildi.', 'success');
      fetchApplications();
    } catch {
      showToast('İşlem başarısız.', 'error');
    }
  };

  const getStatusBadge = (status: ApplicationStatus) => {
    switch (status) {
      case 'GONDERILDI': return <span className="status-badge sent">GÖNDERİLDİ</span>;
      case 'INCELENIYOR': return <span className="status-badge processing">İNCELENİYOR</span>;
      case 'KABUL_EDILDI': return <span className="status-badge approved">KABUL EDİLDİ</span>;
      case 'REDDEDILDI': return <span className="status-badge rejected">REDDEDİLDİ</span>;
      case 'GERI_CEKILDI': return <span className="status-badge withdrawn">GERİ ÇEKİLDİ</span>;
      default: return <span className="status-badge">{status}</span>;
    }
  };

  if (loading) return <div className="panel-loading">Yükleniyor...</div>;

  return (
    <div className="panel-applications">
      <div className="panel-header">
        <h1 className="panel-title">{isFirm ? 'Gelen Başvurular' : 'Başvurularım'}</h1>
        <p className="panel-subtitle">
          {isFirm 
            ? 'Projelerinize yapılan başvuruları yönetin ve adayları değerlendirin.' 
            : 'İşbirliği ilanlarına yaptığınız müracatların durumunu takip edin.'}
        </p>
      </div>

      <div className="applications-grid">
        <div className="applications-list">
          {applications.length === 0 ? (
            <GlassCard className="empty-state">
              <p>Henüz herhangi bir başvuru bulunmuyor.</p>
            </GlassCard>
          ) : (
            applications.map(app => (
              <GlassCard 
                key={app.id} 
                className={`app-item-card ${selectedApp?.id === app.id ? 'active' : ''}`}
                onClick={() => setSelectedApp(app)}
              >
                <div className="app-main-info">
                  <div className="app-project-name">{app.projectTitle}</div>
                  <div className="app-org-name">
                    {isFirm ? (
                      <Link to={`/kurumlar/${app.supplierOrganizationId}`} style={{ color: 'inherit', textDecoration: 'none', borderBottom: '1px dashed var(--color-primary)' }}>
                        {app.supplierOrganizationName}
                      </Link>
                    ) : 'İlana Başvuru'}
                  </div>
                  <div className="app-date">
                    <Clock size={12} /> {new Date(app.createdAt).toLocaleDateString('tr-TR')}
                  </div>
                </div>
                <div className="app-status-area">
                  {getStatusBadge(app.status)}
                </div>
              </GlassCard>
            ))
          )}
        </div>

        <div className="application-detail-view">
          {selectedApp ? (
            <GlassCard className="detail-card">
              <div className="detail-header">
                <h3>{selectedApp.projectTitle}</h3>
                {getStatusBadge(selectedApp.status)}
              </div>

              <div className="detail-section">
                <label>BAŞVURU ÖZETİ</label>
                <p>{selectedApp.shortDescription}</p>
              </div>

              <div className="detail-section">
                <label>NEDEN UYGUN?</label>
                <p>{selectedApp.whyFit}</p>
              </div>

              {selectedApp.referenceProjectNote && (
                <div className="detail-section">
                  <label>REFERANSLAR</label>
                  <p>{selectedApp.referenceProjectNote}</p>
                </div>
              )}

              {selectedApp.documentUrl && (
                <div className="detail-section">
                  <label>EK DOKÜMAN</label>
                  <a href={selectedApp.documentUrl} target="_blank" rel="noreferrer" className="doc-link">
                    Dokümanı Görüntüle
                  </a>
                </div>
              )}

              <div className="detail-actions">
                {isFirm ? (
                  <>
                    {selectedApp.status === 'GONDERILDI' && (
                      <button className="btn btn-outline" onClick={() => handleStatusUpdate(selectedApp.id, 'INCELENIYOR')}>
                        <Eye size={18} /> İNCELEMEYE AL
                      </button>
                    )}
                    <button className="btn btn-success" onClick={() => handleStatusUpdate(selectedApp.id, 'KABUL_EDILDI')}>
                      <CheckCircle2 size={18} /> KABUL ET
                    </button>
                    <button className="btn btn-danger" onClick={() => handleStatusUpdate(selectedApp.id, 'REDDEDILDI')}>
                      <XCircle size={18} /> REDDET
                    </button>
                  </>
                ) : (
                  selectedApp.status !== 'GERI_CEKILDI' && (
                    <button className="btn btn-outline" onClick={() => handleWithdraw(selectedApp.id)}>
                      <CornerUpLeft size={18} /> BAŞVURUYU GERİ ÇEK
                    </button>
                  )
                )}
              </div>
            </GlassCard>
          ) : (
            <div className="no-selection">
              <p>Detayları görüntülemek için bir başvuru seçin.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PanelApplications;
