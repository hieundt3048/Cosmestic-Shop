import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const productAPI = {
  // Tạo sản phẩm mới
  createProduct: async (productData) => {
    const response = await api.post('/products/create_product', productData);
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

  // Tìm kiếm sản phẩm
  searchProducts: async (query) => {
    const response = await api.get('/products/search', { params: { query } });
    return response.data;
  },
};

export default api;
