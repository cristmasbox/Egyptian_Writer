package com.blueapps.egyptianwriter.editor;

import android.content.Context;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.issuecenter.Issue;
import com.blueapps.glpyhconverter.GlyphConverter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class FileMaster {

    private File file;
    private final File path;
    private Context context;

    private ArrayList<FileListener> listeners = new ArrayList<>();
    // Content
    private Document glyphX;
    private String content;
    private Document rootDocument;

    private String mdc = "";

    // Constants
    // XML
    public static final String ROOT_TAG_GLYPHX = "ancientText";
    public static final String ROOT_TAG_DOCUMENT = "ancientDocument";
    public static final String TAG_NAME_GLYPHX = "ancientText";
    public static final String TAG_NAME_MDC = "mdc";
    public static final String TAG_NAME_SETTINGS = "settings";
    public static final String TAG_NAME_ITEM = "item";
    public static final String ATTR_NAME = "name";


    public FileMaster(Context context, File file){
        constructor(context);
        this.path = new File(context.getFilesDir() + "/Documents");
        this.file = file;
    }

    public FileMaster(Context context, String filename){
        constructor(context);
        this.path = new File(context.getFilesDir() + "/Documents");
        this.file = new File(path, filename);
    }

    private void constructor(Context context){
        this.context = context;
    }

    public void extractData(){

        try {
            //throw new FileNotFoundException();
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            reader.close();
            content = stringBuilder.toString();

            if(content.isEmpty()){
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                //root elements
                glyphX = docBuilder.newDocument();

                Element rootElement = glyphX.createElement(ROOT_TAG_GLYPHX);
                glyphX.appendChild(rootElement);
            } else {
                rootDocument = loadXMLFromString(content);

                if (rootDocument.hasChildNodes()){
                    Element rootElement = rootDocument.getDocumentElement();
                    if (Objects.equals(rootElement.getTagName(), ROOT_TAG_GLYPHX)){
                        glyphX = rootDocument;
                        this.mdc = GlyphConverter.convertToMdC(glyphX);
                    } else if (Objects.equals(rootElement.getTagName(), ROOT_TAG_DOCUMENT)) {
                        NodeList glyphxNodes = rootDocument.getElementsByTagName(TAG_NAME_GLYPHX);
                        if (glyphxNodes.getLength() > 0) {
                            Node glyphxNode = glyphxNodes.item(0);

                            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                            glyphX = docBuilder.newDocument();
                            glyphX.appendChild(glyphX.adoptNode(glyphxNode));

                        } else {
                            throw new Exception();// TODO
                        }

                        NodeList mdcNodes = rootDocument.getElementsByTagName(TAG_NAME_MDC);
                        if (mdcNodes.getLength() > 0){
                            Node mdcNode = mdcNodes.item(0);
                            NodeList childNodes = mdcNode.getChildNodes();

                            for (int i=0; i < childNodes.getLength(); i++){
                                Node node = childNodes.item(i);

                                if (node instanceof Text){
                                    Text text = (Text) node;
                                    this.mdc = text.getWholeText();
                                    this.mdc = this.mdc.trim();
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e){
            // TODO: display popup window despite activity is not running
            new Issue(context, context.getString(R.string.error_unexpected_title),
                    context.getString(R.string.error_unexpected_text),
                    "Trying to extract data: FileNotFoundException on java.io.FileInputStream: " + e.getLocalizedMessage()).show();
        } catch (Exception e) {
            // TODO: Error Handling
            throw new RuntimeException(e);
        }

    }

    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
    public static String DocumentToString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public Document getGlyphX() {
        return glyphX;
    }

    public String getContent() {
        return content;
    }

    public Document getRootDocument() {
        return rootDocument;
    }

    public String getMdc(){
        return mdc;
    }

    public void setMdc(String mdc){
        this.mdc = mdc;
        // Inform listeners
        for (FileListener listener: listeners){
            listener.onMdCChanged(this.mdc);
        }

        // calculate glyphx
        changeGlyphXWithMdC();

        // Apply changes to file
        applyContentToDocument();
        new Thread(new FileChanger(file, rootDocument)).start();
    }


    public void addFileListener(FileListener listener){
        this.listeners.add(listener);
    }

    private void changeGlyphXWithMdC(){
        new Thread(() -> {
            // calculate glyphx
            if (mdc.isEmpty()){
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = null;
                try {
                    docBuilder = docFactory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }

                //root elements
                glyphX = docBuilder.newDocument();

                Element rootElement = glyphX.createElement("ancientText");
                glyphX.appendChild(rootElement);
            } else {
                try {
                    this.glyphX = GlyphConverter.convertToGlyphXDocument(this.mdc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (FileListener listener: listeners){
                listener.onGlyphXChanged(this.glyphX);
            }
        }).start();
    }

    private void applyContentToDocument(){
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //root elements
            rootDocument = docBuilder.newDocument();

            Element rootElement = rootDocument.createElement(ROOT_TAG_DOCUMENT);

            // children
            // MdC
            Element mdc = rootDocument.createElement(TAG_NAME_MDC);
            Text mdcNode = rootDocument.createTextNode(this.mdc);
            mdc.appendChild(mdcNode);
            rootElement.appendChild(mdc);

            // GlyphX
            Node glyphXNode;
            if (this.glyphX.hasChildNodes()) {
                Element oldGlyphX = getGlyphX().getDocumentElement();
                glyphXNode = rootDocument.adoptNode(oldGlyphX.cloneNode(true));
            } else {
                glyphXNode = rootDocument.createElement(TAG_NAME_GLYPHX);
            }
            rootElement.appendChild(glyphXNode);


            rootDocument.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}
