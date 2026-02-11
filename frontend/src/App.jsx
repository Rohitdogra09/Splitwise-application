import { useEffect, useMemo, useState } from "react";
import "./App.css";

const API = "http://localhost:8080/api";

export default function App() {
  // USERS
  const [users, setUsers] = useState([]);
  const [userName, setUserName] = useState("");
  const [email, setEmail] = useState("");

  // GROUPS
  const [groups, setGroups] = useState([]);
  const [groupName, setGroupName] = useState("");
  const [selectedGroup, setSelectedGroup] = useState(null);

  // MEMBERS
  const [members, setMembers] = useState([]);
  const [memberUserId, setMemberUserId] = useState("");

  // EXPENSE
  const [expenseTitle, setExpenseTitle] = useState("");
  const [expenseAmount, setExpenseAmount] = useState("");
  const [paidByUserId, setPaidByUserId] = useState("");

  // BALANCES + SETTLEMENTS
  const [balances, setBalances] = useState([]);
  const [settlements, setSettlements] = useState([]);

  // supports both backend formats:
  // DTO: { userId, userName, userEmail }
  // Entity: { id, user: { id, name, email } }
  const normalizedMembers = useMemo(() => {
    return (members || [])
      .map(m => ({
        id: m.userId ?? m.user?.id,
        name: m.userName ?? m.user?.name,
        email: m.userEmail ?? m.user?.email
      }))
      .filter(m => m.id && m.name);
  }, [members]);

  async function safeJson(url, options) {
    try {
      const res = await fetch(url, options);

      if (res.status === 204) return null;

      const contentType = res.headers.get("content-type") || "";
      const isJson = contentType.includes("application/json");

      if (!res.ok) {
        const errText = isJson ? JSON.stringify(await res.json()) : await res.text();
        console.error("HTTP Error", res.status, url, errText);
        return null;
      }

      return isJson ? await res.json() : await res.text();
    } catch (e) {
      console.error("Fetch failed:", url, e);
      return null;
    }
  }

  // ---------- Loaders ----------
  async function loadUsers() {
    const data = await safeJson(`${API}/users`);
    if (data) setUsers(data);
  }

  async function loadGroups() {
    const data = await safeJson(`${API}/groups`);
    if (data) setGroups(data);
  }

  async function loadMembers(groupId) {
    const data = await safeJson(`${API}/groups/${groupId}/members`);
    setMembers(data || []);
  }

  async function loadBalances(groupId) {
    const data = await safeJson(`${API}/groups/${groupId}/balances`);
    setBalances(data || []);
  }

  async function loadSettlements(groupId) {
    const data = await safeJson(`${API}/groups/${groupId}/settlements`);
    setSettlements(data || []);
  }

  async function refreshGroupData(groupId) {
    await loadMembers(groupId);
    await loadBalances(groupId);
    await loadSettlements(groupId);
  }

  // ---------- Actions ----------
  async function createUser() {
    if (!userName || !email) return;

    await safeJson(`${API}/users`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: userName, email })
    });

    setUserName("");
    setEmail("");
    loadUsers();
  }

  async function createGroup() {
    if (!groupName) return;

    const created = await safeJson(`${API}/groups`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: groupName })
    });

    if (!created) return;

    setGroupName("");
    await loadGroups();
    setSelectedGroup(created);

    // reset
    setMemberUserId("");
    setExpenseTitle("");
    setExpenseAmount("");
    setPaidByUserId("");

    await refreshGroupData(created.id);
  }

  async function selectGroup(g) {
    setSelectedGroup(g);

    setMemberUserId("");
    setExpenseTitle("");
    setExpenseAmount("");
    setPaidByUserId("");

    await refreshGroupData(g.id);
  }

  async function addMember() {
    if (!selectedGroup || !memberUserId) return;

    await safeJson(`${API}/groups/${selectedGroup.id}/members`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId: Number(memberUserId) })
    });

    setMemberUserId("");
    await refreshGroupData(selectedGroup.id);
  }

  // Equal split expense
  async function addExpense() {
    if (!selectedGroup) return;
    if (!expenseTitle) return;

    const amountNum = Number(expenseAmount);
    const payerIdNum = Number(paidByUserId);

    if (!amountNum || amountNum <= 0) return;
    if (!payerIdNum) return;

    if (normalizedMembers.length === 0) {
      alert("Add members first.");
      return;
    }

    const perHead = Math.round((amountNum / normalizedMembers.length) * 100) / 100;

    const splits = {};
    normalizedMembers.forEach(m => {
      splits[m.id] = perHead;
    });

    // fix rounding difference
    const sum = perHead * normalizedMembers.length;
    const diff = Math.round((amountNum - sum) * 100) / 100;
    if (diff !== 0) {
      const firstId = normalizedMembers[0].id;
      splits[firstId] = Math.round((splits[firstId] + diff) * 100) / 100;
    }

    const body = {
      title: expenseTitle,
      amount: amountNum,
      paidByUserId: payerIdNum,
      splits
    };

    const created = await safeJson(`${API}/groups/${selectedGroup.id}/expenses`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    if (!created) return;

    setExpenseTitle("");
    setExpenseAmount("");
    setPaidByUserId("");
    await refreshGroupData(selectedGroup.id);
  }

  async function settle(fromUserId, toUserId, amount) {
    if (!selectedGroup) return;

    const created = await safeJson(`${API}/groups/${selectedGroup.id}/payments`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ fromUserId, toUserId, amount })
    });

    if (!created) return;

    await refreshGroupData(selectedGroup.id);
  }

  useEffect(() => {
    loadUsers();
    loadGroups();
  }, []);

  function balClass(x) {
    if (x > 0.0001) return "balancePos";
    if (x < -0.0001) return "balanceNeg";
    return "balanceZero";
  }

  return (
    <div className="container">
      <div className="header">💸 Splitwise Dashboard</div>

      {/* ROW 1 */}
      <div className="rowGrid">
        <div className="card">
          <h2>Users</h2>
          <div className="sectionRow">
            <input
              className="input"
              placeholder="Name"
              value={userName}
              onChange={e => setUserName(e.target.value)}
            />
            <input
              className="input"
              placeholder="Email"
              value={email}
              onChange={e => setEmail(e.target.value)}
            />
            <button className="btn" onClick={createUser}>
              Add
            </button>
          </div>

          <ul className="list">
            {users.map(u => (
              <li key={u.id}>
                <b>{u.name}</b> <span style={{ opacity: 0.7 }}>({u.email})</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="card">
          <h2>Groups</h2>
          <div className="sectionRow">
            <input
              className="input"
              placeholder="Group name"
              value={groupName}
              onChange={e => setGroupName(e.target.value)}
            />
            <button className="btn" onClick={createGroup}>
              Create
            </button>
          </div>

          <ul className="list">
            {groups.map(g => (
              <li
                key={g.id}
                onClick={() => selectGroup(g)}
                style={{
                  cursor: "pointer",
                  fontWeight: selectedGroup?.id === g.id ? "bold" : "normal",
                  color: selectedGroup?.id === g.id ? "#764ba2" : "black"
                }}
              >
                {g.name}
              </li>
            ))}
          </ul>

          {!selectedGroup ? (
            <p style={{ opacity: 0.7, marginTop: 10 }}>Select a group</p>
          ) : (
            <p style={{ opacity: 0.7, marginTop: 10 }}>
              Selected: <b>{selectedGroup.name}</b>
            </p>
          )}
        </div>
      </div>

      {/* ROW 2 */}
      <div className="rowGrid">
        <div className="card">
          <h2>Members</h2>

          {!selectedGroup ? (
            <p style={{ opacity: 0.7 }}>Select a group first</p>
          ) : (
            <>
              <div className="sectionRow">
                <select
                  className="select"
                  value={memberUserId}
                  onChange={e => setMemberUserId(e.target.value)}
                >
                  <option value="">Select user</option>
                  {users.map(u => (
                    <option key={u.id} value={u.id}>
                      {u.name}
                    </option>
                  ))}
                </select>

                <button className="btn" onClick={addMember}>
                  Add Member
                </button>
              </div>

              <ul className="list">
                {normalizedMembers.map(m => (
                  <li key={m.id}>
                    <b>{m.name}</b>{" "}
                    <span style={{ opacity: 0.7 }}>{m.email ? `(${m.email})` : ""}</span>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>

        <div className="card">
          <h2>Add Expense</h2>

          {!selectedGroup ? (
            <p style={{ opacity: 0.7 }}>Select a group first</p>
          ) : (
            <>
              <div className="sectionRow">
                <input
                  className="input"
                  placeholder="Title"
                  value={expenseTitle}
                  onChange={e => setExpenseTitle(e.target.value)}
                />
                <input
                  className="input"
                  placeholder="Amount"
                  value={expenseAmount}
                  onChange={e => setExpenseAmount(e.target.value)}
                />
              </div>

              <div className="sectionRow">
                <select
                  className="select"
                  value={paidByUserId}
                  onChange={e => setPaidByUserId(e.target.value)}
                >
                  <option value="">Paid by</option>
                  {normalizedMembers.map(m => (
                    <option key={m.id} value={m.id}>
                      {m.name}
                    </option>
                  ))}
                </select>

                <button className="btn" onClick={addExpense}>
                  Add Expense
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {/* ROW 3 */}
      <div className="rowGrid">
        <div className="card">
          <h2>Balances</h2>

          {!selectedGroup ? (
            <p style={{ opacity: 0.7 }}>Select a group</p>
          ) : (
            <ul className="list">
              {balances.map(b => (
                <li key={b.userId}>
                  {b.name} : <span className={balClass(b.balance)}>{b.balance}</span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card">
          <h2>Who Owes Who</h2>

          {!selectedGroup ? (
            <p style={{ opacity: 0.7 }}>Select a group</p>
          ) : settlements.length === 0 ? (
            <p>All Settled ✅</p>
          ) : (
            <ul className="list">
              {settlements.map((s, i) => (
                <li key={i} style={{ display: "flex", justifyContent: "space-between", gap: 10 }}>
                  <span>
                    {s.fromName} → {s.toName} : <b>{s.amount}</b>
                  </span>
                  <button className="btn" onClick={() => settle(s.fromUserId, s.toUserId, s.amount)}>
                    Settle
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
