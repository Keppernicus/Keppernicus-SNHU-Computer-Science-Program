---
title: "Enhancement Two: Algorithms and Data Structures"
---

[Home](index.html) ·
[Code Review](code-review.html) ·
[Software Design & Engineering](enhancement-one-software-design.html) ·
**Algorithms & Data Structures** ·
[Databases](enhancement-three-databases.html) ·
[All Artifact Files](artifacts.html)

---

# Enhancement Two: Algorithms and Data Structures

**Artifact:** Inventorted, an Android inventory-tracking application
**Originally created:** CS360, Mobile Architecture and Programming
**Category:** Algorithms and data structures

| | |
|---|---|
| **Before** | [Browse the original source](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Original) |
| **After** | [Browse the enhanced source](https://github.com/Keppernicus/Keppernicus-SNHU-Computer-Science-Program/tree/main/CS360_Enhanced) |

---

## Summary

The previous enhancement's repository is one class that owns all database access and
was doing a full table read on every single write. Adding, updating, or deleting one item
caused it to re-read the entire inventory from disk to refresh the screen. This enhancement
replaces that pattern with an in memory index, adds search and three sort modes on top of
it, and leaves the on screen redraw unbothered.

---

## Narrative
The artifact is Inventorted, the Android inventory tracking app I built as the final project for CS360, Mobile Architecture and Programming, earlier in the program. A user logs in, adds inventory items with a name and a quantity, taps a row to edit that quantity, deletes an item, and can opt into an SMS alert that fires when an item drops to zero stock. Data lives in a local SQLite database. This is the same artifact I enhanced in the previous milestone, where I refactored it from a single do-everything Activity into MVVM with a repository, two ViewModels, and a validation class that has no Android dependencies.  

I picked Inventorted for all three categories because it is one system rather than three disconnected exercises, and this enhancement is the clearest example of why that choice paid off. The repository I built in the last milestone is where this enhancement lives. I did not have to invent a place to put an index, the previous enhancement had already created the one class in the app that owns the data, and that is exactly the class that should own an in memory copy of it. The flaw I targeted was in that repository. Every write operation ended the same, adding, updating, or deleting a single item, the repository called dbHelper.getAllItems() and re read the entire table from disk to refresh the screen. Changing one row cost a full linear read plus disk I/O, a wasteful pattern the enhancement replaces.  

The component that does the replacing is InventoryIndex. The class that wraps a HashMap keyed by item id. The repository now loads the table from the database exactly once, builds the index from that load in a single O(n) pass, and never does a full read again. After that, an add is index.put, an update is an index.get followed by an index.put, and a delete is index.remove. Those are all O(1) hash operations instead of an O(n) table scan, and none of them touch the disk to produce the refreshed list. The database is still written to on every change, so persistence is unchanged.  

The choice of a plain HashMap over a TreeMap or a LinkedHashMap is deliberate and is the trade off I would defend first. The map is used only as an index for keyed access. It is never trusted for ordering. A TreeMap would keep the keys sorted and charge O(log n) for every operation to do it, and a LinkedHashMap would maintain insertion order, but nothing in the app consumes either guarantee. Ordering is a display concern, so it is imposed at display time instead, and the unordered map is left to do the one thing it is best at.  

Imposing that ordering is the second half of the enhancement. InventorySort is an enum where each constant carries its own Comparator, alphabetical by name, quantity low to high with a name tiebreak, and a low stock view that groups out of stock items at the top and then sorts alphabetically within each group. That last one is a composite comparator, and it is a small deviation from my module one plan, which listed the quantity sort and the low stock sort as the same ordering. Those two modes would have produced identical output as written, so I made low stock a reorder list instead. It answers a different question than the quantity sort does, which is what makes three modes worth having rather than two.  

InventoryQuery is the third new class. It takes the list, filters it by a case insensitive substring match on the name, and then applies the selected comparator. Both steps run in memory against the snapshot the index produces. No search and no re-sort ever issues a database query.  

The state that drives all of this lives in InventoryViewModel, not in the repository, which keeps the separation the previous enhancement established. The repository owns the data,the ViewModel owns what the screen is currently showing, the search text and the sort mode are LiveData in the ViewModel, and a MediatorLiveData observes three sources at once, the repository list, the search text, and the sort mode, and recomputes the displayed list whenever any of them changes. InventoryActivity does not know any of this happened. It observes one list and renders it, and it gained a search field and a three option sort control to feed the ViewModel.  

One thing I did not plan to fix turned up in InventoryAdapter. Its constructor contained the line this.items = items, but the constructor has no items parameter, so it was assigning the field to itself and leaving it null. It compiled cleanly and did nothing. The list survived only because the LiveData observer usually delivered data before the RecyclerView asked for a count. I initialized the field to an empty list and removed the line. This is not the enhancement and I am not counting it as one, but it sits directly in the path this work exercises, because the search feature makes an empty display list a normal, expected state instead of an edge case. All four new classes are plain Java with no Android imports, which continues the pattern I started with InputValidator. That let me write sixteen unit tests that run on the JVM with no emulator, covering the O(1) index operations, all three sort orderings, the case insensitive filter, and null safety.  

In Module One I planned this enhancement against Outcome Three, with a supporting contribution to Outcome Four. I met Outcome Three and I would say Outcome Four is supported rather than fully executed by this work.  

Outcome Three asks for designing and evaluating computing solutions using algorithmic principles while managing trade offs. The complexity argument is replacing a repeated O(n) read plus disk access with O(1) in memory operations, at the cost of holding a second copy of the data in memory and accepting the responsibility of keeping that copy in sync. The map selection is a second trade off and the choice was not to use the more capable structure because nothing consumes what it provides. The third is that filtering and sorting run on the main thread. For an inventory a person maintains by hand that is the right call, since moving it to a background thread would add threading complexity to protect against a cost that does not exist at this scale. I would revisit that if the list ever grew into the thousands, and I would rather write down the threshold than pretend the decision is permanent.  

Outcome Four is supported through the use of standard library tooling rather than hand made equivalents. The comparators are built with Comparator.comparing and then Comparing rather than hand written compare methods, and the sort modes are an enum carrying behavior instead of an integer constant and a switch statement. MediatorLiveData is the framework component built for exactly the case of deriving one observable value from several others, and using it meant the recomputation logic is a few lines rather than a set of manually chained observers.  

I am not claiming Outcome Five here. The security work belongs to category three, where the Room migration and the credential handling are the enhancement, and claiming it early would leave that milestone with less to show. My plan for the remaining category has not changed.  

The most useful thing I learned came from a mistake I made in the middle of this milestone. I wrote InventoryIndex, wrote its unit tests, watched them pass, and moved on to the ViewModel and the Activity. The app built, the tests were green, and the search and sort both worked when I ran it. What I had not done was wire the index into the repository. Every write still ended with the full table reload it always had. The class existed and its tests passed, and none of it was running. The app worked correctly the entire time because the ViewModel recomputes from whatever list the repository posts, and it does not care whether that list came from a hash map or from a fresh table scan.  

That is a specific kind of failure I had not run into before. A passing test suite told me the data structure was correct, and correct is not the same as connected. The test proved the class did what it claimed in isolation, but nothing in it could have detected that the production code path never called it. What caught it was reading back through the repository looking for the O(1) claim I intended to make in this document and not finding it there. The lesson I am taking is that unit tests verify a unit and say nothing about integration, and that the honest check on an efficiency claim is to search the code for the pattern you say you removed.  

The second thing that cost me time was smaller and more embarrassing. My sort tests asserted specific orderings against a fixed sample of items. When I changed the names in that sample, three tests failed, and my first assumption was that the comparators were broken. They were not. The expected values in the assertions still encoded the answers for the old data. Worse, the new quantities I picked happened to make the quantity sort and the low-stock sort produce identical output, so the test asserting that those two modes differ was failing for a completely legitimate reason. The comparator was right and my test data was not exercising it. That taught me something about writing assertions that depend on sample data while burning through the night on excessive caffeine and junk food.  

I also spent time looking errors that were VERY simple. This was also embarrassing and quite frustrating. The IDE flagged what looked like a couple dozen syntax errors across one of the new classes, including unresolved references to methods in the Java standard library. I had accidentally put a semicolon where I should have put a comma. That was the entire problem with that set of errors.  

The change I deliberately did not make was DiffUtil. The adapter still calls notifyDataSetChanged, which redraws every visible row on every update. DiffUtil would compute the minimal set of changes and animate only what actually moved, and it would pair well with the sort feature, where a mode change reorders the whole list. I left it out because it is a rendering optimization rather than for data structure, and this category is about the data structure. It is on the list for later, which is the same ideal I applied to the security defects in the previous milestone.  

The thing I keep coming back to is that this enhancement is only as impressive as the previous one made it easy. The index dropped into a repository that already existed, owned all database access, and ran everything on a single threaded executor, which is why adding a mutable in memory structure introduced no new concurrency problems at all. Every index access happens on that same executor, so the ordering guarantee I built for a different reason last milestone covers the index for free. If I had attempted this against the original CS360 code, where database calls were scattered across an Activity on the main thread, there would have been no single place to put the index and no guarantee about the order changes were applied in. The architecture work was not glamorous, but it is the reason this milestone was mostly a matter of putting the right structure in an obvious place.  


---

## Complexity analysis

| Operation | Before | After | Why |
|---|---|---|---|
| Look up a single item | O(n) - No direct lookup existed, every access came from scanning the full table | O(1) - hash lookup by id against the in memory index | The repository indexes items by id in a `HashMap` instead of scanning on every access |
|
