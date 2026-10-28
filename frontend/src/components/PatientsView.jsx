import { useEffect, useState } from 'react';
import { createPatient, getPatients } from '../api.js';

export default function PatientsView({ refreshKey, onChanged, notify }) {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ name: '', phone: '', email: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setLoading(true);
    getPatients()
      .then(setPatients)
      .catch(() => notify('Could not load patients'))
      .finally(() => setLoading(false));
  }, [refreshKey, notify]);

  const add = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createPatient(form);
      setForm({ name: '', phone: '', email: '' });
      notify('Patient added ✅');
      onChanged();
    } catch (err) {
      notify(`Error: ${err.message}`);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="patients-grid">
      <section className="panel">
        <div className="panel-head">
          <div>
            <h2>Patients by no-show rate</h2>
            <p className="muted">Highest rate first. Patients with more history give a more reliable score.</p>
          </div>
        </div>

        {loading ? (
          <div className="skeleton">Loading patients...</div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Patient</th>
                <th>Phone</th>
                <th>Visits</th>
                <th>No-shows</th>
                <th>Rate</th>
              </tr>
            </thead>
            <tbody>
              {patients.map((p) => {
                const pct = Math.round(p.noShowRate * 100);
                const tone = pct >= 40 ? 'bad' : pct >= 20 ? 'warn' : 'good';
                return (
                  <tr key={p.id}>
                    <td className="strong">{p.name}</td>
                    <td className="muted">{p.phone}</td>
                    <td>{p.totalVisits}</td>
                    <td>{p.noShows}</td>
                    <td>
                      <div className="rate">
                        <div className="rate-bar">
                          <span className={`tone-${tone}`} style={{ width: `${Math.min(pct, 100)}%` }} />
                        </div>
                        <span className="rate-text">{pct}%</span>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </section>

      <form className="panel add-patient" onSubmit={add}>
        <h2>Add patient</h2>
        <p className="muted">Create a patient record before booking them.</p>
        <label className="field">
          <span>Full name</span>
          <input required maxLength={100} value={form.name}
                 onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </label>
        <label className="field">
          <span>Phone</span>
          <input required maxLength={30} value={form.phone}
                 onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </label>
        <label className="field">
          <span>Email (optional)</span>
          <input type="email" maxLength={120} value={form.email}
                 onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </label>
        <button className="btn btn-primary btn-block" disabled={saving}>
          {saving ? 'Saving...' : 'Save patient'}
        </button>
      </form>
    </div>
  );
}
