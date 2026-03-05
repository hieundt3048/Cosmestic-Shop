import React, { useEffect, useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Menu, Search, ShoppingCart, User, X } from 'lucide-react';
import { getCurrentUser, isAuthenticated, logout } from '../api/authApi';
import { getCartItems } from '../utils/shopStorage';

const StorefrontLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [cartCount, setCartCount] = useState(0);

  const currentUser = getCurrentUser();

  useEffect(() => {
    setCartCount(getCartItems().reduce((sum, item) => sum + item.quantity, 0));
  }, [location.pathname, location.search]);

  useEffect(() => {
    setIsMenuOpen(false);
    setUserMenuOpen(false);
  }, [location.pathname]);

  const handleUserAction = () => {
    if (isAuthenticated()) {
      setUserMenuOpen((prev) => !prev);
      return;
    }

    navigate('/login');
  };

  const handleLogout = () => {
    logout();
    setUserMenuOpen(false);
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      <header className="sticky top-0 z-50 bg-white border-b border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button className="lg:hidden p-2" onClick={() => setIsMenuOpen((prev) => !prev)}>
              {isMenuOpen ? <X size={22} /> : <Menu size={22} />}
            </button>
            <Link to="/" className="text-xl font-semibold tracking-widest uppercase">LUMIA</Link>
          </div>

          <nav className="hidden lg:flex items-center gap-8 text-sm uppercase tracking-wider">
            <Link to="/" className="hover:text-slate-500">Trang chủ</Link>
            <Link to="/catalog" className="hover:text-slate-500">Danh mục</Link>
            <Link to="/account" className="hover:text-slate-500">Tài khoản</Link>
          </nav>

          <div className="flex items-center gap-3">
            <button onClick={() => navigate('/catalog')} className="p-2 rounded-lg hover:bg-slate-100">
              <Search size={20} />
            </button>

            <div className="relative">
              <button onClick={handleUserAction} className="p-2 rounded-lg hover:bg-slate-100">
                <User size={20} />
              </button>
              {userMenuOpen && isAuthenticated() && (
                <div className="absolute right-0 mt-2 w-56 bg-white border border-slate-200 shadow-lg rounded-lg py-2">
                  <div className="px-4 py-3 border-b border-slate-100">
                    <p className="text-xs uppercase tracking-wider text-slate-400">Xin chào</p>
                    <p className="text-sm font-medium text-slate-900">{currentUser?.fullName || currentUser?.username}</p>
                  </div>
                  <button
                    onClick={() => {
                      setUserMenuOpen(false);
                      navigate('/account');
                    }}
                    className="w-full text-left px-4 py-2 text-sm hover:bg-slate-50"
                  >
                    Tài khoản & đơn hàng
                  </button>
                  <button onClick={handleLogout} className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50">
                    Đăng xuất
                  </button>
                </div>
              )}
            </div>

            <button onClick={() => navigate('/cart')} className="p-2 rounded-lg hover:bg-slate-100 relative">
              <ShoppingCart size={20} />
              {cartCount > 0 && (
                <span className="absolute -top-1 -right-1 text-[10px] bg-slate-900 text-white rounded-full w-4 h-4 grid place-items-center">
                  {cartCount}
                </span>
              )}
            </button>
          </div>
        </div>

        {isMenuOpen && (
          <div className="lg:hidden border-t border-slate-200 px-4 py-3 flex flex-col gap-3 text-sm">
            <Link to="/catalog" onClick={() => setIsMenuOpen(false)}>Danh mục</Link>
            <Link to="/account" onClick={() => setIsMenuOpen(false)}>Tài khoản</Link>
            <Link to="/cart" onClick={() => setIsMenuOpen(false)}>Giỏ hàng</Link>
          </div>
        )}
      </header>

      <Outlet />

      <footer className="bg-white border-t border-slate-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <p className="text-lg font-semibold tracking-widest uppercase text-slate-900 mb-3">LUMIA</p>
            <p className="text-sm text-slate-600">Mỹ phẩm chính hãng, chăm sóc da an toàn và giao nhanh toàn quốc.</p>
          </div>

          <div>
            <p className="text-sm font-semibold uppercase tracking-wider text-slate-900 mb-3">Liên kết nhanh</p>
            <div className="flex flex-col gap-2 text-sm text-slate-600">
              <Link to="/catalog" className="hover:text-slate-900">Danh mục sản phẩm</Link>
              <Link to="/cart" className="hover:text-slate-900">Giỏ hàng</Link>
              <Link to="/account" className="hover:text-slate-900">Tài khoản</Link>
            </div>
          </div>

          <div>
            <p className="text-sm font-semibold uppercase tracking-wider text-slate-900 mb-3">Hỗ trợ</p>
            <div className="text-sm text-slate-600 space-y-2">
              <p>Hotline: 1900 1234</p>
              <p>Email: support@lumia.vn</p>
              <p>Giờ làm việc: 08:00 - 22:00</p>
            </div>
          </div>
        </div>
        <div className="border-t border-slate-100">
          <p className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 text-xs text-slate-500">
            © {new Date().getFullYear()} LUMIA Cosmetic Shop. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default StorefrontLayout;
