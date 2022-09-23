/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

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
 * implementation 'org.apache.commons:commons-lang3:3.12.0'
 * implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.4'
 * implementation 'org.slf4j:slf4j-api:2.0.2'
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public abstract class AbstractApiBase extends AbstractBasics {

}
