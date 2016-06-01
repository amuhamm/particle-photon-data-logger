package io.particle.cloudsdk.example_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class ValueActivity extends AppCompatActivity {

    private static final String ARG_VALUE = "ARG_VALUE";
    private static final String ARG_DEVICEID = "ARG_DEVICEID";
    private final static int INTERVAL = 1000 * 2; //2 seconds
    private TextView tv;

    Handler mHandler = new Handler();
    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            onStartClicked();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value);
        tv = (TextView) findViewById(R.id.value);
        tv.setText(String.valueOf(getIntent().getIntExtra(ARG_VALUE, 0)));

        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClicked();
            }
        });

        startRepeatingTask();

    }

    public void onStartClicked(){
        // Do network work on background thread
        Async.executeAsync(ParticleCloud.get(ValueActivity.this), new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(ParticleCloud ParticleCloud) throws ParticleCloudException, IOException {
                ParticleDevice device = ParticleCloud.getDevice(getIntent().getStringExtra(ARG_DEVICEID));
                Object variable;
                try {
                    variable = device.getVariable("voltage"); // get the "voltage" variable from the particle cloud
                } catch (ParticleDevice.VariableDoesNotExistException e) {

                    //TODO: Need to handle case when Photon times out
                    Toaster.l(ValueActivity.this, e.getMessage());
                    variable = -1;

                }
                return variable;
            }

            @Override
            public void onSuccess(Object i) { // this goes on the main thread

                //TODO: Feed graphing api to create live-stream visual
                DecimalFormat df = new DecimalFormat("#.###");
                tv.setText(df.format(i).toString() + "V");
                System.out.println("Voltage: " + df.format(i) + " V, " + i.getClass().getName());
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                e.printStackTrace();
            }
        });
    }

    public static Intent buildIntent(Context ctx, Integer value, String deviceid) {
        Intent intent = new Intent(ctx, ValueActivity.class);
        intent.putExtra(ARG_VALUE, value);
        intent.putExtra(ARG_DEVICEID, deviceid);

        return intent;
    }

}
