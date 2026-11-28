package com.core9.mechachat;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ModelDownloaderPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
