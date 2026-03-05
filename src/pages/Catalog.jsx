import React, { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Search, Heart, SlidersHorizontal } from 'lucide-react';
import { productAPI } from '../api/productApi';
import { getAllBrands } from '../api/brandApi';
import { getAllCategories } from '../api/categoryApi';
import { addToCart, toggleWishlist } from '../utils/shopStorage';

const Catalog = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [brands, setBrands] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  const [query, setQuery] = useState(searchParams.get('q') || '');
  const [brandFilter, setBrandFilter] = useState(searchParams.get('brand') || 'ALL');
  const [categoryFilter, setCategoryFilter] = useState(searchParams.get('category') || 'ALL');
  const [sortBy, setSortBy] = useState(searchParams.get('sort') || 'newest');

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const [productData, brandData, categoryData] = await Promise.all([
          productAPI.getAllProducts(),
          getAllBrands(),
          getAllCategories(),
        ]);
        setProducts(productData || []);
        setBrands(brandData || []);
        setCategories(categoryData || []);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  useEffect(() => {
    const next = {};
    if (query) next.q = query;
    if (brandFilter !== 'ALL') next.brand = brandFilter;
    if (categoryFilter !== 'ALL') next.category = categoryFilter;
    if (sortBy !== 'newest') next.sort = sortBy;
    setSearchParams(next);
  }, [query, brandFilter, categoryFilter, sortBy, setSearchParams]);

  const filteredProducts = useMemo(() => {
    const q = query.trim().toLowerCase();
    let list = products.filter((product) => {
      const name = (product.productName || product.name || '').toLowerCase();
      const description = (product.description || '').toLowerCase();
      const brandName = (product.brand?.name || '').toLowerCase();
      const categoryName = (product.category?.name || '').toLowerCase();

      const matchesText = !q || name.includes(q) || description.includes(q) || brandName.includes(q);
      const matchesBrand = brandFilter === 'ALL' || product.brand?.name === brandFilter;
      const matchesCategory = categoryFilter === 'ALL' || product.category?.name === categoryFilter;

      return matchesText && matchesBrand && matchesCategory;
    });

    if (sortBy === 'price_asc') list = [...list].sort((a, b) => (a.price || 0) - (b.price || 0));
    if (sortBy === 'price_desc') list = [...list].sort((a, b) => (b.price || 0) - (a.price || 0));
    if (sortBy === 'popular') list = [...list].sort((a, b) => (b.stockQuantity || 0) - (a.stockQuantity || 0));

    return list;
  }, [products, query, brandFilter, categoryFilter, sortBy]);

  return (
    <div className="min-h-screen bg-slate-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-slate-900">Danh mục sản phẩm</h1>
          <Link to="/" className="text-sm text-slate-600 hover:text-slate-900">Về trang chủ</Link>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-4 mb-6 grid grid-cols-1 md:grid-cols-4 gap-3">
          <div className="relative md:col-span-2">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Tìm theo tên hoặc mô tả sản phẩm"
              className="w-full pl-10 pr-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            />
          </div>

          <select
            value={brandFilter}
            onChange={(e) => setBrandFilter(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
          >
            <option value="ALL">Tất cả thương hiệu</option>
            {brands.map((brand) => (
              <option key={brand.id} value={brand.name}>{brand.name}</option>
            ))}
          </select>

          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
          >
            <option value="ALL">Tất cả danh mục</option>
            {categories.map((category) => (
              <option key={category.id} value={category.name}>{category.name}</option>
            ))}
          </select>

          <div className="md:col-span-4 flex items-center justify-between">
            <div className="inline-flex items-center gap-2 text-sm text-slate-600">
              <SlidersHorizontal size={16} />
              <span>Sắp xếp</span>
            </div>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            >
              <option value="newest">Mới nhất</option>
              <option value="price_asc">Giá tăng dần</option>
              <option value="price_desc">Giá giảm dần</option>
              <option value="popular">Phổ biến</option>
            </select>
          </div>
        </div>

        {loading ? (
          <div className="bg-white rounded-xl border border-slate-200 p-6 text-slate-600">Đang tải sản phẩm...</div>
        ) : filteredProducts.length === 0 ? (
          <div className="bg-white rounded-xl border border-slate-200 p-6 text-slate-600">Không tìm thấy sản phẩm phù hợp.</div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredProducts.map((product) => (
              <div key={product.id} className="bg-white rounded-xl border border-slate-200 overflow-hidden">
                <Link to={`/products/${product.id}`}>
                  <img
                    src={product.imageUrl || 'https://via.placeholder.com/400x500?text=Product'}
                    alt={product.productName || product.name}
                    className="w-full h-64 object-cover"
                    loading="lazy"
                  />
                </Link>
                <div className="p-4">
                  <p className="text-xs uppercase tracking-wider text-slate-400 mb-1">{product.category?.name || 'N/A'}</p>
                  <Link to={`/products/${product.id}`} className="font-medium text-slate-900 hover:underline block mb-2">
                    {product.productName || product.name}
                  </Link>
                  <p className="text-sm text-slate-500 mb-3">{product.brand?.name || 'N/A'}</p>
                  <div className="flex items-center justify-between">
                    <p className="font-semibold text-slate-900">{(product.price || 0).toLocaleString('vi-VN')}đ</p>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => toggleWishlist(product)}
                        className="p-2 rounded-lg border border-slate-200 hover:bg-slate-100"
                      >
                        <Heart size={16} />
                      </button>
                      <button
                        onClick={() => addToCart(product, 1)}
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
      </div>
    </div>
  );
};

export default Catalog;
