package cn.edu.bupt.service;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.util.List;

/**
 * Created by FirenzesEagle on 2016/5/30 0030.
 * Email:liumingbo2008@gmail.com
 */
public interface CoreService {

    /**
     * HttpGet请求
     *
     *  @param urlWithParams
     * @throws Exception
     */
    public void requestGet(String urlWithParams) throws IOException;

    /**
     * HttpPost请求
     *
     * @param url
     * @param params
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void requestPost(String url, List<NameValuePair> params) throws ClientProtocolException, IOException;

}
