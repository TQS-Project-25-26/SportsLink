/* App logic para main_page_user - busca, filtros, mapa, modal, favoritos */
(() => {
  const apiBase = '/api/facilities'; // endpoint esperado do backend
  let facilities = [];
  let map, markersLayer;
  let userPos = null;
  const favoritesKey = 'sportslink:favs';
  const favs = new Set(JSON.parse(localStorage.getItem(favoritesKey) || '[]'));

  function initMap() {
    map = L.map('map', {zoomControl:true}).setView([40.0, -8.0], 6);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {attribution:'© OpenStreetMap'}).addTo(map);
    markersLayer = L.layerGroup().addTo(map);
  }

  function setUserLocation(lat, lon) {
    userPos = {lat, lon};
    map.setView([lat, lon], 13);
  }

  function fetchFacilities(query='', filters={}) {
    const url = new URL(apiBase, location.origin);
    if (query) url.searchParams.set('q', query);
    if (filters.minPrice) url.searchParams.set('minPrice', filters.minPrice);
    if (filters.maxPrice) url.searchParams.set('maxPrice', filters.maxPrice);
    if (filters.rating) url.searchParams.set('rating', filters.rating);

    return fetch(url).then(r => {
      if (!r.ok) throw new Error('no api');
      return r.json();
    }).catch(_ => Promise.resolve(simulatedData()));
  }

  function simulatedData(){
    return [
      {id:1,name:'Pavilhão Central',sport:'Futebol',lat:40.6405,lon:-8.6538,price:30,rating:4.5,photos:['/static/img/sample1.jpg'],equipment:['Bolas','Balizas'],description:'Pavilhão coberto com relvado sintético',availability:[]},
      {id:2,name:'Clube Padel Porto',sport:'Padel',lat:41.1579,lon:-8.6291,price:18,rating:4.2,photos:['/static/img/sample2.jpg'],equipment:['Raquetes','Redes'],description:'2 courts exteriores com iluminação',availability:[]},
      {id:3,name:'Centro Atletismo Gaia',sport:'Atletismo',lat:41.1265,lon:-8.5842,price:12,rating:4.8,photos:['/static/img/sample3.jpg'],equipment:['Pistas','Materiais de salto'],description:'Pistas profissionais',availability:[]}
    ];
  }

  function computeDistance(a,b) {
    if (!a || !b) return Infinity;
    const R=6371;
    const dLat=(b.lat-a.lat)*Math.PI/180;
    const dLon=(b.lon-a.lon)*Math.PI/180;
    const la=a.lat*Math.PI/180, lb=b.lat*Math.PI/180;
    const sin=(Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(la)*Math.cos(lb)*Math.sin(dLon/2)*Math.sin(dLon/2));
    const c=2*Math.atan2(Math.sqrt(sin), Math.sqrt(1-sin));
    return R*c; // km
  }

  function renderResults(list) {
    const items = document.getElementById('items');
    items.innerHTML = '';
    markersLayer.clearLayers();

    if (!list.length) {
      document.getElementById('empty').style.display='block';
      return;
    } else document.getElementById('empty').style.display='none';

    list.forEach(f => {
      const d = userPos ? computeDistance(userPos, {lat:f.lat,lon:f.lon}).toFixed(1) : null;
      const el = document.createElement('div'); el.className='card-item';
      el.innerHTML = `
        <img src="${f.photos?.[0] || '/static/img/placeholder.png'}" alt="">
        <div class="meta">
          <h4>${f.name}</h4>
          <div class="tags small">${f.sport} • ${f.rating}★ ${d? '• '+d+' km':''}</div>
          <div class="small" style="margin-top:8px">${f.description?.slice(0,80) || ''}</div>
        </div>
        <div style="display:flex;flex-direction:column;align-items:flex-end;gap:8px">
          <div class="small">${f.price}€ / h</div>
          <div>
            <button class="btn-small" data-id="${f.id}" data-action="view">Ver</button>
            <button class="fav" data-id="${f.id}" title="Favorito">${favs.has(f.id) ? '★' : '☆'}</button>
          </div>
        </div>
      `;
      items.appendChild(el);

      el.querySelector('[data-action="view"]').addEventListener('click', ()=> openModal(f));
      el.querySelector('.fav').addEventListener('click', (ev)=> toggleFav(f.id, ev.target));

      const m = L.marker([f.lat, f.lon]).addTo(markersLayer).bindPopup(`<strong>${f.name}</strong><br>${f.sport} • ${f.price}€/h`);
      m.on('click', ()=> openModal(f));
    });

    const coords = list.map(x=>[x.lat,x.lon]);
    if (coords.length) map.fitBounds(coords, {padding:[60,60]});
  }

  function toggleFav(id, btnEl){
    if (favs.has(id)) { favs.delete(id); btnEl.textContent='☆' }
    else { favs.add(id); btnEl.textContent='★' }
    localStorage.setItem(favoritesKey, JSON.stringify(Array.from(favs)));
  }

  function openModal(f) {
    document.getElementById('m-title').textContent = f.name;
    document.getElementById('m-desc').textContent = f.description || '';
    document.getElementById('m-equip').textContent = (f.equipment||[]).join(', ');
    document.getElementById('m-price').textContent = f.price + '€ / h';
    document.getElementById('m-rating').textContent = f.rating;
    const g = document.getElementById('m-gallery'); g.innerHTML='';
    (f.photos||[]).forEach(src=>{
      const i=document.createElement('img'); i.src=src; g.appendChild(i);
    });
    document.getElementById('modalFav').textContent = favs.has(f.id) ? '★' : '☆';
    document.getElementById('modal').classList.add('open');
    document.getElementById('modal').setAttribute('aria-hidden','false');
    document.getElementById('bookBtn').onclick = ()=> alert('Agendamento simulado — integrar backend');
    document.getElementById('modalFav').onclick = ()=> { toggleFav(f.id, document.getElementById('modalFav')); };
  }

  function closeModal(){
    document.getElementById('modal').classList.remove('open');
    document.getElementById('modal').setAttribute('aria-hidden','true');
  }

  // UI bindings
  document.addEventListener('DOMContentLoaded', ()=>{
    document.getElementById('searchBtn').addEventListener('click', ()=> applySearch());
    document.getElementById('q').addEventListener('keydown', e=> { if(e.key==='Enter'){ e.preventDefault(); applySearch(); }});
    document.getElementById('modalClose').addEventListener('click', closeModal);
    document.getElementById('modal').addEventListener('click', e=> { if (e.target===document.getElementById('modal')) closeModal(); });

    document.getElementById('locBtn').addEventListener('click', ()=>{
      if (!navigator.geolocation) { alert('Geolocalização não suportada'); return; }
      navigator.geolocation.getCurrentPosition(pos=>{
        setUserLocation(pos.coords.latitude, pos.coords.longitude);
        applySearch();
      }, ()=> alert('Não foi possível obter localização'));
    });

    document.getElementById('sort').addEventListener('change', ()=> applySearch());

    initMap();
    applySearch();
  });

  function applySearch(){
    const q = document.getElementById('q').value.trim();
    const filters = {
      minPrice: document.getElementById('minPrice').value || null,
      maxPrice: document.getElementById('maxPrice').value || null,
      rating: document.getElementById('ratingFilter').value || null
    };
    fetchFacilities(q, filters).then(data=>{
      facilities = data.map(f=>({ ...f }));
      if (userPos) facilities.forEach(x => x.distance = computeDistance(userPos, {lat:x.lat,lon:x.lon}));
      let list = facilities.filter(f=>{
        if (filters.minPrice && f.price < +filters.minPrice) return false;
        if (filters.maxPrice && f.price > +filters.maxPrice) return false;
        if (filters.rating && f.rating < +filters.rating) return false;
        if (q) {
          const s = (f.name+' '+(f.sport||'')).toLowerCase();
          if (!s.includes(q.toLowerCase())) return false;
        }
        return true;
      });

      const sort = document.getElementById('sort').value;
      if (sort === 'distance' && userPos) list.sort((a,b)=> (a.distance||999)-(b.distance||999));
      if (sort === 'price') list.sort((a,b)=> a.price-b.price);
      if (sort === 'rating') list.sort((a,b)=> b.rating-a.rating);

      renderResults(list);
    });
  }

  // Expor para debug
  window._sportslink = { applySearch, fetchFacilities, simulatedData };
})();