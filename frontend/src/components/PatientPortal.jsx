import { useEffect, useState } from 'react';
import { bookMyAppointment, getDoctors, getMyAppointments } from '../api.js';

const readableDate = (value) => new Date(value).toLocaleString([], {
  weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit',
});

export default function PatientPortal({ user, onLogout }) {
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookingError, setBookingError] = useState('');
  const [bookingMessage, setBookingMessage] = useState('');
  const [booking, setBooking] = useState(false);
  const [form, setForm] = useState({ doctorId: '', appointmentTime: '', reason: '' });

  const refreshAppointments = () => getMyAppointments().then(setAppointments);

  useEffect(() => {
    Promise.all([getMyAppointments(), getDoctors()])
      .then(([mine, doctorList]) => {
        setAppointments(mine);
        setDoctors(doctorList);
        setForm((current) => ({ ...current, doctorId: current.doctorId || String(doctorList[0]?.id || '') }));
      })
      .catch((err) => setError(err.message || 'Could not load your appointments.'))
      .finally(() => setLoading(false));
  }, []);

  const bookAppointment = async (event) => {
    event.preventDefault();
    setBooking(true);
    setBookingError('');
    setBookingMessage('');
    try {
      await bookMyAppointment({
        doctorId: Number(form.doctorId),
        appointmentTime: `${form.appointmentTime}:00`,
        reason: form.reason.trim(),
      });
      await refreshAppointments();
      setForm((current) => ({ ...current, appointmentTime: '', reason: '' }));
      setBookingMessage('Appointment requested. Your visit is now listed in upcoming appointments.');
    } catch (err) {
      setBookingError(err.message || 'Could not book this appointment.');
    } finally {
      setBooking(false);
    }
  };

  const localDateTime = new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
    .toISOString().slice(0, 16);

  const upcoming = appointments.filter((a) => new Date(a.appointmentTime) >= new Date() && ['SCHEDULED', 'CONFIRMED'].includes(a.status));
  const past = appointments.filter((a) => !upcoming.includes(a));

  return (
    <div className="patient-page">
      <header className="patient-header">
        <div className="patient-brand"><span className="auth-mark">+</span><span>CareSlot</span></div>
        <div className="patient-account"><span>{user.name}</span><button className="btn btn-ghost" onClick={onLogout}>Sign out</button></div>
      </header>
      <main className="patient-main">
        <p className="eyebrow">Patient portal</p>
        <h1>Hello, {user.name.split(' ')[0]}.</h1>
        <p className="patient-welcome">Here are your appointments and visit details.</p>
        {error && <div className="banner-error">{error}</div>}
        <section className="panel patient-appointments">
          <div className="panel-head"><div><h2>Upcoming appointments</h2><p className="muted">Your next visits with the care team.</p></div><span className="patient-count">{upcoming.length} upcoming</span></div>
          {loading ? <div className="skeleton">Loading your appointments...</div> : upcoming.length ? (
            <ul className="patient-visit-list">
              {upcoming.map((a) => <li className="patient-visit" key={a.id}>
                <div className="visit-date"><span>{readableDate(a.appointmentTime)}</span><strong>{a.doctor}</strong><small>{a.specialty}</small></div>
                <span className={`status st-${a.status.toLowerCase().replace('_', '')}`}>{a.status.replace('_', ' ')}</span>
                {a.reason && <p>{a.reason}</p>}
              </li>)}
            </ul>
          ) : !loading && <div className="patient-empty"><span>♡</span><h3>No upcoming appointments</h3><p>Book a visit below to get started.</p></div>}
        </section>
        <section className="panel patient-appointments patient-booking">
          <div className="panel-head"><div><h2>Book an appointment</h2><p className="muted">Choose a doctor and a time. You can book multiple visits with any doctor.</p></div></div>
          {bookingError && <div className="banner-error" role="alert">{bookingError}</div>}
          {bookingMessage && <div className="banner-success" role="status">{bookingMessage}</div>}
          {!doctors.length ? <p className="muted">No doctors are available to book right now.</p> : (
            <form className="patient-booking-form" onSubmit={bookAppointment}>
              <label>Doctor<select value={form.doctorId} onChange={(event) => setForm({ ...form, doctorId: event.target.value })} required>
                {doctors.map((doctor) => <option key={doctor.id} value={doctor.id}>{doctor.name} · {doctor.specialty}</option>)}
              </select></label>
              <label>Date and time<input type="datetime-local" min={localDateTime} value={form.appointmentTime} onChange={(event) => setForm({ ...form, appointmentTime: event.target.value })} required /></label>
              <label>Reason for visit <span className="muted">(optional)</span><input type="text" maxLength="200" value={form.reason} onChange={(event) => setForm({ ...form, reason: event.target.value })} placeholder="For example, follow-up" /></label>
              <button className="btn btn-primary" type="submit" disabled={booking || !form.doctorId || !form.appointmentTime}>{booking ? 'Booking...' : 'Book appointment'}</button>
            </form>
          )}
        </section>
        {past.length > 0 && <section className="panel patient-appointments"><div className="panel-head"><div><h2>Visit history</h2><p className="muted">Your previous and cancelled appointments.</p></div></div><ul className="patient-visit-list">{past.map((a) => <li className="patient-visit past-visit" key={a.id}><div className="visit-date"><span>{readableDate(a.appointmentTime)}</span><strong>{a.doctor}</strong><small>{a.specialty}</small></div><span className={`status st-${a.status.toLowerCase().replace('_', '')}`}>{a.status.replace('_', ' ')}</span>{a.reason && <p>{a.reason}</p>}</li>)}</ul></section>}
      </main>
    </div>
  );
}
