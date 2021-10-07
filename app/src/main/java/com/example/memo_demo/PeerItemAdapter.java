package com.example.memo_demo;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wifi.WifiP2p;

import java.io.Serializable;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class PeerItemAdapter extends RecyclerView.Adapter<PeerItemAdapter.ViewHolder> implements Serializable {

    private List<WifiP2pDevice> mP2pDeviceList;
    private PeerItemListener mListener;

    public PeerItemAdapter(List<WifiP2pDevice> p2pDeviceList) {
        mP2pDeviceList = p2pDeviceList;
    }

    public void setP2pDeviceList(List<WifiP2pDevice> list) {
        this.mP2pDeviceList = list;
    }

    public List<WifiP2pDevice> getP2pDeviceList() {
        return mP2pDeviceList;
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
        holder.deviceName.setText(mP2pDeviceList.get(position).deviceName);
        holder.deviceAddress.setText(mP2pDeviceList.get(position).deviceAddress);
        holder.deviceStatus.setText(WifiP2p.getConnectStatus(mP2pDeviceList.get(position).status));
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mP2pDeviceList.size();
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

