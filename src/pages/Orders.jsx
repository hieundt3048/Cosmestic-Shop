import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Search, Eye, Package } from 'lucide-react';

const Orders = () => {
  const [orders, setOrders] = useState([]);
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [activeTab, setActiveTab] = useState('orders');

  // Mock data - Replace with API call
  useEffect(() => {
    // TODO: Replace with actual API call
    // const data = await orderAPI.getAllOrders();
    setOrders([
      {
        id: 1,
        orderDate: '2024-02-28',
        customerName: 'Nguyễn Văn A',
        totalAmount: 1500000,
        status: 'PENDING',
        items: 3
      },
      {
        id: 2,
        orderDate: '2024-02-27',
        customerName: 'Trần Thị B',
        totalAmount: 850000,
        status: 'DELIVERED',
        items: 2
      },
    ]);

    setComplaints([
      { id: 1, orderId: 1201, customerName: 'Lê Thu Hà', reason: 'Sản phẩm lỗi vòi bơm', status: 'PENDING' },
      { id: 2, orderId: 1193, customerName: 'Võ Minh Khang', reason: 'Giao sai sản phẩm', status: 'PENDING' },
    ]);
  }, []);

  const getStatusBadge = (status) => {
    const statusConfig = {
      PENDING: { label: 'Chờ xử lý', className: 'bg-yellow-100 text-yellow-800' },
      PROCESSING: { label: 'Đang xử lý', className: 'bg-blue-100 text-blue-800' },
      SHIPPING: { label: 'Đang giao', className: 'bg-purple-100 text-purple-800' },
      DELIVERED: { label: 'Đã giao', className: 'bg-green-100 text-green-800' },
      CANCELLED: { label: 'Đã hủy', className: 'bg-red-100 text-red-800' },
    };
    const config = statusConfig[status] || statusConfig.PENDING;
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.className}`}>
        {config.label}
      </span>
    );
  };

  const filteredOrders = orders.filter(order => {
    const matchSearch = order.customerName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
                       order.id.toString().includes(searchQuery);
    const matchStatus = filterStatus === 'all' || order.status === filterStatus;
    return matchSearch && matchStatus;
  });

  const resolveComplaint = (complaintId, nextStatus) => {
    setComplaints((prev) =>
      prev.map((item) => (item.id === complaintId ? { ...item, status: nextStatus } : item))
    );
  };

  return (
    <div>
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Quản lý đơn hàng</h1>
        <p className="text-gray-600">Theo dõi và quản lý đơn hàng của khách hàng</p>
      </div>

      <div className="mb-4 inline-flex rounded-lg border border-gray-200 bg-white p-1">
        <button
          onClick={() => setActiveTab('orders')}
          className={`px-4 py-2 rounded-md text-sm ${
            activeTab === 'orders' ? 'bg-slate-900 text-white' : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          Tất cả đơn hàng
        </button>
        <button
          onClick={() => setActiveTab('complaints')}
          className={`px-4 py-2 rounded-md text-sm ${
            activeTab === 'complaints' ? 'bg-slate-900 text-white' : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          Khiếu nại / Hoàn tiền
        </button>
      </div>

      {/* Filters */}
      {activeTab === 'orders' && (
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Search */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Tìm kiếm đơn hàng, khách hàng..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            />
          </div>
          
          {/* Status Filter */}
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="PENDING">Chờ xử lý</option>
            <option value="PROCESSING">Đang xử lý</option>
            <option value="SHIPPING">Đang giao</option>
            <option value="DELIVERED">Đã giao</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>
      </div>
      )}

      {/* Orders Table */}
      {activeTab === 'orders' ? (
      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-slate-900"></div>
            <p className="mt-2 text-gray-600">Đang tải...</p>
          </div>
        ) : filteredOrders.length === 0 ? (
          <div className="p-8 text-center text-gray-500">
            <ShoppingCart className="mx-auto mb-2 text-gray-400" size={48} />
            <p>Chưa có đơn hàng nào</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Mã đơn
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Khách hàng
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ngày đặt
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Số lượng
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tổng tiền
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Trạng thái
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Thao tác
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredOrders.map((order) => (
                  <tr key={order.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">
                      #{order.id}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      {order.customerName}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {new Date(order.orderDate).toLocaleDateString('vi-VN')}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      <span className="inline-flex items-center">
                        <Package size={16} className="mr-1" />
                        {order.items} sản phẩm
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">
                      {order.totalAmount?.toLocaleString('vi-VN')}đ
                    </td>
                    <td className="px-6 py-4 text-sm">
                      {getStatusBadge(order.status)}
                    </td>
                    <td className="px-6 py-4 text-right text-sm">
                      <button
                        className="text-blue-600 hover:text-blue-900 inline-flex items-center"
                        title="Xem chi tiết"
                      >
                        <Eye size={18} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      ) : (
      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {complaints.length === 0 ? (
          <div className="p-8 text-center text-gray-500">Không có yêu cầu khiếu nại nào.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mã yêu cầu</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Đơn hàng</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Khách hàng</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Lý do</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Quyết định</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {complaints.map((item) => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm text-gray-900">#{item.id}</td>
                    <td className="px-6 py-4 text-sm text-gray-700">#{item.orderId}</td>
                    <td className="px-6 py-4 text-sm text-gray-700">{item.customerName}</td>
                    <td className="px-6 py-4 text-sm text-gray-700">{item.reason}</td>
                    <td className="px-6 py-4 text-sm">
                      <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                        item.status === 'APPROVED' ? 'bg-emerald-100 text-emerald-700' : item.status === 'REJECTED' ? 'bg-red-100 text-red-700' : 'bg-yellow-100 text-yellow-700'
                      }`}>
                        {item.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="inline-flex gap-2">
                        <button
                          onClick={() => resolveComplaint(item.id, 'APPROVED')}
                          className="px-3 py-1.5 text-xs rounded-lg bg-emerald-50 text-emerald-700 hover:bg-emerald-100"
                        >
                          Hoàn tiền
                        </button>
                        <button
                          onClick={() => resolveComplaint(item.id, 'REJECTED')}
                          className="px-3 py-1.5 text-xs rounded-lg bg-red-50 text-red-700 hover:bg-red-100"
                        >
                          Từ chối
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      )}

      {/* Stats Footer */}
      <div className="mt-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
        {!loading && filteredOrders.length > 0 && activeTab === 'orders' && (
          <div className="text-sm text-gray-600">
            Hiển thị {filteredOrders.length} / {orders.length} đơn hàng
          </div>
        )}
        <Link to="/admin/financial-reports" className="text-sm text-slate-700 hover:text-slate-900">
          Xem báo cáo tài chính →
        </Link>
      </div>
    </div>
  );
};

export default Orders;
