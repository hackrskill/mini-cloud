/* Modern frontend for MiniCloud
   Features:
   - Dark mode toggle (persisted)
   - Smooth page switching
   - Buckets CRUD (create, delete, list)
   - File upload via drag/drop + file input + multi-select
   - File preview thumbnails (images) + icons for other types
   - Click file to open in new tab
   - Toast notifications
   - Loading spinner
   - Lambda & Queue UI placeholders (wired where possible)
*/

const API = "http://localhost:8080"; // backend base

// ----- SIMPLE AUTH GUARD -----
const authToken = localStorage.getItem('mini_token');
if (!authToken) {
  // if user not authenticated, go to auth page
  if (!window.location.pathname.endsWith('auth.html')) {
    window.location.href = 'auth.html';
  }
} else {
  const email = localStorage.getItem('mini_user_email');
  const emailEl = document.getElementById('userEmail');
  if (emailEl && email) {
    emailEl.textContent = email;
  }
}

const logoutBtn = document.getElementById('logoutBtn');
if (logoutBtn) {
  logoutBtn.addEventListener('click', () => {
    localStorage.removeItem('mini_token');
    localStorage.removeItem('mini_user_email');
    window.location.href = 'auth.html';
  });
}

const toasts = document.getElementById("toasts");
const spinner = document.getElementById("spinner");
const backendStatus = document.getElementById("backendStatus");

async function authFetch(url, options = {}) {
  if (!authToken) {
    window.location.href = 'auth.html';
    return;
  }
  const fetchOptions = { ...options };
  const headers = fetchOptions.headers ? { ...fetchOptions.headers } : {};
  headers['X-Auth-Token'] = authToken;
  fetchOptions.headers = headers;
  const res = await fetch(url, fetchOptions);
  if (res.status === 401) {
    localStorage.removeItem('mini_token');
    localStorage.removeItem('mini_user_email');
    window.location.href = 'auth.html';
    throw new Error('Unauthorized');
  }
  return res;
}

// ----- UTILITIES -----
function showSpinner(on=true){
  if(on) spinner.classList.remove("hidden"); else spinner.classList.add("hidden");
}
function toast(message, type='success', timeout=4000){
  const t = document.createElement('div');
  t.className = `toast ${type}`;
  t.innerHTML = `<div style="flex:1">${message}</div><button class="icon-btn" style="border:none;background:transparent;cursor:pointer">✖</button>`;
  t.querySelector('button').onclick = () => t.remove();
  toasts.appendChild(t);
  setTimeout(()=> t.remove(), timeout);
}
function handleError(e, msg='An error occurred'){
  console.error(e);
  toast(msg, 'error', 6000);
}

// ----- THEME (dark mode) -----
const themeToggle = document.getElementById('themeToggle');
function applyTheme(theme){
  document.body.classList.remove('dark','light');
  if(theme === 'dark'){
    document.body.classList.add('dark');
    themeToggle.textContent = '☀';
  }else{
    document.body.classList.add('light');
    themeToggle.textContent = '🌙';
  }
  localStorage.setItem('mini_theme', theme);
}
themeToggle.onclick = () => {
  const now = document.body.classList.contains('dark') ? 'light' : 'dark';
  applyTheme(now);
};
const savedTheme = localStorage.getItem('mini_theme') || 'dark';
applyTheme(savedTheme);

// ----- PAGE SWITCHING -----
document.querySelectorAll('.nav-btn').forEach(btn=>{
  btn.addEventListener('click', ()=>{
    document.querySelectorAll('.nav-btn').forEach(b=>b.classList.remove('active'));
    btn.classList.add('active');
    const page = btn.dataset.page;
    document.querySelectorAll('.page').forEach(p=>p.classList.add('hidden'));
    document.getElementById(page).classList.remove('hidden');
    // call init function
    if(page === 'buckets') loadBuckets();
    if(page === 'files') initFilesPage();
    if(page === 'lambda') loadFunctions();
    if(page === 'queue') loadMessages();
  })
});

