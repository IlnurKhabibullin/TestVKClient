package com.example.testvkclient;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Ильнур on 01.07.2015.
 */
public class ImageAdapter extends PagerAdapter {

    Context context;
    private String[] photos;
    private DownloadImageTask dit;

    ImageAdapter(Context context, String[] photos){
        this.context=context;
        this.photos = photos;
    }
    @Override
    public int getCount() {
        return photos.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        int padding = 3;
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundResource(R.drawable.photos_frame);
        dit = new DownloadImageTask(imageView);
        dit.executeAsyncTask(dit, photos[position]);
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }
}
