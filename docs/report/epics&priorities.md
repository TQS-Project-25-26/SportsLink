### 2.3 Project epics and priorities

**Implementation Strategy:**

The project will follow an incremental delivery approach across 5 main iterations (I1-I5), with features prioritized by business value and technical dependencies. Each epic contains multiple user stories that will be decomposed in subsequent planning sessions.

---

#### **Epic 1: User Management & Authentication** [HIGH PRIORITY - I1-I2]

**Description:** Enable users to register, authenticate securely, and manage their profiles with role-based access control.

**Business Value:** Foundation for entire platform; without authentication, no other features work.

**Features included:**
- User registration (email/phone validation)
- Login/logout with password security
- Role assignment (Owner, Renter, Admin)
- Profile management (personal info, payment methods)
- Password reset functionality
- Session management

**Implementation Timeline:**
- I1: Backend authentication service, API endpoints, database schema
- I2: Frontend login/registration UI, password validation, email verification

**Acceptance Criteria:**
- User can register and login securely within 30 seconds
- Passwords stored with bcrypt hashing
- Session tokens expire after 24 hours
- Role-based access control enforced on all protected endpoints

---

#### **Epic 2: Field Discovery & Catalog** [HIGH PRIORITY - I1-I3]

**Description:** Enable renters to search, filter, and discover sports fields with comprehensive details.

**Business Value:** Core renter experience; directly impacts booking conversion rates.

**Features included:**
- Browse all available fields/facilities
- Search by facility name, sport type
- Filter by location (proximity-based)
- Filter by price range, availability, ratings
- View detailed facility information (photos, equipment list, reviews, calendar)
- Favorites/wishlist functionality
- Map visualization of facility locations
- Sort by distance, price, rating, availability

**Implementation Timeline:**
- I1: Backend API for search/filter, database queries optimized
- I2: Frontend search UI, map integration, filtering UI
- I3: Performance optimization, advanced filtering combinations

**Acceptance Criteria:**
- Search results load in <1 second for 100+ facilities
- Filters work in combination (e.g., "Padel + <5km + <€25/hr")
- Map shows up to 50 facilities without lag
- Facilities appear in results within 5 minutes of being posted

---

#### **Epic 3: Booking & Reservation System** [HIGH PRIORITY - I2-I3]

**Description:** Allow renters to view availability, reserve time slots, and manage bookings. Prevent double-bookings.

**Business Value:** Core transaction mechanism; revenue generation depends on reliable booking system.

**Features included:**
- Calendar view of facility availability
- Time slot selection and reservation
- Real-time availability updates
- Double-booking prevention (database constraints)
- Booking confirmation (email/SMS)
- Booking management (view, modify, cancel)
- Cancellation policies
- Wait-list functionality for full slots

**Implementation Timeline:**
- I2: Backend booking logic, conflict detection, confirmation service
- I3: Calendar UI, booking status tracking, modification workflows
- I4: Cancellation policies, wait-list management

**Acceptance Criteria:**
- Booking confirmed within 3 seconds of submission
- Double-bookings impossible (database-level validation)
- Cancellation confirmation received within 5 minutes
- Calendar displays availability in real-time

---

#### **Epic 4: Payment & Transactions** [HIGH PRIORITY - I3-I4]

**Description:** Process payments securely using Stripe, handle deposits/refunds, and track transactions.

**Business Value:** Revenue-critical; enables monetization of the platform.

**Features included:**
- Payment method storage (Stripe tokenization)
- Booking payment processing
- Refund handling
- Platform commission calculation
- Transaction history for renters and owners
- Payment receipts/invoices
- Failed payment retry logic
- Sandbox mode for testing (MVP)

**Implementation Timeline:**
- I3: Backend Stripe integration (sandbox), payment API
- I4: Payment UI (secure forms), transaction history, refund workflows

**Acceptance Criteria:**
- Payment processes within 5 seconds (Stripe API latency included)
- Refunds appear in user account within 2-3 business days
- Failed payments trigger retry after 24 hours
- Transaction records are immutable and audit-logged

---

#### **Epic 5: Owner Field Management** [HIGH PRIORITY - I2-I4]

**Description:** Enable field owners to register, manage, and monitor their facilities and equipment.

**Business Value:** Directly enables field owners to monetize assets; high-value user segment.

**Features included:**
- Register new facility with details (name, address, sport types)
- Upload facility photos
- Add/manage equipment inventory
- Update pricing and availability
- Mark facilities as active/inactive
- Equipment maintenance tracking and status updates
- Owner dashboard showing occupancy, earnings, bookings
- Availability calendar management
- Facility analytics (popular time slots, revenue by facility)

**Implementation Timeline:**
- I2: Backend facility registration, equipment management APIs
- I3: Owner dashboard UI, analytics components
- I4: Advanced analytics, maintenance tracking refinements

**Acceptance Criteria:**
- Facility appears in search results within 5 minutes of posting
- Owner can update equipment status within 30 seconds
- Dashboard shows real-time occupancy percentages
- Revenue calculations are accurate within 2 decimal places

---

### **Future Work**

##### **Reviews & Trust System** [MEDIUM PRIORITY]

**Description:** Build trust through community ratings and reviews of facilities, equipment, and users.

---

##### **Admin Dashboard & Platform Analytics** [MEDIUM PRIORITY ]

**Description:** Provide Ana (admin) with platform-wide visibility into operations, metrics, and issues.

---

##### **Epic 8: Renter Dashboard & Booking History** [MEDIUM PRIORITY]

**Description:** Provide renters (like Maria) with easy access to their booking history and current reservations.

---

##### **Community Features & Occupancy Indicators** [LOWER PRIORITY]

**Description:** Enable community building features like occupancy indicators and group organization (supporting Pedro's scenario).

---

##### **Notifications & Communication** [LOWER PRIORITY]

**Description:** Keep users informed of important events (booking confirmations, reminders, etc.).

---

### 2.3 Project epics and priorities

| Epic | Priority | Timeline | Description |
|------|----------|----------|-------------|
| **1. User Management & Authentication** | HIGH | I1-I2 | Registration, login, role-based access |
| **2. Field Discovery & Catalog** | HIGH | I1-I3 | Search and filter facilities by sport, location, price |
| **3. Booking & Reservation System** | HIGH | I2-I3 | Calendar-based reservations with conflict prevention |
| **4. Payment & Transactions** | HIGH | I3-I4 | Stripe integration (sandbox mode), secure payments |
| **5. Owner Field Management** | HIGH | I2-I4 | Facility registration, equipment tracking, owner dashboard |
| **6. Reviews & Trust System** | MEDIUM | FW | Ratings, reviews, reputation scoring |
| **7. Admin Dashboard & Analytics** | MEDIUM | FW | KPI dashboard, platform metrics and reports |
| **8. Renter Dashboard & History** | MEDIUM | FW | Booking history, favorites, quick re-booking |
| **9. Community Features & Occupancy** | LOW | FW | Occupancy indicators, peer profiles, group chat |
| **10. Notifications & Communication** | LOW | FW | Email notifications, booking reminders, alerts |

**Release Scope:**
- **MVP (I1-I3):** Epics 1-5
- **Future Work:** Epics seguintes