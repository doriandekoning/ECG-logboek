package com.kingdorian.android.ecg_logboek;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static MainListAdapter adapter;
    static ActivityData data;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            ActivityData.readData(this);
        } catch (Exception e) {
            firstStartup();
        }

    }



    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onStart() {
        ActivityData.setCalendar(Calendar.getInstance());
        // Schedule alarm
        Intent intent = new Intent(this, HourlyNotificationService.class);
        PendingIntent pendingintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        System.out.println("Starting alarm!" + new Date(ActivityData.getStartTimeMillis()).toString());
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, ActivityData.getStartTimeMillis(), 10*60*1000, pendingintent);

        ListView listview = (ListView) findViewById(R.id.listView);
        adapter = new MainListAdapter(this, R.layout.activity_main, ActivityData.getDataArrayList());
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
                DialogFragment dialog = EditEntryDialog.newInstance(id);
                dialog.setCancelable(false);
                dialog.show(getFragmentManager(), "Edit hour entry");

            }
        });

        LayoutInflater li = LayoutInflater.from(this);
        final View promptView = li.inflate(R.layout.prompt, null);
        Resources res = getResources();
        final int hourId = (int) data.getCurrentHour();
        String subTitle = res.getString(R.string.beforeTimeSubTitle);
        subTitle += data.getStartTime(hourId) + ":00" + res.getString(R.string.betweenTimeSubTitle);
        subTitle += data.getEndTime(hourId) + ":00" + res.getString(R.string.afterTimeSubTitle);
        ((TextView) promptView.findViewById(R.id.subTitle)).setText(subTitle);
        if (hourId<47&&data.getData()[hourId] != null) {
            ((TextView) promptView.findViewById(R.id.editTextDialogUserInput)).setText(data.getData()[hourId].getDescription());
        }
        DialogFragment dialog = EditEntryDialog.newInstance(hourId);
        dialog.show(getFragmentManager(), "Edit hour entry");
        adapter.notifyDataSetChanged();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.kingdorian.android.ecg_logboek/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();


        data.writeData(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.kingdorian.android.ecg_logboek/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private void firstStartup() {
        System.out.println("First time startup!");

        Intent intent = new Intent(this, FirstTimeActivity.class);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainbar, menu);
        return true;

    }


    public boolean mailData(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"test12345@mailinator.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        intent.putExtra(Intent.EXTRA_TEXT, "body of email");
        String newPath = getExternalCacheDir() + File.pathSeparator + UUID.randomUUID();
        // Copy file
        try {
            FileInputStream fis = openFileInput(ActivityData.FILENAME);
            new File(newPath).createNewFile();
            FileOutputStream out = new FileOutputStream(newPath);
            byte[] buf = new byte[1024];
            int len;
            while((len=fis.read(buf)) > 0) {
                System.out.println(new String(buf));
                out.write(buf, 0, len);
            }
            fis.close();
            out.close();

            ///
            ///
            File uri = new File(Uri.parse(newPath).toString());
            FileInputStream fisf = new FileInputStream(new File(Uri.parse(newPath).getPath()));
            InputStreamReader isr = new InputStreamReader(fisf);
            BufferedReader buffered = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = buffered.readLine()) != null) {
                System.out.println("hello: " + line);
            }
            buffered.close();
            isr.close();
            /// ///
            // Add attachment
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(newPath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try{
            startActivity(Intent.createChooser(intent, "Send mail..."));
            return true;
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "No mail clients installed", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
