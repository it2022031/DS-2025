package com.example.demo.dto;

public record PropertyPhotoDto(
        // Όνομα πεδίων
        Long id,
        Long propertyId,
        String filename,
        String contentType,
        long sizeBytes
) {
    public static PropertyPhotoDto of(com.example.demo.Entities.PropertyPhoto p, long size) {

        // Τιμές πεδίων
        return new PropertyPhotoDto(
                p.getId(),
                p.getProperty().getId(),
                p.getFilename(),
                p.getContentType(),
                size
        );
    }
}