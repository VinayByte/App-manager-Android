package com.egnize.appmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.egnize.appmanager.R;
import com.egnize.appmanager.databinding.RecyclerRowItemBinding;
import com.egnize.appmanager.models.App;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

public class AppRecyclerAdapter extends RecyclerView.Adapter<AppRecyclerAdapter.ViewHolder> {
    private final boolean rootAccessAlreadyObtained;
    private RecyclerRowItemBinding binding;
    private ArrayList<App> installedApps;
    private MutableLiveData<Boolean> adapterCallback;

    public AppRecyclerAdapter(ArrayList<App> installedApps, boolean rootAccessAlreadyObtained, MutableLiveData<Boolean> callback) {
        this.installedApps = installedApps;
        this.rootAccessAlreadyObtained = rootAccessAlreadyObtained;
        this.adapterCallback = callback;
        removeInvisibleApps();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.recycler_row_item,
                parent,
                false);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_row_item, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView title;
        private TextView appPackage;
        private CheckBox selected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            icon = binding.imageView;
            title = binding.primaryText;
            appPackage = binding.secondaryText;
            selected = binding.checkbox;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        App app = installedApps.get(position);
        holder.icon.setImageDrawable(app.getIcon());
        holder.title.setText(app.getName());
        holder.appPackage.setText(app.getPackageName());
        holder.selected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (rootAccessAlreadyObtained){
                app.setSelected(isChecked);
            }else {
                if (!app.isSystemApp()){
                    app.setSelected(isChecked);
                    adapterCallback.postValue(false);
                }else {
                    app.setSelected(false);
                    holder.selected.setChecked(false);
                    adapterCallback.postValue(true);
                }
            }

        });
        boolean appIsSelected = app.isSelected();
        if (rootAccessAlreadyObtained){
            holder.selected.setChecked(appIsSelected);
            adapterCallback.postValue(false);
        }else {
            if (!app.isSystemApp()){
                holder.selected.setChecked(appIsSelected);
                adapterCallback.postValue(false);
            }else {
                holder.selected.setChecked(false);
                adapterCallback.postValue(false);
            }
        }


    }

    @Override
    public int getItemCount() {
        return installedApps.size();
    }

    public void updataList(ArrayList<App> installedApps) {
        this.installedApps = installedApps;
        removeInvisibleApps();
        notifyDataSetChanged();
    }

    private void removeInvisibleApps() {
        List<App> visibleApps = new ArrayList<>();
        for (App app : installedApps) {
            boolean isVisble = app.isVisible();
            if(isVisble)
                visibleApps.add(app);
        }
        installedApps = (ArrayList<App>) visibleApps;
    }
}
