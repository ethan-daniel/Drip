package hhs_students.com.drip;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class SearchResult extends AppCompatActivity {
    private static final String DEBUG_TAG = "HttpExample";
    private String firstLineCSV = "Title:";
    private TableLayout displayData;
    private Button monthlyButton;
    private Button dailyButton;
    private TableLayout holdRow1;
    private TableLayout holdRow2;
    private boolean onDaily;
    private boolean onMonthly;
    private boolean gettingMonthly;
    private boolean hasReservoir;
    private String URLstorage;
    private String mSearchReservoirID;
    private String mSearchOrigQuery;
    private String endData;
    private String mStorageURL;
    private ArrayList<String> mStorageLevels;
    private GraphView graphData;
    private String noData;
    private String[] mDataSplit;
    private String[] mAllReservoirNames;
    private String[] mReservoirName;
    private TextView mLayoutReservoirName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        dailyButton = (Button) findViewById(R.id.HasDaily);
        monthlyButton = (Button) findViewById(R.id.HasMonthly);
        onDaily = false;
        onMonthly = false;
        mStorageLevels = new ArrayList<String>();
        gettingMonthly = false;
        graphData = (GraphView) findViewById(R.id.graph);
        mAllReservoirNames = getResources().getStringArray(R.array.reservoir_names);
        mReservoirName = new String[1];
        handleIntent(getIntent());
        displayData = (TableLayout) findViewById(R.id.data_table);
        holdRow1 = new TableLayout(this);
        holdRow2 = new TableLayout(this);
        mLayoutReservoirName = (TextView) findViewById(R.id.searched_name);
        noData = "N/A";
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
        hasReservoir = false;
        mSearchOrigQuery = intent.getStringExtra("mQuery");
        if (mSearchOrigQuery.length() < 3) {
            Intent errorIntent = new Intent(this, ErrorSearch.class);
            startActivity(errorIntent);
        } else {

            if (mSearchOrigQuery.length() >= 3) {
                for (int i = 0; i < mAllReservoirNames.length; i++) {
                    String[] temp = mAllReservoirNames[i].split(",");
                    if (temp[0].toLowerCase().contains(mSearchOrigQuery)) {
                        mReservoirName = mAllReservoirNames[i].split(",");
                        hasReservoir = true;
                        i = mAllReservoirNames.length;
                    }
                }
            }
            if (hasReservoir)
                mSearchReservoirID = mReservoirName[0];
            else {
                if (mSearchOrigQuery.length() >= 3) {
                    for (int i = 0; i < mAllReservoirNames.length; i++) {
                        String[] temp = mAllReservoirNames[i].split(",");
                        if (temp[1].toLowerCase().contains(mSearchOrigQuery)) {
                            mReservoirName = mAllReservoirNames[i].split(",");
                            hasReservoir = true;
                            i = mAllReservoirNames.length;
                        }
                    }
                }
                if (hasReservoir)
                    mSearchReservoirID = mReservoirName[0];
                else {
                    if (mSearchOrigQuery.length() >= 3) {
                        for (int i = 0; i < mAllReservoirNames.length; i++) {
                            String[] temp = mAllReservoirNames[i].split(",");
                            if (temp[2].toLowerCase().contains(mSearchOrigQuery)) {
                                mReservoirName = mAllReservoirNames[i].split(",");
                                hasReservoir = true;
                                i = mAllReservoirNames.length;
                            }
                        }
                    }
                    if (hasReservoir)
                        mSearchReservoirID = mReservoirName[0];
                    else {
                        if (mSearchOrigQuery.length() >= 3) {
                            for (int i = 0; i < mAllReservoirNames.length; i++) {
                                String[] temp = mAllReservoirNames[i].split(",");
                                if (temp[3].toLowerCase().contains(mSearchOrigQuery)) {
                                    mReservoirName = mAllReservoirNames[i].split(",");
                                    hasReservoir = true;
                                    i = mAllReservoirNames.length;
                                }
                            }
                        }
                        if (hasReservoir)
                            mSearchReservoirID = mReservoirName[0];
                        else {
                            if (mSearchOrigQuery.length() >= 3) {
                                for (int i = 0; i < mAllReservoirNames.length; i++) {
                                    String[] temp = mAllReservoirNames[i].split(",");
                                    if (temp[4].toLowerCase().contains(mSearchOrigQuery)) {
                                        mReservoirName = mAllReservoirNames[i].split(",");
                                        hasReservoir = true;
                                        i = mAllReservoirNames.length;
                                    }
                                }
                            }
                            if (hasReservoir)
                                mSearchReservoirID = mReservoirName[0];
                        }
                    }
                }
            }
            if (!hasReservoir) {
                Intent errorIntent = new Intent(this, ErrorSearch.class);
                startActivity(errorIntent);
                return;
            }


            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (mReservoirName[5].contains("true") && mReservoirName[6].contains("true")) {
                //dodaily, set both visible
                onDaily = true;
                mStorageURL = "http://cdec.water.ca.gov/cgi-progs/queryCSV?station_id=" + mSearchReservoirID + "&sensor_num=15&dur_code=D&start_date=&end_date=&data_wish=View+CSV+Data";//urlText.getText().toString();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask().execute(mStorageURL);
                } else {
                    TextView errorMSG = (TextView) findViewById(R.id.error_message);
                    errorMSG.setText(getString(R.string.network_connection_error));
                }

            } else if (mReservoirName[5].contains("true") && mReservoirName[6].contains("false")) {
                //dodaily, don't set either visible
                mStorageURL = "http://cdec.water.ca.gov/cgi-progs/queryCSV?station_id=" + mSearchReservoirID + "&sensor_num=15&dur_code=D&start_date=&end_date=&data_wish=View+CSV+Data";//urlText.getText().toString();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask().execute(mStorageURL);
                } else {
                    TextView errorMSG = (TextView) findViewById(R.id.error_message);
                    errorMSG.setText(getString(R.string.network_connection_error));
                }
            } else if (mReservoirName[5].contains("false") && mReservoirName[6].contains("true")) {
                mStorageURL = "http://cdec.water.ca.gov/cgi-progs/queryCSV?station_id=" + mSearchReservoirID + "&sensor_num=15&dur_code=M&start_date=&end_date=&data_wish=View+CSV+Data";//urlText.getText().toString();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask().execute(mStorageURL);
                } else {
                    TextView errorMSG = (TextView) findViewById(R.id.error_message);
                    errorMSG.setText(getString(R.string.network_connection_error));
                }
            } else {
                Intent uhOhIntent = new Intent(this, ErrorSearch.class);
                startActivity(uhOhIntent);
                //don't do anything, displayData addview
            }
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

    // Uses AsyncTask to create a task away from the main UI thread. This task
    // takes a
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
        @Override
        protected void onPostExecute(String result) {
            if (result.contains(firstLineCSV)) {
                Log.d("result", result);
                mDataSplit = result.split(";");
                String[] temp;
                String place = "";
                mLayoutReservoirName.setText(mReservoirName[1]);
                mStorageLevels.clear();

                for (int i = 2; i < mDataSplit.length; i++) {
                    temp = mDataSplit[i].split(",");
                    if (!temp[2].equals("m")) {
                        addRow(dateFormat(temp[0]), temp[2]);
                        mStorageLevels.add(temp[2]);
                    }
                    else {
                        temp[2] = noData;
                        addRow(dateFormat(temp[0]), temp[2]);
                    }
                }
                double[] tempStorageLevels = new double[mStorageLevels.size()];
                for (int i = 0; i < mStorageLevels.size(); i++) {
                    tempStorageLevels[i] = Integer.parseInt(mStorageLevels.get(i));
                }
                DataPoint[] tempDataPoints = new DataPoint[mStorageLevels.size()]; // declare an array of DataPoint objects with the same size as your list
                for (int i = 0; i < mStorageLevels.size(); i++) {
                    // add new DataPoint object to the array for each of your list entries
                    tempDataPoints[i] = new DataPoint(i, tempStorageLevels[i]); // not sure but I think the second argument should be of type double
                }
                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(tempDataPoints);
                series.setDrawDataPoints(true);
                graphData.removeAllSeries();
                graphData.addSeries(series);

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
        Log.d("dateForm", date);
        if (!date.contains("<!--"))
            return date.substring(4, 6) + "/" + date.substring(6) + "/" + date.substring(0, 4);
        return "null";
    }
    public void addRow(String date, String levels) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 100, 0, 5);
        row.setLayoutParams(lp);
        TextView tDate = new TextView(this);
        tDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        TextView tLevels = new TextView(this);
        tLevels.setTextColor(Color.BLACK);
        tLevels.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tDate.setText(date);
        tDate.setTextColor(Color.BLACK);
        tDate.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        tLevels.setText(levels);
        tDate.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.addView(tDate);
        row.addView(tLevels);
        displayData.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        }

    public void DailyClicked(View view){
        if(!onDaily && onMonthly) {
            displayData.removeAllViews();
            mStorageURL = "http://cdec.water.ca.gov/cgi-progs/queryCSV?station_id=" + mSearchReservoirID + "&sensor_num=15&dur_code=D&start_date=&end_date=&data_wish=View+CSV+Data";
            new DownloadWebpageTask().execute(mStorageURL);
            onMonthly = false;
            onDaily = true;
        }
    }
    public void MonthlyClicked(View view){
        if(!onMonthly && onDaily){
            Log.d("inside monthly", "inside monthly");
            displayData.removeAllViews();
            mStorageURL = "http://cdec.water.ca.gov/cgi-progs/queryCSV?station_id=" + mSearchReservoirID + "&sensor_num=15&dur_code=M&start_date=&end_date=&data_wish=View+CSV+Data";
            new DownloadWebpageTask().execute(mStorageURL);
            onDaily = false;
            onMonthly = true;
        }
    }
}