// ----- BACKEND STATUS CHECK -----
async function checkBackend(){
  try{
    const res = await fetch(`${API}/actuator/health`, { cache: 'no-store' });
    backendStatus.textContent = res.ok ? 'ok' : 'unavailable';
  }catch(e){
    backendStatus.textContent = 'down';
  }
}
checkBackend();
setInterval(checkBackend, 20000);

// ----- BUCKETS -----
document.getElementById('createBucket').addEventListener('click', async ()=>{
  const name = document.getElementById('bucketName').value.trim();
  if(!name) return toast('Bucket name required', 'error');
  try{
    const res = await authFetch(`${API}/api/buckets?name=${encodeURIComponent(name)}`, { method: 'POST' });
    if(!res.ok) throw new Error(await res.text());
    toast('Bucket created');
    document.getElementById('bucketName').value = '';
    loadBuckets(true);
  }catch(e){
    handleError(e, 'Failed to create bucket');
  }
});

const BUCKET_CACHE_TTL = 10000;
let bucketsCache = null;
let bucketsCacheTimestamp = 0;
let bucketsFetchPromise = null;

async function loadBuckets(force=false){
  if(force){
    bucketsCache = null;
    bucketsCacheTimestamp = 0;
  }
  const now = Date.now();
  if(!force && bucketsCache && (now - bucketsCacheTimestamp) < BUCKET_CACHE_TTL){
    renderBuckets(bucketsCache);
    populateBucketSelects(bucketsCache);
    return bucketsCache;
  }
  if(!force && bucketsFetchPromise){
    const cachedPromise = bucketsFetchPromise;
    const data = await cachedPromise;
    renderBuckets(data);
    populateBucketSelects(data);
    return data;
  }
  const fetchPromise = (async ()=>{
    showSpinner(true);
    try{
      const res = await authFetch(`${API}/api/buckets`);
      if(!res.ok) throw new Error('Failed to load buckets');
      const buckets = await res.json();
      bucketsCache = buckets;
      bucketsCacheTimestamp = Date.now();
      return buckets;
    }finally{
      showSpinner(false);
    }
  })();
  bucketsFetchPromise = fetchPromise;
  try{
    const data = await fetchPromise;
    renderBuckets(data);
    populateBucketSelects(data);
    return data;
  }catch(e){
    handleError(e, 'Unable to load buckets');
    throw e;
  }finally{
    if(bucketsFetchPromise === fetchPromise){
      bucketsFetchPromise = null;
    }
  }
}

function renderBuckets(buckets){
  const el = document.getElementById('bucketsList');
  el.innerHTML = '';
  if(!buckets.length){ el.innerHTML = '<div class="small-muted">No buckets yet</div>'; return; }
  buckets.forEach(b=>{
    const item = document.createElement('div');
    item.className = 'list-item';
    item.innerHTML = `
      <div style="display:flex;flex-direction:column">
        <strong>${escapeHtml(b.name)}</strong>
        <small class="small-muted">id: ${b.id}</small>
      </div>
      <div style="display:flex;gap:8px;align-items:center">
        <button class="btn ghost" onclick="openFilesFromBucket('${encodeURIComponent(b.name)}')">Open</button>
        <button class="btn" onclick="deleteBucket(${b.id})" style="background:#ef4444">Delete</button>
      </div>
    `;
    el.appendChild(item);
  });
}

async function deleteBucket(id){
  if(!confirm('Delete this bucket?')) return;
  showSpinner(true);
  try{
    const res = await authFetch(`${API}/api/buckets/${id}`, { method: 'DELETE' });
    if(!res.ok) throw new Error('Delete failed');
    toast('Bucket deleted');
    loadBuckets(true);
  }catch(e){ handleError(e, 'Failed to delete bucket'); }
  finally{ showSpinner(false); }
}

