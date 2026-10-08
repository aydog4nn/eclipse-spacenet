import { useState, useEffect, useCallback } from 'react';
import AuthService from '../../services/authService';
import type { UserResponse } from '../../services/authService';
import { useToast } from '../../context/ToastContext';
import GlassCard from '../../components/common/GlassCard';
import { CheckCircle, Clock, Mail, Globe } from 'lucide-react';
import './AdminApprovals.css';

const AdminApprovals = () => {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const { showToast } = useToast();

  const fetchUsers = useCallback(async () => {
    try {
      const res = await AuthService.adminGetAllUsers();
      if (res.success) {
        setUsers(res.data.filter((u: UserResponse) => u.role !== 'ADMIN'));
      }
    } catch {
      showToast('Kullanıcı listesi alınırken hata oluştu.', 'error');
    } finally {
      setLoading(false);
    }
  }, [showToast]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleToggleApproval = async (userId: string, currentStatus: boolean) => {
    try {
      const res = await AuthService.adminUpdateApproval(userId, !currentStatus);
      if (res.success) {
        showToast(`Durum başarıyla güncellendi: ${!currentStatus ? 'Onaylandı' : 'Onay Kaldırıldı'}`, 'success');
        // Update local state
        setUsers(prev => prev.map(u => u.id === userId ? { ...u, approved: !currentStatus } : u));
      }
    } catch {
      showToast('Gelen onay isteği işlenemedi.', 'error');
    }
  };

  if (loading) {
    return <div className="admin-loading">Veriler yükleniyor...</div>;
  }

  return (
    <div className="admin-approvals-page">
      <div className="panel-header">
        <h1 className="panel-title">Kurum Onay Yönetimi</h1>
        <p className="panel-subtitle">Sisteme yeni kayıt olan kurumların erişim yetkilerini buradan yönetebilirsiniz.</p>
      </div>

      <GlassCard className="approvals-card">
        <div className="table-responsive">
          <table className="approvals-table">
            <thead>
              <tr>
                <th>KURUM / KULLANICI</th>
                <th>ROL / KATEGORİ</th>
                <th>İLETİŞİM</th>
                <th>DURUM</th>
                <th>AKSİYON</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td>
                    <div className="user-cell">
                      <div className="user-avatar">{user.organizationName?.[0] || user.email[0].toUpperCase()}</div>
                      <div>
                        <div className="org-name">{user.organizationName || 'Bireysel / Belirtilmemiş'}</div>
                        <div className="user-email-sub">{user.email}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className={`role-badge ${user.role.toLowerCase()}`}>{user.role}</span>
                    {user.supplierCategory && (
                      <span className="category-tag">{user.supplierCategory}</span>
                    )}
                  </td>
                  <td>
                    <div className="contact-info">
                      <div className="contact-item"><Mail size={14} /> {user.email}</div>
                      {user.website && <div className="contact-item"><Globe size={14} /> {user.website}</div>}
                    </div>
                  </td>
                  <td>
                    {user.approved ? (
                      <span className="status-pill approved">
                        <CheckCircle size={14} /> ONAYLI
                      </span>
                    ) : (
                      <span className="status-pill pending">
                        <Clock size={14} /> BEKLEMEDE
                      </span>
                    )}
                  </td>
                  <td>
                    <button 
                      className={`btn-action ${user.approved ? 'btn-revoke' : 'btn-approve'}`}
                      onClick={() => handleToggleApproval(user.id, user.approved)}
                    >
                      {user.approved ? 'ONAYI KALDIR' : 'ONAYLA'}
                    </button>
                  </td>
                </tr>
              ))}
              {users.length === 0 && (
                <tr>
                  <td colSpan={5} className="empty-row">Onay bekleyen veya kayıtlı kurum bulunmuyor.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </GlassCard>
    </div>
  );
};

export default AdminApprovals;
