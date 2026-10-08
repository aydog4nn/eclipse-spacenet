import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight, Building2, Rocket, ShieldCheck } from 'lucide-react';
import GlassCard from '../components/common/GlassCard';
import PasswordField from '../components/common/PasswordField';
import AuthService from '../services/authService';
import { useToast } from '../context/ToastContext';
import type { Role, SupplierCategory } from '../types';
import { extractTurkishPhoneDigits, formatTurkishPhoneForRequest, isValidTurkishPhone } from '../utils/phone';
import './Register.css';

type WizardStep = 'firm-basic' | 'firm-details' | 'supplier-basic' | 'supplier-capabilities' | 'supplier-qualifications' | 'account';

type RegisterFormState = {
  organizationName: string;
  username: string;
  email: string;
  password: string;
  phoneNumber: string;
  location: string;
  website: string;
  supplierCategory: SupplierCategory;
  focusAreas: string;
  description: string;
  activityArea: string;
  contactEmail: string;
  certifications: string;
  referenceProjects: string;
  teamSize: string;
  expertiseAreas: string;
  hardwareCapabilities: string;
  softwareCapabilities: string;
  rdCapabilities: string;
  productionInfrastructure: string;
  testInfrastructure: string;
  serviceAreas: string;
};

const DEFAULT_FORM_STATE: RegisterFormState = {
  organizationName: '',
  username: '',
  email: '',
  password: '',
  phoneNumber: '',
  location: '',
  website: '',
  supplierCategory: 'KOBI',
  focusAreas: '',
  description: '',
  activityArea: '',
  contactEmail: '',
  certifications: '',
  referenceProjects: '',
  teamSize: '',
  expertiseAreas: '',
  hardwareCapabilities: '',
  softwareCapabilities: '',
  rdCapabilities: '',
  productionInfrastructure: '',
  testInfrastructure: '',
  serviceAreas: '',
};

const roleMeta: Record<Role, { title: string; subtitle: string; points: string[]; icon: typeof Rocket }> = {
  TEDARIKCI: {
    title: 'Tedarikçi',
    subtitle: 'Yetkinliklerinizi öne çıkarın ve iş birliklerine başvurun.',
    points: [
      'Uzmanlıklarınızı görünür hale getirin',
      'Uygun proje çağrılarına hızlıca ulaşın',
      'Kurumsal profilinizi tek merkezden yönetin',
    ],
    icon: Rocket,
  },
  FIRMA: {
    title: 'Firma',
    subtitle: 'İhtiyaç açın, doğru tedarikçileri bulun ve süreci yönetin.',
    points: [
      'Teknik ihtiyaçlarınızı net şekilde yayınlayın',
      'Uygun tedarikçileri filtreleyip karşılaştırın',
      'Başvuru ve iş birliği akışını tek panelden yürütün',
    ],
    icon: Building2,
  },
  ADMIN: {
    title: 'Admin',
    subtitle: '',
    points: [],
    icon: ShieldCheck,
  },
};

const stepLabels: Record<WizardStep, string> = {
  'firm-basic': 'Temel Bilgiler',
  'firm-details': 'Detaylar',
  'supplier-basic': 'Temel Bilgiler',
  'supplier-capabilities': 'Kabiliyetler',
  'supplier-qualifications': 'Referanslar',
  account: 'Hesap',
};

const parseList = (value: string) =>
  value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);

