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

export const reportAPI = {
  getRevenueStatistics: async (range = 'month') => {
    const response = await api.get('/reports/revenue', { params: { range } });
    return response.data;
  },

  getOrderKpis: async (range = 'month') => {
    const response = await api.get('/reports/order-kpis', { params: { range } });
    return response.data;
  },

  getTopProducts: async (range = 'month', limit = 5) => {
    const response = await api.get('/reports/top-products', { params: { range, limit } });
    return response.data;
  },

  getActiveUsers: async () => {
    const response = await api.get('/reports/active-users');
    return response.data;
  },

  pingActivity: async () => {
    const response = await api.post('/traffic/ping');
    return response.data;
  },
};

export default api;
