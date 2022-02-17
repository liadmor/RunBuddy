package com.example.runbuddy;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CustomStringRequest extends Request<CustomStringRequest.ResponseM> {

    private Response.Listener<CustomStringRequest.ResponseM> mListener;
    String cookie;

    public CustomStringRequest(int method,String cookie, String url, Response.Listener<CustomStringRequest.ResponseM> responseListener, Response.ErrorListener listener) {
        super(method, url, listener);
        if(cookie != null){
            this.cookie = cookie;
        }
        this.mListener = responseListener;
    }


    @Override
    protected void deliverResponse(ResponseM response) {
        this.mListener.onResponse(response);
    }

    @Override
    protected Response<ResponseM> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }

        ResponseM responseM = new ResponseM();
        responseM.headers = response.headers;
        responseM.response = parsed;

        return Response.success(responseM, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String>  params = new HashMap<String, String>();
        params.put("Cookie",this.cookie);

        return params;
    }


    public static class ResponseM {
        Map<String, String> headers;
        String response;
    }

}