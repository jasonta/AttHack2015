package com.gimbal.hello_gimbal_android;

import com.harman.hkwirelessapi.*;

/**
 * Created by tor on 10/24/15.
 */
public class DeviceData {
    public DeviceObj deviceObj;
    public boolean isAvailable;

    public DeviceData(final DeviceObj deviceObj, final boolean isAvailable) {
        this.deviceObj = deviceObj;
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        if (deviceObj == null) return "DeviceData: null";

        return "DeviceData{ deviceObj=[\n" +
                "  active=" + deviceObj.active + "\n" +
                "  balance=" + deviceObj.balance + "\n" +
                "  channelType=" + deviceObj.channelType + "\n" +
                "  deviceId=" + deviceObj.deviceId + "\n" +
                "  deviceName=" + deviceObj.deviceName + "\n" +
                "  groupId=" + deviceObj.groupId + "\n" +
                "  groupName=" + deviceObj.groupName + "\n" +
                "  ipAddress=" + deviceObj.ipAddress + "\n" +
                "  isMaster=" + deviceObj.isMaster + "\n" +
                "  isPlaying=" + deviceObj.isPlaying + "\n" +
                "  macAddress=" + deviceObj.macAddress + "\n" +
                "  modelName=" + deviceObj.modelName + "\n" +
                "  port=" + deviceObj.port + "\n" +
                "  role=" + deviceObj.role + "\n" +
                "  version=" + deviceObj.version + "\n" +
                "  volume=" + deviceObj.volume + "\n" +
                "  wifiSignalStrength=" + deviceObj.wifiSignalStrength + "\n" +
                "  zoneName=" + deviceObj.zoneName + "\n" +
                "], isAvailable=" + isAvailable +
                '}';
    }
}
