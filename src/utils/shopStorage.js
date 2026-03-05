const CART_KEY = 'shop_cart_items';
const WISHLIST_KEY = 'shop_wishlist_items';
const ORDERS_KEY = 'shop_customer_orders';

const readJson = (key, fallback = []) => {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
};

const writeJson = (key, value) => {
  localStorage.setItem(key, JSON.stringify(value));
};

export const getCartItems = () => readJson(CART_KEY, []);

export const setCartItems = (items) => writeJson(CART_KEY, items);

export const addToCart = (product, quantity = 1) => {
  const items = getCartItems();
  const index = items.findIndex((item) => item.productId === product.id);

  if (index >= 0) {
    items[index].quantity += quantity;
  } else {
    items.push({
      productId: product.id,
      name: product.productName || product.name,
      price: product.price,
      imageUrl: product.imageUrl,
      brandName: product.brand?.name,
      quantity,
    });
  }

  setCartItems(items);
  return items;
};

export const updateCartQuantity = (productId, quantity) => {
  const nextQuantity = Math.max(1, Number(quantity) || 1);
  const items = getCartItems().map((item) =>
    item.productId === productId ? { ...item, quantity: nextQuantity } : item
  );
  setCartItems(items);
  return items;
};

export const removeFromCart = (productId) => {
  const items = getCartItems().filter((item) => item.productId !== productId);
  setCartItems(items);
  return items;
};

export const clearCart = () => setCartItems([]);

export const getWishlistItems = () => readJson(WISHLIST_KEY, []);

export const toggleWishlist = (product) => {
  const items = getWishlistItems();
  const existed = items.some((item) => item.productId === product.id);
  const next = existed
    ? items.filter((item) => item.productId !== product.id)
    : [
        ...items,
        {
          productId: product.id,
          name: product.productName || product.name,
          price: product.price,
          imageUrl: product.imageUrl,
          brandName: product.brand?.name,
        },
      ];

  writeJson(WISHLIST_KEY, next);
  return { items: next, existed };
};

export const getOrderHistory = () => readJson(ORDERS_KEY, []);

export const saveOrder = (orderPayload) => {
  const orders = getOrderHistory();
  const next = [orderPayload, ...orders];
  writeJson(ORDERS_KEY, next);
  return next;
};
