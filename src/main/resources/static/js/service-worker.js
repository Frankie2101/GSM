/**
 * Service Worker script for the Garment Simple MES mobile application.
 * This script enables Progressive Web App (PWA) features like offline caching
 * and improved performance.
 */

/**
 * A unique name for the cache.
 */
const CACHE_NAME = 'gsm-prod-input-cache-v1';

/**
 * An array of core application shell URLs that should be pre-cached
 * as soon as the service worker is installed.
 */
const urlsToCache = [
    '/mobile-login',
    '/mobile-output'
];

/**
 * The 'install' event is fired when the service worker is first installed.
 * It's used to open the cache and add the core application files to it.
 */
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

/**
 * The 'activate' event is fired when the service worker becomes active.
 */
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

/**
 * The 'fetch' event intercepts every network request made by the page.
 * This allows the service worker to control how requests are handled,
 * enabling offline functionality.
 */
self.addEventListener('fetch', event => {
    /**
     * Ignore non-GET requests (like POST) and API calls.
     * API calls should always fetch fresh data from the network.
     */
    if (event.request.method !== 'GET' || event.request.url.includes('/api/')) {
        return;
    }

    /**
     * Implements the "Stale-While-Revalidate" caching strategy.
     * 1. Respond immediately with the cached version if available (stale).
     * 2. In the background, fetch a fresh version from the network to update the cache (revalidate).
     */
    event.respondWith(
        caches.open(CACHE_NAME).then(cache => {
            return cache.match(event.request).then(response => {
                // Fetch a fresh response from the network in the background.
                const fetchPromise = fetch(event.request).then(networkResponse => {
                    // If the fetch is successful, update the cache with the new version.
                    if (networkResponse.ok) {
                        cache.put(event.request, networkResponse.clone());
                    }
                    return networkResponse;
                }).catch(err => {
                    console.error('Fetch failed; returning offline page instead.', err);
                });

                // Return the cached response immediately if it exists, otherwise wait for the network fetch.
                return response || fetchPromise;
            });
        })
    );
});
