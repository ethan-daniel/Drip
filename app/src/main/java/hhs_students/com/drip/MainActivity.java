package hhs_students.com.drip;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView stateAverage;
    private TextView reportGenerated;
    private TextView averageWaterStorageText;
    private String DEBUG_TAG = "ERROR:";
    private String mQuery;
    private int lineCountStorage = 558;
    private String reportGeneratedRaw;
    private SearchView searchView;
    private String storagePercentage;
    private String M_SEARCH;
    private Typeface robotoLight;
    /*
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;
    private FrameLayout progressBarHolder;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        robotoLight = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Light.ttf");
        searchView = (SearchView) findViewById(R.id.action_search);
        //progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        stateAverage = (TextView) findViewById(R.id.StateAverage);
        stateAverage.setTypeface(robotoLight);
        reportGenerated = (TextView) findViewById(R.id.generatedText);
        averageWaterStorageText = (TextView) findViewById(R.id.StateAverageTitle);
        averageWaterStorageText.setTypeface(robotoLight);
        /*MyTask task = new MyTask();
        task.execute();*/
        Intent intent = getIntent();
        String stringUrl = "http://cdec.water.ca.gov/cgi-progs/reservoirs/STORSUM";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
        } else {
            TextView errorMSG = (TextView) findViewById(R.id.error_message);
            errorMSG.setText(getString(R.string.network_connection_error));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) (menu.findItem(R.id.action_search).getActionView());
        ComponentName cn = getComponentName();
        cn.getPackageName();
        SearchableInfo si = searchManager.getSearchableInfo(cn);
        searchView.setSearchableInfo(si);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                break;
            case R.id.action_contact:
                break;
            default:
                break;
        }

        return true;
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            // Convert the InputStream into a string
            return readIt(is, len);


            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader in = new BufferedReader(reader);

        String temp;
        int counter = 0;
        String line = "";
        temp = in.readLine();
        while(temp != null) {
            if(counter == lineCountStorage) {
                line = in.readLine();
                break;
            }
            temp = in.readLine();
            counter++;
        }
        String[] TotalNums = line.split("STORAGE AS OF ");
        char c = ' ';
        int index = 0;
        StringBuilder builder = new StringBuilder();
        builder.append("Last Updated: ");
        String targetLine = TotalNums[1];
        while (c != '<') {
            builder.append(targetLine.charAt(index));
            index++;
            c = targetLine.charAt(index);
        }
        reportGeneratedRaw = builder.toString();
        TotalNums = line.split("</td><td>");
        c = ' ';
        index = 0;
        builder = new StringBuilder();
        targetLine = TotalNums[TotalNums.length - 29];
        while (c != '<') {
            builder.append(targetLine.charAt(index));
            index++;
            c = targetLine.charAt(index);
        }
        builder.append('%');
        storagePercentage = builder.toString();
        return storagePercentage;
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            stateAverage.setText(storagePercentage);
            reportGenerated.setText(reportGeneratedRaw);
        }
    }

    /*private class MyTask extends AsyncTask <Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = 0; i < 5; i++) {
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/
}