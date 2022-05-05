package com.example.messengerandroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.messengerandroid.model.request.Result;
import com.example.messengerandroid.model.request.registration.Account;
import com.example.messengerandroid.model.request.registration.Registration;
import com.example.messengerandroid.model.request.registration.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

    }

    public void registrationUser(final View view) {
        final EditText nameEditText = findViewById(R.id.registrationUserName);
        final DatePicker birthDateEditText = findViewById(R.id.registrationBirthDate);//
        final EditText loginEditText = findViewById(R.id.registrationAccountLogin);
        final EditText passwordEditText = findViewById(R.id.registrationAccountPassword);
        final EditText confirmPasswordEditText = findViewById(R.id.registrationAccountConfirmPassword);
        final EditText emailEditText = findViewById(R.id.registrationAccountEmail);
        boolean valid = validateRegistrationRequest(nameEditText, loginEditText, passwordEditText, confirmPasswordEditText, emailEditText);
        if (!valid) {
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Result> resultFuture = executorService.submit(new Callable<Result>() {
            @Override
            public Result call() {
                HttpClient client = HttpClientBuilder.create()
                        .build();
                Registration registration = createRegistrationRequest(nameEditText, birthDateEditText, loginEditText, passwordEditText, emailEditText);
                return sendRegistrationRequest(client, registration);
            }
        });

        try {
            Result result = resultFuture.get();
            if (result != null) {
                if (result.getCode().equals("USER_ALREADY_EXIST")) {
                    loginEditText.setError(result.getMessage());
                }
            } else {
                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                startActivity(intent);
            }


        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean validateRegistrationRequest(final EditText nameEditText, final EditText loginEditText, final EditText passwordEditText, final EditText confirmPasswordEditText, final EditText emailEditText) {
        if (nameEditText.getText().length() == 0) {
            nameEditText.setError(getResources().getString(R.string.registration_empty_name_error));
            return false;
        }
        if (loginEditText.getText().length() == 0) {
            loginEditText.setError(getResources().getString(R.string.registration_empty_login_error));
            return false;
        }
        if (passwordEditText.getText().length() == 0) {
            passwordEditText.setError(getResources().getString(R.string.registration_empty_password_error));
            return false;
        }
        if (!confirmPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())) {
            confirmPasswordEditText.setError(getResources().getString(R.string.registration_empty_confirm_password_error));
            return false;
        }
        if (emailEditText.getText().length() == 0) {
            emailEditText.setError(getResources().getString(R.string.registration_empty_email_error));
            return false;
        }

        return true;
    }

    @NonNull
    private Registration createRegistrationRequest(final EditText nameEditText, final DatePicker birthDateEditText, final EditText loginEditText, final EditText passwordEditText, final EditText emailEditText) {
        User user = new User();
        user.setName(nameEditText.getText().toString());
        int day = birthDateEditText.getDayOfMonth();
        int month = birthDateEditText.getMonth();
        int year = birthDateEditText.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        user.setBirthDate(calendar.getTime());

        Account account = new Account();
        account.setLogin(loginEditText.getText().toString());
        account.setPassword(passwordEditText.getText().toString());
        account.setEmail(emailEditText.getText().toString());

        Registration registration = new Registration();
        registration.setUserDto(user);
        registration.setAccountDto(account);
        return registration;
    }

    private Result sendRegistrationRequest(final HttpClient client, final Registration registration) {
        HttpPost request = new HttpPost("/users");
        request.setEntity(new StringEntity(
                new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .create()
                        .toJson(registration)));
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try {
            ClassicHttpResponse response = client.execute(new HttpHost("192.168.0.107", 8080), request);
            if (response.getCode() != HttpStatus.SC_OK) {
                return readResponse(response);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private Result readResponse(ClassicHttpResponse response) throws IOException {
        InputStream inputStream = response.getEntity().getContent();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        return new Gson().fromJson(bufferedReader.readLine(), Result.class);
    }
}
