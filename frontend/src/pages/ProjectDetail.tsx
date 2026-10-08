import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useData } from '../context/DataContext';
import { useAuth } from '../context/AuthContext';
import ProjectService from '../services/projectService';
import GlassCard from '../components/common/GlassCard';
import ApplyModal from '../components/modals/ApplyModal';
import type { Project } from '../types';
import { Calendar, MapPin, Briefcase, Shield, Clock, CircleDot, ChevronLeft } from 'lucide-react';
import './ProjectDetail.css';

const ProjectDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { getOrganizationById } = useData();
  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);
  const [showApplyModal, setShowApplyModal] = useState(false);

  useEffect(() => {
    const fetchProject = async () => {
      if (!id) return;
      try {
        const res = await ProjectService.getProjectById(id);
        if (res.success) {
          setProject(res.data);
        }
      } catch {
        console.error('Proje detayları çekilemedi');
      } finally {
        setLoading(false);
      }
    };
    fetchProject();
  }, [id]);

  if (loading) return <div className="container loader">İlan Yükleniyor...</div>;
  if (!project) return <div className="container error">İlan bulunamadı.</div>;

  const firm = getOrganizationById(project.organizationId);
  const isSupplier = user?.role === 'TEDARIKCI';

  return (
    <div className="project-detail-page container">
      <button className="back-link" onClick={() => navigate(-1)}>
        <ChevronLeft size={20} /> Geri Dön
      </button>

      <div className="project-detail-grid">
        <div className="detail-main">
          <GlassCard className="main-content-card">
            <div className="project-badge">{project.projectType.replace(/_/g, ' ')}</div>
            <h1 className="project-title">{project.title}</h1>
            
            <div className="project-org-strip">
              <div className="org-mini-logo">{project.organizationName?.substring(0, 1) || 'T'}</div>
              <div>
                <div className="org-label">İLAN SAHİBİ</div>
                <div className="org-name-val">{project.organizationName || firm?.name || 'SpaceNet Üyesi Kurum'}</div>
              </div>
            </div>

            <div className="description-section">
              <h3>PROJE VE İHTİYAÇ AÇIKLAMASI</h3>
              <p>{project.description}</p>
            </div>

            <div className="requirements-section">
              <h3>ARANAN YETKİNLİKLER</h3>
              <div className="cap-list">
                {project.requiredCapabilities?.map((cap) => (
                  <span key={cap.id} className="cap-tag-large">{cap.name}</span>
                ))}
              </div>
            </div>

            {project.certificationRequirements?.length > 0 && (
              <div className="certs-section">
                <h3>GEREKLİ SERTİFİKALAR</h3>
                <div className="cert-list">
                  {project.certificationRequirements.map((cert: string) => (
                    <div key={cert} className="cert-item">
                      <CircleDot size={14} /> {cert}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </GlassCard>
        </div>

        <div className="detail-sidebar">
          <GlassCard className="sidebar-card">
            <h3>PROJE KÜNYESİ</h3>
            
            <div className="info-item">
              <Shield size={18} />
              <div>
                <label>GİZLİLİK SEVİYESİ</label>
                <span>{project.confidentialityLevel}</span>
              </div>
            </div>

            <div className="info-item">
              <Clock size={18} />
              <div>
                <label>SON BAŞVURU</label>
                <span>{project.applicationDeadline}</span>
              </div>
            </div>

            <div className="info-item">
              <Briefcase size={18} />
              <div>
                <label>SÜRE</label>
                <span>{project.durationText || 'Belirtilmedi'}</span>
              </div>
            </div>

            <div className="info-item">
              <Calendar size={18} />
              <div>
                <label>BÜTÇE ARALIĞI</label>
                <span className="text-cyan">{project.budgetRange || 'Bilinmiyor'}</span>
              </div>
            </div>

            <div className="info-item">
              <MapPin size={18} />
              <div>
                <label>LOKASYON TERCİHİ</label>
                <span>{project.locationPreference || 'Tüm Türkiye'}</span>
              </div>
            </div>

            {isSupplier && (
               <button className="btn btn-primary" style={{width: '100%', marginTop: '2rem'}} onClick={() => setShowApplyModal(true)}>
                 ŞİMDİ BAŞVUR
               </button>
            )}
          </GlassCard>

          <div className="sidebar-warning glass">
             <Shield size={20} style={{color: 'var(--color-primary)'}} />
             <p>İlan detayları sadece kayıtlı ekosistem üyeleri tarafından tam olarak görüntülenebilir.</p>
          </div>
        </div>
      </div>

      {showApplyModal && (
        <ApplyModal 
          projectId={project.id}
          projectTitle={project.title}
          onClose={() => setShowApplyModal(false)}
          onSuccess={() => {}}
        />
      )}
    </div>
  );
};

export default ProjectDetail;
