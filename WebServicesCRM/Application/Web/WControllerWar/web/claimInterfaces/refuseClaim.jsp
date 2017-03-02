<%--
        Title: content.jsp
    Copyright: (c) 2002 - 2004 by Consis International
      Company: Consis International
       Author: Israel Santos (ISV)
       Author: Rossi Freytters (RF)
      Version: Acsel-e v2.2
Date creation: 2004-10-03

  Modified by:
      2013-08-26 (ISV)  Created
      2014-05-06 (SR) Added option reject motive.
      2014-05-08 (RF) Changes minor.
--%>


<%@ page import="com.consisint.acsele.util.forms.tools.FileWriter" %>
<%@ page import="com.consisint.acsele.workflow.claimapi.recovery.ClaimUtil,com.consisint.acsele.util.session.SessionConstants" %>

<script language="JavaScript1.2" src="/WController/forms/scripts/forms.js"></script>
<jsp:include page="/common/layout/header.jsp"/>
<jsp:useBean id="parametersform" scope="session" class="java.util.Hashtable" />
<%@ taglib uri="/bean" prefix="bean" %>
<%
    String forma = request.getParameter("form");

    if (forma != null) {
        parametersform.put("form", forma);
    } else {
        forma = (String) parametersform.get("form");
    }

    Locale currentlocale = (Locale) session.getAttribute(SessionConstants.LOCALE);
    ResourceBundle messages = ResourceBundle.getBundle("ClaimMessagesBundle", currentlocale);
    ResourceBundle messagesBundle = ResourceBundle.getBundle("ClaimMessagesBundle", currentlocale);
//    Hashtable dataFormHashtable =  (Hashtable)session.getAttribute("dataForm");
    Claim claimRefuse = ((Claim) request.getSession().getAttribute("Claim"));

    String sourceNode = request.getParameter("sourceNode");
    String classSource = request.getParameter("classSource");
    String aruId = request.getParameter("aruId");
    String ioID = request.getParameter("ioID");
    String ruID = request.getParameter("ruID");
    String closeWindow;
    String state =request.getParameter("state");
    String templateName = request.getAttribute("templateName") == null ?
            request.getParameter("templateName"): (String) request.getAttribute("templateName");
    String coverageId= request.getParameter("coverageId");
    String msj="";
    if (templateName.equals(AcseleConf.getProperty(ClaimComposerWrapper.TEMPLATE_CLAIM_DENIED))){
        if (StringUtil.isEmptyOrNullValue(coverageId)) {
            msj = messagesBundle.getString("claim.refuseConfirmation");
        }
    }else {
        if (templateName.equals(AcseleConf.getProperty(ClaimComposerWrapper.TEMPLATE_CLAIM_CLOSED))) {
            msj = messagesBundle.getString("claim.closedConfirmation");
        }
    }


    if (request.getAttribute("notCloseWindow") != null ||
            "false".equalsIgnoreCase(request.getParameter("cerrar"))) {
        closeWindow = "false";
    }
    StringBuffer params = new StringBuffer("sourceTree=");
    params.append(request.getParameter("sourceTree")).append("&").append("sourceNode").append("=")
            .append(sourceNode);
    params.append("&classSource=").append(classSource).append("&aruId=").append(aruId);
    params.append("&ioID=").append(ioID).append("&ruID=").append(ruID);

    // String  urlForward=(String)request.getAttribute("urlForward");
    ParameterFinder pf = new ParameterFinder(request, session);
    String urlForward = request.getAttribute("urlForward") == null ?
            request.getParameter("urlForward"): (String) request.getAttribute("urlForward");
    System.out.println("[LAD]  urlForward = " + urlForward);
    System.out.println("[contextPageClaimTool] URLFordward" + urlForward);


    if (StringUtil.isEmptyOrNullValue(urlForward)) {
        urlForward = new StringBuffer(
                "/claimInterfaces/updateAffectedObject.do?command=update&redirect=true&")
                .append(params.toString()).toString();
    }

    String urlGo;
    if(StringUtil.isEmptyOrNullValue(state)  && StringUtil.isEmptyOrNullValue(templateName))  {
        urlGo = "/claimInterfaces/contextPageClaimTool.jsp?redirect=true";
    } else {
        urlGo =  "/claimInterfaces/deniew.do?state="+request.getParameter("state")+"&templateName="+templateName;
    }
    String urlBack = "/claimInterfaces/index.jsp?redirect=true&";
    Integer urlGoCRM = 0;
    if(state==null){
        urlGoCRM = 1;
    }
    urlBack = RequestUtil.encode(urlBack, AcseleConf.getSystemCharset());
    urlGo = RequestUtil.encode(urlGo, AcseleConf.getSystemCharset());
    String seleccioneCRM = messages.getString("CRM.selection");

    boolean isLinkClaimDisable=false;
    String modifyClaim = AcseleConf.getProperty("allowModifyClaim");
    if (((String)request.getParameter("isLinkClaimDisable"))!=null){
        if (request.getParameter("isLinkClaimDisable").equals("true")&&modifyClaim.equals("0")){
            isLinkClaimDisable=true;
        }
    }

