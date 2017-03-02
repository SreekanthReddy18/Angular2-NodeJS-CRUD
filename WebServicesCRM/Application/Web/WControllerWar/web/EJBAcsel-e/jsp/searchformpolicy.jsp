<%--
Title: searchformpolicy.jsp
Copyright: (c) 2002 - 2004 by Consis International
Company: Consis International
Version: Acsel-e v2.2
Date creation: yyyy-MM-dd
Author: Belkys Hernández (BCH)
Author: Fanny Sarabia (FS)
Author: Fausto Iocchi (FI)
Author: Juan Castillo (JC)
Author: Sonsire Vidal (SV)
Author: Gorka Siverio (GS)
Author: Alvaro Menendez La Cruz (AML)
Author: Clara Carrera (SUN)
Author: Leslie Chiquito (LMC)
Author: Alejandro Manuel Méndez (AMM)
Author: Eduardo Urra (EUP)
Author: Suely Rodríguez (SR)
Author: Luis Ramirez (LR)
Author: Shiradit Moya (SML)
Author: Miguel Bautista (MAB)
Author: Gilberto Payares Díaz (GPD)
Author: Manuel Yepez (MSY)
Author: Alfredo Reyes G. (ARG)
Author: Erika Valerio (EDV)

Modified by:
	2003-12-02 (BCH)	Some changes
	2005-09-22 (LL)		Fixed wrong character in the include tag
	2005-12-12 (BCH)	Modified thirparty tab call. Removed unused JavaScript functions.
	2006-03-15 (RGP)	Replace DBProductManager.simpleLoad() by Productos.getMappingCopy().
	2006-05-18 (AML)	Active Products list filtered by Productos.getActiveProducts.
	2006-07-02 (SUN-GS-FJQ-RAF) Fix.
	2006-09-25 (SUN-GS)	Product label handling.
	2007-02-12 (SUN)	Performance code
	2007-03-09 (FJQ)	Modified search_search command.
	2007-03-13 (GS)		Performance code temporary removed.
	2007-03-20 (FJQ)	Performance changes in search Policy.
	2007-07-22 (LMC)	Replace Productos.getInstance().getActiveProducts() by Productos.getAllOkProducts()
	2007-06-07 (EEG)	Changes to handle PendingChange.
	2007-09-17 (PDR)	Fix the problem with the clear Buttom
	2008-02-12 (SUN)	change load Locale
	2008-06-30 (AMM)	Minor syntactic changes.
	2008-09-12 (EUP)	Include library acsele.js
	2008-09-17 (EUP)	Minor Changes.
	2009-04-27 (AMM)	Now is possible to perform both "conventional" and "enhanced" searching method for policies.
						The first one uses the same searching method used (witch uses no temporary tables).
						The second one uses the new searching method (with timed sessions and temporary
						tables). There is a new request parameter called "typeOfSearch" that enable the
						one or the other. -1 means "conventional" and 0 means "enhanced".
	2010-09-28 (SR) 	Minor change for policy search command.
	2011-02-02 (LR) 	Put lifeOperation parameter to request and some other minor changes.
	2011-13-10 (SML)	Add field to search by polizeId
	2013-06-21 (MAB)	Added a hidden tag to validation
	2013-08-02 (GPD)	Added a new combo-box for search policy by product´s state
	2013-08-29 (MSY)	Modified form action so that productId travelled in the request. ACSELE-6026
	2013-09-02 (MSY)	Fix ACSELE-6068.
	2013-09-16 (ARG)	Changes were made to fix a consultation policy movements
	2013-10-01 (EMGC)   Changes for filtering product list by those associated with the workflow
	2013-11-15 (EDV)    Add parameter formInitial to control error display stored in session at the beginning (ACSELE-7030)

--%>

<%@ page language="java" %>
<%--<%@ page errorPage="/common/error.jsp" %>--%>
<%@ page contentType="text/html" %>

<%@ page import="com.consisint.acsele.UserInfo,
                 com.consisint.acsele.policy.search.SearchParametersInfo,
                 com.consisint.acsele.policy.search.handlers.SearchRequestHandler,
                 com.consisint.acsele.product.server.Productos,
                 com.consisint.acsele.util.StringUtil" %>
<%@ page import="com.consisint.acsele.util.forms.tools.FileWriter" %>
<%@ page import="com.consisint.acsele.workflow.beans.impls.persister.ProductProcessPersister" %>
<%@ page import="java.util.*" %>
<%@ page import="com.consisint.acsele.product.server.CasoCRM" %>
<%@ page import="com.consisint.acsele.util.AcseleConf" %>
<%@ page import="com.consisint.acsele.ClientInfo" %>

