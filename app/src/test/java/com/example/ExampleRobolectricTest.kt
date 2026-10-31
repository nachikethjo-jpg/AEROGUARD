package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AeroGuard", appName)
  }

  @Test
  fun `test aqi category boundaries`() {
    val good = com.example.data.model.AqiCategory.fromAqi(40)
    assertEquals(com.example.data.model.AqiCategory.GOOD, good)

    val moderate = com.example.data.model.AqiCategory.fromAqi(142)
    assertEquals(com.example.data.model.AqiCategory.MODERATE, moderate)

    val poor = com.example.data.model.AqiCategory.fromAqi(180)
    assertEquals(com.example.data.model.AqiCategory.POOR, poor)

    val severe = com.example.data.model.AqiCategory.fromAqi(250)
    assertEquals(com.example.data.model.AqiCategory.SEVERE, severe)
  }
}
