const API = 'http://localhost:8080';

const tabs = document.querySelectorAll('.tabs button');
const forms = {
  login: document.getElementById('loginForm'),
  signup: document.getElementById('signupForm')
};

tabs.forEach(btn => {
  btn.addEventListener('click', () => {
    tabs.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    Object.values(forms).forEach(f => f.classList.remove('visible'));
    forms[btn.dataset.tab].classList.add('visible');
  });
});

const toast = document.getElementById('authToast');

function showToast(msg, type = 'success') {
  toast.textContent = msg;
  toast.className = `toast ${type === 'error' ? 'error' : ''}`;
  toast.classList.remove('hidden');
  setTimeout(() => toast.classList.add('hidden'), 4000);
}

document.getElementById('loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  const body = {
    email: document.getElementById('loginEmail').value,
    password: document.getElementById('loginPassword').value
  };
  try {
    const res = await fetch(`${API}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!res.ok) throw new Error(await res.text());
    const data = await res.json();
    localStorage.setItem('mini_token', data.token || 'demo');
    localStorage.setItem('mini_user_email', data.email || body.email);
    showToast('Signed in! Redirecting…');
    setTimeout(() => window.location.href = 'index.html', 800);
  } catch (err) {
    showToast('Login failed', 'error');
    console.error(err);
  }
});

document.getElementById('signupForm').addEventListener('submit', async e => {
  e.preventDefault();
  const body = {
    name: document.getElementById('signupName').value,
    email: document.getElementById('signupEmail').value,
    password: document.getElementById('signupPassword').value
  };
  try {
    const res = await fetch(`${API}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!res.ok) throw new Error(await res.text());
    const data = await res.json();
    showToast('Account created! Redirecting…');
    localStorage.setItem('mini_token', data.token);
    localStorage.setItem('mini_user_email', data.email || body.email);
    setTimeout(() => window.location.href = 'index.html', 800);
  } catch (err) {
    showToast('Signup failed', 'error');
    console.error(err);
  }
});


