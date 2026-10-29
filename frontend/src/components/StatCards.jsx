export default function StatCards({ summary }) {
  if (!summary) return null;

  const cards = [
    { label: 'Booked today', value: summary.total, tone: 'blue', icon: '📅' },
    { label: 'Confirmed', value: summary.confirmed, tone: 'green', icon: '✅' },
    { label: 'High risk', value: summary.highRisk, tone: 'red', icon: '⚠️' },
    { label: 'No-show rate', value: `${summary.noShowRate}%`, tone: 'amber', icon: '📉' },
  ];

  return (
    <div className="stat-grid">
      {cards.map((c) => (
        <div key={c.label} className={`stat-card tone-${c.tone}`}>
          <div className="stat-icon">{c.icon}</div>
          <div>
            <div className="stat-number">{c.value}</div>
            <div className="stat-caption">{c.label}</div>
          </div>
        </div>
      ))}
    </div>
  );
}
