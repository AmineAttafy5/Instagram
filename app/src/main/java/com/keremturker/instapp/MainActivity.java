package com.keremturker.instapp;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.keremturker.instapp.CustomViews.AuthenticationDialog;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements AuthenticationListener {

    private AuthenticationDialog dialog;
    Button btnlogin;

    SharedPreferences preferences = null;
    String token = null;

    TextView tv_name=null;
    ImageView img_pic=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnlogin = (Button) findViewById(R.id.btnlogin);
        tv_name = (TextView) findViewById(R.id.username);
        img_pic = (ImageView) findViewById(R.id.pro_pic);

        //check already  have  access token
        preferences = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        token = preferences.getString("token", null);
        if (token != null) {
            btnlogin.setText("LOGOUT");
            getUserInfoByAccessToken(token);

        } else {
            btnlogin.setText("INSTAGRAM LOGIN");
            findViewById(R.id.profile_layout).setVisibility(View.GONE);

        }

        //Get user information  by access token


    }

    private void getUserInfoByAccessToken(String token) {
        new RequestInstagramAPI().execute();
    }

    private class RequestInstagramAPI extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (response != null) {
                try {
                    JSONObject json = new JSONObject(response);
                    Log.d("response", json.toString());

                    //we need the user id

                    JSONObject jsonData=json.getJSONObject("data");
                    if (jsonData.has("id")){

                        String id=jsonData.getString("id");
                        //Save it in the shared preference

                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString("userID",id);
                        editor.apply();

                        //we can use the other data

                        String user_name=jsonData.getString("username");
                        String profile_pic=jsonData.getString("profile_picture");

                        tv_name.setText(user_name);
                        Picasso.get().load(profile_pic).into(img_pic);

                        findViewById(R.id.profile_layout).setVisibility(View.VISIBLE);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        protected String doInBackground(Void... voids) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(Constants.GET_USER_INFO_URL + token);
            try {

                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity httpEntity = response.getEntity();
                String json = EntityUtils.toString(httpEntity);
                return json;
            } catch (Exception e) {

            }
            return null;
        }
    }

    @Override
    public void onCodeReceived(String auth_token) {

        if (auth_token == null)
            return;
        // use the token  for further
        //Save the token in sharedpreference

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token",auth_token);
        editor.apply();
        token = auth_token;
        btnlogin.setText("LOGOUT");
        getUserInfoByAccessToken(token);


    }

    public void tikla(View view) {

        if (token != null) {

            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            btnlogin.setText("INSTAGRAM LOGIN");
            token = null;
            findViewById(R.id.profile_layout).setVisibility(View.GONE);



        } else {
            dialog = new AuthenticationDialog(this, this);
            dialog.setCancelable(true);
            dialog.show();
        }

    }
}