%>
<script LANGUAGE="JavaScript">
    var callbackCRM;
    var urlGoCRM = '<%=urlGoCRM%>';

    function refuseOK(plantilla) {
        var msj= '<%=msj%>';
        var choice;
        if  (msj!=""){
            choice = confirm(msj);
        }else{
            choice=true;
        }
        if (choice) {
            closeIt = 1;
            var url = '<%=urlForward%>';
            var sel = document.getElementsByName("productRejectMotive");
            var productRejectMotiveIndex = sel.length; //posicion
            if (sel[0] != null) {
                var motive = sel[0].value;
                url += "?productRejectMotive=" + motive;
            }
            if (plantilla.datos) {
                if (plantilla.datos.<%=FileWriter.formReference%>.
                redirect
            )
                {
                    plantilla.datos
                .<%=FileWriter.formReference%>.
                    redirect.value = "false";
                    plantilla.datos
                .<%=FileWriter.formReference%>.
                    urlForward.value = url;
                }
                parseRequest(plantilla.datos.<%=FileWriter.formReference%>);
            } else {
                if (plantilla.document.forms['<%=FileWriter.formReference %>'].redirect) {
                    window.plantilla.document.forms['<%=FileWriter.formReference %>'].value = "true";
                }
                window.plantilla.document.forms['<%=FileWriter.formReference %>'].urlForward.value = url;
                window.plantilla.document.forms['<%=FileWriter.formReference %>'].target = "_parent";
                parseRequest(window.plantilla.document.forms['<%=FileWriter.formReference %>']);
            }
        }
    }

    function actionUpdateCRM() {
        // llamara a validar seleccion del radio button
        callbackCRM = {
            success : function(o) {
                YAHOO.containerPanel.hide();
                refuseOK(window.frames['plantilla']);
            },
            failure : function(o) {
                YAHOO.containerPanel.hide();
                //document.getElementsByTagName("body")[0].innerHTML = "<center>Error!!</center>";
                window.alert("Error");
            }
        }
        if(urlGoCRM==1){
            showContainer("/WController/claimInterfaces/clientCrm.do?typeAssociation=RS&plantilla=window.frames['plantilla']", 'Asociar Caso CRM', 500, 300, true, true, true, true, false, 'inner');
        }else{
            showContainer("/WController/claimInterfaces/clientCrm.do?typeAssociation=RC&plantilla=window.frames['plantilla']", 'Asociar Caso CRM', 500, 300, true, true, true, true, false, 'inner');
        }

//        < % = messages.getString("CRMNumberLabel")%>
//        validateSelection('selectAddress','selectLetter', 'formGenLetter');
    }

    function ShowSelected()
    {
        /* Para obtener el texto */
        var combo = document.getElementById("listCRM");
        var selected = combo.options[combo.selectedIndex].text;
        if(selected!= '<%=seleccioneCRM%>') {
            document.getElementById("toAssign").disabled = false;
        }
        else
        {
            document.getElementById("toAssign").disabled=true;
        }
    }

    function closeWin(){
    }

    function showInspectionForClaim(inspectionid) {
        abrirVentana("/WController/inspections/showInspection.do?id=" + inspectionid + "&claim=true", 800, 600);
    }
</script>
<%
    System.out.println(" =========== /claimInterfaces/content.jsp ===========");
    String color="#003366";
    boolean anCov = false;
    session.setAttribute("org.apache.struts.action.ERROR",request.getAttribute("org.apache.struts.action.ERROR"));

    boolean hayDatos = false;
    boolean allow_display = AcseleConstants.SHOW_ALL;
    String cmd = request.getParameter("cmd");
%>

<head>
<%@ taglib uri="/bean"  prefix="bean"%>
<%@ taglib uri="/logic" prefix="logic"%>
<%@ taglib uri="/html" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts/acsele_forms.tld" prefix="acsele_forms" %>
    <%@ page import="java.util.*" %>
    <%@ page import="com.consisint.acsele.util.*" %>
    <%@ page import="com.consisint.acsele.util.StringUtil" %>
    <%@ page import="com.consisint.acsele.openapi.product.ClaimRejectionMotive" %>
    <%@ page import="com.consisint.acsele.workflow.claimapi.Claim" %>
    <%@ page import="com.consisint.acsele.CriteriaDCO" %>
    <%@ page import="com.consisint.acsele.RelationalOperator" %>
    <%@ page import="com.consisint.acsele.template.api.Template" %>
    <%@ page import="com.consisint.acsele.template.server.ConfigurableObjectType" %>
    <%@ page import="com.consisint.acsele.DefaultConfigurableObjectManager" %>
    <%@ page import="com.consisint.acsele.ClientInfo" %>
    <%@ page import="com.consisint.acsele.workflow.claimapi.ClaimComposerWrapper" %>
    <meta http-equiv="Content-Type" content="text/html; charset=<%=AcseleConf.getProperty("charsetPage")%>">
