# SkillSwap

**Trade skills, not cash.**

SkillSwap is a hyperlocal, peer-to-peer skills-exchange Android app. Community members list a
skill they can offer (tutoring, gardening, repairs, tech help, crafts, and more) and browse
skills offered by others nearby. Instead of paying with money, users request a swap and offer
something in return — including simply offering their time. A lightweight gamification layer
(time-credit balance and badges) rewards repeat participation without turning the app into a
payment platform.

This repository contains the full OPSC6312 Part 2 prototype: a working REST API and an Android
client that implements sign-in, settings, and the REST API connection specified in Part 1.

> Student: Asive Homani (ST10441554) · Module: OPSC6312

## Purpose & design considerations

The Research Report (Part 0) found that existing apps — TaskRabbit, Thumbtack, SweepSouth — have
no genuine peer-to-peer barter model, no offline support, and no South African multi-language
accessibility. SkillSwap's Part 1 design addressed that gap with:

- A **REST API** (Node.js/Express + MongoDB Atlas) so the data model (flexible listings, nested
  chat threads) fits a document store naturally, hosted on Render's free tier.
- **JWT-based sessions**: no screen past Login/Register is reachable without a valid, signed,
  expiring token. Passwords are salted and hashed with bcrypt server-side and are never stored
  or transmitted in plaintext.
- **Google Single Sign-On** via Firebase Auth, as a one-tap alternative that avoids storing a
  password at all for those accounts.
- **Offline-first browsing**: listings are cached locally in Room; if a live fetch fails, the
  Home screen falls back to the cache and shows a banner explaining it's offline data.
- **Push notifications** via Firebase Cloud Messaging for new requests, status changes and chat
  messages.
- **Multi-language UI**: English, isiZulu and Afrikaans string resources, switchable from
  Settings.

For the full requirements table, wireframes, API/database design, data dictionary and Gantt
chart, see the Part 1 Planning and Design document.

## Repository structure

```
skillswap/
├── backend/                  # Node.js/Express REST API + MongoDB models
│   ├── src/
│   │   ├── config/           # DB connection, Firebase Admin init
│   │   ├── middleware/       # JWT auth middleware
│   │   ├── models/           # Mongoose schemas: User, SkillListing, SwapRequest, Message
│   │   ├── routes/           # /auth, /users, /listings, /requests
│   │   └── services/         # FCM push notification helper
│   ├── tests/                # Jest + Supertest automated tests (in-memory MongoDB)
│   └── server.js
├── android/                  # Android Studio project (Kotlin, MVVM)
│   └── app/src/main/java/com/skillswap/app/
│       ├── data/api/         # Retrofit service + client
│       ├── data/local/       # Room offline cache
│       ├── data/model/       # DTOs shared with the API
│       ├── data/repository/  # AuthRepository, ListingRepository
│       └── ui/               # login / register / home / settings screens
└── .github/workflows/        # GitHub Actions CI for both projects
```

## Part 2 prototype scope

Per the assignment brief, some Part 1 features are implemented fully now and some are stubbed
with the plumbing in place, to be completed for the final PoE:

| Feature | Status in this prototype |
|---|---|
| Register & log in (email/password, bcrypt) | ✅ Fully working |
| JWT session, auto-login on relaunch | ✅ Fully working |
| Settings: language, notifications, sync preference | ✅ Fully working, persisted via API |
| REST API connected to MongoDB Atlas | ✅ Fully working |
| Browse skill listings (with category filter) | ✅ Fully working |
| Offline cache of listings (Room) | ✅ Working for reads |
| Google SSO | ⚙️ Server endpoint implemented; needs a Firebase project's config to enable on-device |
| Push notifications (FCM) | ⚙️ Token registration + receipt logging implemented; notification UI for final PoE |
| Swap requests & chat | ⚙️ API endpoints implemented; screens for final PoE |
| Multi-language strings | ✅ en / zu / af resources included |

## Getting started

### 1. Backend API

