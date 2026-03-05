import React, { useEffect, useMemo, useState } from 'react';
import { Percent, Plus, Ticket } from 'lucide-react';
import { productAPI } from '../api/productApi';

const ProductPricing = () => {
  const [products, setProducts] = useState([]);
  const [vouchers, setVouchers] = useState([
    { id: 1, code: 'WELCOME10', discount: 10, scope: 'STORE', active: true },
    { id: 2, code: 'SERUM20', discount: 20, scope: 'PRODUCT', active: true },
  ]);
  const [newVoucher, setNewVoucher] = useState({ code: '', discount: '', scope: 'STORE' });

  useEffect(() => {
    const loadProducts = async () => {
      const data = await productAPI.getAllProducts();
      setProducts(data || []);
    };
    loadProducts();
  }, []);

  const totalStock = useMemo(
    () => products.reduce((sum, item) => sum + (item.stockQuantity || 0), 0),
    [products]
  );

  const lowStockCount = useMemo(
    () => products.filter((item) => (item.stockQuantity || 0) <= 10).length,
    [products]
  );

  const addVoucher = (e) => {
    e.preventDefault();
    if (!newVoucher.code || !newVoucher.discount) return;

    setVouchers((prev) => [
      ...prev,
      {
        id: Date.now(),
        code: newVoucher.code.trim().toUpperCase(),
        discount: Number(newVoucher.discount),
        scope: newVoucher.scope,
        active: true,
      },
    ]);

    setNewVoucher({ code: '', discount: '', scope: 'STORE' });
  };

  const toggleVoucher = (id) => {
    setVouchers((prev) => prev.map((item) => (item.id === id ? { ...item, active: !item.active } : item)));
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Giá & khuyến mãi</h1>
        <p className="text-gray-600">Thiết lập voucher giảm giá và theo dõi tồn kho tổng thể.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Tổng sản phẩm</p>
          <p className="text-2xl font-semibold text-gray-900">{products.length}</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Tổng tồn kho</p>
          <p className="text-2xl font-semibold text-gray-900">{totalStock.toLocaleString('vi-VN')}</p>
        </div>
        <div className="bg-white rounded-lg shadow-sm p-4">
          <p className="text-sm text-gray-600">Sản phẩm tồn thấp</p>
          <p className="text-2xl font-semibold text-red-600">{lowStockCount}</p>
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
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {vouchers.map((voucher) => (
                  <tr key={voucher.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm font-medium text-gray-900">{voucher.code}</td>
                    <td className="px-6 py-4 text-sm text-gray-700">{voucher.discount}%</td>
                    <td className="px-6 py-4 text-sm text-gray-700">{voucher.scope === 'STORE' ? 'Toàn shop' : 'Theo sản phẩm'}</td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => toggleVoucher(voucher.id)}
                        className={`px-3 py-1.5 rounded-lg text-xs font-medium ${
                          voucher.active ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-100 text-gray-600'
                        }`}
                      >
                        {voucher.active ? 'Đang bật' : 'Đang tắt'}
                      </button>
                    </td>
                  </tr>
                ))}
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
                value={newVoucher.discount}
                onChange={(e) => setNewVoucher((prev) => ({ ...prev, discount: e.target.value }))}
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

            <button className="w-full mt-2 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800 inline-flex items-center justify-center gap-2">
              <Plus size={16} />
              Tạo voucher
            </button>
          </form>

          <div className="mt-5 pt-5 border-t border-gray-200 space-y-2 text-sm text-gray-600">
            <p className="inline-flex items-center gap-2"><Percent size={14} /> Quản lý giảm giá theo chiến dịch.</p>
            <p className="inline-flex items-center gap-2"><Ticket size={14} /> Kết hợp mã voucher cho toàn shop hoặc từng nhóm sản phẩm.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductPricing;
