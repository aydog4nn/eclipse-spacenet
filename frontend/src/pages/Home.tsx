import { lazy, Suspense, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Orbit, Radar, Sparkles } from 'lucide-react';
import GlassCard from '../components/common/GlassCard';
import { useData } from '../context/DataContext';
import { solarPlanets } from '../components/home/solarSystemData';
import './Home.css';

const SolarSystemScene = lazy(() => import('../components/home/SolarSystemScene'));

const Home = () => {
  const { organizations, projects } = useData();
  const suppliers = organizations.filter((organization) => organization.role === 'TEDARIKCI');
  const firms = organizations.filter((organization) => organization.role === 'FIRMA');
  const [selectedPlanetId, setSelectedPlanetId] = useState('earth');

  const selectedPlanet = useMemo(
    () => solarPlanets.find((planet) => planet.id === selectedPlanetId) ?? solarPlanets[2],
    [selectedPlanetId]
  );

  return (
    <div className="home-page">
      <section className="home-cosmos-section">
        <div className="home-cosmos-scene" aria-hidden="true">
          <Suspense fallback={<div className="solar-scene solar-scene-fallback" />}>
            <SolarSystemScene selectedPlanetId={selectedPlanetId} onSelectPlanet={setSelectedPlanetId} />
          </Suspense>
        </div>
        <div className="home-cosmos-overlay" />

        <div className="container home-cosmos-shell">
          <div className="home-cosmos-copy">
            <div className="hero-pill">
              <Sparkles size={16} />
              Gelecek gökyüzünde şekilleniyor
            </div>

            <h1 className="home-cosmos-title">
              Uzay ekosistemi,
              <span className="text-primary"> tek merkezde buluşuyor</span>
            </h1>

            <p className="home-cosmos-desc">
              Kurumları, projeleri ve yetkinlikleri uzay temalı tek bir deneyimde keşfedin.
            </p>

            <div className="home-cosmos-actions">
              <Link to="/register" className="btn btn-primary btn-lg">
                Ekosisteme Katıl
              </Link>
              <Link to="/discover" className="btn btn-outline btn-lg">
                Rehberi Keşfet
                <ArrowRight size={16} />
              </Link>
            </div>

            <div className="home-cosmos-microstats">
              <div className="hero-microstat">
                <Orbit size={18} />
                {suppliers.length} tedarikçi profili
              </div>
              <div className="hero-microstat">
                <Radar size={18} />
                {projects.length} aktif ihtiyaç kaydı
              </div>
            </div>
          </div>

          <GlassCard className="planet-panel">
            <span className="planet-panel-kicker">Odaktaki Gezegen</span>
            <h2>{selectedPlanet.name}</h2>
            <p className="planet-panel-subtitle">{selectedPlanet.subtitle}</p>
            <p className="planet-panel-desc">{selectedPlanet.description}</p>

            <div className="planet-panel-meta">
              <div className="planet-panel-stat">
                <span>Yörünge</span>
                <strong>{selectedPlanet.distanceAu.toFixed(2)} AU</strong>
              </div>
              <div className="planet-panel-stat">
                <span>Katman</span>
                <strong>{selectedPlanet.stats}</strong>
              </div>
            </div>

            <div className="planet-selector">
              {solarPlanets.map((planet) => (
                <button
                  key={planet.id}
                  type="button"
                  className={`planet-chip ${planet.id === selectedPlanetId ? 'active' : ''}`}
                  onClick={() => setSelectedPlanetId(planet.id)}
                >
                  <span className="planet-chip-dot" style={{ backgroundColor: planet.accent }} />
                  {planet.name}
                </button>
              ))}
            </div>
          </GlassCard>
        </div>
      </section>

      <section className="stats-section">
        <div className="container">
          <div className="stats-grid">
            <div className="stat-item">
              <div className="stat-value">{organizations.length}</div>
              <div className="stat-label">KAYITLI PROFİL</div>
            </div>
            <div className="stat-item">
              <div className="stat-value">{projects.length}</div>
              <div className="stat-label">AKTİF İHTİYAÇ</div>
            </div>
            <div className="stat-item">
              <div className="stat-value">{suppliers.length}</div>
              <div className="stat-label">TEDARİKÇİ</div>
            </div>
            <div className="stat-item">
              <div className="stat-value">{firms.length}</div>
              <div className="stat-label">FİRMA</div>
            </div>
          </div>
        </div>
      </section>

      <section className="modules-section">
        <div className="container">
          <h2 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>SpaceNet Modülleri</h2>
          <p style={{ color: 'var(--color-text-muted)', marginBottom: '3rem' }}>
            İlan yönetimi, kurum görünürlüğü ve eşleşme süreçlerini tek akışta yönetin.
          </p>

          <div className="modules-grid">
            <GlassCard hoverable className="module-card">
              <div className="module-icon">🔍</div>
              <h3 className="module-title">İhtiyaç Yayınla</h3>
              <p className="module-desc">Aradığınız donanım, yazılım veya test altyapısı için net bir talep oluşturun.</p>
              <Link to="/projects/new" className="module-link">İLAN VER →</Link>
            </GlassCard>

            <GlassCard hoverable className="module-card">
              <div className="module-icon">🏢</div>
              <h3 className="module-title">Tedarikçi Bul</h3>
              <p className="module-desc">Yetkinlik, uzmanlık ve konuma göre uygun tedarikçileri filtreleyin.</p>
              <Link to="/discover" className="module-link">KEŞFET →</Link>
            </GlassCard>

            <GlassCard hoverable className="module-card">
              <div className="module-icon">⚡</div>
              <h3 className="module-title">Akıllı Eşleşme</h3>
              <p className="module-desc">Proje ihtiyaçlarınıza en yakın teknik profilleri görün ve değerlendirmeyi hızlandırın.</p>
              <Link to="/projects" className="module-link">ÖNERİLERİ GÖR →</Link>
            </GlassCard>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;
