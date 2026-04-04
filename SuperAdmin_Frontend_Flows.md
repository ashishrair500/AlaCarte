# SuperAdmin Frontend Implementation Guide

This document defines the step-by-step logical flows, pages, and API integrations required to build the Frontend application for the **SuperAdmin** role. Use this guide to structure your frontend architecture, routing, and state management.

---

## 🏗️ 1. Authentication Flow

### Page: **Login / Registration**
**Route:** `/auth/login` and `/auth/signup`
**Purpose:** Authenticate SuperAdmin users into the system.

**Steps:**
1. **Signup Form (Optional initial step):**
   - Collect `username`, `password`, `fullName`, `email`, `phone`.
   - Call API: `POST /api/auth/superadmin/signup`
   - Store resulting `accessToken` and `refreshToken` securely (e.g., localStorage or secure cookies).
2. **Login Form:**
   - Collect `usernameOrEmail` and `password`.
   - Call API: `POST /api/auth/superadmin/login`
   - Evaluate response. If success -> store `accessToken` and `refreshToken`.
3. **Session Verification (On App Load):**
   - Call API: `GET /api/auth/superadmin/me` using the `accessToken` in the Authorization header.
   - Store the user context (permissions, roles, user details) in global state (Redux / Context API).
   - If `accessToken` returns 401 Unauthorized, automatically call `POST /api/auth/superadmin/refresh?refreshToken=<TOKEN>` to renew it. If renewal fails, route back to `/auth/login`.

---

## 📊 2. Dashboard Flow

### Page: **SuperAdmin Dashboard**
**Route:** `/dashboard`
**Purpose:** Provide high-level statistics and shortcuts.

**Steps:**
1. **Fetch Statistics:**
   - Call API: `GET /api/superadmin/restaurants/stats`
   - Display a grid of summary cards (e.g., Total Restaurants, Active Restaurants, Popular Restaurants).
2. **Fetch Popular Restaurants (Top Picks):**
   - Call API: `GET /api/superadmin/restaurants/popular`
   - Display a preview carousel or quick-list of highly-rated/popular restaurants.
3. **Global Search Bar (Navbar):**
   - Input field triggering API: `GET /api/superadmin/restaurants/search?name=<QUERY>`
   - Display autocomplete dropdown linking instantly to restaurant profiles.

---

## 🍽️ 3. Restaurant Listing Flow

### Page: **Restaurants Table / List**
**Route:** `/restaurants`
**Purpose:** Manage, filter, and view the list of all onboarded restaurants.

**Steps:**
1. **Initial Data Load:**
   - Call API: `GET /api/superadmin/restaurants`
   - Render data in a data-table (columns: Name, City, Status, Rating, Type, Actions).
2. **Filtering Sidebar / Dropdowns:**
   - Filter by City: Call `GET /api/superadmin/restaurants/by-city/<CITY>`
   - Filter by Status: Call `GET /api/superadmin/restaurants/by-status/<STATUS>`
   - Advanced Multilevel Filter: Call `GET /api/superadmin/restaurants/advanced-search?city=X&status=Y`
3. **Quick Actions (Per Row):**
   - **Toggle Status:** Toggle switch calling `PATCH /api/superadmin/restaurants/<ID>/status?status=<NEW_STATUS>`
   - **Toggle Popularity:** Star icon calling `PATCH /api/superadmin/restaurants/<ID>/toggle-popular`
   - **Delete Restaurant:** Trash icon calling `DELETE /api/superadmin/restaurants/<ID>` (after a confirmation modal).

---

## 📝 4. Restaurant Onboarding Flow

### Page: **Add Restaurant Wizard**
**Route:** `/restaurants/new`
**Purpose:** Create a brand new restaurant profile.

**Steps:**
1. **Form Input:**
   - Form fields for `name`, `description`, `addressLine1`, `addressLine2`, `city`, `state`, `pincode`, `latitude`, `longitude`, `restaurantType`, etc.
2. **Form Submission:**
   - Call API: `POST /api/superadmin/restaurants`
   - On success, retrieve the newly created `<RESTAURANT_ID>`.
