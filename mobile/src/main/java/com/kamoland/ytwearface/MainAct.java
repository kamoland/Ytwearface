package com.kamoland.ytwearface;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainAct extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (!DeployUtil.isExistRequireYtGold(this)) {
            Toast.makeText(this, R.string.ma_t_require, Toast.LENGTH_LONG).show();
        }
    }
}
