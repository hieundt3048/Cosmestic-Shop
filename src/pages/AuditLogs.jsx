import React, { useMemo, useState } from 'react';
import { ShieldAlert, Search } from 'lucide-react';

const mockLogs = [
  { id: 1, actor: 'admin', role: 'ADMIN', action: 'CREATE_EMPLOYEE', target: 'employee.kthanh', at: '2026-03-03T08:05:00Z' },
  { id: 2, actor: 'admin', role: 'ADMIN', action: 'UPDATE_ROLE', target: 'customer.anhthu', at: '2026-03-03T08:19:00Z' },
  { id: 3, actor: 'employee.hoa', role: 'EMPLOYEE', action: 'UPDATE_ORDER_STATUS', target: 'order#1241', at: '2026-03-03T08:36:00Z' },
  { id: 4, actor: 'admin', role: 'ADMIN', action: 'LOCK_USER', target: 'customer.nghi', at: '2026-03-03T08:44:00Z' },
  { id: 5, actor: 'employee.minh', role: 'EMPLOYEE', action: 'UPDATE_INVENTORY', target: 'product#89', at: '2026-03-03T09:11:00Z' },
];

const AuditLogs = () => {
  const [query, setQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');

  const logs = useMemo(() => {
    const q = query.trim().toLowerCase();
    return mockLogs.filter((log) => {
      const matchesText =
        !q ||
        log.actor.toLowerCase().includes(q) ||
        log.action.toLowerCase().includes(q) ||
        log.target.toLowerCase().includes(q);
      const matchesRole = roleFilter === 'ALL' || log.role === roleFilter;
      return matchesText && matchesRole;
    });
  }, [query, roleFilter]);

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Audit Logs</h1>
        <p className="text-gray-600">Theo dõi lịch sử thao tác của admin và nhân viên.</p>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-4 mb-6 grid grid-cols-1 md:grid-cols-3 gap-3">
        <div className="relative md:col-span-2">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Tìm actor, action hoặc đối tượng tác động"
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
        </select>
      </div>

      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        {logs.length === 0 ? (
          <div className="p-8 text-center text-gray-500">Không có bản ghi phù hợp.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Thời gian</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Người thao tác</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Hành động</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Đối tượng</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm text-gray-700">{new Date(log.at).toLocaleString('vi-VN')}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      <span className="inline-flex items-center gap-2">
                        <ShieldAlert size={14} className="text-gray-500" />
                        {log.actor}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-700">{log.action}</td>
                    <td className="px-6 py-4 text-sm text-gray-700">{log.target}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AuditLogs;
