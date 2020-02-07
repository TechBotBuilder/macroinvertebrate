package com.techbotbuilder.streamteamohio.forms;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.techbotbuilder.streamteamohio.ui.UIElement;
import com.techbotbuilder.streamteamohio.ui.UIFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class FormManager {

    private FormType type;

    private FormData data;

    //flags for passing a bundle to this activity
    public static final String TEMPLATE_ID_FLAG = "TEMPLATE";
    public static final String OLD_DATA_ID_FLAG = "OLDDATA";
    public static final String PREFILL_ID_FLAG = "PREFILLED";

    private static final String templateDir = "templates";
    private static final String prefillDir = "prefilled";
    private static final String savesDir = "complete";

    private LinearLayout layout;
    private Context context;

    public FormManager(Context context, LinearLayout layout, Bundle extras){
        String templateId = extras.getString(TEMPLATE_ID_FLAG, null);
        String oldReportId = extras.getString(OLD_DATA_ID_FLAG, null);
        String prefillId = extras.getString(PREFILL_ID_FLAG, null);
        if (templateId==null && oldReportId==null && prefillId==null){
            throw new IllegalArgumentException("Form Manager needs a resource");
        }
        this.context = context;
        this.layout = layout;

        if (oldReportId != null){
            loadForm(getReportStream(oldReportId));
        } else if (templateId != null){
            loadForm(getTemplateStream(templateId));
        }else if (prefillId != null){
            loadForm(getPrefillStream(prefillId));
        }
        data = new FormData();
    }

    /*
     * From the XML document generate UI, formData, and callbacks
     */
    private void loadForm(InputStream fis){
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(fis);
            document.getDocumentElement().normalize();//eliminate redundancies in the DOM

            //Process generating each <page>
            NodeList nList = document.getElementsByTagName("page");
            for (int page=0; page < nList.getLength(); page++){
                Node pageNode = nList.item(page);
                Element pageElement = (Element) pageNode;

                String name = pageElement.getAttribute("name");
                TextView pageName = new TextView(context);
                pageName.setText(name);
                float page_header_size = 40; //dp?
                pageName.setTextSize(page_header_size);

                layout.addView(new Space(context));
                layout.addView(pageName);
                processNode(pageNode, name, layout);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            try{
                if (fis != null) fis.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    //can maybe move to formManager entirely, passing context, and letting formManager load XML file, keeping reference to save later.
    //recursive factory for populating FormManager.data and FillReportActivity's UI and linking the two.
    private void processNode(Node node, String root, View parent){
        if(node.getNodeType() != Node.ELEMENT_NODE) return;
        Element e = (Element) node;
        View thisView = parent;

        String name = ParseNodeAttributes.getAttribute(e,"name", "");
        String id = ParseNodeAttributes.getAttribute(e,"id", name);
        String hint = ParseNodeAttributes.getAttribute(e, "hint", "");
        String currentValue = ParseNodeAttributes.getContent(e);

        String nodeType = e.getNodeName().toLowerCase();
        UIElement userUIElement = UIFactory.create(context, nodeType, name, currentValue, hint);
        userUIElement.showOn(layout);

        NodeList nodeList = node.getChildNodes();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node childNode = nodeList.item(index);
            processNode(childNode, root+"."+id, thisView);
        }
    }

    private static class ParseNodeAttributes{
        static int getInt(Element element, String name, int def){
            int res = def;
            String attribute = element.getAttribute(name);
            if (attribute.equals("")) return res;
            try{
                res = Integer.parseInt(attribute);
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
            return res;
        }
        static String getAttribute(Element element, String name, String def){
            String res = def;
            if (element.hasAttribute(name)) res=element.getAttribute(name);
            return res;
        }
        static String getContent(Element element){
            String res = element.getTextContent();
            return res;
        }
    }

    private InputStream getTemplateStream(String templateName){
        try {
            return context.getAssets().open(templateDir + "/" + templateName);
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
    private InputStream getReportStream(String reportName){
        try {
            return context.openFileInput(savesDir + "/" + reportName);
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
    private InputStream getPrefillStream(String prefillId) {
        try {
            return context.openFileInput(prefillDir + "/" + prefillId);
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getTemplates(Context context){
        try {
            String[] files = context.getAssets().list(templateDir);
            return Arrays.asList(files);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
    public static boolean addTemplate(Context context) {
        //TODO
        return false;
    }

    public static List<String> getOldForms(Context context) {
        String[] files = context.getDir(savesDir, Context.MODE_PRIVATE).list();
        return Arrays.asList(files);
    }

    public static OldFormMetaData getMeta(String reportName){
        return new OldFormMetaData(reportName);
    }

    public static class OldFormMetaData{
        public String name, formType;
        public Date dateModified, dateCreated;
        OldFormMetaData(String name){
            this.name = name;

        }
        public String getDateModified(){
            return dateFormat.format(dateModified);
        }
        public String getDateCreated(){
            return dateFormat.format(dateCreated);
        }
    }
    // Wkdy Mnth dd yyyy  hr(1-12):min am/pm TMZN
    public static final SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE MMM dd yyyy  KK:mm aa zzz", Locale.US);
}
