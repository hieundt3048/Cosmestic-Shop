import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Brand APIs
export const getAllBrands = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/brands`);
    return response.data;
  } catch (error) {
    console.error('Error fetching brands:', error);
    throw error;
  }
};

export const getBrandById = async (id) => {
  try {
    const response = await axios.get(`${API_BASE_URL}/brands/${id}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching brand:', error);
    throw error;
  }
};

export const createBrand = async (brandData) => {
  try {
    const response = await axios.post(`${API_BASE_URL}/brands`, brandData);
    return response.data;
  } catch (error) {
    console.error('Error creating brand:', error);
    throw error;
  }
};

export const updateBrand = async (id, brandData) => {
  try {
    const response = await axios.put(`${API_BASE_URL}/brands/${id}`, brandData);
    return response.data;
  } catch (error) {
    console.error('Error updating brand:', error);
    throw error;
  }
};

export const deleteBrand = async (id) => {
  try {
    await axios.delete(`${API_BASE_URL}/brands/${id}`);
  } catch (error) {
    console.error('Error deleting brand:', error);
    throw error;
  }
};
