package com.example.testvkclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PostFragment extends Fragment {

    private Post post;
    private DownloadImageTask dim;

    public static PostFragment newInstance(Post post) {
        Bundle args = new Bundle();
        args.putSerializable("post_on_click", post);
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
        post = (Post) args
                .getSerializable("post_on_click");

        View v = inflater.inflate(R.layout.fragment_post, container, false);

        dim = new DownloadImageTask((ImageView) v.findViewById(R.id.author_avatar));
        dim.executeAsyncTask(dim, post.getAuthor_avatar());

        ((TextView) v.findViewById(R.id.author_name)).setText(post.getAuthor_name());

        if (!post.getText().equals(""))
            ((TextView) v.findViewById(R.id.post_text)).setText(post.getText());
        else
            v.findViewById(R.id.post_text).setVisibility(View.GONE);

        if (post.getPost_type()) {

            dim = new DownloadImageTask((ImageView) v.findViewById(R.id.repost_source_avatar));
            dim.executeAsyncTask(dim, post.getRepost_source_avatar());

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

        ImageView iv = (ImageView) v.findViewById(R.id.post_photo);
        if (post.getPost_photos() != null) {//probably shouldn't place "equals" here
            dim = new DownloadImageTask(iv);
            dim.executeAsyncTask(dim, post.getPost_photos()[0]);
        } else
            iv.setVisibility(View.GONE);
//            iv.setImageResource(R.drawable.empty_photo);

        Date date = new Date(post.getDate());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        ((TextView) v.findViewById(R.id.date)).setText(sdf.format(date));

        ((TextView) v.findViewById(R.id.likes)).setText(String.valueOf(post.getLikesCount()));

        return v;
    }
}
