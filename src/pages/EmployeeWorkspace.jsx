import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Package,
  ClipboardList,
  LogOut,
  Bell,
  Search,
  CheckCircle2,
  XCircle,
  EyeOff,
  Eye,
  Users,
  Printer,
  User,
} from 'lucide-react';
import { getCurrentUser, logout, setCurrentUser } from '../api/authApi';
import { productAPI } from '../api/productApi';
import { userAPI } from '../api/userApi';
import { orderAPI } from '../api/orderApi';

const ORDER_STATUS_OPTIONS = ['ALL', 'PENDING', 'CONFIRMED', 'PACKING', 'SHIPPED', 'DELIVERED', 'CANCELED', 'REFUNDED'];

const STATUS_LABELS = {
  PENDING: 'Chờ xử lý',
  CONFIRMED: 'Đã xác nhận',
  PACKING: 'Đang đóng gói',
  SHIPPED: 'Đang giao',
  DELIVERED: 'Hoàn tất',
  COMPLETED: 'Hoàn tất',
  APPROVED: 'Đã duyệt',
  HIDDEN: 'Đã ẩn',
  CANCELED: 'Đã hủy',
  CANCELLED: 'Đã hủy',
  REFUNDED: 'Đã hoàn tiền',
};

const STATUS_STYLES = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  PACKING: 'bg-indigo-100 text-indigo-800',
  SHIPPED: 'bg-purple-100 text-purple-800',
  DELIVERED: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-green-100 text-green-800',
  APPROVED: 'bg-emerald-100 text-emerald-800',
  HIDDEN: 'bg-slate-200 text-slate-700',
  CANCELED: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-red-100 text-red-800',
  REFUNDED: 'bg-emerald-100 text-emerald-800',
};

