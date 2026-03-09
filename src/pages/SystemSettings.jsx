import React, { useEffect, useState } from 'react';
import { DatabaseBackup, LockKeyhole, Save, Settings } from 'lucide-react';
import { systemSettingsApi } from '../api/systemSettingsApi';

const SystemSettings = () => {
  const [loading, setLoading] = useState(true);
  const [savingGeneral, setSavingGeneral] = useState(false);
  const [savingSecurity, setSavingSecurity] = useState(false);
  const [savingBackup, setSavingBackup] = useState(false);
  const [runningBackup, setRunningBackup] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

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
    jwtExpirationMinutes: 60,
    bcryptRounds: 10,
  });

  const [backupConfig, setBackupConfig] = useState({
    autoBackupEnabled: true,
    backupOutputDir: 'backups',
    backupIntervalHours: 24,
  });

  const [backupHistory, setBackupHistory] = useState([]);

  useEffect(() => {
    void loadSettings();
    void loadBackupHistory();
  }, []);

  const loadSettings = async () => {
    try {
      setLoading(true);
      setError('');
      const settings = await systemSettingsApi.getSettings();

      setShopConfig({
        shopName: settings.shopName || '',
        supportPhone: settings.supportPhone || '',
        supportEmail: settings.supportEmail || '',
        shippingFee: Number(settings.shippingFee || 0),
        paymentMethods: settings.paymentMethods || '',
      });

      setSecurityConfig({
        minPasswordLength: Number(settings.minPasswordLength || 8),
        sessionTimeoutMinutes: Number(settings.sessionTimeoutMinutes || 120),
        jwtExpirationMinutes: Number(settings.jwtExpirationMinutes || 60),
        bcryptRounds: Number(settings.bcryptRounds || 10),
      });

      setBackupConfig({
        autoBackupEnabled: Boolean(settings.autoBackupEnabled),
        backupOutputDir: settings.backupOutputDir || 'backups',
        backupIntervalHours: Number(settings.backupIntervalHours || 24),
      });
    } catch (err) {
      setError('Không thể tải cấu hình hệ thống.');
    } finally {
      setLoading(false);
    }
  };

  const loadBackupHistory = async () => {
    try {
      const history = await systemSettingsApi.getBackupHistory();
      setBackupHistory(history || []);
    } catch (err) {
      setBackupHistory([]);
    }
  };

  const saveGeneralSettings = async () => {
    try {
      setSavingGeneral(true);
      setError('');
      setSuccess('');
      await systemSettingsApi.updateGeneralSettings({
        shopName: shopConfig.shopName,
        supportPhone: shopConfig.supportPhone,
        supportEmail: shopConfig.supportEmail,
        shippingFee: Number(shopConfig.shippingFee),
        paymentMethods: shopConfig.paymentMethods,
      });
      setSuccess('Đã lưu cấu hình chung.');
    } catch (err) {
      setError('Không thể lưu cấu hình chung.');
    } finally {
      setSavingGeneral(false);
    }
  };

  const saveSecuritySettings = async () => {
    try {
      setSavingSecurity(true);
      setError('');
      setSuccess('');
      await systemSettingsApi.updateSecuritySettings({
        minPasswordLength: Number(securityConfig.minPasswordLength),
        sessionTimeoutMinutes: Number(securityConfig.sessionTimeoutMinutes),
        jwtExpirationMinutes: Number(securityConfig.jwtExpirationMinutes),
        bcryptRounds: Number(securityConfig.bcryptRounds),
      });
      setSuccess('Đã lưu cấu hình bảo mật. Token mới sẽ dùng chính sách vừa cập nhật.');
    } catch (err) {
      setError('Không thể lưu cấu hình bảo mật.');
    } finally {
      setSavingSecurity(false);
    }
  };

  const saveBackupSettings = async () => {
    try {
      setSavingBackup(true);
      setError('');
      setSuccess('');
      await systemSettingsApi.updateBackupSettings({
        autoBackupEnabled: backupConfig.autoBackupEnabled,
        backupOutputDir: backupConfig.backupOutputDir,
        backupIntervalHours: Number(backupConfig.backupIntervalHours),
      });
      setSuccess('Đã lưu cấu hình sao lưu định kỳ.');
    } catch (err) {
      setError('Không thể lưu cấu hình sao lưu.');
    } finally {
      setSavingBackup(false);
    }
  };

  const runBackupNow = async () => {
    try {
      setRunningBackup(true);
      setError('');
      setSuccess('');
      const result = await systemSettingsApi.runBackupNow();
      if (String(result.status).toUpperCase() === 'SUCCESS') {
        setSuccess('Tạo backup thành công.');
      } else {
        setError(result.message || 'Backup thất bại. Vui lòng kiểm tra quyền SQL Server.');
      }
      await loadBackupHistory();
    } catch (err) {
      setError('Không thể chạy backup thủ công.');
    } finally {
      setRunningBackup(false);
    }
  };

  const statusClassName = (status) => {
    const normalized = String(status || '').toUpperCase();
    if (normalized === 'SUCCESS') {
      return 'bg-emerald-100 text-emerald-700';
    }
    if (normalized === 'FAILED') {
      return 'bg-red-100 text-red-700';
    }
    return 'bg-gray-100 text-gray-700';
  };

  if (loading) {
    return <div className="text-gray-600">Đang tải cấu hình hệ thống...</div>;
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Cấu hình hệ thống & bảo mật</h1>
        <p className="text-gray-600">Quản lý cấu hình chung, bảo mật phiên và sao lưu dữ liệu.</p>
      </div>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {success && (
        <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          {success}
        </div>
      )}

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

          <button
            onClick={saveGeneralSettings}
            disabled={savingGeneral}
            className="mt-4 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 inline-flex items-center gap-2"
          >
            <Save size={16} />
            {savingGeneral ? 'Đang lưu...' : 'Lưu cấu hình chung'}
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

            <label className="block text-sm text-gray-600">JWT expiration (phút)</label>
            <input
              type="number"
              value={securityConfig.jwtExpirationMinutes}
              onChange={(e) => setSecurityConfig((prev) => ({ ...prev, jwtExpirationMinutes: Number(e.target.value) }))}
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

          <button
            onClick={saveSecuritySettings}
            disabled={savingSecurity}
            className="mt-4 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 inline-flex items-center gap-2"
          >
            <Save size={16} />
            {savingSecurity ? 'Đang lưu...' : 'Lưu cấu hình bảo mật'}
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
          <h2 className="text-lg font-semibold text-gray-900 inline-flex items-center gap-2">
            <DatabaseBackup size={18} />
            Sao lưu dữ liệu
          </h2>
          <button
            onClick={runBackupNow}
            disabled={runningBackup}
            className="px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50"
          >
            {runningBackup ? 'Đang tạo backup...' : 'Tạo bản sao lưu ngay'}
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
          <label className="flex items-center gap-2 text-sm text-gray-700">
            <input
              type="checkbox"
              checked={backupConfig.autoBackupEnabled}
              onChange={(e) =>
                setBackupConfig((prev) => ({
                  ...prev,
                  autoBackupEnabled: e.target.checked,
                }))
              }
            />
            Bật backup định kỳ
          </label>

          <input
            value={backupConfig.backupOutputDir}
            onChange={(e) => setBackupConfig((prev) => ({ ...prev, backupOutputDir: e.target.value }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg"
            placeholder="Đường dẫn thư mục backup"
          />

          <input
            type="number"
            value={backupConfig.backupIntervalHours}
            onChange={(e) => setBackupConfig((prev) => ({ ...prev, backupIntervalHours: Number(e.target.value) }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg"
            placeholder="Khoảng cách backup (giờ)"
          />
        </div>

        <button
          onClick={saveBackupSettings}
          disabled={savingBackup}
          className="mb-5 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 inline-flex items-center gap-2"
        >
          <Save size={16} />
          {savingBackup ? 'Đang lưu...' : 'Lưu cấu hình sao lưu'}
        </button>

        <div className="text-xs text-gray-500 mb-3">
          Backup định kỳ được kiểm tra theo lịch hệ thống và chạy theo chu kỳ cấu hình ở trên.
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Thời gian</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Kiểu chạy</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">File</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {backupHistory.map((backup) => (
                <tr key={backup.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-700">
                        {backup.startedAt ? new Date(backup.startedAt).toLocaleString('vi-VN') : 'N/A'}
                      </td>
                  <td className="px-6 py-4">
                        <span className={`px-2.5 py-1 rounded-full text-xs font-medium ${statusClassName(backup.status)}`}>
                      {backup.status}
                    </span>
                  </td>
                      <td className="px-6 py-4 text-sm text-gray-600">{backup.triggerType || 'N/A'}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{backup.backupFilePath || backup.message || 'N/A'}</td>
                </tr>
              ))}
                  {backupHistory.length === 0 && (
                    <tr>
                      <td className="px-6 py-4 text-sm text-gray-500" colSpan={4}>
                        Chưa có bản ghi backup nào.
                      </td>
                    </tr>
                  )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default SystemSettings;
