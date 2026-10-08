import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useData } from '../context/DataContext';
import GlassCard from '../components/common/GlassCard';
import MatchBadge from '../components/common/MatchBadge';
import { Search, SlidersHorizontal, RotateCcw } from 'lucide-react';
import DiscoveryService from '../services/discoveryService';
import type { Organization, SupplierCategory } from '../types';
import './Discover.css';

const collectSearchableStrings = (value: unknown): string[] => {
  if (typeof value === 'string') return [value];
  if (Array.isArray(value)) return value.flatMap(collectSearchableStrings);
  if (value && typeof value === 'object') {
    return Object.values(value).flatMap(collectSearchableStrings);
  }
  return [];
};

const Discover = () => {
  const { organizations, loading } = useData();
  const suppliers = useMemo(
    () => organizations.filter((o) => o.role === 'TEDARIKCI'),
    [organizations]
  );
  
  const [draftSearchTerm, setDraftSearchTerm] = useState('');
  const [draftCategory, setDraftCategory] = useState<SupplierCategory | 'Tum Kategoriler'>('Tum Kategoriler');
  const [searchTerm, setSearchTerm] = useState('');
  const [filterCategory, setFilterCategory] = useState<SupplierCategory | 'Tum Kategoriler'>('Tum Kategoriler');
  const [visibleSuppliers, setVisibleSuppliers] = useState<Organization[]>(suppliers);
  const [filtering, setFiltering] = useState(false);
  const [initialLoaded, setInitialLoaded] = useState(false);

  const normalize = (value: string) =>
    value
      .toLocaleLowerCase('tr-TR')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');

  const filtered = useMemo(() => {
    const normalizedSearch = normalize(searchTerm.trim());
    const searchTokens = normalizedSearch.split(/\s+/).filter(Boolean);

    return visibleSuppliers.filter((supplier) => {
      const searchIndex = [
        supplier.name,
        supplier.description,
        supplier.location,
        supplier.website,
        ...(supplier.capabilities || []),
        ...(supplier.specialties || []),
        ...(supplier.fieldsOfActivity || []),
        ...(supplier.certifications || []),
        ...collectSearchableStrings(supplier)
      ]
        .filter(Boolean)
        .map(normalize)
        .join(' ');

      const matchesSearch =
        searchTokens.length === 0 || searchTokens.every((token) => searchIndex.includes(token));
      const matchesCategory =
        filterCategory === 'Tum Kategoriler' || supplier.supplierCategory === filterCategory;

      return matchesSearch && matchesCategory;
    });
  }, [filterCategory, searchTerm, visibleSuppliers]);

  useEffect(() => {
    if (initialLoaded) return;

    const loadInitialSuppliers = async () => {
      setFiltering(true);

      try {
        const response = await DiscoveryService.getSuppliers();
        if (response.success) {
          setVisibleSuppliers(response.data.filter((supplier) => supplier.role === 'TEDARIKCI'));
          return;
        }
      } catch (error) {
        console.error('Başlangıç tedarikçi listesi alınamadı:', error);
      } finally {
        setFiltering(false);
        setInitialLoaded(true);
      }

      setVisibleSuppliers(suppliers);
    };

    loadInitialSuppliers();
  }, [initialLoaded, suppliers]);

  useEffect(() => {
    if (!initialLoaded || visibleSuppliers.length > 0) return;
    if (suppliers.length > 0) {
      setVisibleSuppliers(suppliers);
    }
  }, [initialLoaded, suppliers, visibleSuppliers.length]);

  const handleApplyFilters = async (e: React.FormEvent) => {
    e.preventDefault();

    setFiltering(true);

    try {
      const response = await DiscoveryService.getSuppliers({
        searchTerm: draftSearchTerm,
        supplierCategory: draftCategory,
      });

      if (response.success) {
        setVisibleSuppliers(response.data.filter((supplier) => supplier.role === 'TEDARIKCI'));
      }
    } catch (error) {
      console.error('Tedarikçi filtreleme isteği başarısız:', error);
      setVisibleSuppliers(suppliers);
    } finally {
      setSearchTerm(draftSearchTerm);
      setFilterCategory(draftCategory);
      setFiltering(false);
    }
  };

  const handleResetFilters = async () => {
    setDraftSearchTerm('');
    setDraftCategory('Tum Kategoriler');

    setFiltering(true);
    try {
      const response = await DiscoveryService.getSuppliers();
      if (response.success) {
        setVisibleSuppliers(response.data.filter((supplier) => supplier.role === 'TEDARIKCI'));
      }
    } catch (error) {
      console.error('Tedarikçi listesi yenilenemedi:', error);
      setVisibleSuppliers(suppliers);
    } finally {
      setSearchTerm('');
      setFilterCategory('Tum Kategoriler');
      setFiltering(false);
    }
  };

  if (loading) {
    return <div className="container" style={{paddingTop: '15rem', textAlign: 'center'}}><h2>Yükleniyor...</h2></div>;
  }

  return (
    <div className="discover-page container">
      <div className="discover-header">
        <h2 className="section-subtitle text-gradient">SPACENET TEDARİKÇİ AĞI</h2>
        <h1 className="section-title">
          Yetkin tedarikçileri<br/>
          <span className="text-primary">hızlıca keşfedin</span>
        </h1>
        <p className="section-desc">
          SpaceNet tedarikçi rehberi; uzmanlık, kategori ve teknik yetkinlik bazında filtreleme yaparak projeniz için en uygun adayları görünür hale getirir.
        </p>
      </div>

      <form className="filter-bar glass" onSubmit={handleApplyFilters}>
        <div className="filter-group">
          <label>TEDARİKÇİ KATEGORİSİ</label>
          <div className="filter-field">
            <SlidersHorizontal size={18} />
          <select 
            value={draftCategory} 
            onChange={(e) => setDraftCategory(e.target.value as SupplierCategory | 'Tum Kategoriler')}
            className="filter-select"
          >
            <option value="Tum Kategoriler">Tüm Kategoriler</option>
            <option value="KOBI">KOBI</option>
            <option value="STARTUP">Startup</option>
            <option value="URETICI">Üretici</option>
            <option value="ARGE_FIRMASI">AR-GE Firması</option>
            <option value="HIZMET_SAGLAYICI">Hizmet Sağlayıcı</option>
            <option value="YAN_SANAYI">Yan Sanayi</option>
          </select>
          </div>
        </div>
        <div className="filter-group">
          <label>KURUM ARA</label>
          <div className="filter-field">
            <Search size={18} />
          <input 
            type="text" 
            placeholder="Kurum, yetkinlik, sertifika veya şehir ara..." 
            value={draftSearchTerm}
            onChange={(e) => setDraftSearchTerm(e.target.value)}
            className="filter-input"
          />
          </div>
        </div>
        <div className="filter-actions">
          <button type="submit" className="filter-btn filter-btn-primary" disabled={filtering}>
            <Search size={18} /> {filtering ? 'Sorgulanıyor' : 'Filtrele'}
          </button>
          <button type="button" className="filter-btn filter-btn-secondary" onClick={handleResetFilters} disabled={filtering}>
            <RotateCcw size={16} /> Temizle
          </button>
        </div>
      </form>

      <div className="filter-summary">
        <span>{filtered.length} tedarikci listeleniyor</span>
        {(searchTerm || filterCategory !== 'Tum Kategoriler') && (
          <span>
            Aktif filtre:
            {searchTerm ? ` "${searchTerm}"` : ' tum anahtar kelimeler'}
            {filterCategory !== 'Tum Kategoriler' ? ` / ${filterCategory}` : ''}
          </span>
        )}
      </div>

      <div className="supplier-grid">
        {filtered.map(supplier => (
          <GlassCard key={supplier.id} hoverable className="supplier-card">
            <div className="supplier-card-header">
              <div className="supplier-icon">
                <span style={{ fontSize: '1.5rem', color: 'var(--color-primary)' }}>🚀</span>
              </div>
              <MatchBadge score={Math.min(98, 60 + supplier.capabilities.length * 6)} />
            </div>
            
            <h3 className="supplier-name">{supplier.name}</h3>
            <p className="supplier-desc">{supplier.description}</p>
            
            <div className="supplier-caps">
              {supplier.capabilities.slice(0, 3).map(cap => (
                <span key={cap} className="cap-tag">{cap}</span>
              ))}
              {supplier.capabilities.length > 3 && (
                <span className="cap-tag">+{supplier.capabilities.length - 3}</span>
              )}
            </div>
            
            <div className="supplier-actions">
              <Link to={`/kurumlar/${supplier.id}`} className="btn btn-outline" style={{width: '100%', textAlign: 'center'}}>PROFİLİ GÖR</Link>
            </div>
          </GlassCard>
        ))}
      </div>

      {filtered.length === 0 && (
        <div className="glass" style={{ padding: '2rem', textAlign: 'center', color: 'var(--color-text-muted)' }}>
          Arama kriterlerinize uygun tedarikçi bulunamadı.
        </div>
      )}
    </div>
  );
};

export default Discover;
