import React, { useMemo } from 'react';
import { Download, FileBarChart2 } from 'lucide-react';

const monthlyData = [
  { month: '01/2026', revenue: 520000000, shipping: 38000000, tax: 52000000, netProfit: 138000000 },
  { month: '02/2026', revenue: 560000000, shipping: 41000000, tax: 56000000, netProfit: 151000000 },
  { month: '03/2026', revenue: 610000000, shipping: 44000000, tax: 61000000, netProfit: 167000000 },
];

const FinancialReports = () => {
  const totals = useMemo(
    () =>
      monthlyData.reduce(
        (acc, item) => {
          acc.revenue += item.revenue;
          acc.shipping += item.shipping;
          acc.tax += item.tax;
          acc.netProfit += item.netProfit;
          return acc;
        },
        { revenue: 0, shipping: 0, tax: 0, netProfit: 0 }
      ),
    []
  );

  const exportCsv = () => {
    const rows = [
      ['Thang', 'Doanh thu', 'Chi phi van chuyen', 'Thue', 'Loi nhuan rong'],
      ...monthlyData.map((item) => [item.month, item.revenue, item.shipping, item.tax, item.netProfit]),
    ];
    const content = rows.map((row) => row.join(',')).join('\n');
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
        <button
          onClick={exportCsv}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800"
        >
          <Download size={16} />
          Xuất CSV
        </button>
      </div>

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
            Chi tiết theo tháng
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
              {monthlyData.map((item) => (
                <tr key={item.month} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-900">{item.month}</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.revenue.toLocaleString('vi-VN')}đ</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.tax.toLocaleString('vi-VN')}đ</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.shipping.toLocaleString('vi-VN')}đ</td>
                  <td className="px-6 py-4 text-sm font-medium text-emerald-700">{item.netProfit.toLocaleString('vi-VN')}đ</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default FinancialReports;
