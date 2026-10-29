import { useEffect, useState } from 'react';
import { createAppointment, getDoctors, getPatients } from '../api.js';

export default function NewAppointmentModal({ date, doctorId, onClose, onCreated }) {
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [form, setForm] = useState({ patientId: '', doctorId: '', time: '09:00', reason: '' });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    Promise.all([getPatients(), getDoctors()])
      .then(([p, d]) => {
        setPatients(p);
        setDoctors(d.filter((doctor) => doctor.id === doctorId));
        setForm((f) => ({ ...f, patientId: p[0]?.id ?? '', doctorId }));
      })
      .catch(() => setError('Could not load patients or doctors.'));
  }, [doctorId]);

  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createAppointment({
        patientId: Number(form.patientId),
        doctorId: Number(form.doctorId),
        appointmentTime: `${date}T${form.time}:00`,
        reason: form.reason || null,
      });
      onCreated();
    } catch (err) {
      setError(err.message);
      setSaving(false);
    }
  };

  return (
    <div className="overlay" onClick={onClose}>
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <button type="button" className="modal-close" onClick={onClose} aria-label="Close">×</button>
        <h2>New appointment</h2>
        <p className="muted">On {date}</p>

        <label className="field">
          <span>Patient</span>
          <select value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} required>
            {patients.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </label>

        <div className="row">
          <label className="field">
            <span>Doctor</span>
            <select value={form.doctorId} onChange={(e) => setForm({ ...form, doctorId: e.target.value })} required>
              {doctors.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </label>
          <label className="field">
            <span>Time</span>
            <input type="time" required value={form.time}
                   onChange={(e) => setForm({ ...form, time: e.target.value })} />
          </label>
        </div>

        <label className="field">
          <span>Reason</span>
          <input maxLength={200} value={form.reason}
                 onChange={(e) => setForm({ ...form, reason: e.target.value })}
                 placeholder="e.g. Follow-up, check-up" />
        </label>

        {error && <div className="form-error">{error}</div>}

        <div className="modal-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={saving || !patients.length}>
            {saving ? 'Booking...' : 'Book appointment'}
          </button>
        </div>
      </form>
    </div>
  );
}
