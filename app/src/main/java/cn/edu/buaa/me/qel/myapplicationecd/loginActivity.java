package cn.edu.buaa.me.qel.myapplicationecd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class loginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //提示
                Toast.makeText(loginActivity.this, "cancel", Toast.LENGTH_SHORT).show();
            }
        });

        Button loginBtn = (Button)findViewById(R.id.login_in);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(loginActivity.this, "login", Toast.LENGTH_SHORT).show();
                // TODO Auto-generated method stub
                //这里就可以判断账号密码是否正确了，这里让大家自己试验动手一下谢谢如果账号密码是admin 123456就成功
                //否则就提示登陆失败，大家试一试吧，我这里直接跳转了，没做验证


                //这个是直接跳转到MainActivity
                Intent intent = new Intent();
                intent.setClass(loginActivity.this, ECDActivity.class);
                startActivity(intent);
                finish();   //登录后就把登录界面关掉
            }
        });
    }


}
