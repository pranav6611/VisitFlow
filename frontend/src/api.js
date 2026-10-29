const BASE = import.meta.env.VITE_API_URL || '/api';

async function request(path, options = {}) {
  const { headers: requestHeaders, ...requestOptions } = options;
  const publicAuthRequest = /^\/auth\/(?:login|register\/)/.test(path);
  const token = publicAuthRequest ? null : localStorage.getItem('careslot-token');
  const res = await fetch(`${BASE}${path}`, {
    ...requestOptions,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...requestHeaders,
    },
  });
  if (res.status === 401 && token) {
    localStorage.removeItem('careslot-token');
    window.dispatchEvent(new Event('careslot-auth-expired'));
  }
  if (!res.ok) {
    let msg = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      msg = body.message || body.error || msg;
    } catch { /* no JSON body */ }
    const error = new Error(msg);
    error.status = res.status;
    throw error;
  }
  return res.status === 204 ? null : res.json();
}

export const login = (body) => request('/auth/login', { method: 'POST', body: JSON.stringify(body) });
export const registerDoctor = (body) => request('/auth/register/doctor', { method: 'POST', body: JSON.stringify(body) });
export const registerPatient = (body) => request('/auth/register/patient', { method: 'POST', body: JSON.stringify(body) });
export const getCurrentUser = () => request('/auth/me');
export const getMyAppointments = () => request('/appointments/mine');
export const bookMyAppointment = (body) =>
  request('/appointments/mine', { method: 'POST', body: JSON.stringify(body) });
export const getDoctors = () => request('/doctors');
export const getPatients = () => request('/patients');
export const getSummary = (date) => request(`/appointments/summary?date=${date}`);
export const getAppointments = (date) => request(`/appointments?date=${date}`);
export const getPastAppointments = () => request('/appointments/past');
export const getUpcomingAppointments = () => request('/appointments/upcoming');
export const createAppointment = (body) =>
  request('/appointments', { method: 'POST', body: JSON.stringify(body) });
export const setStatus = (id, status) =>
  request(`/appointments/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) });
export const remind = (id) => request(`/appointments/${id}/remind`, { method: 'POST' });
export const createPatient = (body) =>
  request('/patients', { method: 'POST', body: JSON.stringify(body) });
