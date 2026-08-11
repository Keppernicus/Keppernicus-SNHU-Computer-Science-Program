---
title: "Enhancement One: Software Design and Engineering"
---

[Home](index.html) ·
[Code Review](code-review.html) ·
**Software Design & Engineering** ·
[Algorithms & Data Structures](enhancement-two-algorithms.html) ·
[Databases](enhancement-three-databases.html) ·
[All Artifact Files](artifacts.html)

---

# Enhancement One: Software Design and Engineering

**Artifact:** Inventorted, an Android inventory-tracking application
**Originally created:** CS360, Mobile Architecture and Programming
**Category:** Software design and engineering

| | |
|---|---|
| **Before** | [Browse the original source](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Original) |
| **After** | [Browse the enhanced source](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Enhanced) |

---

## Summary

The original application concentrated UI rendering, business logic, and direct database
access inside a single Activity. This enhancement decomposed it into an MVVM architecture:

- A **repository** became the only class permitted to touch the database, giving the data
  layer a single entry point.
- All database work moved **off the main thread**, so the UI no longer blocks on I/O.
- **ViewModels** now hold state that survives configuration changes, so rotating the device
  no longer discards in flight work.
- **Input validation** was hardened, closing two unguarded `parseInt` calls that could
  crash the application on unexpected user input.

---

## Narrative

The artifact is an Android inventory-tracking app I named Inventorted. I built it as the final project for CS-360, Mobile Architecture and Programming, earlier in the program. It is a small app in scope but it does a lot. A user creates an account and logs in, adds inventory items with a name and a quantity, taps a row to edit a quantity, deletes an item with a trash icon, and can opt into an SMS alert that fires off when an item drops to zero stock. Data lives in a local SQLite database, so it survives closing the app. When I submitted it for CS-360 it met every requirement and I was happy with it.  

I picked Inventorted because it is one artifact that can handle all three enhancement categories as a single system instead of three random exercises. It has an architecture to redesign for category one, a full-table reload to replace with a real data structure for category two, and a hand-written SQLite layer to migrate to Room for category three, and those three things touch each other. The repository I built for this enhancement is what the Room DAO will sit behind later, and the in-memory index I have planned will live inside that same repository. Choosing one artifact made me reason about the app as a whole rather than patch three disconnected things.  

The more honest reason is that it was flawed in ways I did not see when I first submitted it, and an artifact I could only polish would not show much. My Module Two code review found that InventoryActivity was doing everything at once. It was the UI controller, the business logic, and the database access all in the same class, and it called the database directly on the main UI thread in four different places. There was no separation of concerns so none of that logic could be tested without the emulator, because every method was tied to the Activity life cycle. Rotating the screen destroyed the Activity and re-queried the entire table. Two calls to Integer.parseInt were completely unguarded, so entering a number larger than an int could hold crashed the app outright, which I mentioned on camera during the review. The one try catch in the whole app was a bare catch that swallowed everything and logged nothing.  

The enhancement was a refactor to the MVVM pattern, and the components that show the work best are the ones that did not exist before.   

InventoryRepository is now the only class in the entire app that holds a database reference and I checked this at the end. A search for the database helper across the project returns only the repository. Every database call runs on a single threaded background executor, and results come back either through LiveData or through a callback the repository posts to the main thread so the caller never has to think about threading. The executor is single threaded on purpose, so a write and the reload that follows it stay in order and the screen can never show a list older than the change that was just made.

InventoryViewModel and LoginViewModel own the state of their screens and hold the logic that used to sit in the Activities: validating input, deciding whether an operation should go ahead, and deciding what the user gets told. Because a ViewModel survives a screen rotation, the inventory list loads once in the ViewModel constructor instead of once per Activity creation. That single fact is the rotation-bug fix. On rotation the Activity is rebuilt, re-subscribes, and immediately gets the list it already had.  

InputValidator is a small class with no Android dependencies that guards every parse. It is where the crash I talked about on camera now lives as a returned result instead of an exception. Because it has no framework ties, it is easier to be tested as plain Java which the original design made impossible.  

The two Activities are what is left over, and that is the point of the pattern. InventoryActivity dropped from 267 lines to a View that inflates layouts, shows dialogues and Toasts, handles permissions, and sends the SMS in 233 lines. It holds no list, does no validation, and does not know a database exists. LoginActivity got similar adjustments but on a smaller scale. Leaving login on the old path would have made the whole claim of this enhancement untrue to anyone reading the code, so login goes through the repository too.  

