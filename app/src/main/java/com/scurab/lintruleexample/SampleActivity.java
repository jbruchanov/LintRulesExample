package com.scurab.lintruleexample;

import android.app.Application;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

public class SampleActivity extends AppCompatActivity {

    @Inject
    Application app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new SampleComponent() {
            @Override
            public void inject(AppCompatActivity activity) {

            }
        }.inject(this);
    }
}
