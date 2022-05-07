package com.templars_server.database;

public class AntiVPNRow {

    private final String ip;
    private final boolean vpn;
    private final boolean vpnCheck;

    public AntiVPNRow(String ip, boolean vpn, boolean vpnCheck) {
        this.ip = ip;
        this.vpn = vpn;
        this.vpnCheck = vpnCheck;
    }

    public String getIp() {
        return ip;
    }

    public boolean isVpn() {
        return vpn;
    }

    public boolean isVpnCheck() {
        return vpnCheck;
    }

}
