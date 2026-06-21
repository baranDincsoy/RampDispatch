# RampDispatch ⛽✈️

A **mobile-first aircraft fuel dispatch** app for ramp operations, built entirely with **Kotlin** and **Jetpack Compose**. RampDispatch reimagines a desktop-only dispatch board as a native Android experience designed for the people who actually use it on the ramp — fuelers and team leaders.

> **Why this project:** I spent years working the ramp as an aircraft fueler and team leader. The dispatch tools we used were desktop-only — open them on a phone and you'd get a few columns squeezed into an unreadable mess. RampDispatch is the tool I wish I'd had: built around the real fueling workflow, designed mobile-first, and architected to keep working even when signal on the ramp doesn't.

> **Note:** This is a portfolio project built with **mock data only**. It is my own design and uses no real company data, branding, or proprietary systems.

---

## 📱 App Screenshots


 <img width="438" height="829" alt="Ekran görüntüsü 2026-06-20 204021" src="https://github.com/user-attachments/assets/c9ef9069-661e-451d-bb84-d417efb48eaa" />  <img width="438" height="942" alt="Ekran görüntüsü 2026-06-20 204241" src="https://github.com/user-attachments/assets/fa833549-124c-4b8b-ae60-4d6c07962262" />  <img width="458" height="970" alt="Ekran görüntüsü 2026-06-20 204255" src="https://github.com/user-attachments/assets/0ef91f8a-9d95-4c38-be2b-0511c8d4bb91" />  <img width="440" height="977" alt="Ekran görüntüsü 2026-06-20 204320" src="https://github.com/user-attachments/assets/31a8fbc9-d0f6-4fd1-9bfa-dfdb1a2f0a65" />  <img width="432" height="939" alt="Ekran görüntüsü 2026-06-20 204341" src="https://github.com/user-attachments/assets/0a3f7463-c7b9-455c-b1ab-660c57f99ede" /> |
 <img width="445" height="938" alt="Ekran görüntüsü 2026-06-20 204354" src="https://github.com/user-attachments/assets/57d32365-adeb-4451-a125-9e357e770951" />  <img width="447" height="954" alt="Ekran görüntüsü 2026-06-20 204415" src="https://github.com/user-attachments/assets/23df7eaf-d5ca-48e0-a9d6-27c77086b19e" />  <img width="438" height="963" alt="Ekran görüntüsü 2026-06-20 204422" src="https://github.com/user-attachments/assets/142930d4-06b1-452f-9aac-0d88c0028215" />  <img width="451" height="949" alt="Ekran görüntüsü 2026-06-20 204513" src="https://github.com/user-attachments/assets/8a9cf9e4-3934-40d2-b4be-eaca4873abd5" /> 
 <img width="459" height="962" alt="Ekran görüntüsü 2026-06-20 204530" src="https://github.com/user-attachments/assets/cf0c7a68-9b06-445c-86bf-da1a11cb4a57" /> <img width="447" height="914" alt="Ekran görüntüsü 2026-06-20 204623" src="https://github.com/user-attachments/assets/bf4aac99-f58c-4a40-af77-8a3c0f298be6" />  <img width="449" height="937" alt="Ekran görüntüsü 2026-06-20 204633" src="https://github.com/user-attachments/assets/a09db9d8-2005-43f5-91d1-7e8b54264b39" />  <img width="449" height="944" alt="Ekran görüntüsü 2026-06-20 204641" src="https://github.com/user-attachments/assets/a97e773e-1847-4dd5-b796-066a7b686223" />|
---

## 🌟 Key Features

* **Role-Based Access (RBAC):** Two roles with different permissions and data scope.
  * **Team Leader** — sees all orders; can assign, reassign, and unassign fuelers; views station-wide stats.
  * **Fueler** — sees only their own assigned orders; runs the fueling workflow; cannot access assignment controls.
* **Live Dispatch Board:** Active orders sorted by departure, with three independent, combinable controls — **status filter**, **concourse filter**, and **sort** (ETD / gate / fueler).
* **Multi-Step Fueling Wizard:** An 8-step guided workflow modeled on the real ramp process — tail verification, equipment entry, **per-tank** arrival readings, pumping, per-tank final readings (±200 lbs tolerance), fuel-cap safety check, gallon-based totalizer, and close-out by employee ID.
* **Per-Aircraft Tank Layouts:** Tank configuration comes from the order data — Boeing narrowbodies show 3 tanks (Left/Center/Right); Airbus adds ACT 1 / ACT 2 for 5 tanks. The UI adapts automatically.
* **Stats Dashboard:** Completed-order counts, total fuel pumped, and per-fueler workload — all aggregated at the database level.
* **Status History Timeline:** Every status transition is timestamped and logged per order.