<%@ taglib uri="/WEB-INF/struts/struts-html.tld" prefix="html" %>
<%@ taglib uri="/logic" prefix="logic" %>
<%--<%@ taglib uri="optimize" prefix="opt" %>--%>
<%--<opt:Compress>--%>


<html:html locale="true">
<%
    String isFacultative = "";
    if(request.getParameter("isFacultative") != null){
        isFacultative = request.getParameter("isFacultative");
    }
    String isReinsurance = "";
    if(request.getParameter("isReinsurance") != null){
        isReinsurance = request.getParameter("isReinsurance");
    }
    boolean isPendingChange = !StringUtil
            .isEmptyOrNullValue(request.getParameter("isPendingChange")) || request.getAttribute(
            "isPendingChange") != null;
    if (isPendingChange) {
        request.setAttribute("isPendingChange", "yes");
    }

    String lifeOperation = request.getParameter("lifeOperation");
    lifeOperation = lifeOperation != null ? lifeOperation : (String) request.getAttribute("lifeOperation");
    boolean isLifeOperation = !StringUtil.isEmptyOrNullValue(lifeOperation);
    String lifeParam = isLifeOperation ? "lifeOperation=" + lifeOperation : "";
    if (isLifeOperation) {
        request.setAttribute("lifeOperation", lifeOperation);
    }

    SearchParametersInfo searchParametersInfo =
            (SearchParametersInfo) session.getAttribute("searchParametersInfo");

    int orderField = request.getAttribute("_orderField") != null ?
                     Integer.parseInt((String) request.getAttribute("_orderField")) :
                     Integer.parseInt(searchParametersInfo.getOrderField());

    long productId = request.getAttribute("_productId") != null ?
            Long.parseLong(request.getAttribute("_productId").toString()) :
                    searchParametersInfo.getProductId();

    String stateProductId = request.getAttribute( "_stateProductId" ) != null ? request.getAttribute("_stateProductId").toString() : "";

    Object clientName = request.getAttribute("_clientName") != null ?
            request.getAttribute("_clientName").toString() : searchParametersInfo.getClientName();

    Object insuredName = request.getAttribute("_insuredName") != null ?
                         request.getAttribute("_insuredName").toString() :
            searchParametersInfo.getInsuredName();
    Object policyId = request.getAttribute("_policyId") != null ?
                         request.getAttribute("_policyId").toString() :
            searchParametersInfo.getPolicyId();
    String crmNumberCase = request.getAttribute( "_crmNumberCase" ) != null ? request.getAttribute("_crmNumberCase").toString() : "";
     String fromDateShow = request.getAttribute("_fromDateShow") != null ?
             request.getAttribute("_fromDateShow").toString() :
             searchParametersInfo.getFromDateShow();
    String toDateShow = request.getAttribute("_toDateShow") != null ?
            request.getAttribute("_toDateShow").toString() :
            searchParametersInfo.getToDateShow();

    String forma =
            searchParametersInfo.getTemplateTypeForm(searchParametersInfo.getCurrentTemplateType());

    ResourceBundle messages = ResourceBundle.getBundle("SearchMessageBundle", UserInfo.getLocale());

    ResourceBundle rb = ResourceBundle.getBundle("PolicyToolMessagesBundle", UserInfo.getLocale());

    ResourceBundle frontEndMessages = ResourceBundle.getBundle( "FrontEndMessagesBundle", UserInfo.getLocale());

    ArrayList productList;

    ArrayList stateProductList = new ArrayList();

    request.getSession().removeAttribute("productIds");

    Long operationTypeId=null;

    String fromWF= (String) request.getAttribute("fromWF");
    if(StringUtil.isEmptyOrNullValue(fromWF)){
        fromWF= request.getParameter("fromWF");
        if(StringUtil.isEmptyOrNullValue(fromWF)){
            fromWF=(String) request.getSession().getAttribute("fromWF");
            if(StringUtil.isEmptyOrNullValue(fromWF)){
                fromWF="false";
            }
        }
    }
    String fomWFBK = request.getSession().getAttribute("fromWFBK")!= null ? (String) request.getSession().getAttribute("fromWFBK") : "false";
    //Parche
    if(!fromWF.equals("true") && fomWFBK.equals("true")){
        request.getSession().setAttribute("fromWF",fomWFBK );
        fromWF = fomWFBK;
    }

    if(fromWF.equals("true") && request.getSession().getAttribute("operationTypeId")!=null &&
            ProductProcessPersister.Impl.getInstance().hasProductsAssociatedByOperationTypeId(
                    (Long) request.getSession().getAttribute("operationTypeId"))){

        operationTypeId = (Long) request.getSession().getAttribute("operationTypeId");

        Hashtable<String, String> products = ProductProcessPersister.Impl.getInstance()
                .getActiveProductNamesByProcess(operationTypeId);
        productList = new ArrayList(products.entrySet());

        String productIds = "";
        for(String id : products.keySet()){
            productIds += id+",";
        }
        if(!productIds.isEmpty()){
            productIds = productIds.substring(0,productIds.length()-1);
            request.getSession().setAttribute("productIds",productIds);
        }
    }else{
        Hashtable products = Productos.getInstance().getActiveProducts();
        Productos.getInstance().getProductsByUser(products);
        productList = new ArrayList(products.entrySet());
    }


    Collections.sort(productList, new Comparator<Map.Entry>() {
        public int compare(Map.Entry o1, Map.Entry o2) {
            return o1.getValue().toString().compareToIgnoreCase(o2.getValue().toString());
        }
    });
    request.setAttribute("fromWF", request.getParameter("fromWF"));

    //linea cambiada debido a ticket ACSELE-6068.
    //String entered = request.getParameter("entered");
    String entered = StringUtil.defaultIfEmptyOrNull(request.getParameter("entered"), null);

    Hashtable stateProducts = Productos.getInstance().getAllStateProducts();

    String searchResult = ( String ) request.getAttribute( "stateProductList" );
    String productIdFromServlet = ( String ) request.getAttribute( "_productId" );

    if( null == searchResult || searchResult.isEmpty() && ( null == productIdFromServlet || productIdFromServlet.isEmpty() ) )
    {
        stateProductList = new ArrayList( stateProducts.entrySet() );
    }
    else
    {
        Hashtable<String, String> myStateProductList =  new Hashtable<String, String>();

        if( null != searchResult && !searchResult.isEmpty() )
        {
            String temp = null;
            int indice = 0;
            while( !searchResult.isEmpty() )
            {
                indice = searchResult.indexOf( ',', 0 );
                if( -1 == indice )
                {
                    break;
                }
                else
                {
                    temp = searchResult.substring( 0, indice );
                    myStateProductList.put( temp, temp );
                    searchResult = searchResult.substring( indice + 1 );
                }
            }
            stateProductList = new ArrayList( myStateProductList.entrySet() );
        }
    }

    String lookForOper = request.getParameter("lookForOper");

   if(!fromWF.equals("true") ){
        operationTypeId=null;
        request.getSession().removeAttribute("operationTypeId");
       request.getSession().setAttribute("fromWF", fomWFBK);
    }

    String isSearchPolicyCreateFacultative = "";
    if(request.getParameter("isSearchPolicyCreateFacultative") != null){
        isSearchPolicyCreateFacultative = request.getParameter("isSearchPolicyCreateFacultative");
        session.setAttribute("isSearchPolicyCreateFacultative",isSearchPolicyCreateFacultative);
        request.removeAttribute("isSearchPolicyCreateFacultative");
    }
