"""
===============================================================================
 FLOWPAY - PYTHON FLASK PAYMENT ENGINE BACKEND (app.py)
 ===============================================================================
 FlowPay is an offline-first digital mobile payments web application built using:
 1. Python & Flask framework for web server routes and API logic.
 2. SQLite3 database for local persistent storage of wallet & transactions.
 3. Jinja2 templates (HTML) for the visual user interface.
 4. Vanilla CSS & JS for modern UI styling and dynamic fetch requests.
===============================================================================
"""

import os
import sqlite3
import random
import datetime
from flask import Flask, render_template, request, jsonify, redirect, url_for, session

# -----------------------------------------------------------------------------
# FLASK APP INITIALIZATION
# -----------------------------------------------------------------------------
app = Flask(__name__)

# Secret key is required by Flask to securely encrypt user session cookies
app.secret_key = 'flowpay_super_secret_key_for_session_management'

# Database file location (SQLite local file / Vercel serverless /tmp fallback)
if os.environ.get('VERCEL') or os.environ.get('AWS_LAMBDA_FUNCTION_NAME') or not os.access('.', os.W_OK):
    DATABASE_FILE = os.path.join('/tmp', 'flowpay.db')
else:
    DATABASE_FILE = 'flowpay.db'

_db_initialized = False

def ensure_db_initialized():
    """Ensures database tables are initialized once on startup or serverless cold start."""
    global _db_initialized
    if not _db_initialized:
        _db_initialized = True
        try:
            init_db()
        except Exception as e:
            print(f"Database initialization exception: {e}")


# -----------------------------------------------------------------------------
# DATABASE HELPER FUNCTIONS
# -----------------------------------------------------------------------------

