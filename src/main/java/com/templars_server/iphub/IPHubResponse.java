package com.templars_server.iphub;

public class IPHubResponse {

    private String ip;
    private String hostname;
    private String countryCode;
    private String countryName;
    private int asn;
    private String isp;
    private int block;
    private String error;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public int getAsn() {
        return asn;
    }

    public void setAsn(int asn) {
        this.asn = asn;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}