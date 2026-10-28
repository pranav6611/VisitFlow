const ITEMS = [
  { key: 'dashboard', label: 'Dashboard', icon: '📊' },
  { key: 'patients', label: 'Patients', icon: '🧑‍⚕️' },
];

export default function Sidebar({ view, setView, user, onLogout }) {
  return (
    <aside className="sidebar">
      <div className="logo">
        <span className="logo-mark">✚</span>
        <div>
          <div className="logo-name">CareSlot</div>
          <div className="logo-sub">No-show reduction</div>
        </div>
      </div>

      <nav>
        {ITEMS.map((i) => (
          <button
            key={i.key}
            className={`nav-item ${view === i.key ? 'active' : ''}`}
            onClick={() => setView(i.key)}
          >
            <span>{i.icon}</span> {i.label}
          </button>
        ))}
      </nav>

      <div className="sidebar-tip">
        <div className="tip-title">💡 Tip</div>
        <p>Confirm high-risk visits 24 hours ahead. A quick reminder can cut no-shows noticeably.</p>
      </div>

      <div className="sidebar-account">
        <div className="sidebar-avatar">{user?.name?.[0]?.toUpperCase() || 'D'}</div>
        <div className="sidebar-account-copy"><strong>{user?.name}</strong><span>Doctor account</span></div>
        <button className="sidebar-logout" onClick={onLogout} aria-label="Sign out" title="Sign out">↗</button>
      </div>
    </aside>
  );
}
