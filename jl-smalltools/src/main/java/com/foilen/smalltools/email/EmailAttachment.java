/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.email;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * This is a way to specify attachments for the {@link EmailBuilder}. Not to use directly.
 */
class EmailAttachment {

    private String id;
    private Resource resource;

    public EmailAttachment(String id, ByteArrayResource resource) {
        this.id = id;
        this.resource = resource;
    }

    public EmailAttachment(String id, ClassPathResource resource) {
        this.id = id;
        this.resource = resource;
    }

    public EmailAttachment(String id, FileSystemResource resource) {
        this.id = id;
        this.resource = resource;
    }

    public String getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

}
