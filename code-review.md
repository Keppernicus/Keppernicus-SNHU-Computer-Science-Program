---
title: Code Review
---

[Home](index.html) ·
**Code Review** ·
[Software Design & Engineering](enhancement-one-software-design.html) ·
[Algorithms & Data Structures](enhancement-two-algorithms.html) ·
[Databases](enhancement-three-databases.html) ·
[All Artifact Files](artifacts.html)

---

# Code Review

This code review was recorded before any of the enhancement work began. It walks through the
original CS360 Inventorted application as it existed at the start of the capstone,
identifies the specific weaknesses I intended to address, and lays out the plan for all
three enhancement categories.

<iframe width="640" height="360"
        src="https://www.youtube.com/embed/ZiXA1sHbvfw"
        title="CS 499 code review of the Inventorted Android application"
        frameborder="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen></iframe>

**[Open the code review on YouTube](https://youtu.be/ZiXA1sHbvfw)**

---

## What the review covers

**Existing functionality.** A walkthrough of the original application: the login screen,
the inventory grid, add and delete operations, and the SMS low stock alert permission
flow, including how the pieces actually talked to each other before any refactoring.

**Code analysis.** The specific weaknesses I found, organized by category:

- **Structure** - a single Activity holding UI, business logic, and database access at once
- **Efficiency** - a full-table reload and blanket list invalidation on every data change
- **Security** - credentials stored in plaintext and a destructive database upgrade path
- **Robustness** - unguarded integer parsing that could crash the app on user input
- **Documentation** - comments that described what a line did rather than why

**Enhancement plan.** The changes I committed to for each of the three categories, and
which course outcome each one was intended to demonstrate.

---

## Where the plan changed

Three things I said on camera did not survive contact with the work, and each of the
narratives explains the reasoning in full.

**The Kotlin port became an MVVM refactor.** My original plan for the software design
category was to port the app to Kotlin. Learning the language well enough to write it
idiomatically, weighed against the time available and the modest architectural gain, made
MVVM the better use of the same hours. The same design skill at a scope I could actually
finish. Discussed in [Enhancement One](enhancement-one-software-design.html).

**The low-stock sort became a distinct ordering.** The plan listed the quantity sort and
the low stock sort as the same ordering, which would have produced identical output. I
made low-stock a composite comparator that groups out of stock items first, so the two
modes answer different questions. Discussed in
[Enhancement Two](enhancement-two-algorithms.html).

**The data migration was cut entirely.** I started building a formal version-to-version
migration with an instrumented test, then stopped: the old passwords were plaintext and
cannot be un-hashed, and the app has no install base, so the migration would have been
protecting data that does not exist. Room opens on a new database file and the old one is
left unread. Discussed in [Enhancement Three](enhancement-three-databases.html).

One planned change was deliberately *not* made. DiffUtil would replace the adapter's
full-list redraw with a minimal set of updates, but it is a rendering optimization rather
than a data-structure one, so it stayed out of scope for the algorithms category.

---

[← Back to Home](index.html)
