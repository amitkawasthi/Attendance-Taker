/*
 * Copyright (C) 2015 Ferid Cafer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.darbose.classroom.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.darbose.classroom.R;
import in.darbose.classroom.model.Attendance;

/**
 * Created by ferid.cafer on 4/16/2015.
 */
public class PastAttendancesListAdapter extends ArrayAdapter<Attendance> {
    private Context context;
    private int layoutResId;
    private ArrayList<Attendance> items;

    public PastAttendancesListAdapter(Context context, int layoutResId, ArrayList<Attendance> objects) {
        super(context, layoutResId, objects);
        this.items = objects;
        this.context = context;
        this.layoutResId = layoutResId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // return your progress view goes here. Ensure that it has the ID
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResId, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.counter = (TextView) convertView.findViewById(R.id.counter);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Attendance item = items.get(position);

        viewHolder.counter.setText(String.valueOf(position+1));
        viewHolder.text.setText(item.getDateTime());

        return convertView;
    }

    public class ViewHolder {
        TextView counter;
        TextView text;
    }
}
