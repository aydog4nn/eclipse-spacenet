import api from './api';
import type { RestResponse } from '../types';

export interface CapabilityResponse {
  id: string;
  name: string;
  capabilityGroup: string;
  description: string;
  active: boolean;
}

export interface CapabilityRequest {
  name: string;
  capabilityGroup: string;
  description?: string;
  active?: boolean;
}

const CapabilityService = {
  getCapabilities: async () => {
    const response = await api.get<RestResponse<CapabilityResponse[]>>('/capabilities');
    return response.data;
  },

  createCapability: async (request: CapabilityRequest) => {
    const response = await api.post<RestResponse<CapabilityResponse>>('/capabilities', request);
    return response.data;
  },

  updateCapability: async (id: string, request: CapabilityRequest) => {
    const response = await api.put<RestResponse<CapabilityResponse>>(`/capabilities/${id}`, request);
    return response.data;
  },

  deleteCapability: async (id: string) => {
    const response = await api.delete(`/capabilities/${id}`);
    return response.data;
  }
};

export default CapabilityService;
