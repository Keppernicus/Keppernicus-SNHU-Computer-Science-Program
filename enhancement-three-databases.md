---
title: "Enhancement Three: Databases"
---

[Home](index.html) ·
[Code Review](code-review.html) ·
[Software Design & Engineering](enhancement-one-software-design.html) ·
[Algorithms & Data Structures](enhancement-two-algorithms.html) ·
**Databases** ·
[All Artifact Files](artifacts.html)

---

# Enhancement Three: Databases

**Artifact:** Inventorted, an Android inventory-tracking application
**Originally created:** CS360, Mobile Architecture and Programming
**Category:** Databases

| | |
|---|---|
| **Before** | [Browse the original source](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Original) |
| **After** | [Browse the enhanced source](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Enhanced) |

---

## Summary

The data layer was the weakest part of the app: a hand-written `SQLiteOpenHelper` with
raw SQL, passwords stored and matched in plaintext, and an upgrade path that dropped
every table on a schema change. This enhancement replaces the helper with Room, replaces
plaintext credentials with salted PBKDF2 hashing verified in constant time, and removes
the destructive migration path — deliberately, without replacing it with a formal
migration either. That decision is explained below.

---

## Narrative

The artifact is Inventorted, the Android inventory tracking app I built as the final project for CS-360, Mobile Architecture and Programming. A user logs in, adds items with a name and a quantity, edits a quantity, deletes an item, and can opt into an SMS alert when an item drops to zero stock, with everything stored in a local database on the device. It is the same artifact I used for the two earlier enhancements, and this is the third and last one, the database category. Because all three enhancements are the same app rather than three separate programs, this one lands on top of the repository and the in memory index that the first two put in place.  

I selected this artifact for the database category because its data layer was the weakest and least defensible part of the whole app, which made it the best place to show database and security enhancements. three things were wrong with it. The database was a hand written SQLiteOpenHelper with raw SQL strings throughout it. The login password was stored in the users table as plaintext and checked by matching it directly in a SQL WHERE clause, so anyone who opened the database file could read every password, and the onUpgrade method dropped both tables and recreated them on any version bump, which meant a schema change would  destroy every user’s data.  

The first change was replacing the hand written helper with Room. The two tables became annotated entities, InventoryItem and User, the raw SQL became two DAO interfaces that Room implements at compile time, and a single AppDatabase class owns the schema. The repository from the first enhancement was already the only class that touched the database, so Room moved in behind it without affecting the rest of the app, and the in memory index from the second enhancement still sits unchanged. I also added a unique index on the username column, a constraint the hand written schema never had, so the database itself now refuses to create two accounts with the same name instead of trusting the code to check.  

The second change is the one that matters most for security. Passwords are no longer stored or compared in the clear. A new PasswordHasher class derives a salted hash with PBKDF2 using HMAC-SHA256, generates a fresh random salt for every password from SecureRandom, and stores the result as a single self describing string of the iteration count, the salt, and the hash. Verifying a login re derives the hash from the candidate password and the stored salt and compares it with a constant time check, so the comparison cannot leak information through timing. The password never goes into a SQL query again and the repository fetches the stored hash by username and verifies against it in memory. I chose PBKDF2 deliberately over a third party library like BCrypt because it is part of the Java standard library and has been available on Android since API 26, which is this app’s minimum SDK, so the security upgrade added no new dependencies. Because the class has no Android imports, I could unit test it on the JVM with no emulator, and those tests proved that a correct password verifies, a wrong one doesn’t, the stored value never contains the plaintext, and the same password hashed twice produces different output because the salt is random.  

The third flaw, the destructive upgrade, is where I changed my mind partway through. My first instinct was to write a formal version-to-version migration and an instrumented test proving that data survived it. I started down that road and then stopped, because it was solving a problem I didn’t have. The old passwords were plaintext and cannot be un-hashed, so there was nothing safe to carry forward, and the app has no install base, so a migration that preserved existing data would have been protecting data that does not exist. The fix was that Room opens on a new database file, the old plaintext database is left behind and never read, and I chose to not enable Room’s destructive fallback option, which would have reintroduced the drop and recreate behavior I was trying to remove. Room manages the schema safely instead.  

In module one I planned this enhancement against outcome five, the security mindset, with outcome four as the supporting one, and I met both. Outcome five is the why I did the hashing work. When I looked at the data layer with an adversarial mindset, I found plaintext passwords and a password matched in SQL. I closed both, then added the unique constraint and the constant time comparison as further hardening. Outcome four, using well founded techniques and tools, is covered by replacing hand rolled SQLite with Room, the standard persistence library for Android, and by reaching for PBKDF2 in the standard library instead of pulling in a third party crypto dependency. The one update to my module one plan is that I did not preserve legacy data. In this instance of a prerelease app  there isn’t anything worth conserving. With this enhancement the artifact is complete across all three categories. Together these all cover the program outcomes. The communication outcome through the narratives and documentation, the data structures outcome through the algorithms enhancement, the tools outcome through the design and database work, and the security outcome here.  

The thing I learned most in this enhancement was practicing restraint in my planning and scope. The mistake I nearly made was building the migration. Catching this showed me that knowing when work is unnecessary before committing too heavily is valuable. The security enhancement didn’t teach me as much as it did reinforce what I knew to be good practice. Applying the hashing correctly, using a vetted algorithm from the platform, a unique random salt, a constant-time comparison, and an encoded format let the work factor rise later without invalidating existing hashes. The challenge that cost me the most time was the tooling. Room’s newer versions parse their schema through a serialization library, and I hit a binary version conflict in the test tooling that produced a wall of errors having nothing to do with anything I had written. That was what made me question whether the migration test was worth keeping and deciding it was not made the conflict disappear along with the code that caused it. The best part across all three enhancements is that they were one system and each one made the next easier. The repository gave Room a single place to live, and the index sat untouched through a change that rewrote everything beneath it.  

---

## Security assessment

| Weakness | Before | After |
|---|---|---|
| Password storage | Stored as plaintext in the `users` table and was readable by anyone who opened the database file | Salted PBKDF2-HMAC-SHA256 hash, 120,000 iterations. Now a stolen database contains no recoverable passwords |
| Login comparison | Password matched directly inside a SQL `WHERE` clause | Hash re-derived from the candidate password and compared with `MessageDigest.isEqual`, avoiding a timing side channel |
| Salt | None - identical passwords produced identical stored values | A fresh random salt per password via `SecureRandom` means identical passwords hash to different output |
| Duplicate accounts | Enforced only in application code, if at all | Unique index on `username` at the schema level, the database itself rejects duplicates |
| Schema upgrades | `onUpgrade` dropped and recreated both tables, destroying all data on any version bump | No destructive fallback now. Room manages the schema, and the old plaintext database is left unread rather than migrated |

---

## Course outcomes demonstrated

| Outcome | Status | How this enhancement demonstrates it |
|---|---|---|
| **5.** Security mindset anticipating adversarial exploits | Met | Plaintext credential storage and SQL-based password matching identified and closed. Unique username constraint and constant time comparison added as further hardening |
| **4.** Well founded and innovative techniques, skills, and tools | Met (supporting) | Room in place of hand-rolled SQLite, PBKDF2 from the Java standard library, available since the app's minimum SDK, instead of a third-party crypto dependency |

The narrative also states a deliberate scope decision. No formal data migration was built,
since the only data at risk was unrecoverable plaintext with no install base to protect.

---

[← Enhancement Two](enhancement-two-algorithms.html) · [Back to Home](index.html)
