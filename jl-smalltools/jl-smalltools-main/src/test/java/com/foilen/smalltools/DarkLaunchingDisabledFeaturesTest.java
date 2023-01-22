/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DarkLaunchingDisabledFeatures}.
 */
public class DarkLaunchingDisabledFeaturesTest {

    private static final String FEA_DIS_1 = "FEA_DIS_1";
    private static final String FEA_DIS_2 = "FEA_DIS_2";
    private static final String FEA_DIS_3 = "FEA_DIS_3";

    private static final String FEA_ENA_1 = "FEA_ENA_1";
    private static final String FEA_ENA_2 = "FEA_ENA_2";
    private static final String FEA_ENA_3 = "FEA_ENA_3";

    private DarkLaunchingDisabledFeatures darkLaunchingDisabledFeatures;

    @Before
    public void setUp() throws Exception {
        darkLaunchingDisabledFeatures = new DarkLaunchingDisabledFeatures();
        darkLaunchingDisabledFeatures.setDisabledFeatures(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3);
    }

    @Test
    public void testIsAllFeaturesDisabled() {
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled());

        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_ENA_1));

        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1, FEA_DIS_2));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1, FEA_ENA_1));

        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3, FEA_ENA_1));

        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_ENA_1, FEA_ENA_2, FEA_ENA_3));
    }

    @Test
    public void testIsAllFeaturesEnabled() {
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesEnabled());

        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1));
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_ENA_1));

        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1, FEA_DIS_2));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1, FEA_ENA_1));

        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3, FEA_ENA_1));

        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_ENA_1, FEA_ENA_2, FEA_ENA_3));
    }

    @Test
    public void testIsAnyFeaturesDisabled() {
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled());

        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_ENA_1));

        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1, FEA_DIS_2));
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1, FEA_ENA_1));

        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3));
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3, FEA_ENA_1));

        Assert.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_ENA_1, FEA_ENA_2, FEA_ENA_3));
    }

    @Test
    public void testIsAnyFeaturesEnabled() {
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled());

        Assert.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1));
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_ENA_1));

        Assert.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1, FEA_DIS_2));
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1, FEA_ENA_1));

        Assert.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3));
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3, FEA_ENA_1));

        Assert.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_ENA_1, FEA_ENA_2, FEA_ENA_3));
    }

    @Test
    public void testSettingDisabledFeatures() {
        List<String> disabledFeaturesList = new ArrayList<String>();
        disabledFeaturesList.add(FEA_DIS_1);
        darkLaunchingDisabledFeatures.setDisabledFeatures(disabledFeaturesList);
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_2));

        darkLaunchingDisabledFeatures.setDisabledFeatures("FEA_DIS_1,FEA_DIS_2");
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1));
        Assert.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_2));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_3));

        darkLaunchingDisabledFeatures.setDisabledFeatures("");
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_2));
        Assert.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_3));
    }

}
