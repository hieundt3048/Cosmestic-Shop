import React, { useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { getCurrentUser } from '../api/authApi';
import { getOrderHistory, getWishlistItems } from '../utils/shopStorage';

const Account = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = searchParams.get('tab') || 'profile';
  const currentUser = getCurrentUser();

  const [profile, setProfile] = useState({
    fullName: currentUser?.fullName || '',
    email: currentUser?.email || '',
    phone: currentUser?.phone || '',
  });

  const orders = useMemo(() => getOrderHistory(), []);
  const wishlist = useMemo(() => getWishlistItems(), []);

  const switchTab = (nextTab) => setSearchParams({ tab: nextTab });

  return (
    <div className="min-h-screen bg-slate-50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 grid grid-cols-1 md:grid-cols-4 gap-6">
        <aside className="md:col-span-1 bg-white rounded-xl border border-slate-200 p-4 h-fit">
          <h2 className="font-semibold text-slate-900 mb-4">Tài khoản của tôi</h2>
          <div className="space-y-2">
            <button onClick={() => switchTab('profile')} className={`w-full text-left px-3 py-2 rounded-lg ${tab === 'profile' ? 'bg-slate-900 text-white' : 'hover:bg-slate-100'}`}>Hồ sơ cá nhân</button>
            <button onClick={() => switchTab('orders')} className={`w-full text-left px-3 py-2 rounded-lg ${tab === 'orders' ? 'bg-slate-900 text-white' : 'hover:bg-slate-100'}`}>Lịch sử đơn hàng</button>
            <button onClick={() => switchTab('wishlist')} className={`w-full text-left px-3 py-2 rounded-lg ${tab === 'wishlist' ? 'bg-slate-900 text-white' : 'hover:bg-slate-100'}`}>Wishlist</button>
          </div>
          <Link to="/catalog" className="mt-4 inline-block text-sm text-slate-600 hover:text-slate-900">Tiếp tục mua sắm</Link>
        </aside>

        <section className="md:col-span-3 bg-white rounded-xl border border-slate-200 p-6">
          {tab === 'profile' && (
            <div>
              <h3 className="text-xl font-semibold text-slate-900 mb-4">Hồ sơ cá nhân</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <input
                  value={profile.fullName}
                  onChange={(e) => setProfile((prev) => ({ ...prev, fullName: e.target.value }))}
                  placeholder="Họ và tên"
                  className="px-3 py-2 border border-slate-300 rounded-lg"
                />
                <input
                  value={profile.phone}
                  onChange={(e) => setProfile((prev) => ({ ...prev, phone: e.target.value }))}
                  placeholder="Số điện thoại"
                  className="px-3 py-2 border border-slate-300 rounded-lg"
                />
                <input
                  value={profile.email}
                  onChange={(e) => setProfile((prev) => ({ ...prev, email: e.target.value }))}
                  placeholder="Email"
                  className="px-3 py-2 border border-slate-300 rounded-lg sm:col-span-2"
                />
              </div>
              <button className="mt-4 px-4 py-2 rounded-lg bg-slate-900 text-white">Lưu thay đổi</button>
            </div>
          )}

          {tab === 'orders' && (
            <div>
              <h3 className="text-xl font-semibold text-slate-900 mb-4">Lịch sử mua hàng</h3>
              {orders.length === 0 ? (
                <p className="text-slate-600">Bạn chưa có đơn hàng nào.</p>
              ) : (
                <div className="space-y-3">
                  {orders.map((order) => (
                    <div key={order.id} className="border border-slate-200 rounded-lg p-4">
                      <div className="flex justify-between items-center mb-2">
                        <p className="font-medium text-slate-900">Đơn #{order.id}</p>
                        <span className="text-xs px-2 py-1 rounded-full bg-yellow-100 text-yellow-700">{order.status}</span>
                      </div>
                      <p className="text-sm text-slate-600">Ngày đặt: {new Date(order.createdAt).toLocaleString('vi-VN')}</p>
                      <p className="text-sm text-slate-600">Tổng tiền: {order.totalAmount.toLocaleString('vi-VN')}đ</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {tab === 'wishlist' && (
            <div>
              <h3 className="text-xl font-semibold text-slate-900 mb-4">Danh sách yêu thích</h3>
              {wishlist.length === 0 ? (
                <p className="text-slate-600">Bạn chưa có sản phẩm yêu thích nào.</p>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {wishlist.map((item) => (
                    <div key={item.productId} className="border border-slate-200 rounded-lg p-3 flex gap-3">
                      <img src={item.imageUrl || 'https://via.placeholder.com/100'} alt={item.name} className="w-20 h-20 rounded object-cover" loading="lazy" />
                      <div>
                        <p className="font-medium text-slate-900">{item.name}</p>
                        <p className="text-sm text-slate-500">{item.brandName || 'N/A'}</p>
                        <p className="text-sm font-semibold text-slate-900">{(item.price || 0).toLocaleString('vi-VN')}đ</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default Account;
