package com.inbravo.scribe.external.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import com.inbravo.scribe.rest.service.crm.zd.auth.ZDAuthManager;
import com.inbravo.scribe.rest.service.crm.zd.auth.basic.ZDBasicAuthManager;

/**
 * 
 * @author amit.dixit
 * 
 */
public final class ZDCRMTest {

  private static String cookie;

  /**
   * @param args
   * @throws Exception
   */
  public static final void main(final String[] args) throws Exception {

    /* Show menu to the customer */
    showMenu();

    /* Get user input */
    final String choice = getUserInput();

    final ZDCRMTest test = new ZDCRMTest();

    switch (new Integer(choice).intValue()) {
      case 1:
        test.getUsers("amit.dixit@impetus.co.in", "pilkhuwa", "amitdixitinc.zendesk.com");
        break;
      case 2:
        test.getTickets("amit.dixit@impetus.co.in", "pilkhuwa", "amitdixitinc.zendesk.com");
        break;
      case 3:
        test.getEntries("amit.dixit@impetus.co.in", "pilkhuwa", "amitdixitinc.zendesk.com");
        break;
      case 4:
        for (int i = 0; i < 99; i++) {
          test.search("amit.dixit@impetus.co.in", "pilkhuwa", "amitdixitinc.zendesk.com", "type:user");
        }
        break;
      case 5:
        /* Create auth manager */
        ZDAuthManager zDAuthManager = new ZDBasicAuthManager();

        final String firstCookie = zDAuthManager.getSessionId("amit.dixit@impetus.co.in", "pilkhuwa", "amitdixitinc.zendesk.com", "http", 80);

        final String secCookie = zDAuthManager.getSessionId("zoe.li@contactual.com", "1qaz2wsx", "ctl.zendesk.com", "http", 80);

        final String thirdCookie = zDAuthManager.getSessionId("vickytaurus@gmail.com", "mumbai", "vikas1234.zendesk.com", "http", 80);

        System.out.println("Session id: " + firstCookie);
        System.out.println("Session id: " + secCookie);
        System.out.println("Session id: " + thirdCookie);

        for (int i = 0; i < 1; i++) {
          (new Thread() {

            public void run() {

              for (int i = 0; i < 9; i++) {
                try {
                  test.dummySearch("amit.dixit@impetus.co.in", "pilkhuwa", "amitdixitinc.zendesk.com", "type:user", firstCookie);
                } catch (final Exception e) {
                  System.out.println(e);
                }
              }
            }
          }).start();

          (new Thread() {

            public void run() {

              for (int i = 0; i < 9; i++) {
                try {
                  test.dummySearch("zoe.li@contactual.com", "1qaz2wsx", "ctl.zendesk.com", "type:user", secCookie);
                } catch (final Exception e) {
                  System.out.println(e);
                }
              }
            }
          }).start();

          (new Thread() {

            public void run() {

              for (int i = 0; i < 9; i++) {
                try {
                  test.dummySearch("amit.dixit@impetus.co.in", "pilkhuwa", "amitdixitinc.zendesk.com", "type:user", firstCookie);
                } catch (final Exception e) {
                  System.out.println(e);
                }
              }
            }
          }).start();

          (new Thread() {

            public void run() {

              for (int i = 0; i < 9; i++) {
                try {
                  test.dummySearch("zoe.li@contactual.com", "1qaz2wsx", "ctl.zendesk.com", "type:user", secCookie);
                } catch (final Exception e) {
                  System.out.println(e);
                }
              }
            }
          }).start();

          (new Thread() {

            public void run() {

              for (int i = 0; i < 9; i++) {
                try {
                  test.dummySearch("vickytaurus@gmail.com", "mumbai", "vikas1234.zendesk.com", "type:user", thirdCookie);
                } catch (final Exception e) {
                  System.out.println(e);
                }
              }
            }
          }).start();
        }
        break;
    }
  }

  private static final String getUserInput() {
    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    try {
      return bufferedReader.readLine();
    } catch (final IOException ex) {
      return null;
    }
  }

  private static final void showMenu() {

    System.out.println(" 1. Get Users");
    System.out.println(" 2. Get Tickets");
    System.out.println(" 3. Get Entries");
    System.out.println(" 4. Search records");
    System.out.println(" 5. Search records");
    System.out.println("Enter the number of the operation to run (e.g. Enter 1 for Get Users): ");
  }