%>

<head>
<script type="text/javascript">


 function searchProductStates( myform, product )
 {
     myform.action = '/WController/policy/stateProducList.do?lookForOper=<%=lookForOper%>&productID=' + product;
     <% if(isFacultative != null && !isFacultative.equals("")){ %>
        myform.action = myform.action + "&isFacultative=" + <%= isFacultative %>;
     <% }else if(isReinsurance != null && !isReinsurance.equals("")){ %>
     myform.action = myform.action + "&isReinsurance=" + <%= isReinsurance %>;
     <% } %>
     myform.submit();
 }
</script>





<!-- start of the document caching code -->
<META http-equiv="pragma" content="no-cache">
<META http-equiv="expires" content="0">
<%--<%@ page import="com.consisint.acsele.util.AcseleConf" %>--%>
<%--<meta http-equiv="Content-Type" content="text/html; charset=<%=AcseleConf.getProperty("charsetPage")%>">--%>
<!-- end of the document caching code -->

<title><%=rb.getString("pageTitle.searchPolicy")%>
</title>
<html:base />
<link rel="stylesheet" href="/WController/webStyles/AcselStyle_Search.css" type="text/css">
<script language=javascript src="/WController/forms/scripts/acsele.js"></script>
<script language="JavaScript" src="/WController/webmenus/XMLMenuScript.js"></script>
<link rel="stylesheet" type="text/css" href="/WController/webmenus/menuStyle.css">
<script language="JavaScript1.2" src="/WController/forms/scripts/forms.js"></script>

<style type="text/css">
    .LockOff {
        display: none;
        visibility: hidden;
    }

    .LockOn {
        display: block;
        visibility: visible;
        position: absolute;
        z-index: 999;
        top: 0px;
        left: 0px;
        width: 105%;
        height: 130%;
        text-align: center;
        padding-top: 20%;
        color: #FFF;
        /* el color de fondo transparente para IE */
        background-color: transparent;
        /* el color de fondo usando rgba() */
        background-color: rgba(0, 0, 0, 0.3);
        /* el filtro de IE */
        filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#80000000',endColorstr='#80000000');
    }

    .LockMsg{
        width: 100px;
        height: 15px;
        border: 1px;
        border-style: solid;
        background-color: rgb(153,178,191);
        position: relative;
    }
