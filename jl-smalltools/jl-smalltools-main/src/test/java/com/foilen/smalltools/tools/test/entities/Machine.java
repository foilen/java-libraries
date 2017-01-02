/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.test.entities;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Machine implements java.io.Serializable {

    private static final long serialVersionUID = 2015122201L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Version
    private long version;

    @Column(unique = true, nullable = false)
    private String name;

    @ElementCollection
    private Set<String> ips;

    public Machine() {
    }

    public Machine(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public Set<String> getIps() {
        return ips;
    }

    public String getName() {
        return this.name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIps(Set<String> ips) {
        this.ips = ips;
    }

    public void setName(String name) {
        this.name = name;
    }

}
