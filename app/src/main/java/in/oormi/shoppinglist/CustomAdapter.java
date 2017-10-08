package in.oormi.shoppinglist;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<GroupInfo> taskList;

    public CustomAdapter(Context context, ArrayList<GroupInfo> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<ChildInfo> productList = taskList.get(groupPosition).getDetailsList();
        return productList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View view, ViewGroup parent) {

        ChildInfo detailInfo = (ChildInfo) getChild(groupPosition, childPosition);

        //if (view == null) { //do not reuse views, it messes up colored backgrounds
            LayoutInflater infalInflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.child_items, null);
        //}

        TextView t1 = (TextView) view.findViewById(R.id.childItemQuantity);
        t1.setText(detailInfo.getQuantity());

        TextView t2 = (TextView) view.findViewById(R.id.childItemCost);
        String scost = detailInfo.getCost();
        if (scost.isEmpty()) scost = "0";
        if (scost.equals(".")) scost = "0";
        float cost = Float.parseFloat(scost);
        t2.setText(String.format("%.2f", cost));

        TextView t3 = (TextView) view.findViewById(R.id.childItemNotes);
        t3.setText(detailInfo.getNotes());


       if (detailInfo.hasError) {
            ObjectAnimator colorFade = ObjectAnimator.ofObject(view, "backgroundColor",
                    new ArgbEvaluator(), Color.argb(255, 255, 255, 255), 0xffffeecc);
            colorFade.setDuration(2000);
            colorFade.start();
        }
        else {
            if (detailInfo.isNew) {

                ObjectAnimator colorFade1 = ObjectAnimator.ofObject(view, "backgroundColor",
                        new ArgbEvaluator(), Color.argb(255, 255, 255, 255), 0xd9edfccf);
                colorFade1.setDuration(2000);
                colorFade1.start();

            }
        }
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        ArrayList<ChildInfo> productList = taskList.get(groupPosition).getDetailsList();
        return productList.size();

    }

    @Override
    public Object getGroup(int groupPosition) {
        return taskList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return taskList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view,
                             ViewGroup parent) {

        GroupInfo headerInfo = (GroupInfo) getGroup(groupPosition);
        //if (view == null) {
            LayoutInflater inf =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.group_items, null);
        //}

        TextView heading = (TextView) view.findViewById(R.id.heading);
        heading.setText(headerInfo.getTask().trim());

        ImageView iv = (ImageView) view.findViewById(R.id.imageViewStatus);

        if (headerInfo.getEnabled()){
            heading.setTextColor(Color.BLACK);
            iv.setImageResource(R.drawable.item1);
        } else {
            heading.setTextColor(Color.LTGRAY);
            iv.setImageResource(R.drawable.item0);
        }

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}