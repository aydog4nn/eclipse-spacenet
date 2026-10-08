export interface SolarPlanet {
  id: string;
  name: string;
  subtitle: string;
  description: string;
  surfaceType: 'rocky' | 'cloudy' | 'gas';
  distanceAu: number;
  size: number;
  color: string;
  emissive?: string;
  speed: number;
  accent: string;
  stats: string;
  moons?: number;
  hasRings?: boolean;
  hasClouds?: boolean;
  modelPath?: string;
  modelScale?: number;
}

export const solarPlanets: SolarPlanet[] = [
  {
    id: 'mercury',
    name: 'Merkur',
    subtitle: 'Hızlı karar katmanı',
    description: 'Sistemin çevik tarafını temsil eder. İlk eşleşmeler, hızlı keşifler ve erken temaslar burada başlar.',
    surfaceType: 'rocky',
    distanceAu: 0.39,
    size: 0.42,
    color: '#b7a58f',
    speed: 0.58,
    accent: '#f0c59d',
    stats: 'Erken eşleşme sinyalleri'
  },
  {
    id: 'venus',
    name: 'Venüs',
    subtitle: 'İş birliği yoğunluğu',
    description: 'Kurumsal yakınlık ve stratejik ortaklık hissini taşır. Isısı yüksek, ilişkileri kuvvetli bir katman.',
    surfaceType: 'cloudy',
    distanceAu: 0.72,
    size: 0.72,
    color: '#d8a46d',
    speed: 0.42,
    accent: '#ffb36f',
    stats: 'Partner yoğunluğu yüksek',
    hasClouds: true
  },
  {
    id: 'earth',
    name: 'Dünya',
    subtitle: 'Canlı ekosistem merkezi',
    description: 'Firma, tedarikçi ve proje ihtiyaçlarının en dengeli birleştiği merkez katman. Platformun ana yaşam alanı.',
    surfaceType: 'rocky',
    distanceAu: 1,
    size: 0.88,
    color: '#49a5ff',
    emissive: '#163b72',
    speed: 0.31,
    accent: '#7ee0ff',
    stats: 'Ana ağ çekirdeği',
    moons: 1,
    hasClouds: true,
    modelPath: '/models/earth-nasa.glb',
    modelScale: 0.92
  },
  {
    id: 'mars',
    name: 'Mars',
    subtitle: 'Üretim ve prototipleme',
    description: 'Deneysel üretim, prototip ve saha doğrulama kabiliyetlerinin birleştiği daha sert ama net bir alan.',
    surfaceType: 'rocky',
    distanceAu: 1.52,
    size: 0.62,
    color: '#c7654f',
    speed: 0.25,
    accent: '#ff8f73',
    stats: 'Prototip akışı',
    moons: 2,
    modelPath: '/models/mars-nasa.glb',
    modelScale: 0.72
  },
  {
    id: 'saturn',
    name: 'Satürn',
    subtitle: 'Büyük sistem mimarisi',
    description: 'Büyük ölçekli entegrasyon, mimari derinlik ve çok katmanlı sanayi kabiliyetleri bu halkalı yapıda toplanır.',
    surfaceType: 'gas',
    distanceAu: 9.58,
    size: 1.58,
    color: '#d8be9a',
    speed: 0.15,
    accent: '#f1d8b5',
    stats: 'Sistem seviyesi yoğunluk',
    moons: 3,
    hasRings: true,
    hasClouds: true
  }
];