</style>

<script language="JavaScript">

function skm_LockScreen(str){
    var lock = document.getElementById('skm_LockPane');
    if (lock){
        lock.className = 'LockOn';

        var lock_msg = document.getElementById('skm_LockPane_msg');
        lock_msg.className = 'LockMsg';
        lock_msg.innerHTML = str;
    }
}

function addStaticParameter(form) {

    var names = new Array("_orderField", "_productId", "_stateProductId" ,"_clientName", "_insuredName", "_policyId", "_crmNumberCase", "entered","_fromDateShow","_toDateShow");
    for (i = 0; i < names.length; i++) {
        var oInput = document.createElement("INPUT");
        oInput.name = names[i];
        oInput.id = names[i];
        oInput.type = "hidden"
        form.appendChild(oInput);
    }
}

function clearParameters(parLimpiar) {
    for (i = 0; i < parLimpiar.length; i++) {
        if (parLimpiar[i].type == "select-one")
            parLimpiar[i].value = 0;
        else if (parLimpiar[i].type == "text")
            parLimpiar[i].value = "";
    }
    document.getElementById("_idProduct").value = '';
}

function getProductID(){
     if(document.getElementById("_idProduct").value==''
             || document.getElementById("_idProduct").value=='0'){
         return '';
     }else{
         return '&_productId='+document.getElementById("_idProduct").value;
     }
}

function isFormEmpty(myform) {
    var elem;

    addParameter(myform, document.getElementById("_orderField"));
    addParameter(myform, document.getElementById("_productId"));
    addParameter(myform, document.getElementById("_clientName"));
    addParameter(myform, document.getElementById("_insuredName"));
    addParameter(myform, document.getElementById("_policyId"));
    addParameter(myform, document.getElementById("_crmNumberCase"));
    addParameter(myform, document.getElementById("entered"));
    addParameter(myform, document.getElementById("_stateProductId"));
    addParameter(myform, document.getElementById("_fromDateShow"));
    addParameter(myform, document.getElementById("_toDateShow"));

    for (var i = 0; i < myform.elements.length; i++) {
        elem = myform.elements[i];
        if (elem.tagName == "INPUT") {
            if (elem.getAttribute("type") != "submit") {
                if (elem.getAttribute("type") != "reset") {
                    if (elem.getAttribute("type") == "radio") {
                        if (elem.checked) return false;
                    } else if ((elem.getAttribute("value").length > 0)) {
                        return false;
                    }
                }
            }
        } else if (elem.tagName == "SELECT") {
            if (elem.getAttribute("name") != "insuranceObject") {
                if (elem.getAttribute("value") != null && elem.getAttribute("value") != "") {
                    return false;
                }
            }
        }
    }
    //end for
    return true;
}
var paso = false;

function updateTypeOfSearch(myform, typeOfSearch) {
        myform.typeOfSearch.value = typeOfSearch;
    }

function setCommand(myform, action, nextpage, state, templatetype) {
    myform.action = "/WController/servlet/ServletPolicy?" +
    "lookForOper=<%=lookForOper%><%=("true".equals(request.getParameter("fromWF")))?"&" +
             "fromWF=true":""%>"+getProductID()+"<%=isLifeOperation?"&":""%><%=lifeParam%>";

    <% if(isFacultative != null && !isFacultative.equals("")){ %>
        myform.action = myform.action + "&isFacultative=" + <%= isFacultative %>;
    <% } else if(isReinsurance != null && !isReinsurance.equals("")){ %>
        myform.action = myform.action + "&isReinsurance=" + <%= isReinsurance %>;
    <% } %>

    if (!paso) {
        if (isFormEmpty(myform)) {
            if (action == "submit") {
                myform.command.value = "search_search";
                myform.executeCount.value = "true";
                myform.nextcommand.value = "";
            } else {
                paso = true;
                myform.command.value = "getform_search";
                myform.executeCount.value = "true";
                myform.nextcommand.value = "";
                myform.nextpage.value = nextpage;
                myform.state.value = state;
                myform.templatetype.value = templatetype;
                myform.submit();
            }
        } else { //form is not empty
            if (action == "submit") {
                myform.command.value = "addparameter_search";
                myform.executeCount.value = "true";
                myform.nextcommand.value = "search_search";
            } else {
                paso = true;
                myform.command.value = "addparameter_search";
                myform.nextcommand.value = "getform_search";
                myform.executeCount.value = "true";
                myform.nextpage.value = nextpage;
                myform.state.value = state;
                myform.templatetype.value = templatetype;
                myform.submit();
            }
        }
    } else return true;
    return true;
}

