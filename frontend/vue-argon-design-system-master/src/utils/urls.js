// src/utils/urls.js
export function origin() {
    return window.location.origin; // κρατάει πάντα port (π.χ. :8090)
}

export function photoUrl(photoId) {
    return `${origin()}/api/properties/photos/${photoId}`;
}