def get_db_connection():
    """
    Establishes a connection to the local SQLite database.
    row_factory = sqlite3.Row enables column access by name like dictionary keys: row['balance']
    """
    ensure_db_initialized()
    conn = sqlite3.connect(DATABASE_FILE)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    """
    Creates necessary database tables if they do not exist and populates initial demo data.
    Tables created:
    1. user_profile - Stores account balance, PIN, offline limits, and bank info.
    2. transactions - Stores all completed and pending offline payment records.
    3. system_logs  - Stores execution activity logs displayed in the UI log modal.
    """
    conn = sqlite3.connect(DATABASE_FILE)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    # 1. Table for User Profile & Wallet Settings
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS user_profile (
            id INTEGER PRIMARY KEY DEFAULT 1,
            name TEXT,
            phone TEXT,
            upi_id TEXT,
            bank_name TEXT,
            account_number TEXT,
            ifsc TEXT,
            pin TEXT,
            balance REAL,
            offline_limit REAL,
            offline_limit_remaining REAL,
            is_online INTEGER DEFAULT 1,
            is_low_battery INTEGER DEFAULT 0
        )
    ''')

    # 2. Table for Transaction History
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS transactions (
            id TEXT PRIMARY KEY,
            amount REAL,
            receiver_name TEXT,
            receiver_upi TEXT,
            date TEXT,
            time TEXT,
            status TEXT,
            is_offline INTEGER
        )
    ''')

    # 3. Table for Real-time System Logs
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS system_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            timestamp TEXT,
            message TEXT
        )
    ''')

    # Seed Default User Profile if table is empty
    cursor.execute('SELECT COUNT(*) FROM user_profile')
    if cursor.fetchone()[0] == 0:
        # Default user with ₹10,000 balance and ₹5,000 offline payment capacity
        cursor.execute('''
            INSERT INTO user_profile (
                id, name, phone, upi_id, bank_name, account_number, ifsc, pin, 
                balance, offline_limit, offline_limit_remaining, is_online, is_low_battery
            ) VALUES (
                1, 'Aarav Sharma', '+91 98765 43210', 'aarav@upiflow', 
                'ICICI Bank', '109283746562', 'ICIC0000123', '1234', 
                10000.00, 5000.00, 5000.00, 1, 0
            )
        ''')

        # Seed sample initial transactions
        initial_txns = [
            ('TXN82749201', 1500.00, 'Starbucks Coffee', 'starbucks@upi', '04 Jul 2026', '09:30 AM', 'Synced', 0),
            ('TXN73849202', 120.00, 'Karan Sharma (Taxi)', 'karan.sharma@okaxis', '04 Jul 2026', '02:15 PM', 'Synced', 0),
            ('TXN38491029', 850.00, 'Supermarket Store', 'groceries@ybl', '03 Jul 2026', '08:45 PM', 'Synced', 0)
        ]
        cursor.executemany('''
            INSERT INTO transactions (id, amount, receiver_name, receiver_upi, date, time, status, is_offline)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ''', initial_txns)

        # Log system initialization
        current_time = datetime.datetime.now().strftime("%I:%M:%S %p")
        cursor.execute('INSERT INTO system_logs (timestamp, message) VALUES (?, ?)', 
                       (current_time, 'FlowPay Python Flask Engine & Database Initialized'))

    conn.commit()
    conn.close()


def log_event(message):
    """Utility function to log system events into the database safely."""
    try:
        conn = get_db_connection()
        timestamp = datetime.datetime.now().strftime("%I:%M:%S %p")
        conn.cursor().execute('INSERT INTO system_logs (timestamp, message) VALUES (?, ?)', (timestamp, message))
        conn.commit()
        conn.close()
    except Exception as e:
        print(f"Log event failed: {e}")


def get_user_data():
    """Fetch the single active user profile from SQLite with safe fallback."""
    try:
        conn = get_db_connection()
        user = conn.cursor().execute('SELECT * FROM user_profile WHERE id = 1').fetchone()
        conn.close()
        if user:
            return dict(user)
    except Exception as e:
        print(f"Error fetching user data: {e}")
    
    return {
        'id': 1, 'name': 'Aarav Sharma', 'phone': '+91 98765 43210', 'upi_id': 'aarav@upiflow', 
        'bank_name': 'ICICI Bank', 'account_number': '109283746562', 'ifsc': 'ICIC0000123', 'pin': '1234', 
        'balance': 10000.00, 'offline_limit': 5000.00, 'offline_limit_remaining': 5000.00, 
        'is_online': 1, 'is_low_battery': 0
    }


def get_transactions_list():
    """Fetch all transactions in reverse chronological order with fallback."""
    try:
        conn = get_db_connection()
        txns = conn.cursor().execute('SELECT * FROM transactions ORDER BY rowid DESC').fetchall()
        conn.close()
        if txns:
            return [dict(t) for t in txns]
    except Exception as e:
        print(f"Error fetching transactions: {e}")
    
    return [
        {'id': 'TXN82749201', 'amount': 1500.00, 'receiver_name': 'Starbucks Coffee', 'receiver_upi': 'starbucks@upi', 'date': '04 Jul 2026', 'time': '09:30 AM', 'status': 'Synced', 'is_offline': 0},
        {'id': 'TXN73849202', 'amount': 120.00, 'receiver_name': 'Karan Sharma (Taxi)', 'receiver_upi': 'karan.sharma@okaxis', 'date': '04 Jul 2026', 'time': '02:15 PM', 'status': 'Synced', 'is_offline': 0},
        {'id': 'TXN38491029', 'amount': 850.00, 'receiver_name': 'Supermarket Store', 'receiver_upi': 'groceries@ybl', 'date': '03 Jul 2026', 'time': '08:45 PM', 'status': 'Synced', 'is_offline': 0}
    ]


def get_recent_logs(limit=30):
    """Fetch recent execution logs for terminal display with fallback."""
    try:
        conn = get_db_connection()
        logs = conn.cursor().execute('SELECT * FROM system_logs ORDER BY id DESC LIMIT ?', (limit,)).fetchall()
        conn.close()
        if logs:
            return [dict(l) for l in logs]
    except Exception as e:
        print(f"Error fetching logs: {e}")
    
    return [{'id': 1, 'timestamp': datetime.datetime.now().strftime("%I:%M:%S %p"), 'message': 'FlowPay Python Flask Engine Initialized'}]


# -----------------------------------------------------------------------------
# FLASK ROUTE HANDLERS (PAGES & HTML RENDERING)
# -----------------------------------------------------------------------------

@app.before_request
def check_auth():
    """
    Security & System Middleware:
    Ensures database initialization and user authentication via session.
    """
    ensure_db_initialized()
    allowed_routes = ['login', 'static', 'api_login']
    if not session.get('authenticated') and request.endpoint not in allowed_routes:
        return redirect(url_for('login'))


@app.route('/login', methods=['GET', 'POST'])
def login():
    """
    Login Screen:
    Handles PIN authentication (Default PIN: 1234) and instant showcase demo access.
    """
    # Instant 1-Click Demo access via query parameter or button click
    if request.args.get('demo') == 'true' or request.form.get('demo') == 'true':
        session['authenticated'] = True
        log_event('User authenticated via Instant Demo Access')
        return redirect(url_for('index'))

    if request.method == 'POST':
        entered_pin = request.form.get('pin', '')
        user = get_user_data()
        if user and entered_pin == user.get('pin', '1234'):
            session['authenticated'] = True
            log_event('User authenticated successfully with security PIN')
            return redirect(url_for('index'))
        else:
            log_event('Authentication failed: Invalid PIN entry')
            return render_template('login.html', error='Invalid Security PIN. Default PIN is 1234.')
    
    return render_template('login.html', error=None)


@app.route('/logout')
def logout():
    """Logs out user session and redirects to login."""
    session.pop('authenticated', None)
    log_event('User logged out of session')
    return redirect(url_for('login'))


@app.route('/')
def index():
    """Home Dashboard: displays wallet balance, offline capacity, quick actions, and recent transactions."""
    user = get_user_data()
    txns = get_transactions_list()
    logs = get_recent_logs()
    pending_count = len([t for t in txns if t['status'] == 'Pending Sync'])
    return render_template('index.html', user=user, transactions=txns[:5], pending_count=pending_count, logs=logs)


@app.route('/wallet')
def wallet():
    """Wallet Screen: Add money or withdraw funds to bank account."""
    user = get_user_data()
    return render_template('wallet.html', user=user)


@app.route('/pay')
def pay():
    """Payment Screen: Initiate online payments or offline payments."""
    user = get_user_data()
    return render_template('pay.html', user=user)


@app.route('/history')
def history():
    """Transaction History Screen: View full log of payments with sync filters."""
    user = get_user_data()
    txns = get_transactions_list()
    return render_template('history.html', user=user, transactions=txns)


@app.route('/sync')
def sync():
    """Offline Sync Center: Synchronize pending offline transactions to server."""
    user = get_user_data()
    txns = get_transactions_list()
    pending_txns = [t for t in txns if t['status'] == 'Pending Sync']
    return render_template('sync.html', user=user, pending_transactions=pending_txns)


@app.route('/profile')
def profile():
    """Profile & Bank Credentials Screen."""
    user = get_user_data()
    return render_template('profile.html', user=user)


@app.route('/settings')
def settings():
    """Settings & Security Screen: PIN management & System Reset."""
    user = get_user_data()
    return render_template('settings.html', user=user)


# -----------------------------------------------------------------------------
# REST API ENDPOINTS (JSON RESPONSE HANDLERS)
# -----------------------------------------------------------------------------

@app.route('/api/login', methods=['POST'])
def api_login():
    """JSON API for PIN login validation."""
    data = request.get_json() or {}
    pin = data.get('pin', '')
    user = get_user_data()
    if pin == user['pin']:
        session['authenticated'] = True
        log_event('API Login successful')
        return jsonify({'success': True})
    return jsonify({'success': False, 'message': 'Incorrect PIN. Try 1234.'}), 401


@app.route('/api/wallet/add', methods=['POST'])
def add_money():
    """API Endpoint: Load money into wallet."""
    data = request.get_json() or {}
    try:
        amount = float(data.get('amount', 0))
    except ValueError:
        return jsonify({'success': False, 'message': 'Invalid amount value.'}), 400

    if amount <= 0:
        return jsonify({'success': False, 'message': 'Amount must be greater than zero.'}), 400

    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('UPDATE user_profile SET balance = balance + ? WHERE id = 1', (amount,))
    
    # Record transaction
    txn_id = f"TXN{random.randint(10000000, 99999999)}"
    now = datetime.datetime.now()
    date_str = now.strftime("%d %b %Y")
    time_str = now.strftime("%I:%M %p")
    user = get_user_data()
    status = 'Synced' if user['is_online'] else 'Pending Sync'
    is_offline = 0 if user['is_online'] else 1

    cursor.execute('''
        INSERT INTO transactions (id, amount, receiver_name, receiver_upi, date, time, status, is_offline)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ''', (txn_id, amount, 'Loaded to Wallet', 'self@flowpay', date_str, time_str, status, is_offline))
    conn.commit()
    conn.close()

    log_event(f'Loaded ₹{amount:.2f} into Wallet. Txn ID: {txn_id}')
    return jsonify({
        'success': True, 
        'message': f'Successfully loaded ₹{amount:.2f} to your wallet!', 
        'new_balance': user['balance'] + amount
    })


@app.route('/api/wallet/withdraw', methods=['POST'])
def withdraw_money():
    """API Endpoint: Withdraw money to linked bank account."""
    data = request.get_json() or {}
    try:
        amount = float(data.get('amount', 0))
    except ValueError:
        return jsonify({'success': False, 'message': 'Invalid amount value.'}), 400

    user = get_user_data()
    if amount <= 0 or amount > user['balance']:
        return jsonify({'success': False, 'message': 'Insufficient funds in wallet.'}), 400

    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('UPDATE user_profile SET balance = balance - ? WHERE id = 1', (amount,))
    
    txn_id = f"TXN{random.randint(10000000, 99999999)}"
    now = datetime.datetime.now()
    date_str = now.strftime("%d %b %Y")
    time_str = now.strftime("%I:%M %p")
    status = 'Synced' if user['is_online'] else 'Pending Sync'
    is_offline = 0 if user['is_online'] else 1

    cursor.execute('''
        INSERT INTO transactions (id, amount, receiver_name, receiver_upi, date, time, status, is_offline)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ''', (txn_id, amount, 'Withdrawn to Bank Account', 'bank@account', date_str, time_str, status, is_offline))
    conn.commit()
    conn.close()

    log_event(f'Withdrew ₹{amount:.2f} to Bank Account. Txn ID: {txn_id}')
    return jsonify({
        'success': True, 
        'message': f'Successfully withdrawn ₹{amount:.2f} to bank account!', 
        'new_balance': user['balance'] - amount
    })


@app.route('/api/pay', methods=['POST'])
def execute_pay():
    """
    API Endpoint: Process payment.
    Handles both Online Mode (instant sync) and Offline Mode (queued pending transaction).
    Enforces offline rules:
    - Maximum ₹2,000 per single offline payment.
    - Remaining offline balance limit check.
    """
    data = request.get_json() or {}
    try:
        amount = float(data.get('amount', 0))
    except ValueError:
        return jsonify({'success': False, 'message': 'Invalid amount.'}), 400

    receiver_name = data.get('receiver_name', 'Merchant').strip()
    receiver_upi = data.get('receiver_upi', 'merchant@upi').strip()

    if not receiver_name or not receiver_upi:
        return jsonify({'success': False, 'message': 'Please provide recipient name and UPI ID.'}), 400

    user = get_user_data()
    is_online = bool(user['is_online'])

    # 1. Total Wallet Balance Check
    if user['balance'] < amount:
        log_event(f'Payment Failed: Insufficient wallet balance (Requested: ₹{amount:.2f}, Available: ₹{user["balance"]:.2f})')
        return jsonify({'success': False, 'message': 'Insufficient funds in wallet.'}), 400

    # 2. Offline Mode Checks & Limits
    if not is_online:
        if amount > 2000:
            log_event('Security Alert: Single offline payment limit of ₹2,000 exceeded.')
            return jsonify({'success': False, 'message': 'Single offline payment limit is ₹2,000 max.'}), 400
        
        if user['offline_limit_remaining'] < amount:
            log_event('Payment Failed: Total remaining offline spend limit exceeded.')
            return jsonify({'success': False, 'message': 'Exceeds remaining offline spend limit.'}), 400

    conn = get_db_connection()
    cursor = conn.cursor()

    # Deduct balance and update offline remaining if offline
    if not is_online:
        new_offline_rem = user['offline_limit_remaining'] - amount
        cursor.execute('UPDATE user_profile SET balance = balance - ?, offline_limit_remaining = ? WHERE id = 1', 
                       (amount, new_offline_rem))
        status = 'Pending Sync'
        is_offline = 1
        log_msg = f'OFFLINE payment of ₹{amount:.2f} to {receiver_name} queued (Pending Sync).'
    else:
        cursor.execute('UPDATE user_profile SET balance = balance - ? WHERE id = 1', (amount,))
        status = 'Synced'
        is_offline = 0
        log_msg = f'ONLINE payment of ₹{amount:.2f} completed to {receiver_name}.'

    txn_id = f"TXN{random.randint(10000000, 99999999)}"
    now = datetime.datetime.now()
    date_str = now.strftime("%d %b %Y")
    time_str = now.strftime("%I:%M %p")

    cursor.execute('''
        INSERT INTO transactions (id, amount, receiver_name, receiver_upi, date, time, status, is_offline)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ''', (txn_id, amount, receiver_name, receiver_upi, date_str, time_str, status, is_offline))

    conn.commit()
    conn.close()

    log_event(f"{log_msg} Txn ID: {txn_id}")
    return jsonify({
        'success': True, 
        'message': f'Payment of ₹{amount:.2f} to {receiver_name} successful!',
        'txn_id': txn_id,
        'status': status,
        'is_offline': is_offline
    })


@app.route('/api/sync', methods=['POST'])
def execute_sync():
    """API Endpoint: Synchronize all pending offline transactions to server."""
    conn = get_db_connection()
    cursor = conn.cursor()

    pending = cursor.execute("SELECT COUNT(*) FROM transactions WHERE status = 'Pending Sync'").fetchone()[0]
    if pending == 0:
        conn.close()
        return jsonify({'success': True, 'message': 'No pending transactions to synchronize.', 'synced_count': 0})

    # Mark pending transactions as Synced and reset offline capacity limit
    cursor.execute("UPDATE transactions SET status = 'Synced' WHERE status = 'Pending Sync'")
    cursor.execute("UPDATE user_profile SET offline_limit_remaining = offline_limit WHERE id = 1")
    conn.commit()
    conn.close()

    log_event(f'Synchronization Complete: {pending} offline transaction(s) synced to server.')
    return jsonify({
        'success': True, 
        'message': f'Successfully synchronized {pending} offline transaction(s)!', 
        'synced_count': pending
    })


@app.route('/api/toggle-network', methods=['POST'])
def toggle_network():
    """API Endpoint: Toggle simulated network connectivity (Online vs Offline)."""
    user = get_user_data()
    new_status = 0 if user['is_online'] else 1
    
    conn = get_db_connection()
    conn.cursor().execute('UPDATE user_profile SET is_online = ? WHERE id = 1', (new_status,))
    conn.commit()
    conn.close()

    status_str = "ONLINE Mode" if new_status else "OFFLINE Mode"
    log_event(f'Network connectivity simulator switched to {status_str}')
    return jsonify({'success': True, 'is_online': bool(new_status), 'message': f'Switched to {status_str}'})


@app.route('/api/toggle-battery', methods=['POST'])
def toggle_battery():
    """API Endpoint: Toggle low power mode simulation."""
    user = get_user_data()
    new_status = 0 if user['is_low_battery'] else 1

    conn = get_db_connection()
    conn.cursor().execute('UPDATE user_profile SET is_low_battery = ? WHERE id = 1', (new_status,))
    conn.commit()
    conn.close()

    status_str = "Low Power Mode (12% Battery)" if new_status else "Standard Power Mode"
    log_event(f'Battery simulator status changed to {status_str}')
    return jsonify({'success': True, 'is_low_battery': bool(new_status), 'message': status_str})


@app.route('/api/profile', methods=['POST'])
def update_profile():
    """API Endpoint: Update profile and bank information."""
    data = request.get_json() or {}
    name = data.get('name', '').strip()
    phone = data.get('phone', '').strip()
    upi_id = data.get('upi_id', '').strip()
    bank_name = data.get('bank_name', '').strip()
    account_number = data.get('account_number', '').strip()
    ifsc = data.get('ifsc', '').strip()

    if not name or not phone or not upi_id:
        return jsonify({'success': False, 'message': 'Name, Phone, and UPI ID are required.'}), 400

    conn = get_db_connection()
    conn.cursor().execute('''
        UPDATE user_profile 
        SET name = ?, phone = ?, upi_id = ?, bank_name = ?, account_number = ?, ifsc = ? 
        WHERE id = 1
    ''', (name, phone, upi_id, bank_name, account_number, ifsc))
    conn.commit()
    conn.close()

    log_event('User profile and bank details updated.')
    return jsonify({'success': True, 'message': 'Profile details saved successfully.'})


@app.route('/api/settings/pin', methods=['POST'])
def update_pin():
    """API Endpoint: Update security 4-digit PIN."""
    data = request.get_json() or {}
    old_pin = data.get('old_pin', '').strip()
    new_pin = data.get('new_pin', '').strip()

    user = get_user_data()
    if old_pin != user['pin']:
        return jsonify({'success': False, 'message': 'Incorrect current PIN.'}), 400

    if len(new_pin) != 4 or not new_pin.isdigit():
        return jsonify({'success': False, 'message': 'New PIN must be a 4-digit number.'}), 400

    conn = get_db_connection()
    conn.cursor().execute('UPDATE user_profile SET pin = ? WHERE id = 1', (new_pin,))
    conn.commit()
    conn.close()

    log_event('Security PIN updated successfully.')
    return jsonify({'success': True, 'message': 'Security PIN updated successfully.'})


@app.route('/api/reset', methods=['POST'])
def reset_data():
    """API Endpoint: Restore database to factory default demo state."""
    if os.path.exists(DATABASE_FILE):
        os.remove(DATABASE_FILE)
    init_db()
    log_event('System Factory Reset: Database restored to default demo values.')
    return jsonify({'success': True, 'message': 'Database reset to initial demo state.'})


@app.route('/api/logs', methods=['GET'])
def get_logs():
    """API Endpoint: Fetch activity execution logs for modal display."""
    logs = get_recent_logs(50)
    return jsonify({'success': True, 'logs': logs})


# -----------------------------------------------------------------------------
# MAIN ENTRY POINT
# -----------------------------------------------------------------------------
if __name__ == '__main__':
    # Initialize database tables on server launch
    init_db()
    
    print("\n========================================================")
    print(" ⚡ FlowPay Python Flask Server Running Successfully")
    print(" 🌐 URL: http://127.0.0.1:5000")
    print(" 🔑 Default Login PIN: 1234")
    print("========================================================\n")
    
    # Run development server on port 5000
    app.run(host='127.0.0.1', port=5000, debug=True)
