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

export const orderAPI = {
  getAllOrders: async ({ status, q } = {}) => {
    const response = await api.get('/orders/admin', { params: { status, q } });
    return response.data;
  },

  updateOrderStatus: async (orderId, status, reason) => {
    const response = await api.patch(`/orders/admin/${orderId}/status`, { status, reason });
    return response.data;
  },

  getComplaints: async ({ decision, q } = {}) => {
    const response = await api.get('/orders/admin/complaints', { params: { decision, q } });
    return response.data;
  },

  createComplaint: async (orderId, payload) => {
    const response = await api.post(`/orders/admin/${orderId}/complaints`, payload);
    return response.data;
  },

  resolveComplaint: async (complaintId, payload) => {
    const response = await api.patch(`/orders/admin/complaints/${complaintId}/decision`, payload);
    return response.data;
  },

  getEmployeeOrders: async ({ status, q } = {}) => {
    const response = await api.get('/orders/employee', { params: { status, q } });
    return response.data;
  },

  advanceEmployeeOrderStatus: async (orderId) => {
    const response = await api.patch(`/orders/employee/${orderId}/advance`);
    return response.data;
  },

  cancelOrderByEmployee: async (orderId, reason) => {
    const response = await api.patch(`/orders/employee/${orderId}/cancel`, { reason });
    return response.data;
  },
};

export default api;
