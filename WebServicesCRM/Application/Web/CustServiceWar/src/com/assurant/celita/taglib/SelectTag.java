package com.assurant.celita.taglib;

import com.consisint.acsele.persistent.managers.AggregatedManager;
import com.consisint.acsele.persistent.managers.DBTrasforFilaManager;
import com.consisint.acsele.persistent.objects.PersistentObjectManager;
import com.consisint.acsele.product.server.Productos;
import com.consisint.acsele.template.server.*;
import com.consisint.acsele.template.server.jsp.GeneratedFormBean;
import com.consisint.acsele.util.*;
import com.consisint.acsele.util.forms.tools.FileWriter;
import com.consisint.acsele.util.logging.AcseleLogger;
import org.apache.struts.taglib.html.BaseHandlerTag;
import org.apache.struts.taglib.html.Constants;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.ResponseUtils;

import javax.servlet.jsp.JspException;
import java.util.*;

/**
 * This class represents a select tag.
 * Part of the acsel-e / struts edition framework
 * Loosely based on BodyTagSupport & org.apache.struts.taglib.html.SelectTag <p>
 * Title: SelectTag.java <br>
 * Copyright: (c) 2003 Consis International<br>
 * Company: Consis International<br>
 * @author Consis International (CON)
 * @author Gorka Siverio (GS)
 * @author Rafael Alvarez (RAF)
 * @author Cesar Rodriguez (CS)
 * @author Jes�s Le�n (JJL)
 * @author Duvelis Carao (DAC)
 * @author Dennys Aldana (DES)
 * @author Gustavo Acu�a (GA)
 * @author Luisa Ledezma (LL)
 * @author Danilo Freitez (DF)
 * @author Nelson Crespo (NC)
 * @author Luis Alberto Dom�nguez (LAD)
 * @author Belkys Hern�ndez (BCH)
 * @author Luisa Ledezma (LL)
 * @author Militza Castillo (MCC)
 * @author Carlos Ruiz (CR)
 * @author Carlos Ruiz (CR)
 * @author Leslie Chiquito (LMC)
 * @author Randy Rosales (RR)
 * @author Jos� Miguel Galea Yrausquin (JMG)
 * @author Julio Espinoza (JCE)
 * @version Acsel-e v2.2
 * <br>
 * Changes:<br>
 * <ul>
 *      <li> 2003-01-23 (GS)  Creation of the class </li>
 *      <li> 2005-03-03 (LAD) Null validation for parent, category and template values </li>
 *      <li> 2005-03-12 (NC)  Change in appendSelectOpenTag method </li>
 *      <li> 2005-03-12 (NC)  Correption in "filterTransformerData" method </li>
 *      <li> 2005-04-28 (GS)  Minor Changes </li>
 *      <li> 2005-04-29 (GS)  Merge into 2.4 </li>
 *      <li> 2005-05-17 (LAD) Ocurrencies of linkTXT were replaced by bundle </li>
 *      <li> 2005-12-16 (GS)  Categorias becomes Singleton I: Documentation </li>
 *      <li> 2005-12-20 (JES) Modified doEndTag method </li>
 *      <li> 2006-01-07 (RAF) Merge 2.4.4</li>
 *      <li> 2006-04-03 (GS-BCH) Categorias becomes Singleton II. </li>
 *      <li> 2006-06-30 (GS)  Changes in Categorias' handling. </li>
 *      <li> 2006-07-03 (MLB-LAD) Patched 1499: Changes in load y save the OperationOk, new method
 *                            for CreateOperation </li>
 *      <li> 2006-09-11 (LL)  Removed refresh list for parent-child properties </li>
 *      <li> 2007-02-01 (MCC) Modified cargarListaSegunPadre method. </li>
 *      <li> 2007-02-14 (GS-LAD) Refreshing fix. </li>
 *      <li> 2007-02-15 (MCC) Minor Changes in appendJavascript method. </li>
 *      <li> 2007-02-21 (LAD-GS) Patch applied; COT's load deleted. </li>
 *      <li> 2007-03-19 (RL)  Modified doEndTag method </li>
 *      <li> 2007-04-02 (GS)  Changes in use of split(). </li>
 *      <li> 2007-04-27 (CR)  Changes in processBody method with Split. </li>
 *      <li> 2007-05-21 (LMC)  Added Set-Get Property pK</li>
 *      <li> 2008-04-21 (RR-JMG) Added support for CoParents   <li>
 *      <li> 2008-05-13 (EG-JNN) Fixed the select tag<li>
 *      <li> 2008-09-05 (FJQ) Modified cargarListaSegunPadre Method.(Performance)<li>
 *      <li> 2008-09-12 (FJQ) Modified cargarListaSegunPadre Method.(Performance)<li>
 *      <li> 2009-01-27 (FJQ) Modified cargarListaSegunPadre Method to show elements<li>
 *      <li> 2009-01-29 (FJQ) Modified cargarListaSegunPadre Method.<li>
 *      <li> 2009-02-03 (RCH) Class cast exception handled. <li>
 *      <li> 2009-02-26 (JCE) Performance load Productos </li>
 * </ul>
 **/

public class SelectTag extends BaseHandlerTag {

    private static final AcseleLogger log = AcseleLogger.getLogger(SelectTag.class);

    public static final String OBJECT = "object";
    public static final String DESC = "desc";
    public static final String VALUE = "value";
    public static final String PK = "pk";


    private boolean visible = true;

