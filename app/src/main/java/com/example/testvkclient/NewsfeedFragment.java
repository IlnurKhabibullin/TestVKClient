package com.example.testvkclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NewsfeedFragment extends Fragment {

    private ArrayAdapter<Post> listAdapter;
    private final List<Post> posts = new ArrayList<>();
    ListView listView;
    private String start_from = "";
    private DownloadImageTask dim;

    private VKRequest currentRequest;

    public static NewsfeedFragment newInstance() {
//        Bundle args = new Bundle();
        NewsfeedFragment fragment = new NewsfeedFragment();
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_newsfeed, container, false);


        Button logout_button = (Button)v.findViewById(R.id.logout_button);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKSdk.logout();
                VKSdk.authorize(VKScope.FRIENDS, VKScope.PHOTOS, VKScope.WALL);
            }
        });

        final SwipyRefreshLayout mSwipyRefreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.pull_to_refresh);
        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
//                Boolean loadMore = direction != SwipyRefreshLayoutDirection.TOP;
                mSwipyRefreshLayout.setRefreshing(true);
                startLoading(direction != SwipyRefreshLayoutDirection.TOP);
                mSwipyRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipyRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        listAdapter = new ArrayAdapter<Post>(getActivity().getApplicationContext(),
                R.layout.posts_list_item, R.id.list_post_text, posts) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                Post post = getItem(position);

                ((TextView) view.findViewById(R.id.list_author_name)).setText(post.getAuthor_name());

                dim = new DownloadImageTask((ImageView) view.findViewById(R.id.list_author_avatar));
                dim.executeAsyncTask(dim, post.getAuthor_avatar());
                ImageView iv = (ImageView) view.findViewById(R.id.list_post_photo);
                if (post.getPost_photos() != null) {
                    dim = new DownloadImageTask(iv);
                    dim.executeAsyncTask(dim, post.getPost_photos()[0]);
                } else
                    iv.setImageResource(R.drawable.empty_photo);

                if (!post.getText().equals("")) {
                    String text = post.getText();
                    if (text.length() > 100)
                        text = text.substring(0, 100) + " (...читать далее)";
                    ((TextView) view.findViewById(R.id.list_post_text)).setText(text);
                } else {
                    view.findViewById(R.id.list_post_text).setVisibility(View.INVISIBLE);
                }

                Date date = new Date(post.getDate());
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));
                ((TextView) view.findViewById(R.id.list_date)).setText(sdf.format(date));

                ((TextView) view.findViewById(R.id.list_likes)).setText(String.valueOf(post.getLikesCount()));
                return view;

            }

        };

        listView = ((ListView)v.findViewById(R.id.posts_list));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (VKSdk.wakeUpSession()) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, PostFragment.newInstance(listAdapter.getItem(position)))
                            .addToBackStack(null).commit();
                }
            }
        });
        listView.setAdapter(listAdapter);
        startLoading(false);

        return v;
    }

    private void startLoading(final Boolean loadMore) {
        if (start_from.length() > 0 && loadMore) {
            currentRequest = new VKRequest("newsfeed.get", VKParameters.from(VKApiConst.COUNT, 30,
                    "start_from", start_from, VKRequest.HttpMethod.GET));
        } else {
            currentRequest = new VKRequest("newsfeed.get", VKParameters.from(VKApiConst.COUNT, 30,
                    VKRequest.HttpMethod.GET));
            posts.clear();
        }
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);
                JSONObject res;
                JSONArray items;
                JSONArray profiles;
                JSONArray groups;

                try {
                    res = response.json.getJSONObject("response");
                    items = res.getJSONArray("items");
                    groups = res.getJSONArray("groups");
                    profiles = res.getJSONArray("profiles");
                    start_from = res.getString("next_from");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject post = items.getJSONObject(i);
                        if (!post.has("post_source"))
                            continue;
                        posts.add(getNewPost(profiles, groups, post));
                    }
                } catch (NullPointerException | JSONException e) {
                    e.printStackTrace();
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

    private Post getNewPost (JSONArray profiles, JSONArray groups, JSONObject post) throws JSONException{
        String[] publisherData = getPosterName(profiles, groups, post, "source_id");

        Post postToAdd = new Post();
        postToAdd.setAuthor_name(publisherData[0]);
        postToAdd.setAuthor_avatar(publisherData[1]);
        postToAdd.setDate(post.getLong("date"));
        postToAdd.setLikesCount(post.getJSONObject("likes").getInt("count"));
        postToAdd.setText(post.getString("text"));
        postToAdd.setPost_photos(getPhotosArray(post));
        postToAdd.setPost_type(false);
        if (post.has("copy_history")) {
            postToAdd.setPost_type(true);
            JSONObject copyHistory = (JSONObject)post.getJSONArray("copy_history").get(0);
            postToAdd.setRepost_text(copyHistory.getString("text"));
            publisherData = getPosterName(profiles, groups, copyHistory, "owner_id");
            postToAdd.setRepost_source_name(publisherData[0]);
            postToAdd.setRepost_source_avatar(publisherData[1]);
        }
        return postToAdd;
    }

    private String[] getPosterName(JSONArray profiles, JSONArray groups, JSONObject post, String id_tag) throws JSONException {
        JSONObject publisher = null;
        int id = post.getInt(id_tag);
        String[] publisherData = new String[2];
        if (id < 0) {
            id = -id;
            for (int j = 0; j < groups.length(); j++) {
                publisher = groups.getJSONObject(j);
                if (publisher.getInt("id") == id) {
                    break;
                }
            }
            publisherData[0] = publisher.getString("name");
        } else {
            for (int j = 0; j < profiles.length(); j++) {
                publisher = profiles.getJSONObject(j);
                if (publisher.getInt("id") == id)
                    break;
            }
            publisherData[0] = publisher.getString("first_name") + " " + publisher.getString("last_name");
        }
        publisherData[1] = publisher.getString("photo_50");

        return publisherData;
    }

    private String[] getPhotosArray(JSONObject post) throws JSONException {

        if (post.has("attachments")) {
            JSONArray photos = post.getJSONArray("attachments");
            ArrayList<String> post_photos = new ArrayList<>();
            for (int i = 0; i < photos.length(); i++) {
                JSONObject item = photos.getJSONObject(i);
                if (item.getString("type").equals("photo")) {
                    post_photos.add(item.getJSONObject("photo").getString("photo_130"));
                }
            }
            if (post_photos.size() > 0) {
                String[] photo_list = new String[post_photos.size()];
                for (int i = 0; i < post_photos.size(); i++) {
                    photo_list[i] = post_photos.get(i);
                }
                return photo_list;
            }
        } else if (post.has("copy_history")) {
            //TODO there could be just text in repost, so then JsonException appears. Need to check all possible variants of posts
            JSONArray photos = ((JSONObject)post.getJSONArray("copy_history").get(0)).getJSONArray("attachments");
            ArrayList<String> post_photos = new ArrayList<>();
            for (int i = 0; i < photos.length(); i++) {
                JSONObject item = photos.getJSONObject(i);
                if (item.getString("type").equals("photo"))
                    post_photos.add(item.getJSONObject("photo").getString("photo_130"));
            }
            if (post_photos.size() > 0) {
                String[] list = new String[post_photos.size()];
                for (int i = 0; i < list.length; i++) {
                    list[i] = post_photos.get(i);
                }
                return list;
            }
        }
        return null;
    }
}
