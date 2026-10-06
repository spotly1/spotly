const { before, after, beforeEach, test } = require("node:test");
const { readFileSync } = require("node:fs");
const { initializeTestEnvironment, assertSucceeds, assertFails } = require("@firebase/rules-unit-testing");
const { doc, setDoc, getDoc, getDocs, collection, updateDoc, deleteDoc, writeBatch, runTransaction, serverTimestamp, query, limit, orderBy } = require("firebase/firestore");
let env;
test("ubicación opcional: acepta GeoPoint y publicaciones viejas; rechaza otro formato", async () => {
  await seed();
  const { GeoPoint } = require("firebase/firestore");
  await assertSucceeds(setDoc(doc(dbFor(), "posts/located"), { ...post(), location: new GeoPoint(-34.6037, -58.3816) }));
  await assertSucceeds(setDoc(doc(dbFor(), "posts/no-location"), { ...post(), location: null }));
  await assertSucceeds(setDoc(doc(dbFor(), "posts/older-schema"), post()));
  await assertFails(setDoc(doc(dbFor(), "posts/invalid-location"), { ...post(), location: "Buenos Aires" }));
});
test("perfil: todas las publicaciones propias, sin mezclar autores ni limitar a 50", async () => {
  await env.withSecurityRulesDisabled(async c => {
    const batch = writeBatch(c.firestore());
    for (let i = 0; i < 51; i++) batch.set(doc(c.firestore(), `posts/alice-${i}`), post());
    batch.set(doc(c.firestore(), "posts/bob"), { ...post(), authorId: "bob" });
    await batch.commit();
  });
  const { where } = require("firebase/firestore");
  const own = query(collection(dbFor(), "posts"), where("authorId", "==", "alice"));
  const result = await assertSucceeds(getDocs(own));
  require("node:assert/strict").equal(result.size, 51);
  await assertFails(getDocs(query(collection(dbFor(), "posts"), where("authorId", "==", "bob"))));
  await assertFails(getDocs(collection(dbFor(), "posts")));
});
const post = () => ({ authorId: "alice", username: "alice", description: "Un mural",
  imageUrl: "https://res.cloudinary.com/iufz7yvd/image/upload/spotly/posts/alice/photo.jpg",
  imagePublicId: "spotly/posts/alice/photo", createdAt: serverTimestamp() });
test("publicar y leer feed limitado; reintento con mismo ID no duplica", async () => {
  await seed();
  const db = dbFor();
  const ref = doc(db, "posts/first");
  for (let attempt = 0; attempt < 2; attempt++) {
    await assertSucceeds(runTransaction(db, async tx => {
      if (!(await tx.get(ref)).exists()) tx.set(ref, post());
    }));
  }
  const feed = query(collection(dbFor("bob"), "posts"), orderBy("createdAt", "desc"), limit(50));
  const result = await assertSucceeds(getDocs(feed));
  require("node:assert/strict").equal(result.size, 1);
  await assertFails(getDocs(collection(db, "posts")));
  await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(), "posts/first")));
  await assertFails(updateDoc(ref, { description: "Editada" }));
  await assertFails(deleteDoc(ref));
});
test("publicaciones rechazan suplantación, imágenes ajenas y datos inválidos", async () => {
  await seed();
  for (const change of [
    { authorId: "bob" }, { username: "bob" }, { description: "" },
    { description: "a".repeat(1001) }, { createdAt: new Date(0) },
    { imageUrl: "https://example.com/photo.jpg" },
    { imagePublicId: "spotly/posts/bob/photo" }, { email: "private@example.com" }
  ]) {
    await assertFails(setDoc(doc(dbFor(), "posts/invalid"), { ...post(), ...change }));
  }
  await assertFails(setDoc(doc(env.unauthenticatedContext().firestore(), "posts/anonymous"), post()));
});
const profile = () => ({ username: "alice", description: "", profileImageUrl: "", profileImagePublicId: "" });
const dbFor = (uid = "alice") => env.authenticatedContext(uid, { email: uid + "@example.com" }).firestore();
const ref = db => doc(db, "users/alice");
const nameRef = db => doc(db, "usernames/alice");
const privateRef = db => doc(db, "privateUsers/alice");
async function seed(data = profile()) {
  await env.withSecurityRulesDisabled(async c => {
    await setDoc(ref(c.firestore()), data);
    await setDoc(nameRef(c.firestore()), { uid: "alice" });
    await setDoc(privateRef(c.firestore()), { email: "alice@example.com" });
  });
}
before(async () => {
  if (!process.env.FIRESTORE_EMULATOR_HOST) throw new Error("Solo ejecutar con el emulador.");
  env = await initializeTestEnvironment({
    projectId: "demo-spotly-rules",
    firestore: { rules: readFileSync("../../firestore.rules", "utf8") }
  });
});
beforeEach(async () => env.clearFirestore());
after(async () => env?.cleanup());
test("registro: transacción igual a Android", async () => {
  const db = dbFor();
  await assertSucceeds(runTransaction(db, async tx => {
    await tx.get(nameRef(db));
    tx.set(nameRef(db), { uid: "alice" });
    tx.set(ref(db), profile());
    tx.set(privateRef(db), { email: "alice@example.com" });
  }));
});
test("perfil público para autenticados; email solo para su dueño", async () => {
  await seed();
  await assertSucceeds(getDoc(ref(dbFor())));
  await assertSucceeds(getDoc(ref(dbFor("bob"))));
  await assertSucceeds(getDoc(privateRef(dbFor())));
  await assertFails(getDoc(privateRef(dbFor("bob"))));
  await assertFails(getDoc(privateRef(env.unauthenticatedContext().firestore())));
  await assertFails(getDocs(collection(dbFor(), "privateUsers")));
  await assertFails(getDoc(ref(env.unauthenticatedContext().firestore())));
  await assertFails(getDocs(collection(dbFor(), "users")));
});
test("perfil y reserva deben crearse juntos", async () => {
  await assertFails(setDoc(ref(dbFor()), profile()));
  await assertFails(setDoc(nameRef(dbFor()), { uid: "alice" }));
});
test("registro rechaza email ajeno, campos extra y nombres inconsistentes", async () => {
  for (const changes of [{ email: "bob@example.com" }, { admin: true }, { username: "otro" }]) {
    const db = dbFor();
    const batch = writeBatch(db);
    batch.set(ref(db), { ...profile(), ...changes });
    batch.set(nameRef(db), { uid: "alice" });
    batch.set(privateRef(db), { email: "alice@example.com" });
    await assertFails(batch.commit());
  }
});
test("no reservar nombres extra, robar, modificar o liberar reservas", async () => {
  await seed();
  await assertFails(setDoc(doc(dbFor(), "usernames/extra"), { uid: "alice" }));
  await assertFails(setDoc(nameRef(dbFor("bob")), { uid: "bob" }));
  await assertFails(deleteDoc(nameRef(dbFor())));
  await assertFails(deleteDoc(ref(dbFor())));
  await assertFails(getDocs(collection(dbFor(), "usernames")));
});
test("registro no puede reservar dos nombres en la misma transacción", async () => {
  const db = dbFor();
  const batch = writeBatch(db);
  batch.set(ref(db), profile());
  batch.set(nameRef(db), { uid: "alice" });
  batch.set(privateRef(db), { email: "alice@example.com" });
  batch.set(doc(db, "usernames/extra"), { uid: "alice" });
  await assertFails(batch.commit());
});


