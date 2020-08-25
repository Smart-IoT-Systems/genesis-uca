/**
 * https://memorynotfound.com/ignore-certificate-errors-apache-httpclient/
 * https://www.tutorialspoint.com/apache_httpclient/apache_httpclient_response_handlers.htm
 */

package ENACTConsumer.logic;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;


public class HTTPS {

//	public static void main(String... args)  throws Exception{
//		//String res=HTTPs.get("https://localhost:8443/jwt");
//		//System.out.println(res);
//		String res = HTTPS.post("https://localhost:8443/jwt","{\"id\":\"aeiou\"}");
//		System.out.println(res);
//	}
	
   public static String post(String url,String payload)  throws Exception{
		try (CloseableHttpClient httpclient = createAcceptSelfSignedCertificateClient()) {
		    HttpPost post = new HttpPost(url);
		    HttpEntity stringEntity = new StringEntity(payload,ContentType.APPLICATION_JSON);
		    post.setEntity(stringEntity);
		    MyResponseHandler res = new MyResponseHandler();
		    httpclient.execute(post,res);
		    return res.result;
		} catch (Exception e) {
		    throw e;
		}
    }
		 
   public static String get(String url)  throws Exception{
        try (CloseableHttpClient httpclient = createAcceptSelfSignedCertificateClient()) {
            HttpGet httpget = new HttpGet(url);
            MyResponseHandler res = new MyResponseHandler();
            httpclient.execute(httpget,res);
            return res.result;
        } catch (Exception e) {
            throw e;
        }
    }
	    
    private static class MyResponseHandler implements ResponseHandler<String>{
    	public String result = null;
		public String handleResponse(final HttpResponse res) throws IOException{
			result = EntityUtils.toString(res.getEntity());
			return "OK";
		}
    }
	    
    private static CloseableHttpClient createAcceptSelfSignedCertificateClient() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
            .loadTrustMaterial(new TrustSelfSignedStrategy()).build();
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();//disable hostname verification
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
        return HttpClients.custom().setSSLSocketFactory(connectionFactory).build();
    }
}

