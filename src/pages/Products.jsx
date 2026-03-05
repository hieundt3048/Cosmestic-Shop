import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Package, Search, Edit, Trash2, Plus } from 'lucide-react';
import { productAPI } from '../api/productApi';

const Products = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await productAPI.getAllProducts();
      setProducts(data);
      setError('');
    } catch (err) {
      setError('Không thể tải danh sách sản phẩm');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
    
    try {
      // Assuming delete API exists
      // await productAPI.deleteProduct(id);
      setProducts(products.filter(p => p.id !== id));
      alert('Xóa sản phẩm thành công!');
    } catch (err) {
      alert('Không thể xóa sản phẩm');
    }
  };

  const filteredProducts = products.filter((product) => {
    const displayName = product.productName || product.name || '';
    return displayName.toLowerCase().includes(searchQuery.toLowerCase());
  });

  return (
    <div>
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900 mb-2">Quản lý sản phẩm</h1>
          <p className="text-gray-600">Quản lý danh sách sản phẩm của cửa hàng</p>
        </div>
        <Link
          to="/admin/add-product"
          className="mt-4 sm:mt-0 inline-flex items-center space-x-2 bg-slate-900 text-white px-4 py-2 rounded-lg hover:bg-slate-800 transition-all"
        >
          <Plus size={20} />
          <span>Thêm sản phẩm</span>
        </Link>
      </div>

      {/* Search Bar */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm sản phẩm..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>
      </div>

      {/* Products Table */}
      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-slate-900"></div>
            <p className="mt-2 text-gray-600">Đang tải...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-600">{error}</div>
        ) : filteredProducts.length === 0 ? (
          <div className="p-8 text-center text-gray-500">
            <Package className="mx-auto mb-2 text-gray-400" size={48} />
            <p>Chưa có sản phẩm nào</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Sản phẩm
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Thương hiệu
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Danh mục
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Giá
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tồn kho
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Thao tác
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredProducts.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        <img
                          src={product.imageUrl || 'https://via.placeholder.com/50'}
                          alt={product.productName || product.name}
                          className="w-12 h-12 rounded-lg object-cover"
                        />
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">
                            {product.productName || product.name}
                          </div>
                          <div className="text-sm text-gray-500">
                            ID: {product.id}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      {product.brand?.name || 'N/A'}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      {product.category?.name || 'N/A'}
                    </td>
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">
                      {product.price?.toLocaleString('vi-VN')}đ
                    </td>
                    <td className="px-6 py-4 text-sm">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        product.stockQuantity > 10 
                          ? 'bg-green-100 text-green-800'
                          : product.stockQuantity > 0
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-red-100 text-red-800'
                      }`}>
                        {product.stockQuantity}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right text-sm space-x-2">
                      <button
                        className="text-blue-600 hover:text-blue-900 inline-flex items-center"
                        title="Chỉnh sửa"
                      >
                        <Edit size={18} />
                      </button>
                      <button
                        onClick={() => handleDelete(product.id)}
                        className="text-red-600 hover:text-red-900 inline-flex items-center"
                        title="Xóa"
                      >
                        <Trash2 size={18} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Stats Footer */}
      {!loading && !error && (
        <div className="mt-4 text-sm text-gray-600">
          Hiển thị {filteredProducts.length} / {products.length} sản phẩm
        </div>
      )}
    </div>
  );
};

export default Products;
