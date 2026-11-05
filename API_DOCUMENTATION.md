# SkillSwap REST API Specification & Technical Documentation

**Version:** 1.0.0  
**Backend Technology Stack:** Node.js, Express.js, MongoDB Atlas (Mongoose ODM), JWT, Firebase Admin SDK  
**Deployed Base URL:** `https://skillswap-api.onrender.com/`  
**Local Development Base URL:** `http://localhost:5000/` or `http://10.0.2.2:5000/` (Android Emulator)  

---

## 1. Overview & Architecture

The SkillSwap REST API acts as the backend server for the SkillSwap Android application. It manages user authentication, skill listings, settings preferences, and push notification tokens.

### Security Highlights:
- **HTTPS Enforcement:** All API traffic is encrypted in transit over HTTPS.
- **Password Hashing:** Passwords are salted and hashed using **bcrypt** (12 rounds) server-side prior to MongoDB persistence. Plaintext passwords are never stored or returned.
- **JWT Authentication:** Stateful user sessions are managed via signed **JSON Web Tokens (JWT)**. Protected endpoints require the HTTP header:
  ```http
  Authorization: Bearer <JWT_TOKEN>
  ```
- **Input Validation & Request Logging:** All incoming HTTP requests are validated and logged to `stdout` with ISO timestamps for auditability and debugging.

---

## 2. General Endpoints

### 2.1 System Health Check
- **Endpoint:** `GET /health`
- **Authentication:** None (Public)
- **Description:** Verifies that the API server is online and operational.
- **Response (200 OK):**
  ```json
  {
    "status": "ok"
  }
  ```

### 2.2 Privacy Policy Page
- **Endpoint:** `GET /privacy`
- **Authentication:** None (Public)
- **Description:** Serves the official HTML Privacy Policy for Google Play Store compliance.
- **Response (200 OK):** `text/html` rendered Privacy Policy document.

---

## 3. Authentication Routes (`/auth`)

### 3.1 User Registration
- **Endpoint:** `POST /auth/register`
- **Authentication:** None (Public)
- **Description:** Registers a new user account. Hashes the password with bcrypt and issues a signed JWT token.
- **Request Body:**
  ```json
  {
    "name": "Asive Homani",
    "email": "asive@example.com",
    "password": "Password123!"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "userId": "67293a10f1e82a938c110a01",
      "name": "Asive Homani",
      "email": "asive@example.com",
      "languagePref": "en",
      "timeCredits": 0,
      "notificationsEnabled": true,
      "syncOnWifiOnly": true
    }
  }
  ```
- **Error Responses:**
  - `400 Bad Request`: Missing fields or password under 8 characters.
  - `409 Conflict`: Email address is already registered.

---

### 3.2 User Login
- **Endpoint:** `POST /auth/login`
- **Authentication:** None (Public)
- **Description:** Authenticates email and password against bcrypt hash; issues a signed JWT session token upon verification.
- **Request Body:**
  ```json
  {
    "email": "asive@example.com",
    "password": "Password123!"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "userId": "67293a10f1e82a938c110a01",
      "name": "Asive Homani",
      "email": "asive@example.com",
      "languagePref": "en",
      "timeCredits": 0,
      "notificationsEnabled": true,
      "syncOnWifiOnly": true
    }
  }
  ```
- **Error Response:**
  - `401 Unauthorized`: Invalid email or password credentials.

---

### 3.3 Google Single Sign-On (SSO)
- **Endpoint:** `POST /auth/google`
- **Authentication:** None (Public)
- **Description:** Verifies Google ID tokens issued by Firebase Auth on Android devices. Automatically creates or finds the user account.
- **Request Body:**
  ```json
  {
    "idToken": "google_firebase_id_token_string"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "userId": "67293a10f1e82a938c110a02",
      "name": "Asive Homani",
      "email": "asive.google@example.com",
      "languagePref": "en",
      "timeCredits": 0,
      "notificationsEnabled": true,
      "syncOnWifiOnly": true
    }
  }
  ```

---