    // The actual values we will match against, calculated in doStartTag().
    private String[] match = null;
    protected static MessageResources messages = MessageResources
            .getMessageResources(Constants.Package + ".LocalStrings");
    private String multiple = null;
    private String name = Constants.BEAN_KEY;
    private String property = null;
    private String saveBody = null;
    private String size = null;
    private String value = null;
    private ArrayList valueObj = null;
    private String text = null;
    private String bundle; // = Action.MESSAGES_KEY;
    private boolean disabled = false;
    private String key = null;
    private String locale; // = Action.LOCALE_KEY;
    private String style = null;
    private String styleClass = null;
    private String propertyPk = null;

    // Tipo de lista
    private String type = null;
    private boolean isSelect = true;

    // Nombre de la ventana a abrir
    private String newWindow = "lista";

    // Usados en el font de los links de la nueva ventana
    private String color = null;
    private String face = null;
    private String weight = null;
    private String fontsize = null;

    // Usados en la ventana a abrir
    private String width = "400";
    private String height = "520";
    private String resizable = "no";
    private String scrollbars = "yes";
    private String toolbar = "no";
    private String location = "no";
    private String directories = "no";
    private String status = "no";
    private String menubar = "no";
    private String copyhistory = "no";

    // Controlamos la paginaci�n
    private int listSize = 10;
    private int pageSize = 10;

    // Controlamos la paginaci�n
    // private String linkURL = "./pager-demo.jsp";
    private String linkURL = "/customerservices/forms/jsp/pager-demo.jsp";
    //    private String linkTXT = "Seleccionar";
    private ResourceBundle rb = ResourceBundle.getBundle("ApplicationResources");

    // Usado para que la lista pop-up se pueda armar
    private String categoria = "";
    private String plantilla = "";

    private String parentValue = "";
    private String coParent = "";
    private String numeroForma = "";
    private String selectChanged = "";

    private int tab = -1;

    private String url = "";
    private String shift = "";

    private String refresh = null;
    public static final String PAGER_DEMO_REFRESH = "pager_demo_refresh";
    public static final String LIST_TYPE = "list";
    // private final static String separator = "**->";

    /***************************************************
     * SETs & GETs
     ***************************************************/

    /**
     * It is the getter of property Pk
     * @return property Pk
     */
    public String getPropertyPk() {
        return propertyPk;
    }

    /**
     * Set a new property Pk
     * @param propertyPk
     */
    public void setPropertyPk(String propertyPk) {
        this.propertyPk = propertyPk;
    }

    /**
     * It is the getter of shift attribute
     * @return String
     */
    public String getShift() {
        return shift;
    }

    /**
     * It is the setter of shift attribute
     * @param shift String
     */
    public void setShift(String shift) {
        this.shift = shift;
    }

    /**
     * It is the getter of url attribute
     * @return String
     */
    public String getUrl() {
        return url;
    }

    /**
     * It is the setter of url attribute
     * @param url String
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * It is the getter of match attribute
     * @return String
     */
    public String[] getMatch() {
        return match;
    }

    /**
     * It is the setter of match attribute
     * @param match String[]
     */
    public void setMatch(String[] match) {
        this.match = match;
    }

    /**
     * It is the getter of multiple attribute
     * @return String
     */
    public String getMultiple() {
        return multiple;
    }

    /**
     * It is the setter of multiple attribute
     * @param multiple String
     */
    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

    /**
     * It is the getter of name attribute
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * It is the setter of name attribute
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * It is the getter of property attribute
     * @return String
     */
    public String getProperty() {
        return property;
    }

    /**
     * It is the setter of property attribute
     * @param property String
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * It is the getter of saveBody attribute
     * @return String
     */
    public String getSaveBody() {
        return saveBody;
    }

    /**
     * It is the setter of saveBody attribute
     * @param saveBody String
     */
    public void setSaveBody(String saveBody) {
        this.saveBody = saveBody;
    }

    /**
     * It is the getter of size attribute
     * @return String
     */
    public String getSize() {
        return size;
    }

    /**
     * It is the setter of size attribute
     * @param size String
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * It is the getter of value attribute
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * It is the setter of value attribute
     * @param value String
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * It is the getter of valueObj attribute
     * @return ArrayList
     */
    public ArrayList getValueObj() {
        return valueObj;
    }

    /**
     * It is the setter of valueObj attribute
     * @param valueObj ArrayList
     */
    public void setValueObj(ArrayList valueObj) {
        this.valueObj = valueObj;
    }

    /**
     * It is the getter of text attribute
     * @return String
     */
    public String getText() {
        return text;
    }

    /**
     * It is the setter of text attribute
     * @param text String
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * It is the getter of bundle attribute
     * @return String
     */
    public String getBundle() {
        return bundle;
    }

    /**
     * It is the setter of bundle attribute
     * @param bundle String
     */
    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    /**
     * It is the getter of disabled attribute
     * @return boolean
     */
    public boolean getDisabled() {
        return disabled;
    }

    /**
     * Returns a boolean to notify whether is disabled or not
     * @return boolean
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * It is the setter of disabled attribute
     * @param disabled boolean
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * It is the setter of disabled attribute
     * @param disabled String
     */
    public void setDisabled(String disabled) {
        this.disabled = ((disabled != null) && (disabled.compareToIgnoreCase("true") == 0));
    }

    /**
     * It is the getter of key attribute
     * @return String
     */
    public String getKey() {
        return key;
    }

