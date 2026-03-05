import React, { useState } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Package, 
  ShoppingCart, 
  Users, 
  Briefcase,
  Tags, 
  FolderTree, 
  ShieldCheck,
  FileClock,
  PercentCircle,
  FileBarChart2,
  Settings,
  LogOut,
  Menu,
  X
} from 'lucide-react';
import { logout, getCurrentUser } from '../api/authApi';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const currentUser = getCurrentUser();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const menuItems = [
    { path: '/admin', icon: LayoutDashboard, label: 'Tổng quan', exact: true },
    { path: '/admin/products', icon: Package, label: 'Sản phẩm' },
    { path: '/admin/add-product', icon: Package, label: 'Thêm sản phẩm' },
    { path: '/admin/orders', icon: ShoppingCart, label: 'Đơn hàng' },
    { path: '/admin/customers', icon: Users, label: 'Khách hàng' },
    { path: '/admin/employees', icon: Briefcase, label: 'Nhân viên' },
    { path: '/admin/user-management', icon: ShieldCheck, label: 'Người dùng' },
    { path: '/admin/audit-logs', icon: FileClock, label: 'Audit Logs' },
    { path: '/admin/brands', icon: Tags, label: 'Thương hiệu' },
    { path: '/admin/categories', icon: FolderTree, label: 'Danh mục' },
    { path: '/admin/product-pricing', icon: PercentCircle, label: 'Giá & khuyến mãi' },
    { path: '/admin/financial-reports', icon: FileBarChart2, label: 'Báo cáo tài chính' },
    { path: '/admin/system-settings', icon: Settings, label: 'Cấu hình hệ thống' },
  ];

  const isActive = (path, exact = false) => {
    if (exact) {
      return location.pathname === path;
    }
    return location.pathname.startsWith(path);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <aside 
        className={`fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Logo */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-gray-200">
          <Link to="/" className="flex items-center space-x-2">
            <h1 className="text-2xl font-light tracking-[0.2em] uppercase text-slate-900">LUMIA</h1>
          </Link>
          <button 
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden text-gray-500 hover:text-gray-700"
          >
            <X size={24} />
          </button>
        </div>

        {/* User Info */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-full bg-slate-900 flex items-center justify-center text-white font-medium">
              {currentUser?.username?.charAt(0).toUpperCase()}
            </div>
            <div>
              <p className="text-sm font-medium text-gray-900">{currentUser?.username}</p>
              <p className="text-xs text-gray-500 uppercase">{currentUser?.role}</p>
            </div>
          </div>
        </div>

        {/* Navigation Menu */}
        <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const active = isActive(item.path, item.exact);
            
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-all ${
                  active
                    ? 'bg-slate-900 text-white'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                <Icon size={20} />
                <span className="text-sm font-medium">{item.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* Logout Button */}
        <div className="p-4 border-t border-gray-200">
          <button
            onClick={handleLogout}
            className="flex items-center space-x-3 px-4 py-3 w-full text-left text-red-600 hover:bg-red-50 rounded-lg transition-all"
          >
            <LogOut size={20} />
            <span className="text-sm font-medium">Đăng xuất</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-h-screen">
        {/* Header */}
        <header className="h-16 bg-white shadow-sm flex items-center justify-between px-4 lg:px-8">
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden text-gray-600 hover:text-gray-900"
          >
            <Menu size={24} />
          </button>
          
          <div className="flex-1 lg:flex-none">
            <h2 className="text-xl font-semibold text-gray-900">
              {menuItems.find(item => isActive(item.path, item.exact))?.label || 'Admin Dashboard'}
            </h2>
          </div>

          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-600 hidden sm:inline">
              Chào mừng, <span className="font-medium">{currentUser?.fullName || currentUser?.username}</span>
            </span>
          </div>
        </header>

        {/* Content Area */}
        <main className="flex-1 p-4 lg:p-8">
          <Outlet />
        </main>
      </div>

      {/* Overlay */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}
    </div>
  );
};

export default AdminDashboard;