  public final void getUsers(final String userId, final String password, final String crmURL) {
    final GetMethod get = new GetMethod("http://" + crmURL + "/users.xml");

    get.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();

    /* Set credentials */
    httpclient.getState().setCredentials(new AuthScope(crmURL, 80), new UsernamePasswordCredentials(userId, password));

    try {

      int result = httpclient.executeMethod(get);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + get.getResponseBodyAsString());
      cookie = get.getResponseHeader("Set-Cookie").getValue();
      System.out.println(cookie);
    } catch (final Exception exception) {
      System.err.println(exception);
    } finally {
      get.releaseConnection();
    }
  }

  public final void getTickets(final String userId, final String password, final String crmURL) {
    final GetMethod get = new GetMethod("http://" + crmURL + "/tickets.xml");

    get.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();

    /* Set credentials */
    httpclient.getState().setCredentials(new AuthScope(crmURL, 80), new UsernamePasswordCredentials(userId, password));

    try {
      int result = httpclient.executeMethod(get);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + get.getResponseBodyAsString());
    } catch (final Exception exception) {
      System.err.println(exception);
    } finally {
      get.releaseConnection();
    }
  }

  public final void search(final String userId, final String password, final String crmURL, final String query) {
    final GetMethod get = new GetMethod("http://" + crmURL + "/search.xml");

    get.addRequestHeader("Content-Type", "application/xml");
    get.addRequestHeader(
        "Cookie",
        "_zendesk_session=BAh7DiIOaXNfbW9iaWxlRjoPc2Vzc2lvbl9pZCIlYmNmNGVlMjk4YWNlNDU3ZDM0YjJjOGU3MDU1NGQ1NjEiHHdhcmRlbi51c2VyLmRlZmF1bHQua2V5aQQBh10EOg1hdXRoX3ZpYSISQmFzaWNTdHJhdGVneToPdXBkYXRlZF9hdGwrB3kGTE46FWF1dGhlbnRpY2F0ZWRfYXRsKwd5BkxOIhN3YXJkZW4ubWVzc2FnZXsAOgxhY2NvdW50aQPKdQE6B2lkIhRjbnRscHlzeTRjMG9zcGg%3D--31e3c6f26da13b568a65872d067d1da2942ace01; path=/; HttpOnly, oid_user=; path=/; expires=Thu, 01-Jan-1970 00:00:00 GMT");
    final HttpClient httpclient = new HttpClient();

    /* Set credentials */
    httpclient.getState().setCredentials(new AuthScope(crmURL, 80), new UsernamePasswordCredentials(userId, password));
    try {

      /* Set request parameter */
      get.setQueryString(new NameValuePair[] {new NameValuePair("query", query)});

      int result = httpclient.executeMethod(get);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + get.getResponseBodyAsString());

    } catch (final Exception exception) {
      System.err.println(exception);
    } finally {
      get.releaseConnection();
    }
  }

  public final void getEntries(final String userId, final String password, final String crmURL) {
    final GetMethod get = new GetMethod("http://" + crmURL + "/entries.xml");

    get.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();

    /* Set credentials */
    httpclient.getState().setCredentials(new AuthScope(crmURL, 80), new UsernamePasswordCredentials(userId, password));

    try {

      int result = httpclient.executeMethod(get);
      System.out.println("Response status code: " + result);
      System.out.println("Response body: " + get.getResponseBodyAsString());

    } catch (final Exception exception) {
      System.err.println(exception);
    } finally {
      get.releaseConnection();
    }
  }

  public final void dummySearch(final String userId, final String password, final String crmURL, final String query, final String cookie)
      throws Exception {
    final GetMethod get = new GetMethod("http://" + crmURL + "/search.xml");

    get.addRequestHeader("Content-Type", "application/xml");
    final HttpClient httpclient = new HttpClient();

    /* Set credentials */
    httpclient.getState().setCredentials(new AuthScope("d" + "s", 80), new UsernamePasswordCredentials(userId, password));

    /* Set session cookie */
    get.addRequestHeader("Cookie", cookie);

    try {
      /* Set request parameter */
      get.setQueryString(new NameValuePair[] {new NameValuePair("query", query)});

      int result = httpclient.executeMethod(get);
      System.out.println("Thread id: " + Thread.currentThread().getId() + " & response status code: " + result + " & body: "
          + get.getResponseBodyAsString());

    } catch (final Exception exception) {
      System.err.println(exception);
    } finally {
      get.releaseConnection();
    }
  }

  @SuppressWarnings("unused")
  private final void showHeaders(final HttpMethodBase methodBase) {

    final Header[] headers = methodBase.getResponseHeaders();

    for (int i = 0; i < headers.length; i++) {
      System.out.println("header name: " + headers[i].getName());
      System.out.println("header value: " + headers[i].getValue());
    }
  }
}
