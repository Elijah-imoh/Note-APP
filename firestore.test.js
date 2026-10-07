const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validNotePayload(userId = ALICE_UID, noteId = "note_1") {
  const now = new Date();
  return {
    id: noteId,
    userId: userId,
    title: "Design System Reflections",
    content: "Calm typography and intentional spacing reduce cognitive load.",
    folderId: "folder_design",
    tags: ["design", "ux"],
    checklistItems: ["[x] Study typography hierarchy", "[ ] Refine dark mode"],
    isPinned: true,
    isArchived: false,
    isTrashed: false,
    isLocked: false,
    colorKey: "warm_sand",
    voiceTranscript: "",
    voiceDurationSec: 0,
    reminderAtMillis: 0,
    wordCount: 9,
    createdAt: now,
    updatedAt: now,
  };
}

function validFolderPayload(userId = ALICE_UID, folderId = "folder_design") {
  const now = new Date();
  return {
    id: folderId,
    userId: userId,
    name: "Design Research",
    iconKey: "book",
    accentKey: "terracotta",
    sortOrder: 1,
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("1. Unauthenticated user cannot read or create notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("notes").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("notes")
      .doc("note_1")
      .set(validNotePayload())
  );
});

test("2. Authenticated owner can create, read, list, update, and delete their own note", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("notes")
    .doc("note_1");

  await assertSucceeds(noteRef.set(validNotePayload(ALICE_UID, "note_1")));
  await assertSucceeds(noteRef.get());
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("notes")
      .where("userId", "==", ALICE_UID)
      .get()
  );
  await assertSucceeds(
    noteRef.update({
      title: "Updated Reflections",
      isPinned: false,
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(noteRef.delete());
});

test("3. Cross-user isolation: Alice cannot read, list, update, or delete Bob's notes", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("notes")
      .doc("bob_note")
      .set(validNotePayload(BOB_UID, "bob_note"));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobNoteRef = aliceDb
    .collection("users")
    .doc(BOB_UID)
    .collection("notes")
    .doc("bob_note");

  await assertFails(bobNoteRef.get());
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("notes")
      .where("userId", "==", BOB_UID)
      .get()
  );
  await assertFails(bobNoteRef.update({ title: "Hacked" }));
  await assertFails(bobNoteRef.delete());
});

test("4. Shadow update (ghost field) and immutable field tampering are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const noteRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("notes")
    .doc("note_1");
  await assertSucceeds(noteRef.set(validNotePayload(ALICE_UID, "note_1")));

  // Ghost field injection
  await assertFails(
    noteRef.update({
      isVerified: true,
      updatedAt: new Date(),
    })
  );

  // Immutable userId tampering
  await assertFails(
    noteRef.update({
      userId: BOB_UID,
      updatedAt: new Date(),
    })
  );
});

test("5. Value poisoning and oversized payloads are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badNoteRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("notes")
    .doc("note_bad");

  const tooManyTags = {
    ...validNotePayload(ALICE_UID, "note_bad"),
    tags: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"],
  };
  await assertFails(badNoteRef.set(tooManyTags));
});

test("6. Authenticated owner can manage folders and cross-user access is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const folderRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("folders")
    .doc("folder_design");

  await assertSucceeds(
    folderRef.set(validFolderPayload(ALICE_UID, "folder_design"))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("folders")
      .where("userId", "==", ALICE_UID)
      .get()
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("folders")
      .doc("folder_design")
      .get()
  );
});
