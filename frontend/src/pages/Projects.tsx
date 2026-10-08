import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useData } from '../context/DataContext';
import GlassCard from '../components/common/GlassCard';
import { Search } from 'lucide-react';
import ApplyModal from '../components/modals/ApplyModal';
import type { Project } from '../types';
import './Projects.css';

const Projects = () => {
  const { projects, getOrganizationById, loading } = useData();
  const [filterType, setFilterType] = useState<string>('Tum');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);

  const normalize = (value: string) => value.toLocaleLowerCase('tr-TR');

  const handleApplyClick = (project: Project) => {
    setSelectedProject(project);
  };

  if (loading) {
    return <div className="container" style={{paddingTop: '15rem', textAlign: 'center'}}><h2>Yükleniyor...</h2></div>;
  }

  const filtered = projects.filter(p => {
    const normalizedSearch = normalize(searchTerm.trim());
    const searchableContent = [
      p.title,
      p.description,
      p.organizationName || getOrganizationById(p.organizationId)?.name || '',
      p.locationPreference || '',
      p.budgetRange || '',
      ...(p.requiredCapabilities?.map((cap) => cap.name) || []),
      ...(p.certificationRequirements || [])
    ]
      .filter(Boolean)
      .map(normalize)
      .join(' ');

    const matchType = filterType === 'Tum' || p.projectType === filterType;
    const matchSearch = normalizedSearch.length === 0 || searchableContent.includes(normalizedSearch);
    const isVisible = p.status === 'AKTIF';
    return matchType && matchSearch && isVisible;
  });

  return (
    <div className="projects-page container">
       <div className="projects-header">
        <h2 className="section-subtitle text-gradient">SPACENET PROJE AKIŞI</h2>
        <h1 className="section-title">
          Açık çağrıları inceleyin,<br/>
          <span className="text-primary">uygun projelere başvurun</span>
        </h1>
        <p className="section-desc">
          Kurumların açtığı aktif ihtiyaçları tek listede görün, yetkinliğinize uyan iş birliklerine doğrudan başvurun.
        </p>
      </div>

      <div className="projects-controls glass">
        <div className="search-box">
          <Search size={20} />
          <input 
            type="text" 
            placeholder="İlan başlığı veya açıklama ile ara..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-tabs">
          <button 
            className={`tab-btn ${filterType === 'Tum' ? 'active' : ''}`}
            onClick={() => setFilterType('Tum')}
          >TÜMÜ</button>
          <button 
            className={`tab-btn ${filterType === 'TEDARIKCI_ARANIYOR' ? 'active' : ''}`}
            onClick={() => setFilterType('TEDARIKCI_ARANIYOR')}
          >TEDARİKÇİ</button>
          <button 
            className={`tab-btn ${filterType === 'ARGE_PARTNERI' ? 'active' : ''}`}
            onClick={() => setFilterType('ARGE_PARTNERI')}
          >AR-GE PARTNERİ</button>
          <button 
            className={`tab-btn ${filterType === 'URETIM_ORTAGI' ? 'active' : ''}`}
            onClick={() => setFilterType('URETIM_ORTAGI')}
          >ÜRETİM ORTAĞI</button>
          <button 
            className={`tab-btn ${filterType === 'TEST_ALTYAPISI' ? 'active' : ''}`}
            onClick={() => setFilterType('TEST_ALTYAPISI')}
          >TEST ALTYAPISI</button>
        </div>
      </div>

      <div className="project-list">
        {filtered.length === 0 && (
          <GlassCard className="project-card">
            <p style={{ color: 'var(--color-text-muted)', textAlign: 'center', width: '100%' }}>
              {searchTerm ? `"${searchTerm}" için eşleşen ilan bulunamadı.` : 'Henüz yayında ilan bulunmuyor.'}
            </p>
          </GlassCard>
        )}

        {filtered.map(proj => {
          const firm = getOrganizationById(proj.organizationId);
          return (
            <GlassCard key={proj.id} hoverable className="project-card">
              <div className="project-card-left">
                <div className="project-badge">{proj.projectType.replace(/_/g, ' ')}</div>
                <h3 className="project-title">{proj.title}</h3>
                <div className="project-meta">
                  <span>Açan Kurum: <strong>{proj.organizationName || firm?.name || 'SpaceNet Üyesi'}</strong></span>
                  <span className="dot">•</span>
                  <span>Son Başvuru: <strong>{proj.applicationDeadline}</strong></span>
                </div>
                <div className="project-caps">
                  {proj.requiredCapabilities && proj.requiredCapabilities.length > 0 ? (
                    proj.requiredCapabilities.map(cap => (
                      <span key={cap.id} className="cap-tag">{cap.name}</span>
                    ))
                  ) : (
                    <span className="cap-tag-empty">Yetkinlik Beyanı Yok</span>
                  )}
                </div>
              </div>
              <div className="project-card-right">
                <div className="project-budget">
                  <span className="budget-label">BÜTÇE ARALIĞI</span>
                  <span className="budget-val text-cyan">{proj.budgetRange}</span>
                </div>
                <div className="project-card-actions">
                  <Link to={`/projects/${proj.id}`} className="btn btn-outline" style={{flex: 1, textAlign: 'center'}}>DETAY</Link>
                  <button 
                    className="btn btn-primary" 
                    style={{flex: 1.5}}
                    onClick={() => handleApplyClick(proj)}
                  >
                    BAŞVUR
                  </button>
                </div>
              </div>
            </GlassCard>
          )
        })}
      </div>

      {selectedProject && (
        <ApplyModal 
          projectId={selectedProject.id}
          projectTitle={selectedProject.title}
          onClose={() => setSelectedProject(null)}
          onSuccess={() => {
            // Success logic if needed
          }}
        />
      )}
    </div>
  );
};

export default Projects;
