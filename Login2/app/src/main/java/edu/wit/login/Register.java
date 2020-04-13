package edu.wit.login;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

//reference: https://www.tutlane.com/tutorial/android/android-login-and-registration-screen-design
public class Register extends Activity {
    NetworkOPS connect = new NetworkOPS();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        TextView register = (TextView)findViewById(R.id.lnkLogin);
        register.setMovementMethod(LinkMovementMethod.getInstance());
        register.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
        });
        TextView create = findViewById(R.id.btnCA);
        create.setMovementMethod(LinkMovementMethod.getInstance());
        register.setOnClickListener(v -> {
            try {
                connect.write(findViewById(R.id.Username).toString(),findViewById(R.id.txtPassword).toString(),findViewById(R.id.txtEmail).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
        });

    }
}