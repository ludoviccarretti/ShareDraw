package umlv.fr.sharedraw.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ludovic
 * @version 1.0
 *          This class permit to do HTTP Request to a web service
 */
public class HttpRequest {
    private final static String CLASS_NAME = HttpRequest.class.getCanonicalName();
    private final Map<String, Method> methods = new HashMap<>();

    public HttpRequest() {
        for (Method m : HttpRequest.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(HttpRequestProperty.class)) {
                String method = m.getAnnotation(HttpRequestProperty.class).value();
                methods.put(method, m);
            }
        }
    }

    /**
     * Execute the HTTP Request
     *
     * @param params must be not null and must have this format: Method to call, parameters to give to the method
     *               <br /><br />
     *               Methods list to give in first argument to executor:<br />
     *               <table>
     *               <tr>
     *               <th>Method</th>
     *               <th>Arguments</th>
     *               </tr>
     *               <tr>
     *               <td>getListOfDashboard</td>
     *               <td>server : String</td>
     *               </tr>
     *               <tr>
     *               <td>postMessage</td>
     *               <td>server    : String</td>
     *               <td>queueName : String</td>
     *               <td>message   : JSON as String</td>
     *               </tr>
     *               <tr>
     *               <td>getMessage</td>
     *               <td>server         : String</td>
     *               <td>queueName      : String</td>
     *               <td>idMessage      : String</td>
     *               <td>Timeout in sec : String</td>
     *               </tr>
     *               </table>
     *               <br />
     */
    public String request(String... params) {
        Method m = methods.get(params[0]);
        return execute(m, params);
    }

    private String execute(final Method m, final String[] params) {
        try {
            return (String) m.invoke(null, new Object[]{params});
        } catch (Exception e) {
            return null;
        }
    }

    @HttpRequestProperty(value = "getListOfDashboard")
    protected static String getListOfDashboard(String... params) {
        if (params.length < 2) {
            Log.e(CLASS_NAME, "This method (getListOfDashboard) must have few arguments: server(String)");
            return null;
        }
        URL url = getURL("http://" + params[1]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();
            if (code == 200) {
                return getStringFromInputStream(connection.getInputStream());
            } else {
                return null;
            }

        } catch (IOException e) {
            Log.e(CLASS_NAME, "Cannot connect to " + params[1]);
            return null;
        }
    }

    @HttpRequestProperty(value = "postMessage")
    protected static String postNewMessage(String... params) {
        if (params.length < 4) {
            Log.e(CLASS_NAME, "This method (postMessage) must have few arguments: server(String), queue(String), message(JSON as String)");
            return null;
        }
        URL url = getURL("http://" + params[1] + "/" + params[2]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", Integer.toString(params[3].getBytes().length));
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setDoInput(true);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
            writer.write(params[3]);
            writer.flush();
            writer.close();
            wr.close();

            int code = connection.getResponseCode();
            if (code == 200) {
                return getStringFromInputStream(connection.getInputStream());
            }
            return null;
        } catch (IOException e) {
            Log.e(CLASS_NAME, "Cannot connect to " + params[1]);
            return null;
        }
    }

    @HttpRequestProperty(value = "getMessage")
    protected static String getMessage(String... params) {
        if (params.length < 5) {
            Log.e(CLASS_NAME, "This method (getMessage) must have few arguments: server(String), queue(String), idMessage(String), timeout(String in sec)");
            return null;
        }
        URL url = getURL("http://" + params[1] + "/" + params[2] + "/" + params[3] + "?timeout=" + params[4]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept-Encoding", "identity");
            int code = connection.getResponseCode();
            if (code == 200) {
                StringBuilder stringBuilder = new StringBuilder(getStringFromInputStream((InputStream) connection.getContent()));
                stringBuilder.insert(1, "\"id\": " + Integer.valueOf(params[3]) + ", ");
                return stringBuilder.toString();
            }
            return null;
        } catch (IOException e) {
            Log.e(CLASS_NAME, "Cannot connect to " + params[1]);
            return null;
        }
    }

    protected static URL getURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.e(CLASS_NAME, "URL is malformed" + url);
            return null;
        }
    }

    protected static String getStringFromInputStream(InputStream is) {
        String inputLine;
        StringBuilder response = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            Log.e(CLASS_NAME, "Cannot read response from the server");
            return null;
        }
        return response.toString();
    }
}