3. **Redirect to Staff Onboarding:**
   - Route to `/restaurants/<RESTAURANT_ID>/onboard-admin` automatically to assign a Restaurant Admin right after creation.

---

## 👨‍💼 5. Restaurant Admin Onboarding Flow

### Page: **Assign Restaurant Admin**
**Route:** `/restaurants/:id/onboard-admin`
**Purpose:** Give login credentials to the person managing the restaurant.

**Steps:**
1. **Context Load:** Display "Assign Admin for [Restaurant Name]".
2. **Form Input:**
   - Collect `employeeCode`, `designation`, `phone`, `email`, `userName`, `password`.
3. **Submission:**
   - Append `restaurantId` from the URL to the payload.
   - Call API: `POST /api/superadmin/restaurants/staff/onboard-restaurant-admin`
   - On success, route to the Restaurant Details page.

---

## 🔍 6. Restaurant Details & Edit Flow

### Page: **Restaurant Details**
**Route:** `/restaurants/:id`
**Purpose:** View and edit full details of a specific restaurant and manage its top-level staff.

**Steps:**
1. **Fetch Profile:**
   - Call API: `GET /api/superadmin/restaurants/<ID>`
   - Populate a read-only view with an "Edit Mode" toggle.
2. **Full Update (Edit Mode):**
   - When editing all fields, submit via `PUT /api/superadmin/restaurants/<ID>`.
3. **Partial Updates (Inline Edits):**
   - For tiny edits (like just updating description), use `PATCH /api/superadmin/restaurants/<ID>` to send only modified fields.
4. **Staff Management Tab:**
   - Display standard restaurant admins.
   - Update an existing admin's details via `PUT /api/superadmin/restaurants/staff/<STAFF_ID>`
   - Easily suspend an admin via `PATCH /api/superadmin/restaurants/staff/<STAFF_ID>` with payload `{"status": "INACTIVE"}`.

---

## 👨‍💼 7. Restaurant Staff Management Flow

### Page: **Restaurant Details -> Staff Tab**
**Route:** `/restaurants/:id/staff`
**Purpose:** Manage the top-level staff (Restaurant Admins) assigned to a given restaurant.

**Steps:**
1. **View Assigned Staff:**
   - *(Note: Since SuperAdmin primarily onboards the first admin, the list of all granular staff is usually retrieved or managed via the Restaurant Admin's own endpoints. However, any known `<STAFF_ID>` can be managed here.)*
2. **Onboard First Admin (If none exists):**
   - Click "Assign Admin" button.
   - Form collects: `employeeCode`, `designation`, `phone`, `email`, `userName`, `password`.
   - Call API: `POST /api/superadmin/restaurants/staff/onboard-restaurant-admin`
3. **Full Update of Admin Details:**
   - Click "Edit Profile" on an admin.
   - Form collects full staff payload.
   - Call API: `PUT /api/superadmin/restaurants/staff/<STAFF_ID>`
4. **Quick Suspend / Role Change:**
   - Click "Suspend" or "Change Role" inline.
   - Call API: `PATCH /api/superadmin/restaurants/staff/<STAFF_ID>`
   - Payload example: `{"status": "SUSPENDED", "isActive": false}` or `{"staffRole": "MANAGER"}`.

---

## 🛡️ Interceptor & State Management Instructions for AI

1. **Axios / Fetch Interceptors:**
   - Ensure you attach the `Authorization: Bearer <TOKEN>` header dynamically to every outward bound request.
   - Implement a global 401 Unauthorized interceptor: catch the error, fire the `/refresh` endpoint automatically locally, then seamlessly replay the failed request.

2. **Routing Guard:**
   - Only render components past `/auth/*` if `accessToken` is present in local storage AND the `/me` API successfully verifies `roles: ["ROLE_SUPER_ADMIN"]`.

3. **Notification System:**
   - Since APIs return `{ "success": boolean, "message": "string", "data": ... }`, intercept the `message` field to feed directly into transient Toast Notifications for user feedback globally.
