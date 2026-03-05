import React, { useState, useEffect } from 'react';
import { Tags, Search, Edit, Trash2, Plus, X } from 'lucide-react';
import { getAllBrands, createBrand, updateBrand, deleteBrand } from '../api/brandApi';

const Brands = () => {
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingBrand, setEditingBrand] = useState(null);
  const [formData, setFormData] = useState({ name: '', origin: '' });

  useEffect(() => {
    loadBrands();
  }, []);

  const loadBrands = async () => {
    try {
      setLoading(true);
      const data = await getAllBrands();
      setBrands(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (brand = null) => {
    if (brand) {
      setEditingBrand(brand);
      setFormData({ name: brand.name, origin: brand.origin || '' });
    } else {
      setEditingBrand(null);
      setFormData({ name: '', origin: '' });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingBrand(null);
    setFormData({ name: '', origin: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingBrand) {
        const updated = await updateBrand(editingBrand.id, formData);
        setBrands(brands.map(b => b.id === updated.id ? updated : b));
        alert('Cập nhật thương hiệu thành công!');
      } else {
        const created = await createBrand(formData);
        setBrands([...brands, created]);
        alert('Thêm thương hiệu thành công!');
      }
      handleCloseModal();
    } catch (err) {
      alert('Có lỗi xảy ra!');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa thương hiệu này?')) return;
    
    try {
      await deleteBrand(id);
      setBrands(brands.filter(b => b.id !== id));
      alert('Xóa thương hiệu thành công!');
    } catch (err) {
      alert('Không thể xóa thương hiệu');
    }
  };

  const filteredBrands = brands.filter(brand =>
    brand.name?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900 mb-2">Quản lý thương hiệu</h1>
          <p className="text-gray-600">Quản lý danh sách thương hiệu sản phẩm</p>
        </div>
        <button
          onClick={() => handleOpenModal()}
          className="mt-4 sm:mt-0 inline-flex items-center space-x-2 bg-slate-900 text-white px-4 py-2 rounded-lg hover:bg-slate-800 transition-all"
        >
          <Plus size={20} />
          <span>Thêm thương hiệu</span>
        </button>
      </div>

      {/* Search Bar */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm thương hiệu..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>
      </div>

      {/* Brands Grid */}
      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-slate-900"></div>
            <p className="mt-2 text-gray-600">Đang tải...</p>
          </div>
        ) : filteredBrands.length === 0 ? (
          <div className="p-8 text-center text-gray-500">
            <Tags className="mx-auto mb-2 text-gray-400" size={48} />
            <p>Chưa có thương hiệu nào</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 p-6">
            {filteredBrands.map((brand) => (
              <div key={brand.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center space-x-3">
                    <Tags className="text-slate-900" size={24} />
                    <div>
                      <h3 className="font-semibold text-gray-900">{brand.name}</h3>
                      {brand.origin && (
                        <p className="text-sm text-gray-500">{brand.origin}</p>
                      )}
                    </div>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleOpenModal(brand)}
                      className="text-blue-600 hover:text-blue-900"
                      title="Chỉnh sửa"
                    >
                      <Edit size={18} />
                    </button>
                    <button
                      onClick={() => handleDelete(brand.id)}
                      className="text-red-600 hover:text-red-900"
                      title="Xóa"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
                <p className="text-xs text-gray-500">ID: {brand.id}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
            <div className="flex items-center justify-between p-6 border-b border-gray-200">
              <h2 className="text-xl font-semibold text-gray-900">
                {editingBrand ? 'Chỉnh sửa thương hiệu' : 'Thêm thương hiệu mới'}
              </h2>
              <button onClick={handleCloseModal} className="text-gray-400 hover:text-gray-600">
                <X size={24} />
              </button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Tên thương hiệu *
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Xuất xứ
                </label>
                <input
                  type="text"
                  value={formData.origin}
                  onChange={(e) => setFormData({ ...formData, origin: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                  placeholder="Ví dụ: Hàn Quốc, Nhật Bản..."
                />
              </div>
              <div className="flex space-x-3 pt-4">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800"
                >
                  {editingBrand ? 'Cập nhật' : 'Thêm'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Brands;
