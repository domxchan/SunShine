package co.addoil.sunshine.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;

/**
 * Created by chandominic on 28/7/14.
 */
public class FullTestSuite {

    public static Test Suite() {
        return new TestSuiteBuilder(FullTestSuite.class)
            .includeAllPackagesUnderHere().build();
    }

    public FullTestSuite() {
        super();
    }
}
