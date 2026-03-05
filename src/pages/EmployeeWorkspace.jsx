import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Package,
  ClipboardList,
  User,
  LogOut,
  Bell,
  Search,
  CheckCircle2,
  XCircle,
  EyeOff,
  Eye,
  Users,
} from 'lucide-react';
import { getCurrentUser, logout } from '../api/authApi';
import { productAPI } from '../api/productApi';
import { userAPI } from '../api/userApi';

const ORDER_FLOW = ['PENDING', 'CONFIRMED', 'PACKING', 'SHIPPING', 'COMPLETED'];

const STATUS_LABELS = {
  PENDING: 'Chờ xử lý',
  CONFIRMED: 'Đã xác nhận',
  PACKING: 'Đang đóng gói',
  SHIPPING: 'Đã bàn giao vận chuyển',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
};

const STATUS_STYLES = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  PACKING: 'bg-indigo-100 text-indigo-800',
  SHIPPING: 'bg-purple-100 text-purple-800',
  COMPLETED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
};

const getMockOrders = () => [
  {
    id: 1001,
    phone: '0901234567',
    customerName: 'Nguyễn Văn A',
    createdAt: '2026-03-02',
    totalAmount: 1250000,
    items: 3,
    status: 'PENDING',
    updatedAt: '2026-03-02',
  },
  {
    id: 1002,
    phone: '0907654321',
    customerName: 'Trần Thị B',
    createdAt: '2026-03-01',
    totalAmount: 780000,
    items: 2,
    status: 'CONFIRMED',
    updatedAt: '2026-03-02',
  },
  {
    id: 1003,
    phone: '0912345678',
    customerName: 'Lê Thu C',
    createdAt: '2026-02-28',
    totalAmount: 2250000,
    items: 5,
    status: 'SHIPPING',
    updatedAt: '2026-03-01',
  },
];

const getMockReviews = () => [
  { id: 1, productName: 'Serum Vitamin C', customerName: 'Nguyễn M', rating: 5, comment: 'Dùng rất thích, da sáng hơn.', status: 'PENDING' },
  { id: 2, productName: 'Kem chống nắng SPF50', customerName: 'Trần N', rating: 2, comment: 'Hơi bí da với mình.', status: 'PENDING' },
];

