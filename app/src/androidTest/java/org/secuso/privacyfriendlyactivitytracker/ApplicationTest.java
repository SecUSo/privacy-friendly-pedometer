package org.secuso.privacyfriendlyactivitytracker;

import static junit.framework.TestCase.assertEquals;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    @Test
    public void instrumentationTest() throws Exception {
        assertEquals("org.secuso.privacyfriendlyactivitytracker", InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
    }
}