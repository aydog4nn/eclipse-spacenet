import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import PanelSidebar from './PanelSidebar';
import PanelDashboard from '../../pages/panel/PanelDashboard';
import PanelProjects from '../../pages/panel/PanelProjects';
import PanelApplications from '../../pages/panel/PanelApplications';
import AddProject from '../../pages/panel/AddProject';
import AdminApprovals from '../../pages/panel/AdminApprovals';
import AdminCapabilities from '../../pages/panel/AdminCapabilities';
import ProfileSettings from '../../pages/panel/ProfileSettings';
import './PanelLayout.css';

const PanelLayout = () => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="panel-container">
      <div className="panel-content glass-panel">
        <Routes>
          <Route index element={<PanelDashboard />} />
          <Route path="projects" element={<PanelProjects />} />
          <Route path="projects/new" element={<AddProject />} />
          <Route path="applications" element={<PanelApplications />} />
          <Route path="admin/approvals" element={<AdminApprovals />} />
          <Route path="admin/capabilities" element={<AdminCapabilities />} />
          <Route path="settings" element={<ProfileSettings />} />
          <Route path="*" element={<Navigate to="/panel" replace />} />
        </Routes>
      </div>
      <PanelSidebar />
    </div>
  );
};

export default PanelLayout;
