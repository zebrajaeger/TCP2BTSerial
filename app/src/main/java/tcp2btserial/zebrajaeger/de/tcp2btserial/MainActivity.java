package tcp2btserial.zebrajaeger.de.tcp2btserial;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private TcpBtServer tcpBtServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btAutoconnect();
        String ipAddress = Utils.wifiIpAddress(getApplicationContext());
        TextView tv = (TextView) findViewById(R.id.ip_text);
        tv.setText(ipAddress);
        try {
            tcpBtServer = new TcpBtServer(BT.I, ipAddress, 7654);
        } catch (UnknownHostException e) {
            Log.e("MainActivity", "Failed to create TcpBtServer", e);
        }
    }

    public boolean btAutoconnect() {
        AppData appData = Storage.I.getAppData(getApplicationContext());
        BT.I.refreshDeviceList();
        String btAdapter = appData.getBtAdapter();
        if (BT.I.getSortedDeviceNames().contains(btAdapter)) {
            if (BT.I.setCurrentDevice(btAdapter)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            buttonBtSelectDeviceOnClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void buttonBtSelectDeviceOnClick() {
        BT.I.refreshDeviceList();
        final String[] names = BT.I.getSortedDeviceNamesAsArray();

        boolean hasDevices = (names.length > 0);
        final AtomicInteger selected = new AtomicInteger(hasDevices ? 0 : -1);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(hasDevices ? "Select Bluetooth Device" : "No Devices Found");
        if (hasDevices) {

            // find index of already selected device if exists
            int checked = 0;
            String btAdapter = Storage.I.getAppData().getBtAdapter();
            if (StringUtils.isNotBlank(btAdapter)) {
                int pos = 0;
                for (String n : names) {
                    if (btAdapter.equals(n)) {
                        checked = pos;
                    }
                    ++pos;
                }
            }

            // title
            builder.setTitle("Select Bluetooth Device");

            // devices
            builder.setSingleChoiceItems(names, checked, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selected.set(which);
                }
            });

            // OK button
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = names[selected.get()];
                    Storage.I.getAppData(getApplicationContext()).setBtAdapter(name);
                    Storage.I.save(getApplicationContext());
                    BT.I.setCurrentDevice(name);
                }
            });

            // CANCEL button
            builder.setNegativeButton("Cancel", null);
        } else {

            // no devices found
            builder.setTitle("No Devices Found");
            builder.setPositiveButton("Ok", null);
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
