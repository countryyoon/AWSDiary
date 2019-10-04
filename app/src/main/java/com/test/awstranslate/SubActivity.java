package com.test.awstranslate;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.handlers.AsyncHandler;

import com.amazonaws.services.comprehend.model.DetectEntitiesRequest;
import com.amazonaws.services.comprehend.model.DetectEntitiesResult;
import com.amazonaws.services.translate.AmazonTranslateAsyncClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.amazonaws.services.comprehend.AmazonComprehendAsyncClient;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


import android.app.Activity;
import android.content.Entity;
import android.util.Log;
import android.os.Bundle;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class SubActivity extends Activity {
    private static final String LOG_TAG = SubActivity.class.getSimpleName();
    String translateResult;
    String comprehendResult;
    String mainSentiment = "";
    Sentiment userSentiment = new Sentiment(null, null, null, null,"","",0);
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

    TextView viewDatePick;  //  viewDatePick - 선택한 날짜를 보여주는 textView
    EditText edtDiary;   //  edtDiary - 선택한 날짜의 일기를 쓰거나 기존에 저장된 일기가 있다면 보여주고 수정하는 영역
    Button btnAnalysis, btnBack, btnResult;   //  btnSave - 선택한 날짜의 일기 저장 및 수정(덮어쓰기) 버튼
    TextView analysisingtext; //+
    ProgressBar progressBar; //+

    String fileName;   //  fileName - 돌고 도는 선택된 날짜의 파일 이름
    ArrayList<String> resultlist=new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        setTitle("오늘 일기, 감정 미디어");

        Intent intent = new Intent(this.getIntent());
        int[] pickDate = intent.getIntArrayExtra("pickDate");
        int year = pickDate[0];
        int monthOfYear = pickDate[1] + 1;
        int dayOfMonth = pickDate[2];

        edtDiary = (EditText) findViewById(R.id.edtDiary);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnResult = (Button) findViewById(R.id.btnResult);
        btnAnalysis = (Button) findViewById(R.id.btnAnalysis);
        viewDatePick = (TextView) findViewById(R.id.viewDatePick);

        progressBar = (ProgressBar) findViewById(R.id.progressBar); //+
        analysisingtext = (TextView) findViewById(R.id.analysis); //+


        checkedDay(year, monthOfYear, dayOfMonth);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(SubActivity.this, MainActivity.class);
                startActivity(ii);
            }
        });

        btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "결과를 분석 중 입니다.", Toast.LENGTH_LONG).show();
                saveDiary(fileName);
                progressBar.setVisibility(View.VISIBLE);
                analysisingtext.setVisibility(View.VISIBLE);
                translateText(edtDiary.getText().toString());//확인하기

            }
        });

        btnAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateText(edtDiary.getText().toString());
                Intent iii = new Intent(SubActivity.this, Analysis2Activity.class);
                iii.putStringArrayListExtra("analysis",resultlist);
                startActivity(iii);
            }
        });

        /*btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateText(edtDiary.getText().toString());
                Intent iii = new Intent(SubActivity.this, AnalysisActivity.class);
                iii.putStringArrayListExtra("analysis",resultlist);
                startActivity(iii);
            }
        });*/

        }


    private void checkedDay(int year, int monthOfYear, int dayOfMonth) {

        // 받은 날짜로 날짜 보여주는
        viewDatePick.setText(year + " . " + monthOfYear + " . " + dayOfMonth);

        // 파일 이름을 만들어준다. 파일 이름은 "20170318.txt" 이런식으로 나옴
        fileName = year + "" + monthOfYear + "" + dayOfMonth + ".txt";

        // 읽어봐서 읽어지면 일기 가져오고
        // 없으면 catch 그냥 살아? 아주 위험한 생각같다..
        FileInputStream fis = null;
        try {
            fis = openFileInput(fileName);

            byte[] fileData = new byte[fis.available()];
            fis.read(fileData);
            fis.close();

            String str = new String(fileData, "UTF-8");
            // 읽어서 토스트 메시지로 보여줌
            Toast.makeText(getApplicationContext(), "일기 써둔 날", Toast.LENGTH_SHORT).show();

            edtDiary.setText(str);
            //translateText(str);

            //btnSave.setText("수정하기");
        } catch (Exception e) { // UnsupportedEncodingException , FileNotFoundException , IOException
            // 없어서 오류가 나면 일기가 없는 것 -> 일기를 쓰게 한다.
            Toast.makeText(getApplicationContext(), "일기 없는 날", Toast.LENGTH_SHORT).show();
            edtDiary.setText("");
            //btnSave.setText("새 일기 저장");
            e.printStackTrace();
        }

    }

    private void translateText(String diaryText) {

        AmazonTranslateAsyncClient translateAsyncClient = new
                AmazonTranslateAsyncClient(awsCredentials);

        TranslateTextRequest translateTextRequest = new TranslateTextRequest()
                .withText(diaryText)
                .withSourceLanguageCode("ko")
                .withTargetLanguageCode("en");

        translateAsyncClient.translateTextAsync(translateTextRequest, new
                AsyncHandler<TranslateTextRequest, TranslateTextResult>() {
                    @Override
                    public void onError(Exception e) {
                        Log.e(LOG_TAG, "## Error occurred in translating the text: " +
                                e.getLocalizedMessage());
                    }

                    @Override
                    public void onSuccess(TranslateTextRequest request, TranslateTextResult
                            translateTextResult) {
                        Log.d(LOG_TAG, "## Original Text: " + request.getText());
                        Log.d(LOG_TAG, "## Translated Text: " +
                                translateTextResult.getTranslatedText());
                        translateResult = translateTextResult.getTranslatedText();

                        comprehend();

                        if(mainSentiment.equals("NEGATIVE")){// 부정적 감정이면
                            Intent iii = new Intent(SubActivity.this, NegativeActivity.class);
                            iii.putExtra("SentResult", comprehendResult);
                            startActivity(iii);
                        }
                        else{//그 외에 MIXED, POSITIVE, NEUTRAL은 다음 페이지에서 그대로 전달!
                            Intent iii = new Intent(SubActivity.this, AnalysisActivity.class);
                            iii.putExtra("SentResult", comprehendResult);
                            startActivity(iii);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        analysisingtext.setVisibility(View.INVISIBLE);
                        //finish();
                    }
                });

    }

    private void comprehend() {

        AmazonComprehendAsyncClient comprehendAsyncClient = new
                AmazonComprehendAsyncClient(awsCredentials);

        // Call detectSentiment API
        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(translateResult)
                .withLanguageCode("en");
        DetectSentimentResult detectSentimentResult = comprehendAsyncClient.detectSentiment(detectSentimentRequest);
        System.out.println("##" + detectSentimentResult);
        comprehendResult = detectSentimentResult.toString();
        parsSentiment(comprehendResult);

        DetectEntitiesRequest detectEntitiesRequest = new DetectEntitiesRequest().withText(translateResult)
                .withLanguageCode("en");

        DetectEntitiesResult detectEntitiesResult = comprehendAsyncClient.detectEntities(detectEntitiesRequest);
        System.out.println("##" + detectEntitiesResult);
        //System.out.println("cast=##" +(LOCATION) detectEntitiesResult);

        Gson gson=new Gson();
        JsonParser jsonParser=new JsonParser();
        List item=detectEntitiesResult.getEntities();
        JsonElement jsonElement=jsonParser.parse(gson.toJson(item));
        //System.out.println("2++"+ jsonElement.toString());

        for(JsonElement i:jsonElement.getAsJsonArray()){
            resultlist.add(i.getAsJsonObject().get("text").toString());
            System.out.println("3++"+i.getAsJsonObject().get("text"));
        }

    }

    public void parsSentiment(String parseText){

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



    // 일기 저장하는 메소드
    private void saveDiary(String readDay) {

        try {
            String content = edtDiary.getText().toString();
            Context context = getApplicationContext();
            String folder = context.getFilesDir().getAbsolutePath();

            File subFolder = new File(folder);

            if (!subFolder.exists()) {
                subFolder.mkdirs();
            }

            FileOutputStream outputStream = new FileOutputStream(new File(subFolder, fileName));

            outputStream.write(content.getBytes());
            outputStream.close();

            //translateText(content);
            //String filePath = folder + File.separator + fileName;

            // getApplicationContext() = 현재 클래스.this ?
            Toast.makeText(getApplicationContext(), "일기 저장됨", Toast.LENGTH_SHORT).show();

        } catch (Exception e) { // Exception - 에러 종류 제일 상위 // FileNotFoundException , IOException
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "오류오류", Toast.LENGTH_SHORT).show();
        }
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
        public Integer getId() {
            return id;
        }
    }

}
