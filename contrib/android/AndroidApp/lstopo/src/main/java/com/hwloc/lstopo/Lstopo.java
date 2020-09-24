package com.hwloc.lstopo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import static androidx.annotation.Dimension.DP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

// Graphic tools to draw a topology
public class Lstopo extends AppCompatActivity {

    private RelativeLayout layout;
    private Activity activity;
    private String currentContent;
    private int screen_height;
    private int screen_width;
    private int hwloc_screen_height;
    private int hwloc_screen_width;
    private float xscale = 1;
    private float yscale = 1;
    private File debugFile;

    public Lstopo(Activity activity) {
        this.activity = activity;
        layout = activity.findViewById(R.id.relative_layout);
        setScreenSize();
        debugFile = getAbsoluteFile("/debug.txt");
        debugFile.delete();
    }

    /**
     * Draw topology box
     */
    public void box(int r, int b, int g, int x, int y, final int width, final int height, int id, String info){
        LinearLayout view = new LinearLayout(activity);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setId(id);
        layout.addView(view);
        setBoxInfo(view, info);
        setBoxAttributes(view, r, b, g,
                screen_width * x / hwloc_screen_width,
                screen_height * y / hwloc_screen_height,
                screen_width * width / hwloc_screen_width,
                screen_height * height / hwloc_screen_height);

    }

    public void setBoxInfo(LinearLayout view, String info){
        if(info.isEmpty()){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            return;
        }
        TextView tv = new TextView(activity);
        view.addView(tv);
        tv.setText(info);
        tv.setTextSize(DP, 100 / (float)((hwloc_screen_height + hwloc_screen_width) / 100));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins((int) (5 * xscale), (int) (1 * yscale), 0, 0);
        tv.setLayoutParams(params);
        tv.setVisibility(GONE);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) ((ViewGroup)v).getChildAt(0);
                int length = ((ViewGroup)v).getChildCount();
                if(tv.getVisibility() == VISIBLE){
                    for(int i = 1 ; i < length ; i ++){
                        ((ViewGroup)v).getChildAt(i).setVisibility(VISIBLE);
                    }
                    tv.setVisibility(GONE);
                } else {
                    for(int i = 1 ; i < length ; i ++){
                        ((ViewGroup)v).getChildAt(i).setVisibility(GONE);
                    }
                    tv.setVisibility(VISIBLE);
                }
            }
        });
    }

    private void setBoxAttributes(View view, int r, int b, int g, int x, int y, int width, int height){
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        view.setY(y);
        view.setX(x);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(Color.rgb(r, g, b));
        shape.setStroke(2, Color.parseColor("#000000"));
        view.setBackground(shape);

        view.setLayoutParams(params);
    }

    /**
     * Draw topology text
     */
    public void text(String text, int x, int y, int fontsize, int id){
        currentContent = text;

        TextView tv = new TextView(activity);
        tv.setMinWidth(screen_width);

        if( id == -1 ){
            tv.setTextIsSelectable(true);
            ScrollView scrollView = new ScrollView(activity);
            layout.addView(scrollView);
            scrollView.addView(tv);
            tv.setX(x);
            tv.setY(y);
        } else {
            tv.setClickable(false);
            LinearLayout viewGroup = layout.findViewById(id);
            viewGroup.addView(tv);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins((int) (10 * xscale), (int) (2 * yscale), 0, 0);
            tv.setLayoutParams(params);
        }

        if(fontsize != 0){
            tv.setTextSize(DP, fontsize * 15 / (float)((hwloc_screen_height + hwloc_screen_width) / 100));
        }

        tv.setText(text);
    }

    /**
     * Draw topology line
     */
    public void line(int x1, int y1, int x2, int y2){
        MyCanvas canvas = new MyCanvas(activity,x1 * xscale,x2 * xscale,y1 * yscale,y2 * yscale);
        layout.addView(canvas);
    }

    public void setScreenSize(){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int toolbars_height = 0;
        // status bar height
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            toolbars_height += activity.getResources().getDimensionPixelSize(resourceId);
        }
        // navigation bar height
        resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            toolbars_height += activity.getResources().getDimensionPixelSize(resourceId);
        }

        screen_height = size.y - toolbars_height;
        screen_width = size.x;
    }

    public void setScale(int hwloc_screen_height, int hwloc_screen_width){
        this.hwloc_screen_height = hwloc_screen_height;
        this.hwloc_screen_width = hwloc_screen_width;
        xscale = ((float) screen_width / (float) hwloc_screen_width);
        yscale = ((float) screen_height / (float) hwloc_screen_height);
    }

    public void setScreen_height(int screen_height) {
        this.screen_height = screen_height;
    }

    public void setScreen_width(int screen_width) {
        this.screen_width = screen_width;
    }

    public String getCurrentContent(){
        return this.currentContent;
    }

    public String getDebugFile() {
        return this.debugFile.getAbsolutePath();
    }

    public void clearDebugFile() {
        debugFile.delete();
    }

    public File getAbsoluteFile(String relativePath){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(activity.getExternalFilesDir(null), relativePath);
        } else {
            return new File(activity.getFilesDir(), relativePath);
        }
    }

    public void writeDebugFile(String content) {
        try {
            FileWriter writer = new FileWriter(debugFile, true);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
