import { useEffect, useState } from "react";

const API = "http://localhost:8080/api";

export default function App() {
  const [users, setUsers] = useState([]);
  const [name, setName] = useState("");
  const[email, setEmail]=useState("");

  async function loadUsers() {
    const res = await fetch(`${API}/users`);
    const data = await res.json();
    setUsers(data);
  }

  async function createUSer() {
    if(!name || !email) return;

    await fetch(`${API}/users` ,{
      method:"POST",
      headers:{"Content-Type": "application/json"},
      body: JSON.stringify({name, email})
    });

    setName("");
    setEmail("");
    loadUsers();
  }

  useEffect(() => {
    loadUsers();
  }, []);

  return (
    <div style={{ padding: 20, fontFamily: "Arial" , maxWidth:400}}>
      <h2>Splitwise</h2>
      <h3>Create User</h3>
      <input
        placeholder="Name"
        value={name}
        onChange={e => setName(e.target.value)}
      />
      <br />
      <input
        placeholder="Email"
        value={email}
        onChange={e => setEmail(e.target.value)}
      />
      <br />
      <button onClick={createUSer}> Add User </button>
      

      <h3 style={{ marginTop: 20 }}>Users</h3>
      <ul>
        {users.map(u => (
          <li key={u.id}>
            {u.name} ({u.email}) — id: {u.id}
          </li>
        ))}
      </ul>
    </div>
  );
}
