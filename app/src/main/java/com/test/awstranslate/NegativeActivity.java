package com.test.awstranslate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class NegativeActivity extends AppCompatActivity {

    private static final String LOG_TAG = SubActivity.class.getSimpleName();
    String mainSentiment = "";
    Sentiment userSentiment = new Sentiment(null, null, null, null,"","",0);
    Sentiment movieResult = new Sentiment(null, null, null, null,"","",0);
    Sentiment musicResult = new Sentiment(null, null, null, null,"","",0);
    Sentiment videoResult = new Sentiment(null, null, null, null,"","",0);
    Sentiment[] movieSentment = new Sentiment[60];
    Sentiment[] musicSentment = new Sentiment[30];
    Sentiment[] videoSentment = new Sentiment[30];
    ArrayList<String> videolink = new ArrayList<String>();
    ArrayList<String> musiclink = new ArrayList<String>();
    ArrayList<String> movielink = new ArrayList<String>();

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

    Button button1, button2; //다음 페이지 넘어가는 버튼
    TextView viewResult; //제대로 받았는지 확인 띄우는거! 곧 지울예정!
    String getResult;

    AmazonDynamoDBAsyncClient dynamoDBAsyncClient = new
            AmazonDynamoDBAsyncClient(awsCredentials);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negative);

        for(int i = 0; i < 60; i++) {
            movieSentment[i] = new Sentiment(null, null, null, null,"","",0);
            if(i < 30){
                videoSentment[i] = new Sentiment(null, null, null, null,"","",0);
                musicSentment[i] = new Sentiment(null, null, null, null,"","",0);
            }
        }

        // 지난 액티비티에서 받은 리절트
        Intent intent = new Intent(this.getIntent());
        getResult = intent.getStringExtra("SentResult"); //지난 결과 받은거! 근데 negative에서는 굳이 필요가 없어짐 어차피 if문으로 negative 판단하고 이 액티비티 들어온거라

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        viewResult = (TextView) findViewById(R.id.viewResult);
        parseSentiment(getResult);
        viewResult.setText("오늘 너의 일기 감정분석결과는 \" " + mainSentiment + " \"라고 나왔어!");


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(NegativeActivity.this, ResultActivity.class);
                dynamoDB("SMUSIC", musicSentment);
                dynamoDB("SVIDEO", videoSentment);
                dynamoDB("ALLMOVIE", movieSentment);

                a.putStringArrayListExtra("video",videolink);
                a.putStringArrayListExtra("music",musiclink);
                a.putStringArrayListExtra("movie",movielink);
                a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(a);

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(NegativeActivity.this, ResultActivity.class);
                dynamoDB("AMUSIC", musicSentment);
                dynamoDB("AVIDEO", videoSentment);
                dynamoDB("ALLMOVIE", movieSentment);

                a.putStringArrayListExtra("video",videolink);
                a.putStringArrayListExtra("music",musiclink);
                a.putStringArrayListExtra("movie",movielink);
                a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(a);
            }
        });


    }

    public void parseSentiment(String parseText){
        try {
            String aa = "[" + parseText + "]";
            JSONArray jarray = new JSONArray(aa);

            JSONObject jObject = jarray.getJSONObject(0);  // JSONObject 추출
            String sss = jObject.getString("SentimentScore");
            mainSentiment = jObject.getString("Sentiment");
            JSONObject jjj = new JSONObject(sss);

            //String aaa = jjj.getString("Positive");

            System.out.println(mainSentiment);
            userSentiment.positive =  jjj.getDouble("Positive");
            userSentiment.negative = jjj.getDouble("Negative");
            userSentiment.neutral = jjj.getDouble("Neutral");
            userSentiment.mixed = jjj.getDouble("Mixed");

            System.out.println("##userSentiment" + userSentiment.getSentiment()[0]);
        }catch (Exception e){
            Log.d(LOG_TAG, "## json error: " +e.getLocalizedMessage());
            //System.out.println("##is in parsingPlace" + detectSentimentResult);
        }
    }

    public void dynamoDB(String tableName, Sentiment[] sentimentKind){
        ArrayList<String> links = new ArrayList<String>();
        //positive의 항목 전부 scan
        Future<ScanResult> scanResult= Scan(tableName);//가져올 영상 리스트 테이블
        Future<ScanResult> usedList = Scan("USEDVM");//시청했던 영상 리스트 테이블

        try{
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            List<Map<String, AttributeValue>> item = scanResult.get().getItems();
            List<Map<String, AttributeValue>> listitem = usedList.get().getItems();
            JsonElement listElement = jsonParser.parse(gson.toJson(listitem));
            JsonElement jsonElement = jsonParser.parse(gson.toJson(item));
            //하나씩 대조
            int counter = 0;
            for(JsonElement i : jsonElement.getAsJsonArray()){String id = i.getAsJsonObject().get("Vid").getAsJsonObject().get("s").getAsString();
                if(listitem.size()==0) {
                    sentimentKind[counter].positive = 0.0;
                    sentimentKind[counter].negative = i.getAsJsonObject().get("Negative").getAsJsonObject().get("s").getAsDouble();
                    sentimentKind[counter].neutral = i.getAsJsonObject().get("Neutral").getAsJsonObject().get("s").getAsDouble();
                    sentimentKind[counter].mixed = i.getAsJsonObject().get("Mixed").getAsJsonObject().get("s").getAsDouble();
                    sentimentKind[counter].videoId = id;
                    sentimentKind[counter].id = i.getAsJsonObject().get("id").getAsJsonObject().get("s").getAsInt();
                    sentimentKind[counter].title = i.getAsJsonObject().get("Title").getAsJsonObject().get("s").getAsString();
                }
                else {
                    boolean exist = false;
                    for(JsonElement j : listElement.getAsJsonArray()) {
                        //list
                        if(j.getAsJsonObject().get("table").getAsJsonObject().get("s").getAsString().equals(tableName)
                                && j.getAsJsonObject().get("Vid").getAsJsonObject().get("s").getAsString().equals(id)){
                            exist = true;
                            break;
                        }
                    }
                    if(!exist) {
                        sentimentKind[counter].positive = 0.0;
                        sentimentKind[counter].negative = i.getAsJsonObject().get("Negative").getAsJsonObject().get("s").getAsDouble();
                        sentimentKind[counter].neutral = i.getAsJsonObject().get("Neutral").getAsJsonObject().get("s").getAsDouble();
                        sentimentKind[counter].mixed = i.getAsJsonObject().get("Mixed").getAsJsonObject().get("s").getAsDouble();
                        sentimentKind[counter].videoId = id;
                        sentimentKind[counter].id = i.getAsJsonObject().get("id").getAsJsonObject().get("s").getAsInt();
                        sentimentKind[counter].title = i.getAsJsonObject().get("Title").getAsJsonObject().get("s").getAsString();
                    }
                }
                counter++;
            }
            if(sentimentKind == videoSentment){
                videoResult = calculateSentiment(sentimentKind);
                videolink.add(tableName); videolink.add(videoResult.getVideoId());videolink.add(videoResult.getTitle());
            } else if(sentimentKind == movieSentment) {
                movieResult = calculateSentiment(sentimentKind);
                movielink.add(tableName); movielink.add(movieResult.getVideoId());movielink.add(movieResult.getTitle());
            }else if(sentimentKind == musicSentment) {
                musicResult = calculateSentiment(sentimentKind);
                musiclink.add(tableName); musiclink.add(musicResult.getVideoId());musiclink.add(musicResult.getTitle());
            }

        }
        catch (Exception e){
            Log.d(LOG_TAG, "## error: " +e.getLocalizedMessage());
        }

    }

    public Sentiment calculateSentiment(Sentiment[] video){
        Sentiment best = null;
        Double minScore = 1000.0;
        //System.out.println("before");
        for(int i = 0; i < video.length; i++) {
            if(video[i].videoId == "") break;
            Double score = Math.abs(video[i].positive - userSentiment.positive)*userSentiment.positive + Math.abs(video[i].negative - userSentiment.negative)*userSentiment.negative + Math.abs(video[i].neutral - userSentiment.neutral)*userSentiment.neutral + Math.abs(video[i].mixed - userSentiment.mixed)*userSentiment.mixed;
            if(score < minScore){
                minScore = score;
                best = video[i];
            }
        }
        //System.out.println("After");
        return best;
    }

    public class Sentiment {
        private String videoId = "a";
        private String title = "a";
        private Integer id = 0;
        private Double positive = 0.0;
        private Double negative = 0.0;
        private Double neutral = 0.0;
        private Double mixed = 0.0;
        private Double[] sentiment = new Double[5];


        public Sentiment(Double posi, Double nega, Double neut, Double mix, String vid, String vtitle, Integer dbid) {
            videoId = vid;
            title = vtitle;
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
        public Integer getId() { return id; }
    }

    private Future<ScanResult> Scan(String table_name){
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(table_name);

        return dynamoDBAsyncClient.scanAsync(scanRequest);
    }
}
