import type { ReactNode } from 'react';
import './GlassCard.css';

interface GlassCardProps {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
  hoverable?: boolean;
}

const GlassCard = ({ children, className = '', onClick, hoverable = false }: GlassCardProps) => {
  return (
    <div 
      className={`glass-card ${hoverable ? 'hoverable' : ''} ${className}`}
      onClick={onClick}
    >
      <div className="glass-card-inner">
        {children}
      </div>
    </div>
  );
};

export default GlassCard;
