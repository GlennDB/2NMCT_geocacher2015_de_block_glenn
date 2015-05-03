package nmct.howest.be.jellyb;


import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XMLParser {

        public String getXmlFromUrl(String url) {
            String xml = null;

            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // return XML
            return xml;
        }

        public HashMap<String, String> getDomElement(String xml){

            HashMap<String, String> values = new HashMap<String, String>();

            try {

                xml = xml.replace(",",".");
                String[] parts = xml.split("Value\":\"");
                String lat = parts[1].split("\"")[0];
                String lon = parts[2].split("\"")[0];
                String alt = parts[3].split("\"")[0];

                values.put("lat", lat);
                values.put("lon", lon);
                values.put("alt", alt);


            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }
            // return values
            return values;
        }

        public String getValue(Element item, String str) {
            NodeList n = item.getElementsByTagName(str);
            return this.getElementValue(n.item(0));
        }

        public final String getElementValue( Node elem ) {
            Node child;
            if( elem != null){
                if (elem.hasChildNodes()){
                    for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                        if( child.getNodeType() == Node.TEXT_NODE  ){
                            return child.getNodeValue();
                        }
                    }
                }
            }
            return "";
        }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }
}
