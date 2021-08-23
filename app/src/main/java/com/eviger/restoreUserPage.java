package com.eviger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class restoreUserPage extends AppCompatActivity {

    String reason;
    String action;
    TextView reasonView;
    TextView blockActionText;
    Button buttonToRestore;

    public SharedPreferences tokenSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_user_page);

        reason = getIntent().getStringExtra("reason");
        action = "";
        reasonView = findViewById(R.id.reason);
        blockActionText = findViewById(R.id.blockText);
        buttonToRestore = findViewById(R.id.restorePage);

        switch (getIntent().getStringExtra("reason")) {

            case "token inactive due hacking":

                action = "заблокирован";
                reason = "взлом профиля";

            break;

            case "token inactive due delete profile at own request":

                action = "удалён";
                reason = "по собственному желанию";

            break;

        }

        blockActionText.setText("Пользователь "+action);
        if (!getIntent().getBooleanExtra("canRestore", true)) {

            reasonView.setText("Ваш профиль был удалён по причине: "+reason+". Восстановлению аккаунт не подлежит, для выхода из аккаунта, пожалуйста нажмите на кнопку ниже.");
            buttonToRestore.setBackgroundResource(R.drawable.button_negative);
            buttonToRestore.setText("Выйти из аккаунта");
            buttonToRestore.setOnClickListener(v -> {

                SharedPreferences.Editor ed = tokenSet.edit();
                ed.putBoolean("isSigned", false);
                ed.apply();
                Intent intent = new Intent(restoreUserPage.this, chooseAuth.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            });

        } else {

            reasonView.setText("Ваш профиль был удалён по причине: "+reason+". Для восстановления нажмите на кнопку ниже.");
            buttonToRestore.setOnClickListener(v -> {

                Intent in = new Intent(restoreUserPage.this, emailConfirm.class);
                in.putExtra("type", "restore");
                in.putExtra("token", getIntent().getStringExtra("token"));

            });

        }

    }
}