### 3.4 Get Current Profile
- **Endpoint:** `GET /auth/me`
- **Authentication:** Required (`Bearer <token>`)
- **Description:** Returns the profile details for the currently logged-in user.
- **Response (200 OK):**
  ```json
  {
    "user": {
      "userId": "67293a10f1e82a938c110a01",
      "name": "Asive Homani",
      "email": "asive@example.com",
      "languagePref": "en",
      "timeCredits": 0,
      "notificationsEnabled": true,
      "syncOnWifiOnly": true
    }
  }
  ```

---

## 4. User Profile & Settings Routes (`/users`)

### 4.1 Update User Settings
- **Endpoint:** `PATCH /users/me`
- **Authentication:** Required (`Bearer <token>`)
- **Description:** Updates settings including application language preference (**en**, **zu**, **af**), notification switches, sync behavior, and Firebase Cloud Messaging (FCM) tokens.
- **Request Body (Partial updates accepted):**
  ```json
  {
    "languagePref": "zu",
    "notificationsEnabled": true,
    "syncOnWifiOnly": false,
    "fcmToken": "fcm_device_token_sample"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "user": {
      "userId": "67293a10f1e82a938c110a01",
      "name": "Asive Homani",
      "email": "asive@example.com",
      "languagePref": "zu",
      "timeCredits": 0,
      "notificationsEnabled": true,
      "syncOnWifiOnly": false
    }
  }
  ```
- **Error Response:**
  - `400 Bad Request`: `languagePref` must be one of `en`, `zu`, `af`.

---

## 5. Skill Listing Routes (`/listings`)

### 5.1 Get All Skill Listings
- **Endpoint:** `GET /listings`
- **Query Parameters:** `category` (optional)
- **Authentication:** Required (`Bearer <token>`)
- **Description:** Returns a list of available skill offerings populated with owner summaries.
- **Response (200 OK):**
  ```json
  {
    "listings": [
      {
        "_id": "67293b20f1e82a938c110b01",
        "title": "Basic Zulu Tutoring",
        "category": "Tutoring",
        "description": "One-on-one Zulu conversation practice.",
        "imageUrl": "https://storage.googleapis.com/skillswap-blobs/zulu_tutoring.jpg",
        "ownerId": {
          "_id": "67293a10f1e82a938c110a01",
          "name": "Asive Homani",
          "timeCredits": 5
        }
      }
    ]
  }
  ```

---

### 5.2 Create New Skill Listing
- **Endpoint:** `POST /listings`
- **Authentication:** Required (`Bearer <token>`)
- **Description:** Creates a new skill listing. Supports media/blob storage URLs.
- **Request Body:**
  ```json
  {
    "title": "Android App Debugging",
    "category": "Tech Help",
    "description": "Assistance with Kotlin and Android Studio setup.",
    "imageUrl": "https://storage.googleapis.com/skillswap-blobs/android_debug.png"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "listing": {
      "_id": "67293b20f1e82a938c110b02",
      "title": "Android App Debugging",
      "category": "Tech Help",
      "description": "Assistance with Kotlin and Android Studio setup.",
      "imageUrl": "https://storage.googleapis.com/skillswap-blobs/android_debug.png",
      "ownerId": "67293a10f1e82a938c110a01"
    }
  }
  ```

---

## 6. Database Schema (MongoDB Document Model)

### 6.1 User Schema (`users` Collection)
```javascript
{
  name: { type: String, required: true },
  email: { type: String, required: true, unique: true, lowercase: true },
  passwordHash: { type: String, select: false },
  ssoProvider: { type: String, enum: ['google', null], default: null },
  languagePref: { type: String, enum: ['en', 'zu', 'af'], default: 'en' },
  timeCredits: { type: Number, default: 0 },
  notificationsEnabled: { type: Boolean, default: true },
  syncOnWifiOnly: { type: Boolean, default: true },
  fcmToken: { type: String, default: null },
  createdAt: { type: Date, default: Date.now }
}
```

### 6.2 SkillListing Schema (`skilllistings` Collection)
```javascript
{
  ownerId: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  title: { type: String, required: true },
  category: { type: String, required: true },
  description: { type: String },
  imageUrl: { type: String, default: null },
  createdAt: { type: Date, default: Date.now }
}
```
