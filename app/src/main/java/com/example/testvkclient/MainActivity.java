package com.example.testvkclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;

public class MainActivity extends FragmentActivity {

    private static final String VK_APP_ID = "4960002";
    Toolbar toolbar;

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

    public FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();

        VKUIHelper.onCreate(this);

        fm = getSupportFragmentManager();

        VKSdk.initialize(sdkListener, VK_APP_ID);
        VKSdk.authorize(VKScope.FRIENDS,VKScope.PHOTOS,VKScope.WALL);

        if (VKSdk.wakeUpSession()) {
            if (savedInstanceState == null)
                startLoading();
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
    }

    public void startLoading() {
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = NewsfeedFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment, "NEWSFEED_FRAGMENT")
                    .commit();
        }
    }


    private void initToolbar() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Новости");
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
//                    если выйти из аккаунта и зайти с другого, то данные предыдущего останутся.
//                    можно очищать данные:
//                    NewsfeedFragment news_fragment = (NewsfeedFragment) getSupportFragmentManager()
//                            .findFragmentByTag("NEWSFEED_FRAGMENT");
//                    if (news_fragment != null && news_fragment.isVisible()) {
//                        news_fragment.posts.clear();
//                        news_fragment.start_from = "";
//                    } else {
//                        PostFragment post_fragment = (PostFragment) getSupportFragmentManager()
//                                .findFragmentByTag("POST_FRAGMENT");
//                        if (post_fragment != null && post_fragment.isVisible()) {
//                            post_fragment.post = null;
//                        }
//                    }
//                    но тогда после входа в аккаунт будет пустой список и его нужно будет обновить вручную (например через
//                    pull to refresh)
//                    возможно, VKSdk.authorize выполняется отдельно и обновление ниже срабатывает с еще не авторизованным
//                    пользователем, потому что обновление в коде не работает.
                    VKSdk.logout();
                    VKSdk.authorize(VKScope.FRIENDS, VKScope.PHOTOS, VKScope.WALL);
//                    вот это обновление:
//                    if (VKSdk.wakeUpSession()) {
//                        startLoading();
//                    }
                    return true;
                } else if (menuItem.getItemId() == R.id.action_scroll) {
                    NewsfeedFragment news_fragment = (NewsfeedFragment) getSupportFragmentManager()
                            .findFragmentByTag("NEWSFEED_FRAGMENT");
                    if (news_fragment != null && news_fragment.isVisible()) {
                        if (news_fragment.scrollUp())
                            menuItem.setIcon(R.drawable.scroll_arrow_reverse);
                        else menuItem.setIcon(R.drawable.scroll_arrow);
                    }
                }

                return false;
            }
        });
        toolbar.inflateMenu(R.menu.menu_main);
    }
}