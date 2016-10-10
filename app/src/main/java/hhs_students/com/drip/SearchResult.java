package hhs_students.com.drip;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SearchResult extends AppCompatActivity {
    private static final String DEBUG_TAG = "HttpExample";
    private TableLayout displayData;
    private Button testButton;
    private String mSearchReservoirID;
    private String endData;
    private String[] mDataSplit;
    private String[] mAllReservoirNames;
    private String[] mReservoirName;
    private TextView mLayoutReservoirName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        displayData = (TableLayout) findViewById(R.id.data_table);
        mLayoutReservoirName = (TextView) findViewById(R.id.searched_name);
        mAllReservoirNames = getResources().getStringArray(R.array.reservoir_names);
        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
        mSearchReservoirID = intent.getStringExtra("mQuery");
        for (String s : mAllReservoirNames) {
            int i = s.indexOf(mSearchReservoirID);
            if (i > 0) {
                mReservoirName = new String[2];
                mReservoirName = mAllReservoirNames[i].split(",");
                Log.d("debug", mReservoirName[1]);
                mLayoutReservoirName.setText(mReservoirName[1]);
            }
    }
        String mStorageURL = "http://cdec.water.ca.gov/cgi-progs/queryCSV?station_id=" + mSearchReservoirID + "&sensor_num=15&dur_code=D&start_date=&end_date=&data_wish=View+CSV+Data";//urlText.getText().toString();
        String mNameOfReservoir = "http://cdec.water.ca.gov/cgi-progs/stationInfo?station_id=" + mSearchReservoirID;
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(mStorageURL);
            new DownloadWebpageTask().execute(mNameOfReservoir);
        } else {
            TextView errorMSG = (TextView) findViewById(R.id.error_message);
            errorMSG.setText(getString(R.string.network_connection_error));
        }
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myClickHandler(View view) {
        /*String mStorageURL = "http://cdec.water.ca.gov/cgi-progs/queryCSV?station_id=" + mSearchReservoirID + "&sensor_num=15&dur_code=D&start_date=&end_date=&data_wish=View+CSV+Data";//urlText.getText().toString();
        String mNameOfReservoir = "http://cdec.water.ca.gov/cgi-progs/stationInfo?station_id=" + mSearchReservoirID;
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(mStorageURL);
            new DownloadWebpageTask().execute(mNameOfReservoir);
        } else {
            TextView errorMSG = (TextView) findViewById(R.id.error_message);
            errorMSG.setText(getString(R.string.network_connection_error));
        }
    }*/
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
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
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            mDataSplit = result.split(";");
            String[] temp;
            for(int i = 2; i < mDataSplit.length-1; i++) {
                temp = mDataSplit[i].split(",");
                addRow(dateFormat(temp[0]), temp[2]);
            }
        }
    }
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 1000 characters of the retrieved
        // web page content.
        int len = 1000;

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
            String contentAsString = readIt(is, len);
            mDataSplit = contentAsString.split(";");
            return contentAsString;

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

        String line;
        String data = "";
        line = in.readLine();
        while(line != null) {
            data += line + ";";
            line = in.readLine();
        }
        data = data.substring(0, data.length()-1);
        return data;
    }

    public String dateFormat(String date){
        Log.d("debug", date);
        if (!date.contains("<!--"))
            return date.substring(4, 6) + "/" + date.substring(6) + "/" + date.substring(0, 4);
        return "null";
    }
    public void addRow(String date, String levels) {
        TableRow row= new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        TextView tDate = new TextView(this);
        TextView tLevels = new TextView(this);
        tDate.setText(date);
        tDate.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        tLevels.setText(levels);
        tDate.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.addView(tDate);
        row.addView(tLevels);
        displayData.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }
}
