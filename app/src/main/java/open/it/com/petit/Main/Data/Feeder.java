package open.it.com.petit.Main.Data;

import java.io.Serializable;

/**
 * Created by user on 2017-10-30.
 */

public class Feeder implements Serializable{
    private String P_NUM;
    private String GUID;
    private String PW;
    private int MS;
    private String P_NAME; // 급식기 이름
    private String F_IMG; // 급식기 이미지
    private String TOKEN = ""; // FCM Token

    public Feeder() {

    }

    public Feeder(String p_NUM, String GUID, String PW, int MS, String p_NAME, String f_IMG, String TOKEN) {
        P_NUM = p_NUM;
        this.GUID = GUID;
        this.PW = PW;
        this.MS = MS;
        P_NAME = p_NAME;
        F_IMG = f_IMG;
        this.TOKEN = TOKEN;
    }

    public String getP_NUM() {
        return P_NUM;
    }

    public void setP_NUM(String p_NUM) {
        P_NUM = p_NUM;
    }

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public String getPW() {
        return PW;
    }

    public void setPW(String PW) {
        this.PW = PW;
    }

    public int getMS() {
        return MS;
    }

    public void setMS(int MS) {
        this.MS = MS;
    }

    public String getP_NAME() {
        return P_NAME;
    }

    public void setP_NAME(String p_NAME) {
        P_NAME = p_NAME;
    }

    public String getF_IMG() {
        return F_IMG;
    }

    public void setF_IMG(String f_IMG) {
        F_IMG = f_IMG;
    }

    public String getToken() {
        return TOKEN;
    }

    public void setToken(String TOKEN) {
        this.TOKEN = TOKEN;
    }
}
