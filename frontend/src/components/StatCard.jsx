import React from 'react';

const StatCard = ({ title, value, icon: Icon, color, bgColor }) => {
  return (
    <div className="stat-card">
      <div className="stat-icon" style={{ backgroundColor: bgColor || 'var(--primary-light)', color: color || 'var(--primary)' }}>
        {Icon && <Icon size={24} />}
      </div>
      <div>
        <div className="stat-value">{value !== undefined ? value : 0}</div>
        <div className="stat-label">{title}</div>
      </div>
    </div>
  );
};

export default StatCard;
