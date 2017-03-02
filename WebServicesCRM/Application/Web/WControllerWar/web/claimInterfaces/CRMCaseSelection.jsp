<%@ page import="java.util.ResourceBundle" %>
<%@ page import="com.consisint.acsele.UserInfo" %>
<%@ page import="java.util.List" %>
<%@ page import="com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase" %>
<%@ page import="com.consisint.acsele.util.AcseleConf" %>
<%@ page
        import="com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.enums.StatusAssociationTypeCRM" %>
<%
ResourceBundle messages = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
ResourceBundle rb = ResourceBundle.getBundle("ThirdPartyMessagesBundle", UserInfo.getLocale());
List<CRMCase> crmList      = (List<CRMCase>) request.getAttribute("crmList");
List<CRMCase> crmCaseAsociated = (List<CRMCase>) request.getAttribute("crmCaseAsociated");
String typeAssociation      = (String) request.getAttribute("typeAssociation");
String groupName1      = (String) request.getAttribute("groupName1");
String groupName2      = (String) request.getAttribute("groupName2");
String formName        = (String) request.getAttribute("formName");
String plantilla        = (String) request.getAttribute("plantilla");
String url = "/WController/claimInterfaces/clientCrmUpdate.do?typeAssociation="+typeAssociation+"&listCRM=listCRM";

%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=<%=AcseleConf.getProperty("charsetPage")%>">
        <title><%= messages.getString("CRM.title")%></title>
    </head>
    <body>
        <form name="formAssociateCRM" id="formAssociateCRM" action="" method="post">
            <div>
                <table>
                    <tr class="TR_BGBLUE_C">
                        <td >
                            <%= messages.getString("CRM.case")%>
                        </td>
                    </tr>
                    <tr align="center">
                        <td class="TD_GREY_C" width="50%" align="center">
                            <select id = "listCRM" name="listCRM" onchange="ShowSelected();" align="center">
                                <option value='-1'><%= messages.getString("CRM.selection")%></option>
                                <%--<option value="0"><%= messages.getString("CRM.selection")%></option>--%>
                    <%
                        for(CRMCase crm : crmList) out.println("<option value='"+crm.getCrmNumber()+"'>"+crm.getCrmNumber()+"</option>");
                    %>
                            </select>
                        </td>
                    </tr>
                    <tr align="center">
                        <%
                            if(((String) typeAssociation).equalsIgnoreCase("RR")){
                        %>
                        <td class="TD_GREY_C" width="50%">
                            <input id = "toAssign" type ="button" name="asign" class="BUTTON" style="width: 100px" disabled="disabled" value="<%= messages.getString("CRM.asign")%>"  onclick="getAjaxResponseTextAccordingForm('<%=url%>', callbackCRM, 'formAssociateCRM')"/>
                        </td>
                        <td class="TD_GREY_C" width="50%">
                            <input type ="button" id = "skyp" value="<%= messages.getString("CRM.skyp")%>"  class="BUTTON" style="width: 100px" onclick="validateSelection('<%=groupName1%>','<%=groupName2%>','<%=formName%>')"/>
                        </td>

                        <%
                        } else if(((String) typeAssociation).equalsIgnoreCase("RS")) {
                        %>
                        <td class="TD_GREY_C" width="50%">
                            <input id = "toAssign" type ="button" name="asign" class="BUTTON" style="width: 100px" disabled="disabled" value="<%= messages.getString("CRM.asign")%>"  onclick="getAjaxResponseTextAccordingForm('<%=url%>', callbackCRM, 'formAssociateCRM')"/>
                        </td>
                        <td class="TD_GREY_C" width="50%">
                            <input type ="button" id = "skyp" value="<%= messages.getString("CRM.skyp")%>"  class="BUTTON" style="width: 100px" onclick="refuseOK(<%=plantilla%>);"/>
                        </td>
                        <%
                        } else if(((String) typeAssociation).equalsIgnoreCase("AP")) {
                        %>
                        <td class="TD_GREY_C" width="50%">
                            <input id = "toAssign" type ="button" name="asign" class="BUTTON" style="width: 100px" disabled="disabled" value="<%= messages.getString("CRM.asign")%>"  onclick="getAjaxResponseTextAccordingForm('<%=url%>', callbackCRM, 'formAssociateCRM')"/>
                        </td>
                        <td class="TD_GREY_C" width="50%">
                            <input type ="button" id = "skyp" value="<%= messages.getString("CRM.skyp")%>"  class="BUTTON" style="width: 100px" onclick="applyPaymentUAA()"/>
                        </td>
                        <%
                        } else if(((String) typeAssociation).equalsIgnoreCase("RC")) {
                        %>
                        <td class="TD_GREY_C" width="50%">
                            <input id = "toAssign" type ="button" name="asign" class="BUTTON" style="width: 100px" disabled="disabled" value="<%= messages.getString("CRM.asign")%>"  onclick="getAjaxResponseTextAccordingForm('<%=url%>', callbackCRM, 'formAssociateCRM')"/>
                        </td>
                        <td class="TD_GREY_C" width="50%">
                            <input type ="button" id = "skyp" value="<%= messages.getString("CRM.skyp")%>"  class="BUTTON" style="width: 100px" onclick="pushOK();"/>
                        </td>
                        <%
                            }
                        %>
                    </tr>
                </table>
                <%
                    for(int i=0;i<crmCaseAsociated.size();i++){
                        if(i==crmCaseAsociated.size()-1){
                            if(!(crmCaseAsociated.get(i).getCrmNumber().isEmpty()) || crmCaseAsociated.get(i).getCrmNumber()!=null){
                %>
                <table>
                    <tr>
                        <td >

                        </td>
                    </tr>
                    <tr class="TR_BGBLACK">
                        <td >
                            <%= messages.getString("CRM.previuslyAsociated")%>
                        </td>
                        <td >
                            <%= crmCaseAsociated.get(i).getCrmNumber()%>
                        </td>
                        <td >
                            <%= messages.getString("CRM.asociationType")%>
                        </td>
                        <td >
                            <%= StatusAssociationTypeCRM.getIntanceByValue(crmCaseAsociated.get(i).getAssociationType()).getDescription()%>
                        </td>
                    </tr>
                </table>
                <%
                            }
                        }
                    }
                %>
            </div>
        </form>
    </body>
</html>