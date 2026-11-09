/* Kharcha - frontend. Plain JavaScript, talks to the Spring Boot API at /api */

const CATEGORIES = ['Groceries', 'Shopping', 'Food', 'Travel', 'Bills', 'Other'];
const PAYMENTS = ['PhonePe', 'Google Pay', 'UPI', 'Card', 'Cash'];
const COLORS = ['#0E6B52', '#C98A0B', '#B23B2B', '#3B6EA5', '#7A5C99', '#4F9D8B', '#D1743A', '#8A8F3A'];

const $ = (s) => document.querySelector(s);
const $$ = (s) => [...document.querySelectorAll(s)];
const inr = (n) => '₹' + Number(n || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 });
const esc = (s) => String(s ?? '').replace(/[&<>"']/g,
  (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

let currentPage = 'today';
let loaded = {};          // expenses on screen, by id (used by Edit)
let pieChart = null;
let dayChart = null;

/* ---------- helpers ---------- */

async function api(path, options = {}) {
  const isForm = options.body instanceof FormData;
  const res = await fetch('/api' + path, {
    headers: isForm ? {} : { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 204) return null;
  const type = res.headers.get('content-type') || '';
  const data = type.includes('json') ? await res.json() : await res.text();
  if (!res.ok) throw new Error((data && data.error) || 'Something went wrong. Please try again.');
  return data;
}

let toastTimer;
function toast(message, isError = false) {
  const el = $('#toast');
  el.textContent = message;
  el.className = 'show' + (isError ? ' error' : '');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (el.className = ''), 3500);
}

/** Wraps a click handler so errors show as a toast. */
function safe(fn) {
  return async (...args) => {
    try { await fn(...args); } catch (e) { toast(e.message, true); }
  };
}

const pad = (n) => String(n).padStart(2, '0');
const todayStr = () => { const d = new Date(); return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`; };
const monthStr = () => todayStr().slice(0, 7);
const toLocalInput = (d) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;

function fmtWhen(iso) {
  const d = new Date(iso);
  const date = d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
  if (iso.includes('T00:00')) return date;          // imported rows have no time
  return date + ', ' + d.toLocaleTimeString('en-IN', { hour: 'numeric', minute: '2-digit' });
}

function fillSelect(el, items, firstLabel) {
  el.innerHTML = (firstLabel !== undefined ? `<option value="">${firstLabel}</option>` : '') +
    items.map((i) => `<option>${esc(i)}</option>`).join('');
}

function barList(el, map, empty = 'Nothing yet this month.') {
  const entries = Object.entries(map || {});
  if (!entries.length) { el.innerHTML = `<p class="empty">${empty}</p>`; return; }
  const max = Math.max(...entries.map((e) => Number(e[1])));
  el.innerHTML = entries.map(([name, value]) => `
    <div>
      <div class="bar-head"><span>${esc(name)}</span><b>${inr(value)}</b></div>
      <div class="bar"><i style="width:${max ? (Number(value) / max) * 100 : 0}%"></i></div>
    </div>`).join('');
}

function expenseTable(list, emptyText) {
  if (!list.length) return `<p class="empty">${emptyText}</p>`;
  loaded = {};
  list.forEach((e) => (loaded[e.id] = e));
  return `<table>
    <thead><tr><th>When</th><th>App</th><th>Category</th><th>Paid through</th><th class="num">Amount</th><th></th></tr></thead>
    <tbody>${list.map((e) => `
      <tr>
        <td>${fmtWhen(e.spentAt)}</td>
        <td title="${esc(e.note)}">${esc(e.merchant)}</td>
        <td>${esc(e.category)}</td>
        <td>${esc(e.paymentMethod)}</td>
        <td class="num">${inr(e.amount)}</td>
        <td class="act"><button data-edit="${e.id}">Edit</button><button data-del="${e.id}">Delete</button></td>
      </tr>`).join('')}
    </tbody></table>`;
}

/* ---------- navigation ---------- */

const loaders = { today: loadToday, expenses: loadExpenses, analysis: loadAnalysis, settings: loadSettings };

function showPage(name) {
  currentPage = name;
  $$('.page').forEach((p) => (p.hidden = p.id !== 'page-' + name));
  $$('#tabs button').forEach((b) => b.classList.toggle('active', b.dataset.page === name));
  safe(loaders[name])();
}
const refresh = () => safe(loaders[currentPage])();

/* ---------- Today ---------- */

async function loadToday() {
  const d = await api('/summary/dashboard');
  $('#todayAmt').textContent = inr(d.todayTotal);
  $('#balanceAmt').textContent = inr(d.balance);
  $('#monthAmt').textContent = inr(d.monthTotal);

  const budget = Number(d.budget);
  const pct = Number(d.budgetUsedPercent);
  const bar = $('#budgetBar');
  if (budget > 0) {
    $('#budgetBox').hidden = false;
    $('#budgetText').textContent = `${inr(d.monthTotal)} of ${inr(budget)} budget used`;
    $('#budgetPct').textContent = pct + '%';
    bar.style.width = Math.min(pct, 100) + '%';
    bar.className = pct >= 100 ? 'over' : pct >= 80 ? 'warn' : '';
  } else {
    $('#budgetBox').hidden = true;
  }

  barList($('#byMerchant'), d.byMerchant, 'No spending yet this month. Add your first expense.');
  barList($('#byPayment'), d.byPayment);

  const today = todayStr();
  const list = await api(`/expenses?from=${today}&to=${today}`);
  $('#todayList').innerHTML = expenseTable(list, 'No expenses today. Use "Add expense" when you spend.');
}

/* ---------- Expenses ---------- */

async function loadExpenses() {
  const p = new URLSearchParams();
  const add = (key, val) => { if (val) p.set(key, val); };
  add('q', $('#fQ').value.trim());
  add('merchant', $('#fMerchant').value.trim());
  add('category', $('#fCategory').value);
  add('paymentMethod', $('#fPayment').value);
  add('from', $('#fFrom').value);
  add('to', $('#fTo').value);

  const list = await api('/expenses?' + p.toString());
  const total = list.reduce((sum, e) => sum + Number(e.amount), 0);
  $('#fTotal').textContent = list.length ? `${list.length} expenses, total ${inr(total)}` : '';
  $('#expenseList').innerHTML = expenseTable(list, 'No expenses match these filters.');
}

/* ---------- Analysis ---------- */

async function loadAnalysis() {
  const month = $('#aMonth').value || monthStr();
  const d = await api('/summary/monthly?month=' + month);

  let change = '<small>No data for the previous month</small>';
  if (d.changePercent !== null && d.changePercent !== undefined) {
    const up = Number(d.changePercent) > 0;
    change = `<small class="${up ? 'up' : 'down'}">${up ? 'Up' : 'Down'} ${Math.abs(d.changePercent)}% from last month (${inr(d.previousMonthTotal)})</small>`;
  }
  const biggest = d.biggest
    ? `<div class="stat"><span>Biggest expense</span><b>${inr(d.biggest.amount)}</b><small> ${esc(d.biggest.merchant)}</small></div>` : '';
  $('#aStats').innerHTML = `
    <div class="stat"><span>Total spent</span><b>${inr(d.total)}</b><br>${change}</div>
    <div class="stat"><span>Number of expenses</span><b>${d.count}</b></div>
    <div class="stat"><span>Average per day</span><b>${inr(d.dailyAverage)}</b></div>
    ${biggest}`;

  barList($('#aMerchant'), d.byMerchant, 'No spending in this month.');
  barList($('#aCategory'), d.byCategory, 'No spending in this month.');
  barList($('#aPayment'), d.byPayment, 'No spending in this month.');

  if (typeof Chart === 'undefined') return;   // CDN not reachable (offline)
  if (pieChart) pieChart.destroy();
  if (dayChart) dayChart.destroy();

  const names = Object.keys(d.byMerchant);
  pieChart = new Chart($('#pieChart'), {
    type: 'doughnut',
    data: { labels: names, datasets: [{ data: names.map((n) => d.byMerchant[n]), backgroundColor: COLORS }] },
    options: { maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } },
  });

  const days = Object.keys(d.byDay);
  dayChart = new Chart($('#dayChart'), {
    type: 'bar',
    data: { labels: days, datasets: [{ label: 'Spent (₹)', data: days.map((k) => d.byDay[k]), backgroundColor: '#0E6B52' }] },
    options: { maintainAspectRatio: false, plugins: { legend: { display: false } },
               scales: { y: { beginAtZero: true } } },
  });
}

/* ---------- Settings ---------- */

async function loadSettings() {
  const s = await api('/settings');
  $('#sBalance').value = s.startingBalance;
  $('#sBudget').value = s.monthlyBudget;
  $('#iDate').value = todayStr();

  const income = await api('/income');
  $('#incomeList').innerHTML = income.length
    ? income.map((i) => `<div class="item"><span>${esc(i.source)}, ${i.receivedOn}</span>
        <span><b>${inr(i.amount)}</b> <button data-del-income="${i.id}">Delete</button></span></div>`).join('')
    : '<p class="empty">No income added yet.</p>';

  const rules = await api('/rules');
  $('#ruleList').innerHTML = rules.map((r) => `<div class="item">
      <span>"${esc(r.keyword)}" becomes <b>${esc(r.merchant)}</b> in ${esc(r.category)}</span>
      <button data-del-rule="${r.id}">Delete</button></div>`).join('');
}

/* ---------- Add / edit dialog ---------- */

function openDialog(expense) {
  $('#dialogTitle').textContent = expense ? 'Edit expense' : 'Add expense';
  $('#eId').value = expense ? expense.id : '';
  $('#eAmount').value = expense ? expense.amount : '';
  $('#eMerchant').value = expense ? expense.merchant : '';
  $('#eCategory').value = expense ? expense.category : CATEGORIES[0];
  $('#ePayment').value = expense ? expense.paymentMethod : PAYMENTS[0];
  $('#eWhen').value = expense ? expense.spentAt.slice(0, 16) : toLocalInput(new Date());
  $('#eNote').value = expense && expense.note ? expense.note : '';
  $('#expenseDialog').showModal();
  $('#eAmount').focus();
}

$('#expenseForm').addEventListener('submit', safe(async (ev) => {
  ev.preventDefault();
  const id = $('#eId').value;
  const body = {
    amount: Number($('#eAmount').value),
    merchant: $('#eMerchant').value.trim(),
    category: $('#eCategory').value,
    paymentMethod: $('#ePayment').value,
    spentAt: $('#eWhen').value,
    note: $('#eNote').value.trim(),
  };
  await api(id ? '/expenses/' + id : '/expenses', { method: id ? 'PUT' : 'POST', body: JSON.stringify(body) });
  $('#expenseDialog').close();
  toast(id ? 'Expense updated' : 'Expense saved');
  refresh();
}));

/* ---------- Events ---------- */

$$('#tabs button').forEach((b) => b.addEventListener('click', () => showPage(b.dataset.page)));
$('#addBtn').addEventListener('click', () => openDialog(null));
$('#eCancel').addEventListener('click', () => $('#expenseDialog').close());

// Edit / delete buttons inside tables and lists
document.addEventListener('click', safe(async (ev) => {
  const t = ev.target;
  if (t.dataset.edit) {
    openDialog(loaded[t.dataset.edit]);
  } else if (t.dataset.del) {
    if (!confirm('Delete this expense?')) return;
    await api('/expenses/' + t.dataset.del, { method: 'DELETE' });
    toast('Expense deleted');
    refresh();
  } else if (t.dataset.delIncome) {
    if (!confirm('Delete this income entry?')) return;
    await api('/income/' + t.dataset.delIncome, { method: 'DELETE' });
    refresh();
  } else if (t.dataset.delRule) {
    await api('/rules/' + t.dataset.delRule, { method: 'DELETE' });
    refresh();
  }
}));

$('#fApply').addEventListener('click', safe(loadExpenses));
$('#fClear').addEventListener('click', () => {
  ['#fQ', '#fMerchant', '#fFrom', '#fTo'].forEach((s) => ($(s).value = ''));
  $('#fCategory').value = '';
  $('#fPayment').value = '';
  safe(loadExpenses)();
});

$('#smsBtn').addEventListener('click', safe(async () => {
  const text = $('#smsText').value.trim();
  const e = await api('/expenses/parse-sms', { method: 'POST', body: JSON.stringify({ text }) });
  $('#smsText').value = '';
  toast(`Saved ${inr(e.amount)} at ${e.merchant}`);
  refresh();
}));

$('#csvBtn').addEventListener('click', safe(async () => {
  const file = $('#csvFile').files[0];
  if (!file) throw new Error('Choose a CSV file first.');
  const form = new FormData();
  form.append('file', file);
  const r = await api('/expenses/import', { method: 'POST', body: form });
  $('#csvFile').value = '';
  toast(`Imported ${r.imported}. Skipped ${r.duplicatesSkipped} duplicates and ${r.rowsSkipped} other rows.`);
  refresh();
}));

$('#exportBtn').addEventListener('click', () => {
  window.location.href = '/api/expenses/export?month=' + ($('#exportMonth').value || monthStr());
});

$('#aMonth').addEventListener('change', safe(loadAnalysis));

$('#sSave').addEventListener('click', safe(async () => {
  await api('/settings', { method: 'PUT', body: JSON.stringify({
    startingBalance: Number($('#sBalance').value),
    monthlyBudget: Number($('#sBudget').value || 0),
  }) });
  toast('Settings saved');
}));

$('#iSave').addEventListener('click', safe(async () => {
  await api('/income', { method: 'POST', body: JSON.stringify({
    amount: Number($('#iAmount').value),
    source: $('#iSource').value.trim(),
    receivedOn: $('#iDate').value || null,
  }) });
  $('#iAmount').value = '';
  $('#iSource').value = '';
  toast('Income added');
  loadSettings();
}));

$('#rSave').addEventListener('click', safe(async () => {
  await api('/rules', { method: 'POST', body: JSON.stringify({
    keyword: $('#rKeyword').value.trim(),
    merchant: $('#rMerchant').value.trim(),
    category: $('#rCategory').value,
  }) });
  $('#rKeyword').value = '';
  $('#rMerchant').value = '';
  toast('Rule added');
  loadSettings();
}));

$('#testBtn').addEventListener('click', safe(async () => {
  const r = await api('/notifications/test', { method: 'POST' });
  if (r.sent.length) toast('Sent through ' + r.sent.join(' and '));
  else if (r.failed.length) throw new Error(r.failed.join('; '));
  else throw new Error('No channel is set up yet. Add Telegram or email details in application.properties.');
}));

/* ---------- Start ---------- */

fillSelect($('#eCategory'), CATEGORIES);
fillSelect($('#ePayment'), PAYMENTS);
fillSelect($('#rCategory'), CATEGORIES);
fillSelect($('#fCategory'), CATEGORIES, 'Any');
fillSelect($('#fPayment'), PAYMENTS, 'Any');
$('#aMonth').value = monthStr();
$('#exportMonth').value = monthStr();
showPage('today');
