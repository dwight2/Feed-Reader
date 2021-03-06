package com.example.mudit.sententia;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by mudit on 23/6/17.
 */

public class QuickReads extends AppCompatActivity {
    private static String TAG = "QuickReads";
    public static String FACEBOOK_URL = "https://www.facebook.com/SententiaMedia1/";
    public static String PAGE_ID = "SententiaMedia1";

    private int defaultImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_reads);

        String title;
//        String creator;
//        String pubDate;
        String content;
        String img_url;
        final String category;
        final String link;

        Intent incomingIntent = getIntent();
        title = incomingIntent.getStringExtra("@string/title");
//        creator = incomingIntent.getStringExtra("@string/creator");
//        pubDate = incomingIntent.getStringExtra("@string/pubDate");
        content = incomingIntent.getStringExtra("@string/content");
        link = incomingIntent.getStringExtra("@string/link");
        img_url = incomingIntent.getStringExtra("@string/image_url");
        category = incomingIntent.getStringExtra("@string/category");

        TextView contentView = (TextView) findViewById(R.id.contentView);
        TextView titleView = (TextView) findViewById(R.id.titleView);
        Button button1 = (Button) findViewById(R.id.button1);
        ImageView back_button = (ImageView) findViewById(R.id.back_button);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        ImageView qr_image = (ImageView) findViewById(R.id.qr_image);
        ProgressBar qr_progress = (ProgressBar) findViewById(R.id.qr_progress);

        qr_progress.setVisibility(View.VISIBLE);

        setupImageLoader();
        toolbar_title.setText(category);

        displayImage(img_url, qr_image, qr_progress);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuickReads.this.finish();
            }
        });

        socialMedia();

        //Log.i(TAG, content);

        //Using JSoup to remove image tags to not be displayed in text view, Regex was slightly erroneous
        if(content != null) {
            Document document = Jsoup.parse(content);
            document.select("img").remove();
            document.select("figure").remove();
            content = document.toString();
        }

        //Converting html to textview
        if(Build.VERSION.SDK_INT >= 24){
            contentView.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
        }
        else {
            contentView.setText(Html.fromHtml(content));
        }

        //contentView.setText(Html.fromHtml(content,new URLImageParser(contentView, this), null)); // For images in text view
        titleView.setText(title);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "onButtonClick: Clicked: ");
                Intent intent = new Intent(QuickReads.this, WebViewActivity.class);
                intent.putExtra("@string/link", link);
                intent.putExtra("@string/category", category);
                startActivity(intent);
            }
        });
    }

    private void socialMedia()
    {
        ImageView fb = (ImageView) findViewById(R.id.fb);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = getFacebookPageURL(QuickReads.this);
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);
            }
        });

        ImageView twitter = (ImageView) findViewById(R.id.twitter);

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://twitter.com/SententiaMedia1"));
                startActivity(intent);
            }
        });

        ImageView insta = (ImageView) findViewById(R.id.insta);

        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://instagram.com/_u/sententiamedia1"));
                startActivity(intent);
            }
        });
    }

    //Method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }

    private void displayImage(String imageURL, ImageView imageView, final ProgressBar progressBar){

        //create the imageloader object
        ImageLoader imageLoader = ImageLoader.getInstance();

        //create display options
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();

        //download and display image from url
        imageLoader.displayImage(imageURL, imageView, options , new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }

        });
    }

    /**
     * Required for setting up the Universal Image loader Library
     */
    private void setupImageLoader(){
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                QuickReads.this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

        defaultImage = QuickReads.this.getResources().getIdentifier("@mipmap/image_failed",null,QuickReads.this.getPackageName());
    }
}