// open bucket in Files page
function openFilesFromBucket(nameEncoded){
  document.querySelectorAll('.nav-btn').forEach(b=>b.classList.remove('active'));
  document.querySelector('.nav-btn[data-page="files"]').classList.add('active');
  document.querySelectorAll('.page').forEach(p=>p.classList.add('hidden'));
  document.getElementById('files').classList.remove('hidden');
  initFilesPage(decodeURIComponent(nameEncoded));
}

// populate selects
function populateBucketSelects(buckets){
  const sel = document.getElementById('bucketSelect');
  const upSel = document.getElementById('uploadBucket'); // if exists in other markup
  if(sel){
    sel.innerHTML = buckets.map(b=>`<option value="${escapeHtml(b.name)}">${escapeHtml(b.name)}</option>`).join('');
  }
  if(upSel){
    upSel.innerHTML = buckets.map(b=>`<option value="${escapeHtml(b.name)}">${escapeHtml(b.name)}</option>`).join('');
  }
}

// ----- FILES PAGE & UPLOAD -----
const selectedFiles = new Map(); // key: name, value: File

async function initFilesPage(preselectBucket){
  await loadBuckets();
  const sel = document.getElementById('bucketSelect');
  if(!sel) return;
  if(preselectBucket) sel.value = preselectBucket;
  sel.onchange = ()=>loadFiles(sel.value);
  document.getElementById('refreshFiles').onclick = ()=>loadFiles(sel.value);
  wireDropZone();
  document.getElementById('fileInput').onchange = (e) => handleFiles(e.target.files);
  document.getElementById('uploadBtn').onclick = uploadSelectedFiles;
  document.getElementById('clearSelection').onclick = () => { selectedFiles.clear(); renderSelectedFiles(); };
  if(sel.value) loadFiles(sel.value);
}

function wireDropZone(){
  const dropZone = document.getElementById('dropZone');
  if(!dropZone) return;
  dropZone.ondragover = (e)=>{ e.preventDefault(); dropZone.classList.add('drag'); }
  dropZone.ondragleave = ()=> dropZone.classList.remove('drag');
  dropZone.ondrop = (e)=>{
    e.preventDefault();
    dropZone.classList.remove('drag');
    const files = e.dataTransfer.files;
    handleFiles(files);
  };
  // clicking opens file input (input is overlay)
  // input already handled by onchange
}

function handleFiles(fileList){
  for(const f of fileList){
    // unique key by name + size
    const key = `${f.name}_${f.size}_${f.lastModified}`;
    selectedFiles.set(key, f);
  }
  renderSelectedFiles();
}

function renderSelectedFiles(){
  const c = document.getElementById('selectedFiles');
  c.innerHTML = '';
  if(selectedFiles.size === 0){ c.innerHTML = '<div class="small-muted">No files selected</div>'; return; }
  for(const [k, f] of selectedFiles.entries()){
    const row = document.createElement('div');
    row.className = 'file-preview';
    row.innerHTML = `
      ${thumbnailHtml(f)}
      <div style="flex:1">
        <div class="meta">${escapeHtml(f.name)}</div>
        <div class="small-muted">${(f.size/1024).toFixed(2)} KB • ${f.type || 'unknown'}</div>
      </div>
      <div style="display:flex;flex-direction:column;gap:6px">
        <button class="btn ghost" data-key="${k}">Remove</button>
      </div>
    `;
    row.querySelector('button').onclick = (ev)=>{ selectedFiles.delete(ev.target.dataset.key); renderSelectedFiles(); };
    c.appendChild(row);
  }
}

async function uploadSelectedFiles(){
  const sel = document.getElementById('bucketSelect');
  if(!sel || !sel.value) return toast('Select a bucket first', 'error');
  if(selectedFiles.size === 0) return toast('No files selected', 'error');

  showSpinner(true);
  try{
    for(const [k,f] of selectedFiles.entries()){
      const fd = new FormData();
      fd.append('file', f);
      // call upload API
      const res = await authFetch(`${API}/api/s3/buckets/${encodeURIComponent(sel.value)}/upload`, {
        method: 'POST',
        body: fd
      });
      if(!res.ok) {
        const errorText = await res.text();
        throw new Error(`Upload failed for ${f.name}: ${errorText || res.statusText}`);
      }
    }
    toast('All files uploaded');
    selectedFiles.clear();
    renderSelectedFiles();
    loadFiles(sel.value);
  }catch(e){ 
    handleError(e, 'Upload error: ' + e.message); 
  } finally {
    showSpinner(false);
  }
}

