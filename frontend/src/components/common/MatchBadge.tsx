import './MatchBadge.css';

interface MatchBadgeProps {
  score: number;
  matchedCount?: number;
  totalRequired?: number;
}

const MatchBadge = ({ score, matchedCount, totalRequired }: MatchBadgeProps) => {
  const isHigh = score >= 75;
  const isMed = score >= 45 && score < 75;
  
  let statusClass = 'low';
  let statusText = 'UYUM';
  if (isHigh) {
    statusClass = 'high';
    statusText = 'YÜKSEK UYUM';
  } else if (isMed) {
    statusClass = 'med';
    statusText = 'ORTA UYUM';
  }

  return (
    <div className={`match-badge match-${statusClass}`}>
      <div className="match-score">
        <span className="match-score-value">%{score}</span>
      </div>
      <div className="match-info-box">
        <div className="match-label">{statusText}</div>
        {totalRequired !== undefined && (
          <div className="match-detail-small">
            {matchedCount}/{totalRequired} Yetkinlik
          </div>
        )}
      </div>
    </div>
  );
};

export default MatchBadge;
