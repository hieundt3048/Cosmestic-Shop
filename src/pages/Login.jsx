import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Lock, User as UserIcon, AlertCircle } from 'lucide-react';
import { login } from '../api/authApi';

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError(''); // Xóa lỗi khi user nhập
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await login(formData);
      
      // Chuyển hướng dựa trên role
      if (response.user?.role === 'ADMIN') {
        navigate('/admin');
      } else if (response.user?.role === 'EMPLOYEE') {
        navigate('/employee');
      } else {
        navigate('/');
      }
    } catch (err) {
      setError(err);
      console.error('Lỗi đăng nhập:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-10">
          <Link to="/" className="inline-block">
            <h1 className="text-4xl font-light tracking-[0.3em] uppercase text-slate-900 mb-2">LUMIA</h1>
            <p className="text-xs uppercase tracking-widest text-slate-400">Skin Science</p>
          </Link>
        </div>

        {/* Form Card */}
        <div className="bg-white shadow-xl rounded-sm p-8 md:p-10">
          <h2 className="text-2xl font-light text-slate-900 mb-2 text-center">Đăng nhập</h2>
          <p className="text-sm text-slate-500 text-center mb-8">Chào mừng bạn trở lại với Lumia</p>

          {/* Error Message */}
          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-sm flex items-start space-x-3">
              <AlertCircle size={20} className="text-red-500 mt-0.5 flex-shrink-0" />
              <p className="text-sm text-red-700">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Username Field */}
            <div>
              <label className="block text-xs uppercase tracking-widest text-slate-600 mb-3 font-medium">
                Tên đăng nhập
              </label>
              <div className="relative">
                <UserIcon className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  className="w-full pl-12 pr-4 py-3 border border-slate-200 rounded-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent transition-all text-sm"
                  placeholder="Nhập tên đăng nhập"
                  required
                />
              </div>
            </div>

            {/* Password Field */}
            <div>
              <label className="block text-xs uppercase tracking-widest text-slate-600 mb-3 font-medium">
                Mật khẩu
              </label>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full pl-12 pr-12 py-3 border border-slate-200 rounded-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent transition-all text-sm"
                  placeholder="Nhập mật khẩu"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            {/* Forgot Password */}
            <div className="flex justify-end">
              <a href="#" className="text-xs text-slate-500 hover:text-slate-900 transition-colors uppercase tracking-wider">
                Quên mật khẩu?
              </a>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-slate-900 text-white py-3.5 text-sm uppercase tracking-widest font-medium hover:bg-slate-800 transition-all transform hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
            </button>
          </form>

          {/* Divider */}
          <div className="relative my-8">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-100"></div>
            </div>
            <div className="relative flex justify-center text-xs uppercase tracking-widest">
              <span className="px-4 bg-white text-slate-400">hoặc</span>
            </div>
          </div>

          {/* Register Link */}
          <p className="text-center text-sm text-slate-600">
            Chưa có tài khoản?{' '}
            <Link to="/register" className="font-medium text-slate-900 hover:underline">
              Đăng ký ngay
            </Link>
          </p>
        </div>

        {/* Back to Home */}
        <div className="text-center mt-8">
          <Link to="/" className="text-xs uppercase tracking-widest text-slate-400 hover:text-slate-900 transition-colors">
            ← Quay về trang chủ
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
