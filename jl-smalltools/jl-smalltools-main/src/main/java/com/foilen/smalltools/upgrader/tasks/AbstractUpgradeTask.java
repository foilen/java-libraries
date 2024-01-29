/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.upgrader.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this to have some common helpers for your task.
 */
public abstract class AbstractUpgradeTask implements UpgradeTask {

    /**
     * The logger.
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

}
