/*
    Privacy Friendly Pedometer is licensed under the GPLv3.
    Copyright (C) 2017  Tobias Neidig

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlyactivitytracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.Manifest;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.viewpager.widget.ViewPager;

import com.yariksoffice.lingver.Lingver;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.secuso.privacyfriendlyactivitytracker.activities.MainActivity;

import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

@LargeTest
@RunWith(Parameterized.class)

public class LocalesTest {
    @Parameterized.Parameter(value = 0)
    public static Locale locale = Locale.ENGLISH;
    @Rule
    public ActivityTestRule<MainActivity> activityRule
            = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            Lingver.getInstance().setLocale(ApplicationProvider.getApplicationContext(), locale);
            super.beforeActivityLaunched();
        }
    };
    @Rule
    public GrantPermissionRule activityRecognitionPermission = (android.os.Build.VERSION.SDK_INT >= 29 ? GrantPermissionRule.grant(Manifest.permission.ACTIVITY_RECOGNITION) : null);

    @BeforeClass
    public static void initAll() {
        Lingver.init(ApplicationProvider.getApplicationContext(), locale);
    }

    @Parameterized.Parameters(name = "locale={0}")
    public static CopyOnWriteArrayList<Object[]> initParameters() {
        CopyOnWriteArrayList<Object[]> params = new CopyOnWriteArrayList<>();
        for (String availableLocale : BuildConfig.AVAILABLE_LOCALES) {
            params.add(new Object[]{parseLocale(availableLocale)});
        }

        return params;
    }

    private static Locale parseLocale(String str) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Locale.forLanguageTag(str);
        } else {
            if (str.contains("-")) {
                String[] args = str.split("-");
                if (args.length > 2) {
                    return new Locale(args[0], args[1], args[3]);
                } else if (args.length > 1) {
                    return new Locale(args[0], args[1]);
                } else if (args.length == 1) {
                    return new Locale(args[0]);
                }
            }

            return new Locale(str);
        }
    }

    Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ApplicationProvider.getApplicationContext()
                    .getResources().getConfiguration().getLocales()
                    .get(0);
        } else {
            return ApplicationProvider.getApplicationContext()
                    .getResources().getConfiguration().locale;
        }
    }

    @Before
    public void setUp() {
        ViewMatchers.assertThat("Locale is not supported", getCurrentLocale(), Is.is(locale));
    }

    // works on local emulators > 31, but not in GH workflow
    @SdkSuppress(maxSdkVersion = 31)
    @Test
    public void application_ShouldNotCrash_WithLanguage() {
        ViewPagerIdlingResource idlingResource = new ViewPagerIdlingResource(activityRule.getActivity().findViewById(R.id.pager), "ViewPager");
        IdlingRegistry.getInstance().register(idlingResource);

        onView(withText(R.string.day)).perform(ViewActions.click());
        onView(withText(R.string.day)).check(ViewAssertions.matches(ViewMatchers.isSelected()));

        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withText(R.string.week)).check(ViewAssertions.matches(ViewMatchers.isSelected()));
        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withText(R.string.month)).check(ViewAssertions.matches(ViewMatchers.isSelected()));
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.menu_settings)).perform(click());
        onView(withText(R.string.pref_header_general)).perform(click());
        onView(isRoot()).perform(ViewActions.pressBack());
        onView(withText(R.string.pref_header_notifications)).perform(click());
        onView(isRoot()).perform(ViewActions.pressBack());
        onView(withText(R.string.pref_header_walking_modes)).perform(click());
    }

    public static class ViewPagerIdlingResource implements IdlingResource {
        private final String resourceName;

        private boolean isIdle = true;

        private ResourceCallback resourceCallback;

        public ViewPagerIdlingResource(ViewPager viewPager, String name) {
            viewPager.addOnPageChangeListener(new ViewPagerListener());
            resourceName = name;
        }

        @Override
        public String getName() {
            return resourceName;
        }

        @Override
        public boolean isIdleNow() {
            return isIdle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }

        private class ViewPagerListener extends ViewPager.SimpleOnPageChangeListener {

            @Override
            public void onPageScrollStateChanged(int state) {
                isIdle = (state == ViewPager.SCROLL_STATE_IDLE);
                if (isIdle && resourceCallback != null) {
                    resourceCallback.onTransitionToIdle();
                }
            }
        }
    }
}
