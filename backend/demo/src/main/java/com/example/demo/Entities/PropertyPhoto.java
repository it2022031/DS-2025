package com.example.demo.Entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "property_photos")
public class PropertyPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image", columnDefinition = "bytea", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)   // αναγκάζει Hibernate να το χειριστεί ως bytea/binary
    private byte[] image;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "filename")
    private String filename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}
