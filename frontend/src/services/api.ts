import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  withCredentials: true, // Required for HttpOnly cookies (refresh_token, csrf)
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add Authorization header
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('tua_access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors (401, etc.)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // If 401 Unauthorized, we might need to handle refresh token or just logout
    if (error.response?.status === 401) {
      // Logic for automatic refresh could go here, 
      // but for now let's just trigger a logout if it fails
      // localStorage.removeItem('tua_access_token');
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
