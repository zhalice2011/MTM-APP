package com.ruiqi.mtm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import android.util.Log;
import android.content.SharedPreferences;

import android.app.ActivityManager;

import org.json.JSONException;
import org.json.JSONObject;
public class loginActivity extends AppCompatActivity {
    private Button btn;
    private EditText account;
    private EditText pwd;
    private String ermsg;
    private CheckBox isremember;
    private SharedPreferences logininfor;


    public void cancelQuit(){
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            System.exit(0);
        } else {// android2.1
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            am.restartPackage(getPackageName());
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            cancelQuit();
            return false;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn = (Button) findViewById(R.id.login);
        account = (EditText) findViewById(R.id.account);
        pwd = (EditText) findViewById(R.id.password);
        isremember=(CheckBox) findViewById(R.id.isremember);
        SharedPreferences readdata=getSharedPreferences("login",0) ;
        String appid=readdata.getString("appid","");
        String appkey=readdata.getString("appkey","");
        String storeid=readdata.getString("storeid","");
        if(!appid.equals("") && !appkey.equals("") && !storeid.equals("")){
            Bundle userinfor = new Bundle();
            userinfor.putString("appid", appid);
            userinfor.putString("appkey",appkey);
            userinfor.putString("storeid",storeid);
            Intent main = new Intent(loginActivity.this, MainActivity.class);
            main.putExtras(userinfor);
            startActivity(main);
            finish();
        }
    }
    public void loginOnClick(View v){
        String username = account.getText().toString();
        String pass = pwd.getText().toString();
        Log.e("logc",username);
        Log.e("logc",pass);
        if (TextUtils.isEmpty(username)) {
            ermsg = "账号不能为空！";
            Toast toast=Toast.makeText(loginActivity.this, ermsg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP,80,150);
            toast.show();
            account.requestFocus()
            ;
        } else if (TextUtils.isEmpty(pass)) {
            ermsg = "密码不能为空！";
            Toast toast=Toast.makeText(loginActivity.this, ermsg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP,80,150);
            toast.show();
            pwd.requestFocus();
        } else {
            String servers = getResources().getString(R.string.server_url);
            httpusrVerify(username, pass, servers);
        }
    }
    //用户登录验证
    public void httpusrVerify(String user, final String pwd, String servers){
        String retrunmsg;
        String urls = servers + "/web/account/login";
        OkHttpClient mycleint = new OkHttpClient();
        //RequestBody body=RequestBody.create(form,data);
        RequestBody body = new FormBody.Builder().add("login_device", "app").add("username", user).add("password", pwd).build();
        Request myrq = new Request.Builder()
                .url(urls)
                .post(body)
                .build();
        mycleint.newCall(myrq).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("账号验证结果e", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Log.v("账号验证结果o", response.toString());
                final String resdata=response.body().string();
                Log.v("服务器数据",resdata);
                if (response.code() != 200) {
                    loginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toastmsg("无效的账号或密码!");
                        }
                    });
                }else{
                    //进入main
                    try {
                        JSONObject userjs = new JSONObject(resdata);
                        Bundle userinfor = new Bundle();
                        JSONObject returndata = userjs.getJSONObject("data");
                        writeinfor(returndata);
                        userinfor.putString("appid", returndata.getString("appid").toString());
                        userinfor.putString("appkey", returndata.getString("appkey").toString());
                        userinfor.putString("storeid", returndata.getString("storeid").toString());
                        Intent main = new Intent(loginActivity.this, MainActivity.class);
                        main.putExtras(userinfor);
                        startActivity(main);
                        finish();
                    }catch (JSONException e){
                        Log.v("bundle数据err", e.toString());
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public void writeinfor(JSONObject userinformation){
        logininfor=getSharedPreferences("login",0);
        Log.v("定位", String.valueOf(isremember.isChecked()));
        SharedPreferences.Editor wr=logininfor.edit();
        if (isremember.isChecked()){
            try {
                wr.putString("appid", userinformation.getString("appid").toString());
                wr.putString("appkey", userinformation.getString("appkey").toString());
                wr.putString("storeid", userinformation.getString("storeid").toString());
                wr.commit();
                Log.v("定位", "write file ok");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    };
    public void toastmsg(String msg){
        Toast toast= Toast.makeText(loginActivity.this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP,0,100);
        toast.show();
    }
}
