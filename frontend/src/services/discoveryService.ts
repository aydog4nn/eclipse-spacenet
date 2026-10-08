import api from './api';
import type { Organization, Project, RestResponse, SupplierCategory } from '../types';

interface SupplierFilters {
  searchTerm?: string;
  supplierCategory?: string;
}

export interface ProjectMatchResponse {
  organizationId: string;
  organizationName: string;
  matchScore: number;
  matchedReasons: string[];
}

interface SupplierApiResponse {
  organizationId?: string;
  id?: string;
  organizationName?: string;
  name?: string;
  supplierCategory?: SupplierCategory;
  description?: string | null;
  location?: string | null;
  expertiseAreas?: string[] | null;
  capabilities?: string[] | null;
  specialties?: string[] | null;
  fieldsOfActivity?: string[] | null;
  certifications?: string[] | null;
  references?: string[] | null;
  website?: string | null;
  verified?: boolean;
  teamSize?: number;
  matchScore?: number;
  matchedReasons?: string[];
}

const normalizeSupplier = (supplier: SupplierApiResponse): Organization => {
  const expertiseAreas = supplier.expertiseAreas || [];
  const capabilities = supplier.capabilities || [];
  const specialties = supplier.specialties || [];

  const mergedCapabilities = Array.from(
    new Set([...capabilities, ...expertiseAreas, ...specialties].filter(Boolean))
  );

  return {
    id: supplier.organizationId || supplier.id || crypto.randomUUID(),
    name: supplier.organizationName || supplier.name || 'İsimsiz Tedarikçi',
    role: 'TEDARIKCI',
    supplierCategory: supplier.supplierCategory,
    description: supplier.description || 'Bu tedarikçi için henüz açıklama eklenmemiş.',
    fieldsOfActivity: supplier.fieldsOfActivity || [],
    specialties,
    capabilities: mergedCapabilities,
    certifications: supplier.certifications || [],
    references: supplier.references || [],
    location: supplier.location || 'Belirtilmedi',
    website: supplier.website || '',
    verified: Boolean(supplier.verified),
    matchScore: supplier.matchScore,
    matchedReasons: supplier.matchedReasons || [],
    teamSize: supplier.teamSize || 0,
  };
};

const DiscoveryService = {
  getSuppliers: async (filters?: SupplierFilters) => {
    const normalizedSearch = filters?.searchTerm?.trim();
    const normalizedCategory =
      filters?.supplierCategory && filters.supplierCategory !== 'Tum Kategoriler'
        ? filters.supplierCategory
        : undefined;

    const res = await api.get<RestResponse<SupplierApiResponse[]>>('/discovery/suppliers', {
      params: {
        search: normalizedSearch,
        keyword: normalizedSearch,
        q: normalizedSearch,
        supplierCategory: normalizedCategory,
        category: normalizedCategory,
      },
    });

    return {
      ...res.data,
      data: (res.data.data || []).map(normalizeSupplier),
    };
  },
  
  getProjectMatches: async (projectId: string) => {
    const res = await api.get<RestResponse<ProjectMatchResponse[]>>(`/discovery/projects/${projectId}/supplier-matches`);
    return res.data;
  },

  getSupplierById: async (organizationId: string) => {
    const response = await DiscoveryService.getSuppliers();
    if (!response.success) {
      return {
        ...response,
        data: undefined as unknown as Organization,
      };
    }

    const supplier = response.data.find((item) => item.id === organizationId);

    return {
      success: Boolean(supplier),
      message: supplier ? response.message : 'Supplier not found',
      data: supplier as Organization,
      timestamp: response.timestamp,
    };
  },
  
  getMatchingProjects: async () => {
    const res = await api.get<RestResponse<Project[]>>('/discovery/me/matching-projects');
    return res.data;
  }
};

export default DiscoveryService;
