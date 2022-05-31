package gr.aueb.simplegram.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import gr.aueb.simplegram.R;
import gr.aueb.simplegram.common.Topic;

public class TopicViewAdapter extends ArrayAdapter<Topic> implements View.OnClickListener{

    private static class TopicView{
        FloatingActionButton fab;
        TextView textView;
    }

    private ArrayList<Topic> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
        ImageView info;
    }

    public TopicViewAdapter(Context context, ArrayList<Topic> data) {
        super(context, R.layout.improved_topic_item, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object = getItem(position);
        Topic dataModel = (Topic) object;

        switch (v.getId())
        {
            case R.id.topic_fab:
                Snackbar.make(v, "Topic name is " +dataModel.getName(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Topic dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        TopicView viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new TopicView();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.improved_topic_item, parent, false);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.label);
            viewHolder.fab = (FloatingActionButton) convertView.findViewById(R.id.topic_fab);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (TopicView) convertView.getTag();
            result=convertView;
        }

        viewHolder.textView.setText(dataModel.getName());
        viewHolder.textView.setOnClickListener(this);
        // TODO: add fab story functionality.
        // Return the completed view to render on screen
        return convertView;
    }
}

