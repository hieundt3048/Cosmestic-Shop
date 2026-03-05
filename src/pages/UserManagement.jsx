import React, { useEffect, useMemo, useState } from 'react';
import { ShieldCheck, Search, Lock, Unlock, UserCog, ClipboardList } from 'lucide-react';
import { userAPI } from '../api/userApi';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  useEffect(() => {
    const loadUsers = async () => {
      try {
        setLoading(true);
        const data = await userAPI.getAllUsers();
        const normalized = (data || []).map((item) => ({
          ...item,
          accountLocked: !!item.accountLocked,
        }));
        setUsers(normalized);
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, []);

  const filteredUsers = useMemo(() => {
    const q = query.trim().toLowerCase();
    return users.filter((user) => {
      const matchesText =
        !q ||
        user.username?.toLowerCase().includes(q) ||
        user.fullName?.toLowerCase().includes(q) ||
        user.email?.toLowerCase().includes(q);
      const matchesRole = roleFilter === 'ALL' || user.role === roleFilter;
      const matchesStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'ACTIVE' && !user.accountLocked) ||
        (statusFilter === 'LOCKED' && user.accountLocked);

      return matchesText && matchesRole && matchesStatus;
    });
  }, [users, query, roleFilter, statusFilter]);

  const updateRole = (userId, role) => {
    setUsers((prev) => prev.map((item) => (item.id === userId ? { ...item, role } : item)));
  };

  const toggleLock = (userId) => {
    setUsers((prev) => prev.map((item) => (item.id === userId ? { ...item, accountLocked: !item.accountLocked } : item)));
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Quản lý người dùng</h1>
        <p className="text-gray-600">Phân quyền tài khoản, khóa/mở khóa và theo dõi trạng thái hoạt động.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow-sm p-4 flex items-center gap-3">
          <div className="p-2 rounded-lg bg-indigo-100 text-indigo-700"><UserCog size={18} /></div>
          <div>
            <p className="text-sm text-gray-600">Tổng tài khoản</p>
            <p className="text-xl font-semibold text-gray-900">{users.length}</p>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4 flex items-center gap-3">
          <div className="p-2 rounded-lg bg-emerald-100 text-emerald-700"><ShieldCheck size={18} /></div>
          <div>
            <p className="text-sm text-gray-600">Đang hoạt động</p>
            <p className="text-xl font-semibold text-gray-900">{users.filter((u) => !u.accountLocked).length}</p>
          </div>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4 flex items-center gap-3">
          <div className="p-2 rounded-lg bg-red-100 text-red-700"><Lock size={18} /></div>
          <div>
            <p className="text-sm text-gray-600">Đang khóa</p>
            <p className="text-xl font-semibold text-gray-900">{users.filter((u) => u.accountLocked).length}</p>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-4 mb-6 grid grid-cols-1 md:grid-cols-4 gap-3">
        <div className="relative md:col-span-2">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Tìm username, họ tên, email..."
            className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
          />
        </div>

        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
        >
          <option value="ALL">Tất cả vai trò</option>
          <option value="ADMIN">ADMIN</option>
          <option value="EMPLOYEE">EMPLOYEE</option>
          <option value="CUSTOMER">CUSTOMER</option>
        </select>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900"
        >
          <option value="ALL">Tất cả trạng thái</option>
          <option value="ACTIVE">Đang hoạt động</option>
          <option value="LOCKED">Đang khóa</option>
        </select>
      </div>

      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-gray-600">Đang tải dữ liệu...</div>
        ) : filteredUsers.length === 0 ? (
          <div className="p-8 text-center text-gray-500">Không có dữ liệu phù hợp.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tài khoản</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Liên hệ</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Vai trò</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <p className="text-sm font-medium text-gray-900">{user.fullName || user.username}</p>
                      <p className="text-xs text-gray-500">@{user.username} • ID {user.id}</p>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-700">
                      <p>{user.email || '-'}</p>
                      <p className="text-xs text-gray-500">{user.phone || '-'}</p>
                    </td>
                    <td className="px-6 py-4">
                      <select
                        value={user.role}
                        onChange={(e) => updateRole(user.id, e.target.value)}
                        className="px-2 py-1 border border-gray-300 rounded-md text-sm"
                      >
                        <option value="ADMIN">ADMIN</option>
                        <option value="EMPLOYEE">EMPLOYEE</option>
                        <option value="CUSTOMER">CUSTOMER</option>
                      </select>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                          user.accountLocked ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-700'
                        }`}
                      >
                        {user.accountLocked ? 'Đang khóa' : 'Hoạt động'}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => toggleLock(user.id)}
                        className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm ${
                          user.accountLocked
                            ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
                            : 'bg-red-50 text-red-700 hover:bg-red-100'
                        }`}
                      >
                        {user.accountLocked ? <Unlock size={14} /> : <Lock size={14} />}
                        {user.accountLocked ? 'Mở khóa' : 'Khóa'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Link to="/admin/audit-logs" className="mt-5 inline-flex items-center gap-2 text-sm text-slate-700 hover:text-slate-900">
        <ClipboardList size={16} />
        Xem Audit Logs
      </Link>
    </div>
  );
};

export default UserManagement;
