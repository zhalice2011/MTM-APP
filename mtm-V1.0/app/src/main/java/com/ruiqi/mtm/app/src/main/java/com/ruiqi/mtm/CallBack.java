package com.ruiqi.mtm;

/**
 * Created by HongYuLiu on 2017/10/11.
 */

public class CallBack {
    public MtBuf m_mtbuf;
    public BgBuf m_bgbuf;
    public BpmBuf m_bpmbuf; //达理

    public  ICallBack m_icall;
    public CallBack( MtBuf mtbuf,ICallBack icall,BgBuf bgbuf,BpmBuf bpmbuf)
    {
        m_mtbuf = mtbuf;
        m_icall = icall;
        m_bgbuf = bgbuf;
        m_bpmbuf = bpmbuf;
    }
    public void call()
    {
        m_icall.call();
    }
}
