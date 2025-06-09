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
