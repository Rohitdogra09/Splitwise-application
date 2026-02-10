import { useEffect, useState } from "react";

const API = "http://localhost:8080/api";

export default function App() {
  // USERS
  const [users, setUsers] = useState([]);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");

  // GROUPS
  const [groups, setGroups] = useState([]);
  const [groupName, setGroupName] = useState("");
  const [selectedGroup, setSelectedGroup] = useState(null);

  // MEMBERS
  const [members, setMembers] = useState([]);
  const [memberUserId, setMemberUserId] = useState("");

  async function loadUsers() {
    const res = await fetch(`${API}/users`);
    setUsers(await res.json());
  }

  async function createUser() {
    if (!name || !email) return;

    await fetch(`${API}/users`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, email })
    });

    setName("");
    setEmail("");
    loadUsers();
  }

  async function loadGroups() {
    const res = await fetch(`${API}/groups`);
    setGroups(await res.json());
  }

  async function createGroup() {
    if (!groupName) return;

    const res = await fetch(`${API}/groups`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: groupName })
    });

    const created = await res.json();
    setGroupName("");
    await loadGroups();
    setSelectedGroup(created);
  }

  async function loadMembers(groupId) {
    const res = await fetch(`${API}/groups/${groupId}/members`);
    setMembers(await res.json());
  }

  async function addMember() {
    if (!selectedGroup || !memberUserId) return;

    await fetch(`${API}/groups/${selectedGroup.id}/members`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId: Number(memberUserId) })
    });

    setMemberUserId("");
    loadMembers(selectedGroup.id);
  }

  // When selecting a group, load its members
  async function selectGroup(g) {
    setSelectedGroup(g);
    setMemberUserId("");
    await loadMembers(g.id);
  }

  useEffect(() => {
    loadUsers();
    loadGroups();
  }, []);

  return (
    <div style={{ padding: 20, fontFamily: "Arial", maxWidth: 800 }}>
      <h2>Splitwise</h2>

      {/* USERS */}
      <div style={{ border: "1px solid #ddd", padding: 15, borderRadius: 8 }}>
        <h3>Users</h3>

        <div style={{ display: "flex", gap: 10, marginBottom: 10 }}>
          <input
            style={{ flex: 1, padding: 8 }}
            placeholder="Name"
            value={name}
            onChange={e => setName(e.target.value)}
          />
          <input
            style={{ flex: 1, padding: 8 }}
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
          />
          <button onClick={createUser} style={{ padding: "8px 12px" }}>
            Add User
          </button>
        </div>

        <ul>
          {users.map(u => (
            <li key={u.id}>
              {u.id}. {u.name} ({u.email})
            </li>
          ))}
        </ul>
      </div>

      {/* GROUPS */}
      <div
        style={{
          border: "1px solid #ddd",
          padding: 15,
          borderRadius: 8,
          marginTop: 20
        }}
      >
        <h3>Groups</h3>

        <div style={{ display: "flex", gap: 10, marginBottom: 10 }}>
          <input
            style={{ flex: 1, padding: 8 }}
            placeholder="Group name (e.g., Munich Trip)"
            value={groupName}
            onChange={e => setGroupName(e.target.value)}
          />
          <button onClick={createGroup} style={{ padding: "8px 12px" }}>
            Create Group
          </button>
        </div>

        <ul>
          {groups.map(g => (
            <li key={g.id}>
              <button
                onClick={() => selectGroup(g)}
                style={{
                  border: "none",
                  background: "transparent",
                  color: "blue",
                  cursor: "pointer",
                  padding: 0
                }}
              >
                {g.name}
              </button>{" "}
              (id: {g.id})
            </li>
          ))}
        </ul>

        {selectedGroup && (
          <div style={{ marginTop: 15, padding: 10, background: "#f7f7f7" }}>
            <b>Selected Group:</b> {selectedGroup.name} (id:{" "}
            {selectedGroup.id})
          </div>
        )}
      </div>

      {/* MEMBERS */}
      {selectedGroup && (
        <div
          style={{
            border: "1px solid #ddd",
            padding: 15,
            borderRadius: 8,
            marginTop: 20
          }}
        >
          <h3>Members of "{selectedGroup.name}"</h3>

          <div style={{ display: "flex", gap: 10, marginBottom: 10 }}>
            <select
              style={{ flex: 1, padding: 8 }}
              value={memberUserId}
              onChange={e => setMemberUserId(e.target.value)}
            >
              <option value="">Select user to add</option>
              {users.map(u => (
                <option key={u.id} value={u.id}>
                  {u.name} (id: {u.id})
                </option>
              ))}
            </select>

            <button onClick={addMember} style={{ padding: "8px 12px" }}>
              Add Member
            </button>
          </div>

          <ul>
            {members.map(m => (
              <li key={m.id}>
                {m.userName} ({m.userEmail}) — userId: {m.userId}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
