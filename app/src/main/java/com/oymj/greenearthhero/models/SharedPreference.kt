package com.oymj.greenearthhero.models

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(val context: Context) {
    private val PREFERENCE_NAME = "greenEarthHero"//NAME OF The Cache We Store. EX:Like a databases name, any name is acceptable
    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    /**
     * Tips:
     * Double exclamation mark at the back of variable(!!)
     * This mean we force The compiler to thrown an Kotlin null pointer exeception if the variable is null
     */
    fun save(KEY_NAME: String, text: String) {

        val sharedPrefEditor: SharedPreferences.Editor = sharedPref.edit()

        sharedPrefEditor.putString(KEY_NAME, text)

        sharedPrefEditor!!.commit()
    }

    fun save(KEY_NAME: String, value: Int) {
        val sharedPrefEditor: SharedPreferences.Editor = sharedPref.edit()

        sharedPrefEditor.putInt(KEY_NAME, value)

        sharedPrefEditor.commit()
    }

    fun save(KEY_NAME: String, status: Boolean) {

        val sharedPrefEditor: SharedPreferences.Editor = sharedPref.edit()

        sharedPrefEditor.putBoolean(KEY_NAME, status!!)

        sharedPrefEditor.commit()
    }

    fun getValueString(KEY_NAME: String): String? {

        return sharedPref.getString(KEY_NAME, null)


    }

    fun getValueInt(KEY_NAME: String): Int {

        return sharedPref.getInt(KEY_NAME, 0)
    }

    fun getValueBoolean(KEY_NAME: String, defaultValue: Boolean): Boolean {

        return sharedPref.getBoolean(KEY_NAME, defaultValue)

    }

    fun clearSharedPreference() {

        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.clear()
        editor.commit()
    }

    fun removeValue(KEY_NAME: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()

        editor.remove(KEY_NAME)
        editor.commit()
    }


}