    /**
     * It is the setter of key attribute
     * @param key String
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * It is the getter of locale attribute
     * @return String
     */
    public String getLocale() {
        return locale;
    }

    /**
     * It is the setter of locale attribute
     * @param locale String
     */

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * It is the getter of style attribute
     * @return String
     */
    public String getStyle() {
        return style;
    }

    /**
     * It is the setter of style attribute
     * @param style String
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * It is the getter of styleClass attribute
     * @return String
     */
    public String getStyleClass() {
        return styleClass;
    }

    /**
     * It is the setter of styleClass attribute
     * @param styleClass String
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * It is the getter of listSize attribute
     * @return int
     */
    public int getListSize() {
        return listSize;
    }

    /**
     * It is the setter of listSize attribute
     * @param listSize int
     */
    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    /**
     * It is the getter of pageSize attribute
     * @return int
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * It is the setter of pageSize attribute
     * @param pageSize int
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * It is the getter of type attribute
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * It is the setter of type attribute
     * @param type String
     */
    public void setType(String type) {
//        this.type = type;
        this.type = StringUtil.isEmptyOrNullValue(type) ? null : type;
//        log.debug("Type-Tag:" + this.type);
    }

    /**
     * It is the getter of newWindow attribute
     * @return String
     */
    public String getNewWindow() {
        return newWindow;
    }

    /**
     * It is the setter of newWindow attribute
     * @param newWindow String
     */
    public void setNewWindow(String newWindow) {
        this.newWindow = newWindow;
    }

    /**
     * It is the getter of color attribute
     * @return String
     */
    public String getColor() {
        return color;
    }

    /**
     * It is the setter of color attribute
     * @param color String
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * It is the getter of face attribute
     * @return String
     */
    public String getFace() {
        return face;
    }

    /**
     * It is the setter of face attribute
     * @param face String
     */
    public void setFace(String face) {
        this.face = face;
    }

    /**
     * It is the getter of weight attribute
     * @return String
     */
    public String getWeight() {
        return weight;
    }

    /**
     * It is the setter of weight attribute
     * @param weight String
     */
    public void setWeight(String weight) {
        this.weight = weight;
    }

    /**
     * It is the getter of fontsize attribute
     * @return String
     */
    public String getFontsize() {
        return fontsize;
    }

    /**
     * It is the setter of fontsize attribute
     * @param fontsize String
     */
    public void setFontsize(String fontsize) {
        this.fontsize = fontsize;
    }

    /**
     * It is the getter of width attribute
     * @return String
     */
    public String getWidth() {
        return width;
    }

    /**
     * It is the setter of width attribute
     * @param width String
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * It is the getter of height attribute
     * @return String
     */
    public String getHeight() {
        return height;
    }

    /**
     * It is the setter of height attribute
     * @param height String
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * It is the getter of resizable attribute
     * @return String
     */
    public String getResizable() {
        return resizable;
    }

    /**
     * It is the setter of resizable attribute
     * @param resizable String
     */
    public void setResizable(String resizable) {
        this.resizable = resizable;
    }

    /**
     * It is the getter of scrollbars attribute
     * @return String
     */
    public String getScrollbars() {
        return scrollbars;
    }

    /**
     * It is the setter of scrollbars attribute
     * @param scrollbars String
     */
    public void setScrollbars(String scrollbars) {
        this.scrollbars = scrollbars;
    }

    /**
     * It is the getter of toolbar attribute
     * @return String
     */
    public String getToolbar() {
        return toolbar;
    }

    /**
     * It is the setter of toolbar attribute
     * @param toolbar String
     */
    public void setToolbar(String toolbar) {
        this.toolbar = toolbar;
    }

    /**
     * It is the getter of location attribute
     * @return String
     */
    public String getLocation() {
        return location;
    }

    /**
     * It is the setter of location attribute
     * @param location String
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * It is the getter of directories attribute
     * @return String
     */
    public String getDirectories() {
        return directories;
    }

    /**
     * It is the setter of directories attribute
     * @param directories String
     */
    public void setDirectories(String directories) {
        this.directories = directories;
    }

    /**
     * It is the getter of status attribute
     * @return String
     */
    public String getStatus() {
        return status;
    }

    /**
     * It is the setter of status attribute
     * @param status String
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * It is the getter of menubar attribute
     * @return String
     */
    public String getMenubar() {
        return menubar;
    }

    /**
     * It is the setter of menubar attribute
     * @param menubar String
     */
    public void setMenubar(String menubar) {
        this.menubar = menubar;
    }

    /**
     * It is the getter of copyhistory attribute
     * @return String
     */
    public String getCopyhistory() {
        return copyhistory;
    }

    /**
     * It is the setter of copyhistory attribute
     * @param copyhistory String
     */
    public void setCopyhistory(String copyhistory) {
        this.copyhistory = copyhistory;
    }

    /**
     * It is the getter of linkURL attribute
     * @return String
     */
    public String getLinkURL() {
        return linkURL;
    }