function thumbnailHtml(file){
  if(file.type && file.type.startsWith('image/')){
    const url = URL.createObjectURL(file);
    return `<img class="thumb" src="${url}" />`;
  }
  // icons for pdf/audio/video/text
  if(file.type === 'application/pdf') return `<div class="thumb" style="display:flex;align-items:center;justify-content:center;font-weight:600">PDF</div>`;
  if(file.type && file.type.startsWith('audio')) return `<div class="thumb" style="display:flex;align-items:center;justify-content:center">🎵</div>`;
  if(file.type && file.type.startsWith('video')) return `<div class="thumb" style="display:flex;align-items:center;justify-content:center">🎬</div>`;
  return `<div class="thumb" style="display:flex;align-items:center;justify-content:center">📄</div>`;
}

// ----- load files in bucket and show previews -----
async function loadFiles(bucketName){
  if(!bucketName) { document.getElementById('filesList').innerHTML = '<div class="small-muted">Select a bucket</div>'; return; }
  showSpinner(true);
  try{
    const res = await authFetch(`${API}/api/s3/buckets/${encodeURIComponent(bucketName)}/objects`);
    if(!res.ok) throw new Error('Failed to list files');
    const files = await res.json();
    renderFiles(files);
  }catch(e){ handleError(e,'Failed to load files'); }
  finally{ showSpinner(false); }
}

function renderFiles(files){
  const grid = document.getElementById('filesList');
  grid.innerHTML = '';
  if(!files || files.length === 0){ grid.innerHTML = '<div class="small-muted">No files in this bucket</div>'; return; }
  files.forEach(f=>{
    const card = document.createElement('div');
    card.className = 'list-item';
    card.onclick = ()=> openFile(f.objectKey);
    const preview = filePreviewRemote(f);
    card.innerHTML = `
      <div style="display:flex;gap:12px;align-items:center">
        ${preview}
        <div>
          <strong>${escapeHtml(f.fileName)}</strong>
          <div class="small-muted">${(f.size/1024).toFixed(2)} KB</div>
        </div>
      </div>
      <div style="display:flex;flex-direction:column;gap:6px;align-items:flex-end">
        <button class="btn ghost" onclick="downloadFile(event, '${encodeURIComponent(f.objectKey)}')">Open</button>
        <button class="btn" style="background:#ef4444" onclick="deleteFile(event,'${encodeURIComponent(f.objectKey)}')">Delete</button>
      </div>
    `;
    grid.appendChild(card);
  });
}

function filePreviewRemote(f){
  // If object is image and browser can preview remote, show thumbnail via download endpoint (may require CORS)
  const ext = (f.fileName || '').split('.').pop().toLowerCase();
  if(['png','jpg','jpeg','gif','webp','svg'].includes(ext)){
    const url = `${API}/api/s3/objects/${encodeURIComponent(f.objectKey)}/download?token=${encodeURIComponent(authToken)}`;
    return `<img class="thumb" src="${url}" onerror="this.style.display='none'" />`;
  }
  if(ext === 'pdf') return `<div class="thumb" style="display:flex;align-items:center;justify-content:center">PDF</div>`;
  if(['mp3','wav','ogg'].includes(ext)) return `<div class="thumb">🎵</div>`;
  return `<div class="thumb">📄</div>`;
}

