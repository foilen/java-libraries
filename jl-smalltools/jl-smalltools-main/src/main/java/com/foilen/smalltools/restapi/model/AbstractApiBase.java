/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.foilen.smalltools.tools.AbstractBasics;

/**
 * The most low-level object for a REST object.
 *
 * <pre>
 * Dependencies:
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public abstract class AbstractApiBase extends AbstractBasics {

}
