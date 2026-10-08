import { useState, useEffect, useCallback } from 'react';
import CapabilityService from '../../services/capabilityService';
import type { CapabilityResponse, CapabilityRequest } from '../../services/capabilityService';
import { useToast } from '../../context/ToastContext';
import GlassCard from '../../components/common/GlassCard';
import { Plus, Trash2, Edit2, Save } from 'lucide-react';
import './AdminCapabilities.css';

const AdminCapabilities = () => {
  const [capabilities, setCapabilities] = useState<CapabilityResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const { showToast } = useToast();
  
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  
  const [formData, setFormData] = useState<CapabilityRequest>({
    name: '',
    capabilityGroup: 'YAZILIM',
    description: '',
    active: true
  });

  const fetchCapabilities = useCallback(async () => {
    try {
      const res = await CapabilityService.getCapabilities();
      if (res.success) {
        setCapabilities(res.data);
      }
    } catch {
      showToast('Yetkinlikler yüklenemedi.', 'error');
    } finally {
      setLoading(false);
    }
  }, [showToast]);

  useEffect(() => {
    fetchCapabilities();
  }, [fetchCapabilities]);

  const handleSave = async () => {
    if (!formData.name) {
      showToast('Yetkinlik adı boş olamaz.', 'error');
      return;
    }
    
    try {
      if (editingId) {
        const res = await CapabilityService.updateCapability(editingId, formData);
        if (res.success) {
          showToast('Yetkinlik güncellendi.', 'success');
          setEditingId(null);
        }
      } else {
        const res = await CapabilityService.createCapability(formData);
        if (res.success) {
          showToast('Yeni yetkinlik eklendi.', 'success');
          setIsAdding(false);
        }
      }
      setFormData({ name: '', capabilityGroup: 'YAZILIM', description: '', active: true });
      fetchCapabilities();
    } catch {
      showToast('İşlem başarısız oldu.', 'error');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Bu yetkinliği silmek istediğinize emin misiniz?')) return;
    
    try {
      await CapabilityService.deleteCapability(id);
      showToast('Yetkinlik silindi.', 'success');
      fetchCapabilities();
    } catch {
      showToast('Silme işlemi başarısız.', 'error');
    }
  };

  const startEdit = (cap: CapabilityResponse) => {
    setEditingId(cap.id);
    setFormData({ 
      name: cap.name, 
      capabilityGroup: cap.capabilityGroup, 
      description: cap.description,
      active: cap.active
    });
    setIsAdding(false);
  };

  if (loading) return <div className="admin-loading">Yükleniyor...</div>;

  return (
    <div className="admin-capabilities-page">
      <div className="panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="panel-title">Yetkinlik Sözlüğü</h1>
          <p className="panel-subtitle">Sisteme yeni uzmanlık alanları ve teknolojiler ekleyin.</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setIsAdding(true); setEditingId(null); setFormData({name: '', capabilityGroup: 'YAZILIM', description: '', active: true}) }}>
          <Plus size={18} /> YENİ EKLE
        </button>
      </div>

      {(isAdding || editingId) && (
        <GlassCard className="capability-form-card">
          <h3 className="form-title">{editingId ? 'Yetkinliği Düzenle' : 'Yeni Yetkinlik Ekle'}</h3>
          <div className="admin-form-row">
            <div className="form-group">
              <label>YETKİNLİK ADI</label>
              <input 
                value={formData.name} 
                onChange={(e) => setFormData({...formData, name: e.target.value})} 
                placeholder="Örn: İyon İtki Sistemleri"
              />
            </div>
            <div className="form-group">
              <label>KATEGORİ</label>
              <select 
                value={formData.capabilityGroup} 
                onChange={(e) => setFormData({...formData, capabilityGroup: e.target.value})}
              >
                <option value="YAZILIM">Yazılım</option>
                <option value="DONANIM">Donanım</option>
                <option value="ARGE_TEST_URETIM">AR-GE / Test / Üretim</option>
              </select>
            </div>
          </div>
          <div className="form-group">
            <label>AÇIKLAMA</label>
            <textarea 
              value={formData.description} 
              onChange={(e) => setFormData({...formData, description: e.target.value})}
              placeholder="Kısa bir açıklama..."
            />
          </div>
          <div className="form-actions">
            <button className="btn btn-outline" onClick={() => { setIsAdding(false); setEditingId(null); }}>İPTAL</button>
            <button className="btn btn-primary" onClick={handleSave}>
              <Save size={18} /> {editingId ? 'GÜNCELLE' : 'KAYDET'}
            </button>
          </div>
        </GlassCard>
      )}

      <div className="capabilities-grid">
        {capabilities.map(cap => (
          <GlassCard key={cap.id} className="cap-list-item">
            <div className="cap-info">
              <div className="cap-cat-badge">{cap.capabilityGroup}</div>
              <h4 className="cap-name">{cap.name}</h4>
              <p className="cap-desc">{cap.description || 'Açıklama belirtilmemiş.'}</p>
            </div>
            <div className="cap-actions">
              <button className="cap-btn edit" onClick={() => startEdit(cap)} title="Düzenle">
                <Edit2 size={16} />
              </button>
              <button className="cap-btn delete" onClick={() => handleDelete(cap.id)} title="Sil">
                <Trash2 size={16} />
              </button>
            </div>
          </GlassCard>
        ))}
      </div>
    </div>
  );
};

export default AdminCapabilities;
