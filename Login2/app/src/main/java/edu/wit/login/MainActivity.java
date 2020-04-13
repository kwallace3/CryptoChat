package edu.wit.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;

//reference: https://www.tutorialspoint.com/android/android_login_screen.htm
public class MainActivity extends AppCompatActivity {
    public void execute() {
        Executor executor = command -> {

        };
        executor.execute( () -> {
            try {
                final String user = findViewById(R.id.Username).toString();
                final String pass = findViewById(R.id.Password).toString();
                NetworkOPS connect = new NetworkOPS();
                if(connect.retrieve(user,pass).equals("login%7success%##username")){
                    Intent intent1 = new Intent(MainActivity.this, MainPage.class);
                    startActivity(intent1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView register = findViewById(R.id.CAbutton);
        register.setMovementMethod(LinkMovementMethod.getInstance());
        register.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });

        TextView login = findViewById(R.id.Lbutton);
        login.setMovementMethod(LinkMovementMethod.getInstance());
        login.setOnClickListener(v -> {
            Intent intent1 = new Intent(MainActivity.this, MainPage.class);
            startActivity(intent1);
            execute();
        });
}

}