# Firebase Setup and Seeding Guide

This guide explains how to create a Firebase project, configure Firestore, and populate it with Pokémon data directly from your terminal.

---

## Prerequisites

Ensure you have installed the Python dependencies required for the seeding script:
```bash
task firebase:setup
```

---

## Step-by-Step Setup

### 1. Log In to Firebase
To authenticate the Firebase CLI with your account, run:
```bash
task firebase:login
```

### 2. Create a Firebase Project
Create a new globally unique Firebase project. Replace `<YOUR_PROJECT_ID>` with your desired project ID (must be 6-30 characters, lowercase, digits, and hyphens):
```bash
task firebase:create:project PROJECT_ID=<YOUR_PROJECT_ID>
```
*Note: This command automatically selects and sets this new project as the active target in your `.firebaserc` config.*

### 3. Create the default Firestore Database
Firestore databases must be initialized before you can write data or deploy rules. Run the following command, specifying the location (e.g., `us-central1` or `nam5` for multi-region):
```bash
task firebase:create:db LOCATION=<LOCATION>
```

### 4. Deploy Rules & Indexes
Deploy the local security rules ([firestore.rules](firestore.rules)) and index configuration ([firestore.indexes.json](firestore.indexes.json)) to your production project:
```bash
task firebase:deploy
```

### 5. Seed the Firestore Database
Finally, seed the production database with all 151 Generation 1 Pokémon:
```bash
task firebase:seed:prod PROJECT_ID=<YOUR_PROJECT_ID>
```
*(Make sure you have run `gcloud auth application-default login` on your local terminal if the seeding script encounters GCP authorization issues connecting to Firestore). After that, run `gcloud auth application-default set-quota-project pokedex-kevinah95`.*
