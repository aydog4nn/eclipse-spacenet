import api from './api';
import type { 
  Project, 
  ProjectType, 
  SupplierCategory, 
  RestResponse, 
  CollaborationType, 
  ConfidentialityLevel, 
  ProjectStatus 
} from '../types';

export interface ProjectRequest {
  title: string;
  description: string;
  projectType: ProjectType | string;
  collaborationType: CollaborationType;
  capabilityIds: string[]; // UUIDs
  preferredSupplierCategory?: SupplierCategory;
  locationPreference?: string;
  durationText?: string;
  budgetRange?: string;
  certificationRequirements: string[];
  applicationDeadline: string; // LocalDate format
  confidentialityLevel: ConfidentialityLevel;
  status: ProjectStatus;
}

const ProjectService = {
  getAllProjects: async () => {
    const response = await api.get<RestResponse<Project[]>>('/projects');
    return response.data;
  },

  getMyProjects: async () => {
    const response = await api.get<RestResponse<Project[]>>('/projects/my');
    return response.data;
  },

  getProjectById: async (id: string) => {
    const response = await api.get<RestResponse<Project>>(`/projects/${id}`);
    return response.data;
  },

  createProject: async (request: ProjectRequest) => {
    const response = await api.post<RestResponse<Project>>('/projects', request);
    return response.data;
  },

  getProjectsByFirm: async (firmId: string) => {
    const response = await api.get<RestResponse<Project[]>>(`/projects/firm/${firmId}`);
    return response.data;
  }
};

export default ProjectService;