<%--
  TODO: mostrar errores
--%>
 <logic:present name="exceptionsMessages" scope="request">
   <%Collection messagesExc= (Collection)request.getAttribute("exceptionsMessages");%>
  <logic:iterate id="message" indexId="index" scope="request" collection="<%=messagesExc%>" type="java.lang.String">
    <tr/>
    <div class="breadcrumb" style="background-color: white">
        <table width="450" align="center" cellpadding="5" cellspacing="0">
      <tr>
       <td >
           <span class="validitycontent">
                 <b> <%=(Object) message%> </b>
           </span>
        </td>
       </tr>
       </table>
    </div>
   </logic:iterate>
</logic:present>
<%
    String formaFull = request.getContextPath() + "/EJBAcsel-e/jsp/generator.jsp";
    closeWindow = "false";
    int index = forma.indexOf("_");
    String category = forma.substring(0, index);
    String template = forma.substring(index + 1, forma.length());
%>
<div >
    <br><br>
    <table  width="100%" align="center">
        <tr  class="TR_BGBLUE_C" >
            <td width="100%" align="center" colspan="2">
                <font face="Microsoft Sans Serif" size="2">
                    <bean:message bundle="<%=ClaimUtil.getMessages()%>" key="rejectTitle"/>
                    &nbsp;
                </font>
            </td>
        </tr>
    </table>
    <br><br>
    <logic:present name="fromDeniedClaim" scope="request">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <bean:message bundle="<%=ClaimUtil.getMessages()%>" key="rejectReason"/>:
        <select name = "productRejectMotive">
            <option value="-1">----</option>
            <logic:iterate id="productRejectMotive" name="productRejectMotive" scope="request" type="com.consisint.acsele.openapi.product.ClaimRejectionMotive">
                <option value="<%= String.valueOf(productRejectMotive.getPk())%>"><%=(Object) productRejectMotive.getRejectionmotive() %>
                </option>
            </logic:iterate>
        </select>
    </logic:present>

<%
    if(isValidShowCRMDialog(claimRefuse) && !((String) templateName).equalsIgnoreCase("ReaperturaSiniestro") && ClientInfo.isClientRunning("Interseguro")){
%>
    <div align="center" >
        <input class="boton01"
               name="button2"
               type="button"
               onClick="actionUpdateCRM();"
               value="<acsele_forms:message key="button.confirm" />">
    </div>
    <%
    } else {
%>
    <div align="center" >
        <input class="boton01"
               name="button2"
               type="button"
               onClick="refuseOK(window.frames['plantilla']);"
               value="<acsele_forms:message key="button.confirm" />">
    </div>
    <%
    }
%>

<%!
    private boolean isValidShowCRMDialog(Claim claim){
        if(claim.getPolicy().getProduct().getName()!=null){
            if(!existInTDProduct(claim.getPolicy().getProduct().getName())) return false;
        }else{
            return false;
        }
        return true;
    }
    public boolean existInTDProduct(String productname){
        List<CriteriaDCO> criteria = new LinkedList<CriteriaDCO>(Arrays.asList(new CriteriaDCO[]{new CriteriaDCO("productos", RelationalOperator.EQ, productname, true)}));
        Template cot = ConfigurableObjectType.Impl.load("TDEXTPRODUCTCASOCRM");
        List<com.consisint.acsele.DefaultConfigurableObject> dcos = DefaultConfigurableObjectManager.loadAllByCOT(cot, criteria);
        if(dcos.size() == 1) return true;
        return false;
    }
%>
    <div class="breadcrumb2" style="background-color: white;">
        <iframe name="plantilla"
                id="plantilla"
                src="<%=formaFull+"?urlBack="+urlBack+"&urlForward="+urlForward+"&urlGo="+urlGo+"&formName="+forma+"&idobject=NONE&cerrar="+closeWindow+"&redirect=true&categoryName="+category+"&templateName="+template+"&flagClaim=true&panelHeight=340&select_url="+request.getContextPath()+"/claimInterfaces/contextPageClaimTool.jsp?redirect=true"%>"
                height="140%"
                width="98%"
                align="left"
                scrolling="auto"
                marginheight="40"
                marginwidth="20"
                frameborder="1"
                vspace="20"
                hspace="20">
        </iframe>
    </div>

    <div align="center" >
         <input class="boton01"
            name="button2"
            type="button"
             <%=isLinkClaimDisable?"disabled":""%>
            onClick="refuseOK(window.frames['plantilla']);"
            value="<acsele_forms:message key="button.confirm"
             />">
     </div>
