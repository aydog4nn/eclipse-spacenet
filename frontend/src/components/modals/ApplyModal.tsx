import { useState } from 'react';
import { X, Send } from 'lucide-react';
import GlassCard from '../common/GlassCard';
import ApplicationService from '../../services/applicationService';
import { useToast } from '../../context/ToastContext';
import './ApplyModal.css';

interface ApplyModalProps {
  projectId: string;
  projectTitle: string;
  onClose: () => void;
  onSuccess: () => void;
}

const ApplyModal = ({ projectId, projectTitle, onClose, onSuccess }: ApplyModalProps) => {
  const { showToast } = useToast();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    shortDescription: '',
    whyFit: '',
    referenceProjectNote: '',
    documentUrl: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await ApplicationService.applyToProject(projectId, formData);
      showToast('Başvurunuz başarıyla iletildi.', 'success');
      onSuccess();
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Başvuru sırasında bir hata oluştu.';
      showToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <GlassCard className="apply-modal-card">
          <div className="modal-header">
            <div>
              <h2 className="modal-title">İlana Başvur</h2>
              <p className="modal-subtitle">{projectTitle}</p>
            </div>
            <button className="close-btn" onClick={onClose}><X size={24} /></button>
          </div>

          <form onSubmit={handleSubmit} className="apply-form">
            <div className="form-group">
              <label>KISA ÖZET / BAŞLIK *</label>
              <input 
                required 
                placeholder="Başvurunuzu özetleyen bir cümle..." 
                value={formData.shortDescription}
                onChange={(e) => setFormData({...formData, shortDescription: e.target.value})}
              />
            </div>

            <div className="form-group">
              <label>NEDEN SİZ? (YETKİNLİK VE TECRÜBE) *</label>
              <textarea 
                required 
                rows={4}
                placeholder="Bu proje için neden en uygun aday olduğunuzu açıklayın..." 
                value={formData.whyFit}
                onChange={(e) => setFormData({...formData, whyFit: e.target.value})}
              />
            </div>

            <div className="form-group">
              <label>BENZER PROJE REFERANSLARI</label>
              <textarea 
                rows={2}
                placeholder="Daha önce gerçekleştirdiğiniz benzer projelerden bahsedin..." 
                value={formData.referenceProjectNote}
                onChange={(e) => setFormData({...formData, referenceProjectNote: e.target.value})}
              />
            </div>

            <div className="form-group">
              <label>EK DOSYA / DOKÜMAN LİNKİ (OPSİYONEL)</label>
              <input 
                type="url"
                placeholder="https://..." 
                value={formData.documentUrl}
                onChange={(e) => setFormData({...formData, documentUrl: e.target.value})}
              />
            </div>

            <div className="modal-actions">
              <button type="button" className="btn btn-outline" onClick={onClose}>VAZGEÇ</button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'GÖNDERİLİYOR...' : (
                  <>GÖNDER <Send size={18} style={{marginLeft: '0.5rem'}} /></>
                )}
              </button>
            </div>
          </form>
        </GlassCard>
      </div>
    </div>
  );
};

export default ApplyModal;
