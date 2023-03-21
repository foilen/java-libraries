/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.trigger;

/**
 * The state of the trigger.
 */
public enum SmoothTriggerState {

    /**
     * No request is done
     */
    IDLE,

    /**
     * When isFirstPassThrough is used and an event is requested, this is the state that waits for delayAfterLastTriggerMs. If a request comes in, it goes in warmup ; else, it goes back to idling.
     */
    COOLDOWN,

    /**
     * When a request is made, it is in pending state. After the action is triggered, it goes in cooldown. The max amount of time in this state is managed by maxDelayAfterFirstRequest.
     */
    WARMUP,

}
