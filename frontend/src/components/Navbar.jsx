import React from 'react';
import { useAuth } from '../context/AuthContext';
import { LogOut, CheckSquare, User as UserIcon } from 'lucide-react';
import { Link } from 'react-router-dom';

const Navbar = () => {
  const { user, logout } = useAuth();

  return (
    <header className="top-navbar">
      <div className="brand">
        <CheckSquare size={26} color="var(--primary)" />
        <span>TaskFlow Platform</span>
      </div>

      <div className="user-profile-menu">
        {user && (
          <>
            <Link
              to="/profile"
              style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--gray-700)', textDecoration: 'none' }}
            >
              <div
                style={{
                  width: '34px',
                  height: '34px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--primary-light)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--primary)',
                  fontWeight: 600,
                  fontSize: '0.875rem'
                }}
              >
                {user.name ? user.name.charAt(0).toUpperCase() : 'U'}
              </div>
              <div>
                <div style={{ fontSize: '0.85rem', fontWeight: 600, lineHeight: 1.2 }}>{user.name}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>{user.email}</div>
              </div>
            </Link>

            <span className={`badge badge-${user.role.toLowerCase()}`}>
              {user.role}
            </span>

            <button
              onClick={logout}
              className="btn btn-secondary btn-sm"
              title="Logout"
              style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}
            >
              <LogOut size={16} />
              <span>Logout</span>
            </button>
          </>
        )}
      </div>
    </header>
  );
};

export default Navbar;
