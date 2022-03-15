package com.example.solution_challenge_2022_vegather_app

import android.app.Application
import android.content.Context

class MyApplication : Application() {
    companion object{
        lateinit var prefs : PrefsManager
    }

    override fun onCreate() {
        prefs = PrefsManager(applicationContext)
        super.onCreate()
    }
}

class PrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("pref_name", Context.MODE_PRIVATE)

    fun getPrefs(key: String, defValue: String) : String {
        return prefs.getString(key, defValue).toString()
    }

    fun setPrefs(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getIntPrefs(key: String, defValue : Int) : Int {
        return prefs.getInt(key, defValue)
    }

    fun setIntPrefs(key: String, value: Int){
        prefs.edit().putInt(key, value).apply()
    }
}