function callInsObject(myform, nextpage, state, templatetype) {
    myform.action = "/WController/servlet/ServletPolicy?";

    <% if(lookForOper==null || lookForOper.equals("")){ %>
    myform.action = myform.action<%=lifeParam%>;
    <% } else { %>
    myform.action = myform.action<%=lifeParam%>+"lookForOper="+<%=lookForOper%>;
    <% } %>

    if (!paso) {
        if (isFormEmpty(myform)) {
            paso = true;
            myform.command.value = "getinsobj_search";
            myform.nextcommand.value = "";
            myform.nextpage.value = nextpage;
            myform.state.value = state;
            myform.templatetype.value = templatetype;
            myform.submit();
        } else { //form is not empty
            paso = true;
            myform.command.value = "addparameter_search";
            myform.nextcommand.value = "getinsobj_search";
            myform.nextpage.value = nextpage;
            myform.state.value = state;
            myform.templatetype.value = templatetype;
            myform.submit();
        }
        paso = true;
    } else return true;
}

var selectNames = new Array();
var selectValues = new Array();
function generateParameterArrays() {
<%
    java.util.Enumeration params = request.getParameterNames();
    int indexArray = 0;
    while (params.hasMoreElements()) {
        String key = (String) params.nextElement();
        String value = request.getParameter(key);
%>
    selectNames[<%=indexArray%>] = '<%=key%>';
    selectValues[<%=indexArray%>] = '<%=value%>';
<%
     indexArray++;
 }
%>
}

function isPresentParameter(myform) {
    for (var i = 0; i < myform.elements.length; i++) {
        elem = myform.elements[i];
        for (var j = 0; j < selectNames.length; j++) {
            if (elem.getAttribute("name") == selectNames[j]) {
                return true;
            }
        }

    }
    return false;
}

</script>

<style type="text/css">
    a {
        text-decoration: none;
    }

    a {
        cursor:auto;
    }
</style>
<%--<jsp:include page="/common/layout/header.jsp"/>--%>

</head>

<body onLoad="addStaticParameter(<%=FileWriter.formReference%>);
              updateTypeOfSearch(<%=FileWriter.formReference%>,<%=SearchRequestHandler.CONVENTIONAL_SEARCH%>)">
<script language="JavaScript">
    menu = "policy";
</script>

<script language="JavaScript" src="/WController/WorkflowEnabledApps/util/workflowheader.js"></script>


<logic:equal name="module" value="claim" scope="session">
    <% session.removeAttribute("urlCode"); %>
    <jsp:include page="/webmenus/includes/webmenuClaim.jsp">
        <jsp:param name="enable" value="true" />
    </jsp:include>
</logic:equal>

<%--<logic:notEqual name="module" value="claim" scope="session">--%>
    <%--<logic:notEqual name="module" value="reinsurance" scope="session">--%>
        <%--<logic:notEqual name="module" value="coinsurance" scope="session">--%>
            <%--<jsp:include page="/webmenus/includes/webmenuinclude.jsp">--%>
                <%--<jsp:param name="enable" value="true" />--%>
                <%--<jsp:param name="menubar1" value="menupolicy" />--%>
                <%--<jsp:param name="namemenubar1" value='<%=rb.getString("l_policy")%>' />--%>
                <%--<jsp:param name="menubar2" value="menureportpolicy" />--%>
                <%--<jsp:param name="namemenubar2" value='<%=rb.getString("l_reports")%>' />--%>
                <%--<jsp:param name="menubar3" value="menuhelp" />--%>
                <%--<jsp:param name="namemenubar3" value='<%=rb.getString("menuHelp")%>' />--%>
                <%--<jsp:param name="applicationname" value='<%=rb.getString("webmenus.applicationname")%>' />--%>
            <%--</jsp:include>--%>
        <%--</logic:notEqual>--%>
    <%--</logic:notEqual>--%>
<%--</logic:notEqual>--%>



<br><br>

<input type="hidden" id="entered" name="entered" value='<%=entered%>'/>
<input type="hidden" id="_idProduct" name="_idProduct" value='<%=productId%>'/>

<table  border=0 align="center" width="60%" border="0" cellpadding="0" cellspacing="0">

