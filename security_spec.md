# Security Specification — My Notes

## 1. Data Invariants
1. **Strict Multi-User Isolation**: Every note (`/users/{userId}/notes/{noteId}`) and folder (`/users/{userId}/folders/{folderId}`) belongs exclusively to `userId`. Only an authenticated user with `request.auth.uid == userId` may read, list, create, update, or delete records under their path.
2. **Identity Integrity**: The `userId` field inside every `Note` and `Folder` document must equal both the path variable `{userId}` and `request.auth.uid`.
3. **Document ID Integrity**: The `id` field inside every `Note` and `Folder` document must match the path variable (`{noteId}` or `{folderId}`) and conform to `isValidId()`.
4. **Immutability of Creation Metadata**: On `update`, `id`, `userId`, and `createdAt` must remain strictly equal to `resource.data`.
5. **Temporal Integrity**: `createdAt` and `updatedAt` must be valid Firestore `timestamp` objects with `<= request.time`.
6. **Bounded Strings & Collections**: All strings enforce strict maximum character lengths (`title <= 300`, `content <= 50000`, `voiceTranscript <= 10000`, `name <= 80`) and all lists (`tags <= 10`, `checklistItems <= 50`) enforce maximum element counts and element type checks.
7. **Mandatory List Filter Alignment**: `allow list` rules explicitly verify `resource.data.userId == request.auth.uid`.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Read/Write**: Request with `request.auth == null` attempting to read or create a note.
2. **Cross-User Read (PII/Thought Leak)**: Authenticated `alice_123` attempting `get` or `list` on `/users/bob_456/notes`.
3. **Identity Spoofing on Create**: Authenticated `alice_123` creating `/users/alice_123/notes/note_1` with `{"userId": "bob_456"}`.
4. **Shadow Update (Ghost Field Injection)**: Authenticated owner updating a note with an undeclared field `{"isAdmin": true}` or `{"isVerified": true}`.
5. **Immortal Field Tampering**: Authenticated owner updating `createdAt` or `userId` or `id` on an existing note.
6. **Value Poisoning (Type Mismatch)**: Updating `title` with a boolean `true` or integer `999` instead of a bounded string.
7. **Denial of Wallet (Oversized String)**: Creating a note with a 60,000-character `content` or 500-character `title`.
8. **Unbounded Array Injection**: Creating a note with 15 `tags` (exceeding max 10) or 60 `checklistItems` (exceeding max 50).
9. **Path ID Poisoning**: Creating a document with an invalid ID containing spaces or special characters.
10. **Future Timestamp Spoofing**: Submitting `createdAt` or `updatedAt` set to a future timestamp (`> request.time`).
11. **Explicit Null Injection on Required Field**: Sending `{"title": null}` on update.
12. **Unfiltered List Query Scraping**: Querying `/users/{userId}/notes` without matching `userId` ownership.

## 3. Red Team Audit Report
- **Shadow Update Test**: PASS — `incoming().keys().hasOnly(...)` on create and `incoming().diff(existing()).affectedKeys().hasOnly(...)` on update block all ghost fields.
- **PII / Thought Blanket Test**: PASS — `isOwner(userId)` restricts all reads and writes to `request.auth.uid == userId`.
- **Query Trust Test**: PASS — `allow list` checks `isOwner(userId) && existing().userId == request.auth.uid`.
- **Value Poisoning Test**: PASS — `isValidNote(incoming(), userId)` and `isValidFolder(incoming(), userId)` wrap the entire `allow update` block.
