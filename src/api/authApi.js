import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth';

// Cấu hình axios instance
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor để tự động thêm token vào header
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// API đăng ký
export const register = async (userData) => {
  try {
    const response = await api.post('/register', userData);
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || 'Đăng ký thất bại';
  }
};

// API đăng nhập
export const login = async (credentials) => {
  try {
    const response = await api.post('/login', credentials);
    if (response.data.token) {
      // Lưu token và thông tin user vào localStorage
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || 'Đăng nhập thất bại';
  }
};

// API đăng xuất
export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

// Lấy thông tin user hiện tại
export const getCurrentUser = () => {
  const userStr = localStorage.getItem('user');
  return userStr ? JSON.parse(userStr) : null;
};

// Kiểm tra đã đăng nhập chưa
export const isAuthenticated = () => {
  return !!localStorage.getItem('token');
};

export default api;
