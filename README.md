# Cosmetic Shop Frontend

React application cho hệ thống quản lý mỹ phẩm.

## 🚀 Công nghệ sử dụng

- **React 18** - UI Framework
- **Vite** - Build tool & Dev server
- **Tailwind CSS** - Styling
- **Axios** - HTTP Client
- **ESLint** - Code linting

## 📋 Yêu cầu

- Node.js version 18.0 trở lên
- npm hoặc yarn
- Backend API đang chạy tại `http://localhost:8080`

## 🛠️ Cài đặt

```bash
# Di chuyển vào thư mục project
cd "E:\tai\CosmeticShop Project\CosmeticFE"

# Cài đặt dependencies (nếu chưa cài)
npm install
```

## 🏃 Chạy ứng dụng

### Development Mode

```bash
npm run dev
```

Ứng dụng sẽ chạy tại: **http://localhost:3000**

### Build cho Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## 📁 Cấu trúc thư mục

```
CosmeticFE/
├── src/
│   ├── api/
│   │   └── productApi.js          # API service cho products
│   ├── components/
│   │   └── ProductForm.jsx        # Form thêm sản phẩm
│   ├── App.jsx                    # Main App component
│   ├── main.jsx                   # Entry point
│   └── index.css                  # Global styles với Tailwind
├── public/                        # Static assets
├── index.html                     # HTML template
├── vite.config.js                 # Vite configuration
├── tailwind.config.js             # Tailwind configuration
└── package.json                   # Dependencies
```

## 🔌 Kết nối Backend

### API Endpoints được sử dụng:

- **POST** `/products/create_product` - Tạo sản phẩm mới
- **GET** `/products` - Lấy danh sách tất cả sản phẩm
- **GET** `/products/{id}` - Lấy sản phẩm theo ID
- **GET** `/products/search?query=...` - Tìm kiếm sản phẩm

### Cấu hình API

File: `src/api/productApi.js`

```javascript
const API_BASE_URL = 'http://localhost:8080';
```

Nếu backend chạy ở port khác, thay đổi giá trị này.

## 📝 Sử dụng ProductForm

Form hỗ trợ tạo sản phẩm mới với các trường:

### Thông tin Product (Required):
- **Tên sản phẩm** (name) *
- **Giá** (price) *
- **Số lượng** (stockQuantity) *
- **Mô tả** (description)
- **Link hình ảnh** (imageUrl)

### Thông tin Brand & Category:
- **Brand ID** - ID của brand đã tồn tại
- **Category ID** - ID của category đã tồn tại

> ⚠️ **Lưu ý**: Hiện tại form chỉ hỗ trợ tạo Product. Brand và Category cần được tạo trước trong database.

## ✅ Kiểm tra kết nối

### 1. Đảm bảo Backend đang chạy

```bash
# Kiểm tra backend tại
curl http://localhost:8080/products
```

Hoặc mở browser: http://localhost:8080/products

### 2. Kiểm tra CORS Configuration

Backend đã được cấu hình để cho phép requests từ `http://localhost:3000`

File: `cosmeticBE/src/main/java/com/cosmeticshop/cosmetic/Config/WebConfig.java`

### 3. Test tạo sản phẩm

1. Mở http://localhost:3000
2. Điền form với thông tin sản phẩm
3. Nhập Brand ID và Category ID (phải tồn tại trong DB)
4. Click "LƯU THÔNG TIN"
5. Kiểm tra Console để xem request/response

## 🐛 Xử lý lỗi thường gặp

### Lỗi: "Không thể kết nối đến server"

**Nguyên nhân**: Backend chưa chạy hoặc chạy sai port

**Giải pháp**:
```bash
# Di chuyển vào folder backend
cd "E:\tai\CosmeticShop Project\cosmeticBE"

# Chạy backend
mvn spring-boot:run
# hoặc
./mvnw spring-boot:run
```

### Lỗi: "CORS error"

**Nguyên nhân**: Backend chưa cấu hình CORS

**Giải pháp**: Đảm bảo file `WebConfig.java` đã được tạo và backend đã restart

### Lỗi: "Foreign key constraint"

**Nguyên nhân**: Brand ID hoặc Category ID không tồn tại

**Giải pháp**: 
- Tạo Brand và Category trong database trước
- Hoặc để trống Brand ID và Category ID (nếu nullable)

## 📦 Scripts có sẵn

```bash
npm run dev        # Chạy development server
npm run build      # Build production
npm run preview    # Preview production build
npm run lint       # Chạy ESLint kiểm tra code
```

## 🎨 Tùy chỉnh Styling

Project sử dụng **Tailwind CSS**. Để tùy chỉnh:

1. Mở `tailwind.config.js`
2. Thêm custom colors, fonts, spacing, etc. vào `theme.extend`

```javascript
theme: {
  extend: {
    colors: {
      primary: '#your-color',
    },
  },
}
```

## 🔄 Next Steps

- [ ] Thêm form tạo Brand
- [ ] Thêm form tạo Category  
- [ ] Hiển thị danh sách sản phẩm
- [ ] Thêm chức năng sửa/xóa sản phẩm
- [ ] Tìm kiếm và filter sản phẩm
- [ ] Upload ảnh thay vì nhập URL
- [ ] Authentication & Authorization
- [ ] Responsive design improvements

## 🤝 Hỗ trợ

Nếu gặp vấn đề, kiểm tra:

1. Console log trong browser (F12)
2. Network tab để xem API requests
3. Backend logs (terminal chạy Spring Boot)
4. Database có Brand và Category chưa

## 📄 License

Copyright © 2026 Cosmetic Shop
