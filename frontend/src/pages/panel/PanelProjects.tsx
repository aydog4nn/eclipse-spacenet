import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useData } from '../../context/DataContext';
import { calculateMatch } from '../../utils/matching';
import GlassCard from '../../components/common/GlassCard';
import MatchBadge from '../../components/common/MatchBadge';
import ApplyModal from '../../components/modals/ApplyModal';
import { Search, Plus } from 'lucide-react';
import type { Project } from '../../types';

type ProjectWithMatch = Project & {
  matchResult?: ReturnType<typeof calculateMatch>;
};

const PanelProjects = () => {
  const { user } = useAuth();
  const { projects, myProjects, organizations, loading, refreshData } = useData();
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState<'ALL' | 'RECOMMENDED'>('ALL');
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);

  if (!user) return null;

  const handleApplyClick = (project: Project) => {
    setSelectedProject(project);
  };

  const isFirm = user.role === 'FIRMA';
  const currentOrg = organizations.find(o => String(o.id) === String(user.organizationId));
  const normalize = (value: string) => value.toLocaleLowerCase('tr-TR');

  const firmOwnedProjects = projects.filter((project) => String(project.organizationId) === String(user.organizationId));
  const baseProjects = isFirm
    ? Array.from(new Map([...myProjects, ...firmOwnedProjects].map((project) => [project.id, project])).values())
    : projects.filter((project) => project.status === 'AKTIF');

  const trSearchTerm = searchTerm.toLocaleLowerCase('tr-TR').trim();
  
  let displayProjects: ProjectWithMatch[] = baseProjects.filter((project) => {
    const searchFields = [
      project.title,
      project.description,
      project.organizationName || '',
      project.locationPreference || '',
      project.budgetRange || '',
      ...(project.requiredCapabilities?.map((cap) => cap.name) || []),
      ...(project.certificationRequirements || [])
    ]
      .filter(Boolean)
      .map(normalize)
      .join(' ');

    return trSearchTerm.length === 0 || searchFields.includes(trSearchTerm);
  });

  if (!isFirm && activeTab === 'RECOMMENDED' && currentOrg) {
    displayProjects = displayProjects
      .map((project) => ({ ...project, matchResult: calculateMatch(project, currentOrg) }))
      .filter((project) => (project.matchResult?.score || 0) > 5)
      .sort((a, b) => (b.matchResult?.score || 0) - (a.matchResult?.score || 0));
  }

  if (loading && baseProjects.length === 0) {
    return (
      <div className="panel-projects">
        <div className="loading-state" style={{ padding: '5rem', textAlign: 'center' }}>
          <div className="spinner"></div>
          <p style={{ marginTop: '1rem', opacity: 0.6 }}>Projeler yükleniyor...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="panel-projects">
      <div className="panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 className="panel-title">{isFirm ? 'Açtığınız İlanlar' : 'Sektörel İlanlar'}</h1>
          <p className="panel-subtitle">Ekosistemdeki fırsatları ve projeleri yönetin.</p>
        </div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button onClick={() => refreshData()} className="btn btn-outline" disabled={loading}>
            {loading ? 'Yükleniyor...' : 'YENİLE'}
          </button>
          {isFirm && (
            <Link to="/panel/projects/new" className="btn btn-primary">
              <Plus size={18} /> YENİ İLAN VER
            </Link>
          )}
        </div>
      </div>

      <div className="panel-controls glass" style={{ padding: '1.5rem', marginBottom: '2rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="search-box">
          <Search size={20} />
          <input 
            type="text" 
            placeholder="İlan ara..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        {!isFirm && (
          <div className="filter-tabs" style={{ marginTop: '0.5rem' }}>
            <button 
              className={`tab-btn ${activeTab === 'ALL' ? 'active' : ''}`}
              onClick={() => setActiveTab('ALL')}
            >TÜM İLANLAR</button>
            <button 
              className={`tab-btn ${activeTab === 'RECOMMENDED' ? 'active' : ''}`}
              onClick={() => setActiveTab('RECOMMENDED')}
            >SİZE UYGUN OLANLAR</button>
          </div>
        )}
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {displayProjects.length === 0 ? (
          <p style={{ textAlign: 'center', opacity: 0.5, padding: '3rem' }}>
            {searchTerm ? `"${searchTerm}" kriterine uygun ilan bulunamadı.` : 'Henüz bir ilan bulunmuyor.'}
          </p>
        ) : (
          displayProjects.map(proj => {
            let score = 0;
            let matchedCount = 0;
            const totalRequired = proj.requiredCapabilities?.length || 0;

            if (!isFirm && currentOrg) {
              const res = proj.matchResult || calculateMatch(proj, currentOrg);
              score = res.score;
              matchedCount = res.matchedCapabilities.length;
            }

            return (
              <GlassCard key={proj.id} className="project-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.5rem' }}>
                      <div className="project-badge">{proj.projectType.toUpperCase()}</div>
                      <span style={{ fontSize: '0.75rem', opacity: 0.6 }}>• {proj.organizationName || 'SpaceNet Üyesi Kurum'}</span>
                    </div>
                    <h3 className="project-title">{proj.title}</h3>
                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginTop: '0.5rem' }}>
                      {proj.requiredCapabilities && proj.requiredCapabilities.length > 0 ? (
                        proj.requiredCapabilities.map(cap => (
                          <span key={cap.id} className="tag">{cap.name}</span>
                        ))
                      ) : (
                        <span className="tag" style={{ opacity: 0.5 }}>Yetkinlik beyanı yok</span>
                      )}
                    </div>
                  </div>
                  {!isFirm ? (
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
                       <MatchBadge 
                          score={score} 
                          matchedCount={matchedCount} 
                          totalRequired={totalRequired} 
                       />
                       <div style={{ display: 'flex', gap: '0.5rem', width: '100%' }}>
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
                  ) : (
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', marginBottom: '0.5rem' }}>GELEN BAŞVURULAR</div>
                      <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--color-primary)' }}>
                        {proj.status === 'AKTIF' ? 'Bekleniyor' : 'Kapalı'}
                      </div>
                      <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                        <Link to={`/projects/${proj.id}`} className="btn btn-outline" style={{flex: 1, textAlign: 'center'}}>DETAY</Link>
                        <Link to="/panel/applications" className="btn btn-primary" style={{flex: 1.5, textAlign: 'center'}}>YÖNET</Link>
                      </div>
                    </div>
                  )}
                </div>
              </GlassCard>
            );
          })
        )}
      </div>

      {selectedProject && (
        <ApplyModal 
          projectId={selectedProject.id}
          projectTitle={selectedProject.title}
          onClose={() => setSelectedProject(null)}
          onSuccess={() => {}}
        />
      )}
    </div>
  );
};

export default PanelProjects;
