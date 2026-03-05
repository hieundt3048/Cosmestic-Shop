import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Category APIs
export const getAllCategories = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/categories`);
    return response.data;
  } catch (error) {
    console.error('Error fetching categories:', error);
    throw error;
  }
};

export const getCategoryById = async (id) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/categories/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching category:', error);
    throw error;
  }
};

export const createCategory = async (categoryData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/categories`, categoryData);
    return response.data;
  } catch (error) {
    console.error('Error creating category:', error);
    throw error;
  }
};

export const updateCategory = async (id, categoryData) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/categories/${id}`, categoryData);
    return response.data;
  } catch (error) {
    console.error('Error updating category:', error);
    throw error;
  }
};

export const deleteCategory = async (id) => {
  try {
    await axios.delete(`${API_BASE_URL}/categories/${id}`);
  } catch (error) {
    console.error('Error deleting category:', error);
    throw error;
  }
};
