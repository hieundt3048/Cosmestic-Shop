import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser } from '../api/authApi';
import { clearCart, getCartItems, saveOrder } from '../utils/shopStorage';

const Checkout = () => {
  const navigate = useNavigate();
  const currentUser = getCurrentUser();
  const [items] = useState(getCartItems());
  const [form, setForm] = useState({
    fullName: currentUser?.fullName || '',
    phone: currentUser?.phone || '',
    address: '',
    paymentMethod: 'COD',
  });

  const subtotal = useMemo(() => items.reduce((sum, item) => sum + item.price * item.quantity, 0), [items]);
  const shipping = subtotal > 0 ? 30000 : 0;
  const total = subtotal + shipping;

  const placeOrder = () => {
    if (!form.fullName || !form.phone || !form.address || items.length === 0) return;

    saveOrder({
      id: Date.now(),
      createdAt: new Date().toISOString(),
      customerName: form.fullName,
      phone: form.phone,
      address: form.address,
      paymentMethod: form.paymentMethod,
      status: 'PENDING',
      totalAmount: total,
      items,
    });

    clearCart();
    navigate('/account?tab=orders');
  };

  return (
    <div className="min-h-screen bg-slate-50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 p-6">
          <h1 className="text-2xl font-semibold text-slate-900 mb-6">Thanh toán</h1>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <input
              value={form.fullName}
              onChange={(e) => setForm((prev) => ({ ...prev, fullName: e.target.value }))}
              placeholder="Họ và tên"
              className="px-3 py-2 border border-slate-300 rounded-lg"
            />
            <input
              value={form.phone}
              onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))}
              placeholder="Số điện thoại"
              className="px-3 py-2 border border-slate-300 rounded-lg"
            />
          </div>

          <textarea
            value={form.address}
            onChange={(e) => setForm((prev) => ({ ...prev, address: e.target.value }))}
            placeholder="Địa chỉ giao hàng"
            rows={4}
            className="mt-4 w-full px-3 py-2 border border-slate-300 rounded-lg"
          />

          <div className="mt-4">
            <label className="block text-sm text-slate-600 mb-2">Phương thức thanh toán</label>
            <select
              value={form.paymentMethod}
              onChange={(e) => setForm((prev) => ({ ...prev, paymentMethod: e.target.value }))}
              className="px-3 py-2 border border-slate-300 rounded-lg"
            >
              <option value="COD">COD (Thanh toán khi nhận hàng)</option>
              <option value="BANK_TRANSFER">Chuyển khoản ngân hàng</option>
            </select>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-5 h-fit">
          <h2 className="font-semibold text-slate-900 mb-4">Xác nhận đơn hàng</h2>
          <div className="space-y-3 max-h-72 overflow-auto pr-2">
            {items.map((item) => (
              <div key={item.productId} className="flex justify-between text-sm text-slate-600">
                <span>{item.name} x{item.quantity}</span>
                <span>{(item.price * item.quantity).toLocaleString('vi-VN')}đ</span>
              </div>
            ))}
          </div>

          <div className="border-t border-slate-100 mt-4 pt-4 space-y-2 text-sm text-slate-600">
            <div className="flex justify-between"><span>Tạm tính</span><span>{subtotal.toLocaleString('vi-VN')}đ</span></div>
            <div className="flex justify-between"><span>Vận chuyển</span><span>{shipping.toLocaleString('vi-VN')}đ</span></div>
            <div className="flex justify-between font-semibold text-slate-900"><span>Tổng cộng</span><span>{total.toLocaleString('vi-VN')}đ</span></div>
          </div>

          <button
            onClick={placeOrder}
            className="w-full mt-4 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800"
          >
            Xác nhận đặt hàng
          </button>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
