package com.mju.exercise.OpenMatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mju.exercise.Domain.OpenMatchDTO;
import com.mju.exercise.R;

import java.util.ArrayList;
import java.util.List;

public class OpenMatchAdapter extends ArrayAdapter implements AdapterView.OnItemClickListener {

    private Context mContext;
    private List list;

    public OpenMatchAdapter(@NonNull Context context, @NonNull ArrayList list) {
        super(context, 0, list);
        this.mContext = context;
        this.list = list;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    class ViewHolder{
        public TextView tvSubect;
        public TextView tvArticle;
        public TextView tvSportType;
        public TextView tvPersonnel;
        public TextView tvPlayDateTime;
        public TextView tvOpenUserId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            convertView = layoutInflater.inflate(R.layout.open_match_item, parent, false);
        }

        viewHolder = new ViewHolder();
        viewHolder.tvArticle = (TextView) convertView.findViewById(R.id.omArticle);
        viewHolder.tvSubect = (TextView) convertView.findViewById(R.id.omSubject);
        viewHolder.tvSportType = (TextView) convertView.findViewById(R.id.omSportType);
        viewHolder.tvPersonnel = (TextView) convertView.findViewById(R.id.omPersonnel);
        viewHolder.tvPlayDateTime = (TextView) convertView.findViewById(R.id.omPlayDateTime);
        viewHolder.tvOpenUserId = (TextView) convertView.findViewById(R.id.omOpenUserId);

        final OpenMatchDTO openMatchDTO = (OpenMatchDTO) list.get(position);
        viewHolder.tvArticle.setText(openMatchDTO.getArticle());
        viewHolder.tvSubect.setText(openMatchDTO.getSubject());
        viewHolder.tvSportType.setText(openMatchDTO.getSportType());
        viewHolder.tvPersonnel.setText(openMatchDTO.getPersonnel());
        viewHolder.tvPlayDateTime.setText(String.valueOf(openMatchDTO.getPlayDateTime()));
        viewHolder.tvOpenUserId.setText(openMatchDTO.getOpenUserId());

        return convertView;

    }
}
