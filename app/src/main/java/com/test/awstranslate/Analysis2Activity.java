package com.test.awstranslate;

        import android.content.Intent;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.view.View;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.amazonaws.handlers.AsyncHandler;
        import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
        import com.amazonaws.auth.AWSCredentials;
        import com.amazonaws.services.dynamodbv2.model.BatchGetItemRequest;
        import com.amazonaws.services.dynamodbv2.model.BatchGetItemResult;
        import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
        import com.amazonaws.services.dynamodbv2.model.GetItemResult;
        import com.amazonaws.services.dynamodbv2.model.QueryRequest;
        import com.amazonaws.services.dynamodbv2.model.QueryResult;
        import com.amazonaws.services.dynamodbv2.model.ScanRequest;
        import com.amazonaws.services.dynamodbv2.model.ScanResult;
        import com.amazonaws.services.dynamodbv2.model.transform.QueryResultJsonUnmarshaller;
        import com.amazonaws.services.dynamodbv2.model.transform.ScanRequestMarshaller;
        import com.amazonaws.services.dynamodbv2.model.transform.ScanResultJsonUnmarshaller;

        import java.io.FileInputStream;
        import java.util.ArrayList;
        import java.util.List;
        import java.util.concurrent.ExecutionException;
        import java.util.concurrent.Future;


public class Analysis2Activity extends AppCompatActivity {

    ListView viewAnalysis;
    TextView textviewAnalysis;
    ArrayList<String> resultlist3=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis2);
        setTitle("오늘 일기분석");

        Intent intent=getIntent();
        resultlist3=intent.getStringArrayListExtra("analysis");
        List<String> list=new ArrayList<>();
        list.clear();
        for(String i:resultlist3) {
            list.add("#"+i);
        }

        viewAnalysis = (ListView) findViewById(R.id.viewAnalysis);
        textviewAnalysis = (TextView) findViewById(R.id.textviewAnalysis);

        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
        textviewAnalysis.setText("일기장에 담긴 단어를 떠올려봐요");
        viewAnalysis.setAdapter(adapter);


        //Intent a = new Intent(AnalysisActivity.this, ResultActivity.class);

    }



}
