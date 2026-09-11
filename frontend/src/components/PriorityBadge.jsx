import React from 'react';

const PriorityBadge = ({ priority }) => {
  if (!priority) return null;

  const normalized = priority.toLowerCase();

  return (
    <span className={`badge badge-${normalized}`}>
      {priority}
    </span>
  );
};

export default PriorityBadge;
