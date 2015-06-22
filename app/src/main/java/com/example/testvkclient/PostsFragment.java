package com.example.testvkclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PostsFragment extends Fragment {

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

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_posts, container, false);

        Button logout_button = (Button)v.findViewById(R.id.logout_button);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKSdk.logout();
                VKSdk.authorize(VKScope.FRIENDS,VKScope.PHOTOS,VKScope.WALL);
            }
        });

        listAdapter = new ArrayAdapter<Post>(getActivity().getApplicationContext(),
                R.layout.posts_list_item, R.id.post_text, posts) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);

                final Post post = getItem(position);

                ((TextView) view.findViewById(R.id.author_name)).setText(post.getAuthor_name());

                new DownloadImageTask((ImageView) view.findViewById(R.id.photo_50))
                        .execute(post.getPhoto_50());

                if (post.getPost_photos() != null) {
                    new DownloadImageTask((ImageView) view.findViewById(R.id.post_photo))
                            .execute(post.getPost_photos()[0]);
                }
                if (post.getText() != "")
                    ((TextView) view.findViewById(R.id.post_text)).setText(post.getText());
                else {
                    view.findViewById(R.id.post_text).setVisibility(View.GONE);
                }

                Date date = new Date(post.getDate() * 1000l);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));
                ((TextView) view.findViewById(R.id.date)).setText(sdf.format(date));

                ((TextView) view.findViewById(R.id.likes)).setText(String.valueOf(post.getLikesCount()));
                return view;

            }

        };

        ((ListView)v.findViewById(R.id.posts_list)).setAdapter(listAdapter);
        startLoading();

        return v;
    }

    /*@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }*/

    private void startLoading() {
        currentRequest = new VKRequest("newsfeed.get", VKParameters.from(VKApiConst.FIELDS, "date,text,likes_count,sex,photo_200,photo_50"),
                VKRequest.HttpMethod.GET, VKUsersArray.class);
        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("VkDemoApp", "onComplete " + response);
                JSONObject res;
                JSONArray items;
                JSONArray profiles;
                JSONArray groups;

                posts.clear();

                try {
                    res = response.json.getJSONObject("response");
                    items = res.getJSONArray("items");
                    groups = res.getJSONArray("groups");
                    profiles = res.getJSONArray("profiles");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject post;
                        if (items.getJSONObject(i).has("post_source"))
                            post = items.getJSONObject(i);
                        else continue;
                        JSONObject publisher = null;
                        int id = post.getInt("source_id");
                        if (id < 0) {
                            id = -id;
                            for (int j = 0; j < groups.length(); j++) {
                                publisher = groups.getJSONObject(j);
                                if (publisher.getInt("id") == id)
                                    break;
                            }
                            posts.add(new Post(publisher.getString("name"), post.getLong("date"), post.getString("text")
                                    , publisher.getString("photo_50"), getPostPhoto(post), post.getJSONObject("likes").getInt("count")));
                        } else {
                            for (int j = 0; j < profiles.length(); j++) {
                                publisher = profiles.getJSONObject(j);
                                if (publisher.getInt("id") == id)
                                    break;
                            }
                            String name = publisher.getString("first_name") + " " + publisher.getString("last_name");
                            posts.add(new Post(name, post.getLong("date"), post.getString("text")
                                    , publisher.getString("photo_50"), getPostPhoto(post), post.getJSONObject("likes").getInt("count")));
                        }
                    }
                } catch (JSONException e) {
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

    private String[] getPostPhoto(JSONObject post) throws JSONException {

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
        }
        return null;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
