import GlassCard from '../components/common/GlassCard';
import './About.css';

const About = () => {
  return (
    <div className="about-page container">
      <section className="about-hero">
        <h2 className="section-subtitle text-gradient">SPACENET</h2>
        <h1 className="section-title">Uzay ekosistemi için<br/><span className="text-primary">tek temas noktası</span></h1>
        <p className="section-desc">
          SpaceNet; ihtiyaç sahibi kurumları, üretim kabiliyeti yüksek tedarikçileri ve uzman ekipleri ortak bir dijital zeminde buluşturan iş birliği altyapısıdır.
        </p>
      </section>

      <div className="about-grid">
        <GlassCard className="about-item">
          <div className="about-icon">🚀</div>
          <h3>Amacımız</h3>
          <p>Doğru ihtiyacın doğru yetkinlikle eşleşmesini sağlayarak proje başlangıç süresini kısaltmak ve iş birliği kalitesini artırmak.</p>
        </GlassCard>

        <GlassCard className="about-item">
          <div className="about-icon">🛰️</div>
          <h3>Vizyonumuz</h3>
          <p>Uzay ve ileri teknoloji alanında çalışan kurumların görünürlüğünü artıran, güvenilir ve ölçülebilir bir iş geliştirme ağı oluşturmak.</p>
        </GlassCard>

        <GlassCard className="about-item">
          <div className="about-icon">🌌</div>
          <h3>Ekosistem</h3>
          <p>KOBİ’lerden üreticilere, AR-GE ekiplerinden test altyapısı sağlayıcılarına kadar tüm paydaşların aynı veri diliyle görünür olmasını sağlıyoruz.</p>
        </GlassCard>
      </div>

      <section className="heritage-section">
        <div className="glass heritage-content">
          <h3>Geleceğe Miras</h3>
          <p>
            SpaceNet yalnızca bir listeleme ekranı değildir. Açılan her ilan, tanımlanan her yetkinlik ve kurulan her bağlantı; gelecekteki uzay projeleri için yaşayan bir iş birliği hafızası oluşturur.
          </p>
        </div>
      </section>
    </div>
  );
};

export default About;
