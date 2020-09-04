package com.dcode.shareindia.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.dcode.shareindia.R;

public class AboutActivity extends AppCompatActivity {
    TextView about_one;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

      //  about_one = findViewById(R.id.about_ONE);
//        WebView view = (WebView) findViewById(R.id.about_ONE);
//        WebView view1 = (WebView) findViewById(R.id.about_ONE);
//        String text;
//        text = "<html><body><p align=\"justify\">";
//        text+= "In this Apk You Can Send All The Files and Data of Your Device. It is VeryFast To Send Apk Files From One Mobile To Another, it is Very to Send File Faster."
//                + "This App is Made By DcodeCompany\n" + "\n</FONT><A HREF=\"http://www.dcodecompany.com/\">" + "and This Apk is Made By The Company AMS Graphics Designers \n" + "</FONT><A HREF=\"http://www.designersguru.in/\">";
//        text+= "</p></body></html>";
//        view.loadData(text, "text/html", "utf-8");



//        text = "<html><body><p align=\"justify\">";
//        text+= "This App is Made By DcodeCompany (WWW.dcodecompany.com) and This Apk is Made By The Company AMS Graphics Designers (WWW.designersguru.in).";
//        text+= "</p></body></html>";
//        view1.loadData(text, "text/html", "utf-8");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            about_one.justificationMode = JUSTIFICATION_MODE_INTER_WORD);
//        }

    }
}
