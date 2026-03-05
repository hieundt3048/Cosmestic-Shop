import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Heart, ShoppingCart, Star } from 'lucide-react';
import { productAPI } from '../api/productApi';
import { addToCart, toggleWishlist } from '../utils/shopStorage';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const data = await productAPI.getProductById(id);
        setProduct(data);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  if (loading) {
    return <div className="min-h-screen p-8 text-slate-600">Đang tải chi tiết sản phẩm...</div>;
  }

  if (!product) {
    return <div className="min-h-screen p-8 text-slate-600">Không tìm thấy sản phẩm.</div>;
  }

  const inStock = (product.stockQuantity || 0) > 0;

  return (
    <div className="min-h-screen bg-slate-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6 text-sm text-slate-500">
          <Link to="/" className="hover:text-slate-900">Trang chủ</Link>
          <span className="mx-2">/</span>
          <Link to="/catalog" className="hover:text-slate-900">Danh mục</Link>
          <span className="mx-2">/</span>
          <span>{product.productName || product.name}</span>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
            <img
              src={product.imageUrl || 'https://via.placeholder.com/700x900?text=Product'}
              alt={product.productName || product.name}
              className="w-full h-full object-cover"
              loading="lazy"
            />
          </div>

          <div className="bg-white rounded-xl border border-slate-200 p-6">
            <p className="text-xs uppercase tracking-wider text-slate-400 mb-2">{product.category?.name || 'N/A'}</p>
            <h1 className="text-2xl font-semibold text-slate-900 mb-2">{product.productName || product.name}</h1>
            <p className="text-slate-500 mb-4">Thương hiệu: {product.brand?.name || 'N/A'}</p>
            <p className="text-2xl font-bold text-slate-900 mb-4">{(product.price || 0).toLocaleString('vi-VN')}đ</p>

            <div className="mb-4">
              {inStock ? (
                <span className="inline-flex px-2 py-1 rounded-full bg-green-100 text-green-700 text-sm">
                  Còn hàng ({product.stockQuantity})
                </span>
              ) : (
                <span className="inline-flex px-2 py-1 rounded-full bg-red-100 text-red-700 text-sm">Hết hàng</span>
              )}
            </div>

            <p className="text-slate-700 leading-relaxed mb-6">{product.description || 'Chưa có mô tả sản phẩm.'}</p>

            <div className="flex gap-3 mb-8">
              <button
                disabled={!inStock}
                onClick={() => addToCart(product, 1)}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 disabled:bg-slate-300"
              >
                <ShoppingCart size={18} />
                Thêm vào giỏ
              </button>
              <button
                onClick={() => toggleWishlist(product)}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-100"
              >
                <Heart size={18} />
                Yêu thích
              </button>
              <button
                onClick={() => navigate('/cart')}
                className="px-4 py-2 rounded-lg border border-slate-200 hover:bg-slate-100"
              >
                Xem giỏ
              </button>
            </div>

            <div className="border-t border-slate-100 pt-4">
              <h2 className="font-semibold text-slate-900 mb-3">Đánh giá gần đây</h2>
              <div className="space-y-3 text-sm text-slate-700">
                <div className="p-3 rounded-lg bg-slate-50">
                  <p className="inline-flex items-center gap-1 text-amber-500 mb-1"><Star size={14} /> 5.0</p>
                  <p>“Sản phẩm dùng rất ổn, thấm nhanh và không kích ứng.”</p>
                </div>
                <div className="p-3 rounded-lg bg-slate-50">
                  <p className="inline-flex items-center gap-1 text-amber-500 mb-1"><Star size={14} /> 4.0</p>
                  <p>“Giá hợp lý, sẽ mua lại lần sau.”</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
