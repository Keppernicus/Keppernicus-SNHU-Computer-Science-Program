---
title: Chris Koepp's Computer Science ePortfolio
---

**Home** ·
[Code Review](code-review.html) ·
[Software Design & Engineering](enhancement-one-software-design.html) ·
[Algorithms & Data Structures](enhancement-two-algorithms.html) ·
[Databases](enhancement-three-databases.html) ·
[All Artifact Files](artifacts.html)

---

## Professional Self-Assessment

I came into this program with about a decade of professional experience as a data
scientist and analytics architect, and I finished it as a computer scientist. That is a 
succinct summary of what the degree did for me. I already knew how to find answers in a 
dataset. What I did not have was an account of *how* the systems producing those
answers should be built. Things like why one data structure beats another under load, what 
a defensible architecture looks like when someone else has to maintain it, and where the 
security assumptions hide. This portfolio is the evidence that I learned how to close that gap,
and the capstone artifact is where I closed it in public.

My professional background is in analysis for operational customers. I spent four years
as a data scientist and analytics architect, then four more leading a team of fifteen
analysts across three concurrent missions, and I deployed five times as an analytical
subject matter expert. That taught me the parts of this field that are difficult to
learn in a classroom, such as how to triage when the data is incomplete and the need is urgent,
how to pick the necessary data out of a disastrous mountain of bits and bytes, and how to recognize
that I'm wrong before it's too late. What it did not teach me was software engineering
discipline, because analytics work rewards a script that produces immediate results
and doesn't care whether it will still be correct in six months. This program
taught me how to answer the second question.

**Collaborating in a team environment.** Leading fifteen analysts meant assigning work by
individual strength rather than by availability. Some people are good at finding patterns
in messy data, others at scaling an approach, others at nuanced analysis. I had to conduct
the training and performance reviews that go with that. Most of what I learned about
collaboration, though, came from the artifacts I left passed on. I architected a migration 
to an Elasticsearch cluster that other people had to operate after me, and I built workflow 
automation that cut manual reporting time by ninety percent for analysts I would never meet. 
The capstone reinforced this at a much smaller scale. Each class I took at SNHU pushed me
to comment my code in a way that explained what it does, why it exists, and in several 
cases what I deliberately left undone and where that work is scheduled. The code review I 
recorded for this course is the same thought but in a different medium. I had to explain a 
codebase to someone who has to make a decision about it. 

**Communicating with stakeholders.** I have produced reports consumed by senior leadership
and military commanders, including a rapid-turnaround pipeline that synthesized multiple
data streams into twice daily executive reporting during a month long emergency operation.
The most useful thing I learned there is that the audience determines the artifact. I now
build dashboards in three deliberately separate flavors: operational for less technical
users, exploratory for analysts, and executive for KPIs and trends, because trying to
serve all three with one view made them useless for all three. I have also taught Kibana 
implementation classes to global customers, which is a different discipline again: 
explaining a tool to people who will use it in contexts I do not control. The written 
narratives in this portfolio were built on the same principle, and so was the decision 
to present this work as a navigable site rather than a folder of documents.

**Data structures and algorithms.** This is where the program changed how I work most
concretely. My instinct from analytics was always to reach for the query, because in a
mature search platform the engine does the optimizing for you. Coursework in data
structures forced me to reason about the cost of a choice rather than delegate it. The
algorithms enhancement in this portfolio is the direct result. I replaced a full table
reload on every write with a `HashMap` index, and I chose a plain `HashMap` over a
`TreeMap` because nothing in the application consumed the ordering guarantee
a `TreeMap` charges O(log n) per operation to provide. I make that same category of
decision professionally, denormalizing where the join cost outweighs the storage cost,
pre-computing aggregations that would otherwise be recalculated on every query, but until
this program I made it by pattern matching against experience rather than by argument.

**Software engineering and databases.** My database experience is deep but was narrow in
shape. Schema and index design for Elasticsearch, ETL pipelines, and the modeling tradeoffs
for speed. In this program I worked across MongoDB in a full stack context, relational design, 
and in the capstone the migration of a hand written SQLite layer to Room with typed, 
compile time verified DAOs. The software engineering side was the bigger change. The first 
enhancement in this portfolio decomposes a single do everything Activity into an architecture 
with a repository, ViewModels, and a validation class carrying no framework dependencies, and the
most valuable thing I took from it was not the pattern but a realization about why
the original had no tests. The structure I made caused expensive testing. I had always treated
writing tests and structuring code as separate jobs. They are the same job.

