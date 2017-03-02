<%@ page import="com.consisint.acsele.CriteriaDCO" %>
<%@ page import="com.consisint.acsele.DefaultConfigurableObjectManager" %>
<%@ page import="com.consisint.acsele.RelationalOperator" %>
<%@ page import="com.consisint.acsele.document.DocumentEngine" %>
<%@ page import="com.consisint.acsele.template.api.Template" %>
<%@ page import="com.consisint.acsele.template.server.ConfigurableObjectType" %>

<%--
        Title: ThirdPartyData.jsp
    Copyright: (c) 2008 by Consis International
      Company: Consis International
       Author: Yimy Alvarez(YJA)
       Author: Saul Montoya (SSM)
       Author: Aidelis Calvo (ACC)

      Version: Acsel-e v8
Date creation: 2008-03-14
  Modified by:
     2008-03-14 (YJA) Creation of the page
     2008-06-03 (YJA) Minor Changes
     2008-08-26 (MCC) Added checkbox with name addressDefault
     2008-10-09 (JML) Eliminated uses the class AddressBookType
     2009-06-29 (SSM) Implementation of the change date format
     2011-07-29 (JCM)  Refactoring to handled/extend new DocumentEngine.(ACSELE-1012)
     2013-01-28 (SSR)  Minor changes.
     2014-07-18 (ACC) Changed the variable CcId to Long.
