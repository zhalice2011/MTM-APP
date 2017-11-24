package com.ruiqi.mtm;

/**
 * Created by HongYuLiu on 2017/10/11.
 */

public class CallBack {
    public MtBuf m_mtbuf;
    public BgBuf m_bgbuf;
    public BpmBuf m_bpmbuf;
    public SpoBuf m_spobuf;
    public WTBuf m_wtbuf;
    public  ICallBack m_icall;
    public CallBack( MtBuf mtbuf,ICallBack icall,BgBuf bgbuf,BpmBuf bpmbuf,SpoBuf spobuf,WTBuf wtbuf)
    {
        m_mtbuf = mtbuf;
        m_icall = icall;
        m_bgbuf = bgbuf;
        m_bpmbuf = bpmbuf;
        m_spobuf = spobuf;
        m_wtbuf = wtbuf;
    }
    public void call()
    {
        m_icall.call();
    }
}
