package com.aion.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import java.net.URI;

/** Lets each installation choose its own server without rebuilding the APK. */
final class ServerAddressDialog {
    interface Callback { void connect(String url); }
    private ServerAddressDialog() {}

    static void show(Activity activity, SharedPreferences prefs, String key,
                     String defaultUrl, Callback callback) {
        EditText input = new EditText(activity);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(prefs.getString(key, defaultUrl));
        input.selectAll();
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("连接你的 AionsHome")
                .setMessage("填写电脑上的服务地址，例如 http://192.168.1.100:8080/chat")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("连接", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    try {
                        String value = input.getText().toString().trim();
                        URI uri = new URI(value);
                        if ((!"http".equalsIgnoreCase(uri.getScheme())
                                && !"https".equalsIgnoreCase(uri.getScheme()))
                                || uri.getHost() == null || uri.getUserInfo() != null
                                || uri.getQuery() != null || uri.getFragment() != null
                                || uri.getPort() > 65535) {
                            throw new IllegalArgumentException("Invalid server URL");
                        }
                        if (uri.getPath() == null || uri.getPath().isEmpty()
                                || "/".equals(uri.getPath())) {
                            value = new URI(uri.getScheme(), null, uri.getHost(),
                                    uri.getPort(), "/chat", null, null).toString();
                        }
                        prefs.edit().putString(key, value).apply();
                        dialog.dismiss();
                        callback.connect(value);
                    } catch (Exception exception) {
                        Toast.makeText(activity, "请输入有效的 http:// 或 https:// 服务地址",
                                Toast.LENGTH_LONG).show();
                    }
                }));
        dialog.show();
    }
}
