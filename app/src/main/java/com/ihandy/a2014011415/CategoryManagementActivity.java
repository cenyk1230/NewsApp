package com.ihandy.a2014011415;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class CategoryManagementActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        mToolbar = (Toolbar)findViewById(R.id.toolbar_category_management);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(R.drawable.backward_arrow);
        setSupportActionBar(mToolbar);
    }
}
