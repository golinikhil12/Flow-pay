/* ----------------------------------------------------
   FLOWPAY CLIENT-SIDE JAVASCRIPT CONTROLLER
   Handles API requests, modals, toast banners, and simulators
   ---------------------------------------------------- */

document.addEventListener('DOMContentLoaded', () => {
    // Attach form listeners if present
    const addMoneyForm = document.getElementById('add-money-form');
    if (addMoneyForm) {
        addMoneyForm.addEventListener('submit', handleAddMoney);
    }

    const withdrawMoneyForm = document.getElementById('withdraw-money-form');
    if (withdrawMoneyForm) {
        withdrawMoneyForm.addEventListener('submit', handleWithdrawMoney);
    }

    const profileForm = document.getElementById('profile-form');
    if (profileForm) {
        profileForm.addEventListener('submit', handleProfileUpdate);
    }

    const changePinForm = document.getElementById('change-pin-form');
    if (changePinForm) {
        changePinForm.addEventListener('submit', handleChangePin);
    }

    // Attach PIN confirm button listener
    const pinConfirmBtn = document.getElementById('pin-confirm-btn');
    if (pinConfirmBtn) {
        pinConfirmBtn.addEventListener('click', submitPaymentWithPin);
    }
});

// Toast Banner Notifications
function showToast(title, message, type = 'info') {
    const banner = document.getElementById('toast-banner');
    const titleEl = document.getElementById('toast-title');
    const msgEl = document.getElementById('toast-msg');
    const iconEl = document.getElementById('toast-icon');

    if (!banner || !titleEl || !msgEl) return;

    titleEl.textContent = title;
    msgEl.textContent = message;

    if (type === 'success') {
        iconEl.className = 'fa-solid fa-circle-check text-success';
    } else if (type === 'warning') {
        iconEl.className = 'fa-solid fa-triangle-exclamation text-warning';
    } else if (type === 'error') {
        iconEl.className = 'fa-solid fa-circle-xmark text-danger';
    } else {
        iconEl.className = 'fa-solid fa-circle-info text-info';
    }

    banner.classList.remove('hidden');

    setTimeout(() => {
        closeToast();
    }, 4500);
}

function closeToast() {
    const banner = document.getElementById('toast-banner');
    if (banner) banner.classList.add('hidden');
}

// Toggle Online / Offline Simulator
async function toggleNetwork() {
    try {
        const res = await fetch('/api/toggle-network', { method: 'POST' });
        const data = await res.json();
        if (data.success) {
            showToast('Network Status Updated', data.message, data.is_online ? 'success' : 'warning');
            setTimeout(() => location.reload(), 800);
        }
    } catch (err) {
        console.error('Toggle Network Error:', err);
    }
}

// Toggle Battery Simulator
async function toggleBattery() {
    try {
        const res = await fetch('/api/toggle-battery', { method: 'POST' });
        const data = await res.json();
        if (data.success) {
            document.body.classList.toggle('low-battery-mode', data.is_low_battery);
            showToast('Power Mode Changed', data.message, data.is_low_battery ? 'warning' : 'info');
            setTimeout(() => location.reload(), 800);
        }
    } catch (err) {
        console.error('Toggle Battery Error:', err);
    }
}

// Quick amount chips for Wallet
function setAddAmount(val) {
    const input = document.getElementById('add-amount');
    if (input) input.value = val;
}

// Handle Add Money to Wallet
async function handleAddMoney(e) {
    e.preventDefault();
    const amount = document.getElementById('add-amount').value;
    
    try {
        const res = await fetch('/api/wallet/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount: parseFloat(amount) })
        });
        const data = await res.json();

        if (data.success) {
            showToast('Money Added', data.message, 'success');
            setTimeout(() => window.location.href = '/', 1200);
        } else {
            showToast('Failed to Add Money', data.message, 'error');
        }
    } catch (err) {
        showToast('Error', 'Unable to connect to server.', 'error');
    }
}

// Handle Withdraw Money
async function handleWithdrawMoney(e) {
    e.preventDefault();
    const amount = document.getElementById('withdraw-amount').value;

    try {
        const res = await fetch('/api/wallet/withdraw', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount: parseFloat(amount) })
        });
        const data = await res.json();

        if (data.success) {
            showToast('Withdrawal Success', data.message, 'success');
            setTimeout(() => window.location.href = '/', 1200);
        } else {
            showToast('Withdrawal Failed', data.message, 'error');
        }
    } catch (err) {
        showToast('Error', 'Unable to connect to server.', 'error');
    }
}

// Fill Recipient Quick Contacts
function fillRecipient(name, upi) {
    const nameInput = document.getElementById('receiver-name');
    const upiInput = document.getElementById('receiver-upi');
    if (nameInput && upiInput) {
        nameInput.value = name;
        upiInput.value = upi;
        showToast('Contact Selected', `Recipient set to ${name}`, 'info');
    }
}

// Open PIN confirmation modal for Payment
function initiatePaymentModal() {
    const name = document.getElementById('receiver-name').value.trim();
    const upi = document.getElementById('receiver-upi').value.trim();
    const amount = document.getElementById('pay-amount').value;

    if (!name || !upi || !amount || amount <= 0) {
        showToast('Incomplete Form', 'Please enter recipient details and amount.', 'warning');
        return;
    }

    const pinModal = document.getElementById('pin-modal');
    const pinInput = document.getElementById('pin-input');
    const pinErr = document.getElementById('pin-error-msg');

    if (pinModal && pinInput) {
        pinInput.value = '';
        if (pinErr) pinErr.classList.add('hidden');
        pinModal.classList.remove('hidden');
        pinInput.focus();
    }
}

