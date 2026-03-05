import React from 'react';
import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminDashboard from './pages/AdminDashboard';
import AdminOverview from './pages/AdminOverview';
import AddProduct from './pages/AddProduct';
import Products from './pages/Products';
import Orders from './pages/Orders';
import Customers from './pages/Customers';
import Employees from './pages/Employees';
import Brands from './pages/Brands';
import Categories from './pages/Categories';
import UserManagement from './pages/UserManagement';
import AuditLogs from './pages/AuditLogs';
import ProductPricing from './pages/ProductPricing';
import FinancialReports from './pages/FinancialReports';
import SystemSettings from './pages/SystemSettings';
import EmployeeWorkspace from './pages/EmployeeWorkspace';
import Catalog from './pages/Catalog';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import Account from './pages/Account';
import ProtectedRoute from './components/ProtectedRoute';
import StorefrontLayout from './components/StorefrontLayout';

function App() {
  return (
    <Routes>
      <Route element={<StorefrontLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/catalog" element={<Catalog />} />
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route
          path="/account"
          element={
            <ProtectedRoute requiredRole="CUSTOMER">
              <Account />
            </ProtectedRoute>
          }
        />
      </Route>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        path="/employee"
        element={
          <ProtectedRoute requiredRole="EMPLOYEE">
            <EmployeeWorkspace />
          </ProtectedRoute>
        }
      />
      
      {/* Admin Routes - Protected */}
      <Route 
        path="/admin" 
        element={
          <ProtectedRoute requiredRole="ADMIN">
            <AdminDashboard />
          </ProtectedRoute>
        }
      >
        <Route index element={<AdminOverview />} />
        <Route path="products" element={<Products />} />
        <Route path="add-product" element={<AddProduct />} />
        <Route path="orders" element={<Orders />} />
        <Route path="customers" element={<Customers />} />
        <Route path="employees" element={<Employees />} />
        <Route path="user-management" element={<UserManagement />} />
        <Route path="audit-logs" element={<AuditLogs />} />
        <Route path="brands" element={<Brands />} />
        <Route path="categories" element={<Categories />} />
        <Route path="product-pricing" element={<ProductPricing />} />
        <Route path="financial-reports" element={<FinancialReports />} />
        <Route path="system-settings" element={<SystemSettings />} />
      </Route>
    </Routes>
  );
}

export default App;
