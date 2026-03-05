import React from 'react';
import { Navigate } from 'react-router-dom';
import { getCurrentUser, isAuthenticated } from '../api/authApi';

const ProtectedRoute = ({ children, requiredRole }) => {
  const authenticated = isAuthenticated();
  const currentUser = getCurrentUser();

  if (!authenticated) {
    // Chưa đăng nhập -> redirect về login
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && currentUser?.role !== requiredRole) {
    // Đã đăng nhập nhưng không đủ quyền -> redirect về home
    return <Navigate to="/" replace />;
  }

  // OK -> render children
  return children;
};

export default ProtectedRoute;
