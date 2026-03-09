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

export const systemSettingsApi = {
  getSettings: async () => {
    const response = await api.get('/system-settings');
    return response.data;
  },

  updateGeneralSettings: async (payload) => {
    const response = await api.patch('/system-settings/general', payload);
    return response.data;
  },

  updateSecuritySettings: async (payload) => {
    const response = await api.patch('/system-settings/security', payload);
    return response.data;
  },

  updateBackupSettings: async (payload) => {
    const response = await api.patch('/system-settings/backup', payload);
    return response.data;
  },

  getBackupHistory: async () => {
    const response = await api.get('/system-settings/backups/history');
    return response.data;
  },

  runBackupNow: async () => {
    const response = await api.post('/system-settings/backups/run');
    return response.data;
  },
};

export default api;