--%>
<html>
  <%   try{
  Locale locale = (Locale) session.getAttribute(SessionConstants.LOCALE);
  ResourceBundle rb = ResourceBundle.getBundle("ThirdPartyMessagesBundle", locale);
  ResourceBundle rb1 = ResourceBundle.getBundle("PolicyToolMessagesBundle", locale);
  ResourceBundle messages = ResourceBundle.getBundle("ClaimMessagesBundle", locale);
  String freeTextLabel = rb1.getString("letters.freetext");
  ParameterFinder finder = new ParameterFinder(request);
  String freeText = finder.getParameter("freeText");
  Claim claimLetter = ((Claim) request.getSession().getAttribute("Claim"));
  String seleccioneCRM = messages.getString("CRM.selection");
  %>

<head>
    <%@ page import="com.consisint.acsele.template.server.CotType" %>
    <%@ page import="com.consisint.acsele.thirdparty.persistent.ThirdParty" %>
    <%@ page import="com.consisint.acsele.uaa.api.Role" %>
    <%@ page import="com.consisint.acsele.util.AcseleConf" %>
    <%@ page import="com.consisint.acsele.util.DateUtil" %>
    <%@ page import="com.consisint.acsele.util.ParameterFinder" %>
    <%@ page import="com.consisint.acsele.util.session.SessionConstants" %>
    <%@ page import="com.consisint.acsele.workflow.claimapi.Claim" %>
    <%@ page import="java.util.*" %>
    <%@ page import="com.consisint.acsele.ClientInfo" %>
    <meta http-equiv="Content-Type" content="text/html; charset=<%=AcseleConf.getProperty("charsetPage")%>">

    <title><%=rb.getString("letter.generate.title")%></title>

    <script type="text/javascript">
        var callbackCRM;

        function isSelected(groupName, formName){
            var resEval= eval("document."+formName+"."+groupName);
            // sizeGroup = eval("document."+formName+"."+groupName).length;
            if(resEval != undefined || resEval != null){
                sizeGroup = resEval.length;
            }
            //  sizeGroup = eval("document."+formName+"."+groupName).length;
            i=0;
            isSelect =false;

            if (sizeGroup!=undefined){
                while (i<sizeGroup && !isSelect){

                    radio = eval("document."+formName+"."+groupName + "["+i+"]");
                    if (radio.checked){
                        isSelect=true;
                    }

                    i++;
                }
            }else{
                radio = eval("document."+formName+"."+groupName);
                if (radio.checked){
                    isSelect=true;
                }

            }

            return isSelect;
        }

        function actionUpdateCRM() {
            // llamara a validar seleccion del radio button
            callbackCRM = {
                success : function(o) {
                    YAHOO.containerPanel.hide();
                    validateSelection('selectAddress', 'selectLetter', 'formGenLetter');
                },
                failure : function(o) {
                    YAHOO.containerPanel.hide();
                    //document.getElementsByTagName("body")[0].innerHTML = "<center>Error!!</center>";
                    alert("Error");
                }
            }
            showContainer("/WController/claimInterfaces/clientCrm.do?typeAssociation=RR&groupName1=selectAddress&groupName2=selectLetter&formName=formGenLetter", 'Asociar Caso CRM', 500, 300, true, true, true, true, false, 'inner');

        }

        function  validateSelection(groupName1, groupName2, formName){

            if (isSelected(groupName1, formName)){

                if (isSelected(groupName2, formName)){
                    eval("document."+formName).submit();
                }else{
                    alert("<%=rb.getString("letter.error.isNotLetterSelect")%>");
                }

            }else{
                alert("<%=rb.getString("letter.error.isNotAddressSelect")%>");
            }

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
    </script>


</head>

     <link rel="stylesheet" href="/WController/webStyles/AcselStyle_Search.css" type="text/css">
    <link rel="stylesheet" href="/WController/webStyles/AcselStyle.css" type="text/css">

    <script type="text/javascript" src="/WController/webmenus/popcalendar.js"></script>
    <link rel="stylesheet" type="text/css" href="/WController/webmenus/menuStyle.css">
    <script type="text/javascript" src="/WController/forms/scripts/forms.js"></script>
<%

    Vector addressBookList = (Vector) request.getAttribute("thirdPartyAddressBookList");
    Vector addressBookTags = (Vector) request.getAttribute("thirdPartyAddressBookPropertyTags");
    ThirdParty thirdparty = (ThirdParty) request.getAttribute("thirdPartyData");
    String requisite = (String) request.getAttribute("requisite");
    Role role = (Role) request.getAttribute("role");
    Collection<DocumentEngine> documents = (Collection) request.getAttribute("letters");
    Long CcId = (Long)request.getAttribute("configuratedCovId");

%>
<body bgcolor="#FFFFFF" text="#000000" link="#C8C8C8"
      vlink="#C8C8C8" alink="#C8C8C8" background="/WController/webmenus/images/backpageNew.gif">
<p></p>
<jsp:include page="/yui/generalContainer.jsp" />
<form name="formGenLetter" action="/WController/thirdparty/thirdPartyFindAddressAndData.do" method="post">

<table width="90%"  cellpadding="0" cellspacing="5" >
<tr align="center"> <td colspan="2"></td></tr>
<tr align="center">
    <td ><strong><%=rb.getString("letter.generate.title")%></strong></td>
</tr>
</table>
    
<table width="70%"  cellpadding="0" cellspacing="5">
<tr width="10%" >
<td><Strong><%=rb.getString("letter.name")%></Strong></td>
<td><%=thirdparty.getName()%></td>
</tr><tr>
<td ><Strong><%=rb.getString("letter.role")%></Strong></td>
<td ><%out.print(role!=null?role.getDescription():"");%></td>
</tr>
<tr>
<td ><Strong> Enviar Mail?</Strong></td>
<td ><input type="checkbox" name="sendMail"></td>
</tr>

</table>

<p></p>
<div align="center"><strong><%=rb.getString("title.address")%></strong></div>



   <input type="hidden" name="commandSendLetter" value="commandSendLetter" />
   <input type="hidden" name="ThirdPartyID" value="<%=thirdparty.getPK()%>"/>
   <input type="hidden" name="requisite" value="<%=requisite%>"/>
   <input type="hidden" name="roleDesc" value="<%=role.getDescription()%>"/>
   <input type="hidden" name="roleId" value="<%=role.getId()%>"/>
   <input type="hidden" name="configuratedCovId" value="<%=CcId%>"/>


<table width="450" align="center" cellpadding="5" cellspacing="0" bordercolor="#EEEEEE" class="itemBorder">
<tr><td>

 <table width="100%" cellspacing="1" cellpadding="5">
    
<tr>

    <%
        Iterator iterTags = addressBookTags.iterator();%>
    <td bgcolor="#006699" > </td>
    <td bgcolor="#006699" ><strong><font color="#FFFFFF"> <%= rb.getString("tag.defectaddressBook")%></font></strong></td>

    <%   while (iterTags.hasNext()) {
            String tag = (String) iterTags.next();
    %>
       <td bgcolor="#006699" ><strong><font color="#FFFFFF"><%=tag%></font></strong></td>


<%}%>

</tr>

    <%
        Iterator IterAddress = addressBookList.iterator();
        boolean color = false;

        while (IterAddress.hasNext()) {
            HashMap element = (LinkedHashMap) IterAddress.next();
            Collection valuesElement = element.values();

            Long addressBookID = (Long) element.get("addressBookID");
            Integer addressDefault = (Integer)element.remove("addressBookDefault");

            Iterator iterValuesElement = valuesElement.iterator();

    %>
    <tr bgcolor="<%out.print(color?"#F7F7F7":"E0E0E0");%>">
        <%
            color = !color; 
            int count =0;
            %>
        <td><input type="radio" name="selectAddress" value="<%=addressBookID%>"  ></td>
        <td> <input type="checkbox" name="addressDefault" value="<%=addressDefault.equals(new Integer(1)) ? "checked=\"checked=\"" : ""%>" disabled="disabled"></td>

        <%
             while(iterValuesElement.hasNext()){

                 Object valueElement = iterValuesElement.next();
             if (count > 2){
                 %>

        <td><%

            if (valueElement != null){



            out.print(valueElement);
            }else{
                out.print(" -- ");
            }
            %></td>

            <%} count++;
            } %>

 </tr>

            <%}%>


</table>


</td></tr>

</table>

<p></p>

<div align="center"><strong><%=rb.getString("title.letter")%></strong></div>
<table width="600" align="center" cellpadding="5" cellspacing="0" bordercolor="#EEEEEE" class="itemBorder">
<tr><td>

 <table width="100%" cellspacing="1" cellpadding="5">
 <tr bgcolor="#006699">
 <td></td>     
 <td><strong><font color="#FFFFFF"><%=rb.getString("letter.name")%></font></strong></td>
 <td><strong><font color="#FFFFFF"><%=rb.getString("letter.initialDate")%></font></strong></td>
 <td><strong><font color="#FFFFFF"><%=rb.getString("letter.finalDate")%></font></strong></td>
 
 </tr>

 <p></p>

<%
   

    Iterator iterLetters = documents.iterator();
    color=true;

    while (iterLetters.hasNext()){
        DocumentEngine doc = (DocumentEngine) iterLetters.next();
%>
       <tr bgcolor="<%out.print(color?"#F7F7F7":"E0E0E0");%>">
        <td><input type="checkbox" name="selectLetter" value="<%=doc.getPk()%>"></td>
        <td><%=doc.getName()%></td>
        <td><%=DateUtil.getDateToShow(doc.getInitialDate())%></td>
        <td><%=DateUtil.getDateToShow(doc.getFinishDate())%></td>
    </tr>
  <%     color=!color;
    }
%>

</table>

</td></tr>
</table>
<br />

<div align="center">
            <br>  <font face="Arial, Helvetica, sans-serif" size="3" color="#000000"><%=freeTextLabel%>
                </font> <br>
<%
        if(isValidShowCRMDialog(claimLetter) && ClientInfo.isClientRunning("Interseguro")){
%>
        <textarea name="freeText" id="freeText" rows="4" cols="50"></textarea>
        <input id="idb_0402036_ThirdPartyData_01" type="button" name="generateLetter" value="<%=rb.getString("letter.generate")%>" onclick="actionUpdateCRM();">
    <%
        } else {
    %>
    <textarea name="freeText" id="freeText" rows="4" cols="50"></textarea>
    <input id="idb_0402036_ThirdPartyData_01" type="button" name="generateLetter" value="<%=rb.getString("letter.generate")%>" onclick="validateSelection('selectAddress', 'selectLetter', 'formGenLetter');">
    <%
        }
    %>
</div>

</form>

</body>
</html>


<%}catch(Exception e){

    System.out.println("ERROR EN JSP THRIDPARTYDATA"+e.toString() );
    e.printStackTrace();
}%>
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