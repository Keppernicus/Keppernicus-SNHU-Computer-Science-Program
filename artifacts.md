---
title: All Artifact Files
---

[Home](index.html) ·
[Code Review](code-review.html) ·
[Software Design & Engineering](enhancement-one-software-design.html) ·
[Algorithms & Data Structures](enhancement-two-algorithms.html) ·
[Databases](enhancement-three-databases.html) ·
**All Artifact Files**

---

# All Artifact Files

Every file below opens in the browser with syntax highlighting. Nothing needs to be
downloaded.

---

## Original artifact

Inventorted as submitted in CS360, before any capstone enhancement.
**[Browse the tree](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Original)**

| File | Role in the original design |
|---|---|
| [DatabaseHelper.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Original/src/main/java/com/example/inventorted/DatabaseHelper.java) | Hand written `SQLiteOpenHelper`: raw SQL, plaintext passwords, destructive upgrade |
| [InventoryActivity.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Original/src/main/java/com/example/inventorted/InventoryActivity.java) | The monolith: UI, business logic, and database access in one class |
| [LoginActivity.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Original/src/main/java/com/example/inventorted/LoginActivity.java) | Authentication against plaintext credentials |
| [InventoryAdapter.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Original/src/main/java/com/example/inventorted/InventoryAdapter.java) | RecyclerView adapter using full invalidation |
| [InventoryItem.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Original/src/main/java/com/example/inventorted/InventoryItem.java) | Plain data holder |
| [build.gradle.kts](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Original/build.gradle.kts) | Original dependencies: Compose, no Room, no lifecycle components |

---

## Enhanced artifact

The same application after all three enhancements.
**[Browse the full tree →](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Enhanced)**

**Data layer**   `app/src/main/java/com/example/inventorted/data/`

| File | What it does |
|---|---|
| [InventoryRepository.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/InventoryRepository.java) | The only class permitted to touch the database |
| [InventoryIndex.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/InventoryIndex.java) | In memory `HashMap` index replacing the full table reload |
| [InventoryQuery.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/InventoryQuery.java) | Case insensitive filter plus the selected sort, applied in memory |
| [InventorySort.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/InventorySort.java) | Enum carrying a `Comparator` per sort mode |
| [AppDatabase.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/AppDatabase.java) | Room database on a new file name, so the old plaintext database is never read |
| [ItemDao.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/ItemDao.java) · [UserDao.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/UserDao.java) | Typed DAOs with compile time verified queries |
| [InventoryItem.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/InventoryItem.java) · [User.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/data/User.java) | Room entities: `User` stores only a hash for password |

**Security**   `app/src/main/java/com/example/inventorted/security/`

| File | What it does |
|---|---|
| [PasswordHasher.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/security/PasswordHasher.java) | PBKDF2-HMAC-SHA256, per-password `SecureRandom` salt, constant time verification |

**Presentation**   `app/src/main/java/com/example/inventorted/ui/`

| File | What it does |
|---|---|
| [InventoryViewModel.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/ui/InventoryViewModel.java) · [LoginViewModel.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/ui/LoginViewModel.java) | State that survives configuration changes; `MediatorLiveData` derives the displayed list from list, search text, and sort mode |
| [InventoryActivity.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/ui/InventoryActivity.java) · [LoginActivity.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/ui/LoginActivity.java) | Activities reduced to view concerns |
| [InventoryAdapter.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/ui/InventoryAdapter.java) | Full-list redraw on every change (`notifyDataSetChanged`), unchanged from the original.  DiffUtil was intentionally left out of scope |
| [UiMessage.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/ui/UiMessage.java) | User-facing messaging |

**Utilities**  `app/src/main/java/com/example/inventorted/util/`

| File | What it does |
|---|---|
| [InputValidator.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/util/InputValidator.java) · [ValidationError.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/util/ValidationError.java) | Validation at the boundary, unguarded `parseInt` returns a result instead of throwing |
| [SingleLiveEvent.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/main/java/com/example/inventorted/util/SingleLiveEvent.java) | One shot event delivery, so a rotation doesn't refire the last message |

**Tests**   none of these existed in the original

| File | What it verifies |
|---|---|
| [InventoryIndexTest.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/test/java/com/example/inventorted/InventoryIndexTest.java) | O(1) index operations and null safety |
| [InventoryQueryTest.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/test/java/com/example/inventorted/InventoryQueryTest.java) | Case-insensitive filtering |
| [InventorySortTest.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/test/java/com/example/inventorted/InventorySortTest.java) | All three sort orderings |
| [PasswordHasherTest.java](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/blob/main/CS360_Enhanced/app/src/test/java/com/example/inventorted/security/PasswordHasherTest.java) | Correct password verifies, wrong one fails, stored value never contains plaintext, same password hashes differently |

All four test classes run on the JVM with no emulator, because none of the classes they
cover import anything from Android.

---

[← Back to Home](index.html)
