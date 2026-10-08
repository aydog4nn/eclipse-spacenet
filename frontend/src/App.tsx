import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/layout/Header';
import Footer from './components/layout/Footer';
import Home from './pages/Home';
import Discover from './pages/Discover';
import Projects from './pages/Projects';
import ProjectDetail from './pages/ProjectDetail';
import Login from './pages/Login';
import About from './pages/About';
import Organizations from './pages/Organizations';
import OrganizationDetail from './pages/OrganizationDetail';
import Register from './pages/Register';
import PanelLayout from './components/layout/PanelLayout';
import Chatbot from './components/common/Chatbot';

import { useAuth } from './context/AuthContext';

function App() {
  const { loading } = useAuth();
  
  if (loading) return null;

  return (
    <Router>
      <div className="app-container">
        <Header />
        
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/discover" element={<Discover />} />
            <Route path="/projects" element={<Projects />} />
            <Route path="/projects/:id" element={<ProjectDetail />} />
            <Route path="/kurumlar" element={<Organizations />} />
            <Route path="/kurumlar/:id" element={<OrganizationDetail />} />
            <Route path="/hakkimizda" element={<About />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/panel/*" element={<PanelLayout />} />
          </Routes>
        </main>
        
        <Footer />
        <Chatbot />
      </div>
    </Router>
  );
}

export default App;