test("reemplazar y quitar foto; rechazar ID ajeno y URL externa", async () => {
  await seed();
  const r = ref(dbFor());
  const url = "https://res.cloudinary.com/iufz7yvd/image/upload/x";
  await assertFails(updateDoc(r, { profileImageUrl: "https://example.com/x", profileImagePublicId: "spotly/profiles/alice/x" }));
  await assertFails(updateDoc(r, { profileImageUrl: url, profileImagePublicId: "spotly/profiles/bob/x" }));
  await assertSucceeds(updateDoc(r, { profileImageUrl: url, profileImagePublicId: "spotly/profiles/alice/x" }));
  await assertSucceeds(updateDoc(r, { profileImageUrl: "", profileImagePublicId: "" }));
});
test("bloquear identidad, campos inválidos, ediciones ajenas y otras colecciones", async () => {
  await seed();
  for (const data of [{ username: "otro" }, { email: "otro@example.com" }, { admin: true }, { description: 42 }, { description: "x".repeat(301) }]) {
    await assertFails(updateDoc(ref(dbFor()), data));
  }
  await assertFails(updateDoc(ref(dbFor("bob")), { description: "intruso" }));
  await assertFails(setDoc(doc(dbFor(), "posts/post1"), { text: "hola" }));
  await assertFails(getDoc(doc(dbFor(), "private/data")));
});
test("registro exige documento privado y email del usuario autenticado", async () => {
  for (const email of [undefined, "bob@example.com"]) {
    const db = dbFor();
    const batch = writeBatch(db);
    batch.set(ref(db), profile());
    batch.set(nameRef(db), { uid: "alice" });
    if (email !== undefined) batch.set(privateRef(db), { email });
    await assertFails(batch.commit());
  }
  await assertFails(setDoc(privateRef(dbFor()), { email: "alice@example.com" }));
});

test("bloquea cambios, borrados y escrituras ajenas en datos privados", async () => {
  await seed();
  await assertFails(updateDoc(privateRef(dbFor()), { email: "otro@example.com" }));
  await assertFails(deleteDoc(privateRef(dbFor())));
  await assertFails(setDoc(privateRef(dbFor("bob")), { email: "bob@example.com" }));
});



test("exige URL y publicId juntos y conserva una foto válida al editar descripción", async () => {
  await seed();
  const r = ref(dbFor());
  const url = "https://res.cloudinary.com/iufz7yvd/image/upload/x";
  await assertFails(updateDoc(r, { profileImageUrl: url }));
  await assertFails(updateDoc(r, { profileImagePublicId: "spotly/profiles/alice/x" }));
  await assertSucceeds(updateDoc(r, { profileImageUrl: url, profileImagePublicId: "spotly/profiles/alice/x" }));
  await assertSucceeds(updateDoc(r, { description: "Nueva descripción" }));
  await assertFails(updateDoc(r, { profileImagePublicId: "" }));
});
