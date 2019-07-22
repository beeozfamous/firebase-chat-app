package com.project.helloworst.firebasechatapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.Random;


public class Utils {
    public void hideKeyboard(Context context, View view){
        InputMethodManager inputMethodManager =(InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);

    }
//    public void setCustomTabFont(TabLayout mTabLayout){
//        ViewGroup vg =(ViewGroup) mTabLayout.getChildAt(0);
//        int tabCounts = vg.getChildCount();
//        for(int j=0;j<tabCounts;j++){
//            ViewGroup vgTab =(ViewGroup) vg.getChildAt(j);
//            int tabChildsCount= vgTab.getChildCount();
//            for(int i=0;i<tabChildsCount;i++){
//                View tabViewChild=vgTab.getChildAt(i);
//                if(tabViewChild instanceof AppCompatTextView){
//                    ((TextView) tabViewChild).setTypeface(Typeface.createFromAsset(getAssets(), "Nosifer-Regular.ttf"));
//                }
//            }
//        }
//    }
    public int avatarGen(){
        Random rand = new Random();

        int tmp = rand.nextInt(12) + 1;
        switch (tmp){
            case 1: return R.drawable.ic_app_clown;
            case 2: return R.drawable.ic_app_dracula;
            case 3: return R.drawable.ic_app_frankenstein;
            case 4: return R.drawable.ic_app_ghost;
            case 5: return R.drawable.ic_app_jackson;
            case 6: return R.drawable.ic_app_mummy;
            case 7: return R.drawable.ic_app_owl;
            case 8: return R.drawable.ic_app_phantom;
            case 9: return R.drawable.ic_app_pumpkin;
            case 10: return R.drawable.ic_app_skull_pirate;
            case 11: return R.drawable.ic_app_spider;
            case 12: return R.drawable.ic_app_zombie;
            default: return R.drawable.ic_app_zombie;
        }
    }

}
