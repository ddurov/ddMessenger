package com.eviger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class restoreProfile extends AppCompatActivity {

    public SharedPreferences tokenSet;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restore_profile);

        TextView actionView = findViewById(R.id.contentCap);
        TextView reasonView = findViewById(R.id.reason_restoreProfile);
        Button toRestore = findViewById(R.id.toEmailCheck_restoreProfile);

        String reason = "";
        String action = "";

        switch (getIntent().getStringExtra("reason")) {

            case "hacked":
                action = "заблокирован";
                reason = "взломан";
                break;

            case "too many authorizations":
                action = "заблокирован";
                reason = "большое кол-во входов за короткий промежуток";
                break;

            case "own request":
                action = "удалён";
                reason = "самостоятельное удаление аккаунта";
                break;

        }

        actionView.setText("Профиль " + action);
        if (getIntent().getBooleanExtra("canRestore", true)) {

            reasonView.setText("Ваш профиль был " + action + " по причине: " + reason + ". Для восстановления нажмите на кнопку ниже");
            toRestore.setOnClickListener(v -> {

                Intent in = new Intent(restoreProfile.this, emailConfirm.class);
                in.putExtra("type", "restore");
                in.putExtra("token", getIntent().getStringExtra("token"));

            });

        } else {

            reasonView.setText("Ваш профиль был " + action + " по причине: " + reason + ". Восстановлению аккаунт не подлежит, для выхода из аккаунта, пожалуйста нажмите на кнопку ниже");
            toRestore.setText("Выйти из аккаунта");
            toRestore.setOnClickListener(v -> {

                SharedPreferences.Editor tokenEditor = tokenSet.edit();
                tokenEditor.remove("token");
                tokenEditor.remove("isSigned");
                tokenEditor.apply();
                Intent intent = new Intent(restoreProfile.this, chooseAuth.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            });

        }

    }
}