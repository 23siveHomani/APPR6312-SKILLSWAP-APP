# SkillSwap Application Privacy Policy

**Effective Date:** November 3, 2025  
**Application Name:** SkillSwap  
**Developer / Student:** Asive Homani (ST10441554)  
**Contact Email:** support@skillswap.app / ST10441554@vcconnect.edu.za  

---

## 1. Overview and Scope

SkillSwap ("we", "our", or "us") is committed to protecting your personal information and respecting your privacy. SkillSwap is a hyperlocal, peer-to-peer skill exchange platform designed for Android devices. This Privacy Policy outlines how we collect, store, process, and safeguard information when you interact with the SkillSwap Android Application and its backing REST API services.

---

## 2. Information We Collect

We collect only the minimum necessary information required to operate a secure skill barter platform:

### A. Account & Registration Credentials
- **Account Details:** Full Name and Email Address.
- **Authentication Data:** Salted and hashed password credentials (processed server-side using **bcrypt** with 12 salt rounds). Plaintext passwords are never stored or logged.
- **Single Sign-On (SSO):** When signing in via Google Single Sign-On, we collect basic profile information (Email address and display name) provided by Google Firebase Authentication.

### B. App Preferences & User Settings
- **Language Preference:** Selected application language (**English**, **isiZulu**, or **Afrikaans**).
- **Notification Preferences:** Status toggles for push notifications and Wi-Fi sync settings.

### C. Skill Listings & User Content
- **Skill Offers & Requests:** Titles, categories, descriptions, time-credit values, and optional image/blob storage URLs provided when posting skill listings.

### D. Device & Technical Information
- **Push Notification Tokens:** Firebase Cloud Messaging (FCM) tokens required to route real-time status alerts and push notifications to your device.
- **Session Tokens:** JSON Web Tokens (JWT) issued upon login, stored locally on your device in AES-256 `EncryptedSharedPreferences` backed by the Android Keystore.

---

## 3. How We Use Your Information

The information collected is used strictly for the following purposes:
1. **Service Provision:** To authenticate account sessions and display community skill listings.
2. **Peer-to-Peer Communication:** To facilitate time-credit transactions and swap requests between users.
3. **Real-time Notifications:** To deliver push alerts regarding swap requests, status updates, and messages.
4. **Localization:** To present the user interface in your selected language (English, isiZulu, or Afrikaans).
5. **Security & Fraud Prevention:** To verify user identities, protect against unauthorized access, and enforce platform security.

---

## 4. Data Security and Storage Practices

We implement industry-standard technical and organizational security measures to protect your data:
- **Transport Layer Security (TLS/HTTPS):** All data transmitted between the Android mobile application and the REST API travels over encrypted HTTPS channels.
- **Server-Side Password Encryption:** Passwords are never saved in plaintext; they are salted and hashed using **bcrypt**.
- **On-Device Token Security:** JWT session tokens are stored using Android's **`EncryptedSharedPreferences`** with AES-256 GCM encryption.
- **Database Security:** Cloud database storage is hosted on **MongoDB Atlas** behind strict network firewalls and authentication barriers.

---

## 5. Third-Party Services and Data Sharing

SkillSwap does **not** sell, rent, or trade your personal information to third-party advertisers or data brokers. We utilize trusted infrastructure services solely for core application functionality:
- **Firebase (Google Cloud Platform):** Used for Google Single Sign-On authentication and Firebase Cloud Messaging (FCM) push notifications.
- **MongoDB Atlas:** Cloud NoSQL database hosting for skill listings and account profiles.
- **Render Cloud Hosting:** HTTPS API server hosting environment.

---

## 6. User Rights, Data Control, and Account Deletion

As a SkillSwap user, you hold the following rights regarding your data:
- **Access & Correction:** You can view and update your profile settings directly within the app's Settings screen.
- **Account Deletion:** You may request complete deletion of your account, listings, and associated data by logging out and submitting a deletion request to **support@skillswap.app**. Upon request, your account and listings are permanently removed from the active database.

---

## 7. Children's Privacy

SkillSwap is intended for general audiences and does not knowingly collect personal data from children under the age of 13. If we become aware that a child under 13 has provided personal data, we will immediately delete such information from our records.

---

## 8. Updates to This Privacy Policy

We may update this Privacy Policy from time to time to reflect improvements to SkillSwap features or legal compliance. Any changes will be posted at `https://skillswap-api.onrender.com/privacy` with an updated effective date.

---

## 9. Contact Information

If you have questions, concerns, or requests regarding this Privacy Policy, please contact us at:
- **Email:** support@skillswap.app / ST10441554@vcconnect.edu.za
- **Web Endpoint:** [https://skillswap-api.onrender.com/privacy](https://skillswap-api.onrender.com/privacy)