<tr>
    <td valign="bottom" width="450" align="left">
        <table width="100%" border="0" cellpadding="0" align="center" cellspacing="0">
            <tr>
                <td width="8" valign="top" height="17" align="right" class="TD_BGBLUE_L"><img
                        src="/WController/UAAWebClient/images/corner_on.gif" width="8" height="17"></td>
                <td class="TD_BGBLUE_L" height="17"><%= messages.getString("policy_label") %>
                </td>
                <%if(!"true".equals(request.getParameter("fromWF"))) {%>
                <td class="TD_BGBLUE_L" height="17" width="8"></td>
                <td height="17" class="TD_BGDBLUE_L" width="8" valign="top"><img
                        src="/WController/UAAWebClient/images/corner.gif" width="8" height="17"></td>
                <td height="17" class="TD_BGDBLUE_L">
                    <a onClick="return setCommand(document.<%=FileWriter.formReference%>,'hyperlink','searchformriskunit.jsp','2','RiskUnitType');" style="cursor: pointer">
                        <%= messages.getString("riskunit_label") %>
                    </a>
                </td>
                <td height="17" class="TD_BGDBLUE_L" width="8"></td>
                <td height="17" class="TD_BGDBLUE_L" valign="top"><img src="/WController/UAAWebClient/images/corner.gif" width="8" height="17"></td>
                <td height="17" class="TD_BGDBLUE_L">
                    <a onClick="return callInsObject(document.<%=FileWriter.formReference%>,'presearchforminsuranceobject.jsp','1','InsuranceObjectType');" style="cursor: pointer">
                        <%= messages.getString("insuranceobject_label") %>
                    </a>
                </td>

                <td height="17" class="TD_BGDBLUE_L" width="8"></td>
                <td height="17" class="TD_BGDBLUE_L" valign="top"><img src="/WController/UAAWebClient/images/corner.gif"
                                                                       width="8" height="17"></td>
                <td height="17" class="TD_BGDBLUE_L">
                    <a onClick="return setCommand(document.<%=FileWriter.formReference%>,'hyperlink','searchformthirdparty.jsp','2','ThirdPartyType');" style="cursor: pointer">
                        <%= messages.getString("thirdparty_label") %>
                    </a>
                </td><%}%>
                <td height="17" class="TD_BGDBLUE_L" width="8">&nbsp;</td>
            </tr>
        </table>
    </td>
    <td>&nbsp;</td>
</tr>
<tr>
    <td class="TD_BGBLUE_L" width="380">&nbsp;</td>
    <td class="TD_BGBLUE_L">&nbsp;</td>
</tr>
<tr valign="top">
<td colspan="2" align="center">
<table width="100%" border="0" cellspacing="0" background="../../images/bg.gif" class="itemBorder">
<tr>
    <td class="TD_BGBLUE_L" width="45%">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= messages.getString("search.label.orderField") %>:
    </td>
    <td bgColor="#f7f7f7" width="55%" align="left">
        <select name="_orderField" id="_orderField" onChange="addParameter(<%=FileWriter.formReference%>,this)">
            <option <%=orderField == 0 ? "selected" : ""%> value="0">----</option>
            <option <%=orderField == 1 ? "selected" : ""%>
                    value="1"><%= messages.getString("search.label.policyNumber") %>
            </option>
            <option <%=orderField == 2 ? "selected" : ""%>
                    value="2"><%= messages.getString("search.label.product") %>
            </option>
            <option <%=orderField == 3 ? "selected" : ""%>
                    value="3"><%= messages.getString("search.label.initialDate") %>
            </option>
            <option <%=orderField == 4 ? "selected" : ""%>
                    value="4"><%= messages.getString("search.label.finishDate") %>
            </option>
            <option <%=orderField == 5 ? "selected" : ""%>
                    value="5"><%= messages.getString("search.label.state") %>
            </option>
            <option <%=orderField == 6 ? "selected" : ""%>
                    value="6"><%= messages.getString("search.label.client") %>
            </option>
            <option <%=orderField == 7 ? "selected" : ""%>
                    value="7"><%= messages.getString("search.label.insured") %>
            </option>
            <option <%=orderField == 8 ? "selected" : "" %>
                    value="8"><%= frontEndMessages.getString("StateProductLabel") %>
            </option>
            <option <%=orderField == 9 ? "selected" : "" %>
                    value="9"><%= frontEndMessages.getString("CRMNumberLabel") %>
            </option>
        </select>
    </td>