```bash
cd backend
cp .env.example .env      # then fill in MONGO_URI and JWT_SECRET
npm install
npm run dev                # starts on http://localhost:5000
```

**MongoDB Atlas setup:** create a free cluster at mongodb.com/atlas, add a database user, allow
your IP (or 0.0.0.0/0 for development), and copy the connection string from *Connect → Drivers*
into `MONGO_URI` in `.env`.

**JWT_SECRET:** generate one locally with `openssl rand -hex 32` and paste it into `.env`. Never
commit `.env` — it's already in `.gitignore`.

Run the automated test suite (spins up an in-memory MongoDB automatically, no Atlas connection
needed for tests):

```bash
npm test
```

Deploy to Render by connecting this repo, setting the root directory to `backend`, build command
`npm install`, start command `npm start`, and adding `MONGO_URI` / `JWT_SECRET` as environment
variables in the Render dashboard.

### 2. Android app

1. Open the `android/` folder in Android Studio (Koala or later).
2. If Android Studio prompts that the Gradle wrapper is missing, let it regenerate the wrapper
   (File → Sync Project with Gradle Files) — this only needs to happen once.
3. In `app/build.gradle.kts`, update `API_BASE_URL` in `defaultConfig`:
   - `http://10.0.2.2:5000/` already points at a backend running on your machine, when the app
     runs in the Android Studio emulator.
   - Change it to your Render URL (e.g. `https://skillswap-api.onrender.com/`) once deployed.
4. **Firebase setup** (required for Google SSO / push notifications, optional for the rest):
   create a Firebase project, add an Android app with package name `com.skillswap.app`, download
   `google-services.json` into `android/app/`. It's git-ignored — every developer needs their own
   copy locally.
5. Run the app on an emulator or device (Run ▶ in Android Studio).

Run the unit tests:

```bash
cd android
./gradlew testDebugUnitTest
```

## Version control workflow

This project follows the branching convention introduced in the module's Azure Repos exercise,
adapted to GitHub:

1. `main` is always deployable.
2. Feature work happens on a descriptively named branch, e.g. `feature-listings-api`,
   `feature-settings-screen`.
3. Commits are small and use meaningful messages (e.g. `Add bcrypt password hashing to
   /auth/register`, not `stuff` or `changes`).
4. A feature branch is merged into `main` via a pull request once it works and its tests pass.

## GitHub Actions (automated build & test)

Two workflows live in `.github/workflows/`:

- **`backend-tests.yml`** — on every push/PR touching `backend/`, installs dependencies and runs
  the Jest/Supertest suite against an in-memory MongoDB.
- **`android-ci.yml`** — on every push/PR touching `android/`, runs the Kotlin unit tests
  (`./gradlew testDebugUnitTest`) and then builds a debug APK, uploaded as a workflow artifact so
  it can be downloaded straight from the Actions tab.

Both give a green ✅ / red ❌ check on commits and PRs, satisfying the brief's requirement to
"make use of GitHub Actions to run tests and build your code."

## Demonstration video

_Add the link to your demo video here before submission._ The video should show: registering and
logging in with an encrypted password, changing settings, the REST API/database connection
(including what's actually stored server-side), and the Part 1 features, narrated throughout.

## Use of AI tools

_Add your ≤500-word AI-usage write-up here before submission, per the brief's requirement — for
example, which parts of this codebase or README were drafted with AI assistance, and how you
reviewed, tested and modified that output._

## References

- Firebase (2026) *Firebase Cloud Messaging Documentation*. https://firebase.google.com/docs/cloud-messaging
- MongoDB, Inc. (2026) *MongoDB Atlas Documentation*. https://www.mongodb.com/docs/atlas/
- Render Services, Inc. (2026) *Render Documentation – Deploy a Node.js App*. https://render.com/docs/deploy-node-express-app
- Independent Institute of Education (2026) *OPSC6312 Open Source Coding (Intermediate): Portfolio of Evidence*. Cape Town: The IIE.
