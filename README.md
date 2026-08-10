# ⚡ FlowPay - Secure Offline UPI Mobile FinTech Application (Android Studio Project)

**FlowPay** is a production-grade, modern Android FinTech application built with **Kotlin**, **Jetpack Compose (Material Design 3)**, **Room Database**, and **MVVM Architecture**.

It demonstrates a **Secure Offline UPI Payment System** that allows users to sign, store, and execute digital payment transactions locally when offline. When internet connectivity is restored, all pending transactions automatically synchronize with the central ledger, update user balances atomically, and issue real-time status notifications.

---

## 🌟 Key Architectural Features

- 🔐 **Demo PIN Authentication**: Multi-account demo switcher between **Nikhil** (`nikhil@flowpay`, ₹50,000) and **Rahul** (`rahul@flowpay`, ₹30,000) protected by a 4-digit security PIN (`1234`).
- ⚡ **Secure Offline UPI Engine**:
  - Sign & queue transactions when offline (`OFFLINE_PENDING`).
  - Enforce per-transaction and total offline quota limits (e.g. ₹5,000 limit).
  - Provisional local wallet balance reservations.
- 🔄 **Automatic Offline Synchronization (`OfflineSyncManager`)**:
  - Automatically processes queued offline transactions as soon as internet connectivity returns.
  - Atomically settles sender and receiver balances in local SQLite (Room DB).
  - Emits real-time notifications ("Offline Payment Synchronized").
- 🛡️ **Duplicate Payment Protection (`PaymentTokenEngine`)**:
  - Encrypted **SHA-256** token generation derived from `(Sender, Receiver, Amount, Timestamp, Nonce)`.
  - Enforces strict anti-replay validation; duplicate tokens are immediately flagged and rejected.
- 📷 **Demo QR Code Scanner**:
  - Viewfinder scanner frame with animated laser scanner beam.
  - Instant demo QR triggers to test scanning payloads (`upi://pay?pa=rahul@flowpay...`).
- 💳 **Complete Banking & Passbook Suite**:
  - **Home Dashboard**: Quick transfer grid, balance card with show/hide PIN toggle, recent transactions, utility bills, rewards.
  - **Send & Receive Money**: Transfer via UPI ID, Mobile Number, Bank Account + IFSC, QR Code.
  - **Banking Section**: Linked Bank details (SBI / HDFC), balance enquiry, mini statement, IFSC, account status.
  - **Passbook**: Income vs. Expense monthly summary breakdown, ledger timeline.
  - **History**: Searchable & filterable transaction list (All, Offline Pending, Completed, Sent, Received).
  - **Profile & Settings**: KYC status badge, biometric toggle, dark/light theme, demo data factory reset.

---

## 🔑 Demo Account Credentials

| User | Name | Phone Number | UPI ID | Bank Name | Account Number | Wallet Balance | PIN |
|---|---|---|---|---|---|---|---|
| **Demo User 1** | Nikhil | `9876543210` | `nikhil@flowpay` | State Bank of India | `1234567890` | **₹50,000** | `1234` |
| **Demo User 2** | Rahul | `9123456780` | `rahul@flowpay` | HDFC Bank | `9876543210` | **₹30,000** | `1234` |

---

## 📁 Android Studio Project Structure

```text
FlowPay/
├── build.gradle.kts                # Root project build script
├── settings.gradle.kts             # Gradle settings & repositories
├── gradle.properties               # AndroidX & JVM compiler settings
├── gradle/
│   ├── wrapper/                    # Gradle wrapper configuration
│   └── libs.versions.toml          # Centralized version catalog
├── app/
    ├── build.gradle.kts            # App module dependencies (Compose M3, Room, Coroutines)
    └── src/main/
        ├── AndroidManifest.xml     # Permissions (Camera, Internet, Network state)
        ├── java/com/flowpay/app/
        │   ├── MainActivity.kt     # Launcher Activity & NavHost setup
        │   ├── data/
        │   │   ├── local/
        │   │   │   ├── FlowPayDatabase.kt          # Room DB & Demo Data Seeder
        │   │   │   ├── dao/
        │   │   │   │   ├── UserDao.kt
        │   │   │   │   ├── TransactionDao.kt
        │   │   │   │   ├── TokenDao.kt
        │   │   │   │   └── NotificationDao.kt
        │   │   │   └── entity/
        │   │   │       ├── UserEntity.kt
        │   │   │       ├── TransactionEntity.kt
        │   │   │       ├── TokenEntity.kt
        │   │   │       └── NotificationEntity.kt
        │   │   └── repository/
        │   │       └── UserPreferencesRepository.kt # DataStore settings
        │   ├── engine/
        │   │   ├── PaymentTokenEngine.kt           # SHA-256 Token Fraud Protection
        │   │   └── OfflineSyncManager.kt           # Background Queue Sync Manager
        │   └── ui/
        │       ├── theme/                          # Material Design 3 HSL Theme
        │       ├── components/                     # Reusable TopBar, BalanceCard, Grid, Items
        │       ├── screens/                        # 12+ Jetpack Compose Screens
        │       └── viewmodel/                      # MVVM ViewModels
        └── res/                            # String resources, themes, drawables
```

---

## 🚀 How to Open & Run in Android Studio

1. Launch **Android Studio** (Jellyfish / Koala / Ladybug or newer recommended).
2. Click **Open** and select this directory: `FlowPay/`.
3. Wait for Gradle sync to complete.
4. Select an Android Emulator or connected device (API 24+ / Android 7.0+).
5. Click **Run 'app'** (`Shift + F10`) to build and launch the application.

---

## 🧪 Testing Offline Payments & Synchronization

1. Launch the app and log in as **Nikhil** (PIN: `1234`).
2. Click the top header connection pill (**ONLINE**) to toggle to **OFFLINE** mode.
3. Tap **Send Money** or **Scan & Pay**, enter recipient `rahul@flowpay`, enter amount `₹500`, and enter PIN `1234`.
4. Observe that the transaction status is saved as **`OFFLINE PENDING`**, a unique cryptographic token is registered, and an offline notification is logged.
5. Toggle the header connection pill back to **ONLINE**.
6. The `OfflineSyncManager` instantly executes background sync, settles balances (Nikhil ₹49,500, Rahul ₹30,500), updates the transaction status to **`COMPLETED`**, and displays a sync confirmation toast!
