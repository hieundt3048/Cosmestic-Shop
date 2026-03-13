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

export const userAPI = {
  getCustomers: async () => {
    const response = await api.get('/users/customers');
    return response.data;
  },

  getCustomerPurchaseHistory: async (id) => {
    const response = await api.get(`/users/customers/${id}/orders`);
    return response.data;
  },

  getEmployees: async () => {
    const response = await api.get('/users/employees');
    return response.data;
  },

  createEmployee: async (employeeData) => {
    const response = await api.post('/users/employees', employeeData);
    return response.data;
  },

  getAllUsers: async () => {
    const response = await api.get('/users');
    return response.data;
  },

  getUserById: async (id) => {
    const response = await api.get(`/users/${id}`);
    return response.data;
  },

  deleteUser: async (id) => {
    const response = await api.delete(`/users/${id}`);
    return response.data;
  },

  updateUserRole: async (id, role) => {
    const response = await api.patch(`/users/${id}/role`, { role });
    return response.data;
  },

  updateUserLockStatus: async (id, accountLocked) => {
    const response = await api.patch(`/users/${id}/lock`, { accountLocked });
    return response.data;
  },

  updateUserStatus: async (id, status, reason) => {
    const response = await api.patch(`/users/${id}/status`, { status, reason });
    return response.data;
  },

  updateMyProfile: async ({ fullName, phone }) => {
    const response = await api.patch('/users/me/profile', { fullName, phone });
    return response.data;
  },

  changeMyPassword: async ({ currentPassword, newPassword, confirmPassword }) => {
    const response = await api.patch('/users/me/password', {
      currentPassword,
      newPassword,
      confirmPassword,
    });
    return response.data;
  },
};

export default api;
