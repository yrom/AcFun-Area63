package tv.acfun.a63.util;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;

import tv.acfun.a63.api.entity.Comment;
import tv.acfun.a63.api.entity.User;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;


public class MemberUtils{
	
	public static HashMap<String, Object> login(String host, String username,String password) throws HttpException, IOException,UnknownHostException, JSONException{
			HashMap<String, Object> map = new HashMap<String, Object>();
			PostMethod post = new PostMethod("/login.aspx");
	        NameValuePair[] nps = new NameValuePair[2];
	        nps[0] = new NameValuePair("username", username);
	        nps[1] = new NameValuePair("password", password);
	        post.setRequestBody(nps);
	        post.setRequestHeader("Content-Type", Connectivity.CONTENT_TYPE_FORM);
	        HttpClient client = new HttpClient();
	        client.getParams().setParameter("http.protocol.single-cookie-header", true);
	        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
	        client.getHostConfiguration().setHost(host, 80, "http");
	        int state = client.executeMethod(post);
	        
	        if(state>200){
	        	map.put("success", false);
	        	map.put("result", "ac娘大姨妈？");
	        }else{
	            JSONObject re = JSON.parseObject(post.getResponseBodyAsString());
		        
		        if(re.getBoolean("success")){
			        Cookie[] cks = client.getState().getCookies();
			        PostMethod mempost = new PostMethod("/user_check.aspx");
			        HttpState localHttpState = new HttpState();
			        localHttpState.addCookies(cks);
			        client.setState(localHttpState);
			        client.executeMethod(mempost);
			        String jsonstring = mempost.getResponseBodyAsString();
			        
			        JSONObject job =  JSON.parseObject(jsonstring);
			        
			        String uname = job.getString("uname");
			        String signature = job.getString("signature");
			        String avatar = job.getString("avatar");
			        int uid = job.getIntValue("uid");
			        User user = new User(uid, uname, avatar, signature);
			        user.cookies = JSON.toJSONString(cks,false);
			        map.put("user", user);
			        map.put("success", true);
		        }else{
		        	map.put("success", false);
		        	map.put("result", re.get("result"));
		        }
	        }
	    
	        return map;
	}
	
	public static boolean postComments(String comment,int aid, String host, Cookie[] cks) throws HttpException, IOException{
	    return postComments(comment, null, aid,host, cks);
	}
	public static boolean postComments(String comment, Comment quote,int aid, String host, Cookie[] cks) throws HttpException, IOException{
	    PostMethod post = new PostMethod("/comment.aspx");
        NameValuePair[] nps = { new NameValuePair("name", "sendComm()"),
                new NameValuePair("name", "mimiko"), 
                new NameValuePair("text", comment),
                new NameValuePair("quoteId", quote == null ? "0" : quote.cid + ""),
                new NameValuePair("contentId", String.valueOf(aid)), 
                new NameValuePair("cooldown", "5000"),
                new NameValuePair("quoteName", quote == null ? "" : quote.userName) };
        post.setRequestBody(nps);
        post.setRequestHeader("Content-Type",Connectivity.CONTENT_TYPE_FORM);
        int state  = Connectivity.doPost(post, host, cks);
        return state == 200;
	}

    public static boolean addFavourite(String cid, String host, Cookie[] cks) {
        NameValuePair[] nps = new NameValuePair[2];
        nps[0] = new NameValuePair("cId", cid);
        nps[1] = new NameValuePair("operate", "1");
        return Connectivity.postResultJson("/member/collect.aspx", host, nps, cks).getBooleanValue("success");
    }

    public static boolean deleteFavourite(String cid, String host, Cookie[] cookies) {
        NameValuePair[] nps = new NameValuePair[2];
        nps[0] = new NameValuePair("cId", cid);
        nps[1] = new NameValuePair("operate", "0");
        return Connectivity.postResultJson("/member/collect.aspx", host, nps, cookies).getBooleanValue("success");
    }

    public static JSONObject checkIn(String host, Cookie[] cks) {
        return Connectivity.postResultJson("/member/checkin.aspx", host, null, cks);
    }
}
