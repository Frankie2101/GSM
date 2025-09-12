// sw.js - Service Worker

const CACHE_NAME = 'gsm-prod-input-cache-v1';
// Cac tai nguyen cot loi cua ung dung can duoc luu vao cache
const urlsToCache = [
    '/mobile-login',
    '/mobile-input'
];

// Su kien 'install': duoc kich hoat khi service worker duoc cai dat
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('Cache opened');
                return cache.addAll(urlsToCache);
            })
    );
    self.skipWaiting();
});

// Su kien 'activate': duoc kich hoat khi service worker duoc active
// Thuong duoc dung de don dep cac cache cu
self.addEventListener('activate', event => {
    const cacheWhitelist = [CACHE_NAME];
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    if (cacheWhitelist.indexOf(cacheName) === -1) {
                        console.log('Deleting old cache:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
    return self.clients.claim();
});

// Su kien 'fetch': can thiep vao moi request cua trang web
self.addEventListener('fetch', event => {
    // Bo qua cac request khong phai la GET
    // Bo qua cac request toi API de luon lay du lieu moi
    if (event.request.method !== 'GET' || event.request.url.includes('/api/')) {
        return; // De trinh duyet tu xu ly
    }

    event.respondWith(
        caches.open(CACHE_NAME).then(cache => {
            return cache.match(event.request).then(response => {
                // Neu tim thay response trong cache, tra ve no
                // Dong thoi, van fetch request moi tu network de cap nhat cache
                const fetchPromise = fetch(event.request).then(networkResponse => {
                    // Neu request thanh cong, cap nhat cache
                    if (networkResponse.ok) {
                        cache.put(event.request, networkResponse.clone());
                    }
                    return networkResponse;
                }).catch(err => {
                    console.error('Fetch failed; returning offline page instead.', err);
                });

                // Tra ve phan hoi tu cache ngay lap tuc neu co, neu khong thi doi fetch
                return response || fetchPromise;
            });
        })
    );
});
