# Task 0 – Traditional Hashing (Modulo)

This module introduces the **baseline strategy** for key distribution:  
assigning each key to a server using `hash(key) % N`.

## Goal

Implement a naive sharder to see why modulo hashing is problematic
when the number of servers changes.

## Instructions (TODOs)

1. Implement `addServer` so it adds a new unique server ID and keeps the list sorted.
2. Implement `removeServer` so it removes the server ID if present.
3. Implement `getServerForKey` using `md5ToLong` (from [HashFunction](../../hashing/HashFunction.kt)) + modulo.  
   Return `null` when no servers are available.

## Learning outcomes

- Observe that **almost all keys are remapped** when a server is added or removed.
- Compare these results with later tasks (consistent hashing, virtual nodes, replication).

## Checklist for students

- [ ] Add and remove servers correctly.
- [ ] Confirm that keys map deterministically to servers.
- [ ] Run the provided tests in `TraditionalHashingTest` and `TraditionalHashingMetricsTest`.
- [ ] Explain why modulo hashing is *not* suitable for scalable distributed systems.

---

➡️ Proceed to Task 1 (basic consistent hashing) once all tests here are green.
