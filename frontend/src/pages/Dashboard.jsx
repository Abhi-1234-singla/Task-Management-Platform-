import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import StatCard from '../components/StatCard';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import {
  FolderKanban,
  CheckCircle2,
  Clock,
  AlertTriangle,
  ListTodo,
  Plus,
  ArrowRight
} from 'lucide-react';

const Dashboard = () => {
  const { user, canManageProjects, canManageTasks } = useAuth();
  const [metrics, setMetrics] = useState({
    totalProjects: 0,
    totalTasks: 0,
    completedTasks: 0,
    inProgressTasks: 0,
    pendingTasks: 0,
  });
  const [recentTasks, setRecentTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const [metricsRes, tasksRes, projectsRes] = await Promise.all([
          axiosClient.get('/dashboard/metrics'),
          axiosClient.get('/tasks?page=0&size=5&sortBy=createdAt&sortDir=desc'),
          axiosClient.get('/projects'),
        ]);

        setMetrics(metricsRes.data);
        setRecentTasks(tasksRes.data.content || []);
        setProjects(projectsRes.data || []);
      } catch (err) {
        console.error('Failed to load dashboard data:', err);
        setError('Unable to load dashboard metrics. Please check server connection.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--gray-500)' }}>
        Loading dashboard insights...
      </div>
    );
  }

  return (
    <div className="page-body">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--gray-900)' }}>
            Welcome back, {user?.name}!
          </h1>
          <p style={{ color: 'var(--gray-500)', fontSize: '0.95rem' }}>
            Here is your real-time platform overview and active project workload.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          {canManageProjects() && (
            <Link to="/projects" className="btn btn-secondary">
              <FolderKanban size={16} />
              <span>Projects</span>
            </Link>
          )}
          {canManageTasks() && (
            <Link to="/tasks" className="btn btn-primary">
              <Plus size={16} />
              <span>New Task</span>
            </Link>
          )}
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {/* Metrics Row */}
      <div className="grid-5" style={{ marginBottom: '2rem' }}>
        <StatCard
          title="Total Projects"
          value={metrics.totalProjects}
          icon={FolderKanban}
          color="#0176D3"
          bgColor="#EBF4FC"
        />
        <StatCard
          title="Total Tasks"
          value={metrics.totalTasks}
          icon={ListTodo}
          color="#6B7280"
          bgColor="#F3F4F6"
        />
        <StatCard
          title="Completed"
          value={metrics.completedTasks}
          icon={CheckCircle2}
          color="#2E844A"
          bgColor="#E8F5E9"
        />
        <StatCard
          title="In Progress"
          value={metrics.inProgressTasks}
          icon={Clock}
          color="#0284C7"
          bgColor="#E0F2FE"
        />
        <StatCard
          title="Pending / Blocked"
          value={metrics.pendingTasks}
          icon={AlertTriangle}
          color="#DD7A01"
          bgColor="#FEF7E6"
        />
      </div>

      {/* Main Grid: Recent Tasks & Active Projects */}
      <div className="grid-2">
        {/* Recent Tasks */}
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Recent Tasks</h2>
            <Link to="/tasks" style={{ fontSize: '0.85rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <span>View all</span>
              <ArrowRight size={14} />
            </Link>
          </div>

          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Task Title</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Due Date</th>
                </tr>
              </thead>
              <tbody>
                {recentTasks.length === 0 ? (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', color: 'var(--gray-500)', padding: '2rem' }}>
                      No tasks found.
                    </td>
                  </tr>
                ) : (
                  recentTasks.map((task) => (
                    <tr key={task.id}>
                      <td>
                        <Link to={`/tasks/${task.id}`} style={{ fontWeight: 600, color: 'var(--gray-900)' }}>
                          {task.title}
                        </Link>
                        {task.projectName && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>
                            {task.projectName}
                          </div>
                        )}
                      </td>
                      <td><PriorityBadge priority={task.priority} /></td>
                      <td><StatusBadge status={task.status} /></td>
                      <td style={{ fontSize: '0.85rem', color: 'var(--gray-600)' }}>
                        {task.dueDate || 'No date'}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Active Projects Summary */}
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Workspace Projects</h2>
            <Link to="/projects" style={{ fontSize: '0.85rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <span>Explore</span>
              <ArrowRight size={14} />
            </Link>
          </div>

          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {projects.length === 0 ? (
              <div style={{ textAlign: 'center', color: 'var(--gray-500)', padding: '2rem' }}>
                No active projects.
              </div>
            ) : (
              projects.slice(0, 4).map((project) => {
                const percent = project.totalTasks > 0
                  ? Math.round((project.completedTasks / project.totalTasks) * 100)
                  : 0;

                return (
                  <div
                    key={project.id}
                    style={{
                      padding: '1rem',
                      borderRadius: 'var(--radius)',
                      border: '1px solid var(--border-color)',
                      backgroundColor: 'var(--gray-50)'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                      <div>
                        <Link
                          to={`/projects/${project.id}`}
                          style={{ fontWeight: 600, color: 'var(--gray-900)', fontSize: '0.95rem' }}
                        >
                          {project.name}
                        </Link>
                        <div style={{ fontSize: '0.8rem', color: 'var(--gray-500)' }}>
                          Owner: {project.createdBy?.name || 'Workspace'}
                        </div>
                      </div>
                      <StatusBadge status={project.status} />
                    </div>

                    {/* Progress Bar */}
                    <div style={{ marginTop: '0.75rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--gray-600)', marginBottom: '0.25rem' }}>
                        <span>Completion Rate</span>
                        <span>{percent}% ({project.completedTasks}/{project.totalTasks})</span>
                      </div>
                      <div style={{ height: '6px', background: 'var(--gray-200)', borderRadius: '3px', overflow: 'hidden' }}>
                        <div
                          style={{
                            width: `${percent}%`,
                            height: '100%',
                            background: percent === 100 ? 'var(--success)' : 'var(--primary)',
                            transition: 'width 0.3s ease'
                          }}
                        />
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
