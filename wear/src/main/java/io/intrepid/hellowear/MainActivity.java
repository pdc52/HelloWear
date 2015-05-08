package io.intrepid.hellowear;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    public static final String PHONE_COUNT_KEY = "PHONE_COUNT_KEY";
    public static final String WATCH_COUNT_KEY = "WATCH_COUNT_KEY";

    public static final String PHONE_COUNT_DB_PATH = "/phone-count";
    public static final String WATCH_COUNT_DB_PATH = "/watch-count";

    TextView phonePresses;
    TextView watchPresses;
    Button watchButton;

    int phoneCount;
    int watchCount;

    GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);

        phonePresses = (TextView) findViewById(R.id.phone_button_presses);
        watchPresses = (TextView) findViewById(R.id.watch_button_presses);
        watchButton = (Button) findViewById(R.id.watch_button);
        watchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseWatchCount();
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    private void increaseWatchCount() {
        watchCount++;
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(WATCH_COUNT_DB_PATH);
        putDataMapRequest.getDataMap().putInt(WATCH_COUNT_KEY, watchCount);
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleApiClient, putDataRequest);
        watchPresses.setText(getResources().getQuantityString(R.plurals.number_watch_presses, watchCount, watchCount));
    }

    private void updatePhoneCount() {
        phonePresses.setText(getResources().getQuantityString(R.plurals.number_phone_presses, phoneCount, phoneCount));
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(PHONE_COUNT_DB_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    phoneCount = dataMap.getInt(PHONE_COUNT_KEY);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updatePhoneCount();
                        }
                    });
                }
            }
        }
    }
}
