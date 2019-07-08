package open.it.com.petit;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private MainActivityTmp activity;

    @Rule
    public ActivityTestRule<MainActivityTmp> activityTestRule = new ActivityTestRule<MainActivityTmp>(MainActivityTmp.class);

    @Before
    public void setUp() {
        this.activity = activityTestRule.getActivity();
    }

    @Test
    public void useAppContext() throws Exception {
        activity.sendRegistrationToServer();
    }
}
