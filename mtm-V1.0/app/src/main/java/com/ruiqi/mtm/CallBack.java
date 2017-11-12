package com.ruiqi.mtm;

/**
 * Created by HongYuLiu on 2017/10/11.
 */

public class CallBack {
    public MtBuf m_mtbuf;
    public BgBuf m_bgbuf;
    public  ICallBack m_icall;
    public CallBack( MtBuf mtbuf,ICallBack icall,BgBuf bgbuf)
    {
        m_mtbuf = mtbuf;
        m_icall = icall;
        m_bgbuf = bgbuf;
    }
    public void call()
    {
        m_icall.call();
    }
}
