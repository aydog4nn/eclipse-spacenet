import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import GlassCard from '../components/common/GlassCard';
import { ArrowUpRight, Building2, Globe, MapPin, Search, ShieldCheck, Sparkles, Users2 } from 'lucide-react';
import type { Organization } from '../types';
import DiscoveryService from '../services/discoveryService';
import './Organizations.css';

const normalize = (value: string) =>
  value
    .toLocaleLowerCase('tr-TR')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');

const Organizations = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadOrganizations = async () => {
      setLoading(true);
      try {
        const response = await DiscoveryService.getSuppliers();
        if (response.success) {
          setOrganizations(response.data.filter((organization) => organization.role === 'TEDARIKCI'));
        }
      } catch (error) {
        console.error('Kayıtlı kurumlar yüklenemedi:', error);
      } finally {
        setLoading(false);
      }
    };

    loadOrganizations();
  }, []);

  const filteredOrganizations = useMemo(() => {
    const normalizedSearch = normalize(searchTerm.trim());
    const tokens = normalizedSearch.split(/\s+/).filter(Boolean);

    return organizations
      .filter((organization) => {
        const searchIndex = [
          organization.name,
          organization.description,
          organization.location,
          organization.website,
          organization.activityArea || '',
          organization.contactEmail || '',
          ...(organization.capabilities || []),
          ...(organization.fieldsOfActivity || []),
          ...(organization.certifications || []),
          ...(organization.references || []),
        ]
          .filter(Boolean)
          .map(normalize)
          .join(' ');

        return tokens.length === 0 || tokens.every((token) => searchIndex.includes(token));
      })
      .sort((left, right) => {
        if (left.verified !== right.verified) return Number(right.verified) - Number(left.verified);
        if ((right.completionPercentage || 0) !== (left.completionPercentage || 0)) {
          return (right.completionPercentage || 0) - (left.completionPercentage || 0);
        }
        return left.name.localeCompare(right.name, 'tr');
      });
  }, [organizations, searchTerm]);

  const highlightedOrganization = filteredOrganizations[0];

  const getOrganizationTags = (organization: Organization) => {
    return Array.from(
      new Set([
        ...(organization.fieldsOfActivity || []),
        ...(organization.capabilities || []),
      ].filter(Boolean))
    ).slice(0, 4);
  };

  if (loading) {
    return <div className="container" style={{paddingTop: '15rem', textAlign: 'center'}}><h2>Yükleniyor...</h2></div>;
  }

  return (
    <div className="organizations-page container">
      <section className="org-header">
        <div className="org-header-copy">
          <h2 className="section-subtitle text-gradient">SPACENET TEDARİKÇİ DİZİNİ</h2>
          <h1 className="section-title">Uzman tedarikçi<br/><span className="text-primary">profillerini keşfedin</span></h1>
          <p className="section-desc">
            Backend’de canlı olarak dönen tedarikçi profillerini inceleyin. Bu sayfada SpaceNet üzerinde görünür olan uzman kurumlar listelenir.
          </p>
        </div>

        <div className="org-stats">
          <GlassCard className="stat-card highlight">
            <span className="stat-kicker"><Building2 size={16} /> Kayıtlı Ağ</span>
            <span className="stat-value">{organizations.length}</span>
            <span className="stat-label">Tedarikçi profili</span>
          </GlassCard>
          <GlassCard className="stat-card">
            <span className="stat-kicker"><ShieldCheck size={16} /> Doğrulama</span>
            <span className="stat-value">{organizations.filter((organization) => organization.verified).length}</span>
            <span className="stat-label">Doğrulanmış hesap</span>
          </GlassCard>
          <GlassCard className="stat-card">
            <span className="stat-kicker"><Sparkles size={16} /> Profil Derinliği</span>
            <span className="stat-value">
              {organizations.length === 0
                ? 0
                : Math.round(
                  organizations.reduce((total, organization) => total + (organization.completionPercentage || 0), 0) /
                  organizations.length
                )}
            </span>
            <span className="stat-label">Ortalama doluluk</span>
          </GlassCard>
          <GlassCard className="stat-card">
            <span className="stat-kicker"><Users2 size={16} /> Görünür Sonuç</span>
            <span className="stat-value">{filteredOrganizations.length}</span>
            <span className="stat-label">Filtrelenen kurum</span>
          </GlassCard>
        </div>
      </section>

      <section className="org-controls glass">
        <div className="org-search">
          <Search size={18} />
          <input
            type="text"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Kurum, şehir, faaliyet alanı, sertifika veya referans ara..."
          />
        </div>

        <div className="org-filter-tabs">
          <div className="org-filter-tab active">Canlı tedarikçi kayıtları</div>
        </div>
      </section>

      {highlightedOrganization && (
        <GlassCard className="featured-org-card">
          <div className="featured-org-topline">
            <span className="featured-chip">Öne Çıkan Tedarikçi</span>
            <span className="role-badge firma">Tedarikçi</span>
          </div>

          <div className="featured-org-grid">
            <div>
              <div className="featured-org-brand">
                <div className="featured-org-logo">{highlightedOrganization.name.substring(0, 1)}</div>
                <div>
                  <h3 className="featured-org-name">{highlightedOrganization.name}</h3>
                  <div className="featured-org-meta">
                    <span><MapPin size={14} /> {highlightedOrganization.location || 'Türkiye'}</span>
                    {highlightedOrganization.website && (
                      <span><Globe size={14} /> Web sitesi mevcut</span>
                    )}
                    {highlightedOrganization.verified && (
                      <span><ShieldCheck size={14} /> Doğrulanmış profil</span>
                    )}
                  </div>
                </div>
              </div>

              <p className="featured-org-description">
                {highlightedOrganization.description || 'Bu kurum için henüz detaylı açıklama paylaşılmadı.'}
              </p>

              <div className="featured-org-tags">
                {getOrganizationTags(highlightedOrganization).map((tag) => (
                  <span key={tag} className="tag">{tag}</span>
                ))}
              </div>
            </div>

            <div className="featured-org-side">
              <div className="featured-metrics">
                <div>
                  <span>Yetkinlik</span>
                  <strong>{highlightedOrganization.capabilities.length}</strong>
                </div>
                <div>
                  <span>Sertifika</span>
                  <strong>{highlightedOrganization.certifications.length}</strong>
                </div>
                <div>
                  <span>Referans</span>
                  <strong>{highlightedOrganization.references.length}</strong>
                </div>
              </div>
              <Link to={`/kurumlar/${highlightedOrganization.id}`} className="btn btn-primary featured-org-link">
                Profili İncele <ArrowUpRight size={16} />
              </Link>
            </div>
          </div>
        </GlassCard>
      )}

      <div className="org-list">
        {filteredOrganizations.map((organization) => (
          <GlassCard key={organization.id} className="org-card">
            <div className="org-card-top">
              <div className="org-logo">
                {organization.name.substring(0, 1)}
              </div>
              <div className="org-card-meta">
                <span className="role-badge firma">Tedarikçi</span>
                
                {organization.verified && (
                  <span className="verified-pill"><ShieldCheck size={14} /> Doğrulandı</span>
                )}
              </div>
            </div>

            <div className="org-info">
              <Link to={`/kurumlar/${organization.id}`} className="org-title-link">
                <h3 className="org-name-link">{organization.name}</h3>
              </Link>

              <div className="org-location-row">
                <span><MapPin size={14} /> {organization.location || 'Türkiye'}</span>
                {organization.website && (
                  <span><Globe size={14} /> {organization.website.replace(/^https?:\/\//, '')}</span>
                )}
              </div>

              <p className="org-description">
                {organization.description || 'Kurum için açıklama bilgisi henüz girilmedi.'}
              </p>

              <div className="org-tags">
                {getOrganizationTags(organization).length > 0 ? (
                  getOrganizationTags(organization).map((tag) => (
                    <span key={tag} className="tag">{tag}</span>
                  ))
                ) : (
                  <span className="tag muted">Profil detayları güncelleniyor</span>
                )}
              </div>

              <div className="org-card-footer">
                <div className="org-mini-stats">
                  <span>{organization.certifications.length} sertifika</span>
                  <span>{organization.references.length} referans</span>
                </div>
                <Link to={`/kurumlar/${organization.id}`} className="btn btn-outline org-card-link">
                  Profili Gör
                </Link>
              </div>
            </div>
          </GlassCard>
        ))}
      </div>

      {filteredOrganizations.length === 0 && (
        <GlassCard className="org-empty-state">
          <h3>Aradığınız kurum bulunamadı</h3>
          <p>Arama kelimelerini sadeleştirerek tekrar deneyin.</p>
        </GlassCard>
      )}
    </div>
  );
};

export default Organizations;
