import React, { useState } from 'react';
import { DatabaseBackup, LockKeyhole, Save, Settings } from 'lucide-react';

const SystemSettings = () => {
  const [shopConfig, setShopConfig] = useState({
    shopName: 'LUMIA Cosmetic Shop',
    supportPhone: '1900 1234',
    supportEmail: 'support@lumia.vn',
    shippingFee: 30000,
    paymentMethods: 'COD, BANK_TRANSFER',
  });

  const [securityConfig, setSecurityConfig] = useState({
    minPasswordLength: 8,
    sessionTimeoutMinutes: 120,
    jwtExpirationHours: 24,
    bcryptRounds: 10,
  });

  const backupHistory = [
    { id: 1, at: '2026-03-03 02:00', status: 'SUCCESS' },
    { id: 2, at: '2026-03-02 02:00', status: 'SUCCESS' },
    { id: 3, at: '2026-03-01 02:00', status: 'SUCCESS' },
  ];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Cấu hình hệ thống & bảo mật</h1>
        <p className="text-gray-600">Quản lý cấu hình chung, bảo mật phiên và sao lưu dữ liệu.</p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-6">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 inline-flex items-center gap-2">
            <Settings size={18} />
            Cấu hình chung
          </h2>

          <div className="space-y-3">
            <input
              value={shopConfig.shopName}
              onChange={(e) => setShopConfig((prev) => ({ ...prev, shopName: e.target.value }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              placeholder="Tên shop"
            />
            <input
              value={shopConfig.supportPhone}
              onChange={(e) => setShopConfig((prev) => ({ ...prev, supportPhone: e.target.value }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              placeholder="Số hotline"
            />
            <input
              value={shopConfig.supportEmail}
              onChange={(e) => setShopConfig((prev) => ({ ...prev, supportEmail: e.target.value }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              placeholder="Email hỗ trợ"
            />
            <input
              type="number"
              value={shopConfig.shippingFee}
              onChange={(e) => setShopConfig((prev) => ({ ...prev, shippingFee: Number(e.target.value) }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              placeholder="Phí vận chuyển"
            />
            <input
              value={shopConfig.paymentMethods}
              onChange={(e) => setShopConfig((prev) => ({ ...prev, paymentMethods: e.target.value }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              placeholder="Phương thức thanh toán"
            />
          </div>

          <button className="mt-4 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 inline-flex items-center gap-2">
            <Save size={16} />
            Lưu cấu hình chung
          </button>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 inline-flex items-center gap-2">
            <LockKeyhole size={18} />
            Cấu hình bảo mật
          </h2>

          <div className="space-y-3">
            <label className="block text-sm text-gray-600">Độ dài mật khẩu tối thiểu</label>
            <input
              type="number"
              value={securityConfig.minPasswordLength}
              onChange={(e) => setSecurityConfig((prev) => ({ ...prev, minPasswordLength: Number(e.target.value) }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
            />

            <label className="block text-sm text-gray-600">Session timeout (phút)</label>
            <input
              type="number"
              value={securityConfig.sessionTimeoutMinutes}
              onChange={(e) => setSecurityConfig((prev) => ({ ...prev, sessionTimeoutMinutes: Number(e.target.value) }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
            />

            <label className="block text-sm text-gray-600">JWT expiration (giờ)</label>
            <input
              type="number"
              value={securityConfig.jwtExpirationHours}
              onChange={(e) => setSecurityConfig((prev) => ({ ...prev, jwtExpirationHours: Number(e.target.value) }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
            />

            <label className="block text-sm text-gray-600">BCrypt rounds</label>
            <input
              type="number"
              value={securityConfig.bcryptRounds}
              onChange={(e) => setSecurityConfig((prev) => ({ ...prev, bcryptRounds: Number(e.target.value) }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
            />
          </div>

          <button className="mt-4 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 inline-flex items-center gap-2">
            <Save size={16} />
            Lưu cấu hình bảo mật
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
          <h2 className="text-lg font-semibold text-gray-900 inline-flex items-center gap-2">
            <DatabaseBackup size={18} />
            Sao lưu dữ liệu
          </h2>
          <button className="px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800">Tạo bản sao lưu ngay</button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Thời gian</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {backupHistory.map((backup) => (
                <tr key={backup.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-700">{backup.at}</td>
                  <td className="px-6 py-4">
                    <span className="px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-100 text-emerald-700">
                      {backup.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default SystemSettings;
