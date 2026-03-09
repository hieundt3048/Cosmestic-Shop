import React, { useEffect, useMemo, useState } from 'react';
import { Percent, Plus, Ticket } from 'lucide-react';
import { productAPI } from '../api/productApi';

const ProductPricing = () => {
  const [products, setProducts] = useState([]);
  const [vouchers, setVouchers] = useState([]);
  const [inventorySummary, setInventorySummary] = useState({
    totalProducts: 0,
    totalStockUnits: 0,
    totalInventoryValue: 0,
    lowStockProducts: 0,
    outOfStockProducts: 0,
    lowStockThreshold: 10,
    restockSuggestions: [],
  });
  const [newVoucher, setNewVoucher] = useState({
    code: '',
    discountPercent: '',
    scope: 'STORE',
    productId: '',
  });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError('');
        const [productData, voucherData, inventoryData] = await Promise.all([
          productAPI.getAllProducts(),
          productAPI.getAllVouchers(),
          productAPI.getInventorySummary(10),
        ]);

        setProducts(productData || []);
        setVouchers(voucherData || []);
        setInventorySummary(inventoryData || {
          totalProducts: 0,
          totalStockUnits: 0,
          totalInventoryValue: 0,
          lowStockProducts: 0,
          outOfStockProducts: 0,
          lowStockThreshold: 10,
          restockSuggestions: [],
        });
      } catch (err) {
        setError('Không thể tải dữ liệu giá và khuyến mãi. Vui lòng thử lại.');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const activeVoucherCount = useMemo(() => vouchers.filter((item) => item.active).length, [vouchers]);

  const addVoucher = async (e) => {
    e.preventDefault();
    if (!newVoucher.code || !newVoucher.discountPercent) return;
    if (newVoucher.scope === 'PRODUCT' && !newVoucher.productId) return;

    try {
      setSubmitting(true);
      setError('');
      const createdVoucher = await productAPI.createVoucher({
        code: newVoucher.code,
        discountPercent: Number(newVoucher.discountPercent),
        scope: newVoucher.scope,
        productId: newVoucher.scope === 'PRODUCT' ? Number(newVoucher.productId) : null,
      });

      setVouchers((prev) => [createdVoucher, ...prev]);
      setNewVoucher({ code: '', discountPercent: '', scope: 'STORE', productId: '' });
    } catch (err) {
      setError(err?.response?.data || 'Không thể tạo voucher mới.');
    } finally {
      setSubmitting(false);
    }
  };

  const toggleVoucher = async (voucher) => {
    try {
      const updatedVoucher = await productAPI.updateVoucherStatus(voucher.id, !voucher.active);
      setVouchers((prev) => prev.map((item) => (item.id === voucher.id ? updatedVoucher : item)));
    } catch (err) {
      setError('Không thể cập nhật trạng thái voucher.');
    }
  };

  const removeVoucher = async (voucherId) => {
    if (!window.confirm('Bạn có chắc muốn xóa voucher này?')) return;

    try {
      await productAPI.deleteVoucher(voucherId);
      setVouchers((prev) => prev.filter((item) => item.id !== voucherId));
    } catch (err) {
      setError('Không thể xóa voucher.');
    }
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Giá & khuyến mãi</h1>
        <p className="text-gray-600">Thiết lập voucher giảm giá và theo dõi tồn kho cấp vĩ mô để lên kế hoạch nhập hàng.</p>
      </div>

      {error && (
        <div className="mb-4 p-3 rounded-lg border border-red-200 bg-red-50 text-red-700 text-sm">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Tổng sản phẩm</p>
          <p className="text-2xl font-semibold text-gray-900">{inventorySummary.totalProducts.toLocaleString('vi-VN')}</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Tổng tồn kho</p>
          <p className="text-2xl font-semibold text-gray-900">{inventorySummary.totalStockUnits.toLocaleString('vi-VN')}</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Sản phẩm tồn thấp</p>
          <p className="text-2xl font-semibold text-red-600">{inventorySummary.lowStockProducts.toLocaleString('vi-VN')}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Giá trị tồn kho ước tính</p>
          <p className="text-xl font-semibold text-gray-900">{inventorySummary.totalInventoryValue.toLocaleString('vi-VN')}đ</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Sản phẩm hết hàng</p>
          <p className="text-xl font-semibold text-red-600">{inventorySummary.outOfStockProducts.toLocaleString('vi-VN')}</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Voucher đang bật</p>
          <p className="text-xl font-semibold text-emerald-700">{activeVoucherCount.toLocaleString('vi-VN')}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <div className="xl:col-span-2 bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Danh sách voucher</h2>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mã</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Giảm giá</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Phạm vi</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sản phẩm</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {vouchers.map((voucher) => (
                  <tr key={voucher.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">{voucher.code}</td>
                    <td className="px-6 py-4 text-sm text-gray-700">{voucher.discountPercent}%</td>
                    <td className="px-6 py-4 text-sm text-gray-700">{voucher.scope === 'STORE' ? 'Toàn shop' : 'Theo sản phẩm'}</td>
                    <td className="px-6 py-4 text-sm text-gray-700">{voucher.product?.name || 'Toàn bộ sản phẩm'}</td>
                    <td className="px-6 py-4 text-right">
                      <div className="inline-flex items-center gap-2">
                        <button
                          onClick={() => toggleVoucher(voucher)}
                          className={`px-3 py-1.5 rounded-lg text-xs font-medium ${
                            voucher.active ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-100 text-gray-600'
                          }`}
                        >
                          {voucher.active ? 'Đang bật' : 'Đang tắt'}
                        </button>
                        <button
                          onClick={() => removeVoucher(voucher.id)}
                          className="px-3 py-1.5 rounded-lg text-xs font-medium bg-red-50 text-red-600 hover:bg-red-100"
                        >
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {!loading && vouchers.length === 0 && (
                  <tr>
                    <td className="px-6 py-6 text-sm text-gray-500" colSpan={5}>
                      Chưa có voucher nào. Hãy tạo voucher mới để chạy khuyến mãi.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Thêm voucher</h2>
          <form onSubmit={addVoucher} className="space-y-3">
            <div>
              <label className="block text-sm text-gray-600 mb-1">Mã voucher</label>
              <input
                value={newVoucher.code}
                onChange={(e) => setNewVoucher((prev) => ({ ...prev, code: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                placeholder="VD: FLASH15"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">% giảm</label>
              <input
                type="number"
                min="1"
                max="100"
                value={newVoucher.discountPercent}
                onChange={(e) => setNewVoucher((prev) => ({ ...prev, discountPercent: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                placeholder="10"
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">Phạm vi áp dụng</label>
              <select
                value={newVoucher.scope}
                onChange={(e) => setNewVoucher((prev) => ({ ...prev, scope: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              >
                <option value="STORE">Toàn shop</option>
                <option value="PRODUCT">Theo sản phẩm</option>
              </select>
            </div>

            {newVoucher.scope === 'PRODUCT' && (
              <div>
                <label className="block text-sm text-gray-600 mb-1">Sản phẩm áp dụng</label>
                <select
                  value={newVoucher.productId}
                  onChange={(e) => setNewVoucher((prev) => ({ ...prev, productId: e.target.value }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                >
                  <option value="">-- Chọn sản phẩm --</option>
                  {products.map((product) => (
                    <option key={product.id} value={product.id}>
                      {product.productName || product.name}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <button
              disabled={submitting}
              className="w-full mt-2 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 inline-flex items-center justify-center gap-2 disabled:opacity-60"
            >
              <Plus size={16} />
              {submitting ? 'Đang tạo...' : 'Tạo voucher'}
            </button>
          </form>

          <div className="mt-5 pt-5 border-t border-gray-200 space-y-2 text-sm text-gray-600">
            <p className="inline-flex items-center gap-2"><Percent size={14} /> Quản lý giảm giá theo chiến dịch.</p>
            <p className="inline-flex items-center gap-2"><Ticket size={14} /> Kết hợp mã voucher cho toàn shop hoặc từng nhóm sản phẩm.</p>
          </div>
        </div>
      </div>

      <div className="mt-6 bg-white rounded-lg shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between gap-3">
          <h2 className="text-lg font-semibold text-gray-900">Đề xuất nhập hàng (vĩ mô)</h2>
          <span className="text-sm text-gray-500">Ngưỡng cảnh báo: {inventorySummary.lowStockThreshold}</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sản phẩm</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tồn hiện tại</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Đề xuất nhập thêm</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ngân sách ước tính</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {inventorySummary.restockSuggestions.map((item) => (
                <tr key={item.productId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm font-medium text-gray-900">{item.productName}</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.currentStock}</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.recommendedRestockUnits}</td>
                  <td className="px-6 py-4 text-sm text-gray-700">{item.estimatedBudget.toLocaleString('vi-VN')}đ</td>
                </tr>
              ))}
              {!loading && inventorySummary.restockSuggestions.length === 0 && (
                <tr>
                  <td className="px-6 py-6 text-sm text-gray-500" colSpan={4}>
                    Không có sản phẩm tồn thấp theo ngưỡng hiện tại.
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

export default ProductPricing;