const EmployeeWorkspace = () => {
  const navigate = useNavigate();
  const [sessionUser, setSessionUser] = useState(getCurrentUser());
  const realtimeSnapshotRef = useRef(null);
  const [activeTab, setActiveTab] = useState('dashboard');

  const [orders, setOrders] = useState([]);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [ordersError, setOrdersError] = useState('');
  const [activeOrderId, setActiveOrderId] = useState(null);
  const [orderQuery, setOrderQuery] = useState('');
  const [orderStatus, setOrderStatus] = useState('ALL');
  const [orderDate, setOrderDate] = useState('');

  const [products, setProducts] = useState([]);
  const [productsError, setProductsError] = useState('');
  const [productQuery, setProductQuery] = useState('');
  const [brandFilter, setBrandFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [visibilityFilter, setVisibilityFilter] = useState('ALL');
  const [expiryFilter, setExpiryFilter] = useState('ALL');
  const [stockDrafts, setStockDrafts] = useState({});
  const [expiryDrafts, setExpiryDrafts] = useState({});
  const [activeProductId, setActiveProductId] = useState(null);
  const [productsLoading, setProductsLoading] = useState(false);

  const [customers, setCustomers] = useState([]);
  const [customerQuery, setCustomerQuery] = useState('');
  const [customersLoading, setCustomersLoading] = useState(false);
  const [customerHistoryLoadingId, setCustomerHistoryLoadingId] = useState(null);
  const [selectedCustomerHistory, setSelectedCustomerHistory] = useState(null);
  const [customerHistoryError, setCustomerHistoryError] = useState('');

  const [reviews, setReviews] = useState([]);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewsError, setReviewsError] = useState('');
  const [activeReviewId, setActiveReviewId] = useState(null);
  const [reviewStatus, setReviewStatus] = useState('ALL');
  const [reviewQuery, setReviewQuery] = useState('');

  const [dashboardStats, setDashboardStats] = useState({
    pendingOrders: 0,
    packedToday: 0,
    lowStockProducts: 0,
    nearExpiryProducts: 0,
    outOfStockProducts: 0,
    newOrdersToday: 0,
    totalOrders: 0,
  });
  const [systemAlerts, setSystemAlerts] = useState([]);
  const [lastRealtimeCheckAt, setLastRealtimeCheckAt] = useState('');

  const [profileForm, setProfileForm] = useState(() => ({
    fullName: getCurrentUser()?.fullName || '',
    phone: getCurrentUser()?.phone || '',
  }));
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [profileSubmitting, setProfileSubmitting] = useState(false);
  const [passwordSubmitting, setPasswordSubmitting] = useState(false);
  const [profileSuccess, setProfileSuccess] = useState('');
  const [profileError, setProfileError] = useState('');

  useEffect(() => {
    void loadCustomers();
  }, []);

  useEffect(() => {
    void loadReviews();
  }, [reviewStatus, reviewQuery]);

  useEffect(() => {
    void loadProducts();
  }, [productQuery, visibilityFilter]);

  useEffect(() => {
    void loadOrders();
  }, [orderQuery, orderStatus]);

  useEffect(() => {
    let active = true;

    const refreshRealtimeSignals = async () => {
      try {
        const [orderData, productData] = await Promise.all([
          orderAPI.getEmployeeOrders(),
          productAPI.getEmployeeInventory(),
        ]);

        if (!active) {
          return;
        }

        const snapshot = buildRealtimeSnapshot(orderData || [], productData || []);
        setDashboardStats(snapshot);
        setLastRealtimeCheckAt(new Date().toISOString());

        const previous = realtimeSnapshotRef.current;
        if (previous) {
          const changes = buildRealtimeAlerts(previous, snapshot);
          if (changes.length > 0) {
            const now = new Date().toLocaleTimeString('vi-VN');
            const decorated = changes.map((text) => `${now} - ${text}`);
            setSystemAlerts((prev) => [...decorated, ...prev].slice(0, 12));
          }
        }
        realtimeSnapshotRef.current = snapshot;
      } catch {
        // Ignore polling errors to keep dashboard usable while backend recovers.
      }
    };

    void refreshRealtimeSignals();
    const intervalId = window.setInterval(() => {
      void refreshRealtimeSignals();
    }, 30000);

    return () => {
      active = false;
      window.clearInterval(intervalId);
    };
  }, []);

  const loadOrders = async () => {
    try {
      setOrdersLoading(true);
      setOrdersError('');
      const data = await orderAPI.getEmployeeOrders({
        status: orderStatus === 'ALL' ? undefined : orderStatus,
        q: orderQuery || undefined,
      });
      setOrders(data || []);
    } catch {
      setOrders([]);
      setOrdersError('Không thể tải danh sách đơn hàng.');
    } finally {
      setOrdersLoading(false);
    }
  };

  const loadProducts = async () => {
    try {
      setProductsLoading(true);
      setProductsError('');
      const data = await productAPI.getEmployeeInventory({
        q: productQuery || undefined,
        visibility: visibilityFilter === 'ALL' ? undefined : visibilityFilter,
      });
      setProducts(data || []);
    } catch {
      setProducts([]);
      setProductsError('Không thể tải dữ liệu kho và sản phẩm.');
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

  const loadReviews = async () => {
    try {
      setReviewsLoading(true);
      setReviewsError('');
      const data = await productAPI.getEmployeeReviews({
        status: reviewStatus === 'ALL' ? undefined : reviewStatus,
        q: reviewQuery || undefined,
      });
      setReviews(data || []);
    } catch {
      setReviews([]);
      setReviewsError('Không thể tải dữ liệu đánh giá khách hàng.');
    } finally {
      setReviewsLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const buildRealtimeSnapshot = (orderData, productData) => {
    const today = new Date().toISOString().slice(0, 10);
    const pendingOrders = orderData.filter((order) => String(order.status || '').toUpperCase() === 'PENDING').length;
    const packedToday = orderData.filter((order) => {
      const status = String(order.status || '').toUpperCase();
      return order.orderDate === today && ['PACKING', 'SHIPPED', 'DELIVERED'].includes(status);
    }).length;
    const newOrdersToday = orderData.filter((order) => order.orderDate === today).length;

    const lowStockProducts = productData.filter((item) => Number(item.stockQuantity || 0) <= 10).length;
    const outOfStockProducts = productData.filter((item) => Number(item.stockQuantity || 0) <= 0).length;
    const nearExpiryProducts = productData.filter((item) => {
      const days = getExpiryDays(item.expiryDate);
      return days !== null && days >= 0 && days <= 30;
    }).length;

    return {
      pendingOrders,
      packedToday,
      lowStockProducts,
      nearExpiryProducts,
      outOfStockProducts,
      newOrdersToday,
      totalOrders: orderData.length,
    };
  };

  const buildRealtimeAlerts = (prev, next) => {
    const changes = [];

    if (next.pendingOrders > prev.pendingOrders) {
      changes.push(`Có thêm ${next.pendingOrders - prev.pendingOrders} đơn hàng mới đang chờ xử lý.`);
    }
    if (next.lowStockProducts > prev.lowStockProducts) {
      changes.push(`Số sản phẩm tồn thấp tăng thêm ${next.lowStockProducts - prev.lowStockProducts}.`);
    }
    if (next.outOfStockProducts > prev.outOfStockProducts) {
      changes.push(`Có thêm ${next.outOfStockProducts - prev.outOfStockProducts} sản phẩm đã hết hàng.`);
    }
    if (next.nearExpiryProducts > prev.nearExpiryProducts) {
      changes.push(`Có thêm ${next.nearExpiryProducts - prev.nearExpiryProducts} sản phẩm sắp hết hạn.`);
    }

    return changes;
  };

  const canAdvanceOrder = (status) => ['PENDING', 'CONFIRMED', 'PACKING', 'SHIPPED'].includes(String(status || '').toUpperCase());

  const canCancelOrder = (status) => {
    const normalized = String(status || '').toUpperCase();
    return normalized !== 'DELIVERED' && normalized !== 'CANCELED' && normalized !== 'REFUNDED';
  };

  const getAdvanceActionLabel = (status) => {
    const normalized = String(status || '').toUpperCase();
    if (normalized === 'PENDING') return 'Xác nhận';
    if (normalized === 'CONFIRMED') return 'Đóng gói';
    if (normalized === 'PACKING') return 'Bàn giao vận chuyển';
    if (normalized === 'SHIPPED') return 'Hoàn tất';
    return 'Chuyển bước';
  };

  const advanceOrderStatus = async (orderId) => {
    try {
      setActiveOrderId(orderId);
      setOrdersError('');
      const updated = await orderAPI.advanceEmployeeOrderStatus(orderId);
      setOrders((prev) => prev.map((order) => (order.id === orderId ? updated : order)));
    } catch {
      setOrdersError('Không thể chuyển trạng thái đơn hàng.');
    } finally {
      setActiveOrderId(null);
    }
  };

  const cancelOrder = async (orderId) => {
    const reason = window.prompt('Nhập lý do hủy đơn (ví dụ: khách yêu cầu hủy, không liên lạc được):');
    if (!reason || !reason.trim()) {
      return;
    }

    try {
      setActiveOrderId(orderId);
      setOrdersError('');
      const updated = await orderAPI.cancelOrderByEmployee(orderId, reason.trim());
      setOrders((prev) => prev.map((order) => (order.id === orderId ? updated : order)));
    } catch {
      setOrdersError('Không thể hủy đơn hàng.');
    } finally {
      setActiveOrderId(null);
    }
  };

  const printDocument = (order, documentType) => {
    const printWindow = window.open('', '_blank', 'width=900,height=700');
    if (!printWindow) {
      setOrdersError('Không mở được cửa sổ in. Hãy kiểm tra popup blocker.');
      return;
    }

    const title = documentType === 'invoice' ? 'Hóa đơn bán hàng' : 'Phiếu đóng gói';
    const itemRows = (order.items || [])
      .map(
        (item, index) => `
          <tr>
            <td>${index + 1}</td>
            <td>${item.productName || 'N/A'}</td>
            <td>${item.quantity || 0}</td>
            <td>${(item.unitPrice || 0).toLocaleString('vi-VN')}đ</td>
            <td>${(item.lineTotal || 0).toLocaleString('vi-VN')}đ</td>
          </tr>
        `
      )
      .join('');

    const html = `
      <html>
        <head>
          <title>${title} #${order.id}</title>
          <style>
            body { font-family: Arial, sans-serif; margin: 24px; color: #111827; }
            h1 { margin-bottom: 4px; }
            .meta { margin-bottom: 16px; font-size: 14px; }
            table { border-collapse: collapse; width: 100%; margin-top: 12px; }
            th, td { border: 1px solid #d1d5db; padding: 8px; font-size: 13px; }
            th { background: #f3f4f6; text-align: left; }
            .total { margin-top: 12px; font-weight: bold; }
          </style>
        </head>
        <body>
          <h1>${title}</h1>
          <div class="meta">
            <div>Mã đơn: #${order.id}</div>
            <div>Khách hàng: ${order.customerName || 'N/A'} | SĐT: ${order.customerPhone || 'N/A'}</div>
            <div>Ngày đặt: ${order.orderDate || 'N/A'}</div>
            <div>Địa chỉ giao hàng: ${order.shippingAddress || 'N/A'}</div>
            <div>Trạng thái hiện tại: ${STATUS_LABELS[order.status] || order.status || 'N/A'}</div>
          </div>

          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Sản phẩm</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              ${itemRows || '<tr><td colspan="5">Không có dữ liệu sản phẩm</td></tr>'}
            </tbody>
          </table>
          <div class="total">Tổng tiền: ${(order.totalAmount || 0).toLocaleString('vi-VN')}đ</div>
          <script>window.onload = function() { window.print(); };</script>
        </body>
      </html>
    `;

    printWindow.document.open();
    printWindow.document.write(html);
    printWindow.document.close();
  };

  const filteredOrders = useMemo(() => {
    return orders.filter((order) => {
      const matchesDate = !orderDate || order.orderDate === orderDate;
      return matchesDate;
    });
  }, [orders, orderDate]);

  const brandOptions = useMemo(() => {
    const map = new Map();
    products.forEach((item) => {
      if (item.brand?.id && item.brand?.name) {
        map.set(String(item.brand.id), item.brand.name);
      }
    });

    return [{ id: 'ALL', name: 'Tất cả thương hiệu' }, ...Array.from(map.entries()).map(([id, name]) => ({ id, name }))];
  }, [products]);

  const categoryOptions = useMemo(() => {
    const map = new Map();
    products.forEach((item) => {
      if (item.category?.id && item.category?.name) {
        map.set(String(item.category.id), item.category.name);
      }
    });

    return [{ id: 'ALL', name: 'Tất cả danh mục' }, ...Array.from(map.entries()).map(([id, name]) => ({ id, name }))];
  }, [products]);

  const getExpiryDays = (expiryDate) => {
    if (!expiryDate) {
      return null;
    }

    const today = new Date();
    const target = new Date(expiryDate);
    today.setHours(0, 0, 0, 0);
    target.setHours(0, 0, 0, 0);
    return Math.ceil((target.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
  };

  const filteredProducts = useMemo(() => {
    return products.filter((product) => {
      const matchesBrand = brandFilter === 'ALL' || String(product.brand?.id || '') === brandFilter;
      const matchesCategory = categoryFilter === 'ALL' || String(product.category?.id || '') === categoryFilter;

      const expiryDays = getExpiryDays(product.expiryDate);
      const matchesExpiry =
        expiryFilter === 'ALL'
        || (expiryFilter === 'MISSING' && expiryDays === null)
        || (expiryFilter === 'EXPIRED' && expiryDays !== null && expiryDays < 0)
        || (expiryFilter === 'NEAR' && expiryDays !== null && expiryDays >= 0 && expiryDays <= 30)
        || (expiryFilter === 'SAFE' && expiryDays !== null && expiryDays > 30);

      return matchesBrand && matchesCategory && matchesExpiry;
    });
  }, [products, brandFilter, categoryFilter, expiryFilter]);

  const applyStockUpdate = async (product) => {
    const value = Number(stockDrafts[product.id] ?? product.stockQuantity);
    if (Number.isNaN(value) || value < 0) {
      setProductsError('Số lượng tồn kho không hợp lệ.');
      return;
    }

    const reason = window.prompt('Ghi chú cập nhật tồn kho (không bắt buộc):', '') || undefined;

    try {
      setActiveProductId(product.id);
      setProductsError('');
      const updated = await productAPI.updateStockByEmployee(product.id, value, reason);
      setProducts((prev) => prev.map((p) => (p.id === product.id ? updated : p)));
    } catch {
      setProductsError('Không thể cập nhật tồn kho.');
    } finally {
      setActiveProductId(null);
    }
  };

  const applyExpiryUpdate = async (product) => {
    const draft = expiryDrafts[product.id] ?? product.expiryDate;
    if (!draft) {
      setProductsError('Vui lòng chọn hạn sử dụng.');
      return;
    }

    const note = window.prompt('Ghi chú cập nhật hạn dùng (không bắt buộc):', '') || undefined;

    try {
      setActiveProductId(product.id);
      setProductsError('');
      const updated = await productAPI.updateExpiryByEmployee(product.id, draft, note);
      setProducts((prev) => prev.map((p) => (p.id === product.id ? updated : p)));
    } catch {
      setProductsError('Không thể cập nhật hạn sử dụng.');
    } finally {
      setActiveProductId(null);
    }
  };

  const toggleVisibility = async (product) => {
    const currentVisible = product.visible !== false;
    const reason = window.prompt(
      currentVisible
        ? 'Lý do tạm ẩn sản phẩm khỏi giao diện khách hàng:'
        : 'Lý do hiển thị lại sản phẩm:',
      ''
    ) || undefined;

    try {
      setActiveProductId(product.id);
      setProductsError('');
      const updated = await productAPI.updateVisibilityByEmployee(product.id, !currentVisible, reason);
      setProducts((prev) => prev.map((p) => (p.id === product.id ? updated : p)));
    } catch {
      setProductsError('Không thể cập nhật trạng thái hiển thị sản phẩm.');
    } finally {
      setActiveProductId(null);
    }
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

  const loadCustomerPurchaseHistory = async (customer) => {
    try {
      setCustomerHistoryLoadingId(customer.id);
      setCustomerHistoryError('');
      const history = await userAPI.getCustomerPurchaseHistory(customer.id);
      setSelectedCustomerHistory(history);
    } catch {
      setSelectedCustomerHistory(null);
      setCustomerHistoryError('Không thể tải lịch sử mua hàng của khách này.');
    } finally {
      setCustomerHistoryLoadingId(null);
    }
  };

  const saveMyProfile = async (event) => {
    event.preventDefault();
    try {
      setProfileSubmitting(true);
      setProfileError('');
      setProfileSuccess('');

      const updated = await userAPI.updateMyProfile({
        fullName: profileForm.fullName,
        phone: profileForm.phone,
      });

      const merged = {
        ...(sessionUser || {}),
        ...updated,
      };
      setCurrentUser(merged);
      setSessionUser(merged);
      setProfileSuccess('Cập nhật hồ sơ thành công.');
    } catch (error) {
      setProfileError(error?.response?.data?.message || 'Không thể cập nhật hồ sơ cá nhân.');
    } finally {
      setProfileSubmitting(false);
    }
  };

  const changeMyPassword = async (event) => {
    event.preventDefault();

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setProfileError('Xác nhận mật khẩu mới không khớp.');
      setProfileSuccess('');
      return;
    }

    try {
      setPasswordSubmitting(true);
      setProfileError('');
      setProfileSuccess('');

      const message = await userAPI.changeMyPassword(passwordForm);
      setPasswordForm({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      setProfileSuccess(message || 'Đổi mật khẩu thành công.');
    } catch (error) {
      setProfileError(error?.response?.data?.message || 'Không thể đổi mật khẩu.');
    } finally {
      setPasswordSubmitting(false);
    }
  };

  const immediateTasks = [
    dashboardStats.pendingOrders > 0
      ? `Có ${dashboardStats.pendingOrders} đơn hàng mới cần xử lý ngay.`
      : null,
    dashboardStats.lowStockProducts > 0
      ? `${dashboardStats.lowStockProducts} sản phẩm chạm ngưỡng tồn thấp, cần kiểm tra nhập kho.`
      : null,
    dashboardStats.nearExpiryProducts > 0
      ? `${dashboardStats.nearExpiryProducts} sản phẩm sắp hết hạn (<= 30 ngày), cần ưu tiên xử lý.`
      : null,
  ].filter(Boolean);

  const renderStatusBadge = (status) => (
    <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_STYLES[status] || 'bg-gray-100 text-gray-700'}`}>
      {STATUS_LABELS[status] || status}
    </span>
  );

  const reviewAction = async (reviewId, action) => {
    const reasonPrompt = action === 'HIDE'
      ? 'Lý do ẩn bình luận (không bắt buộc):'
      : 'Ghi chú duyệt bình luận (không bắt buộc):';
    const reason = window.prompt(reasonPrompt, '') || undefined;

    try {
      setActiveReviewId(reviewId);
      setReviewsError('');
      const updated = await productAPI.moderateReviewByEmployee(reviewId, action, reason);
      setReviews((prev) => prev.map((item) => (item.id === reviewId ? updated : item)));
    } catch {
      setReviewsError('Không thể cập nhật trạng thái kiểm duyệt đánh giá.');
    } finally {
      setActiveReviewId(null);
    }
  };

  const tabs = [
    { key: 'dashboard', label: 'Dashboard', icon: Bell },
    { key: 'orders', label: 'Đơn hàng', icon: ClipboardList },
    { key: 'inventory', label: 'Kho & Sản phẩm', icon: Package },
    { key: 'customers', label: 'Khách hàng & Đánh giá', icon: Users },
    { key: 'profile', label: 'Hồ sơ cá nhân', icon: User },
  ];

  return (
    <div className="min-h-screen bg-gray-50 flex">
      <aside className="w-72 bg-white border-r border-gray-200 shadow-sm hidden md:flex md:flex-col">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">Khu vực nhân viên</h1>
          <p className="text-sm text-gray-600 mt-2">
            Xin chào, {sessionUser?.fullName || sessionUser?.username}
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
              {sessionUser?.fullName || sessionUser?.username}
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
                <p className="text-3xl font-semibold mt-1 text-gray-900">{dashboardStats.pendingOrders}</p>
              </div>
              <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                <p className="text-sm text-gray-500">Đơn đã xử lý hôm nay</p>
                <p className="text-3xl font-semibold mt-1 text-gray-900">{dashboardStats.packedToday}</p>
              </div>
              <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                <p className="text-sm text-gray-500">Sản phẩm tồn kho thấp</p>
                <p className="text-3xl font-semibold mt-1 text-gray-900">{dashboardStats.lowStockProducts}</p>
              </div>
            </div>

            <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Việc cần làm ngay</h3>
              {immediateTasks.length === 0 ? (
                <p className="text-sm text-gray-600">Hiện không có việc gấp cần xử lý.</p>
              ) : (
                <ul className="space-y-2">
                  {immediateTasks.map((task) => (
                    <li key={task} className="text-sm text-gray-700 flex items-start gap-2">
                      <Bell size={16} className="mt-0.5 text-amber-500" />
                      <span>{task}</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Thông báo hệ thống</h3>
              <p className="text-xs text-gray-500 mb-3">
                Cập nhật tự động mỗi 30 giây{lastRealtimeCheckAt ? `, lần gần nhất: ${new Date(lastRealtimeCheckAt).toLocaleTimeString('vi-VN')}` : ''}
              </p>
              {systemAlerts.length === 0 ? (
                <p className="text-sm text-gray-600">Chưa phát hiện biến động mới về kho hoặc đơn hàng.</p>
              ) : (
                <ul className="space-y-2">
                  {systemAlerts.map((note) => (
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

        {activeTab === 'profile' && (
          <div className="space-y-4">
            {profileError && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {profileError}
              </div>
            )}

            {profileSuccess && (
              <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                {profileSuccess}
              </div>
            )}

            <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Cập nhật hồ sơ cá nhân</h3>
              <form onSubmit={saveMyProfile} className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <input
                  value={profileForm.fullName}
                  onChange={(e) => setProfileForm((prev) => ({ ...prev, fullName: e.target.value }))}
                  placeholder="Họ tên"
                  className="px-3 py-2 border border-gray-300 rounded-lg"
                />
                <input
                  value={profileForm.phone}
                  onChange={(e) => setProfileForm((prev) => ({ ...prev, phone: e.target.value }))}
                  placeholder="Số điện thoại"
                  className="px-3 py-2 border border-gray-300 rounded-lg"
                />
                <div className="md:col-span-2">
                  <button
                    type="submit"
                    disabled={profileSubmitting}
                    className="px-4 py-2 rounded-lg bg-slate-900 text-white disabled:bg-gray-300"
                  >
                    {profileSubmitting ? 'Đang lưu...' : 'Lưu hồ sơ'}
                  </button>
                </div>
              </form>
            </div>

            <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Đổi mật khẩu</h3>
              <form onSubmit={changeMyPassword} className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <input
                  type="password"
                  value={passwordForm.currentPassword}
                  onChange={(e) => setPasswordForm((prev) => ({ ...prev, currentPassword: e.target.value }))}
                  placeholder="Mật khẩu hiện tại"
                  className="px-3 py-2 border border-gray-300 rounded-lg"
                />
                <input
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={(e) => setPasswordForm((prev) => ({ ...prev, newPassword: e.target.value }))}
                  placeholder="Mật khẩu mới"
                  className="px-3 py-2 border border-gray-300 rounded-lg"
                />
                <input
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(e) => setPasswordForm((prev) => ({ ...prev, confirmPassword: e.target.value }))}
                  placeholder="Xác nhận mật khẩu mới"
                  className="px-3 py-2 border border-gray-300 rounded-lg"
                />
                <div className="md:col-span-2">
                  <button
                    type="submit"
                    disabled={passwordSubmitting}
                    className="px-4 py-2 rounded-lg bg-slate-900 text-white disabled:bg-gray-300"
                  >
                    {passwordSubmitting ? 'Đang đổi...' : 'Đổi mật khẩu'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {activeTab === 'orders' && (
          <div className="space-y-4">
            {ordersError && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {ordersError}
              </div>
            )}

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
                {ORDER_STATUS_OPTIONS.map((s) => (
                  <option key={s} value={s}>{s === 'ALL' ? 'Tất cả trạng thái' : STATUS_LABELS[s]}</option>
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
              {ordersLoading ? (
                <div className="p-6 text-sm text-gray-600">Đang tải dữ liệu đơn hàng...</div>
              ) : (
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã đơn</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Khách hàng</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">SĐT</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày đặt</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Địa chỉ</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tổng tiền</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                      <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {filteredOrders.map((order) => (
                      <tr key={order.id}>
                        <td className="px-4 py-3 text-sm font-medium text-gray-900">#{order.id}</td>
                        <td className="px-4 py-3 text-sm text-gray-700">{order.customerName || 'N/A'}</td>
                        <td className="px-4 py-3 text-sm text-gray-700">{order.customerPhone || 'N/A'}</td>
                        <td className="px-4 py-3 text-sm text-gray-700">{order.orderDate || 'N/A'}</td>
                        <td className="px-4 py-3 text-sm text-gray-700">{order.shippingAddress || 'N/A'}</td>
                        <td className="px-4 py-3 text-sm text-gray-900">{(order.totalAmount || 0).toLocaleString('vi-VN')}đ</td>
                        <td className="px-4 py-3">{renderStatusBadge(order.status)}</td>
                        <td className="px-4 py-3 text-right space-x-2">
                          <button
                            disabled={!canAdvanceOrder(order.status) || activeOrderId === order.id}
                            onClick={() => advanceOrderStatus(order.id)}
                            className="px-3 py-1.5 rounded-lg text-xs bg-slate-900 text-white disabled:bg-gray-300"
                          >
                            {getAdvanceActionLabel(order.status)}
                          </button>
                          <button
                            disabled={!canCancelOrder(order.status) || activeOrderId === order.id}
                            onClick={() => cancelOrder(order.id)}
                            className="px-3 py-1.5 rounded-lg text-xs bg-red-100 text-red-700 disabled:bg-gray-100 disabled:text-gray-400"
                          >
                            Hủy đơn
                          </button>
                          <button
                            onClick={() => printDocument(order, 'invoice')}
                            className="px-3 py-1.5 rounded-lg text-xs bg-emerald-100 text-emerald-700 inline-flex items-center gap-1"
                          >
                            <Printer size={12} /> In hóa đơn
                          </button>
                          <button
                            onClick={() => printDocument(order, 'packing-slip')}
                            className="px-3 py-1.5 rounded-lg text-xs bg-indigo-100 text-indigo-700 inline-flex items-center gap-1"
                          >
                            <Printer size={12} /> In phiếu gói
                          </button>
                        </td>
                      </tr>
                    ))}
                    {filteredOrders.length === 0 && (
                      <tr>
                        <td colSpan={8} className="px-4 py-6 text-sm text-gray-500 text-center">Không có đơn hàng phù hợp.</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {activeTab === 'inventory' && (
          <div className="space-y-4">
            {productsError && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {productsError}
              </div>
            )}

            <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 grid grid-cols-1 md:grid-cols-5 gap-3">
              <div className="relative md:col-span-2">
                <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input
                  value={productQuery}
                  onChange={(e) => setProductQuery(e.target.value)}
                  placeholder="Tìm theo tên/mã/brand/category"
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                />
              </div>
              <select
                value={brandFilter}
                onChange={(e) => setBrandFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              >
                {brandOptions.map((item) => (
                  <option key={item.id} value={item.id}>{item.name}</option>
                ))}
              </select>

              <select
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              >
                {categoryOptions.map((item) => (
                  <option key={item.id} value={item.id}>{item.name}</option>
                ))}
              </select>

              <select
                value={visibilityFilter}
                onChange={(e) => setVisibilityFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              >
                <option value="ALL">Hiển thị: Tất cả</option>
                <option value="visible">Hiển thị: Đang bán</option>
                <option value="hidden">Hiển thị: Đang ẩn</option>
              </select>

              <select
                value={expiryFilter}
                onChange={(e) => setExpiryFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
              >
                <option value="ALL">Hạn dùng: Tất cả</option>
                <option value="MISSING">Thiếu hạn dùng</option>
                <option value="EXPIRED">Đã hết hạn</option>
                <option value="NEAR">Sắp hết hạn (≤30 ngày)</option>
                <option value="SAFE">An toàn (&gt;30 ngày)</option>
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
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Danh mục</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tồn kho</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Hạn dùng</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Hiển thị</th>
                      <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {filteredProducts.map((product) => {
                      const expiryDays = getExpiryDays(product.expiryDate);
                      const lowStock = (product.stockQuantity || 0) <= 10;
                      const isVisible = product.visible !== false;
                      return (
                        <tr key={product.id} className={!isVisible ? 'opacity-60' : ''}>
                          <td className="px-4 py-3">
                            <p className="text-sm font-medium text-gray-900">{product.productName || product.name}</p>
                            <p className="text-xs text-gray-500">ID: {product.id}</p>
                          </td>
                          <td className="px-4 py-3 text-sm text-gray-700">{product.brand?.name || 'N/A'}</td>
                          <td className="px-4 py-3 text-sm text-gray-700">{product.category?.name || 'N/A'}</td>
                          <td className="px-4 py-3">
                            <span className={`inline-flex px-2 py-0.5 rounded-full text-xs ${lowStock ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
                              {product.stockQuantity}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-sm">
                            {!product.expiryDate ? (
                              <span className="text-gray-500">Chưa khai báo</span>
                            ) : expiryDays < 0 ? (
                              <span className="text-red-700">Đã hết hạn</span>
                            ) : expiryDays <= 30 ? (
                              <span className="text-amber-700">Còn {expiryDays} ngày</span>
                            ) : (
                              <span className="text-gray-700">Còn {expiryDays} ngày</span>
                            )}
                          </td>
                          <td className="px-4 py-3 text-sm">
                            <span className={`inline-flex px-2 py-0.5 rounded-full text-xs ${isVisible ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-700'}`}>
                              {isVisible ? 'Đang hiển thị' : 'Đang ẩn'}
                            </span>
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
                              onClick={() => applyStockUpdate(product)}
                              disabled={activeProductId === product.id}
                              className="px-3 py-1.5 rounded-lg text-xs bg-slate-900 text-white disabled:bg-gray-300"
                            >
                              Lưu tồn
                            </button>

                            <input
                              type="date"
                              value={expiryDrafts[product.id] ?? (product.expiryDate || '')}
                              onChange={(e) => setExpiryDrafts((prev) => ({ ...prev, [product.id]: e.target.value }))}
                              className="w-36 px-2 py-1.5 border border-gray-300 rounded text-sm"
                            />
                            <button
                              onClick={() => applyExpiryUpdate(product)}
                              disabled={activeProductId === product.id}
                              className="px-3 py-1.5 rounded-lg text-xs bg-amber-100 text-amber-800 disabled:bg-gray-100 disabled:text-gray-400"
                            >
                              Lưu hạn
                            </button>
                            <button
                              onClick={() => toggleVisibility(product)}
                              disabled={activeProductId === product.id}
                              className="px-3 py-1.5 rounded-lg text-xs bg-gray-100 text-gray-700 disabled:bg-gray-100 disabled:text-gray-400"
                            >
                              {isVisible ? <span className="inline-flex items-center gap-1"><EyeOff size={12} />Ẩn nhanh</span> : <span className="inline-flex items-center gap-1"><Eye size={12} />Hiện lại</span>}
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                    {filteredProducts.length === 0 && (
                      <tr>
                        <td colSpan={7} className="px-4 py-6 text-sm text-gray-500 text-center">Không có sản phẩm phù hợp.</td>
                      </tr>
                    )}
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

            {customerHistoryError && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {customerHistoryError}
              </div>
            )}

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
                      <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Thao tác</th>
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
                        <td className="px-4 py-3 text-right">
                          <button
                            onClick={() => loadCustomerPurchaseHistory(customer)}
                            disabled={customerHistoryLoadingId === customer.id}
                            className="px-3 py-1.5 rounded-lg text-xs bg-slate-900 text-white disabled:bg-gray-300"
                          >
                            {customerHistoryLoadingId === customer.id ? 'Đang tải...' : 'Xem lịch sử'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            {selectedCustomerHistory && (
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-x-auto">
                <div className="p-4 border-b border-gray-100 flex items-center justify-between">
                  <div>
                    <h3 className="text-base font-semibold text-gray-900">Lịch sử mua hàng khách</h3>
                    <p className="text-xs text-gray-500 mt-1">
                      {selectedCustomerHistory.fullName || selectedCustomerHistory.username} | {selectedCustomerHistory.email || 'N/A'} | {selectedCustomerHistory.phone || 'N/A'}
                    </p>
                  </div>
                  <button
                    onClick={() => setSelectedCustomerHistory(null)}
                    className="px-3 py-1.5 rounded-lg text-xs bg-gray-100 text-gray-700"
                  >
                    Đóng
                  </button>
                </div>
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Mã đơn</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ngày đặt</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tổng tiền</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Địa chỉ</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Số sản phẩm</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {(selectedCustomerHistory.orders || []).map((order) => (
                      <tr key={order.id}>
                        <td className="px-4 py-3 text-sm font-medium text-gray-900">#{order.id}</td>
                        <td className="px-4 py-3 text-sm text-gray-700">{order.orderDate || 'N/A'}</td>
                        <td className="px-4 py-3 text-sm text-gray-700">{(order.totalAmount || 0).toLocaleString('vi-VN')}đ</td>
                        <td className="px-4 py-3 text-sm">{renderStatusBadge(order.status)}</td>
                        <td className="px-4 py-3 text-sm text-gray-700">{order.shippingAddress || 'N/A'}</td>
                        <td className="px-4 py-3 text-sm text-gray-700">{order.totalItems || 0}</td>
                      </tr>
                    ))}
                    {(selectedCustomerHistory.orders || []).length === 0 && (
                      <tr>
                        <td colSpan={6} className="px-4 py-6 text-sm text-gray-500 text-center">Khách này chưa có đơn hàng nào.</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            )}

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-x-auto">
              <div className="p-4 border-b border-gray-100">
                <h3 className="text-base font-semibold text-gray-900">Kiểm duyệt đánh giá</h3>
              </div>
              <div className="p-4 border-b border-gray-100 grid grid-cols-1 md:grid-cols-3 gap-3">
                <div className="relative md:col-span-2">
                  <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    value={reviewQuery}
                    onChange={(e) => setReviewQuery(e.target.value)}
                    placeholder="Tìm theo sản phẩm, khách, email, SĐT, nội dung"
                    className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                  />
                </div>
                <select
                  value={reviewStatus}
                  onChange={(e) => setReviewStatus(e.target.value)}
                  className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
                >
                  <option value="ALL">Tất cả trạng thái</option>
                  <option value="PENDING">Chờ duyệt</option>
                  <option value="APPROVED">Đã duyệt</option>
                  <option value="HIDDEN">Đã ẩn</option>
                </select>
              </div>
              {reviewsError && (
                <div className="mx-4 my-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                  {reviewsError}
                </div>
              )}
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
                  {reviewsLoading ? (
                    <tr>
                      <td colSpan={5} className="px-4 py-6 text-sm text-gray-500 text-center">Đang tải đánh giá...</td>
                    </tr>
                  ) : reviews.map((review) => {
                    const rating = Number(review.rating || 0);
                    return (
                    <tr key={review.id}>
                      <td className="px-4 py-3 text-sm text-gray-900">{review.productName}</td>
                      <td className="px-4 py-3 text-sm text-gray-700">
                        <p>{review.customerName || 'N/A'}</p>
                        <p className="text-xs text-gray-500">{review.customerEmail || 'N/A'} | {review.customerPhone || 'N/A'}</p>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-700">
                        <p>{'★'.repeat(rating)}{'☆'.repeat(Math.max(0, 5 - rating))}</p>
                        <p className="text-xs text-gray-500">{review.comment}</p>
                      </td>
                      <td className="px-4 py-3 text-sm">{renderStatusBadge(review.moderationStatus)}</td>
                      <td className="px-4 py-3 text-right space-x-2">
                        <button
                          onClick={() => reviewAction(review.id, 'APPROVE')}
                          disabled={activeReviewId === review.id}
                          className="px-3 py-1.5 text-xs rounded-lg bg-green-100 text-green-700 inline-flex items-center gap-1 disabled:bg-gray-100 disabled:text-gray-400"
                        >
                          <CheckCircle2 size={12} />Duyệt
                        </button>
                        <button
                          onClick={() => reviewAction(review.id, 'HIDE')}
                          disabled={activeReviewId === review.id}
                          className="px-3 py-1.5 text-xs rounded-lg bg-red-100 text-red-700 inline-flex items-center gap-1 disabled:bg-gray-100 disabled:text-gray-400"
                        >
                          <XCircle size={12} />Ẩn
                        </button>
                      </td>
                    </tr>
                    );
                  })}
                  {!reviewsLoading && reviews.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-4 py-6 text-sm text-gray-500 text-center">Không có đánh giá phù hợp bộ lọc.</td>
                    </tr>
                  )}
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
