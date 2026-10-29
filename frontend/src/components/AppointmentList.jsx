const STATUS_LABEL = {
  SCHEDULED: { text: 'Scheduled', cls: 'st-scheduled' },
  CONFIRMED: { text: 'Confirmed', cls: 'st-confirmed' },
  COMPLETED: { text: 'Completed', cls: 'st-completed' },
  NO_SHOW: { text: 'No-show', cls: 'st-noshow' },
  CANCELLED: { text: 'Cancelled', cls: 'st-cancelled' },
};

const RISK = {
  HIGH: { text: 'High risk', cls: 'risk-high' },
  MEDIUM: { text: 'Medium', cls: 'risk-medium' },
  LOW: { text: 'Low', cls: 'risk-low' },
};

const fmtTime = (iso, showDate) =>
  new Date(iso).toLocaleString([], showDate
    ? { weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }
    : { hour: '2-digit', minute: '2-digit' });

const initials = (name) =>
  name.split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase();

export default function AppointmentList({
  items,
  onStatus,
  onRemind,
  showDate = false,
  emptyTitle = 'No appointments here',
  emptyDescription = 'Try another day or filter.',
}) {
  if (items.length === 0) {
    return (
      <div className="empty">
        <div className="empty-icon">🗓️</div>
        <h3>{emptyTitle}</h3>
        <p className="muted">{emptyDescription}</p>
      </div>
    );
  }

  return (
    <ul className="appt-list">
      {items.map((a) => {
        const open = a.status === 'SCHEDULED' || a.status === 'CONFIRMED';
        const risk = open ? RISK[a.riskLevel] : null;
        const st = STATUS_LABEL[a.status];
        return (
          <li key={a.id} className={`appt ${open && a.riskScore >= 60 ? 'appt-alert' : ''}`}>
            <div className="appt-time">{fmtTime(a.appointmentTime, showDate)}</div>

            <div className="avatar">{initials(a.patient.name)}</div>

            <div className="appt-info">
              <div className="appt-name">
                {a.patient.name}
                <span className={`status ${st.cls}`}>{st.text}</span>
              </div>
              <div className="appt-meta">
                {a.doctor.name} · {a.reason || 'Consultation'} · {a.patient.phone}
              </div>
              <div className="appt-meta">
                Visits {a.patient.totalVisits} · No-shows {a.patient.noShows}
                {a.reminderCount > 0 && ` · Reminders sent: ${a.reminderCount}`}
              </div>
            </div>

            <div className="risk-col">
              {open ? (
                <>
                  <div className="risk-score">{a.riskScore}%</div>
                  <div className="risk-bar">
                    <span style={{ width: `${a.riskScore}%` }} className={risk.cls} />
                  </div>
                  <span className={`risk-label ${risk.cls}`}>{risk.text}</span>
                </>
              ) : (
                <span className="muted small">—</span>
              )}
            </div>

            <div className="appt-actions">
              {open && a.status !== 'CONFIRMED' && (
                <button className="btn btn-soft" onClick={() => onStatus(a, 'CONFIRMED', 'Marked as confirmed')}>
                  Confirm
                </button>
              )}
              {open && (
                <button className="btn btn-soft" onClick={() => onRemind(a)}>
                  Remind
                </button>
              )}
              {open && (
                <>
                  <button className="btn btn-good" onClick={() => onStatus(a, 'COMPLETED', 'Visit completed')}>
                    Arrived
                  </button>
                  <button className="btn btn-bad" onClick={() => onStatus(a, 'NO_SHOW', 'Recorded as no-show')}>
                    No-show
                  </button>
                </>
              )}
              {!open && <span className="muted small">Closed</span>}
            </div>
          </li>
        );
      })}
    </ul>
  );
}