</tr>
<tr>
    <td colspan="2"><br>
        <%
            int index = forma.indexOf("_");
            String category = forma.substring(0, index);
            String template = forma.substring(index + 1, forma.length());

            // Realizamos la carga de la forma
            if (forma.equals("error")) {
        %>
        <jsp:include page="/forms/jsp/error.jsp" />
        <%
        } else {
            String formaFull = "/EJBAcsel-e/jsp/generator.jsp";
        %>
        <table class="border" width="100%" cellspacing=0 cellpadding=0>
            <tr>
                <td class="TD_BGBLUE_L" width="45%">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= messages.getString("search.label.product") %>:
                </td>

                <td bgColor="#f7f7f7" width="55%" align="left">
                    <select name="_productId" id="_productId"
                            onChange="skm_LockScreen('Processing...');addParameter(<%=FileWriter.formReference%>,this); searchProductStates( <%=FileWriter.formReference%>, this.value );">
                        <option value="0">----</option>
                        <% for(Object keys : productList) {
                            Map.Entry<String,String> obj = (Map.Entry)keys;
                        %>
                        <option <%=productId == Integer.parseInt(obj.getKey()) ? "selected" : ""%>
                                value="<%=obj.getKey()%>"><%=obj.getValue()%>
                        </option>
                        <%
                            }
                        %>
                    </select>
                </td>

            </tr>
            <tr>
                <td class="TD_BGBLUE_L" width="45%">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= frontEndMessages.getString("StateProductLabel") %>:
                </td>

                    <td bgColor="#f7f7f7" width="55%" align="left">
                        <select name="_stateProductId" id="_stateProductId"
                                onChange="addParameter(<%=FileWriter.formReference%>,this); " onclick="">
                            <option value="0">----</option>
                            <% for(Object keys : stateProductList )
                            {
                                Map.Entry<String,String> obj = (Map.Entry)keys;
                            %>
                            <option <%=stateProductId == obj.getKey() ? "selected" : ""%>
                                    value="<%=obj.getKey()%>"><%=obj.getValue()%>
                            </option>
                            <%
                                }//end for
                            %>
                        </select>
                    </td>

            </tr>
            <tr>
                <td class="TD_BGBLUE_L" width="45%">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= messages.getString("search.label.client") %>:
                </td>
                <td bgColor="#f7f7f7" width="55%" align="left">
                    <input type="text" name="_clientName" id="_clientName" value="<%=clientName%>"
                           onBlur="addParameter(<%=FileWriter.formReference%>,this)">
                </td>
            </tr>
            <tr>
                <td class="TD_BGBLUE_L" width="45%">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= messages.getString("search.label.insured") %>:
                </td>
                <td bgColor="#f7f7f7" width="55%" align="left">
                    <input type="text" name="_insuredName" id="_insuredName" value="<%=insuredName%>"
                           onBlur="addParameter(<%=FileWriter.formReference%>,this)">
                </td>
            </tr>
            <tr>
                <td class="TD_BGBLUE_L" width="45%">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= messages.getString("search.label.policyId") %>:
                </td>
                <td bgColor="#f7f7f7" width="55%" align="left">
                    <input type="text" name="_policyId" id="_policyId" value="<%=policyId%>"
                           onBlur="addParameter(<%=FileWriter.formReference%>,this)">
                </td>
            </tr>
            <tr style="<%=!ClientInfo.isClientRunning("Interseguro")? "display: none;":""%>" >
                <td class="TD_BGBLUE_L" width="45%">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= frontEndMessages.getString("CRMNumberLabel") %>:
                </td>

                <td bgColor="#f7f7f7" width="55%">
                    <input type="text" name="_crmNumberCase" id="_crmNumberCase" value="<%=crmNumberCase%>"
                           onBlur="addParameter(<%=FileWriter.formReference%>,this)">
                </td>

            </tr>
            <tr>
                <td class="TD_BGBLUE_L" width="45%">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= messages.getString("search.label.fromDate") %>:
                </td>
                <td bgColor="#f7f7f7" width="55%" align="left">
                    <input type="text" name="_fromDateShow" id="_fromDateShow" value="<%=fromDateShow%>" onblur="validateDateNew(this,  document.getElementsByName('NoticeDate')[0], 'dd-MM-yyyy', new Array('dd-MM-yyyy'), false, 'null', 'null', false , 'null' , 'La Fecha es Requerida' , 'Formato de Fecha Invalido');addParameter(<%=FileWriter.formReference%>,this)">
                    <input type="hidden" name="_fromDate" value="">
                    <img id="calendarFromDate" src="/WController/images/calendar_icon.png" onclick="popUpCalendar(this,document.getElementsByName('_fromDateShow')[0],document.getElementsByName('NoticeDate')[0], 'dd-MM-yyyy', '-' )" style="cursor:pointer">
                </td>
            </tr>
            <tr>
                <td class="TD_BGBLUE_L" width="45%">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%= messages.getString("search.label.toDate") %>:
                </td>
                <td bgColor="#f7f7f7" width="55%" align="left">
                    <input type="text" name="_toDateShow" id="_toDateShow" value="<%=toDateShow%>" onblur="validateDateNew(this,  document.getElementsByName('NoticeDate')[0], 'dd-MM-yyyy', new Array('dd-MM-yyyy'), false, 'null', 'null', false , 'null' , 'La Fecha es Requerida' , 'Formato de Fecha Invalido');addParameter(<%=FileWriter.formReference%>,this)">
                    <input type="hidden" name="_toDate" value="">
                    <img id="calendarToDate" src="/WController/images/calendar_icon.png" onclick="popUpCalendar(this,document.getElementsByName('_toDateShow')[0],document.getElementsByName('NoticeDate')[0], 'dd-MM-yyyy', '-' )" style="cursor:pointer">
                </td>
            </tr>
            <tr>
                <td height="25" align=center colspan="2">
                </td>
            </tr>
            <tr valign=middle>
                <td align=center colspan="2">
                    <jsp:include page="<%=formaFull%>">
                        <jsp:param name="urlBack" value="/EJBAcsel-e/jsp/searchformpolicy.jsp" />
                        <jsp:param name="urlForward" value="" />
                        <jsp:param name="formName" value="<%=forma%>" />
                        <jsp:param name="nextCommand" value="search_search" />
                        <jsp:param name="tableName" value="PolicyType" />
                        <jsp:param name="nextPage" value="" />
                        <jsp:param name="state" value="" />
                        <jsp:param name="templateType" value="" />
                        <jsp:param name="PARAM_EDIT" value="true" />
                        <jsp:param name="PARAM_VISIBLE" value="false" />
                        <jsp:param name="categoryName" value="<%=category%>" />
                        <jsp:param name="templateName" value="<%=template%>" />
                        <jsp:param name="executeCount" value="true" />
                        <jsp:param name="isPendingChange" value='<%=(isPendingChange ? "yes" : "no")%>' />
                        <jsp:param name="formInitial" value="1"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
        <% } %>
    </td>