function openFile(objectKey){
  window.open(`${API}/api/s3/objects/${encodeURIComponent(objectKey)}/download?token=${encodeURIComponent(authToken)}`, '_blank');
}
async function downloadFile(ev, objectKey){
  ev.stopPropagation();
  openFile(decodeURIComponent(objectKey));
}
async function deleteFile(ev, objectKey){
  ev.stopPropagation();
  if(!confirm('Delete this file?')) return;
  showSpinner(true);
  try{
    const res = await authFetch(`${API}/api/s3/objects/${objectKey}`, { method: 'DELETE' });
    if(!res.ok) throw new Error('Delete failed');
    toast('File deleted');
    // refresh
    const sel = document.getElementById('bucketSelect');
    if(sel && sel.value) loadFiles(sel.value);
  }catch(e){ handleError(e,'Failed to delete file'); }
  finally{ showSpinner(false); }
}

// ----- LAMBDA (basic wiring) -----
document.getElementById('createFn').addEventListener('click', async ()=>{
  const name = document.getElementById('fnName').value.trim(); const runtime = document.getElementById('fnRuntime').value;
  const code = document.getElementById('fnCode').value;
  if(!name||!code) return toast('Function name and code required','error');
  showSpinner(true);
  try{
    const res = await authFetch(`${API}/api/lambda/create`, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ functionName:name, runtime, code}) });
    if(!res.ok) throw new Error(await res.text());
    toast('Function created');
    loadFunctions();
  }catch(e){ handleError(e,'Failed to create function'); }
  finally{ showSpinner(false); }
});

async function loadFunctions(){
  showSpinner(true);
  try{
    const res = await authFetch(`${API}/api/lambda/functions`);
    if(!res.ok) {
      console.error('Failed to load functions:', res.status, res.statusText);
      document.getElementById('fnList').innerHTML = '<div class="small-muted">No functions found</div>';
      return;
    }
    const list = await res.json();
    const el = document.getElementById('fnList'); 
    el.innerHTML = '';
    if(!list || list.length === 0) {
      el.innerHTML = '<div class="small-muted">No functions yet</div>';
      return;
    }
    list.forEach(fn=>{
      const div = document.createElement('div'); 
      div.className='list-item';
      div.innerHTML = `<strong>${escapeHtml(fn.functionName)}</strong><small class="small-muted">${fn.runtime}</small>`;
      el.appendChild(div);
    });
  }catch(e){ 
    console.error('Error loading functions:', e);
    document.getElementById('fnList').innerHTML = '<div class="small-muted">Failed to load functions</div>';
  } finally {
    showSpinner(false);
  }
}

// ----- QUEUE (basic) -----
document.getElementById('sendMsg').addEventListener('click', async ()=>{
  const queue = document.getElementById('qName').value || 'default'; const payload = document.getElementById('qPayload').value || '';
  try{
    const res = await authFetch(`${API}/api/queue/send?queue=${encodeURIComponent(queue)}`, { method:'POST', headers:{'Content-Type':'text/plain'}, body: payload });
    if(!res.ok) throw new Error('Send failed');
    toast('Message sent');
    loadMessages();
  }catch(e){ handleError(e,'Failed to send message'); }
});

async function loadMessages(){
  try{
    const queue = document.getElementById('qName').value || 'default';
    const res = await authFetch(`${API}/api/queue/messages?queue=${encodeURIComponent(queue)}`);
    if(!res.ok) { document.getElementById('msgList').innerHTML = '<div class="small-muted">No messages or endpoint missing</div>'; return; }
    const msgs = await res.json(); const el = document.getElementById('msgList'); el.innerHTML = '';
    msgs.forEach(m=>{
      const d = document.createElement('div'); d.className='list-item'; d.innerHTML = `<div><strong>${escapeHtml(m.messageBody)}</strong><div class="small-muted">status: ${m.status}</div></div>`; el.appendChild(d);
    });
  }catch(e){ document.getElementById('msgList').innerHTML = '<div class="small-muted">Failed to load messages</div>'; }
}

// ----- small helpers -----
function escapeHtml(s=''){ return String(s).replace(/[&<>"']/g, c=> ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
window.escapeHtml = escapeHtml;

// Initialize first view
(async function init(){
  await loadBuckets();
})();