import React, { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Minus, Plus, Trash2 } from 'lucide-react';
import { getCartItems, removeFromCart, updateCartQuantity } from '../utils/shopStorage';

const Cart = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState(getCartItems());

  const subtotal = useMemo(
    () => items.reduce((sum, item) => sum + item.price * item.quantity, 0),
    [items]
  );
  const shipping = subtotal > 0 ? 30000 : 0;
  const total = subtotal + shipping;

  const onQtyChange = (productId, qty) => {
    const next = updateCartQuantity(productId, qty);
    setItems(next);
  };

  const onRemove = (productId) => {
    const next = removeFromCart(productId);
    setItems(next);
  };

  return (
    <div className="min-h-screen bg-slate-50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-semibold text-slate-900">Giỏ hàng</h1>
          <Link to="/catalog" className="text-sm text-slate-600 hover:text-slate-900">Tiếp tục mua sắm</Link>
        </div>

        {items.length === 0 ? (
          <div className="bg-white rounded-xl border border-slate-200 p-8 text-center">
            <p className="text-slate-600 mb-4">Giỏ hàng của bạn đang trống.</p>
            <Link to="/catalog" className="px-4 py-2 rounded-lg bg-slate-900 text-white">Khám phá sản phẩm</Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 overflow-hidden">
              {items.map((item) => (
                <div key={item.productId} className="p-4 border-b last:border-b-0 border-slate-100 flex gap-4">
                  <img
                    src={item.imageUrl || 'https://via.placeholder.com/120x120?text=Product'}
                    alt={item.name}
                    className="w-24 h-24 object-cover rounded-lg"
                    loading="lazy"
                  />
                  <div className="flex-1">
                    <p className="font-medium text-slate-900">{item.name}</p>
                    <p className="text-sm text-slate-500 mb-2">{item.brandName || 'N/A'}</p>
                    <p className="text-sm font-semibold text-slate-900">{item.price.toLocaleString('vi-VN')}đ</p>
                  </div>
                  <div className="flex flex-col items-end justify-between">
                    <button onClick={() => onRemove(item.productId)} className="text-red-600 hover:text-red-700">
                      <Trash2 size={18} />
                    </button>
                    <div className="inline-flex items-center border border-slate-200 rounded-lg">
                      <button
                        className="px-2 py-1"
                        onClick={() => onQtyChange(item.productId, Math.max(1, item.quantity - 1))}
                      >
                        <Minus size={14} />
                      </button>
                      <span className="px-3 text-sm">{item.quantity}</span>
                      <button className="px-2 py-1" onClick={() => onQtyChange(item.productId, item.quantity + 1)}>
                        <Plus size={14} />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="bg-white rounded-xl border border-slate-200 p-5 h-fit">
              <h2 className="font-semibold text-slate-900 mb-4">Tóm tắt đơn hàng</h2>
              <div className="space-y-2 text-sm text-slate-600">
                <div className="flex justify-between"><span>Tạm tính</span><span>{subtotal.toLocaleString('vi-VN')}đ</span></div>
                <div className="flex justify-between"><span>Phí vận chuyển</span><span>{shipping.toLocaleString('vi-VN')}đ</span></div>
              </div>
              <div className="border-t border-slate-100 mt-4 pt-4 flex justify-between font-semibold text-slate-900">
                <span>Tổng cộng</span>
                <span>{total.toLocaleString('vi-VN')}đ</span>
              </div>
              <button
                onClick={() => navigate('/checkout')}
                className="w-full mt-4 px-4 py-2 rounded-lg bg-slate-900 text-white hover:bg-slate-800"
              >
                Tiến hành thanh toán
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Cart;
