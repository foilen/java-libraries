package com.foilen.smalltools;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    public void setUp() throws Exception {
        darkLaunchingDisabledFeatures = new DarkLaunchingDisabledFeatures();
        darkLaunchingDisabledFeatures.setDisabledFeatures(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3);
    }

    @Test
    public void testIsAllFeaturesDisabled() {
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled());

        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_ENA_1));

        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1, FEA_DIS_2));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1, FEA_ENA_1));

        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3, FEA_ENA_1));

        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_ENA_1, FEA_ENA_2, FEA_ENA_3));
    }

    @Test
    public void testIsAllFeaturesEnabled() {
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesEnabled());

        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1));
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_ENA_1));

        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1, FEA_DIS_2));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1, FEA_ENA_1));

        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3, FEA_ENA_1));

        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesEnabled(FEA_ENA_1, FEA_ENA_2, FEA_ENA_3));
    }

    @Test
    public void testIsAnyFeaturesDisabled() {
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled());

        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_ENA_1));

        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1, FEA_DIS_2));
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1, FEA_ENA_1));

        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3));
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3, FEA_ENA_1));

        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesDisabled(FEA_ENA_1, FEA_ENA_2, FEA_ENA_3));
    }

    @Test
    public void testIsAnyFeaturesEnabled() {
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled());

        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1));
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_ENA_1));

        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1, FEA_DIS_2));
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1, FEA_ENA_1));

        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3));
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_DIS_1, FEA_DIS_2, FEA_DIS_3, FEA_ENA_1));

        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAnyFeaturesEnabled(FEA_ENA_1, FEA_ENA_2, FEA_ENA_3));
    }

    @Test
    public void testSettingDisabledFeatures() {
        List<String> disabledFeaturesList = new ArrayList<String>();
        disabledFeaturesList.add(FEA_DIS_1);
        darkLaunchingDisabledFeatures.setDisabledFeatures(disabledFeaturesList);
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_2));

        darkLaunchingDisabledFeatures.setDisabledFeatures("FEA_DIS_1,FEA_DIS_2");
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1));
        Assertions.assertTrue(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_2));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_3));

        darkLaunchingDisabledFeatures.setDisabledFeatures("");
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_1));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_2));
        Assertions.assertFalse(darkLaunchingDisabledFeatures.isAllFeaturesDisabled(FEA_DIS_3));
    }

}
