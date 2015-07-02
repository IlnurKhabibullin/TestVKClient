package com.example.testvkclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PostFragment extends Fragment {

    public Post post;
    private DownloadImageTask dit;

    public static PostFragment newInstance(Post post) {
        Bundle args = new Bundle();
        args.putParcelable("post_on_click", post);
        PostFragment fragment = new PostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PostFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        post = args.getParcelable("post_on_click");

        View v = inflater.inflate(R.layout.fragment_post, container, false);

        dit = new DownloadImageTask((ImageView) v.findViewById(R.id.author_avatar));
        dit.executeAsyncTask(dit, post.getAuthor_avatar());

        ((TextView) v.findViewById(R.id.author_name)).setText(post.getAuthor_name());

        if (!post.getText().equals(""))
            ((TextView) v.findViewById(R.id.post_text)).setText(post.getText());
        else
            v.findViewById(R.id.post_text).setVisibility(View.GONE);

        if (post.getPost_type()) {

            dit = new DownloadImageTask((ImageView) v.findViewById(R.id.repost_source_avatar));
            dit.executeAsyncTask(dit, post.getRepost_source_avatar());

            ((TextView) v.findViewById(R.id.repost_source_name)).setText(post.getRepost_source_name());


            if (!post.getRepost_text().equals(""))
                ((TextView) v.findViewById(R.id.repost_text)).setText(post.getRepost_text());
            else
                v.findViewById(R.id.repost_text).setVisibility(View.GONE);

        } else {
            v.findViewById(R.id.repost_source_avatar).setVisibility(View.GONE);
            v.findViewById(R.id.repost_arrow_image).setVisibility(View.GONE);
            v.findViewById(R.id.repost_source_name).setVisibility(View.GONE);
        }
        if (post.getPost_photos() != null) {
            ViewPager gallery = (ViewPager) v.findViewById(R.id.gallery);
            gallery.setAdapter(new ImageAdapter(getActivity(), post.getPost_photos()));
        } else v.findViewById(R.id.gallery).setVisibility(View.GONE);

        Date date = new Date(post.getDate());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        ((TextView) v.findViewById(R.id.date)).setText(sdf.format(date));

        ((TextView) v.findViewById(R.id.likes)).setText(String.valueOf(post.getLikesCount()));

        return v;
    }
}
