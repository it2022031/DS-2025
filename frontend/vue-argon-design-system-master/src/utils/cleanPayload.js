// src/utils/cleanPayload.js
export function cleanPayload(obj, forbiddenFields = ["id", "ownerId", "createdAt", "updatedAt"]) {
    const cleaned = { ...obj };
    forbiddenFields.forEach(f => delete cleaned[f]);
    return cleaned;
}
