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

****LATER.


## Use of AI tools

For Part 3 of my SkillSwap Android app, I used AI tools as a support aid, not as a replacement for my own work. I designed the app, built the features and made the final decisions myself. AI helped me understand requirements, set up automation and solve problems faster.

### Tools used

- **Gemini in Android Studio (agent mode):** used inside my project to read files and suggest changes.
- **Claude (Anthropic):** used in chat to extract text and to help me troubleshoot.
- **Google AI Studio:** used to create an API key after I reached the hourly limit.

### Where and how I used AI

- **Understanding the brief.** I gave Claude screenshots of the Part 3 brief and asked it to extract the text page by page. I used this to build a checklist of requirements: register and log in, single sign-on, settings, REST API, offline sync, push notifications, multi-language support, README, GitHub Actions and publishing evidence.
- **GitHub Actions and automated builds.** I used the Gemini agent to read my project, including the .github folder and my android-ci.yml file, and to help check the workflow that builds the app and runs tests. I compared it with the build.yml example provided in the brief.
- **README and documentation.** Gemini helped me look through my README and think about its structure: the purpose of the app, design considerations, use of GitHub and GitHub Actions, and release notes.
- **Troubleshooting tools.** When Android Studio showed that I had exceeded the prompt request limit, I used Claude to understand the message, find the Model Providers settings and set up my own API key from Google AI Studio so I could continue working.

### How it helped me

AI saved me time on repetitive and unfamiliar tasks, especially continuous integration, which I had not used before. It explained errors in plain language, which helped me learn instead of just copying fixes. Extracting the brief into a clear checklist also made sure I did not miss any requirement, such as the PoE-only features.

### Checking and taking responsibility

I did not accept AI output blindly. In Android Studio I reviewed the suggested edits and used Keep All or Revert All to decide what stayed in my project. I built and ran the app on a physical phone, checked that the GitHub Actions build worked, and added my own comments and logging so that I understand my code. I kept my API key private and did not place it in my source code or on GitHub.

### Limitations

The free tier of Gemini has strict hourly limits, so I had to break my requests into smaller prompts. AI suggestions also needed testing and sometimes correction, so I treated them as a starting point only.

### Conclusion

AI tools helped me work more efficiently and understand new concepts, but all features, testing and final decisions in SkillSwap are my own work.
## References

- Firebase (2026) *Firebase Cloud Messaging Documentation*. https://firebase.google.com/docs/cloud-messaging
- MongoDB, Inc. (2026) *MongoDB Atlas Documentation*. https://www.mongodb.com/docs/atlas/
- Render Services, Inc. (2026) *Render Documentation – Deploy a Node.js App*. https://render.com/docs/deploy-node-express-app
- Independent Institute of Education (2026) *OPSC6312 Open Source Coding (Intermediate): Portfolio of Evidence*. Cape Town: The IIE.
