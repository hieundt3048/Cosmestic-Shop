import React from 'react';
import ProductForm from '../components/ProductForm';

const AddProduct = () => {
  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Thêm sản phẩm mới</h1>
        <p className="text-gray-600">Điền thông tin để thêm sản phẩm mới vào hệ thống</p>
      </div>
      
      <div className="bg-white rounded-lg shadow-sm">
        <ProductForm />
      </div>
    </div>
  );
};

export default AddProduct;
