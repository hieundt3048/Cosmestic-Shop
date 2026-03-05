import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Activity, Package, ShoppingCart, TrendingUp, UserCheck, Users } from 'lucide-react';
import { reportAPI } from '../api/reportApi';

const AdminOverview = () => {
  const [revenueRange, setRevenueRange] = useState('month');
  const [revenuePoints, setRevenuePoints] = useState([]);
  const [revenueTotal, setRevenueTotal] = useState(0);
  const [orderKpis, setOrderKpis] = useState({
    success: 0,
    cancelled: 0,
    conversionRate: 0,
  });
  const [topProducts, setTopProducts] = useState([]);
  const [activeUsers, setActiveUsers] = useState(0);

  const traffic = {
    todayVisits: 1842,
    avgSessionMinutes: 6.4,
  };

  useEffect(() => {
    const loadReportData = async () => {
      try {
        const [revenueData, orderData, topProductData] = await Promise.all([
          reportAPI.getRevenueStatistics(revenueRange),
          reportAPI.getOrderKpis(revenueRange),
          reportAPI.getTopProducts(revenueRange, 5),
        ]);

        setRevenuePoints(revenueData?.points || []);
        setRevenueTotal(revenueData?.totalRevenue || 0);
        setOrderKpis({
          success: orderData?.successfulOrders || 0,
          cancelled: orderData?.canceledOrders || 0,
          conversionRate: orderData?.conversionRate || 0,
        });
        setTopProducts(topProductData || []);
      } catch (error) {
        setRevenuePoints([]);
        setRevenueTotal(0);
        setOrderKpis({ success: 0, cancelled: 0, conversionRate: 0 });
        setTopProducts([]);
      }
    };

    loadReportData();
  }, [revenueRange]);

  useEffect(() => {
    const loadActiveUsers = async () => {
      try {
        const trafficData = await reportAPI.getActiveUsers();
        setActiveUsers(trafficData?.activeUsers || 0);
      } catch {
        setActiveUsers(0);
      }
    };

    loadActiveUsers();
    const intervalId = window.setInterval(loadActiveUsers, 30000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, []);

  const maxRevenue = useMemo(() => {
    if (revenuePoints.length === 0) {
      return 1;
    }
    return Math.max(...revenuePoints.map((point) => point.revenue || 0), 1);
  }, [revenuePoints]);

  const stats = [
    {
      title: 'Tổng sản phẩm',
      value: '158',
      icon: Package,
      color: 'bg-slate-900',
      link: '/admin/products'
    },
    {
      title: 'Đơn thành công',
      value: orderKpis.success.toLocaleString('vi-VN'),
      icon: ShoppingCart,
      color: 'bg-emerald-600',
      link: '/admin/orders'
    },
    {
      title: 'Người dùng hoạt động',
      value: activeUsers.toLocaleString('vi-VN'),
      icon: UserCheck,
      color: 'bg-indigo-600',
      link: '/admin/user-management'
    },
    {
      title: 'Doanh thu',
      value: `${revenueTotal.toLocaleString('vi-VN')}đ`,
      icon: TrendingUp,
      color: 'bg-orange-600',
      link: '/admin/financial-reports'
    },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-semibold text-gray-900 mb-2">Tổng quan</h1>
        <p className="text-gray-600">Thống kê và quản lý hệ thống cosmetic shop</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {stats.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <Link
              key={index}
              to={stat.link}
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <div className={`${stat.color} p-3 rounded-lg`}>
                  <Icon className="text-white" size={24} />
                </div>
              </div>
              <h3 className="text-gray-600 text-sm mb-1">{stat.title}</h3>
              <p className="text-2xl font-semibold text-gray-900">{stat.value}</p>
            </Link>
          );
        })}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-8">
        <div className="xl:col-span-2 bg-white rounded-lg shadow-sm p-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-5">
            <h2 className="text-xl font-semibold text-gray-900">Thống kê doanh thu</h2>
            <select
              value={revenueRange}
              onChange={(e) => setRevenueRange(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            >
              <option value="day">Theo ngày</option>
              <option value="week">Theo tuần</option>
              <option value="month">Theo tháng</option>
              <option value="year">Theo năm</option>
            </select>
          </div>

          <div className="h-56 flex items-end gap-2">
            {revenuePoints.map((point, index) => (
              <div key={`${revenueRange}-${point.label}-${index}`} className="flex-1 flex flex-col justify-end items-center">
                <div
                  className="w-full bg-slate-900/85 rounded-t-md"
                  style={{ height: `${((point.revenue || 0) / maxRevenue) * 100}%`, minHeight: '8px' }}
                />
                <span className="text-[10px] text-gray-500 mt-2">{point.label || index + 1}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-5">Chỉ số đơn hàng</h2>
          <div className="space-y-4">
            <div className="p-4 rounded-lg bg-emerald-50 border border-emerald-100">
              <p className="text-sm text-gray-600">Đơn thành công</p>
              <p className="text-2xl font-semibold text-emerald-700">{orderKpis.success.toLocaleString('vi-VN')}</p>
            </div>
            <div className="p-4 rounded-lg bg-red-50 border border-red-100">
              <p className="text-sm text-gray-600">Đơn bị hủy</p>
              <p className="text-2xl font-semibold text-red-700">{orderKpis.cancelled.toLocaleString('vi-VN')}</p>
            </div>
            <div className="p-4 rounded-lg bg-indigo-50 border border-indigo-100">
              <p className="text-sm text-gray-600">Tỷ lệ chuyển đổi</p>
              <p className="text-2xl font-semibold text-indigo-700">{orderKpis.conversionRate.toFixed(2)}%</p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-8">
        <div className="xl:col-span-2 bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Sản phẩm bán chạy</h2>
          {topProducts.length === 0 ? (
            <p className="text-gray-500">Chưa có dữ liệu sản phẩm bán chạy trong kỳ.</p>
          ) : (
            <div className="space-y-4">
              {topProducts.map((product, index) => (
                <div key={product.productId || `${product.productName}-${index}`}>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-700">#{index + 1} {product.productName}</span>
                    <span className="font-medium text-gray-900">{product.totalSold} lượt bán</span>
                  </div>
                  <div className="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-slate-900"
                      style={{ width: `${(product.totalSold / (topProducts[0]?.totalSold || 1)) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Lưu lượng truy cập</h2>
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-md bg-green-100 text-green-700">
                <Activity size={18} />
              </div>
              <div>
                <p className="text-sm text-gray-600">Người dùng đang hoạt động</p>
                <p className="text-xl font-semibold text-gray-900">{activeUsers.toLocaleString('vi-VN')}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-md bg-blue-100 text-blue-700">
                <Users size={18} />
              </div>
              <div>
                <p className="text-sm text-gray-600">Lượt truy cập hôm nay</p>
                <p className="text-xl font-semibold text-gray-900">{traffic.todayVisits.toLocaleString('vi-VN')}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-md bg-purple-100 text-purple-700">
                <TrendingUp size={18} />
              </div>
              <div>
                <p className="text-sm text-gray-600">Thời gian phiên trung bình</p>
                <p className="text-xl font-semibold text-gray-900">{traffic.avgSessionMinutes} phút</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Thao tác nhanh</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Link
            to="/admin/add-product"
            className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-slate-900 hover:bg-gray-50 transition-all text-center"
          >
            <Package className="mx-auto mb-2 text-gray-600" size={32} />
            <p className="font-medium text-gray-900">Thêm sản phẩm mới</p>
          </Link>
          
          <Link
            to="/admin/orders"
            className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-slate-900 hover:bg-gray-50 transition-all text-center"
          >
            <ShoppingCart className="mx-auto mb-2 text-gray-600" size={32} />
            <p className="font-medium text-gray-900">Quản lý đơn hàng</p>
          </Link>
          
          <Link
            to="/admin/customers"
            className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-slate-900 hover:bg-gray-50 transition-all text-center"
          >
            <Users className="mx-auto mb-2 text-gray-600" size={32} />
            <p className="font-medium text-gray-900">Quản lý khách hàng</p>
          </Link>

          <Link
            to="/admin/user-management"
            className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-slate-900 hover:bg-gray-50 transition-all text-center"
          >
            <UserCheck className="mx-auto mb-2 text-gray-600" size={32} />
            <p className="font-medium text-gray-900">Phân quyền & tài khoản</p>
          </Link>

          <Link
            to="/admin/financial-reports"
            className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-slate-900 hover:bg-gray-50 transition-all text-center"
          >
            <TrendingUp className="mx-auto mb-2 text-gray-600" size={32} />
            <p className="font-medium text-gray-900">Báo cáo tài chính</p>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AdminOverview;
