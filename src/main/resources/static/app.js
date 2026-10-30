const API = '/api/expenses';
const categories = { FOOD: '食費', TRANSPORT: '交通費', EDUCATION: '書籍・学習', DAILY: '日用品', ENTERTAINMENT: '娯楽', OTHER: 'その他' };
const payments = { CASH: '現金', CREDIT_CARD: 'クレジットカード', E_MONEY: '電子マネー', BANK_TRANSFER: '銀行振込' };
const yen = new Intl.NumberFormat('ja-JP', { style: 'currency', currency: 'JPY' });
const form = document.querySelector('#expense-form');
const dialog = document.querySelector('#expense-dialog');
const deleteDialog = document.querySelector('#delete-dialog');
let expenses = [];
let selected = null;
let mode = 'create';
let busy = false;
let detailLoading = false;
let toastTimer;

async function request(path = '', options = {}) {
  const response = await fetch(API + path, options);
  if (!response.ok) {
    throw new Error(await response.text() || '処理に失敗しました');
  }
  return response.status === 204 ? null : response.json();
}

async function loadExpenses() {
  document.querySelector('#reload-button').disabled = true;
  document.querySelector('#load-error').hidden = true;
  try {
    expenses = await request();
    renderExpenses();
  } catch (error) {
    document.querySelector('#load-error span').textContent = '支出を読み込めませんでした。';
    document.querySelector('#load-error').hidden = false;
    document.querySelector('#list-status').hidden = false;
    document.querySelector('#list-status').textContent = '一覧を取得できませんでした。再読み込みをお試しください。';
    document.querySelector('#list-count').textContent = '最新のデータを取得できていません';
  }
  document.querySelector('#reload-button').disabled = false;
}

function renderExpenses() {
  const rows = document.querySelector('#rows');
  rows.replaceChildren();
  for (const expense of expenses) {
    const row = document.createElement('tr');
    const values = [expense.expenseDate.replaceAll('-', '/'), expense.title, '', payments[expense.paymentMethod] || expense.paymentMethod || '—', yen.format(expense.amount)];
    values.forEach((value, index) => {
      const cell = document.createElement('td');
      cell.textContent = value;
      if (index === 2) {
        const tag = document.createElement('span');
        tag.className = 'category';
        tag.dataset.category = expense.category;
        tag.textContent = categories[expense.category] || expense.category || '未分類';
        cell.append(tag);
      }
      if (index === 4) cell.className = 'amount';
      row.append(cell);
    });
    const action = document.createElement('td');
    const button = document.createElement('button');
    button.className = 'secondary detail-button';
    button.textContent = '詳細';
    button.setAttribute('aria-label', `${expense.title}の詳細`);
    button.disabled = detailLoading || busy;
    button.addEventListener('click', () => openDetail(expense.id));
    action.append(button);
    row.append(action);
    rows.append(row);
  }
  document.querySelector('#list-status').hidden = expenses.length > 0;
  document.querySelector('#list-status').textContent = expenses.length ? '' : 'まだ支出がありません。「支出を登録」から最初の記録を追加しましょう。';
  document.querySelector('#list-count').textContent = `${expenses.length}件の支出`;
}

function updateControls() {
  document.querySelector('#new-button').disabled = busy || detailLoading;
  for (const button of document.querySelectorAll('.detail-button')) button.disabled = busy || detailLoading;
  for (const button of document.querySelectorAll('dialog button')) button.disabled = busy;
}

function setMode(value) {
  mode = value;
  document.querySelector('#dialog-title').textContent = mode === 'create' ? '支出を登録' : '支出を編集';
  document.querySelector('#save-button').textContent = mode === 'create' ? '登録する' : '更新する';
  document.querySelector('#delete-button').hidden = mode === 'create';
  document.querySelector('#form-error').hidden = true;
}

document.querySelector('#new-button').addEventListener('click', () => {
  if (busy || detailLoading) return;
  selected = null;
  form.reset();
  setMode('create');
  const now = new Date();
  form.elements.expenseDate.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  dialog.showModal();
  form.elements.title.focus();
});

async function openDetail(id) {
  if (busy || detailLoading || dialog.open) return;
  detailLoading = true;
  updateControls();
  try {
    selected = await request(`/${id}`);
    for (const name of ['title', 'amount', 'category', 'paymentMethod', 'expenseDate', 'memo']) form.elements[name].value = selected[name] ?? '';
    setMode('edit');
    dialog.showModal();
  } catch (error) { toast('詳細を取得できませんでした。'); }
  finally { detailLoading = false; updateControls(); }
}

for (const button of document.querySelectorAll('.close-button')) button.addEventListener('click', () => { if (!busy) dialog.close(); });
for (const modal of [dialog, deleteDialog]) modal.addEventListener('cancel', event => { if (busy) event.preventDefault(); });

form.addEventListener('submit', async event => {
  event.preventDefault();
  if (busy) return;
  const errorBox = document.querySelector('#form-error');
  errorBox.hidden = true;
  if (!form.elements.title.value.trim()) {
    errorBox.textContent = 'タイトルを入力してください';
    errorBox.hidden = false;
    form.elements.title.focus();
    return;
  }
  busy = true;
  updateControls();
  try {
    if (mode === 'create') {
      await request('', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: new URLSearchParams(new FormData(form)).toString()
      });
    } else {
      const data = Object.fromEntries(new FormData(form));
      data.amount = Number(data.amount);
      data.paymentMethod = data.paymentMethod || null;
      data.memo = data.memo || null;
      await request(`/${selected.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
    }
    dialog.close();
    toast(mode === 'create' ? '支出を登録しました。' : '支出を更新しました。');
    await loadExpenses();
  } catch (error) {
    errorBox.textContent = '保存できませんでした。入力内容を確認してください。';
    errorBox.hidden = false;
  } finally { busy = false; updateControls(); }
});

document.querySelector('#delete-button').addEventListener('click', () => {
  document.querySelector('#delete-description').textContent = `「${selected.title}」 ${yen.format(selected.amount)}`;
  document.querySelector('#delete-error').hidden = true;
  deleteDialog.showModal();
  document.querySelector('#cancel-delete').focus();
});
document.querySelector('#cancel-delete').addEventListener('click', () => { if (!busy) deleteDialog.close(); });
document.querySelector('#confirm-delete').addEventListener('click', async () => {
  if (busy) return;
  busy = true;
  updateControls();
  try {
    await request(`/${selected.id}`, { method: 'DELETE' });
    deleteDialog.close();
    dialog.close();
    toast('支出を削除しました。');
    await loadExpenses();
  } catch (error) {
    document.querySelector('#delete-error').textContent = '削除できませんでした。';
    document.querySelector('#delete-error').hidden = false;
  } finally { busy = false; updateControls(); }
});

function toast(message) {
  clearTimeout(toastTimer);
  const element = document.querySelector('#toast');
  element.textContent = message;
  element.hidden = false;
  toastTimer = setTimeout(() => { element.hidden = true; }, 5000);
}
document.querySelector('#reload-button').addEventListener('click', loadExpenses);
loadExpenses();
