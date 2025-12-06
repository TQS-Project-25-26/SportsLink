// Auth helper utilities used by pages

// Sincronizar sessionStorage -> localStorage, para compatibilizar login/register com outras páginas
(function syncAuthStorage() {
  try {
    const sessionToken = sessionStorage.getItem('token');
    const sessionRole = sessionStorage.getItem('role');

    if (sessionToken && !localStorage.getItem('token')) {
      localStorage.setItem('token', sessionToken);
    }
    if (sessionRole && !localStorage.getItem('role')) {
      localStorage.setItem('role', sessionRole);
    }
  } catch (e) {
    console.warn('Failed to sync auth storage', e);
  }
})();

function authHeaders() {
  // Tenta vários sítios para garantir compatibilidade
  const sessionToken = sessionStorage.getItem('token');
  const localToken = localStorage.getItem('token');
  const authTokenRaw = localStorage.getItem('authToken'); // pode já vir com "Bearer "

  let headerValue = null;

  if (authTokenRaw) {
    // Se já tiver "Bearer", mantemos; senão, adicionamos
    headerValue = authTokenRaw.startsWith('Bearer ')
      ? authTokenRaw
      : 'Bearer ' + authTokenRaw;
  } else {
    const bareToken = localToken || sessionToken;
    if (bareToken) {
      headerValue = 'Bearer ' + bareToken;
    }
  }

  if (!headerValue) return {};
  return { 'Authorization': headerValue };
}

function logout() {
  // tenta fazer logout com o token atual (de onde quer que venha)
  const headers = Object.assign({ 'Content-Type': 'application/json' }, authHeaders());

  if (headers['Authorization']) {
    fetch('/api/auth/logout', {
      method: 'POST',
      headers: headers
    }).catch(() => { /* ignorar erros de rede */ });
  }

  // Limpar auth em ambos storages
  try {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('role');
  } catch (e) {
    console.warn('Error clearing sessionStorage auth', e);
  }

  try {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
  } catch (e) {
    console.warn('Error clearing localStorage auth', e);
  }

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
