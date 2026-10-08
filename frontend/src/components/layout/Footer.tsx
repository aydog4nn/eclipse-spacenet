import './Footer.css';

const Footer = () => {
  return (
    <footer className="site-footer">
      <div className="container site-footer-inner">
        <div className="site-footer-brand">
          <div className="site-footer-title-row">
            <span className="text-gradient site-footer-logo">SpaceNet</span>
            <span className="site-footer-tagline">UZAY VE İLERİ TEKNOLOJİ EKOSİSTEMİ İÇİN DİJİTAL İŞ BİRLİĞİ ALTYAPISI</span>
          </div>
          <p className="site-footer-copy">
            © {new Date().getFullYear()} SpaceNet. Tüm hakları saklıdır.
          </p>
        </div>
        <div className="site-footer-links">
          <a href="#">Gizlilik Politikası</a>
          <a href="#">Kullanım Şartları</a>
          <a href="#">İletişim</a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
