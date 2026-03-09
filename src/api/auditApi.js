import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const auditAPI = {
  getAuditLogs: async (role, q) => {
    const params = {};
    if (role && role !== 'ALL') {
      params.role = role;
    }
    if (q && q.trim()) {
      params.q = q.trim();
    }
    const response = await api.get('/audit-logs', { params });
    return response.data;
  },
};
