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
package org.secuso.privacyfriendlyactivitytracker

import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.io.File


class TestUtils {
    companion object {
        @JvmStatic
        fun readFileToString(file: File): String? {
            return file.readText()
        }

        @JvmStatic
        fun getResourceString(id: Int): String? {
            val targetContext: Context = ApplicationProvider.getApplicationContext()
            return targetContext.resources.getString(id)
        }

        @JvmStatic
        fun isExternalStorageAvailable(): Boolean {
            val extStorageState = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED == extStorageState) {
                true
            } else false
        }

        @JvmStatic
        fun getText(matcher: Matcher<View?>?): String? {
            val stringHolder = arrayOf<String?>(null)
            onView(matcher).perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return isAssignableFrom(TextView::class.java)
                }

                override fun getDescription(): String {
                    return "getting text from a TextView"
                }

                override fun perform(uiController: UiController, view: View) {
                    val tv = view as TextView //Save, because of check in getConstraints()
                    stringHolder[0] = tv.text.toString()
                }
            })
            return stringHolder[0]
        }

        @JvmStatic
        fun greaterOrEqual(value: Int): Matcher<View?>? {
            var convertedValue = 0
            return object : TypeSafeMatcher<View?>() {
                override fun describeTo(description: Description) {
                    description.appendText("TextView with integer value >= $value")
                }

                override fun matchesSafely(item: View?): Boolean {
                    if (item !is TextView) return false
                    convertedValue = Integer.valueOf(item.text.toString())
                    return convertedValue >= value
                }
            }
        }

        @JvmStatic
        fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?>? {
            return object : TypeSafeMatcher<View?>() {
                var currentIndex = 0
                override fun describeTo(description: Description) {
                    description.appendText("with index: ")
                    description.appendValue(index)
                    matcher.describeTo(description)
                }

                override fun matchesSafely(view: View?): Boolean {
                    return matcher.matches(view) && currentIndex++ == index
                }
            }
        }

        @JvmStatic
        fun waitFor(millis: Long): ViewAction? {
            return object : ViewAction {

                override fun getDescription(): String {
                    return "Wait for $millis milliseconds."
                }

                override fun getConstraints(): Matcher<View> {
                    return isRoot()
                }

                override fun perform(uiController: UiController, view: View?) {
                    uiController.loopMainThreadForAtLeast(millis)
                }
            }
        }
    }
}