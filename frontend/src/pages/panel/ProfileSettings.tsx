import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import AuthService from '../../services/authService';
import CapabilityService from '../../services/capabilityService';
import type { CapabilityResponse } from '../../services/capabilityService';
import { useToast } from '../../context/ToastContext';
import GlassCard from '../../components/common/GlassCard';
import PasswordField from '../../components/common/PasswordField';
import { User, Lock, Globe, MapPin, Phone, Building, Search, Check } from 'lucide-react';
import { extractTurkishPhoneDigits, formatTurkishPhoneForRequest } from '../../utils/phone';
import './ProfileSettings.css';

type ProfileResponse = Awaited<ReturnType<typeof AuthService.getProfile>>['data'] & {
  capabilityIds?: string[];
};

const ProfileSettings = () => {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [loading, setLoading] = useState(false);
  const [capabilities, setCapabilities] = useState<CapabilityResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  
  const [formData, setFormData] = useState({
    organizationName: '',
    website: '',
    location: '',
    phoneNumber: '',
    focusAreas: '',
    description: '',
    capabilityIds: [] as string[]
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  useEffect(() => {
    const init = async () => {
      if (user) {
        setFormData(prev => ({
          ...prev,
          organizationName: user.organizationName || '',
          website: user.website || '',
          location: user.location || '',
          phoneNumber: extractTurkishPhoneDigits(user.phoneNumber || ''),
        }));
        
        try {
          const [profileRes, capRes] = await Promise.all([
            AuthService.getProfile(),
            CapabilityService.getCapabilities()
          ]);

          if (capRes.success) {
            setCapabilities(capRes.data);
          }

          if (profileRes.success) {
            const profile = profileRes.data as ProfileResponse;
            setFormData(prev => ({
              ...prev,
              organizationName: profile.organizationName || '',
              website: profile.website || '',
              location: profile.location || '',
              phoneNumber: extractTurkishPhoneDigits(profile.phoneNumber || ''),
              focusAreas: Array.isArray(profile.focusAreas) ? profile.focusAreas.join(', ') : '',
              capabilityIds: profile.capabilityIds || []
            }));
          }
        } catch {
          console.error('Profil veya yetkinlikler yüklenemedi');
        }
      }
    };
    init();
  }, [user]);

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (user?.role === 'FIRMA') {
        await AuthService.updateFirmProfile({
          ...formData,
          phoneNumber: formatTurkishPhoneForRequest(formData.phoneNumber),
        });
      } else {
        const supplierRequest = {
          ...formData,
          phoneNumber: formatTurkishPhoneForRequest(formData.phoneNumber),
          focusAreas: formData.focusAreas.split(',').map(s => s.trim()).filter(s => s !== '')
        };
        await AuthService.updateSupplierProfile(supplierRequest);
      }
      showToast('Profil başarıyla güncellendi.', 'success');
    } catch {
      showToast('Profil güncellenirken hata oluştu.', 'error');
    } finally {
      setLoading(false);
    }
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

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      showToast('Yeni şifreler eşleşmiyor.', 'error');
      return;
    }
    
    setLoading(true);
    try {
      await AuthService.changePassword(passwordData);
      showToast('Şifreniz başarıyla değiştirildi.', 'success');
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Şifre değiştirilemedi.';
      showToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="profile-settings-page">
      <div className="panel-header">
        <h1 className="panel-title">Profil Ayarları</h1>
        <p className="panel-subtitle">Hesap bilgilerinizi ve güvenliğinizi yönetin.</p>
      </div>

      <div className="settings-grid">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          <GlassCard className="settings-card">
            <h3 className="settings-section-title"><User size={20} /> KURUM BİLGİLERİ</h3>
            <form onSubmit={handleProfileSubmit}>
              <div className="form-group">
                <label><Building size={16} /> Kurum Adı</label>
                <input 
                  value={formData.organizationName} 
                  onChange={(e) => setFormData({...formData, organizationName: e.target.value})} 
                />
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label><Globe size={16} /> Web Sitesi</label>
                  <input 
                    value={formData.website} 
                    onChange={(e) => setFormData({...formData, website: e.target.value})} 
                  />
                </div>
                <div className="form-group">
                  <label><MapPin size={16} /> Lokasyon</label>
                  <input 
                    value={formData.location} 
                    onChange={(e) => setFormData({...formData, location: e.target.value})} 
                  />
                </div>
              </div>

              <div className="form-group">
                <label><Phone size={16} /> Telefon Numarası</label>
                <div className="phone-input">
                  <span className="phone-prefix">+90</span>
                  <input 
                    inputMode="numeric"
                    maxLength={10}
                    value={formData.phoneNumber} 
                    onChange={(e) => setFormData({...formData, phoneNumber: extractTurkishPhoneDigits(e.target.value)})} 
                  />
                </div>
              </div>

              <button type="submit" className="btn btn-primary" disabled={loading}>
                BİLGİLERİ GÜNCELLE
              </button>
            </form>
          </GlassCard>

          {user?.role === 'TEDARIKCI' && (
            <GlassCard className="settings-card">
              <h3 className="settings-section-title"><Check size={20} /> YETKİNLİK YÖNETİMİ</h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--color-text-muted)', marginBottom: '1.5rem' }}>
                Kurumunuzun teknik kabiliyetlerini seçerek eşleşme oranınızı artırın.
              </p>
              
              <div className="capability-search" style={{ marginBottom: '1.5rem' }}>
                <Search size={18} />
                <input 
                  type="text" 
                  placeholder="Yetkinlik ara..." 
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>

              <div className="capability-selector-area" style={{ maxHeight: '300px', overflowY: 'auto' }}>
                {capabilityGroups.map(group => {
                  const groupCaps = filteredCapabilities.filter(c => c.capabilityGroup === group);
                  if (groupCaps.length === 0) return null;
                  
                  return (
                    <div key={group} className="capability-category-group">
                      <h5 className="category-label text-gradient" style={{ fontSize: '0.75rem', marginBottom: '0.8rem' }}>{group.replace(/_/g, ' ')}</h5>
                      <div className="capability-selector-grid">
                        {groupCaps.map(cap => (
                          <div 
                            key={cap.id} 
                            className={`capability-item ${formData.capabilityIds.includes(cap.id) ? 'selected' : ''}`}
                            onClick={() => handleCapabilityToggle(cap.id)}
                            style={{ padding: '0.5rem', fontSize: '0.8rem' }}
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
              
              <button 
                type="button" 
                className="btn btn-primary" 
                style={{ marginTop: '1.5rem', width: '100%' }}
                onClick={handleProfileSubmit}
                disabled={loading}
              >
                YETKİNLİKLERİ KAYDET
              </button>
            </GlassCard>
          )}
        </div>

        <GlassCard className="settings-card">
          <h3 className="settings-section-title"><Lock size={20} /> ŞİFRE DEĞİŞTİR</h3>
          <form onSubmit={handlePasswordSubmit}>
            <div className="form-group">
              <label>Mevcut Şifre</label>
              <PasswordField
                value={passwordData.currentPassword} 
                onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})} 
              />
            </div>
            <div className="form-group">
              <label>Yeni Şifre</label>
              <PasswordField
                value={passwordData.newPassword} 
                onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})} 
              />
            </div>
            <div className="form-group">
              <label>Yeni Şifre (Tekrar)</label>
              <PasswordField
                value={passwordData.confirmPassword} 
                onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})} 
              />
            </div>
            <button type="submit" className="btn btn-outline" disabled={loading} style={{ width: '100%', marginTop: '1rem' }}>
              ŞİFREYİ GÜNCELLE
            </button>
          </form>
        </GlassCard>
      </div>
    </div>
  );
};

export default ProfileSettings;
