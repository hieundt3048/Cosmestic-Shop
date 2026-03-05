import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Heart, ChevronRight } from 'lucide-react';
import { productAPI } from '../api/productApi';
import { addToCart, toggleWishlist } from '../utils/shopStorage';

const HomePage = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadProducts = async () => {
      try {
        setLoading(true);
        const data = await productAPI.getAllProducts();
        setProducts(data || []);
      } finally {
        setLoading(false);
      }
    };

    loadProducts();
  }, []);

  const featuredProducts = useMemo(() => [...products].slice(0, 6), [products]);

  const quickCategories = useMemo(() => {
    const map = new Map();
    products.forEach((product) => {
      const categoryName = product.category?.name || 'Khác';
      map.set(categoryName, (map.get(categoryName) || 0) + 1);
    });

    return Array.from(map.entries())
      .map(([name, count]) => ({ name, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 6);
  }, [products]);

  const handleAddToCart = (product) => {
    addToCart(product, 1);
  };

  return (
    <>
      <section className="bg-gradient-to-r from-slate-900 to-slate-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <p className="text-sm uppercase tracking-[0.2em] text-slate-200 mb-3">Ưu đãi tuần này</p>
          <h1 className="text-4xl md:text-5xl font-light mb-4">Giảm đến 30% cho serum & chống nắng</h1>
          <p className="text-slate-200 max-w-2xl mb-6">
            Khám phá bộ sưu tập chăm sóc da mới nhất, giao nhanh toàn quốc, ưu đãi cho khách hàng đăng ký tài khoản.
          </p>
          <Link to="/catalog" className="inline-flex items-center gap-2 px-5 py-3 bg-white text-slate-900 rounded-lg font-medium hover:bg-slate-100">
            Mua ngay
            <ChevronRight size={16} />
          </Link>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h2 className="text-2xl font-semibold text-slate-900 mb-6">Danh mục nhanh</h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          {quickCategories.map((category) => (
            <button
              key={category.name}
              onClick={() => navigate(`/catalog?category=${encodeURIComponent(category.name)}`)}
              className="bg-white border border-slate-200 rounded-xl p-4 text-left hover:border-slate-300 hover:shadow-sm"
            >
              <p className="text-sm font-medium text-slate-900 line-clamp-2">{category.name}</p>
              <p className="text-xs text-slate-500 mt-1">{category.count} sản phẩm</p>
            </button>
          ))}
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-16">
        <div className="flex items-end justify-between mb-6">
          <h2 className="text-2xl font-semibold text-slate-900">Sản phẩm nổi bật</h2>
          <Link to="/catalog" className="text-sm text-slate-600 hover:text-slate-900 inline-flex items-center gap-1">
            Xem tất cả
            <ChevronRight size={15} />
          </Link>
        </div>

        {loading ? (
          <div className="bg-white rounded-xl border border-slate-200 p-6 text-slate-600">Đang tải sản phẩm...</div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {featuredProducts.map((product) => (
              <div key={product.id} className="bg-white rounded-xl border border-slate-200 overflow-hidden">
                <Link to={`/products/${product.id}`}>
                  <img
                    src={product.imageUrl || 'https://via.placeholder.com/500x620?text=Product'}
                    alt={product.productName || product.name}
                    className="w-full h-64 object-cover"
                    loading="lazy"
                  />
                </Link>

                <div className="p-4">
                  <p className="text-xs uppercase tracking-widest text-slate-400 mb-1">{product.category?.name || 'N/A'}</p>
                  <Link to={`/products/${product.id}`} className="text-base font-medium text-slate-900 hover:underline block mb-2">
                    {product.productName || product.name}
                  </Link>
                  <p className="text-sm text-slate-500 mb-3">{product.brand?.name || 'N/A'}</p>
                  <div className="flex items-center justify-between">
                    <p className="text-lg font-semibold text-slate-900">{(product.price || 0).toLocaleString('vi-VN')}đ</p>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => toggleWishlist(product)}
                        className="p-2 border border-slate-200 rounded-lg hover:bg-slate-100"
                      >
                        <Heart size={16} />
                      </button>
                      <button
                        onClick={() => handleAddToCart(product)}
                        className="px-3 py-2 rounded-lg bg-slate-900 text-white text-sm hover:bg-slate-800"
                      >
                        Thêm giỏ
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </>
  );
};

export default HomePage;
