import { useState } from 'react';
import { login, registerDoctor, registerPatient } from '../api.js';

export default function AuthPage({ onAuthenticated }) {
  const [mode, setMode] = useState('login');
  const [role, setRole] = useState('PATIENT');
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', specialty: '' });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));
  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const account = mode === 'login'
        ? await login({ email: form.email, password: form.password, role })
        : role === 'DOCTOR'
          ? await registerDoctor({ name: form.name, email: form.email, password: form.password, specialty: form.specialty })
          : await registerPatient({ name: form.name, email: form.email, password: form.password, phone: form.phone });
      onAuthenticated(account);
    } catch (err) {
      if (mode === 'login' && err.status === 401) {
        setError(`No ${role.toLowerCase()} account matches those credentials. Check the selected account type, or create an account below.`);
      } else {
        setError(err.message || 'Could not sign in. Please try again.');
      }
    } finally {
      setSaving(false);
    }
  };

  const registration = mode === 'register';

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <div className="auth-brand"><span className="auth-mark">+</span><span>CareSlot</span></div>
        <p className="eyebrow">Clinic appointment care</p>
        <h1>{registration ? 'Create your account' : 'Welcome back'}</h1>
        <p className="auth-intro">{registration ? 'Choose your access to get started.' : 'Sign in to continue to your CareSlot workspace.'}</p>

        <div className="auth-switch" role="group" aria-label="Account type">
          <button type="button" aria-pressed={role === 'PATIENT'} className={role === 'PATIENT' ? 'selected' : ''} onClick={() => setRole('PATIENT')}>Patient</button>
          <button type="button" aria-pressed={role === 'DOCTOR'} className={role === 'DOCTOR' ? 'selected' : ''} onClick={() => setRole('DOCTOR')}>Doctor</button>
        </div>

        <form onSubmit={submit}>
          {registration && <label className="field"><span>{role === 'DOCTOR' ? 'Doctor name' : 'Full name'}</span><input autoComplete="name" maxLength={100} required value={form.name} onChange={(e) => update('name', e.target.value)} /></label>}
          <label className="field"><span>Email</span><input type="email" autoComplete="email" maxLength={120} required value={form.email} onChange={(e) => update('email', e.target.value)} /></label>
          {registration && role === 'PATIENT' && <label className="field"><span>Phone</span><input type="tel" autoComplete="tel" maxLength={30} required value={form.phone} onChange={(e) => update('phone', e.target.value)} /></label>}
          {registration && role === 'DOCTOR' && <label className="field"><span>Specialty</span><input maxLength={80} required placeholder="e.g. General Practice" value={form.specialty} onChange={(e) => update('specialty', e.target.value)} /></label>}
          <label className="field"><span>Password</span><input type="password" autoComplete={registration ? 'new-password' : 'current-password'} minLength={8} maxLength={72} required value={form.password} onChange={(e) => update('password', e.target.value)} /><small className="auth-hint">Use at least 8 characters.</small></label>
          {error && <div className="form-error" role="alert">{error}</div>}
          <button className="btn btn-primary btn-block auth-submit" disabled={saving}>{saving ? 'Please wait…' : registration ? `Create ${role === 'DOCTOR' ? 'doctor' : 'patient'} account` : `Sign in as ${role.toLowerCase()}`}</button>
        </form>

        <p className="auth-footer">{registration ? 'Already have an account?' : 'New to CareSlot?'}{' '}
          <button type="button" className="auth-link" onClick={() => { setMode(registration ? 'login' : 'register'); setError(''); }}>
            {registration ? 'Sign in' : 'Create an account'}
          </button>
        </p>
      </section>
      <aside className="auth-aside">
        <div className="auth-orbit orbit-one" /><div className="auth-orbit orbit-two" />
        <p className="eyebrow">Better visits start here</p>
        <h2>{role === 'DOCTOR' ? 'Your clinic, clearly in view.' : 'Your care, all in one place.'}</h2>
        <p>{role === 'DOCTOR' ? 'Coordinate appointments, follow up with patients, and make every slot count.' : 'Keep track of upcoming appointments and stay connected with your care team.'}</p>
        <div className="auth-aside-note"><span>✓</span> A simpler way to manage care</div>
      </aside>
    </main>
  );
}
