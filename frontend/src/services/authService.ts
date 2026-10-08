import api from './api';
import type { Role, SupplierCategory, RestResponse } from '../types';

export type OnboardingStep = 'BASIC_INFO' | 'CAPABILITIES' | 'QUALIFICATIONS' | 'COMPLETED';

export interface UserLoginRequest {
  email: string;
  password?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserResponse {
  id: string;
  email: string;
  role: Role;
  organizationId: string;
  organizationName?: string;
  verified: boolean;
  approved: boolean;
  username?: string;
  name?: string;
  surname?: string;
  supplierCategory?: SupplierCategory;
  website?: string;
  phoneNumber?: string;
  location?: string;
  focusAreas?: string[];
  capabilityIds?: string[];
  onboardingCompleted?: boolean;
  onboardingStep?: OnboardingStep | null;
}

export interface FirmRegisterRequest {
  organizationName: string;
  username: string;
  email: string;
  password: string;
  phoneNumber: string;
  location?: string;
  website?: string;
}

export interface SupplierRegisterRequest extends FirmRegisterRequest {
  supplierCategory: SupplierCategory;
  focusAreas: string[];
}

export interface OnboardingStatusResponse {
  role: Role;
  approved: boolean;
  onboardingCompleted: boolean;
  onboardingStep: OnboardingStep | null;
  completionPercentage: number;
}

export interface FirmOnboardingBasicRequest {
  organizationName: string;
  description?: string;
  activityArea?: string;
  location?: string;
  website?: string;
  phoneNumber?: string;
  contactEmail?: string;
}

export interface FirmOnboardingDetailsRequest {
  focusAreas: string[];
  certifications?: string[];
  referenceProjects?: string[];
}

export interface SupplierOnboardingBasicRequest {
  organizationName: string;
  supplierCategory: SupplierCategory;
  description?: string;
  location?: string;
  website?: string;
  phoneNumber?: string;
  contactEmail?: string;
  teamSize?: number;
}

export interface SupplierOnboardingCapabilitiesRequest {
  focusAreas: string[];
  expertiseAreas?: string[];
  hardwareCapabilities?: string[];
  softwareCapabilities?: string[];
  rdCapabilities?: string[];
  productionInfrastructure?: string[];
  testInfrastructure?: string[];
}

export interface SupplierOnboardingQualificationsRequest {
  certifications?: string[];
  referenceProjects?: string[];
  serviceAreas?: string[];
}

const AuthService = {
  login: async (request: UserLoginRequest) => {
    const response = await api.post<RestResponse<AuthResponse>>('/auth/login', request);
    return response.data;
  },
  
  logout: async () => {
    const response = await api.post<RestResponse<string>>('/auth/logout');
    return response.data;
  },

  registerFirm: async (request: FirmRegisterRequest) => {
    const response = await api.post<RestResponse<UserResponse>>('/auth/register', request);
    return response.data;
  },

  registerSupplier: async (request: SupplierRegisterRequest) => {
    const response = await api.post<RestResponse<UserResponse>>('/auth/register/supplier', request);
    return response.data;
  },

  getProfile: async () => {
    const response = await api.get<RestResponse<UserResponse>>('/user/profile');
    return response.data;
  },

  getOnboardingStatus: async () => {
    const response = await api.get<RestResponse<OnboardingStatusResponse>>('/user/onboarding');
    return response.data;
  },

  completeFirmBasicOnboarding: async (request: FirmOnboardingBasicRequest) => {
    const response = await api.put<RestResponse<string>>('/user/onboarding/firm/basic', request);
    return response.data;
  },

  completeFirmDetailsOnboarding: async (request: FirmOnboardingDetailsRequest) => {
    const response = await api.put<RestResponse<string>>('/user/onboarding/firm/details', request);
    return response.data;
  },

  completeSupplierBasicOnboarding: async (request: SupplierOnboardingBasicRequest) => {
    const response = await api.put<RestResponse<string>>('/user/onboarding/supplier/basic', request);
    return response.data;
  },

  completeSupplierCapabilitiesOnboarding: async (request: SupplierOnboardingCapabilitiesRequest) => {
    const response = await api.put<RestResponse<string>>('/user/onboarding/supplier/capabilities', request);
    return response.data;
  },

  completeSupplierQualificationsOnboarding: async (request: SupplierOnboardingQualificationsRequest) => {
    const response = await api.put<RestResponse<string>>('/user/onboarding/supplier/qualifications', request);
    return response.data;
  },

  // Admin Methods
  adminGetAllUsers: async () => {
    const response = await api.get<RestResponse<UserResponse[]>>('/admin/users');
    return response.data;
  },

  adminUpdateApproval: async (userId: string, approved: boolean) => {
    const response = await api.patch<RestResponse<string>>(`/admin/users/${userId}/approval`, { approved });
    return response.data;
  },

  updateFirmProfile: async (request: any) => {
    const response = await api.put<RestResponse<UserResponse>>('/user/profile/firm', request);
    return response.data;
  },

  updateSupplierProfile: async (request: any) => {
    const response = await api.put<RestResponse<UserResponse>>('/user/profile/supplier', request);
    return response.data;
  },

  changePassword: async (request: any) => {
    const response = await api.post<RestResponse<string>>('/user/change-password', request);
    return response.data;
  }
};

export default AuthService;
