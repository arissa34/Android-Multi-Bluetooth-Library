package net.reixd.bluetoothservice_sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.button_start_bt_service);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent serviceIntent = new Intent(v.getContext(), MyService.class);
                startService(serviceIntent);
            }
        });

    }
}