</tr>

<%--
        NOTA:   AMM 2009-06-01 : Esta opcion ha sido deshabilitada por recomendacion de Miguel Cerra. Se esperara a que
                se determine con el cliente cual va a ser el mecanismo de busqueda a utilizar. Por lo momentos, con esta
                opcion deshabilitada, se asume que la busqueda seguira siendo de la manera tradicional (sin tablas
                temporales)
--%>

<%--<tr>
    <td class="TD_GREY_L">
        <table>
            <tr>
                <td class="TD_GREY_L">&nbsp;</td>
                <td class="TD_GREY_L">
                    <font face="Microsoft Sans Serif" size="2">
                        <input type="radio"
                               name="<%=SearchRequestHandler.TYPE_OF_SEARCH%>"
                               onclick="updateTypeOfSearch(<%=FileWriter.formReference%>,
                                                       <%=SearchRequestHandler.CONVENTIONAL_SEARCH%>)"
                               value="<%=SearchRequestHandler.CONVENTIONAL_SEARCH%>" checked/>
                        <%= messages.getString("search.label.typeOfSearchNormal") %><br>
                        <input type="radio"
                               name="<%=SearchRequestHandler.TYPE_OF_SEARCH%>"
                               onclick="updateTypeOfSearch(<%=FileWriter.formReference%>,
                                                       <%=SearchRequestHandler.ENHANCED_SEARCH%>)"
                               value="<%=SearchRequestHandler.ENHANCED_SEARCH%>"/>
                        <%= messages.getString("search.label.typeOfSearchEnhanced") %><br>
                    </font>
                </td>
            </tr>
        </table>
    </td>
    <td class="TD_GREY_L">&nbsp;</td>
</tr>--%>
</table>
</td>
</tr>
<tr class="TR_BGBLUE_C">
    <td colspan="2">
        <input id="idb_040201401_searchformpolicy_01" type="button"
                onClick="setCommand(<%=FileWriter.formReference%>,'submit'); parseRequest(<%=FileWriter.formReference%>);<%request.getSession().setAttribute("operationTypeId", operationTypeId);%> "
               value='<%= messages.getString("search_button")%>' class="BUTTON01">
        &nbsp;<%
    %>
            <%-- <input type="reset" value='<%= messages.getString("clear_button")%>' class="BUTTON"> --%>

        <input id="idb_040201401_searchformpolicy_02" type="reset"
               onClick="clearParameters(new Array(_orderField,_productId,_stateProductId,_clientName,_insuredName, _policyId,_crmNumberCase,_fromDateShow,_toDateShow));document.forms.<%=FileWriter.formReference%>.reset();"
               value='<%= messages.getString("clear_button")%>' class="BUTTON01">
    </td>
</tr>
</table>
<div id="skm_LockPane" class="LockOff">
    <div id="skm_LockPane_msg"></div>
</div>

<%String confidentialityMessage=(String) session.getAttribute("confidentialityMessage");
    if (!confidentialityMessage.equals("")){%>
<jsp:include page="confidentialityMessage.jsp" > <jsp:param name="confidentialityMessage" value='<%=confidentialityMessage%>' /> </jsp:include>
<%}%>
</body>
</html:html>
<%--</opt:Compress>--%>

<%

%>
