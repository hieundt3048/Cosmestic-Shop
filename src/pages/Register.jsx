import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Lock, User as UserIcon, Mail, Phone as PhoneIcon, UserCircle, AlertCircle, CheckCircle } from 'lucide-react';
import { register } from '../api/authApi';

const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    fullName: '',
    phone: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [passwordErrors, setPasswordErrors] = useState([]);

  // Validate password theo yêu cầu BE
  const validatePassword = (password) => {
    const errors = [];
    if (password.length < 8) {
      errors.push('Tối thiểu 8 ký tự');
    }
    if (!/[a-z]/.test(password)) {
      errors.push('Chứa chữ thường');
    }
    if (!/[A-Z]/.test(password)) {
      errors.push('Chứa chữ hoa');
    }
    if (!/[0-9]/.test(password)) {
      errors.push('Chứa số');
    }
    if (!/[@$!%*?&]/.test(password)) {
      errors.push('Chứa ký tự đặc biệt (@$!%*?&)');
    }
    return errors;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
    setError(''); // Xóa lỗi khi user nhập

    // Validate password real-time
    if (name === 'password') {
      setPasswordErrors(validatePassword(value));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Validate password match
    if (formData.password !== formData.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return;
    }

    // Validate password strength
    const errors = validatePassword(formData.password);
    if (errors.length > 0) {
      setError('Mật khẩu chưa đủ mạnh. Vui lòng kiểm tra lại yêu cầu.');
      return;
    }

    // Validate password không chứa username
    if (formData.password.toLowerCase().includes(formData.username.toLowerCase())) {
      setError('Mật khẩu không được chứa tên đăng nhập');
      return;
    }

    // Validate password không chứa email
    const emailLocalPart = formData.email.split('@')[0];
    if (formData.password.toLowerCase().includes(emailLocalPart.toLowerCase())) {
      setError('Mật khẩu không được chứa email');
      return;
    }

    setLoading(true);

    try {
      // Loại bỏ confirmPassword trước khi gửi
      const { confirmPassword, ...registrationData } = formData;
      const response = await register(registrationData);
      
      console.log('Đăng ký thành công:', response);
      setSuccess(true);
      
      // Chuyển hướng đến trang đăng nhập sau 2 giây
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(err);
      console.error('Lỗi đăng ký:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-2xl">
        {/* Logo */}
        <div className="text-center mb-10">
          <Link to="/" className="inline-block">
            <h1 className="text-4xl font-light tracking-[0.3em] uppercase text-slate-900 mb-2">LUMIA</h1>
            <p className="text-xs uppercase tracking-widest text-slate-400">Skin Science</p>
          </Link>
        </div>

        {/* Form Card */}
        <div className="bg-white shadow-xl rounded-sm p-8 md:p-10">
          <h2 className="text-2xl font-light text-slate-900 mb-2 text-center">Tạo tài khoản</h2>
          <p className="text-sm text-slate-500 text-center mb-8">Tham gia cùng Lumia để trải nghiệm làm đẹp tuyệt vời</p>

          {/* Success Message */}
          {success && (
            <div className="mb-6 p-4 bg-green-50 border border-green-100 rounded-sm flex items-start space-x-3">
              <CheckCircle size={20} className="text-green-500 mt-0.5 flex-shrink-0" />
              <div>
                <p className="text-sm text-green-700 font-medium">Đăng ký thành công!</p>
                <p className="text-xs text-green-600 mt-1">Đang chuyển hướng đến trang đăng nhập...</p>
              </div>
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-sm flex items-start space-x-3">
              <AlertCircle size={20} className="text-red-500 mt-0.5 flex-shrink-0" />
              <p className="text-sm text-red-700">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Two columns layout for larger screens */}
            <div className="grid md:grid-cols-2 gap-6">
              {/* Username Field */}
              <div>
                <label className="block text-xs uppercase tracking-widest text-slate-600 mb-3 font-medium">
                  Tên đăng nhập *
                </label>
                <div className="relative">
                  <UserIcon className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-slate-200 rounded-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent transition-all text-sm"
                    placeholder="Username"
                    required
                    minLength={3}
                    maxLength={50}
                    pattern="[a-zA-Z0-9_]+"
                    title="Chỉ chứa chữ cái, số và dấu gạch dưới"
                  />
                </div>
              </div>

              {/* Full Name Field */}
              <div>
                <label className="block text-xs uppercase tracking-widest text-slate-600 mb-3 font-medium">
                  Họ và tên *
                </label>
                <div className="relative">
                  <UserCircle className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                  <input
                    type="text"
                    name="fullName"
                    value={formData.fullName}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-slate-200 rounded-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent transition-all text-sm"
                    placeholder="Nguyễn Văn A"
                    required
                    maxLength={100}
                  />
                </div>
              </div>
            </div>

            {/* Two columns for email and phone */}
            <div className="grid md:grid-cols-2 gap-6">
              {/* Email Field */}
              <div>
                <label className="block text-xs uppercase tracking-widest text-slate-600 mb-3 font-medium">
                  Email *
                </label>
                <div className="relative">
                  <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-slate-200 rounded-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent transition-all text-sm"
                    placeholder="email@example.com"
                    required
                  />
                </div>
              </div>

              {/* Phone Field */}
              <div>
                <label className="block text-xs uppercase tracking-widest text-slate-600 mb-3 font-medium">
                  Số điện thoại *
                </label>
                <div className="relative">
                  <PhoneIcon className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                  <input
                    type="tel"
                    name="phone"
                    value={formData.phone}
                    onChange={handleChange}
                    className="w-full pl-12 pr-4 py-3 border border-slate-200 rounded-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent transition-all text-sm"
                    placeholder="0123456789"
                    required
                    pattern="^0[0-9]{9}$"
                    title="Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số"
                  />
                </div>
              </div>
            </div>

            {/* Password Field */}
            <div>
              <label className="block text-xs uppercase tracking-widest text-slate-600 mb-3 font-medium">
                Mật khẩu *
              </label>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full pl-12 pr-12 py-3 border border-slate-200 rounded-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent transition-all text-sm"
                  placeholder="••••••••"
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
              
              {/* Password Requirements */}
              {formData.password && (
                <div className="mt-3 p-3 bg-slate-50 rounded-sm">
                  <p className="text-[10px] uppercase tracking-widest text-slate-500 mb-2 font-medium">Yêu cầu mật khẩu:</p>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <div className={passwordErrors.includes('Tối thiểu 8 ký tự') ? 'text-red-500' : 'text-green-600'}>
                      {passwordErrors.includes('Tối thiểu 8 ký tự') ? '✗' : '✓'} Tối thiểu 8 ký tự
                    </div>
                    <div className={passwordErrors.includes('Chứa chữ thường') ? 'text-red-500' : 'text-green-600'}>
                      {passwordErrors.includes('Chứa chữ thường') ? '✗' : '✓'} Chữ thường (a-z)
                    </div>
                    <div className={passwordErrors.includes('Chứa chữ hoa') ? 'text-red-500' : 'text-green-600'}>
                      {passwordErrors.includes('Chứa chữ hoa') ? '✗' : '✓'} Chữ hoa (A-Z)
                    </div>
                    <div className={passwordErrors.includes('Chứa số') ? 'text-red-500' : 'text-green-600'}>
                      {passwordErrors.includes('Chứa số') ? '✗' : '✓'} Số (0-9)
                    </div>
                    <div className={passwordErrors.includes('Chứa ký tự đặc biệt (@$!%*?&)') ? 'text-red-500' : 'text-green-600'}>
                      {passwordErrors.includes('Chứa ký tự đặc biệt (@$!%*?&)') ? '✗' : '✓'} Ký tự đặc biệt
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Confirm Password Field */}
            <div>
              <label className="block text-xs uppercase tracking-widest text-slate-600 mb-3 font-medium">
                Xác nhận mật khẩu *
              </label>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className="w-full pl-12 pr-12 py-3 border border-slate-200 rounded-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent transition-all text-sm"
                  placeholder="••••••••"
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {formData.confirmPassword && formData.password !== formData.confirmPassword && (
                <p className="mt-2 text-xs text-red-500">Mật khẩu xác nhận không khớp</p>
              )}
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading || success}
              className="w-full bg-slate-900 text-white py-3.5 text-sm uppercase tracking-widest font-medium hover:bg-slate-800 transition-all transform hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              {loading ? 'Đang xử lý...' : success ? 'Đăng ký thành công!' : 'Đăng ký'}
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

          {/* Login Link */}
          <p className="text-center text-sm text-slate-600">
            Đã có tài khoản?{' '}
            <Link to="/login" className="font-medium text-slate-900 hover:underline">
              Đăng nhập ngay
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

export default Register;
