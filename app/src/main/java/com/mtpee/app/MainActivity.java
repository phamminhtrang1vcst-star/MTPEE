package com.mtpee.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.content.ComponentName;

import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends Activity {
    private static final int PICK_FILE = 100;
    private ValueCallback<Uri[]> pendingFileSelection;
private ListenableFuture<MediaController> playbackControllerFuture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {
                if (pendingFileSelection != null) {
                    pendingFileSelection.onReceiveValue(null);
                }
                pendingFileSelection = callback;

                try { Intent picker = params.createIntent();

if (params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
    picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    picker.addCategory(Intent.CATEGORY_OPENABLE);
    picker.setType("audio/*");
    picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
}

startActivityForResult(picker, PICK_FILE);} catch (ActivityNotFoundException e) {
                    pendingFileSelection = null;
                    callback.onReceiveValue(null);
                }
                return true;
            }
        });
        webView.setOnApplyWindowInsetsListener((view, insets) -> {
    if (android.os.Build.VERSION.SDK_INT >= 30
            && getApplicationInfo().targetSdkVersion >= 35
            && android.os.Build.VERSION.SDK_INT >= 35) {
        android.graphics.Insets bars = insets.getInsets(
                android.view.WindowInsets.Type.systemBars()
                | android.view.WindowInsets.Type.displayCutout());
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
    }
    return insets;
});

        setContentView(webView);
        webView.loadUrl("file:///android_asset/MTPEE_Anh_Nen.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE && pendingFileSelection != null) {
            ValueCallback<Uri[]> callback = pendingFileSelection;
            pendingFileSelection = null;
            if (resultCode == RESULT_OK && data != null && data.getClipData() != null) {
    android.content.ClipData selection = data.getClipData();
    Uri[] files = new Uri[selection.getItemCount()];

    for (int i = 0; i < files.length; i++) {
        files[i] = selection.getItemAt(i).getUri();
    }

    callback.onReceiveValue(files);
} else {
    callback.onReceiveValue(
            WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                               }
        }
    }
@Override
protected void onStart() {
    super.onStart();

    SessionToken token = new SessionToken(
            this, new ComponentName(this, PlaybackService.class));
    playbackControllerFuture = new MediaController.Builder(this, token)
            .buildAsync();
}

@Override
protected void onStop() {
    if (playbackControllerFuture != null) {
        MediaController.releaseFuture(playbackControllerFuture);
        playbackControllerFuture = null;
    }

    super.onStop();
}}
