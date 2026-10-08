import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Check } from 'lucide-react';
import GlassCard from '../../components/common/GlassCard';
import ProjectService from '../../services/projectService';
import CapabilityService from '../../services/capabilityService';
import { useToast } from '../../context/ToastContext';
import { useData } from '../../context/DataContext';
import type { 
  ProjectType, 
  SupplierCategory, 
  CollaborationType, 
  ConfidentialityLevel, 
  ProjectStatus 
} from '../../types';
import type { CapabilityResponse } from '../../services/capabilityService';
import type { ProjectRequest } from '../../services/projectService';
import './AddProject.css';

const AddProject = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { refreshData, addProjectToCache } = useData();
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [capabilities, setCapabilities] = useState<CapabilityResponse[]>([]);
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    projectType: 'TEDARIKCI_ARANIYOR' as ProjectType,
    collaborationType: 'TEDARIKCI_ARIYORUM' as CollaborationType,
    capabilityIds: [] as string[],
    preferredSupplierCategory: 'KOBI' as SupplierCategory,
    locationPreference: 'Ankara',
    durationText: '',
    budgetRange: '',
    certificationRequirements: '',
    applicationDeadline: '',
    confidentialityLevel: 'ORTA' as ConfidentialityLevel,
    status: 'AKTIF' as ProjectStatus
  });

  useEffect(() => {
    const fetchCapabilities = async () => {
      try {
        const res = await CapabilityService.getCapabilities();
        if (res.success) {
          setCapabilities(res.data);
        }
      } catch {
        console.error('Yetkinlikler alınamadı');
      }
    };
    fetchCapabilities();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleCapabilityToggle = (id: string) => {
    setFormData(prev => {
      const exists = prev.capabilityIds.includes(id);
      if (exists) {
        return { ...prev, capabilityIds: prev.capabilityIds.filter(c => c !== id) };
      }
      return { ...prev, capabilityIds: [...prev.capabilityIds, id] };
    });
  };

  const filteredCapabilities = capabilities.filter(cap => 
    cap.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const capabilityGroups: string[] = ['DONANIM', 'YAZILIM', 'ARGE_TEST_URETIM'];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.capabilityIds.length === 0) {
      showToast('Lütfen en az bir yetkinlik seçin.', 'error');
      return;
    }

    setLoading(true);

    try {
      const { certificationRequirements, ...rest } = formData;
      const request = {
        ...rest,
        certificationRequirements: certificationRequirements.split(',').map(s => s.trim()).filter(s => s !== '')
      };
      
      const response = await ProjectService.createProject(request as ProjectRequest);
      
      if (response.success) {
        addProjectToCache(response.data);
        await refreshData();
        showToast('İlan başarıyla yayına alındı.', 'success');
        navigate('/panel/projects');
      } else {
        throw new Error(response.message || 'İlan oluşturulamadı.');
      }
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || err.message || 'İlan oluşturulurken bir hata oluştu.';
      console.error('Submission error:', err.response?.data);
      showToast(errorMsg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="add-project-page">
      <div className="panel-header">
        <h1 className="panel-title">Yeni Proje / İhtiyaç İlanı</h1>
        <p className="panel-subtitle">Ekosisteme yeni bir çağrı başlatın ve en uygun partnerleri bulun.</p>
      </div>

      <GlassCard className="form-card">
        <form onSubmit={handleSubmit} className="project-form">
          <div className="form-group">
            <label>İLAN BAŞLIĞI *</label>
            <input name="title" required value={formData.title} onChange={handleChange} placeholder="Örn: X-Band Anten Modülü Tedariği" />
          </div>

          <div className="form-group">
            <label>PROJE AÇIKLAMASI *</label>
            <textarea name="description" required value={formData.description} onChange={handleChange} rows={4} placeholder="İhtiyacınızı detaylandırın..." />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>İLAN TİPİ *</label>
              <select name="projectType" value={formData.projectType} onChange={handleChange}>
                <option value="TEDARIKCI_ARANIYOR">Tedarikçi Aranıyor</option>
                <option value="ARGE_PARTNERI">AR-GE Partneri</option>
                <option value="PROTOTIP_GELISTIRME">Prototip Geliştirme</option>
                <option value="URETIM_ORTAGI">Üretim Ortağı</option>
                <option value="TEST_ALTYAPISI">Test Altyapısı</option>
              </select>
            </div>
            <div className="form-group">
              <label>İŞ BİRLİĞİ TİPİ *</label>
              <select name="collaborationType" value={formData.collaborationType} onChange={handleChange}>
                <option value="TEDARIKCI_ARIYORUM">Tedarikçi Arıyorum</option>
                <option value="ARGE_PARTNERI_ARIYORUM">AR-GE Partneri Arıyorum</option>
                <option value="PROTOTIP_DESTEGI_ARIYORUM">Prototip Desteği Arıyorum</option>
                <option value="URETIM_ORTAGI_ARIYORUM">Üretim Ortağı Arıyorum</option>
                <option value="TEST_ALTYAPISI_ARIYORUM">Test Altyapısı Arıyorum</option>
              </select>
            </div>
          </div>

          <div className="form-group capability-container">
            <label>ARANAN YETKİNLİKLER *</label>
            <div className="capability-search">
              <Search size={18} />
              <input 
                type="text" 
                placeholder="Yetkinlik ara..." 
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>
            
            <div className="capability-selector-area">
              {capabilityGroups.map(group => {
                const groupCaps = filteredCapabilities.filter(c => c.capabilityGroup === group);
                if (groupCaps.length === 0) return null;
                
                return (
                  <div key={group} className="capability-category-group">
                    <h5 className="category-label text-gradient">{group.replace(/_/g, ' ')}</h5>
                    <div className="capability-selector-grid">
                      {groupCaps.map(cap => (
                        <div 
                          key={cap.id} 
                          className={`capability-item ${formData.capabilityIds.includes(cap.id) ? 'selected' : ''}`}
                          onClick={() => handleCapabilityToggle(cap.id)}
                        >
                          <div className="cap-check">
                            {formData.capabilityIds.includes(cap.id) && <Check size={10} />}
                          </div>
                          {cap.name}
                        </div>
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>SON BAŞVURU TARİHİ *</label>
              <input type="date" name="applicationDeadline" required value={formData.applicationDeadline} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label>LOKASYON TERCİHİ</label>
              <input name="locationPreference" value={formData.locationPreference} onChange={handleChange} />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>TERCİH EDİLEN KATEGORİ</label>
              <select name="preferredSupplierCategory" value={formData.preferredSupplierCategory} onChange={handleChange}>
                <option value="KOBI">KOBI</option>
                <option value="STARTUP">Startup</option>
                <option value="URETICI">Üretici</option>
                <option value="ARGE_FIRMASI">AR-GE Firması</option>
                <option value="HIZMET_SAGLAYICI">Hizmet Sağlayıcı</option>
                <option value="YAN_SANAYI">Yan Sanayi</option>
              </select>
            </div>
             <div className="form-group">
              <label>GİZLİLİK SEVİYESİ *</label>
              <select name="confidentialityLevel" value={formData.confidentialityLevel} onChange={handleChange}>
                <option value="DUSUK">Düşük</option>
                <option value="ORTA">Orta</option>
                <option value="YUKSEK">Yüksek</option>
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>TAHMİNİ SÜRE</label>
              <input name="durationText" placeholder="Örn: 6 Ay" value={formData.durationText} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label>BÜTÇE ARALIĞI</label>
              <input name="budgetRange" placeholder="Örn: 100k - 500k" value={formData.budgetRange} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
             <label>GEREKLİ SERTİFİKALAR (Virgülle ayırın)</label>
             <input name="certificationRequirements" value={formData.certificationRequirements} onChange={handleChange} placeholder="Örn: AS9100, ISO 9001" />
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>İPTAL</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'YAYINLANIYOR...' : 'İLANI YAYINLA'}
            </button>
          </div>
        </form>
      </GlassCard>
    </div>
  );
};

export default AddProject;
