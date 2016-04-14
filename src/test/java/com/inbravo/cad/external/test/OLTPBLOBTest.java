package com.inbravo.cad.external.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

public class OLTPBLOBTest {

  private static final String edsaURL = "http://localhost:8080/edsa";

  public static void main(String[] args) throws HttpException, IOException, UnsupportedEncodingException {
    readOltpBlobObject();
  }

  private static void readOltpBlobObject() throws HttpException, IOException, UnsupportedEncodingException {

    try {
      for (int i = 0; i < 5; i++) {

        new Thread() {
          public void run() {
            GetMethod post = null;
            try {
              post =
                  new GetMethod(edsaURL + "/object/oltpblob/"
                      + URLEncoder.encode("select * from tenant0003.FILES where tenant0003.FILES.ID=21 OR tenant0003.FILES.ID=22", "UTF-8")
                      + "?tenant=dev5ns");
              post.addRequestHeader("Content-Type", "application/xml");
              HttpClient httpclient = new HttpClient();

              try {
                System.out.println("URI: " + post.getURI());
                int result = httpclient.executeMethod(post);
                System.out.println("Response status code: " + result);
                System.out.println("Response body: " + post.getResponseBodyAsString());
              } catch (Exception exception) {
                System.err.println(exception);
              }
            } catch (Exception e) {

            } finally {
              post.releaseConnection();
            }
          }
        }.start();
      }
    } catch (Exception e) {

    }

  }
}
