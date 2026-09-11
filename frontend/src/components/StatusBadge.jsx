import React from 'react';

const StatusBadge = ({ status }) => {
  if (!status) return null;

  const normalized = status.toLowerCase();
  const formatLabel = (s) => s.replace(/_/g, ' ');

  return (
    <span className={`badge badge-${normalized}`}>
      {formatLabel(status)}
    </span>
  );
};

export default StatusBadge;
