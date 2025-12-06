// Auth helper utilities used by pages
function authHeaders() {
  const token = localStorage.getItem('token');
  if (!token) return {};
  return { 'Authorization': 'Bearer ' + token };
}

function logout() {
  const token = localStorage.getItem('token');
  if (token) {
    fetch('/api/auth/logout', { method: 'POST', headers: Object.assign({'Content-Type':'application/json'}, authHeaders()) }).catch(()=>{});
  }
  localStorage.removeItem('token');
  localStorage.removeItem('role');
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