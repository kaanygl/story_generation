package com.example.story;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    TextView promptText;
    Button button;
    TextView result;
    String audience ="audience";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        promptText = findViewById(R.id.prompt);
        button = findViewById(R.id.button);
        result = findViewById(R.id.result);
        result.setMovementMethod(new ScrollingMovementMethod());

        Spinner spinner = (Spinner) findViewById(R.id.audience);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.audience,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = promptText.getText().toString();
                if (!prompt.isEmpty()) {
                    sendRequestToAPI(prompt);
                }
            }
        });

    }




    private void sendRequestToAPI(String prompt) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.groq.com/openai/v1/chat/completions";
        String final_prompt = "write a short story about" + prompt + "for"+ audience;
        // JSON body
        String full_prompt;
        String jsonBody = "{ \"messages\": [{ \"role\": \"user\", \"content\": \"" + final_prompt + "\" }], \"model\": \"llama3-groq-70b-8192-tool-use-preview\" }";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer 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")  // Add Authorization header
                .addHeader("Content-Type", "application/json")  // Add Content-Type header
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> result.setText("Request failed"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> displayResponse(responseData));
                } else {
                    runOnUiThread(() -> result.setText("Error: " + response.message()));
                }
            }
        });
    }
    private void displayResponse(String jsonResponse) {
        // Extract the story content from the JSON response
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            result.setText(content); // Set the story content to the result EditText
        } catch (Exception e) {
            e.printStackTrace();
            result.setText("Error parsing response");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        audience = adapterView.getItemAtPosition(i).toString();
        //Toast.makeText(getApplicationContext(), choice, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //Toast.makeText(getApplicationContext(), audience, Toast.LENGTH_LONG).show();
    }
}