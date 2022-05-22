package com.aizaz.accessandroid.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private SharedPreferences sharedPreferences;
    public PreferenceManager(Context context){
        sharedPreferences = context.getSharedPreferences(Constant.KEY_PREFERENCE, Context.MODE_PRIVATE);
    }
    public void putBoolen(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public Boolean getBoolen(String key){
        return sharedPreferences.getBoolean(key,false);
    }
    public void putString(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public String getString(String key){
        return sharedPreferences.getString(key, null);
    }
    public void clearPreferences(String key){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.clear();
        editor.remove(key);
        editor.apply();
    }
}