**Security.** I have worked my entire career in environments where data sensitivity is the
default assumption, so the mindset was not new, but applying it as a builder rather than a
consumer was. The databases enhancement is where I demonstrate that. Looking at my own artifact
adversarially, I found credentials stored in plaintext and matched inside a SQL `WHERE`
clause, and an upgrade path that would destroy user data on any schema change. I replaced
the credential handling with salted PBKDF2 hashing verified in constant time, chose the
platform's own implementation over a third-party dependency, and removed the destructive
migration without reintroducing it through a fallback option. Equally important was what I
did not build. I started a formal data migration and abandoned it once I recognized it
would have been protecting data that does not exist and cannot be recovered anyway.

**How the artifacts fit together.** The three enhancements that follow are all the same
application, Inventorted, an Android inventory tracking app I originally built in CS360,
enhanced in sequence rather than three separate programs enhanced in parallel. That was an 
intentional choice, and it is the thing I would point to first. Each enhancement had to
function with the next one. The repository built for the software design enhancement became the
only sensible place to put the in memory index in the algorithms enhancement, and it became
the single seam Room could work behind for the databases enhancement without disturbing
anything above it. Since every database call already ran on one single-threaded executor,
adding a mutable in-memory structure introduced no new concurrency problems. That is
the point this portfolio is making, I chose not that I can perform three techniques, but 
instead made architectural decisions compound on each other, and that the  work of putting 
a boundary in the right place made everything after it cheaper. Read in order of software design,
then algorithms, then databases, the three narratives are a record of a system getting
progressively easier to change.

---

## The Artifact

All three enhancements were made to a single artifact: **Inventorted**, an Android
inventory tracking application I originally built in CS360, Mobile Architecture and
Programming. I chose to use one artifact across all three categories. This way, rather 
than three isolated enhancements, a single codebase went from prototype to something
that is stronger, more efficient, and secure.

The original is five Java classes in a single flat package. The enhanced version is 
nineteen classes across four packages —`data`, `ui`, `security`, and `util` — plus 
four unit test classes that did not exist before.

| | Original | Enhanced |
|---|---|---|
| **Browse** | [CS360_Original](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Original) | [CS360_Enhanced](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Enhanced) |
| **Java classes** | 5 | 19 |
| **Unit tests** | 0 | 4 |
| **Persistence** | Hand written `SQLiteOpenHelper` | Room with typed DAOs |
| **Credentials** | Stored in plaintext | Salted PBKDF2 hash |

---

## Enhancements

### [One: Software Design and Engineering](enhancement-one-software-design.html)
Refactored a monolithic Activity into MVVM with a repository layer, moved database work
off the main thread, and hardened input validation.

### [Two: Algorithms and Data Structures](enhancement-two-algorithms.html)
Replaced a full table reload on every change with an in memory index, added search and
three sort modes, and deliberately left the adapter's full redraw in place as out of scope.

### [Three: Databases](enhancement-three-databases.html)
Migrated to Room, replaced plaintext credential storage with salted PBKDF2 hashing, and
removed the destructive upgrade path.

---

## Code Review

Before writing any enhancement, I recorded a walkthrough of the original codebase, what
it did, where it was weak, and what I planned to change and why.

**[Watch the code review](code-review.html)**

---

## Course Outcome Map

This portfolio is assessed against five course outcomes. 

| Course Outcome | Evidence |  
|---|---|
| **1.** Collaborative environments enabling diverse audiences to support decision making | [Code Review](code-review.html); intent explaining comments throughout the enhanced source; the stakeholder framing in each narrative |
| **2.** Professional quality oral, written, and visual communication | [Code Review](code-review.html) video; this site's navigation and structure; the three narratives |
| **3.** Design and evaluate computing solutions using algorithmic principles, managing trade-offs | [Enhancement Two](enhancement-two-algorithms.html) indexing strategy with Big-O analysis and documented trade offs |
| **4.** Well founded and innovative techniques, skills, and tools | [Enhancement One](enhancement-one-software-design.html) - MVVM, repository pattern, lifecycle aware components; [Enhancement Three](enhancement-three-databases.html) - Room, plus the four unit test classes demonstrating iterative testing |
| **5.** Security mindset anticipating adversarial exploits | [Enhancement Three](enhancement-three-databases.html) — PBKDF2 with per-user salt, constant-time comparison, no destructive fallback; input validation in [Enhancement One](enhancement-one-software-design.html) |

---

<sub>Chris Koepp · Southern New Hampshire University · B.S. Computer Science ·
[GitHub](https://github.com/Keppernicus)</sub>
