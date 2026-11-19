package com.mashad.launcher;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {

    private final Context context;
    private final List<AppModel> apps;

    public AppsAdapter(Context context, List<AppModel> apps) {
        this.context = context;
        this.apps = apps;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_card, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppModel app = apps.get(position);
        holder.tvAppName.setText(app.label);
        holder.ivAppIcon.setImageDrawable(app.icon);

        holder.itemView.setFocusable(true);
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.08f).scaleY(1.08f).translationZ(8f).setDuration(150).start();
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(150).start();
            }
        });

        holder.itemView.setOnClickListener(v -> {
            try {
                Intent launchIntent = context.getPackageManager().getLeanbackLaunchIntentForPackage(app.packageName);
                if (launchIntent == null) {
                    launchIntent = context.getPackageManager().getLaunchIntentForPackage(app.packageName);
                }
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                } else {
                    Toast.makeText(context, "Cannot open " + app.label, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error launching app", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAppIcon;
        TextView tvAppName;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.ivAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
        }
    }
}
