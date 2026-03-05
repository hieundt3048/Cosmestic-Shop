import React, { useState, useEffect } from 'react';
import { productAPI } from '../api/productApi';
import { getAllBrands, createBrand } from '../api/brandApi';
import { getAllCategories, createCategory } from '../api/categoryApi';

const ProductForm = () => {
  const [formData, setFormData] = useState({
    // Product Details
    productName: '',
    price: '',
    productDesc: '',
    imageUrl: '',
    stockQuantity: '',
    brandId: '',
    categoryId: ''
  });

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  // State cho danh sách Brand và Category
  const [brands, setBrands] = useState([]);
  const [categories, setCategories] = useState([]);

  // State cho modal thêm nhanh
  const [showBrandModal, setShowBrandModal] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [newBrandData, setNewBrandData] = useState({ name: '', origin: '' });
  const [newCategoryData, setNewCategoryData] = useState({ name: '' });

  // Load danh sách Brand và Category khi component mount
  useEffect(() => {
    loadBrands();
    loadCategories();
  }, []);

  const loadBrands = async () => {
    try {
      const data = await getAllBrands();
      setBrands(data);
    } catch (error) {
      console.error('Lỗi khi tải danh sách brands:', error);
    }
  };

  const loadCategories = async () => {
    try {
      const data = await getAllCategories();
      setCategories(data);
    } catch (error) {
      console.error('Lỗi khi tải danh sách categories:', error);
    }
  };

  // Xử lý thêm nhanh Brand
  const handleQuickAddBrand = async () => {
    if (!newBrandData.name.trim()) {
      alert('Vui lòng nhập tên Brand!');
      return;
    }
    
    try {
      const createdBrand = await createBrand(newBrandData);
      setBrands([...brands, createdBrand]);
      setFormData({ ...formData, brandId: createdBrand.id });
      setNewBrandData({ name: '', origin: '' });
      setShowBrandModal(false);
      setMessage({ type: 'success', text: 'Thêm Brand thành công!' });
    } catch (error) {
      console.error('Lỗi khi thêm brand:', error);
      alert('Có lỗi khi thêm Brand!');
    }
  };

  // Xử lý thêm nhanh Category
  const handleQuickAddCategory = async () => {
    if (!newCategoryData.name.trim()) {
      alert('Vui lòng nhập tên Category!');
      return;
    }
    
    try {
      const createdCategory = await createCategory(newCategoryData);
      setCategories([...categories, createdCategory]);
      setFormData({ ...formData, categoryId: createdCategory.id });
      setNewCategoryData({ name: '' });
      setShowCategoryModal(false);
      setMessage({ type: 'success', text: 'Thêm Category thành công!' });
    } catch (error) {
      console.error('Lỗi khi thêm category:', error);
      alert('Có lỗi khi thêm Category!');
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });

    // Validate Brand và Category
    if (!formData.brandId) {
      setMessage({ type: 'error', text: 'Vui lòng chọn Brand!' });
      setLoading(false);
      return;
    }

    if (!formData.categoryId) {
      setMessage({ type: 'error', text: 'Vui lòng chọn Category!' });
      setLoading(false);
      return;
    }

    try {
      const productData = {
        name: formData.productName,
        price: parseFloat(formData.price),
        description: formData.productDesc,
        imageUrl: formData.imageUrl,
        stockQuantity: parseInt(formData.stockQuantity),
        brandId: parseInt(formData.brandId),
        categoryId: parseInt(formData.categoryId)
      };

      console.log("Dữ liệu gửi đi:", productData);

      const response = await productAPI.createProduct(productData);
      
      console.log("Phản hồi từ server:", response);
      
      setMessage({ 
        type: 'success', 
        text: `Tạo sản phẩm thành công! ID: ${response.id}` 
      });

      // Reset form sau khi thành công
      setFormData({
        productName: '',
        price: '',
        productDesc: '',
        imageUrl: '',
        stockQuantity: '',
        brandId: '',
        categoryId: ''
      });

    } catch (error) {
      console.error("Lỗi khi tạo sản phẩm:", error);
      
      let errorMessage = 'Có lỗi xảy ra khi tạo sản phẩm!';
      
      if (error.response) {
        errorMessage = `Lỗi: ${error.response.status} - ${error.response.data.message || error.response.statusText}`;
      } else if (error.request) {
        errorMessage = 'Không thể kết nối đến server. Vui lòng kiểm tra backend đã chạy chưa!';
      }
      
      setMessage({ type: 'error', text: errorMessage });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6">
      {/* Hiển thị thông báo */}
      {message.text && (
        <div className={`mb-4 p-4 rounded-lg ${
          message.type === 'success' 
            ? 'bg-green-50 border border-green-200 text-green-700' 
            : 'bg-red-50 border border-red-200 text-red-700'
        }`}>
          <p className="font-medium">{message.text}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-6">
        
        {/* SECTION: BRAND & CATEGORY */}
        <div className="space-y-4 border border-gray-200 p-4 rounded-lg bg-gray-50">
          <h3 className="font-semibold text-gray-900">Thông tin Brand & Category</h3>
          
          {/* Brand Dropdown với nút "Thêm nhanh" */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Chọn Brand *</label>
            <div className="flex gap-2">
              <select 
                name="brandId" 
                value={formData.brandId}
                onChange={handleChange}
                required
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              >
                <option value="">-- Chọn Brand --</option>
                {brands.map(brand => (
                  <option key={brand.id} value={brand.id}>
                    {brand.name} {brand.origin ? `(${brand.origin})` : ''}
                  </option>
                ))}
              </select>
              <button
                type="button"
                onClick={() => setShowBrandModal(true)}
                className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 transition"
              >
                + Thêm
              </button>
            </div>
          </div>

          {/* Category Dropdown với nút "Thêm nhanh" */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Chọn Category *</label>
            <div className="flex gap-2">
              <select 
                name="categoryId" 
                value={formData.categoryId}
                onChange={handleChange}
                required
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              >
                <option value="">-- Chọn Category --</option>
                {categories.map(category => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
              <button
                type="button"
                onClick={() => setShowCategoryModal(true)}
                className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 transition"
              >
                + Thêm
              </button>
            </div>
          </div>
        </div>

        {/* SECTION: PRODUCT DETAILS */}
        <div className="space-y-4 border border-gray-200 p-4 rounded-lg bg-gray-50">
          <h3 className="font-semibold text-gray-900">Thông tin Product</h3>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tên sản phẩm (Name) *</label>
            <input 
              type="text" 
              name="productName" 
              value={formData.productName}
              onChange={handleChange} 
              required 
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900" 
              placeholder="VD: Chanel No.5 Eau de Parfum"
            />
          </div>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Giá (Price) *</label>
              <input 
                type="number" 
                step="0.01"
                name="price" 
                value={formData.price}
                onChange={handleChange} 
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900" 
                placeholder="0.00"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Số lượng (Stock) *</label>
              <input 
                type="number" 
                name="stockQuantity" 
                value={formData.stockQuantity}
                onChange={handleChange} 
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900" 
                placeholder="0"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Link hình ảnh (Image URL)</label>
            <input 
              type="text" 
              name="imageUrl" 
              value={formData.imageUrl}
              onChange={handleChange} 
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900" 
              placeholder="https://example.com/image.jpg"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả sản phẩm</label>
            <textarea 
              name="productDesc" 
              value={formData.productDesc}
              onChange={handleChange} 
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900" 
              rows="3"
              placeholder="Mô tả chi tiết về sản phẩm..."
            ></textarea>
          </div>
        </div>

        <button 
          type="submit" 
          disabled={loading}
          className={`md:col-span-2 py-3 rounded-lg font-semibold transition ${
            loading 
              ? 'bg-gray-400 cursor-not-allowed' 
              : 'bg-slate-900 text-white hover:bg-slate-800'
          }`}
        >
          {loading ? 'ĐANG XỬ LÝ...' : 'LƯU THÔNG TIN'}
        </button>
      </form>

      {/* Modal thêm Brand */}
      {showBrandModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-xl max-w-md w-full m-4">
            <h3 className="text-xl font-bold mb-4 text-gray-900">Thêm Brand Mới</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1 text-gray-700">Tên Brand *</label>
                <input
                  type="text"
                  value={newBrandData.name}
                  onChange={(e) => setNewBrandData({ ...newBrandData, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                  placeholder="VD: Gucci"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1 text-gray-700">Xuất xứ (Origin)</label>
                <input
                  type="text"
                  value={newBrandData.origin}
                  onChange={(e) => setNewBrandData({ ...newBrandData, origin: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                  placeholder="VD: Italy"
                />
              </div>
              <div className="flex gap-2 justify-end">
                <button
                  type="button"
                  onClick={() => {
                    setShowBrandModal(false);
                    setNewBrandData({ name: '', origin: '' });
                  }}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                >
                  Hủy
                </button>
                <button
                  type="button"
                  onClick={handleQuickAddBrand}
                  className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 transition"
                >
                  Lưu Brand
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal thêm Category */}
      {showCategoryModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-xl max-w-md w-full m-4">
            <h3 className="text-xl font-bold mb-4 text-gray-900">Thêm Category Mới</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1 text-gray-700">Tên Category *</label>
                <input
                  type="text"
                  value={newCategoryData.name}
                  onChange={(e) => setNewCategoryData({ ...newCategoryData, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                  placeholder="VD: Son môi"
                />
              </div>
              <div className="flex gap-2 justify-end">
                <button
                  type="button"
                  onClick={() => {
                    setShowCategoryModal(false);
                    setNewCategoryData({ name: '' });
                  }}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                >
                  Hủy
                </button>
                <button
                  type="button"
                  onClick={handleQuickAddCategory}
                  className="px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 transition"
                >
                  Lưu Category
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductForm;
