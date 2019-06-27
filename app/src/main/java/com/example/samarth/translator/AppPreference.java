package com.example.samarth.translator;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreference {

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    public static final String PREFS_NAME = "default";
    public static final String SELECTION_ONE = "selection1";
    public static final String SELECTION_TWO = "selection2";
    public static final String TEXT_TO_TRANSLATE = "textToTranslate";
    public static final String TRANSLATED_TEXT = "translatedText";
    public static final String IS_FAVOURITE = "isFavourite";

    private static AppPreference appPreference;

    public static AppPreference getInstance(Context context){
        if(appPreference == null){
            appPreference = new AppPreference(context);
        }
        return appPreference;
    }
    public AppPreference(Context context){
        sharedPref = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void setSelectionOne(int pos){
        editor.putInt(SELECTION_ONE,pos);
        editor.apply();
    }

    public void setSelectionTwo(int pos){
        editor.putInt(SELECTION_TWO,pos);
        editor.apply();
    }

    public void setTextToTranslate(String textToTranslate){
        editor.putString(TEXT_TO_TRANSLATE,textToTranslate);
        editor.apply();
    }

    public void setTranslatedText(String translatedText){
        editor.putString(TRANSLATED_TEXT,translatedText);
        editor.apply();
    }

    public void setIsFavourite(boolean isFavourite){
        editor.putBoolean(IS_FAVOURITE,isFavourite);
        editor.apply();
    }

    public String getTextToTranslate(){
        return sharedPref.getString(TEXT_TO_TRANSLATE,"");
    }

    public String getTranslatedText(){
        return sharedPref.getString(TRANSLATED_TEXT,"");
    }

    public int getSelectionOne(){
        return sharedPref.getInt(SELECTION_ONE,0);
    }

    public int getSelectionTwo(){
        return sharedPref.getInt(SELECTION_TWO,1);
    }

    public boolean getIsFavourite(){
        return sharedPref.getBoolean(IS_FAVOURITE,false);
    }
    
}
