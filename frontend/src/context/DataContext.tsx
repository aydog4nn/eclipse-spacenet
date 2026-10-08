import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import type { Capability, Organization, Project } from '../types';
import ProjectService from '../services/projectService';
import DiscoveryService from '../services/discoveryService';
import CapabilityService from '../services/capabilityService';

interface DataContextType {
  organizations: Organization[];
  projects: Project[];
  myProjects: Project[];
  capabilities: Capability[];
  loading: boolean;
  getOrganizationById: (id: string) => Organization | undefined;
  getProjectById: (id: string) => Project | undefined;
  refreshData: () => Promise<void>;
  addProjectToCache: (project: Project) => void;
}

const DataContext = createContext<DataContextType | undefined>(undefined);

const mergeProjects = (incoming: Project[], existing: Project[] = []) => {
  const projectMap = new Map<string, Project>();

  [...incoming, ...existing].forEach((project) => {
    projectMap.set(project.id, project);
  });

  return Array.from(projectMap.values()).sort((a, b) => {
    const left = new Date(b.createdAt || 0).getTime();
    const right = new Date(a.createdAt || 0).getTime();
    return left - right;
  });
};

export const DataProvider = ({ children }: { children: ReactNode }) => {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [myProjects, setMyProjects] = useState<Project[]>([]);
  const [capabilities, setCapabilities] = useState<Capability[]>([]);
  const [loading, setLoading] = useState(true);

  const addProjectToCache = useCallback((project: Project) => {
    setProjects((prev) => mergeProjects([project], prev));
    setMyProjects((prev) => mergeProjects([project], prev));
  }, []);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [projRes, orgRes, myProjRes, capRes] = await Promise.all([
        ProjectService.getAllProjects(),
        DiscoveryService.getSuppliers(),
        ProjectService.getMyProjects().catch(() => ({ success: false, data: [] })),
        CapabilityService.getCapabilities().catch(() => ({ success: false, data: [] }))
      ]);
      
      if (projRes.success) {
        setProjects((prev) => mergeProjects(projRes.data, prev));
      }
      if (orgRes.success) {
        setOrganizations(orgRes.data);
      }
      if (myProjRes.success) {
        setMyProjects((prev) => mergeProjects(myProjRes.data, prev));
      } else if (projRes.success) {
        setMyProjects((prev) => mergeProjects(prev, projRes.data));
      }
      if (capRes.success) {
        setCapabilities(
          capRes.data.map((capability) => ({
            id: capability.id,
            name: capability.name,
            capabilityGroup: capability.capabilityGroup as Capability['capabilityGroup'],
          }))
        );
      }
    } catch (err) {
      console.error('Data fetch error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const getOrganizationById = useCallback((id: string) => organizations.find((o) => o.id === id), [organizations]);
  const getProjectById = useCallback((id: string) => {
    return projects.find((p) => p.id === id) || myProjects.find((p) => p.id === id);
  }, [myProjects, projects]);

  return (
    <DataContext.Provider value={{
      organizations,
      projects,
      myProjects,
      capabilities,
      loading,
      getOrganizationById,
      getProjectById,
      refreshData: fetchData,
      addProjectToCache
    }}>
      {children}
    </DataContext.Provider>
  );
};

export const useData = () => {
  const context = useContext(DataContext);
  if (!context) throw new Error('useData must be used within DataProvider');
  return context;
};
