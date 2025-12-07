// Auth helper utilities used by pages

/**
 * Obtém o token JWT armazenado em localStorage
 */
function getToken() {
  return localStorage.getItem('token');
}

/**
 * Obtém o role do utilizador armazenado em localStorage
 */
function getRole() {
  return localStorage.getItem('role');
}

/**
 * Verifica se o utilizador está autenticado
 */
function isAuthenticated() {
  return !!getToken();
}

/**
 * Armazena credenciais de autenticação
 */
function setAuthCredentials(token, role) {
  localStorage.setItem('token', token);
  localStorage.setItem('role', role || 'RENTER');
}

/**
 * Retorna headers com autenticação JWT
 */
function authHeaders() {
  const token = getToken();
  
  const headers = {
    'Content-Type': 'application/json'
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return headers;
}

/**
 * Logout do utilizador
 * 1. Notifica o backend para adicionar o token à blacklist
 * 2. Limpa localStorage
 * 3. Redireciona para login
 */
function logout() {
  const token = getToken();

  // Tentar fazer logout no backend (blacklist token)
  if (token) {
    fetch('/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      credentials: 'include'  // Incluir cookies
    })
    .catch(err => {
      console.warn('Logout request failed (continuing anyway):', err);
    });
  }

  // Limpar credenciais do localStorage
  try {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
  } catch (e) {
    console.warn('Error clearing localStorage:', e);
  }

  // Redirecionar para página de login
  window.location.href = '/index.html';
}

// Simple toast helper: projects can override `showBootstrapToast` for nicer UI.
function showBootstrapToast(message, kind='info'){
  // kind: 'info'|'success'|'warning'|'danger'
  try{
    // if bootstrap toasts are present, create a temporary one
    const containerId = 'sl-toast-container';
    let container = document.getElementById(containerId);
    if (!container) {
      container = document.createElement('div');
      container.id = containerId;
      container.className = 'position-fixed bottom-0 end-0 p-3';
      container.style.zIndex = 1080;
      document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${kind} border-0 show`;
    toast.role = 'alert'; toast.ariaLive = 'assertive'; toast.ariaAtomic = 'true';
    toast.style.minWidth = '200px';
    toast.innerHTML = `<div class="d-flex"><div class="toast-body">${message}</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button></div>`;
    container.appendChild(toast);
    setTimeout(()=>{ toast.remove(); }, 4000);
  } catch(e) { alert(message); }
}

/**
 * Proteção de página: redireciona para login se não autenticado
 * Use isto no <head> de páginas protegidas:
 * <script> requireAuth(); </script>
 */
function requireAuth() {
  if (!isAuthenticated()) {
    window.location.href = '/index.html';
  }
}
