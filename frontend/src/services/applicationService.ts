import api from './api';
import type { Application, RestResponse, ApplicationStatus } from '../types';

export interface ApplicationRequest {
  shortDescription: string;
  whyFit: string;
  documentUrl?: string;
  referenceProjectNote?: string;
}

export interface ApplicationStatusUpdateRequest {
  status: ApplicationStatus;
  adminNote?: string;
}

const ApplicationService = {
  applyToProject: async (projectId: string, request: ApplicationRequest) => {
    const response = await api.post<RestResponse<Application>>(`/applications/projects/${projectId}`, request);
    return response.data;
  },

  withdrawApplication: async (applicationId: string) => {
    const response = await api.patch<RestResponse<string>>(`/applications/${applicationId}/withdraw`);
    return response.data;
  },

  getMyApplications: async () => {
    const response = await api.get<RestResponse<Application[]>>('/applications/my');
    return response.data;
  },

  getApplicationsForProject: async (projectId: string) => {
    const response = await api.get<RestResponse<Application[]>>(`/applications/projects/${projectId}`);
    return response.data;
  },

  updateApplicationStatus: async (applicationId: string, status: ApplicationStatus) => {
    const response = await api.patch<RestResponse<Application>>(`/applications/${applicationId}/status`, { status });
    return response.data;
  }
};

export default ApplicationService;
