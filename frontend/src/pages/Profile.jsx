import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import { User, Mail, Shield, Calendar, CheckSquare, ArrowRight } from 'lucide-react';

const Profile = () => {
  const { user } = useAuth();
  const [assignedTasks, setAssignedTasks] = useState([]);
  const [loadingTasks, setLoadingTasks] = useState(true);

  useEffect(() => {
    const fetchUserTasks = async () => {
      if (!user?.id) return;
      try {
        setLoadingTasks(true);
        const res = await axiosClient.get(`/tasks?assignee=${user.id}&size=50`);
        setAssignedTasks(res.data.content || []);
      } catch (err) {
        console.error('Failed to load user tasks:', err);
      } finally {
        setLoadingTasks(false);
      }
    };

    fetchUserTasks();
  }, [user]);

  if (!user) return null;

  const completedCount = assignedTasks.filter((t) => t.status === 'COMPLETED').length;
  const inProgressCount = assignedTasks.filter((t) => t.status === 'IN_PROGRESS').length;

  return (
    <div className="page-body">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--gray-900)' }}>
          User Profile
        </h1>
        <p style={{ color: 'var(--gray-500)', fontSize: '0.95rem' }}>
          Account credentials, role permissions, and assigned work items.
        </p>
      </div>

      <div className="grid-3" style={{ gridTemplateColumns: '1fr 2fr', alignItems: 'start' }}>
        {/* Profile Details Card */}
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Account Details</h2>
          </div>

          <div className="card-body">
            <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
              <div
                style={{
                  width: '72px',
                  height: '72px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--primary-light)',
                  color: 'var(--primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '2rem',
                  fontWeight: 700,
                  margin: '0 auto 1rem'
                }}
              >
                {user.name.charAt(0).toUpperCase()}
              </div>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--gray-900)' }}>{user.name}</h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--gray-500)' }}>{user.email}</p>
              <div style={{ marginTop: '0.75rem' }}>
                <span className={`badge badge-${user.role.toLowerCase()}`}>
                  {user.role} Clearances
                </span>
              </div>
            </div>

            <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', fontSize: '0.875rem' }}>
                <Shield size={16} color="var(--primary)" />
                <span style={{ color: 'var(--gray-500)' }}>Role:</span>
                <span style={{ fontWeight: 600, color: 'var(--gray-800)' }}>{user.role}</span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', fontSize: '0.875rem' }}>
                <Mail size={16} color="var(--primary)" />
                <span style={{ color: 'var(--gray-500)' }}>Email:</span>
                <span style={{ color: 'var(--gray-800)' }}>{user.email}</span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', fontSize: '0.875rem' }}>
                <Calendar size={16} color="var(--primary)" />
                <span style={{ color: 'var(--gray-500)' }}>Member Since:</span>
                <span style={{ color: 'var(--gray-800)' }}>
                  {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'Active'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Assigned Tasks Card */}
        <div className="card">
          <div className="card-header">
            <div>
              <h2 className="card-title">My Assigned Tasks ({assignedTasks.length})</h2>
              <div style={{ fontSize: '0.8rem', color: 'var(--gray-500)', marginTop: '0.2rem' }}>
                {completedCount} Completed • {inProgressCount} In Progress
              </div>
            </div>

            <Link to="/tasks" className="btn btn-secondary btn-sm">
              All Tasks
            </Link>
          </div>

          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Task</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Due Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {loadingTasks ? (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '2rem', color: 'var(--gray-500)' }}>
                      Loading assignments...
                    </td>
                  </tr>
                ) : assignedTasks.length === 0 ? (
                  <tr>
                    <td colSpan="5" style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--gray-500)' }}>
                      You have no tasks currently assigned to you.
                    </td>
                  </tr>
                ) : (
                  assignedTasks.map((t) => (
                    <tr key={t.id}>
                      <td>
                        <Link to={`/tasks/${t.id}`} style={{ fontWeight: 600, color: 'var(--gray-900)' }}>
                          {t.title}
                        </Link>
                        {t.projectName && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>
                            {t.projectName}
                          </div>
                        )}
                      </td>
                      <td><PriorityBadge priority={t.priority} /></td>
                      <td><StatusBadge status={t.status} /></td>
                      <td style={{ fontSize: '0.85rem', color: 'var(--gray-600)' }}>
                        {t.dueDate || '—'}
                      </td>
                      <td>
                        <Link to={`/tasks/${t.id}`} className="btn btn-secondary btn-sm">
                          <span>Open</span>
                          <ArrowRight size={13} />
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
