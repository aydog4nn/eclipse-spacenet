import type { Project, Organization } from '../types';

export interface MatchResult {
  score: number;
  matchedCapabilities: string[];
  matchedCertifications: string[];
  missingCapabilities: string[];
}

export const calculateMatch = (project: Project, supplier: Organization): MatchResult => {
  let score = 0;
  
  // Capability Match
  const reqCaps = (project.requiredCapabilities || []).map(c => typeof c === 'string' ? c : c.id);
  const supCaps = supplier.capabilities || [];
  
  const matchedCaps = reqCaps.filter(cap => supCaps.includes(cap));
  const missingCaps = reqCaps.filter(cap => !supCaps.includes(cap));
  
  if (reqCaps.length > 0) {
    score += (matchedCaps.length / reqCaps.length) * 60; // 60% weight
  }

  // Certification Match
  const reqCerts = project.certificationRequirements || [];
  const supCerts = supplier.certifications || [];
  
  const matchedCerts = reqCerts.filter(cert => supCerts.includes(cert));
  
  if (reqCerts.length > 0) {
    score += (matchedCerts.length / reqCerts.length) * 20; // 20% weight
  }

  // Preferred Category Match
  if (project.preferredSupplierCategory && project.preferredSupplierCategory === supplier.supplierCategory) {
    score += 20; // 20% weight
  }

  return {
    score: Math.round(score),
    matchedCapabilities: matchedCaps,
    matchedCertifications: matchedCerts,
    missingCapabilities: missingCaps
  };
};

// Compatibility export
export const calculateMatchScore = (project: Project, supplier: Organization): number => {
  return calculateMatch(project, supplier).score;
};

// Compatibility export
export const getRecommendedSuppliersForProject = (project: Project, suppliers: Organization[]): (Organization & { matchScore: number })[] => {
  return suppliers
    .map(sup => ({
      ...sup,
      matchScore: calculateMatchScore(project, sup)
    }))
    .sort((a, b) => b.matchScore - a.matchScore);
};
