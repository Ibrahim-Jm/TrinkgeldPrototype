package com.example.trinkgeldprototype;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class RealMain extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realmain);
    }

    public void goConnect (View v){
        startActivity(new Intent(this,MainActivity.class));
    }

}
