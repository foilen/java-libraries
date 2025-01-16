/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a darklaunching feature to tell which features should be disabled. Any feature not listed is enabled by default.
 *
 * <pre>
 * Tip: Use constants for features names instead of using Strings. That makes it easier to remove one component when no more needed.
 *
 * Set up the dark launching by providing a list of disabled feature or provide a comma separated list:
 * DarkLaunchingDisabledFeatures darkLaunchingDisabledFeatures = new DarkLaunchingDisabledFeatures();
 * darkLaunchingDisabledFeatures.setDisabledFeatures("FEA_DIS_1,FEA_DIS_2");
 *
 * Then, you can check if one or many features are enabled or disabled:
 * darkLaunchingDisabledFeatures.isAllFeaturesEnabled("FEA_ENA_1", "FEA_DIS_2");
 * </pre>
 */
public class DarkLaunchingDisabledFeatures {

    private static final Logger logger = LoggerFactory.getLogger(DarkLaunchingDisabledFeatures.class);
    private List<String> disabledFeatures = new ArrayList<String>();

    /**
     * Tells if the specified features are all currently disabled.
     *
     * @param featureNames all the needed features names
     * @return true if all the features are disabled
     */
    public boolean isAllFeaturesDisabled(String... featureNames) {
        // No names provided
        if (featureNames == null || featureNames.length == 0) {
            return false;
        }

        for (String featureName : featureNames) {
            if (!disabledFeatures.contains(featureName)) {
                logger.debug("The feature {} is enabled", featureName);
                return false;
            }
        }

        return true;
    }

    /**
     * Tells if the specified features are all currently enabled.
     *
     * @param featureNames all the needed features names
     * @return true if all the features are enabled
     */
    public boolean isAllFeaturesEnabled(String... featureNames) {
        // No names provided
        if (featureNames == null || featureNames.length == 0) {
            return true;
        }

        for (String featureName : featureNames) {
            if (disabledFeatures.contains(featureName)) {
                logger.debug("The feature {} is disabled", featureName);
                return false;
            }
        }

        return true;
    }

    /**
     * Tells if any of the specified features is currently disabled.
     *
     * @param featureNames features names
     * @return true if any the features is disabled
     */
    public boolean isAnyFeaturesDisabled(String... featureNames) {
        // No names provided
        if (featureNames == null || featureNames.length == 0) {
            return false;
        }

        for (String featureName : featureNames) {
            if (disabledFeatures.contains(featureName)) {
                logger.debug("The feature {} is disabled", featureName);
                return true;
            }
        }

        return false;
    }

    /**
     * Tells if any of the specified features is currently enabled.
     *
     * @param featureNames features names
     * @return true if any the features is enabled
     */
    public boolean isAnyFeaturesEnabled(String... featureNames) {
        // No names provided
        if (featureNames == null || featureNames.length == 0) {
            return true;
        }

        for (String featureName : featureNames) {
            if (!disabledFeatures.contains(featureName)) {
                logger.debug("The feature {} is enabled", featureName);
                return true;
            }
        }

        return false;
    }

    /**
     * Provide all the features names that are disabled.
     *
     * @param disabledFeatures provide a list
     */
    public void setDisabledFeatures(List<String> disabledFeatures) {
        this.disabledFeatures = disabledFeatures;
    }

    /**
     * Provide all the features names that are disabled.
     *
     * @param disabledFeatures provide a comma separated String. Eg: CREATE_USER,LOGIN_ADMIN
     */
    public void setDisabledFeatures(String disabledFeatures) {
        // Null or empty
        if (disabledFeatures == null || disabledFeatures.isEmpty()) {
            this.disabledFeatures = new ArrayList<String>();
        }

        // Split and trim
        String[] disabledFeaturesArray = disabledFeatures.split(",");
        for (int i = 0; i < disabledFeaturesArray.length; ++i) {
            disabledFeaturesArray[i] = disabledFeaturesArray[i].trim();
        }

        setDisabledFeatures(disabledFeaturesArray);
    }

    /**
     * Provide all the features names that are disabled.
     *
     * @param disabledFeatures provide an array
     */
    public void setDisabledFeatures(String... disabledFeatures) {
        this.disabledFeatures = Arrays.asList(disabledFeatures);
    }

}
