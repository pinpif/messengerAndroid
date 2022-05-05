package com.example.messengerandroid;

import com.example.messengerandroid.model.request.Result;

public interface HttpRequestCallback {
    void onComplete(Result result, int statusCode);
}
