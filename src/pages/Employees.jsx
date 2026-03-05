import React, { useEffect, useState } from 'react';
import { Briefcase, Search, Eye, EyeOff, Mail, Phone, Trash2, X } from 'lucide-react';
import { userAPI } from '../api/userApi';

const Employees = () => {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [error, setError] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [deletingId, setDeletingId] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [newEmployee, setNewEmployee] = useState({
    username: '',
    password: '',
    email: '',
    phone: '',
    fullName: '',
  });

  useEffect(() => {
    loadEmployees();
  }, []);

  const loadEmployees = async () => {
    try {
      setLoading(true);
      const data = await userAPI.getEmployees();
      setEmployees(data);
      setError('');
    } catch (err) {
      setError('Không thể tải danh sách nhân viên');
    } finally {
      setLoading(false);
    }
  };

  const filteredEmployees = employees.filter((employee) => {
    const query = searchQuery.toLowerCase();
    return (
      employee.username?.toLowerCase().includes(query) ||
      employee.fullName?.toLowerCase().includes(query) ||
      employee.email?.toLowerCase().includes(query) ||
      employee.phone?.includes(query)
    );
  });

  const handleViewDetail = async (id) => {
    try {
      const detail = await userAPI.getUserById(id);
      setSelectedEmployee(detail);
    } catch (err) {
      setError('Không thể tải chi tiết nhân viên');
    }
  };

  const handleDeleteEmployee = async (employee) => {
    const confirmed = window.confirm(`Xóa nhân viên \"${employee.fullName}\"?`);
    if (!confirmed) return;

    try {
      setDeletingId(employee.id);
      await userAPI.deleteUser(employee.id);
      setEmployees((prev) => prev.filter((item) => item.id !== employee.id));
      if (selectedEmployee?.id === employee.id) {
        setSelectedEmployee(null);
      }
    } catch (err) {
      setError('Xóa nhân viên thất bại');
    } finally {
      setDeletingId(null);
    }
  };

  const handleCreateEmployee = async (e) => {
    e.preventDefault();

    try {
      setCreating(true);
      setCreateError('');
      await userAPI.createEmployee(newEmployee);

      setShowCreateModal(false);
      setShowPassword(false);
      setNewEmployee({
        username: '',
        password: '',
        email: '',
        phone: '',
        fullName: '',
      });
      await loadEmployees();
    } catch (err) {
      const responseData = err?.response?.data;
      let errorMessage = 'Tạo nhân viên thất bại';

      if (typeof responseData === 'string' && responseData.trim()) {
        errorMessage = responseData;
      } else if (responseData?.message) {
        errorMessage = responseData.message;
      } else if (Array.isArray(responseData?.errors) && responseData.errors.length > 0) {
        errorMessage = responseData.errors.join('; ');
      }

      setCreateError(errorMessage);
    } finally {
      setCreating(false);
    }
  };

  return (
    <div>
      <div className="mb-6 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900 mb-2">Quản lý nhân viên</h1>
          <p className="text-gray-600">Quản lý thông tin nhân viên trong hệ thống</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 text-sm"
        >
          Thêm nhân viên
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Tìm kiếm nhân viên theo tên, email, số điện thoại..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-gray-300 border-t-slate-900"></div>
            <p className="mt-2 text-gray-600">Đang tải...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-600">{error}</div>
        ) : filteredEmployees.length === 0 ? (
          <div className="p-8 text-center text-gray-500">
            <Briefcase className="mx-auto mb-2 text-gray-400" size={48} />
            <p>Chưa có nhân viên nào</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Nhân viên
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Tên đăng nhập
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Liên hệ
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Đơn đã xử lý
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Thao tác
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredEmployees.map((employee) => (
                  <tr key={employee.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        <div className="w-10 h-10 rounded-full bg-slate-900 flex items-center justify-center text-white font-medium">
                          {employee.fullName?.charAt(0).toUpperCase()}
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">
                            {employee.fullName}
                          </div>
                          <div className="text-sm text-gray-500">ID: {employee.id}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">{employee.username}</td>
                    <td className="px-6 py-4 text-sm">
                      <div className="space-y-1">
                        <div className="flex items-center text-gray-600">
                          <Mail size={14} className="mr-2" />
                          {employee.email}
                        </div>
                        <div className="flex items-center text-gray-600">
                          <Phone size={14} className="mr-2" />
                          {employee.phone}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {employee.totalOrders || 0} đơn
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right text-sm">
                      <div className="inline-flex items-center gap-3">
                        <button
                          className="text-blue-600 hover:text-blue-900 inline-flex items-center"
                          title="Xem chi tiết"
                          onClick={() => handleViewDetail(employee.id)}
                        >
                          <Eye size={18} />
                        </button>
                        <button
                          className="text-red-600 hover:text-red-800 inline-flex items-center disabled:opacity-50"
                          title="Xóa nhân viên"
                          onClick={() => handleDeleteEmployee(employee)}
                          disabled={deletingId === employee.id}
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

      {selectedEmployee && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div className="px-5 py-4 border-b border-gray-200 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">Chi tiết nhân viên</h3>
              <button onClick={() => setSelectedEmployee(null)} className="text-gray-500 hover:text-gray-800">
                <X size={18} />
              </button>
            </div>
            <div className="p-5 space-y-3 text-sm text-gray-700">
              <p><span className="font-medium">ID:</span> {selectedEmployee.id}</p>
              <p><span className="font-medium">Họ tên:</span> {selectedEmployee.fullName}</p>
              <p><span className="font-medium">Tên đăng nhập:</span> {selectedEmployee.username}</p>
              <p><span className="font-medium">Email:</span> {selectedEmployee.email || '-'}</p>
              <p><span className="font-medium">Số điện thoại:</span> {selectedEmployee.phone || '-'}</p>
              <p><span className="font-medium">Vai trò:</span> {selectedEmployee.role}</p>
              <p><span className="font-medium">Trạng thái khóa:</span> {selectedEmployee.accountLocked ? 'Đã khóa' : 'Đang hoạt động'}</p>
            </div>
            <div className="px-5 py-4 border-t border-gray-200 flex justify-end">
              <button
                onClick={() => setSelectedEmployee(null)}
                className="px-4 py-2 rounded-lg border border-gray-300 hover:bg-gray-50"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg">
            <div className="px-5 py-4 border-b border-gray-200 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">Thêm nhân viên mới</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-gray-500 hover:text-gray-800">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleCreateEmployee} className="p-5 space-y-4">
              {createError && (
                <div className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                  {createError}
                </div>
              )}

              <input
                value={newEmployee.fullName}
                onChange={(e) => setNewEmployee((prev) => ({ ...prev, fullName: e.target.value }))}
                placeholder="Họ và tên"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                required
              />
              <input
                value={newEmployee.username}
                onChange={(e) => setNewEmployee((prev) => ({ ...prev, username: e.target.value }))}
                placeholder="Tên đăng nhập"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                required
              />
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={newEmployee.password}
                  onChange={(e) => setNewEmployee((prev) => ({ ...prev, password: e.target.value }))}
                  placeholder="Mật khẩu"
                  className="w-full px-3 py-2 pr-10 border border-gray-300 rounded-lg"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-800"
                  title={showPassword ? 'Ẩn mật khẩu' : 'Hiển thị mật khẩu'}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              <input
                type="email"
                value={newEmployee.email}
                onChange={(e) => setNewEmployee((prev) => ({ ...prev, email: e.target.value }))}
                placeholder="Email"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                required
              />
              <input
                value={newEmployee.phone}
                onChange={(e) => setNewEmployee((prev) => ({ ...prev, phone: e.target.value }))}
                placeholder="Số điện thoại (10 số, bắt đầu bằng 0)"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                required
              />

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateModal(false);
                    setShowPassword(false);
                  }}
                  className="px-4 py-2 rounded-lg border border-gray-300 hover:bg-gray-50"
                  disabled={creating}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50"
                  disabled={creating}
                >
                  {creating ? 'Đang tạo...' : 'Tạo nhân viên'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {!loading && !error && filteredEmployees.length > 0 && (
        <div className="mt-4 text-sm text-gray-600">Hiển thị {filteredEmployees.length} / {employees.length} nhân viên</div>
      )}
    </div>
  );
};

export default Employees;