const Register = () => {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [role, setRole] = useState<Role>('TEDARIKCI');
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<RegisterFormState>(DEFAULT_FORM_STATE);

  const steps = useMemo<WizardStep[]>(
    () =>
      role === 'FIRMA'
        ? ['firm-basic', 'firm-details', 'account']
        : ['supplier-basic', 'supplier-capabilities', 'supplier-qualifications', 'account'],
    [role]
  );

  const currentStep = steps[currentStepIndex];
  const isLastStep = currentStepIndex === steps.length - 1;
  const currentRoleMeta = roleMeta[role];
  const CurrentRoleIcon = currentRoleMeta.icon;

  useEffect(() => {
    setCurrentStepIndex(0);
    setFormData((prev) => ({
      ...DEFAULT_FORM_STATE,
      email: prev.email,
      password: prev.password,
      username: prev.username,
    }));
  }, [role]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'phoneNumber' ? extractTurkishPhoneDigits(value) : value,
    }));
  };

  const validateCurrentStep = () => {
    if (!isValidTurkishPhone(formData.phoneNumber)) {
      showToast('Lütfen 10 haneli geçerli bir telefon numarası girin.', 'error');
      return false;
    }

    if (currentStep === 'account') {
      if (!formData.organizationName || !formData.username || !formData.email || !formData.password) {
        showToast('Lütfen zorunlu alanları doldurun.', 'error');
        return false;
      }
    }

    if ((currentStep === 'firm-details' || currentStep === 'supplier-capabilities') && !formData.focusAreas.trim()) {
      showToast('Lütfen odak alanlarını girin.', 'error');
      return false;
    }

    if (currentStep === 'supplier-basic' && !formData.organizationName.trim()) {
      showToast('Lütfen kurum adını girin.', 'error');
      return false;
    }

    return true;
  };

  const handleNextStep = () => {
    if (!validateCurrentStep()) {
      return;
    }

    setCurrentStepIndex((prev) => Math.min(prev + 1, steps.length - 1));
  };

  const handlePreviousStep = () => {
    setCurrentStepIndex((prev) => Math.max(prev - 1, 0));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!isLastStep) {
      handleNextStep();
      return;
    }

    if (!validateCurrentStep()) {
      return;
    }

    setLoading(true);

    try {
      if (role === 'FIRMA') {
        await AuthService.registerFirm({
          organizationName: formData.organizationName,
          username: formData.username,
          email: formData.email,
          password: formData.password,
          phoneNumber: formatTurkishPhoneForRequest(formData.phoneNumber),
          location: formData.location || undefined,
          website: formData.website || undefined,
        });
      } else {
        await AuthService.registerSupplier({
          organizationName: formData.organizationName,
          username: formData.username,
          email: formData.email,
          password: formData.password,
          phoneNumber: formatTurkishPhoneForRequest(formData.phoneNumber),
          supplierCategory: formData.supplierCategory,
          location: formData.location || undefined,
          website: formData.website || undefined,
          focusAreas: parseList(formData.focusAreas),
        });
      }

      showToast('Kayıt alındı. Giriş ekranına yönlendiriliyorsunuz.', 'success');
      navigate('/login');
    } catch (error: unknown) {
      const fallbackMessage =
        error instanceof Error
          ? error.message
          : 'Kayıt veya onboarding sırasında bir hata oluştu. Lütfen tekrar deneyin.';

      const responseMessage =
        typeof error === 'object' &&
        error !== null &&
        'response' in error &&
        typeof (error as { response?: { data?: { message?: string } } }).response?.data?.message === 'string'
          ? (error as { response: { data: { message: string } } }).response.data.message
          : fallbackMessage;

      showToast(responseMessage, 'error');
    } finally {
      setLoading(false);
    }
  };

  const renderStepContent = () => {
    if (currentStep === 'firm-basic') {
      return (
        <div className="register-step-grid">
          <div className="form-group register-step-span-2">
            <label>KURUM ADI *</label>
            <input
              name="organizationName"
              value={formData.organizationName}
              onChange={handleChange}
              placeholder="Örn: TUA Uzay Sistemleri"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>KISA TANITIM</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Kurumunuzun ne ürettiğini, hangi iş birliklerine açık olduğunu anlatın."
            />
          </div>
          <div className="form-group">
            <label>FAALİYET ALANI</label>
            <input
              name="activityArea"
              value={formData.activityArea}
              onChange={handleChange}
              placeholder="Örn: Uydu Sistemleri"
            />
          </div>
          <div className="form-group">
            <label>LOKASYON</label>
            <input
              name="location"
              value={formData.location}
              onChange={handleChange}
              placeholder="Örn: Ankara, Türkiye"
            />
          </div>
          <div className="form-group">
            <label>TELEFON *</label>
            <div className="phone-input">
              <span className="phone-prefix">+90</span>
              <input
                name="phoneNumber"
                inputMode="numeric"
                maxLength={10}
                value={formData.phoneNumber}
                onChange={handleChange}
                placeholder="5XX XXX XX XX"
              />
            </div>
          </div>
          <div className="form-group">
            <label>İLETİŞİM E-POSTASI</label>
            <input
              type="email"
              name="contactEmail"
              value={formData.contactEmail}
              onChange={handleChange}
              placeholder="iletisim@kurum.com"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>WEB SİTESİ</label>
            <input
              name="website"
              value={formData.website}
              onChange={handleChange}
              placeholder="https://..."
            />
          </div>
        </div>
      );
    }

    if (currentStep === 'firm-details') {
      return (
        <div className="register-step-grid">
          <div className="form-group register-step-span-2">
            <label>ODAK ALANLARI * (Virgülle ayırın)</label>
            <input
              name="focusAreas"
              value={formData.focusAreas}
              onChange={handleChange}
              placeholder="Örn: Uydu elektroniği, Gömülü yazılım, Mekanik tasarım"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>SERTİFİKALAR</label>
            <input
              name="certifications"
              value={formData.certifications}
              onChange={handleChange}
              placeholder="Örn: ISO 9001, AS9100"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>REFERANS PROJELER</label>
            <textarea
              name="referenceProjects"
              value={formData.referenceProjects}
              onChange={handleChange}
              placeholder="Örn: Haberleşme uydusu alt sistemi, termal analiz altyapısı"
            />
          </div>
        </div>
      );
    }

    if (currentStep === 'supplier-basic') {
      return (
        <div className="register-step-grid">
          <div className="form-group register-step-span-2">
            <label>KURUM ADI *</label>
            <input
              name="organizationName"
              value={formData.organizationName}
              onChange={handleChange}
              placeholder="Örn: Savunma Teknolojileri"
            />
          </div>
          <div className="form-group">
            <label>TEDARİKÇİ KATEGORİSİ *</label>
            <select name="supplierCategory" value={formData.supplierCategory} onChange={handleChange}>
              <option value="KOBI">KOBI</option>
              <option value="STARTUP">Startup</option>
              <option value="URETICI">Üretici</option>
              <option value="ARGE_FIRMASI">AR-GE Firması</option>
              <option value="HIZMET_SAGLAYICI">Hizmet Sağlayıcı</option>
              <option value="YAN_SANAYI">Yan Sanayi</option>
            </select>
          </div>
          <div className="form-group">
            <label>EKİP BÜYÜKLÜĞÜ</label>
            <input
              type="number"
              min="0"
              name="teamSize"
              value={formData.teamSize}
              onChange={handleChange}
              placeholder="Örn: 24"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>KISA TANITIM</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Tedarikçi profilinizi ve uzmanlığınızı özetleyin."
            />
          </div>
          <div className="form-group">
            <label>LOKASYON</label>
            <input
              name="location"
              value={formData.location}
              onChange={handleChange}
              placeholder="Örn: Ankara, Türkiye"
            />
          </div>
          <div className="form-group">
            <label>TELEFON *</label>
            <div className="phone-input">
              <span className="phone-prefix">+90</span>
              <input
                name="phoneNumber"
                inputMode="numeric"
                maxLength={10}
                value={formData.phoneNumber}
                onChange={handleChange}
                placeholder="5XX XXX XX XX"
              />
            </div>
          </div>
          <div className="form-group">
            <label>İLETİŞİM E-POSTASI</label>
            <input
              type="email"
              name="contactEmail"
              value={formData.contactEmail}
              onChange={handleChange}
              placeholder="iletisim@kurum.com"
            />
          </div>
          <div className="form-group">
            <label>WEB SİTESİ</label>
            <input
              name="website"
              value={formData.website}
              onChange={handleChange}
              placeholder="https://..."
            />
          </div>
        </div>
      );
    }

    if (currentStep === 'supplier-capabilities') {
      return (
        <div className="register-step-grid">
          <div className="form-group register-step-span-2">
            <label>ODAK ALANLARI * (Virgülle ayırın)</label>
            <input
              name="focusAreas"
              value={formData.focusAreas}
              onChange={handleChange}
              placeholder="Örn: Aviyonik, RF, termal analiz, üretim"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>UZMANLIK ALANLARI</label>
            <input
              name="expertiseAreas"
              value={formData.expertiseAreas}
              onChange={handleChange}
              placeholder="Örn: FPGA, güç elektroniği, gömülü Linux"
            />
          </div>
          <div className="form-group">
            <label>DONANIM KABİLİYETLERİ</label>
            <textarea
              name="hardwareCapabilities"
              value={formData.hardwareCapabilities}
              onChange={handleChange}
              placeholder="PCB tasarım, kart üretimi, entegrasyon"
            />
          </div>
          <div className="form-group">
            <label>YAZILIM KABİLİYETLERİ</label>
            <textarea
              name="softwareCapabilities"
              value={formData.softwareCapabilities}
              onChange={handleChange}
              placeholder="RTOS, sürücü geliştirme, yer istasyonu yazılımları"
            />
          </div>
          <div className="form-group">
            <label>AR-GE KABİLİYETLERİ</label>
            <textarea
              name="rdCapabilities"
              value={formData.rdCapabilities}
              onChange={handleChange}
              placeholder="Prototipleme, doğrulama, simülasyon"
            />
          </div>
          <div className="form-group">
            <label>ÜRETİM ALTYAPISI</label>
            <textarea
              name="productionInfrastructure"
              value={formData.productionInfrastructure}
              onChange={handleChange}
              placeholder="CNC, temiz oda, montaj hattı"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>TEST ALTYAPISI</label>
            <textarea
              name="testInfrastructure"
              value={formData.testInfrastructure}
              onChange={handleChange}
              placeholder="EMC, çevresel test, titreşim, fonksiyonel test"
            />
          </div>
        </div>
      );
    }

    if (currentStep === 'supplier-qualifications') {
      return (
        <div className="register-step-grid">
          <div className="form-group register-step-span-2">
            <label>SERTİFİKALAR</label>
            <input
              name="certifications"
              value={formData.certifications}
              onChange={handleChange}
              placeholder="Örn: ISO 9001, EN 9100"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>REFERANS PROJELER</label>
            <textarea
              name="referenceProjects"
              value={formData.referenceProjects}
              onChange={handleChange}
              placeholder="Örn: Küp uydu güç kartı, RF modül üretim desteği"
            />
          </div>
          <div className="form-group register-step-span-2">
            <label>HİZMET BÖLGELERİ</label>
            <input
              name="serviceAreas"
              value={formData.serviceAreas}
              onChange={handleChange}
              placeholder="Örn: Ankara, İstanbul, Eskişehir"
            />
          </div>
        </div>
      );
    }

    return (
      <div className="register-step-grid">
        <div className="form-group">
          <label>KULLANICI ADI *</label>
          <input
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="kullaniciadi"
          />
        </div>
        <div className="form-group">
          <label>E-POSTA *</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="kurum@tua.gov.tr"
          />
        </div>
        <div className="form-group register-step-span-2">
          <label>ŞİFRE *</label>
          <PasswordField
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="En az 6 karakter"
          />
        </div>
      </div>
    );
  };

  const progress = ((currentStepIndex + 1) / steps.length) * 100;

  return (
    <div className="register-page container">
      <GlassCard className="register-card">
        <div className="register-layout">
          <section className={`register-showcase register-showcase-${role.toLowerCase()}`}>

            <div className={`role-selector role-${role.toLowerCase()}`}>
              <div className="role-selector-indicator" aria-hidden="true" />
              <button
                type="button"
                className={`role-btn ${role === 'TEDARIKCI' ? 'active' : ''}`}
                onClick={() => setRole('TEDARIKCI')}
              >
                <span className="role-btn-title">TEDARİKÇİ</span>
                <span className="role-btn-copy">Profilinizi vitrine çıkarın</span>
              </button>
              <button
                type="button"
                className={`role-btn ${role === 'FIRMA' ? 'active' : ''}`}
                onClick={() => setRole('FIRMA')}
              >
                <span className="role-btn-title">FİRMA</span>
                <span className="role-btn-copy">İhtiyaç ve ilan akışını başlatın</span>
              </button>
            </div>

            <CurrentRoleIcon size={28} />
            <h2 className="register-title">Ekosisteme daha iyi bir giriş deneyimiyle katılın</h2>
            <p className="register-desc">
              Rolünüzü seçin, bilgilerinizi birkaç adımda tamamlayın ve onboarding akışını tek ekranda bitirin.
            </p>

            <div className="showcase-copy">
              <span className="register-form-kicker">{currentRoleMeta.title} Modu</span>
              <p>{currentRoleMeta.subtitle}</p>
              <div className="showcase-points">
                {currentRoleMeta.points.map((point) => (
                  <div key={point} className="showcase-point">
                    {point}
                  </div>
                ))}
              </div>
            </div>
          </section>

          <section className="register-main">
            <div className="register-section-head">
              <div>
                <div className="register-section-kicker">Adım {currentStepIndex + 1}</div>
                <h3>{stepLabels[currentStep]}</h3>
              </div>
            </div>

            <div
              aria-hidden="true"
              style={{
                height: '10px',
                borderRadius: '999px',
                background: 'rgba(255, 255, 255, 0.08)',
                overflow: 'hidden',
                marginBottom: '1rem',
              }}
            >
              <div className="register-progress-fill" style={{ width: `${progress}%` }} />
            </div>

            <div className="register-stepper" aria-label="Kayıt adımları">
              {steps.map((step, index) => {
                const stateClass =
                  index === currentStepIndex ? 'active' : index < currentStepIndex ? 'done' : '';

                return (
                  <div key={step} className={`register-step-pill ${stateClass}`.trim()}>
                    <span>{index + 1}</span>
                    {stepLabels[step]}
                  </div>
                );
              })}
            </div>

            <div key={`${role}-${currentStep}`} className="register-form-shell">
              <div className="register-form-intro">
                <span className="register-form-kicker">{currentRoleMeta.title} Kaydı</span>
                <p>{currentRoleMeta.subtitle}</p>
              </div>

              <form className="register-form" onSubmit={handleSubmit}>
                {renderStepContent()}

                <div className="register-actions">
                  {currentStepIndex > 0 ? (
                    <button
                      type="button"
                      className="btn btn-secondary register-nav-btn"
                      onClick={handlePreviousStep}
                      disabled={loading}
                    >
                      <ArrowLeft size={18} />
                      Önceki
                    </button>
                  ) : null}

                  <button type="submit" className="btn btn-primary register-submit" disabled={loading}>
                    {loading ? 'KAYDEDİLİYOR...' : isLastStep ? 'KAYDI TAMAMLA' : 'Devam Et'}
                    {!loading && <ArrowRight size={18} />}
                  </button>
                </div>
              </form>
            </div>

            <p className="auth-footer">
              Zaten hesabınız var mı? <Link to="/login">Giriş Yap</Link>
            </p>
          </section>
        </div>
      </GlassCard>
    </div>
  );
};

export default Register;
