import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import {
  CheckSquare,
  ArrowLeft,
  Calendar,
  User,
  FolderKanban,
  MessageSquare,
  Send,
  Trash2,
  AlertCircle
} from 'lucide-react';

const TaskDetails = () => {
  const { id } = useParams();
  const { user, canManageTasks, isAdmin } = useAuth();

  const [task, setTask] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(true);
  const [submittingComment, setSubmittingComment] = useState(false);
  const [error, setError] = useState('');

  const fetchTaskAndComments = async () => {
    try {
      setLoading(true);
      const [taskRes, commentsRes] = await Promise.all([
        axiosClient.get(`/tasks/${id}`),
        axiosClient.get(`/tasks/${id}/comments`),
      ]);
      setTask(taskRes.data);
      setComments(commentsRes.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load task details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTaskAndComments();
  }, [id]);

  const handleStatusChange = async (newStatus) => {
    try {
      const res = await axiosClient.patch(`/tasks/${id}/status`, { status: newStatus });
      setTask(res.data);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update status.');
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    setSubmittingComment(true);

    try {
      const res = await axiosClient.post(`/tasks/${id}/comments`, { content: newComment.trim() });
      setComments((prev) => [...prev, res.data]);
      setNewComment('');
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to post comment.');
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('Delete this comment?')) return;

    try {
      await axiosClient.delete(`/comments/${commentId}`);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete comment.');
    }
  };

  if (loading) {
    return <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--gray-500)' }}>Loading task...</div>;
  }

  if (error || !task) {
    return (
      <div className="page-body">
        <div className="alert alert-danger">{error || 'Task not found.'}</div>
        <Link to="/tasks" className="btn btn-secondary">
          <ArrowLeft size={16} />
          <span>Back to Tasks</span>
        </Link>
      </div>
    );
  }

  const canChangeStatus = canManageTasks() || (task.assignedTo && task.assignedTo.id === user?.id);

  return (
    <div className="page-body">
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/tasks" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--gray-600)', fontSize: '0.875rem' }}>
          <ArrowLeft size={16} />
          <span>Back to Tasks</span>
        </Link>
      </div>

      <div className="grid-3" style={{ gridTemplateColumns: '2fr 1fr', alignItems: 'start' }}>
        {/* Left: Task Overview & Comments */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Main Card */}
          <div className="card">
            <div className="card-header">
              <h1 style={{ fontSize: '1.35rem', fontWeight: 700, color: 'var(--gray-900)' }}>
                {task.title}
              </h1>
              <PriorityBadge priority={task.priority} />
            </div>

            <div className="card-body">
              <h3 style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--gray-500)', textTransform: 'uppercase', marginBottom: '0.5rem' }}>
                Description
              </h3>
              <p style={{ fontSize: '0.95rem', color: 'var(--gray-800)', lineHeight: '1.6', whiteSpace: 'pre-wrap' }}>
                {task.description || 'No description provided for this task.'}
              </p>
            </div>
          </div>

          {/* Comments Section */}
          <div className="card">
            <div className="card-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <MessageSquare size={18} color="var(--primary)" />
                <h2 className="card-title" style={{ fontSize: '1.1rem' }}>
                  Collaboration & Comments ({comments.length})
                </h2>
              </div>
            </div>

            <div className="card-body">
              {/* New Comment Input */}
              <form onSubmit={handleAddComment} style={{ marginBottom: '1.5rem' }}>
                <div className="form-group">
                  <textarea
                    className="form-control"
                    rows={3}
                    placeholder="Add a progress update, question, or note..."
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    required
                  />
                </div>
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button
                    type="submit"
                    className="btn btn-primary btn-sm"
                    disabled={submittingComment || !newComment.trim()}
                  >
                    <Send size={14} />
                    <span>{submittingComment ? 'Posting...' : 'Post Comment'}</span>
                  </button>
                </div>
              </form>

              {/* Comments Thread */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {comments.length === 0 ? (
                  <div style={{ textAlign: 'center', color: 'var(--gray-400)', padding: '1.5rem', fontSize: '0.875rem' }}>
                    No comments yet. Be the first to start the discussion!
                  </div>
                ) : (
                  comments.map((comment) => {
                    const isAuthor = user?.id === comment.user?.id;
                    const canDelete = isAuthor || isAdmin();

                    return (
                      <div
                        key={comment.id}
                        style={{
                          padding: '1rem',
                          borderRadius: 'var(--radius)',
                          backgroundColor: 'var(--gray-50)',
                          border: '1px solid var(--border-color)',
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <span style={{ fontWeight: 600, fontSize: '0.875rem', color: 'var(--gray-900)' }}>
                              {comment.user?.name}
                            </span>
                            <span className={`badge badge-${comment.user?.role?.toLowerCase()}`} style={{ fontSize: '0.65rem' }}>
                              {comment.user?.role}
                            </span>
                            <span style={{ fontSize: '0.75rem', color: 'var(--gray-400)' }}>
                              {new Date(comment.createdAt).toLocaleString()}
                            </span>
                          </div>

                          {canDelete && (
                            <button
                              onClick={() => handleDeleteComment(comment.id)}
                              className="btn btn-secondary btn-sm"
                              style={{ color: 'var(--danger)', padding: '0.2rem 0.4rem', border: 'none' }}
                              title="Delete comment"
                            >
                              <Trash2 size={14} />
                            </button>
                          )}
                        </div>

                        <p style={{ fontSize: '0.875rem', color: 'var(--gray-700)', lineHeight: '1.5', whiteSpace: 'pre-wrap' }}>
                          {comment.content}
                        </p>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Right: Task Metadata Sidebar Card */}
        <div className="card">
          <div className="card-header">
            <h3 className="card-title" style={{ fontSize: '0.95rem' }}>Task Details</h3>
          </div>

          <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div>
              <label className="form-label" style={{ fontSize: '0.75rem', textTransform: 'uppercase' }}>
                Workflow Status
              </label>
              {canChangeStatus ? (
                <select
                  className="form-control"
                  value={task.status}
                  onChange={(e) => handleStatusChange(e.target.value)}
                >
                  <option value="TODO">TODO</option>
                  <option value="IN_PROGRESS">IN PROGRESS</option>
                  <option value="COMPLETED">COMPLETED</option>
                  <option value="BLOCKED">BLOCKED</option>
                </select>
              ) : (
                <StatusBadge status={task.status} />
              )}
            </div>

            <div>
              <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--gray-500)', textTransform: 'uppercase', marginBottom: '0.25rem' }}>
                Associated Project
              </div>
              {task.projectId ? (
                <Link
                  to={`/projects/${task.projectId}`}
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontWeight: 600, color: 'var(--primary)' }}
                >
                  <FolderKanban size={16} />
                  <span>{task.projectName}</span>
                </Link>
              ) : (
                <span style={{ color: 'var(--gray-400)' }}>None</span>
              )}
            </div>

            <div>
              <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--gray-500)', textTransform: 'uppercase', marginBottom: '0.25rem' }}>
                Assigned Developer
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <User size={16} color="var(--gray-400)" />
                <span style={{ fontSize: '0.9rem', color: 'var(--gray-800)', fontWeight: 500 }}>
                  {task.assignedTo ? `${task.assignedTo.name} (${task.assignedTo.email})` : 'Unassigned'}
                </span>
              </div>
            </div>

            <div>
              <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--gray-500)', textTransform: 'uppercase', marginBottom: '0.25rem' }}>
                Due Date
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Calendar size={16} color="var(--gray-400)" />
                <span style={{ fontSize: '0.9rem', color: 'var(--gray-800)' }}>
                  {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No deadline set'}
                </span>
              </div>
            </div>

            <div>
              <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--gray-500)', textTransform: 'uppercase', marginBottom: '0.25rem' }}>
                Created By
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--gray-600)' }}>
                {task.createdBy?.name} on {new Date(task.createdAt).toLocaleDateString()}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TaskDetails;