In module one I planned this enhancement against outcomes three and four, with a supporting contribution to outcome one. I met all three, and the work came out close to what I projected.  

Outcome four (well-founded and innovative techniques, skills, and tools) is the strongest one here. ViewModel and LiveData are the current standard AndroidX components, not something I invented for the assignment. Leaning on the framework’s own lifecycle aware pieces is exactly what let the rotation fix be one line in a constructor instead of a pile of manual state-saving code.  

Outcome three (design and evaluate computing solutions while managing the trade-offs in design choices) is met by the architecture and by one trade off in particular. My original idea was to port the app to Kotlin. I dropped that on purpose as the design decision, not a retreat from one. Learning Kotlin well enough to write it idiomatically, weighed against the time I had and the small architectural gain, made MVVM the better use of the same hours. I had also already implemented MVVM once at work after some guidance from a senior engineer, so I knew the pattern would demonstrate the same design skill at a scope I could actually finish.

Outcome One (building collaborative environments) is supported but not fully met. Every new class carries a comment block that says what it does, why it exists, and, in a few cases, what I deliberately left undone and where that work is scheduled. The point is that whoever picks this up after me, including me in a later milestone, can reconstruct the reasoning without me in the room.  

I do not have real updates to my outcome coverage plans. One thing worth not is that I hardened input validation as part of this work, which serves outcome five, but I am not claiming outcome five here. The security work that for it is category three, and I would rather claim that outcome once later in the project. Categories two and three still point at Outcome Three and at Outcomes Four and Five respectively.  

The thing I did not expect to learn is that the architecture was about testability. I went in thinking of this as an architecture exercise. What I learned is that the reason the original app had no real tests needed was the design itself. I had honestly forgotten why I hadn’t made tests (an attribute of poor documentation as well). The validation logic lived inside an activity, so reaching it needed an emulator, an layout, and a tap. Pulling the logic into a plain class with no framework ties is what makes it testable. I had always treated writing tests and structuring code as separate jobs and this taught me otherwise, and showed me that bad structure made the tests too expensive in the first place.  

The hardest part was scope. The code review turned up defects that are not in my approved plan for this category like plaintext passwords, a destructive database upgrade that wipes data, and an error message on account creation that tells a potential attacker whether a username exists. I chose to leave some for later enhancements in the class. Some belong to category three, where fixing them is the enhancement, and doing it early would leave that category with nothing to show. Others were never approved as scope, so I left them for later.  

The technical problem I did not expect was LiveData’s behavior. LiveData replays its current value to any new observer, which is right for the item list and wrong for a one time message like a toast. My first version put user messages in ordinary LiveData, and every screen rotation refired the last message, because the rebuilt Activity was a new observer subscribing to a value that had never gone away. The bug forced me to name a distinction between durable state that should survive a rotation, like the list, and one-time events that should not, like a toast or the navigation to the next screen.  

The smaller thing I keep coming back to is the SMS alert. In the original, the zero-stock alert fired from the dialogue’s Save button the instant the user typed a zero. In the refactor it fires from the repository’s success callback, after the write actually lands. I did not plan that change; it fell out of having a callback to hang it on. But it fixed a bug I had never noticed, because in the old version a database write that failed could still send a customer an out-of-stock alert for an item that was not actually out of stock. Good structure showed me a real defect by making the wrong version of the code awkward.  

That is the case for this enhancement in one line. The part of my CS-360 work I was most confident in turned out to be problematic while the difference between what I thought I did well versus the reality taught me the most.  

---

## Course outcomes demonstrated

| Outcome | How this enhancement demonstrates it |
|---|---|
| **4.** Well-founded and innovative techniques, skills, and tools | MVVM, the repository pattern, and lifecycle-aware components are the current industry-standard architecture for Android; adopting them replaced an ad-hoc structure with a recognized one |
| **3.** Design and evaluate computing solutions, managing trade-offs | Dropped the planned Kotlin port in favor of MVVM. The same design skill at a scope was achievable in the time available
| **1.** Collaborative environments | In-code comments were rewritten to explain intent and decisions rather than restating syntax, so another developer can pick up the reasoning |

---

[← Back to Home](index.html) · [Next: Enhancement Two →](enhancement-two-algorithms.html)
