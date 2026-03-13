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

export const productAPI = {
  // Tạo sản phẩm mới
  createProduct: async (productData) => {
    const response = await api.post('/products', productData);
    return response.data;
  },

  // Cập nhật sản phẩm
  updateProduct: async (id, productData) => {
    const response = await api.put(`/products/${id}`, productData);
    return response.data;
  },

  // Xóa sản phẩm
  deleteProduct: async (id) => {
    const response = await api.delete(`/products/${id}`);
    return response.data;
  },

  // Lấy tất cả sản phẩm
  getAllProducts: async () => {
    const response = await api.get('/products');
    return response.data;
  },

  // Lấy sản phẩm theo ID
  getProductById: async (id) => {
    const response = await api.get(`/products/${id}`);
    return response.data;
  },

  getPublicReviewsByProduct: async (productId) => {
    const response = await api.get(`/products/${productId}/reviews`);
    return response.data;
  },

  // Tìm kiếm sản phẩm
  searchProducts: async (query) => {
    const response = await api.get('/products/search', { params: { query } });
    return response.data;
  },

  // Tổng quan tồn kho cho quản trị cấp vĩ mô
  getInventorySummary: async (lowStockThreshold = 10) => {
    const response = await api.get('/products/inventory/summary', {
      params: { lowStockThreshold },
    });
    return response.data;
  },

  // Inventory APIs cho employee/admin vận hành kho
  getEmployeeInventory: async ({ q, brandId, categoryId, visibility } = {}) => {
    const response = await api.get('/products/employee', {
      params: { q, brandId, categoryId, visibility },
    });
    return response.data;
  },

  updateStockByEmployee: async (productId, stockQuantity, reason) => {
    const response = await api.patch(`/products/employee/${productId}/stock`, {
      stockQuantity,
      reason,
    });
    return response.data;
  },

  updateExpiryByEmployee: async (productId, expiryDate, note) => {
    const response = await api.patch(`/products/employee/${productId}/expiry`, {
      expiryDate,
      note,
    });
    return response.data;
  },

  updateVisibilityByEmployee: async (productId, visible, reason) => {
    const response = await api.patch(`/products/employee/${productId}/visibility`, {
      visible,
      reason,
    });
    return response.data;
  },

  getEmployeeReviews: async ({ status, q } = {}) => {
    const response = await api.get('/products/employee/reviews', {
      params: { status, q },
    });
    return response.data;
  },

  moderateReviewByEmployee: async (reviewId, action, reason) => {
    const response = await api.patch(`/products/employee/reviews/${reviewId}/moderation`, {
      action,
      reason,
    });
    return response.data;
  },

  // Voucher APIs
  getAllVouchers: async () => {
    const response = await api.get('/vouchers');
    return response.data;
  },

  createVoucher: async (voucherData) => {
    const response = await api.post('/vouchers', voucherData);
    return response.data;
  },

  updateVoucherStatus: async (id, active) => {
    const response = await api.patch(`/vouchers/${id}/status`, { active });
    return response.data;
  },

  deleteVoucher: async (id) => {
    const response = await api.delete(`/vouchers/${id}`);
    return response.data;
  },
};

export default api;
