import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import GlassCard from '../components/common/GlassCard';
import { MapPin, Globe, Award, Database, Briefcase, ChevronLeft } from 'lucide-react';
import type { Organization } from '../types';
import DiscoveryService from '../services/discoveryService';
import './OrganizationDetail.css';

const OrganizationDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [org, setOrg] = useState<Organization | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadOrganization = async () => {
      if (!id) {
        setLoading(false);
        return;
      }

      try {
        const response = await DiscoveryService.getSupplierById(id);
        if (response.success) {
          setOrg(response.data);
        }
      } catch (error) {
        console.error('Kurum detayi alinamadi:', error);
      } finally {
        setLoading(false);
      }
    };

    loadOrganization();
  }, [id]);

  if (loading) return <div className="container loader">Kurum Verileri Yükleniyor...</div>;
  if (!org) return <div className="container error">Kurum bulunamadı.</div>;

  return (
    <div className="org-detail-page container">
      <button className="back-link" onClick={() => navigate(-1)}>
        <ChevronLeft size={20} /> Geri Dön
      </button>

      <div className="org-profile-header">
        <div className="org-hero glass">
           <div className="org-logo-large">{org.name.substring(0, 1)}</div>
           <div className="org-hero-content">
             <span className={`role-badge ${org.role.toLowerCase()}`}>{org.role}</span>
             <h1 className="org-name-title">{org.name}</h1>
             <div className="org-quick-meta">
                <span><MapPin size={16} /> {org.location || 'Türkiye'}</span>
                {org.website && (
                  <a href={org.website} target="_blank" rel="noreferrer"><Globe size={16} /> Web Sitesi</a>
                )}
             </div>
           </div>
        </div>
      </div>

      <div className="org-detail-grid">
        <div className="org-main-info">
          <GlassCard className="info-card">
            <h3>KURUMSAL ÖZETİ</h3>
            <p className="org-description">{org.description || 'Bu kurum henüz detaylı bir açıklama eklememiştir.'}</p>
            
            <div className="info-section">
              <h3><Database size={18} /> TEKNİK YETKİNLİKLER</h3>
              <div className="cap-list">
                {org.capabilities?.length > 0 ? org.capabilities.map((cap) => (
                  <span key={cap} className="cap-tag-glow">{cap}</span>
                )) : <p className="empty-text">Henüz yetkinlik beyanı girilmemiştir.</p>}
              </div>
            </div>

            <div className="info-section">
              <h3><Award size={18} /> SERTİFİKALAR VE KALİTE BELGELERİ</h3>
              <div className="cert-grid">
                {org.certifications?.length > 0 ? org.certifications.map((cert) => (
                  <div key={cert} className="cert-card-mini glass">
                    <Award size={24} className="cert-icon" />
                    <span>{cert}</span>
                  </div>
                )) : <p className="empty-text">Henüz sertifika bilgisi girilmemiştir.</p>}
              </div>
            </div>
          </GlassCard>
        </div>

        <div className="org-sidebar">
          <GlassCard className="stats-card">
             <h3>KURUM KİMLİĞİ</h3>
             <div className="sidebar-stat">
               <label>KATEGORİ</label>
               <span>{org.supplierCategory || 'Tedarikçi'}</span>
             </div>
             <div className="sidebar-stat">
               <label>EKOSİSTEM DURUMU</label>
               <span className="text-cyan">{org.verified ? 'DOĞRULANMIŞ PROFİL' : 'PROFİL TAMAMLANIYOR'}</span>
             </div>
             <div className="sidebar-stat">
               <label>TOPLAM REFERANS</label>
               <span>{org.references?.length || 0} Proje</span>
             </div>
          </GlassCard>

          <GlassCard className="references-card">
            <h3>REFERANS PROJELER</h3>
            <div className="ref-list">
               {org.references?.length > 0 ? org.references.map((ref) => (
                 <div key={ref} className="ref-item">
                    <Briefcase size={14} /> {ref}
                 </div>
               )) : <p className="empty-text">Referans listesi boş.</p>}
            </div>
          </GlassCard>
        </div>
      </div>
    </div>
  );
};

export default OrganizationDetail;
