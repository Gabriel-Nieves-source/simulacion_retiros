// Guarda la app en el celular para que abra aunque no haya internet.
const CACHE = "cajero-umb-v1";
const ARCHIVOS = ["./", "./index.html", "./manifest.webmanifest", "./icon-192.png", "./icon-512.png"];
self.addEventListener("install", e => e.waitUntil(caches.open(CACHE).then(c => c.addAll(ARCHIVOS))));
self.addEventListener("activate", e => e.waitUntil(
  caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k))))));
self.addEventListener("fetch", e => e.respondWith(
  caches.match(e.request).then(r => r || fetch(e.request).then(res => {
    const copia = res.clone(); caches.open(CACHE).then(c => c.put(e.request, copia)); return res;
  }).catch(() => caches.match("./index.html")))));