function closePinModal() {
    const pinModal = document.getElementById('pin-modal');
    if (pinModal) pinModal.classList.add('hidden');
}

// Submit payment with PIN check
async function submitPaymentWithPin() {
    const pin = document.getElementById('pin-input').value;
    const name = document.getElementById('receiver-name').value;
    const upi = document.getElementById('receiver-upi').value;
    const amount = parseFloat(document.getElementById('pay-amount').value);
    const pinErr = document.getElementById('pin-error-msg');

    if (!pin || pin.length !== 4) {
        if (pinErr) {
            pinErr.textContent = 'Please enter a 4-digit PIN.';
            pinErr.classList.remove('hidden');
        }
        return;
    }

    // Verify PIN first via login API or send payment directly
    try {
        const payRes = await fetch('/api/pay', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                amount: amount,
                receiver_name: name,
                receiver_upi: upi
            })
        });
        const data = await payRes.json();

        if (data.success) {
            closePinModal();
            const alertType = data.is_offline ? 'warning' : 'success';
            showToast('Payment Successful!', data.message, alertType);
            setTimeout(() => window.location.href = '/', 1500);
        } else {
            if (pinErr) {
                pinErr.textContent = data.message;
                pinErr.classList.remove('hidden');
            }
        }
    } catch (err) {
        if (pinErr) {
            pinErr.textContent = 'Server connection error.';
            pinErr.classList.remove('hidden');
        }
    }
}

// Start Sync Process Animation
async function startSyncProcess() {
    const btn = document.getElementById('start-sync-btn');
    const progressBox = document.getElementById('sync-progress-box');
    const fill = document.getElementById('sync-progress-fill');
    const statusText = document.getElementById('sync-status-text');

    if (btn) btn.disabled = true;
    if (progressBox) progressBox.classList.remove('hidden');

    const steps = [
        { pct: 25, msg: 'Verifying offline security signatures...' },
        { pct: 60, msg: 'Uploading pending payment batches to server...' },
        { pct: 90, msg: 'Updating ledger & restoring offline spend limit...' },
        { pct: 100, msg: 'Synchronization Complete!' }
    ];

    for (let i = 0; i < steps.length; i++) {
        await new Promise(r => setTimeout(r, 600));
        if (fill) fill.style.width = steps[i].pct + '%';
        if (statusText) statusText.textContent = steps[i].msg;
    }

    try {
        const res = await fetch('/api/sync', { method: 'POST' });
        const data = await res.json();
        if (data.success) {
            showToast('Sync Completed', data.message, 'success');
            setTimeout(() => window.location.href = '/', 1200);
        }
    } catch (err) {
        showToast('Sync Error', 'Failed to synchronize with server.', 'error');
    }
}

// Handle Profile Update
async function handleProfileUpdate(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('prof-name').value,
        phone: document.getElementById('prof-phone').value,
        upi_id: document.getElementById('prof-upi').value,
        bank_name: document.getElementById('prof-bank').value,
        account_number: document.getElementById('prof-acc').value,
        ifsc: document.getElementById('prof-ifsc').value
    };

    try {
        const res = await fetch('/api/profile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showToast('Profile Saved', data.message, 'success');
        } else {
            showToast('Error', data.message, 'error');
        }
    } catch (err) {
        showToast('Error', 'Unable to update profile.', 'error');
    }
}

// Handle Change PIN
async function handleChangePin(e) {
    e.preventDefault();
    const oldPin = document.getElementById('old-pin').value;
    const newPin = document.getElementById('new-pin').value;

    try {
        const res = await fetch('/api/settings/pin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ old_pin: oldPin, new_pin: newPin })
        });
        const data = await res.json();
        if (data.success) {
            showToast('PIN Updated', data.message, 'success');
            document.getElementById('change-pin-form').reset();
        } else {
            showToast('PIN Error', data.message, 'error');
        }
    } catch (err) {
        showToast('Error', 'Unable to change PIN.', 'error');
    }
}

// Reset System Demo Data
async function resetSystemData() {
    if (!confirm('Are you sure you want to reset all demo data to default values?')) return;

    try {
        const res = await fetch('/api/reset', { method: 'POST' });
        const data = await res.json();
        if (data.success) {
            showToast('System Reset', data.message, 'info');
            setTimeout(() => window.location.href = '/', 1200);
        }
    } catch (err) {
        showToast('Error', 'Reset failed.', 'error');
    }
}

// Filter Transactions in History view
function filterTransactions(type) {
    const buttons = document.querySelectorAll('.filter-btn');
    buttons.forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');

    const items = document.querySelectorAll('.history-item');
    items.forEach(item => {
        const status = item.getAttribute('data-status');
        if (type === 'all' || status === type) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

// Activity Log Terminal Modal
async function openLogsModal() {
    const modal = document.getElementById('logs-modal');
    const terminalBox = document.getElementById('log-terminal-box');

    if (modal) modal.classList.remove('hidden');

    try {
        const res = await fetch('/api/logs');
        const data = await res.json();

        if (data.success && terminalBox) {
            terminalBox.innerHTML = '';
            data.logs.forEach(log => {
                const line = document.createElement('div');
                line.className = 'log-line';
                line.textContent = `[${log.timestamp}] ${log.message}`;
                terminalBox.appendChild(line);
            });
        }
    } catch (err) {
        if (terminalBox) terminalBox.innerHTML = '<p class="text-danger">Failed to fetch logs.</p>';
    }
}

function closeLogsModal() {
    const modal = document.getElementById('logs-modal');
    if (modal) modal.classList.add('hidden');
}
