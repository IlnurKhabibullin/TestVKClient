package com.example.testvkclient;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


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

public class PostsFragment extends ListFragment {

    private ArrayAdapter<Post> listAdapter;
    private final List<Post> posts = new ArrayList<>();

    private VKRequest currentRequest;

    public PostsFragment() {
    }

    public static PostsFragment newInstance() {
//        Bundle args = new Bundle();
        PostsFragment fragment = new PostsFragment();
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listAdapter = new ArrayAdapter<Post>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, posts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                final Post post = getItem(position);

                ((TextView) view.findViewById(android.R.id.text1)).setText(post.getName());

                String birthDateStr = "Не задано";

                DateTime dt = post.getBirthDate();

                if (dt != null) {
                    birthDateStr = dt.toString(DateTimeFormat.forPattern(post.getDateFormat()));
                }

                ((TextView) view.findViewById(android.R.id.text2)).setText(birthDateStr);
                return view;

            }
        };


        // TODO: Change Adapter to display your content
        setListAdapter(listAdapter);
        startLoading();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }

    private void startLoading() {
        currentRequest = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,bdate"));
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);

                VKUsersArray usersArray = (VKUsersArray) response.parsedModel;
                posts.clear();
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
                    posts.add(new Post(userFull.toString(), birthDate, format));
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
