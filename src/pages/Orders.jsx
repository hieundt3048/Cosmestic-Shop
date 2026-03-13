import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Package, Search, ShoppingCart } from 'lucide-react';
import { orderAPI } from '../api/orderApi';

const ORDER_STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Chờ xử lý' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'PACKING', label: 'Đang đóng gói' },
  { value: 'SHIPPED', label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELED', label: 'Đã hủy' },
  { value: 'REFUNDED', label: 'Đã hoàn tiền' },
];

const COMPLAINT_DECISION_OPTIONS = [
  { value: 'all', label: 'Tất cả' },
  { value: 'PENDING', label: 'Đang chờ' },
  { value: 'REFUND_APPROVED', label: 'Duyệt hoàn tiền' },
  { value: 'CANCEL_APPROVED', label: 'Duyệt hủy đơn' },
  { value: 'REJECTED', label: 'Đã từ chối' },
];

const Orders = () => {
  const [orders, setOrders] = useState([]);
  const [complaints, setComplaints] = useState([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [loadingComplaints, setLoadingComplaints] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [complaintDecision, setComplaintDecision] = useState('all');
  const [activeTab, setActiveTab] = useState('orders');
  const [error, setError] = useState('');

  useEffect(() => {
    void loadOrders();
  }, [filterStatus, searchQuery]);

  useEffect(() => {
    if (activeTab !== 'complaints') {
      return;
    }
    void loadComplaints();
  }, [activeTab, complaintDecision, searchQuery]);

  const loadOrders = async () => {
    try {
      setLoadingOrders(true);
      setError('');
      const data = await orderAPI.getAllOrders({
        status: filterStatus === 'all' ? undefined : filterStatus,
        q: searchQuery || undefined,
      });
      setOrders(data || []);
    } catch (err) {
      setOrders([]);
      setError('Không thể tải danh sách đơn hàng.');
    } finally {
      setLoadingOrders(false);
    }
  };

  const loadComplaints = async () => {
    try {
      setLoadingComplaints(true);
      setError('');
      const data = await orderAPI.getComplaints({
        decision: complaintDecision === 'all' ? undefined : complaintDecision,
        q: searchQuery || undefined,
      });
      setComplaints(data || []);
    } catch (err) {
      setComplaints([]);
      setError('Không thể tải danh sách khiếu nại/hoàn tiền.');
    } finally {
      setLoadingComplaints(false);
    }
  };

  const updateOrderStatus = async (orderId, status, reason = '') => {
    try {
      await orderAPI.updateOrderStatus(orderId, status, reason);
      await loadOrders();
    } catch (err) {
      setError('Không thể cập nhật trạng thái đơn hàng.');
    }
  };

  const createComplaint = async (orderId) => {
    const reason = window.prompt('Nhập lý do khiếu nại/hoàn tiền:');
    if (!reason || !reason.trim()) {
      return;
    }

    const requestedRefundAmountInput = window.prompt('Số tiền đề xuất hoàn (để trống nếu chưa xác định):', '0');
    if (requestedRefundAmountInput === null) {
      return;
    }

    const requestedRefundAmount = Number(requestedRefundAmountInput || 0);
    if (Number.isNaN(requestedRefundAmount) || requestedRefundAmount < 0) {
      setError('Số tiền hoàn đề xuất không hợp lệ.');
      return;
    }

    try {
      await orderAPI.createComplaint(orderId, { reason: reason.trim(), requestedRefundAmount });
      setActiveTab('complaints');
      await loadComplaints();
    } catch (err) {
      setError('Không thể tạo khiếu nại cho đơn hàng.');
    }
  };

  const resolveComplaint = async (complaintId, decision) => {
    let approvedRefundAmount = 0;
    if (decision === 'REFUND_APPROVED') {
      const amountInput = window.prompt('Nhập số tiền hoàn được duyệt:');
      if (amountInput === null) {
        return;
      }
      approvedRefundAmount = Number(amountInput);
      if (Number.isNaN(approvedRefundAmount) || approvedRefundAmount < 0) {
        setError('Số tiền hoàn duyệt không hợp lệ.');
        return;
      }
    }

    const resolutionNote = window.prompt('Ghi chú quyết định (không bắt buộc):', '') ?? '';

    try {
      await orderAPI.resolveComplaint(complaintId, {
        decision,
        resolutionNote,
        approvedRefundAmount,
      });
      await Promise.all([loadComplaints(), loadOrders()]);
    } catch (err) {
      setError('Không thể xử lý quyết định khiếu nại.');
    }
  };

  const getStatusBadge = (status) => {
    const normalized = String(status || '').toUpperCase();
    const statusConfig = {
      PENDING: { label: 'Chờ xử lý', className: 'bg-yellow-100 text-yellow-800' },
      CONFIRMED: { label: 'Đã xác nhận', className: 'bg-blue-100 text-blue-800' },
      PACKING: { label: 'Đang đóng gói', className: 'bg-indigo-100 text-indigo-800' },
      SHIPPED: { label: 'Đang giao', className: 'bg-purple-100 text-purple-800' },
      DELIVERED: { label: 'Đã giao', className: 'bg-green-100 text-green-800' },
      CANCELED: { label: 'Đã hủy', className: 'bg-red-100 text-red-800' },
      REFUNDED: { label: 'Đã hoàn tiền', className: 'bg-emerald-100 text-emerald-800' },
      REFUND_APPROVED: { label: 'Duyệt hoàn tiền', className: 'bg-emerald-100 text-emerald-800' },
      CANCEL_APPROVED: { label: 'Duyệt hủy đơn', className: 'bg-orange-100 text-orange-800' },
      REJECTED: { label: 'Từ chối', className: 'bg-red-100 text-red-800' },
    };

    const config = statusConfig[normalized] || { label: normalized || 'UNKNOWN', className: 'bg-gray-100 text-gray-700' };
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.className}`}>
        {config.label}
      </span>
    );
  };

  const totalOrderAmount = useMemo(
    () => orders.reduce((sum, order) => sum + (order.totalAmount || 0), 0),
    [orders]
  );

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Quản lý đơn hàng</h1>
        <p className="text-gray-600">Admin giám sát toàn bộ đơn hàng và xử lý quyết định hoàn tiền/hủy đơn đặc biệt.</p>
      </div>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

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
          onClick={() => {
            setActiveTab('complaints');
            void loadComplaints();
          }}
          className={`px-4 py-2 rounded-md text-sm ${
            activeTab === 'complaints' ? 'bg-slate-900 text-white' : 'text-gray-600 hover:bg-gray-100'
          }`}
        >
          Khiếu nại / Hoàn tiền
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="relative md:col-span-2">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Tìm theo mã đơn, tên khách hoặc địa chỉ..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            />
          </div>

          {activeTab === 'orders' ? (
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            >
              <option value="all">Tất cả trạng thái</option>
              {ORDER_STATUS_OPTIONS.map((item) => (
                <option key={item.value} value={item.value}>{item.label}</option>
              ))}
            </select>
          ) : (
            <select
              value={complaintDecision}
              onChange={(e) => setComplaintDecision(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
            >
              {COMPLAINT_DECISION_OPTIONS.map((item) => (
                <option key={item.value} value={item.value}>{item.label}</option>
              ))}
            </select>
          )}
        </div>
      </div>

      {activeTab === 'orders' ? (
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          {loadingOrders ? (
            <div className="p-8 text-center text-gray-600">Đang tải đơn hàng...</div>
          ) : orders.length === 0 ? (
            <div className="p-8 text-center text-gray-500">
              <ShoppingCart className="mx-auto mb-2 text-gray-400" size={48} />
              <p>Không có đơn hàng phù hợp</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mã đơn</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Khách hàng</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ngày đặt</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Số lượng</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tổng tiền</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Quyết định admin</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {orders.map((order) => (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">#{order.id}</td>
                      <td className="px-6 py-4 text-sm text-gray-900">{order.customerName || 'N/A'}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {order.orderDate ? new Date(order.orderDate).toLocaleDateString('vi-VN') : 'N/A'}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600 inline-flex items-center gap-1">
                        <Package size={14} /> {order.totalItems || 0}
                      </td>
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">{(order.totalAmount || 0).toLocaleString('vi-VN')}đ</td>
                      <td className="px-6 py-4 text-sm">{getStatusBadge(order.status)}</td>
                      <td className="px-6 py-4 text-right">
                        <div className="inline-flex items-center gap-2">
                          <button
                            onClick={() => createComplaint(order.id)}
                            className="px-3 py-1.5 text-xs rounded-lg bg-amber-50 text-amber-700 hover:bg-amber-100"
                          >
                            Tạo khiếu nại
                          </button>
                          <button
                            onClick={() => updateOrderStatus(order.id, 'CANCELED', 'Admin hủy đơn đặc biệt')}
                            className="px-3 py-1.5 text-xs rounded-lg bg-red-50 text-red-700 hover:bg-red-100"
                            disabled={String(order.status).toUpperCase() === 'CANCELED' || String(order.status).toUpperCase() === 'REFUNDED'}
                          >
                            Hủy đặc biệt
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
      ) : (
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          {loadingComplaints ? (
            <div className="p-8 text-center text-gray-600">Đang tải khiếu nại...</div>
          ) : complaints.length === 0 ? (
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
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Đề xuất hoàn</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Quyết định</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {complaints.map((item) => {
                    const isPending = String(item.decision).toUpperCase() === 'PENDING';
                    return (
                      <tr key={item.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 text-sm text-gray-900">#{item.id}</td>
                        <td className="px-6 py-4 text-sm text-gray-700">#{item.orderId}</td>
                        <td className="px-6 py-4 text-sm text-gray-700">{item.customerName || 'N/A'}</td>
                        <td className="px-6 py-4 text-sm text-gray-700">{item.reason}</td>
                        <td className="px-6 py-4 text-sm text-gray-700">{(item.requestedRefundAmount || 0).toLocaleString('vi-VN')}đ</td>
                        <td className="px-6 py-4 text-sm">{getStatusBadge(item.decision)}</td>
                        <td className="px-6 py-4 text-right">
                          {isPending ? (
                            <div className="inline-flex gap-2">
                              <button
                                onClick={() => resolveComplaint(item.id, 'REFUND_APPROVED')}
                                className="px-3 py-1.5 text-xs rounded-lg bg-emerald-50 text-emerald-700 hover:bg-emerald-100"
                              >
                                Duyệt hoàn tiền
                              </button>
                              <button
                                onClick={() => resolveComplaint(item.id, 'CANCEL_APPROVED')}
                                className="px-3 py-1.5 text-xs rounded-lg bg-orange-50 text-orange-700 hover:bg-orange-100"
                              >
                                Duyệt hủy đơn
                              </button>
                              <button
                                onClick={() => resolveComplaint(item.id, 'REJECTED')}
                                className="px-3 py-1.5 text-xs rounded-lg bg-red-50 text-red-700 hover:bg-red-100"
                              >
                                Từ chối
                              </button>
                            </div>
                          ) : (
                            <span className="text-xs text-gray-500">Đã xử lý</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      <div className="mt-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
        <div className="text-sm text-gray-600">
          {activeTab === 'orders'
            ? `Hiển thị ${orders.length} đơn hàng | Tổng giá trị ${(totalOrderAmount || 0).toLocaleString('vi-VN')}đ`
            : `Hiển thị ${complaints.length} yêu cầu khiếu nại`}
        </div>
        <Link to="/admin/financial-reports" className="text-sm text-slate-700 hover:text-slate-900">
          Xem báo cáo tài chính →
        </Link>
      </div>
    </div>
  );
};

export default Orders;