---

## 🛠 Tech Stack & Libraries

* **Language:** Kotlin
* **UI:** Jetpack Compose, Material 3 (dark-first design)
* **Architecture:** MVVM with Unidirectional Data Flow (UDF)
* **Local Persistence:** Room (single source of truth)
* **Networking:** Retrofit + Kotlinx Serialization
* **Asynchronous / Reactivity:** Coroutines, Flow, StateFlow
* **Navigation:** Navigation Compose

---

## 🏗 Architecture

RampDispatch uses an **offline-first** architecture. The UI **never** reads from the network directly — it only ever observes the local Room database. A remote JSON source (served from GitHub raw, simulating a REST API) feeds Room; the UI reacts to Room.

```
Retrofit (remote JSON)  ──►  Repository  ──►  Room (source of truth)  ──►  StateFlow  ──►  Compose UI
                                                  ▲                                            │
                                                  └──────────  user actions (writes)  ◄────────┘
```

The result: lose signal on the ramp and the board still works from cache. Data changes propagate automatically — complete an order and it disappears from the board on its own, with no manual "refresh the list" code.

**Layers**

* **`domain/model`** — pure Kotlin models and enums (`FuelOrder`, `OrderStatus`, `FuelTank`, `UserRole`). No Android, Room, or Retrofit dependencies.
* **`data`** — `remote` (Retrofit API + DTOs), `local` (Room entities + DAOs), `repository` (single source of truth + DTO/Entity/Domain mappers), `session` (in-memory auth/role state).
* **`ui`** — one package per screen (`board`, `detail`, `fueling`, `stats`, `login`), each with a ViewModel exposing a single immutable `UiState` via `StateFlow`.
* **`navigation`** — centralized routes and the navigation graph.

---

## 💡 Engineering Decisions & Trade-offs

This section captures the reasoning behind the design — the parts I'd want to talk through in an interview.

* **Derived state, not stored state.** "Overdue" is never stored; it's computed from departure time vs. now. Likewise, fueling totals and "gallons pumped" are derived from inputs. Store the facts, derive the conclusions.
* **`OnConflictStrategy.IGNORE` on refresh.** When new data is pulled from the remote source, existing rows are left untouched so local status changes survive a refresh. Local is the source of truth; a sync must never overwrite the user's work.
* **Database-level aggregation.** Stats use SQL `COUNT` / `SUM` / `GROUP BY` rather than pulling all rows into memory and looping in Kotlin — compute close to the data.
* **UI as a state machine.** What a user can do with an order depends entirely on its status (and their role). The fueling wizard is the same idea at larger scale: each step has its own validation gate, and data is held in memory and persisted to Room only at close-out — so an abandoned wizard never leaves partial data behind.
* **Defense in depth for authorization.** Fuelers can't *see* assignment controls (UI), and they can't *reach* other fuelers' orders (the query filters by fueler ID at the database level). UI hiding is UX; the real boundary is in the data layer.
* **Manual DI.** A small `Application`-scoped container wires dependencies instead of a DI framework — appropriate scope for an MVP; Hilt is on the roadmap.

### Known Scope Limits (intentional)

* **Authentication is a demo.** Login selects a role/identity from the mock data; it demonstrates **authorization** (who can see/do what), not real **authentication** (password hashing, server-side verification, tokens). Real auth requires a backend.
* **Destructive migrations.** The app uses `fallbackToDestructiveMigration()` in development — a schema change rebuilds the local cache (safe here, since data re-syncs from remote). Production would require proper Room `Migration`s to preserve user data.
* **Tank rules simplified.** Tank layouts are modeled, but inter-tank fueling rules (e.g. max imbalance, fill order) are intentionally out of scope to keep the demo focused.

---

## 🚀 How to Run

1. Clone this repository:
   ```bash
   git clone https://github.com/baranDincsoy/RampDispatch.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle files.
4. Run on an emulator or physical device (Min SDK 26). The app fetches its mock dispatch data on launch, so an internet connection is needed for the first sync; after that it runs from the local cache.

---

## 🔮 Roadmap

* **Real authentication** with Firebase Auth (email/password) and roles stored in Firestore.
* **Persisted session** so the app remembers the logged-in user across launches.
* **Gallon ↔ lbs conversion** with fuel density, restoring totalizer reconciliation against the panel reading within tolerance.
* **Tank fueling rules** — inter-tank imbalance limits and required fill order.
* **Room migrations** to replace destructive fallback for production-grade persistence.
* **Hilt** for dependency injection.
* **Unit & UI tests** — ViewModel validation logic and Compose UI tests.

---

Developed by **Baran Cenk Dincsoy**

