import { useCallback, useEffect, useRef, useState } from 'react';
import { getAppointments, getPastAppointments, getUpcomingAppointments, getSummary, setStatus, remind } from './api.js';
import Sidebar from './components/Sidebar.jsx';
import StatCards from './components/StatCards.jsx';
import AppointmentList from './components/AppointmentList.jsx';
import PatientsView from './components/PatientsView.jsx';
import NewAppointmentModal from './components/NewAppointmentModal.jsx';
import AuthPage from './components/AuthPage.jsx';
import PatientPortal from './components/PatientPortal.jsx';

const todayStr = () => {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000);
  return local.toISOString().slice(0, 10);
};

export default function App() {
  const [user, setUser] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [view, setView] = useState('dashboard');
  const [date, setDate] = useState(todayStr());
  const [appointments, setAppointments] = useState([]);
  const [pastAppointments, setPastAppointments] = useState([]);
  const [upcomingAppointments, setUpcomingAppointments] = useState([]);
  const [summary, setSummary] = useState(null);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [adding, setAdding] = useState(false);
  const [toast, setToast] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);

  const toastTimer = useRef(null);
  const notify = useCallback((msg) => {
    setToast(msg);
    clearTimeout(toastTimer.current);
    toastTimer.current = setTimeout(() => setToast(''), 2600);
  }, []);

  const reload = useCallback(() => setRefreshKey((k) => k + 1), []);

  const acceptAccount = useCallback((account) => {
    localStorage.setItem('careslot-token', account.token);
    setUser(account);
    setAuthLoading(false);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('careslot-token');
    setUser(null);
  }, []);

  useEffect(() => {
    localStorage.removeItem('careslot-token');
    setUser(null);
    setAuthLoading(false);
  }, []);

  useEffect(() => {
    window.addEventListener('careslot-auth-expired', logout);
    return () => window.removeEventListener('careslot-auth-expired', logout);
  }, [logout]);

  useEffect(() => {
    if (!user || user.role !== 'DOCTOR') return;
    setLoading(true);
    Promise.all([getAppointments(date), getSummary(date), getPastAppointments(), getUpcomingAppointments()])
      .then(([list, sum, past, upcoming]) => {
        setAppointments(list);
        setSummary(sum);
        setPastAppointments(past);
        setUpcomingAppointments(upcoming);
        setError('');
      })
      .catch(() => setError('Could not reach the server. Is the Spring Boot backend running on port 8081?'))
      .finally(() => setLoading(false));
  }, [date, refreshKey, user]);

  const act = async (fn, msg) => {
    try {
      await fn();
      notify(msg);
      reload();
    } catch (e) {
      notify(`Error: ${e.message}`);
    }
  };

  const onStatus = (appt, status, msg) => act(() => setStatus(appt.id, status), msg);
  const onRemind = (appt) => act(() => remind(appt.id), `Reminder logged for ${appt.patient.name}`);

  const visible = appointments.filter((a) => {
    if (filter === 'HIGH') return a.riskScore >= 60 && ['SCHEDULED', 'CONFIRMED'].includes(a.status);
    if (filter === 'OPEN') return ['SCHEDULED', 'CONFIRMED'].includes(a.status);
    return true;
  });

  if (authLoading) return <div className="auth-loading">Opening your secure workspace…</div>;
  if (!user) return <AuthPage onAuthenticated={acceptAccount} />;
  if (user.role === 'PATIENT') return <PatientPortal user={user} onLogout={logout} />;

  return (
    <div className="app">
      <Sidebar view={view} setView={setView} user={user} onLogout={logout} />

      <main className="main">
        <header className="topbar">
          <div>
            <p className="eyebrow">Clinic overview</p>
            <h1>{view === 'dashboard' ? 'Today at a glance' : 'Patients'}</h1>
          </div>
          <div className="topbar-actions">
            <span className="doctor-greeting">Dr. {user.name.replace(/^Dr\.\s*/i, '')}</span>
            {view === 'dashboard' && (
              <>
                <input type="date" className="date-input" value={date} onChange={(e) => setDate(e.target.value)} />
                <button className="btn btn-primary" onClick={() => setAdding(true)}>+ New appointment</button>
              </>
            )}
          </div>
        </header>

        {error && <div className="banner-error">{error}</div>}

        {view === 'dashboard' && (
          <>
            <StatCards summary={summary} />

            <section className="panel patient-appointments">
              <div className="panel-head">
                <div>
                  <h2>Past appointments</h2>
                  <p className="muted">Previous visits and their recorded outcomes.</p>
                </div>
                <span className="patient-count">{pastAppointments.length} past</span>
              </div>
              <AppointmentList
                items={pastAppointments}
                onStatus={onStatus}
                onRemind={onRemind}
                showDate
                emptyTitle="No past appointments"
                emptyDescription="Previous appointments for this doctor will appear here."
              />
            </section>

            <section className="panel">
              <div className="panel-head">
                <div>
                  <h2>{date === todayStr() ? "Today's appointments" : `Appointments for ${date}`}</h2>
                  <p className="muted">Sorted by time. Risk shows the estimated chance of a no-show.</p>
                </div>
                <div className="chips">
                  {[
                    { k: 'ALL', label: 'All' },
                    { k: 'OPEN', label: 'Open' },
                    { k: 'HIGH', label: '⚠ High risk' },
                  ].map((c) => (
                    <button
                      key={c.k}
                      className={`chip ${filter === c.k ? 'active' : ''}`}
                      onClick={() => setFilter(c.k)}
                    >
                      {c.label}
                    </button>
                  ))}
                </div>
              </div>

              {loading ? (
                <div className="skeleton">Loading appointments...</div>
              ) : (
                <AppointmentList
                  items={visible}
                  onStatus={onStatus}
                  onRemind={onRemind}
                  emptyTitle="No appointments for this day"
                  emptyDescription="Choose another date or book an appointment."
                />
              )}
            </section>

            <section className="panel patient-appointments">
              <div className="panel-head">
                <div>
                  <h2>Upcoming appointments</h2>
                  <p className="muted">Appointments scheduled for future dates.</p>
                </div>
                <span className="patient-count">{upcomingAppointments.length} upcoming</span>
              </div>
              <AppointmentList
                items={upcomingAppointments}
                onStatus={onStatus}
                onRemind={onRemind}
                showDate
                emptyTitle="No upcoming appointments"
                emptyDescription="Future appointments for this doctor will appear here."
              />
            </section>
          </>
        )}

        {view === 'patients' && <PatientsView refreshKey={refreshKey} onChanged={reload} notify={notify} />}
      </main>

      {adding && (
        <NewAppointmentModal
          date={date}
          doctorId={user.actorId}
          onClose={() => setAdding(false)}
          onCreated={() => {
            setAdding(false);
            notify('Appointment booked ✅');
            reload();
          }}
        />
      )}
      {toast && <div className="toast">{toast}</div>}
    </div>
  );
}
