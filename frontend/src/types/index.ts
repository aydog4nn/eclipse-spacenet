export type Role = 'FIRMA' | 'TEDARIKCI' | 'ADMIN';
export type SupplierCategory = 'KOBI' | 'STARTUP' | 'URETICI' | 'HIZMET_SAGLAYICI' | 'ARGE_FIRMASI' | 'YAN_SANAYI';
export type ProjectType = 'TEDARIKCI_ARANIYOR' | 'ARGE_PARTNERI' | 'PROTOTIP_GELISTIRME' | 'URETIM_ORTAGI' | 'TEST_ALTYAPISI';
export type ProjectStatus = 'AKTIF' | 'PASIF' | 'KAPALI' | 'TASLAK';
export type ApplicationStatus = 
  | 'GONDERILDI' 
  | 'INCELENIYOR' 
  | 'ON_DEGERLENDIRME' 
  | 'GORUSME_ASAMASINDA' 
  | 'KABUL_EDILDI' 
  | 'REDDEDILDI' 
  | 'GERI_CEKILDI';

export type CollaborationType = 
  | 'TEDARIKCI_ARIYORUM' 
  | 'ARGE_PARTNERI_ARIYORUM' 
  | 'PROTOTIP_DESTEGI_ARIYORUM' 
  | 'URETIM_ORTAGI_ARIYORUM' 
  | 'TEST_ALTYAPISI_ARIYORUM';

export type ConfidentialityLevel = 'DUSUK' | 'ORTA' | 'YUKSEK';

export type CapabilityGroup = 'DONANIM' | 'YAZILIM' | 'ARGE_TEST_URETIM';

export interface User {
  id: string;
  email: string;
  role: Role;
  organizationId: string;
  organizationName?: string;
  username?: string;
  phoneNumber?: string;
  website?: string;
  location?: string;
  verified: boolean;
  approved: boolean;
}

export interface Organization {
  id: string;
  name: string;
  role: Role;
  supplierCategory?: SupplierCategory;
  description: string;
  logoUrl?: string;
  activityArea?: string;
  fieldsOfActivity: string[]; // For firm
  specialties: string[]; // For supplier
  capabilities: string[]; // e.g., ["PCB Tasarımı", "Gömülü Yazılım"]
  certifications: string[];
  references: string[];
  location: string;
  website: string;
  phoneNumber?: string;
  contactEmail?: string;
  verified: boolean;
  approved?: boolean;
  visible?: boolean;
  completionPercentage?: number;
  matchScore?: number; // Calculated dynamic value
  matchedReasons?: string[];
  teamSize?: number;
}

export interface Project {
  id: string;
  organizationId: string; // The firm that created it
  organizationName?: string;
  title: string;
  description: string;
  projectType: ProjectType;
  collaborationType: CollaborationType;
  requiredCapabilities: Capability[]; // Backend returns full objects
  preferredSupplierCategory?: SupplierCategory;
  locationPreference?: string;
  durationText?: string;
  budgetRange?: string;
  certificationRequirements: string[];
  applicationDeadline: string;
  confidentialityLevel: ConfidentialityLevel;
  status: ProjectStatus;
  createdAt: string;
}

export interface Application {
  id: string;
  projectId: string;
  projectTitle?: string;
  supplierOrganizationId: string;
  supplierOrganizationName?: string;
  shortDescription: string;
  whyFit: string;
  documentUrl?: string;
  referenceProjectNote?: string;
  status: ApplicationStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface Capability {
  id: string;
  name: string;
  capabilityGroup: CapabilityGroup;
}

export interface RestResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
