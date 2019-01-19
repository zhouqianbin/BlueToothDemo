package com.zhouqianbin.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class BlueToothRecyAdapter extends BaseQuickAdapter<BluetoothDevice,BaseViewHolder> {

    public BlueToothRecyAdapter(int layoutResId, @Nullable List<BluetoothDevice> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BluetoothDevice item) {
        //蓝牙名称
        helper.setText(R.id.item_device_name,item.getName());
        //蓝牙地址
        helper.setText(R.id.item_device_address,item.getAddress());
        //蓝牙类型
        switch (item.getType()){
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                helper.setText(R.id.item_device_blue_type,"经典蓝牙");
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                helper.setText(R.id.item_device_blue_type,"BLE蓝牙");
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                helper.setText(R.id.item_device_blue_type,"双向蓝牙");
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                helper.setText(R.id.item_device_blue_type,"未知");
                break;
        }
        //蓝牙匹配状态
        switch (item.getBondState()){
            case BluetoothDevice.BOND_BONDED:
                helper.setText(R.id.item_bond_state,"匹配完成");
                break;
            case BluetoothDevice.BOND_BONDING:
                helper.setText(R.id.item_bond_state,"正在匹配");
                break;
            case BluetoothDevice.BOND_NONE:
                helper.setText(R.id.item_bond_state,"未匹配");
                break;
        }
        //蓝牙设备的主要类型
        switch (item.getBluetoothClass().getMajorDeviceClass()){
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                helper.setText(R.id.item_device_type,"音视频");
                break;
            case BluetoothClass.Device.Major.COMPUTER:
                helper.setText(R.id.item_device_type,"电脑");
                break;
            case BluetoothClass.Device.Major.HEALTH:
                helper.setText(R.id.item_device_type,"健康设备");
                break;
            case BluetoothClass.Device.Major.MISC:
                helper.setText(R.id.item_device_type,"麦克风");
                break;
            case BluetoothClass.Device.Major.PERIPHERAL:
                helper.setText(R.id.item_device_type,"外设");
                break;
            case BluetoothClass.Device.Major.PHONE:
                helper.setText(R.id.item_device_type,"电话");
                break;
            case BluetoothClass.Device.Major.TOY:
                helper.setText(R.id.item_device_type,"玩具");
                break;
            case BluetoothClass.Device.Major.WEARABLE:
                helper.setText(R.id.item_device_type,"可穿戴设备");
                break;
            case BluetoothClass.Device.Major.NETWORKING:
                helper.setText(R.id.item_device_type,"网络");
                break;
            case BluetoothClass.Device.Major.IMAGING:
                helper.setText(R.id.item_device_type,"成像，镜像");
                break;
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                helper.setText(R.id.item_device_type,"未分类");
                break;
        }
    }
}
