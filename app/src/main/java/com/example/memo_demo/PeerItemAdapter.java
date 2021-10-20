package com.example.memo_demo;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wifi.WifiP2p;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

public class PeerItemAdapter extends RecyclerView.Adapter<PeerItemAdapter.ViewHolder> implements Serializable {

    private List<MemoHost.ClientDeviceStatus> mClientList;
    private PeerItemListener mListener;

    public PeerItemAdapter(List<MemoHost.ClientDeviceStatus> clientList) {
        mClientList = clientList;
    }

    public void setListener(PeerItemListener listener) {
        mListener = listener;
    }

    public PeerItemListener getListener() {
        return mListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.peer_item, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick((int) view.getTag());
                }
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.deviceName.setText(mClientList.get(position).name);
        holder.deviceAddress.setText(mClientList.get(position).address.getHostAddress());
        holder.deviceStatus.setText(mClientList.get(position).status.toString());
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mClientList.size();
    }

    public interface PeerItemListener {
        void onItemClick(int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView deviceName;
        private final TextView deviceAddress;
        private final TextView deviceStatus;

        ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.peer_deviceName);
            deviceAddress = itemView.findViewById(R.id.peer_deviceAddress);
            deviceStatus = itemView.findViewById(R.id.peer_deviceStatus);
        }

    }
}

