package com.test.awstranslate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.transform.QueryResultJsonUnmarshaller;
import com.amazonaws.services.dynamodbv2.model.transform.ScanRequestMarshaller;
import com.amazonaws.services.dynamodbv2.model.transform.ScanResultJsonUnmarshaller;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class ResultActivity extends AppCompatActivity {


    static AWSCredentials awsCredentials = new AWSCredentials() {
        @Override
        public String getAWSAccessKeyId() {
            return "";
        }

        @Override
        public String getAWSSecretKey() {
            return "";
        }
    };

    Button btnBback;
    Button MusicButton,VideoButton,MovieButton;
    String url;
    ArrayList<String> videoid = new ArrayList<String>();
    ArrayList<String> musicid = new ArrayList<String>();
    ArrayList<String> movieid = new ArrayList<String>();

    AmazonDynamoDBAsyncClient dynamoDBAsyncClient = new
            AmazonDynamoDBAsyncClient(awsCredentials);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setTitle("이거 해볼래?");

        Intent intent = new Intent(this.getIntent());
        movieid=intent.getStringArrayListExtra("movie");
        musicid=intent.getStringArrayListExtra("music");
        videoid=intent.getStringArrayListExtra("video");

        btnBback = (Button) findViewById(R.id.btnBback);
        MusicButton = (Button)findViewById(R.id.Musicbutton);
        VideoButton = (Button)findViewById(R.id.Videobutton);
        MovieButton = (Button)findViewById(R.id.Moviebutton);

        MusicButton.setText(musicid.get(2));
        VideoButton.setText(videoid.get(2));
        MovieButton.setText(movieid.get(2));

        btnBback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(a);
            }
        });

        MusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("music in");
                url = DBput(musicid);
                YoutubeConnect(url);
            }
        });

        MovieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("movie in");
                url = DBput(movieid);
                YoutubeConnect(url);
            }
        });

        VideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("video in");
                url = DBput(videoid);
                YoutubeConnect(url);
            }
        });

    }

    private String DBput(ArrayList<String> linkData){
        //dynamodb테이블종류

        String table_name = linkData.get(0);
        String link = linkData.get(1);

        //특정 table의 항목 전부 scan
        Future<ScanResult> usedList = Scan("USEDVM");//시청했던 영상 리스트 테이블

        try {
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            List<Map<String, AttributeValue>> listitem = usedList.get().getItems();
            JsonElement listElement = jsonParser.parse(gson.toJson(listitem));

            Put(link,table_name,listElement);
            return link;
        }
        catch (Exception e){
            //Log.d(LOG_TAG, "## error: " +e.getLocalizedMessage());
        }
        return null;
    }

    private Future<ScanResult> Scan(String table_name){
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(table_name);

        return dynamoDBAsyncClient.scanAsync(scanRequest);
    }

    private void Put(String link,String table_name,JsonElement listElement){
        Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
        int size =listElement.getAsJsonArray().size();
        if(size==0){
            key.put("id", new AttributeValue("1"));
            key.put("check",new AttributeValue("1"));
        }
        else if (size==20){
            int index = 0, id;
            while(listElement.getAsJsonArray().get(index).getAsJsonObject().get("id").getAsJsonObject().get("s").getAsInt()!=20){
                index++;
            }
            int end = listElement.getAsJsonArray().get(index).getAsJsonObject().get("check").getAsJsonObject().get("s").getAsInt();
            index = 0;
            if(end == 1){
                id = Check(end,listElement);
                key.put("id", new AttributeValue(String.valueOf(id)));key.put("check",new AttributeValue("0"));
            }
            if(end == 0){
                id = Check(end,listElement);
                key.put("id", new AttributeValue(String.valueOf(id)));key.put("check",new AttributeValue("1"));
            }
        }else{
            key.put("id", new AttributeValue(String.valueOf(size+1)));
            key.put("check",new AttributeValue("1"));
        }
        key.put("table",new AttributeValue(table_name));
        key.put("Vid",new AttributeValue(link));

        PutItemRequest putItemRequest = new PutItemRequest("USEDVM",key);

        dynamoDBAsyncClient.putItemAsync(putItemRequest);
    }

    private int Check(int check, JsonElement listElement){
        int id=1, index=0;
        while(index<20){
            if(listElement.getAsJsonArray().get(index).getAsJsonObject().get("id").getAsJsonObject().get("s").getAsInt() == id){
                if(listElement.getAsJsonArray().get(index).getAsJsonObject().get("check").getAsJsonObject().get("s").getAsInt()==check){
                    return id;
                }else {id++;index=-1;}
            }
            index++;
        }
        return 0;
    }
    //유튜브 연결 링크
    private void YoutubeConnect(String url){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + url));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + url));
        try {
            startActivity(appIntent);
            System.out.println("app");
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
            System.out.println("web");
        }
    }

    @SuppressLint("ParcelCreator")
    public class Sentiment implements Parcelable {
        private String videoId = "a";
        private String title = "a";
        private String tablename = "a";
        private Integer id = 0;
        private Double positive = 0.0;
        private Double negative = 0.0;
        private Double neutral = 0.0;
        private Double mixed = 0.0;
        private Double[] sentiment = new Double[5];


        public Sentiment(Double posi, Double nega, Double neut, Double mix, String vid, String vtitle, String vtable, Integer dbid) {
            videoId = vid;
            title = vtitle;
            tablename = vtable;
            id = dbid;
            positive = posi;
            negative = nega;
            neutral = neut;
            mixed = mix;
        }
        public Double[] getSentiment(){
            sentiment[0] = positive;
            sentiment[1] = negative;
            sentiment[2] = neutral;
            sentiment[3] = mixed;
            return sentiment;
        }
        public String getVideoId() {
            return videoId;
        }
        public String getTitle() {
            return title;
        }
        public Integer getId() {
            return id;
        }
        public String getTable() {
            return tablename;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }

    @Override
    public void onBackPressed() {
    }//뒤로가기 막음

}