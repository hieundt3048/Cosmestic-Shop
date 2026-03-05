import React, { useState, useEffect } from 'react';
import { Users, Search, Eye, Mail, Phone, Trash2, X } from 'lucide-react';
import { userAPI } from '../api/userApi';

const Customers = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [error, setError] = useState('');
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [deletingId, setDeletingId] = useState(null);

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      setLoading(true);
      const data = await userAPI.getCustomers();
      setCustomers(data);
      setError('');
    } catch (err) {
      setError('Không thể tải danh sách khách hàng');
    } finally {
      setLoading(false);
    }
  };

  const filteredCustomers = customers.filter(customer => {
    const query = searchQuery.toLowerCase();
    return (
      customer.username?.toLowerCase().includes(query) ||
      customer.fullName?.toLowerCase().includes(query) ||
      customer.email?.toLowerCase().includes(query) ||
      customer.phone?.includes(query)
    );
  });

  const handleViewDetail = async (id) => {
    try {
      const detail = await userAPI.getUserById(id);
      setSelectedCustomer(detail);
    } catch (err) {
      setError('Không thể tải chi tiết khách hàng');
    }
  };

  const handleDeleteCustomer = async (customer) => {
    const confirmed = window.confirm(`Xóa khách hàng \"${customer.fullName}\"?`);
    if (!confirmed) return;

    try {
      setDeletingId(customer.id);
      await userAPI.deleteUser(customer.id);
      setCustomers((prev) => prev.filter((item) => item.id !== customer.id));
      if (selectedCustomer?.id === customer.id) {
        setSelectedCustomer(null);
      }
    } catch (err) {
      setError('Xóa khách hàng thất bại');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div>
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Quản lý khách hàng</h1>
        <p className="text-gray-600">Quản lý thông tin khách hàng và lịch sử mua hàng</p>
      </div>

      {/* Search Bar */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm khách hàng theo tên, email, số điện thoại..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>
      </div>

      {/* Customers Table */}
      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-slate-900"></div>
            <p className="mt-2 text-gray-600">Đang tải...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-600">{error}</div>
        ) : filteredCustomers.length === 0 ? (
          <div className="p-8 text-center text-gray-500">
            <Users className="mx-auto mb-2 text-gray-400" size={48} />
            <p>Chưa có khách hàng nào</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Khách hàng
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tên đăng nhập
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Liên hệ
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Đơn hàng
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Thao tác
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredCustomers.map((customer) => (
                  <tr key={customer.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        <div className="w-10 h-10 rounded-full bg-slate-900 flex items-center justify-center text-white font-medium">
                          {customer.fullName?.charAt(0).toUpperCase()}
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">
                            {customer.fullName}
                          </div>
                          <div className="text-sm text-gray-500">
                            ID: {customer.id}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      {customer.username}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      <div className="space-y-1">
                        <div className="flex items-center text-gray-600">
                          <Mail size={14} className="mr-2" />
                          {customer.email}
                        </div>
                        <div className="flex items-center text-gray-600">
                          <Phone size={14} className="mr-2" />
                          {customer.phone}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {customer.totalOrders || 0} đơn
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right text-sm">
                      <div className="inline-flex items-center gap-3">
                        <button
                          className="text-blue-600 hover:text-blue-900 inline-flex items-center"
                          title="Xem chi tiết"
                          onClick={() => handleViewDetail(customer.id)}
                        >
                          <Eye size={18} />
                        </button>
                        <button
                          className="text-red-600 hover:text-red-800 inline-flex items-center disabled:opacity-50"
                          title="Xóa khách hàng"
                          onClick={() => handleDeleteCustomer(customer)}
                          disabled={deletingId === customer.id}
                        >
                          <Trash2 size={18} />
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

      {selectedCustomer && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div className="px-5 py-4 border-b border-gray-200 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">Chi tiết khách hàng</h3>
              <button onClick={() => setSelectedCustomer(null)} className="text-gray-500 hover:text-gray-800">
                <X size={18} />
              </button>
            </div>
            <div className="p-5 space-y-3 text-sm text-gray-700">
              <p><span className="font-medium">ID:</span> {selectedCustomer.id}</p>
              <p><span className="font-medium">Họ tên:</span> {selectedCustomer.fullName}</p>
              <p><span className="font-medium">Tên đăng nhập:</span> {selectedCustomer.username}</p>
              <p><span className="font-medium">Email:</span> {selectedCustomer.email || '-'}</p>
              <p><span className="font-medium">Số điện thoại:</span> {selectedCustomer.phone || '-'}</p>
              <p><span className="font-medium">Vai trò:</span> {selectedCustomer.role}</p>
              <p><span className="font-medium">Trạng thái khóa:</span> {selectedCustomer.accountLocked ? 'Đã khóa' : 'Đang hoạt động'}</p>
            </div>
            <div className="px-5 py-4 border-t border-gray-200 flex justify-end">
              <button
                onClick={() => setSelectedCustomer(null)}
                className="px-4 py-2 rounded-lg border border-gray-300 hover:bg-gray-50"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Stats Footer */}
      {!loading && filteredCustomers.length > 0 && (
        <div className="mt-4 text-sm text-gray-600">
          Hiển thị {filteredCustomers.length} / {customers.length} khách hàng
        </div>
      )}
    </div>
  );
};

export default Customers;
