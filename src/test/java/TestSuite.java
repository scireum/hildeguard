/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Runs all tests
 */
@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class", "**/*Spec.class"})
public class TestSuite {

    @BeforeClass
    public static void setUp() {

    }

    @AfterClass
    public static void tearDown() {

    }
}