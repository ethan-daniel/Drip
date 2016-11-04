package hhs_students.com.drip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ErrorSearch extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_message);
    }
    @Override
    protected void onNewIntent(Intent intent){

    }
}
