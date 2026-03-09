import React, { useEffect, useMemo, useState } from 'react';
import { Download, FileBarChart2 } from 'lucide-react';
import { reportAPI } from '../api/reportApi';

const FinancialReports = () => {
  const [range, setRange] = useState('month');
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadFinancialReport = async () => {
      try {
        setLoading(true);
        setError('');
        const data = await reportAPI.getFinancialReport(range);
        setRows(data?.points || []);
      } catch (err) {
        setRows([]);
        setError('Không thể tải báo cáo tài chính từ hệ thống.');
      } finally {
        setLoading(false);
      }
    };

    loadFinancialReport();
  }, [range]);

  const totals = useMemo(
    () =>
      rows.reduce(
        (acc, item) => {
          acc.revenue += item.revenue;
          acc.shipping += item.shipping;
          acc.tax += item.tax;
          acc.netProfit += item.netProfit;
          return acc;
        },
        { revenue: 0, shipping: 0, tax: 0, netProfit: 0 }
      ),
    [rows]
  );

  const exportCsv = () => {
    const csvRows = [
      ['Thang', 'Doanh thu', 'Chi phi van chuyen', 'Thue', 'Loi nhuan rong'],
      ...rows.map((item) => [item.label, item.revenue, item.shipping, item.tax, item.netProfit]),
    ];
    const content = csvRows.map((row) => row.join(',')).join('\n');
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'financial-report.csv';
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900 mb-2">Báo cáo tài chính</h1>
          <p className="text-gray-600">Theo dõi thuế, chi phí vận chuyển và lợi nhuận ròng.</p>
        </div>
        <div className="flex items-center gap-2">
          <select
            value={range}
            onChange={(e) => setRange(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg"
          >
            <option value="day">Theo ngày</option>
            <option value="week">Theo tuần</option>
            <option value="month">Theo tháng</option>
            <option value="year">Theo năm</option>
          </select>
          <button
            onClick={exportCsv}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800"
          >
            <Download size={16} />
            Xuất CSV
          </button>
        </div>
      </div>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Tổng doanh thu</p>
          <p className="text-xl font-semibold text-gray-900">{totals.revenue.toLocaleString('vi-VN')}đ</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Tổng thuế</p>
          <p className="text-xl font-semibold text-gray-900">{totals.tax.toLocaleString('vi-VN')}đ</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Chi phí vận chuyển</p>
          <p className="text-xl font-semibold text-gray-900">{totals.shipping.toLocaleString('vi-VN')}đ</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Lợi nhuận ròng</p>
          <p className="text-xl font-semibold text-emerald-700">{totals.netProfit.toLocaleString('vi-VN')}đ</p>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 inline-flex items-center gap-2">
            <FileBarChart2 size={18} />
            Chi tiết theo kỳ
          </h2>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tháng</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Doanh thu</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Thuế</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Vận chuyển</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Lợi nhuận ròng</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {rows.map((item) => (
                <tr key={item.label} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-900">{item.label}</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.revenue.toLocaleString('vi-VN')}đ</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.tax.toLocaleString('vi-VN')}đ</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.shipping.toLocaleString('vi-VN')}đ</td>
                  <td className="px-6 py-4 text-sm font-medium text-emerald-700">{item.netProfit.toLocaleString('vi-VN')}đ</td>
                </tr>
              ))}
              {!loading && rows.length === 0 && (
                <tr>
                  <td className="px-6 py-5 text-sm text-gray-500" colSpan={5}>Chưa có dữ liệu tài chính trong kỳ đã chọn.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default FinancialReports;
