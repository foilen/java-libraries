/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.jsch;

import com.foilen.smalltools.tools.AbstractBasics;
import com.jcraft.jsch.UserInfo;

public class AutoApproveUserinfo extends AbstractBasics implements UserInfo {

    private <T> T ask(String forWhat, String message, T returnedValue) {
        logger.info("Asking for {} with message {}. Returning {}", forWhat, message, returnedValue);
        return returnedValue;
    }

    private <T> T ask(String forWhat, T returnedValue) {
        logger.info("Asking for {}. Returning {}", forWhat, returnedValue);
        return returnedValue;
    }

    @Override
    public String getPassphrase() {
        return ask("passphrase", null);
    }

    @Override
    public String getPassword() {
        return ask("password", null);
    }

    @Override
    public boolean promptPassphrase(String message) {
        return ask("promptPassphrase", message, false);
    }

    @Override
    public boolean promptPassword(String message) {
        return ask("promptPassword", message, false);
    }

    @Override
    public boolean promptYesNo(String message) {
        return ask("promptYesNo", message, true);
    }

    @Override
    public void showMessage(String message) {
        logger.info("Asks to show message: {}", message);
    }

}
