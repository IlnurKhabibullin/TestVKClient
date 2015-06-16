package com.example.testvkclient;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {

    private static final String VK_APP_ID = "4960002";

    private final VKSdkListener sdkListener = new VKSdkListener() {

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            Log.d("VkDemoApp", "onAcceptUserToken " + token);
            startLoading();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            Log.d("VkDemoApp", "onReceiveNewToken " + newToken);
            startLoading();
        }

        @Override
        public void onRenewAccessToken(VKAccessToken token) {
            Log.d("VkDemoApp", "onRenewAccessToken " + token);
            startLoading();
        }

        @Override
        public void onCaptchaError(VKError captchaError) {
            Log.d("VkDemoApp", "onCaptchaError " + captchaError);
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            Log.d("VkDemoApp", "onTokenExpired " + expiredToken);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            Log.d("VkDemoApp", "onAccessDenied " + authorizationError);
        }

    };

    private VKRequest currentRequest;
    private Button loginButton;

    private final List<User> users = new ArrayList<>();
    private ArrayAdapter<User> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        listAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_2, android.R.id.text1, users) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                final User user = getItem(position);

                ((TextView) view.findViewById(android.R.id.text1)).setText(user.getName());

                String birthDateStr = "Не задано";

                DateTime dt = user.getBirthDate();

                if (dt != null) {
                    birthDateStr = dt.toString(DateTimeFormat.forPattern(user.getDateFormat()));
                }

                ((TextView) view.findViewById(android.R.id.text2)).setText(birthDateStr);
                return view;

            }
        };
        setListAdapter(listAdapter);

        VKSdk.initialize(sdkListener, VK_APP_ID);

        VKUIHelper.onCreate(this);

        loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VKSdk.authorize(VKScope.FRIENDS);
            }
        });

        if (VKSdk.wakeUpSession()) {
            startLoading();
        } else {
            loginButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
        if (currentRequest != null) {
            currentRequest.cancel();
        }
    }

    private void startLoading() {
        loginButton.setVisibility(View.GONE);
        if (currentRequest != null) {
            currentRequest.cancel();
        }
        currentRequest = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,bdate"));
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);

                VKUsersArray usersArray = (VKUsersArray) response.parsedModel;
                users.clear();
                final String[] formats = new String[]{"dd.MM.yyyy", "dd.MM"};

                for (VKApiUserFull userFull : usersArray) {
                    DateTime birthDate = null;
                    String format = null;
                    if (!TextUtils.isEmpty(userFull.bdate)) {
                        for (String f : formats) {
                            format = f;
                            try {
                                birthDate = DateTimeFormat.forPattern(format).parseDateTime(userFull.bdate);
                            } catch (Exception ignored) {
                            }
                            if (birthDate != null) {
                                break;
                            }
                        }

                    }
                    users.add(new User(userFull.toString(), birthDate, format));
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("VkDemoApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("VkDemoApp", "onError: " + error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkDemoApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }

}