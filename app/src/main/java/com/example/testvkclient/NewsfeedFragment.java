package com.example.testvkclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
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
import java.util.TimeZone;

public class NewsfeedFragment extends Fragment {

    private ArrayAdapter<Post> listAdapter;
    public final ArrayList<Post> posts = new ArrayList<>();
    ListView listView;
    public String start_from = "";
    public int current_position = 0;
    private DownloadImageTask dit;

    public static NewsfeedFragment newInstance() {
        return new NewsfeedFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList("list_of_posts", posts);
        savedInstanceState.putString("start_from", start_from);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            posts.addAll((ArrayList)savedInstanceState.getParcelableArrayList("list_of_posts"));
            start_from = savedInstanceState.getString("start_from");
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_newsfeed, container, false);


        final SwipyRefreshLayout mSwipyRefreshLayout = (SwipyRefreshLayout) v.findViewById(R.id.pull_to_refresh);
        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                mSwipyRefreshLayout.setRefreshing(true);
                startLoading(direction != SwipyRefreshLayoutDirection.TOP);
                mSwipyRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipyRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        listAdapter = new ArrayAdapter<Post>(getActivity().getApplicationContext(),
                R.layout.posts_list_item, R.id.list_post_text, posts) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
//                иногда в элементах не отображаются TextView, хотя данные загружены. Возможно это
//                из-за ассинхронности загрузки данных и заполнения списка, но текст не появляется
//                и в том случае, когда листаешь список, вызывая getView снова. Только когда переходишь к отдельному
//                посту, или обновляешься через pullToRefresh, текст появляется.

                View view = super.getView(position, convertView, parent);

                Post post = getItem(position);

                dit = new DownloadImageTask((ImageView) view.findViewById(R.id.list_author_avatar));
                dit.executeAsyncTask(dit, post.getAuthor_avatar());

                ((TextView) view.findViewById(R.id.list_author_name)).setText(post.getAuthor_name());

                String text = post.getText();
                if (!(text.equals("") || text == null)) {
                    if (text.length() > 140)
                        text = text.substring(0, 140) + " (...читать далее)";
                    ((TextView) view.findViewById(R.id.list_post_text)).setText(text);
                } else {
                    view.findViewById(R.id.list_post_text).setVisibility(View.GONE);
                }

                String description = "";
                if (post.getPost_type()) {
                    description = "Репост от \"" + post.getRepost_source_name() + "\". ";
                }
                if (post.getPost_photos() != null) {
                    description += "В посте есть фото.";
                }
                if (!description.equals(""))
                    ((TextView) view.findViewById(R.id.repost_description)).setText(description);
                else
                    view.findViewById(R.id.repost_description).setVisibility(View.GONE);

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
                            .replace(R.id.fragmentContainer, PostFragment.newInstance(listAdapter.getItem(position))
                                    , "POST_FRAGMENT")
                            .addToBackStack(null).commit();
                }
            }
        });
        listView.setAdapter(listAdapter);
        if (posts.isEmpty())
            startLoading(false);
        return v;
    }

    private void startLoading(final Boolean loadMore) {
        VKRequest currentRequest;
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
                        setNewPost(profiles, groups, post);
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

    private void setNewPost(JSONArray profiles, JSONArray groups, JSONObject item) throws JSONException{
        String[] publisherData = getPosterName(profiles, groups, item, "source_id");

        Post postToAdd = new Post();
        postToAdd.setAuthor_name(publisherData[0]);
        postToAdd.setAuthor_avatar(publisherData[1]);
        postToAdd.setDate(item.getLong("date"));
        postToAdd.setLikesCount(item.getJSONObject("likes").getInt("count"));
        postToAdd.setText(item.getString("text"));
        postToAdd.setPost_type(false);
        if (item.has("copy_history")) {
            postToAdd.setPost_type(true);
            JSONObject copyHistory = (JSONObject)item.getJSONArray("copy_history").get(0);
            if (copyHistory.has("attachments")) {
                postToAdd.setPost_photos(getPhotosArray(copyHistory.getJSONArray("attachments")));
                if (postToAdd.getPost_photos() != null && postToAdd.getPost_photos().length == 0) return;
            }
            postToAdd.setRepost_text(copyHistory.getString("text"));
            publisherData = getPosterName(profiles, groups, copyHistory, "owner_id");
            postToAdd.setRepost_source_name(publisherData[0]);
            postToAdd.setRepost_source_avatar(publisherData[1]);
        } else if (item.has("attachments")) {
            postToAdd.setPost_photos(getPhotosArray(item.getJSONArray("attachments")));
            if (postToAdd.getPost_photos() != null && postToAdd.getPost_photos().length == 0) return;
        }
        posts.add(postToAdd);
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

    private String[] getPhotosArray(JSONArray attachments) throws JSONException {
        ArrayList<String> post_photos = new ArrayList<>();
        for (int i = 0; i < attachments.length(); i++) {
            JSONObject item = attachments.getJSONObject(i);
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
        return null;
    }

    public Boolean scrollUp () {
        if (listView.getFirstVisiblePosition() != 0) {
            current_position = listView.getLastVisiblePosition();
            System.out.println("current position " + current_position);
            listView.smoothScrollToPosition(0);
            return true;
        } else {
            listView.smoothScrollToPosition(current_position);
            return false;
        }
    }
}