    /**
     * It is the setter of linkURL attribute
     * @param linkURL String
     */
    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
    }

    /**
     * It is the getter of linkTXT attribute
     * @return String
     */
    public String getLinkTXT() {
        return rb.getString("pager.selection");
    }

    /**
     * It is the getter of categoria attribute
     * @return String
     */
    public String getCategoria() {
        return categoria;
    }

    /**
     * It is the setter of categoria attribute
     * @param categoria String
     */
    public void setCategoria(String categoria) {
        this.categoria = StringUtil.isEmptyOrNullValue(categoria) ? null : categoria;
    }

    /**
     * It is the getter of plantilla attribute
     * @return String
     */
    public String getPlantilla() {
        return plantilla;
    }

    /**
     * It is the setter of plantilla attribute
     * @param plantilla String
     */
    public void setPlantilla(String plantilla) {
        this.plantilla = StringUtil.isEmptyOrNullValue(plantilla) ? null : plantilla;
    }

    /**
     * It is the getter of tab attribute
     * @return int
     */
    public int getTab() {
        return tab;
    }

    /**
     * It is the setter of tab attribute
     * @param tab int
     */
    public void setTab(int tab) {
        this.tab = tab;
    }

    /**
     * It is the getter of parentValue attribute
     * @return String
     */
    public String getParentValue() {
        return parentValue;
    }

    /**
     * It is the setter of parent attribute
     * @param parent String
     */
    public void setParentValue(String parent) {
        this.parentValue = StringUtil.isEmptyOrNullValue(parent) ? null : parent;
    }

    /**
     *
     * @return String true if this property is a "coParent", false otherwise
     */
    private boolean hasCoParent() {
        return coParent != null;
    }

    /**
     *
     * @return String coParent's name if this property is a "coParent", null otherwise
     */
    public String getCoParent() {
        return coParent;
    }

    /**
     *
     * @param coParent String coParent's name if this property is a "coParent", null otherwise
     */
    public void setCoParent(String coParent) {
//        log.debug("coParent = '" + coParent + "'");
//        log.debug("coParent.getClass().getName() = '" + coParent.getClass().getName() + "'");
        this.coParent = StringUtil.isEmptyOrNullValue(coParent) ? null : coParent;
    }


    public String getSelectChanged() {
        return selectChanged;
    }

    public void setSelectChanged(String selectChanged) {
        this.selectChanged = selectChanged;
    }

    /**
     * It is the getter of numeroForma attribute
     * @return String
     */
    public String getNumeroForma() {
        return numeroForma;
    }

    /**
     * It is the setter of numeroForma attribute
     * @param numeroForma String
     */
    public void setNumeroForma(String numeroForma) {
        this.numeroForma = numeroForma;
    }

    /**
     *
     * @return boolean
     */
    public boolean isVisible() {
        return visible;
    }

    /**                                                       F
     * It is the setter of visible attribute
     * @param visible boolean
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * It is the getter of refresh attribute
     * @return String
     */
    public String getRefresh() {
        return refresh;
    }

    /**
     * It is the setter of refresh attribute
     * @param refresh String
     */
    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    /**
     *
     * @param value String
     * @return boolean
     */
    public boolean isMatched(String value) {
        if ((match == null) || (value == null)) {
            return (false);
        }
        for (int i = 0; i < match.length; i++) {
            if (value.equals(match[i])) {
                return (true);
            }
        }
        return (false);
    }

    /***************************************************
     * Manejo de los TAGs
     ***************************************************/

    /**
     *
     * @return int
     * @throws javax.servlet.jsp.JspException
     */
    public int doStartTag() throws JspException {

        // Revisamos el tipo de lista a generar - Si indico expl�citamente list o LIST (Siempre hay monoling�es).
//        isSelect = !(((type != null) && ((type.trim().equalsIgnoreCase("list")) ||
//                                         (type.trim().equalsIgnoreCase("lista")))));
//        isSelect = !(((type != null) && (type.equals(LIST_TYPE))));
        isSelect = !(LIST_TYPE.equals(type));

        // TODO: put the appendJavascript call outside the iff
        // 2004-03-30 (GS) Seteo de Visibilidad y Edici�n
        if (this.isVisible() && isSelect) {
            // Create an appropriate "form" element based on our parameters
            StringBuffer results = new StringBuffer();
            appendJavascript(results);
            appendSelectOpenTag(results);
            appendFirstRow(results);
            // Print this field to our output writer
            ResponseUtils.write(pageContext, results.toString());
        }

        // Continue processing this page
        return (javax.servlet.jsp.tagext.IterationTag.EVAL_BODY_AGAIN);
    }

    /**
     *
     * @return int
     * @throws javax.servlet.jsp.JspException
     */
    public int doAfterBody() throws JspException {
        return (SKIP_BODY); // EVAL_BODY_AGAIN to repeat the body
    }

    /**
     * This method is performed when the close tag is found.
     * @return int
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        // 2004-03-30 (GS) Seteo de Visibilidad y Edici�n
        if (this.isVisible()) {
            if (isSelect) {
                StringBuffer results = new StringBuffer();
                if (bodyContent != null) {
                    String valor = bodyContent.getString();

                    if (valor.indexOf("<option ") == -1) {
                        valor = processBody(valor);
                    }

                    if (valor.length() > 0) {
                        if (StringUtil.isEmptyOrNullValue(coParent)) {
                            results.append(this.setSelected(valor, value));
                        } else {
                            if (this.property.equals(this.selectChanged)) {//if selectquecambio
                                int index = valor.indexOf("selected=");
                                if (index != -1) {
                                    valor = valor.substring(0, index) + valor.substring(index + 15);
                                }
//                                log.error(valor);
                                results.append(this.setSelected(valor, value));
                            } else {
                                results.append(valor);
                            }
                        }
                    }
                }
                results.append("</select>");
                ResponseUtils.write(pageContext, results.toString());

            } else {

                // Escribimos el text tag
                StringBuffer linkInputType = new StringBuffer(200);
                linkInputType.append("<input type=\"text\" name=\"")
                        .append(property).append("\" value=\"");
                linkInputType
                        .append(RequestUtil.decode(
                                StringUtil.isEmptyOrNullValue(value) ? "" : value, "ISO-8859-1"));
                linkInputType
                        .append("\" onmouseover=\"showhint(this.value, this, event, '150px')\"");
                linkInputType.append(" onFocus=\"this.blur()\"");
                if (this.getReadonly()) {
                    linkInputType.append(" disabled");
                }
                linkInputType.append(" >");
                ResponseUtils.write(pageContext, linkInputType.toString());

                // Escribimos el link
                StringBuffer link = new StringBuffer(200);
                link.append("&nbsp;<a href=\"javascript:void(0)\" onClick=\"")
                        .append(((this.getReadonly()) ? "return false; " : ""))
                        .append("top.lista = window.open('','").append(newWindow)
                        .append("','width=").append(width).append(",height=").append(height)
                        .append(",resizable=").append(resizable)
                        .append(",scrollbars=").append(scrollbars)
                        .append(",toolbar=").append(toolbar).append(",location=").append(location)
                        .append(",directories=").append(directories)
                        .append(",status=").append(status)
                        .append(",menubar=").append(menubar)
                        .append(",copyhistory=").append(copyhistory)
                        .append("'); top.lista.location='").append(linkURL)
                        .append("?prop=").append(property)
                        .append("&cat=").append(categoria)
                        //.append("&temp=").append(plantilla)
                        .append("&temp=").append(plantilla).append("&").append(PAGER_DEMO_REFRESH)
                        .append("=").append(refresh)
                        .append("&select_url=").append(url)
                        .append("&propertyPk=").append(propertyPk);

                Propiedad prop = getPropertyByName(categoria, plantilla, property);

                if (this.hasCoParent()) {
                    link.append("&coParent=").append(this.coParent);
                }
                if ((prop.isDependant())) {
//LAD                    link.append("&parent='+document.").append(categoria).append("_").append(plantilla).append(".").append(prop.getParent()).append(".value");
                    link.append("&parent='+document.").append(FileWriter.formReference).append(".")
                            .append(prop.getParent()).append(".value");
                } else {
                    link.append("'");
                }
//                link.append(";\">").append(linkTXT).append("</a>");
                link.append(";");
//                Iterator children =
//                        getPropertiesChildren(categoria, plantilla, property).iterator();
//                while (children.hasNext()) {
//                    String propertyChild = (String) children.next();
//                    link.append("document.").append(FileWriter.formReference).append(".")
//                            .append(propertyChild).append(".value='';");
//                }
                link.append("\">").append(rb.getString("pager.selection")).append("</a>");
                ResponseUtils.write(pageContext, link.toString());

            }
        }
        // 2004-03-30 (GS) Seteo de Visibilidad y Edici�n
        if ((!this.isVisible()) || (this.getReadonly())) {
            StringBuffer results = new StringBuffer(200);
            results.append("<input type=\"hidden\" name=\"").append(property).append("\" value=\"")
                    .append((value == null) ? "" :
                            RequestUtil.decode(ResponseUtils.filter(value), "ISO-8859-1"))
                    .append("\">");
            ResponseUtils.write(pageContext, results.toString());
        }

        // Continue processing this page
        return (EVAL_PAGE);
    }

    /**
     * This method is performed to perform everything between an open tag and close tag.
     * @param bodyContent String
     * @return String
     */
    private String processBody(String bodyContent) {

        // 2004-03-30 (GS) Seteo de Visibilidad y Edici�n
        if ((!isSelect) || (!this.isVisible())) {
            return "";
        }

        StringBuffer result = new StringBuffer(bodyContent.length());

        if (bodyContent.length() > 0) {
            for (StringTokenizer stringTokenizer = new StringTokenizer(bodyContent, "\"");
                 stringTokenizer.hasMoreTokens();) {
                String pair = stringTokenizer.nextToken().trim();
                if (pair.length() > 0) {
                    StringTokenizer pairTokenizer = new StringTokenizer(pair, ":");

                    String value = pairTokenizer.nextToken().trim();
                    String depends;
                    try {
                        depends = pairTokenizer.nextToken();
                    } catch (NoSuchElementException e) {
                        depends = "";
                    }

//                    this.coParent
//                    log.debug("[*GSG*] this.coParent = '" + this.coParent + "'");
//                    boolean esUnCoPadre = "true".equals(this.coParent);
                    boolean isCoParent = this.hasCoParent();
//                   log.debug("[*GSG*] isCoParent = '" + isCoParent + "'");
//                   log.debug("[*GSG*] parentValue = '" + parentValue + "'");
//                   log.debug("[*GSG*] depends = '" + depends + "'");

//                    MODIFICAR EL IF PAAR QUE NO FINTRE SI T>IENE COPARENT, Y CUADRAR EL NOMBRE
                    if ((Arrays.asList(StringUtil.oldSplit(depends,
                                                           TransformadorFila.TRANSFORMER_SEPARATOR)).contains(
                            parentValue)) || isCoParent) {
                        String trimmedValue = value.trim();
                        result.append("<option value=\"").append(trimmedValue).append((isCoParent &&
                                                                                       depends !=
                                                                                       null &&
                                                                                            depends.equals(
                                                                                                    parentValue)) ?
                                                                                                                  "\" selected=\"true" :
                                                                                                                  "")
                                .append("\">").append(trimmedValue).append("</option>");
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Release attributes, so modified the state.
     */
    public void release() {
        super.release();
        match = null;
        multiple = null;
        name = Constants.BEAN_KEY;
        property = null;
        saveBody = null;
        size = null;
        value = null;
        valueObj = null;
        style = null;
        coParent = null;
        selectChanged = null;
    }

    /***************************************************
     * Llamadas internas desde los m�todos anteriores
     ***************************************************/

    /**
     * Builds Javascripts with features
     * @param results StringBuffer
     */
    private void appendJavascript(StringBuffer results) {

        /**
         * No hemos contemplado el caso de m�s de una forma en la misma p�gina.
         * Antes siempre trabaj�bamos con la primera forma (0).
         * El par�metro de inclusi�n de la forma 'numeroForma' indica a qu� forma es a
         * la que se va a hacer submit
         * <%=((request.getParameter(\"numeroForma\") != null) ? request.getParameter(\"numeroForma\") : \"0\" ) %>
         */

        this.numeroForma = StringUtil.isEmpty(this.numeroForma) ? "0" : this.numeroForma;

        String action;
        if ((StringUtil.isEmptyOrNullValue(this.url))) {
            action = "   forma.action='/WController/forms/jsp/container.jsp'; \n";
        } else {
            action = "   forma.action='" + this.url + "'; \n";
        }

        if (this.refresh.equalsIgnoreCase("true") || (this.hasCoParent())) {

//            results.append("<Script Language=\"JavaScript\"> \n <!-- \n function doRefresh" + property + "() { \n");
            results.append("<script Language=\"JavaScript\"> \n <!-- \n function doRefresh")
                    .append(property).append("(forma, selectValue) { \n")
                    .append("	if (forma.tab) { \n		forma.tab.value = ").append(tab)
                    .append("; \n	}\n")
                    .append("	forma.selectChanged.value = selectValue;\n")
                    .append(action)
                    .append("	forma.submit();\n}\n--></script>\n");
        }
    }

    /**
     * Builds select tags with a specific features
     * @param results StringBuffer
     * @throws javax.servlet.jsp.JspException Error
     */
    private void appendSelectOpenTag(StringBuffer results) throws JspException {

        // 2004-04-19 (GS) Refrescamiento autom�tico
        results.append("<select onchange=\"");
        if (getOnchange() != null) {
            results.append(this.getOnchange()).append(";");
        }
        if (this.refresh.equalsIgnoreCase("true")) {
            results.append("doRefresh").append(property).append("(this.form, this.name)");
        }
        results.append("\" name=\"");

        if (indexed) {
            prepareIndex(results, name);
        }
        results.append(property).append("\"");

        if (accesskey != null) {
            results.append(" accesskey=\"").append(accesskey).append("\"");
        }
        if (multiple != null) {
            results.append(" multiple=\"multiple\"");
        }
        if (size != null) {
            results.append(" size=\"").append(size).append("\"");
        }
        if (tabindex != null) {
            results.append(" tabindex=\"").append(tabindex).append("\"");
        }
        results.append(prepareEventHandlers()).append(prepareStyles());
        // 2004-03-30 (GS) Seteo de Visibilidad y Edici�n
        results.append((this.getReadonly()) ? " disabled" : "").append(">");
    }

    /**
     * Adds the first row
     * @param results StringBuffer
     */
    private void appendFirstRow(StringBuffer results) {
        results.append("<option value=\"\">----</option>");
    }

    /**
     * Gets property by name
     * @param categoria String
     * @param plantilla String
     * @param property String
     * @return Propiedad
     */
    public static Propiedad getPropertyByName(String categoria, String plantilla, String property) {
        log.debug("categoria = " + categoria);
        log.debug("plantilla = " + plantilla);
        log.debug("property = " + property);
        Categorias categories = Categorias.getBean(Categorias.ALL_TEMPLATES_STATE);
        log.debug("categories = " + categories);
        ConfigurableObjectType cot = (ConfigurableObjectType) categories
                .get(CotType.getCotType(categoria), plantilla);
//        cot.load();

        return cot.get(property);
    }

    /**
     * Gets children properties names
     * @param categoria String
     * @param plantilla String
     * @param prop String
     * @return Children properties
     */
    public static ArrayList getPropertiesChildren(String categoria, String plantilla, String prop) {
        ArrayList childs = new ArrayList();
        if (StringUtil.isEmptyOrNullValue(categoria) || StringUtil.isEmptyOrNullValue(plantilla)) {
            return childs;
        }
        Categorias categories = Categorias.getBean(Categorias.ALL_TEMPLATES_STATE);
        ConfigurableObjectType cot = (ConfigurableObjectType) categories
                .get(CotType.getCotType(categoria), plantilla);
        Enumeration props = cot.getPropiedades().elements();
        while (props.hasMoreElements()) {
            Propiedad propAux = (Propiedad) props.nextElement();
            if (propAux.getParent() != null && propAux.getParent().equals(prop)) {
                childs.add(propAux.getDesc());
            }
        }
        return childs;
    }

    /**
     * Sets selected during the construction of a particular page
     * @param text String
     * @param value String
     * @return String
     */
    private String setSelected(String text, String value) {
        StringBuffer buffer = new StringBuffer(text);
        if ((value != null) && (value.trim().length() > 0)) {
            //int index = text.indexOf("value=\"" + Util.unsubs(value) + "\"");
            int index = text.indexOf("value=\"" + value + "\"");
            if (index > -1) {
                buffer.insert(index, "selected=\"true\" ");
            }
        }
        return buffer.toString();
    }

    /***************************************************
     * Llamadas externas para el pop-up
     ***************************************************/

    /**
     * Loads a list considering the parent
     * @param categoria String
     * @param plantilla String
     * @param prop Propiedad
     * @param parentValue String
     * @param coParent String
     * @param filter String
     * @param search_type String
     * @param numPage String
     * @return Vector
     */
    // TODO: Bajar esta carga de las litas, y su filtrado, a un nivel mas bajo.
    // TODO: Para una guia, busca los usos de THIRDPARTY_WITH_ROLE_SYSTEM_FUNCTION
    public static Vector cargarListaSegunPadre(String categoria, String plantilla, Propiedad prop,
                                               String parentValue, String coParent, String filter,
                                               String search_type, String numPage) {
        try {
            if (StringUtil.isEmptyOrNullValue(categoria) ||
                StringUtil.isEmptyOrNullValue(plantilla)) {
                return new Vector();
            }

            String function =
                    prop.getFilterTransformer() == null ? "" : prop.getFilterTransformer();

            Vector data = new Vector();

            if (function.startsWith(Transformador.THIRDPARTY_WITH_ROLE_SYSTEM_FUNCTION)) {
                DBTrasforFilaManager dbTrasforFilaManager = DBTrasforFilaManager.getInstance();
                ThirdPartyType cot =
                        (ThirdPartyType) Categorias.getDefaultTemplate(CotType.THIRDPARTY);
                String field = dbTrasforFilaManager.getThirdPartyNameStatement(cot);
                String queryFields = "tpStatic.TPT_Id, " + field + " ";

                StringBuilder baseQuery = new StringBuilder(dbTrasforFilaManager.getThirdPartyWithRoleQuery());
                //log.debug("QUERY (BASE) " + baseQuery.toString());

                if (!StringUtil.isEmptyOrNullValue(filter)) {
                    baseQuery.append(" AND UPPER (").append(field).append(") LIKE '%")
                            .append(filter.toUpperCase())
                            .append("%'");
                    //    log.debug("QUERY (THIRD) " + baseQuery.toString());
                }

                AggregatedManager.addQueryPostfix(search_type, baseQuery, "", 1, queryFields,
                                                  Integer.parseInt(numPage), true,false, null);
                //log.debug("QUERY (FINAL) " + baseQuery.toString());
                List vector = JDBCUtil.doQueryList(baseQuery.toString());

                for (int i = 0; vector.size() > i; i++) {
                    Properties properties = (Properties) vector.get(i);
                    String descValue = "".equals(properties.getProperty("DESCRIPTION")) ?
                                       PersistentObjectManager.THREE_DASHES :
                                       properties.getProperty("DESCRIPTION");
                    String pkValue = properties.getProperty("PK");
                    Hashtable elementoAux = new Hashtable();
                    elementoAux.put(DESC, descValue);
                    elementoAux.put(VALUE, descValue);
                    elementoAux.put(PK, pkValue);
                    data.add(elementoAux);
                    log.debug("newLista = " + data.get(i));
                }

            } else if (function.startsWith(Transformador.PRODUCT_SYSTEM_FUNCTION)) {

                // Set a Products type Active, Commercial, Visible
                Hashtable filteredProducts = Productos.getInstance().getAllOkProducts();
                Enumeration keys = filteredProducts.keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String desc = (String) filteredProducts.get(key);

                    if (isOkByFilter(desc, filter)) {
                        Hashtable elementoAux = new Hashtable();
                        elementoAux.put(DESC, desc);
                        elementoAux.put(VALUE, desc);
                        elementoAux.put(PK, key);
                        data.add(elementoAux);
                    }
                }

            } else if (prop.getTransformador().getTransformadores().size() >
                       Integer.parseInt(AcseleConf.getProperty("ntransformers"))) {

                DBTrasforFilaManager dbTrasforFilaManager = DBTrasforFilaManager.getInstance();
                String queryFields = "trf.DESCRIPTION ";
                StringBuilder baseQuery =
                        new StringBuilder(dbTrasforFilaManager.getTransformersListQuery(prop.getPk()));


                if (!StringUtil.isEmptyOrNullValue(filter)) {
                    baseQuery.append(" AND UPPER (").append(queryFields).append(") LIKE '%")
                            .append(filter.toUpperCase())
                            .append("%'");
                    //log.debug("QUERY (THIRD) " + baseQuery.toString());
                }
                AggregatedManager.addQueryPostfix(search_type, baseQuery, "", 1, queryFields,
                                                  Integer.parseInt(numPage), true,false, null);
                //log.debug("QUERY (FINAL) " + baseQuery.toString());
                List vector = JDBCUtil.doQueryList(baseQuery.toString());
                //log.debug("vector.size()" + vector.size());


                for (int i = 0; vector.size() > i; i++) {
                    log.debug("vector.get(i)" + vector.get(i));
                    Properties properties = (Properties) vector.get(i);
                    String descValue = "".equals(properties.getProperty("DESCRIPTION")) ?
                                       PersistentObjectManager.THREE_DASHES :
                                       properties.getProperty("DESCRIPTION");
                    String pkValue = properties.getProperty("PK");
                    List descValues = AcseleLabels.loadLabelsByTransformerDesc(descValue);
                    log.debug("list,size()" + descValues.size());
                    TransformerLabel transformerDesc = (TransformerLabel) descValues.get(0);
                    log.debug("list" + descValues.get(0));
                    descValue = transformerDesc.getName();
                    log.debug("descValue_VAL = " + descValue);
                    Hashtable elementoAux = new Hashtable();
                    elementoAux.put(DESC, descValue);
                    elementoAux.put(VALUE, descValue);
                    //elementoAux.put(SelectTag.PK, pkValue);
                    data.add(elementoAux);
                    //log.debug("newLista = " + data.get(i));
                }

            } else {
                // Esto carga las propiedades sin lista del sistema, o con alguna lista que no
                // sea una de las anteriores
                data = prop.getTransformador().getData();
                if ((categoria != null) && (plantilla != null) && (parentValue != null)) {
                    data = filterTransformerData(data, prop, parentValue, coParent);
                }

                if (!StringUtil.isEmptyOrNullValue(filter)) {
                    // If we have a filtered search
                    Vector temporalList = new Vector();
                    for (int i = 0; i < data.size(); i++) {
                        TransformadorFila elemento = (TransformadorFila) data.elementAt(i);
                        log.debug("data.elementAt(i)" + data.elementAt(i));
                        log.debug(">>> elemento[" + i + "] = '" + elemento + "'");
                        String tF = elemento.getDesc();
                        // Adding the elements to be showed
                        if (tF.toLowerCase().startsWith(filter.toLowerCase())) {
                            temporalList.add(data.get(i));
                        }
                    }
                    data = temporalList;
                }

                for (int i = 0; i < data.size(); i++) {
                    Hashtable data2 = new Hashtable();
                    data2.put(OBJECT, data.get(i));
                    Object object = data.elementAt(i);
                    String value = "";
                    if(object instanceof TransformadorFila){
                        value = ((TransformadorFila) object).getDesc();
                    } else {
                        value = object.toString();
                    }
                    data2.put(DESC, value);
                    data2.put(VALUE, value);
                    data.setElementAt(data2, i);
                }
            }

            return data;

        } catch (Exception e) {
            log.error("Error loading values to show in the pager page.", e);
            return new Vector();
        }
    }

    public static String getTotalPages(StringBuffer query, String filter, boolean anyTransformer)
            throws Exception {
        String field;
        if (!StringUtil.isEmptyOrNullValue(filter)) {
            DBTrasforFilaManager dbTrasforFilaManager = DBTrasforFilaManager.getInstance();
            if (anyTransformer) {
                field = "trf.DESCRIPTION ";
            } else {
                ThirdPartyType cot =
                        (ThirdPartyType) Categorias.getDefaultTemplate(CotType.THIRDPARTY);
                field = dbTrasforFilaManager.getThirdPartyNameStatement(cot);
            }
            //log.debug("field  = " + field);
            query.append(" AND UPPER (").append(field).append(") LIKE '%")
                    .append(filter.toUpperCase())
                    .append("%'");
        }
        List vector = JDBCUtil.doQueryList(query.toString());
        String totalRows = String.valueOf(vector.size());
        //log.debug("totalRows = " + totalRows);
        double totalPage = Math.ceil(Double.parseDouble(totalRows) /
                                     Integer.parseInt(AcseleConf.getProperty("recordDisplay")));
        int totalPageInt = (int) totalPage;
        String numTotalPage = String.valueOf(totalPageInt);
        //log.debug("numTotalPage new = " + numTotalPage);
        return numTotalPage;
    }

    private static boolean isOkByFilter(String desc, String filter) {
        return StringUtil.isEmptyOrNullValue(filter) || StringUtil.isEmptyOrNullValue(desc) ||
               desc.toLowerCase().startsWith(filter.toLowerCase());
    }

    /**
     * This method is a filter regarding the data of a particular transformer
     * @param data Vector
     * @param prop Propiedad
     * @param parentValue String
     * @param coParent String
     * @return Vector
     */
    private static Vector filterTransformerData(Vector data, Propiedad prop, String parentValue,
                                                String coParent) {
        if (prop.isDependant() && (coParent == null)) {
            Vector lista = new Vector();
            for (Iterator iter = data.iterator(); iter.hasNext();) {
                TransformadorFila transformadorFila = (TransformadorFila) iter.next();
                if ((transformadorFila != null) && (transformadorFila.getParentValues() != null) &&
                    (Arrays.asList(transformadorFila.getParentValues()).contains(parentValue))) {
                    lista.add(transformadorFila);
                }
            }
            return lista;
        } else {
            return data;
        }
    }

    /**
     * Gets the property's parent value
     * @param isDependant boolean true if the property is a child, false otherwise
     * @param parentValue String request.getParameter(parent)
     * @param templateValues GeneratedFormBean with properties' data
     * @param data Hashtable with properties' data
     * @param parent String parent property's name
     * @return String parent's value
     */
    public static String getParentValue(boolean isDependant, String parentValue,
                                        GeneratedFormBean templateValues, Hashtable data,
                                        String parent) {
        if (isDependant) {
            parentValue = StringUtil.isEmptyOrNullValue(parentValue) ? templateValues.get(parent) :
                          parentValue;
            if (parentValue == null) {
                String newValue = (String) data.get(parent);
                return (newValue == null) ? "----" : newValue;
            } else {
                return parentValue;
            }
        } else {
            return null;
        }
    }

}