const EmployeeWorkspace = () => {
  const navigate = useNavigate();
  const currentUser = getCurrentUser();
  const [activeTab, setActiveTab] = useState('dashboard');

  const [orders, setOrders] = useState(getMockOrders());
  const [orderQuery, setOrderQuery] = useState('');
  const [orderStatus, setOrderStatus] = useState('ALL');
  const [orderDate, setOrderDate] = useState('');

  const [products, setProducts] = useState([]);
  const [productQuery, setProductQuery] = useState('');
  const [brandFilter, setBrandFilter] = useState('ALL');
  const [stockDrafts, setStockDrafts] = useState({});
  const [hiddenProducts, setHiddenProducts] = useState({});
  const [productsLoading, setProductsLoading] = useState(false);

  const [customers, setCustomers] = useState([]);
  const [customerQuery, setCustomerQuery] = useState('');
  const [customersLoading, setCustomersLoading] = useState(false);

  const [reviews, setReviews] = useState(getMockReviews());

  useEffect(() => {
    loadProducts();
    loadCustomers();
  }, []);

  const loadProducts = async () => {
    try {
      setProductsLoading(true);
      const data = await productAPI.getAllProducts();
      setProducts(data || []);
    } catch {
      setProducts([]);
    } finally {
      setProductsLoading(false);
    }
  };

  const loadCustomers = async () => {
    try {
      setCustomersLoading(true);
      const data = await userAPI.getCustomers();
      setCustomers(data || []);
    } catch {
      setCustomers([]);
    } finally {
      setCustomersLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const nextStatus = (status) => {
    const idx = ORDER_FLOW.indexOf(status);
    if (idx === -1 || idx === ORDER_FLOW.length - 1) return status;
    return ORDER_FLOW[idx + 1];
  };

  const advanceOrderStatus = (orderId) => {
    setOrders((prev) =>
      prev.map((order) => {
        if (order.id !== orderId || order.status === 'CANCELLED' || order.status === 'COMPLETED') return order;
        return { ...order, status: nextStatus(order.status), updatedAt: new Date().toISOString().slice(0, 10) };
      })
    );
  };

  const cancelOrder = (orderId) => {
    setOrders((prev) =>
      prev.map((order) =>
        order.id === orderId && order.status !== 'COMPLETED'
          ? { ...order, status: 'CANCELLED', updatedAt: new Date().toISOString().slice(0, 10) }
          : order
      )
    );
  };

  const filteredOrders = useMemo(() => {
    return orders.filter((order) => {
      const query = orderQuery.trim().toLowerCase();
      const matchesQuery =
        !query ||
        order.customerName?.toLowerCase().includes(query) ||
        order.phone?.includes(query) ||
        String(order.id).includes(query);

      const matchesStatus = orderStatus === 'ALL' || order.status === orderStatus;
      const matchesDate = !orderDate || order.createdAt === orderDate;

      return matchesQuery && matchesStatus && matchesDate;
    });
  }, [orders, orderQuery, orderStatus, orderDate]);

  const brandOptions = useMemo(() => {
    const set = new Set(products.map((p) => p.brand?.name).filter(Boolean));
    return ['ALL', ...Array.from(set)];
  }, [products]);

  const getExpiryDays = (product) => {
    const raw = ((product.id || 1) * 17) % 210;
    return raw - 20;
  };

  const filteredProducts = useMemo(() => {
    return products.filter((product) => {
      const query = productQuery.trim().toLowerCase();
      const displayName = product.productName || product.name || '';
      const matchesQuery = !query || displayName.toLowerCase().includes(query);
      const matchesBrand = brandFilter === 'ALL' || product.brand?.name === brandFilter;
      return matchesQuery && matchesBrand;
    });
  }, [products, productQuery, brandFilter]);

  const applyStockUpdate = (productId) => {
    const value = Number(stockDrafts[productId]);
    if (Number.isNaN(value) || value < 0) return;

    setProducts((prev) =>
      prev.map((p) => (p.id === productId ? { ...p, stockQuantity: value } : p))
    );
  };

  const toggleVisibility = (productId) => {
    setHiddenProducts((prev) => ({ ...prev, [productId]: !prev[productId] }));
  };

  const filteredCustomers = useMemo(() => {
    const q = customerQuery.trim().toLowerCase();
    return customers.filter((customer) => {
      return (
        !q ||
        customer.username?.toLowerCase().includes(q) ||
        customer.fullName?.toLowerCase().includes(q) ||
        customer.email?.toLowerCase().includes(q) ||
        customer.phone?.includes(q)
      );
    });
  }, [customers, customerQuery]);

  const today = new Date().toISOString().slice(0, 10);
  const pendingOrders = orders.filter((o) => o.status === 'PENDING').length;
  const packedToday = orders.filter((o) => o.updatedAt === today && ['PACKING', 'SHIPPING', 'COMPLETED'].includes(o.status)).length;
  const lowStockProducts = products.filter((p) => (p.stockQuantity || 0) <= 10).length;

  const notifications = [
    pendingOrders > 0 ? `Có ${pendingOrders} đơn hàng mới đang chờ xử lý.` : null,
    lowStockProducts > 0 ? `Có ${lowStockProducts} sản phẩm chạm mức tồn kho tối thiểu.` : null,
  ].filter(Boolean);

  const renderStatusBadge = (status) => (
    <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_STYLES[status] || 'bg-gray-100 text-gray-700'}`}>
      {STATUS_LABELS[status] || status}
    </span>
  );

  const reviewAction = (reviewId, status) => {
    setReviews((prev) => prev.map((r) => (r.id === reviewId ? { ...r, status } : r)));
  };

  const tabs = [
    { key: 'dashboard', label: 'Dashboard', icon: Bell },
    { key: 'orders', label: 'Đơn hàng', icon: ClipboardList },
    { key: 'inventory', label: 'Kho & Sản phẩm', icon: Package },
    { key: 'customers', label: 'Khách hàng & Đánh giá', icon: Users },
  ];

  return (
    <div className="min-h-screen bg-gray-50 flex">
      <aside className="w-72 bg-white border-r border-gray-200 shadow-sm hidden md:flex md:flex-col">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">Khu vực nhân viên</h1>
          <p className="text-sm text-gray-600 mt-2">
            Xin chào, {currentUser?.fullName || currentUser?.username}
          </p>
        </div>

        <nav className="flex-1 p-4 space-y-2">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const active = activeTab === tab.key;
            return (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`w-full inline-flex items-center gap-3 px-4 py-3 rounded-lg text-sm transition-all ${
                  active ? 'bg-slate-900 text-white' : 'text-gray-700 hover:bg-gray-100'
                }`}
              >
                <Icon size={18} />
                {tab.label}
              </button>
            );
          })}
        </nav>

        <div className="p-4 border-t border-gray-200 space-y-2">
          <button
            onClick={() => navigate('/')}
            className="w-full px-4 py-2 rounded-lg text-sm bg-gray-100 text-gray-700 hover:bg-gray-200 transition-all"
          >
            Về trang chủ
          </button>
          <button
            onClick={handleLogout}
            className="w-full inline-flex items-center justify-center gap-2 px-4 py-2 rounded-lg text-red-600 hover:bg-red-50 transition-all"
          >
            <LogOut size={18} />
            Đăng xuất
          </button>
        </div>
      </aside>

      <main className="flex-1 min-w-0">
        <header className="bg-white border-b border-gray-200 px-4 md:px-8 py-4 flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">
              {tabs.find((t) => t.key === activeTab)?.label}
            </h2>
            <p className="text-sm text-gray-600 mt-1 md:hidden">
              {currentUser?.fullName || currentUser?.username}
            </p>
          </div>
          <button
            onClick={handleLogout}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-red-600 hover:bg-red-50 transition-all"
          >
            <LogOut size={18} />
            Đăng xuất
          </button>
        </header>

        <div className="px-4 md:px-8 py-6">

        {activeTab === 'dashboard' && (
          <div className="space-y-6">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                <p className="text-sm text-gray-500">Đơn chờ xử lý</p>
                <p className="text-3xl font-semibold mt-1 text-gray-900">{pendingOrders}</p>
              </div>
              <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                <p className="text-sm text-gray-500">Đơn đã xử lý hôm nay</p>
                <p className="text-3xl font-semibold mt-1 text-gray-900">{packedToday}</p>
              </div>
              <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                <p className="text-sm text-gray-500">Sản phẩm tồn kho thấp</p>
                <p className="text-3xl font-semibold mt-1 text-gray-900">{lowStockProducts}</p>
              </div>
            </div>

            <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Thông báo hệ thống</h3>
              {notifications.length === 0 ? (
                <p className="text-sm text-gray-600">Hiện chưa có cảnh báo mới.</p>
              ) : (
                <ul className="space-y-2">
                  {notifications.map((note) => (
                    <li key={note} className="text-sm text-gray-700 flex items-start gap-2">
                      <Bell size={16} className="mt-0.5 text-amber-500" />
                      <span>{note}</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        )}

        {activeTab === 'orders' && (
          <div className="space-y-4">
            <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 grid grid-cols-1 md:grid-cols-4 gap-3">
              <div className="relative md:col-span-2">
                <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  value={orderQuery}
                  onChange={(e) => setOrderQuery(e.target.value)}
                  placeholder="Tìm theo mã đơn, SĐT, khách hàng"
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                />
              </div>
              <select
                value={orderStatus}
                onChange={(e) => setOrderStatus(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              >
                <option value="ALL">Tất cả trạng thái</option>
                {Object.keys(STATUS_LABELS).map((s) => (
                  <option key={s} value={s}>{STATUS_LABELS[s]}</option>
                ))}
              </select>
              <input
                type="date"
                value={orderDate}
                onChange={(e) => setOrderDate(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              />
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã đơn</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khách hàng</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">SĐT</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày đặt</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tổng tiền</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                    <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredOrders.map((order) => (
                    <tr key={order.id}>
                      <td className="px-4 py-3 text-sm font-medium text-gray-900">#{order.id}</td>
                      <td className="px-4 py-3 text-sm text-gray-700">{order.customerName}</td>
                      <td className="px-4 py-3 text-sm text-gray-700">{order.phone}</td>
                      <td className="px-4 py-3 text-sm text-gray-700">{order.createdAt}</td>
                      <td className="px-4 py-3 text-sm text-gray-900">{order.totalAmount.toLocaleString('vi-VN')}đ</td>
                      <td className="px-4 py-3">{renderStatusBadge(order.status)}</td>
                      <td className="px-4 py-3 text-right space-x-2">
                        <button
                          disabled={order.status === 'CANCELLED' || order.status === 'COMPLETED'}
                          onClick={() => advanceOrderStatus(order.id)}
                          className="px-3 py-1.5 rounded-lg text-xs bg-slate-900 text-white disabled:bg-gray-300"
                        >
                          Chuyển bước
                        </button>
                        <button
                          disabled={order.status === 'CANCELLED' || order.status === 'COMPLETED'}
                          onClick={() => cancelOrder(order.id)}
                          className="px-3 py-1.5 rounded-lg text-xs bg-red-100 text-red-700 disabled:bg-gray-100 disabled:text-gray-400"
                        >
                          Hủy đơn
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === 'inventory' && (
          <div className="space-y-4">
            <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 grid grid-cols-1 md:grid-cols-3 gap-3">
              <div className="relative md:col-span-2">
                <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  value={productQuery}
                  onChange={(e) => setProductQuery(e.target.value)}
                  placeholder="Tìm theo tên sản phẩm"
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                />
              </div>
              <select
                value={brandFilter}
                onChange={(e) => setBrandFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              >
                {brandOptions.map((b) => (
                  <option key={b} value={b}>{b === 'ALL' ? 'Tất cả thương hiệu' : b}</option>
                ))}
              </select>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-x-auto">
              {productsLoading ? (
                <div className="p-6 text-sm text-gray-600">Đang tải dữ liệu sản phẩm...</div>
              ) : (
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Sản phẩm</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Thương hiệu</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tồn kho</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Hạn dùng</th>
                      <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {filteredProducts.map((product) => {
                      const expiryDays = getExpiryDays(product);
                      const lowStock = (product.stockQuantity || 0) <= 10;
                      return (
                        <tr key={product.id} className={hiddenProducts[product.id] ? 'opacity-60' : ''}>
                          <td className="px-4 py-3">
                            <p className="text-sm font-medium text-gray-900">{product.productName || product.name}</p>
                            <p className="text-xs text-gray-500">ID: {product.id}</p>
                          </td>
                          <td className="px-4 py-3 text-sm text-gray-700">{product.brand?.name || 'N/A'}</td>
                          <td className="px-4 py-3">
                            <span className={`inline-flex px-2 py-0.5 rounded-full text-xs ${lowStock ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
                              {product.stockQuantity}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-sm">
                            {expiryDays <= 0 ? (
                              <span className="text-red-700">Đã hết hạn</span>
                            ) : expiryDays <= 30 ? (
                              <span className="text-amber-700">Còn {expiryDays} ngày</span>
                            ) : (
                              <span className="text-gray-700">Còn {expiryDays} ngày</span>
                            )}
                          </td>
                          <td className="px-4 py-3 text-right space-x-2">
                            <input
                              type="number"
                              min="0"
                              value={stockDrafts[product.id] ?? product.stockQuantity}
                              onChange={(e) => setStockDrafts((prev) => ({ ...prev, [product.id]: e.target.value }))}
                              className="w-20 px-2 py-1.5 border border-gray-300 rounded text-sm"
                            />
                            <button
                              onClick={() => applyStockUpdate(product.id)}
                              className="px-3 py-1.5 rounded-lg text-xs bg-slate-900 text-white"
                            >
                              Cập nhật
                            </button>
                            <button
                              onClick={() => toggleVisibility(product.id)}
                              className="px-3 py-1.5 rounded-lg text-xs bg-gray-100 text-gray-700"
                            >
                              {hiddenProducts[product.id] ? <span className="inline-flex items-center gap-1"><Eye size={12} />Hiện</span> : <span className="inline-flex items-center gap-1"><EyeOff size={12} />Ẩn</span>}
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {activeTab === 'customers' && (
          <div className="space-y-4">
            <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
              <div className="relative">
                <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  value={customerQuery}
                  onChange={(e) => setCustomerQuery(e.target.value)}
                  placeholder="Tra cứu khách hàng theo tên, email, SĐT"
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                />
              </div>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-x-auto">
              {customersLoading ? (
                <div className="p-6 text-sm text-gray-600">Đang tải dữ liệu khách hàng...</div>
              ) : (
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khách hàng</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Liên hệ</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Lịch sử mua</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {filteredCustomers.map((customer) => (
                      <tr key={customer.id}>
                        <td className="px-4 py-3">
                          <p className="text-sm font-medium text-gray-900">{customer.fullName || customer.username}</p>
                          <p className="text-xs text-gray-500">@{customer.username}</p>
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-700">
                          <p>{customer.email}</p>
                          <p>{customer.phone}</p>
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-700">{customer.totalOrders || 0} đơn</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-x-auto">
              <div className="p-4 border-b border-gray-100">
                <h3 className="text-base font-semibold text-gray-900">Kiểm duyệt đánh giá</h3>
              </div>
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Sản phẩm</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khách</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Đánh giá</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                    <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {reviews.map((review) => (
                    <tr key={review.id}>
                      <td className="px-4 py-3 text-sm text-gray-900">{review.productName}</td>
                      <td className="px-4 py-3 text-sm text-gray-700">{review.customerName}</td>
                      <td className="px-4 py-3 text-sm text-gray-700">
                        <p>{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</p>
                        <p className="text-xs text-gray-500">{review.comment}</p>
                      </td>
                      <td className="px-4 py-3 text-sm">{renderStatusBadge(review.status)}</td>
                      <td className="px-4 py-3 text-right space-x-2">
                        <button onClick={() => reviewAction(review.id, 'COMPLETED')} className="px-3 py-1.5 text-xs rounded-lg bg-green-100 text-green-700 inline-flex items-center gap-1"><CheckCircle2 size={12} />Duyệt</button>
                        <button onClick={() => reviewAction(review.id, 'CANCELLED')} className="px-3 py-1.5 text-xs rounded-lg bg-red-100 text-red-700 inline-flex items-center gap-1"><XCircle size={12} />Ẩn</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        </div>
      </main>
    </div>
  );
};

export default EmployeeWorkspace;
