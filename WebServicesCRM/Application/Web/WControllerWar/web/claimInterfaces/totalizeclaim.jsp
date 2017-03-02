<%--
        Title: totalizeclaim.jsp
    Copyright: (c) 2002 - 2008 by Consis International
      Company: Consis International
       Author: Nelson Crespo (NC)
       Author: Luisa Ledezma (LL)
               Manuel Chinea (MC)
               Dennys Aldana (DES)
               Daniel Franklin (DFR)
               Eliseth Perez (EPG)
               Israel Santos (ISV)
      Version: Acsel-e v5.2
Date creation:

  Modified by:
    2004-03-10 (JY) Add Jsp Header for comments
    2004-04-02 (GA)(JY) configure decimal precision to 2 digits
    2004-06-28 (NC) Added input hidden to support parameters needed for ClaimEntity
    2004-07-21 (LL) support for new status of payments
    2004-12-01 (MC) Support to detect the claim status and disable the appropiate buttons.
    2004-12-02 (NC) Changes to show the message of error when totalizing
    2004-12-03 (MC) Validation of claim status to enable/disable submit buttons
    2004-12-08 (GRV) change getLocale() for session.getAttribute(SessionConstants.LOCALE)
    2005-02-21 (NC) Changes to use with the new interface of claim
    2005-03-01 (MC) Changes to make the claim module multi-language
	2005-03-23 (NC) Change to totalize payments order with claim in statu closed or denied
	2005-06-16 (RAF) Changed so only pending movements are enabled for totalization
	2005-09-14 (DES) Now the cancelled payments are separated from the pending ones
	2006-01-11 (DES) The Cancelled payments are now shown along the paid and pending ones
	2006-01-17 (RDN) Now the payment orders of a pending payment are shown as approved
	2006-08-24 (RDN) Disabled the submit button when the claim is closed
	2006-12-12 (RL)  Set up values in numberFormatter to fix amount reserve format
	2007-03-09 (RL) Fixed the totalize process.
	2008-02-12 (SUN) add multilanguage tag
	2008-04-07 (DFR) Created various dynamic javascripts to fix the massive payment selection.
	            The uses of ClaimTotalize's Hashtables where changed to ClaimtotalizeOneByOne's Vectors.
	            The logics iterates used to iterate the payments in the ClaimTotalize where commented,
	            now we use ClaimTotalizeOneByOne and there are no association by ThirdParties and Currency
	            so we will just iterate payments.
	2008-04-23 (MCC) Added substatus policy validation
    2011-09-23 (EPG) Added reloadClaim function and changed the onClick function on back button from backInit to reloadClaim.
    2013-08-28 (ISV) Minor change at javascript seCurrency to show the new value of the amount

--%>
<%@ page import="com.consisint.acsele.UserInfo,com.consisint.acsele.util.PrecisionUtil,
                 com.consisint.acsele.util.session.SessionConstants,
                 com.consisint.acsele.workflow.claimInterfaces.forms.DataClaimForm,
                 com.consisint.acsele.workflow.claimapi.Claim,
                 com.consisint.acsele.claim.api.ClaimStatus,
                 com.consisint.acsele.workflow.claimapi.Payment,
                 com.consisint.acsele.claim.api.payment.PaymentStatus,
                 java.text.DecimalFormat" %>
<%@ page import="java.text.DecimalFormatSymbols"%>
<%@ page import="java.text.NumberFormat"%>
<%@ page import="com.consisint.acsele.util.StringUtil" %>
<%@ page import="com.consisint.acsele.claim.api.paymentorder.PaymentOrderStatus" %>
<%@ taglib uri="/logic" prefix="logic" %>
<%@ taglib uri="i18ntaglib"  prefix="i18n" %>

<% Locale currentlocale = (Locale) session.getAttribute(SessionConstants.LOCALE);
    ResourceBundle messages = ResourceBundle.getBundle("ClaimMessagesBundle", currentlocale);
    ResourceBundle messagesEx = ResourceBundle.getBundle("exceptions", currentlocale);


    try {
        boolean isLinkClaimDisable=false;
        String modifyClaim = AcseleConf.getProperty("allowModifyClaim");
        if (request.getParameter("isLinkClaimDisable")!=null){
            if (request.getParameter("isLinkClaimDisable").equals("true") && modifyClaim.equals("0")){
                isLinkClaimDisable=true;
            }
        }
%>

<html>

<head>
<title>
    <%=messages.getString("totalizeClaim.title") %>
</title>
<%@ page import="com.consisint.acsele.util.AcseleConf" %>
<%@ page import="java.util.*" %>
<%@ page import="com.consisint.acsele.openapi.currency.*" %>
<%@ page import="java.util.Currency" %>
    <%@ page import="com.consisint.acsele.claim.api.ReserveType" %>
    <%@ page import="com.consisint.acsele.policy.server.PolicySystem" %>
    <%@ page import="com.consisint.acsele.management.api.Rights" %>
    <%@ page import="com.consisint.acsele.management.api.GlobalUserWidgetPermission" %>
    <%@ page import="com.consisint.acsele.util.NumberUtil" %>
    <%@ page import="com.consisint.acsele.template.api.Template" %>
    <%@ page import="com.consisint.acsele.template.server.ConfigurableObjectType" %>
    <%@ page import="com.consisint.acsele.*" %>
    <meta http-equiv="Content-Type" content="text/html; charset=<%=AcseleConf.getProperty("charsetPage")%>">
<link rel="stylesheet" href="/WController/webStyles/AcselStyle.css" type="text/css">
<meta http-equiv="expires" content=0>
<script language=javascript src="/WController/forms/scripts/acsele.js"></script>
<script LANGUAGE="JavaScript">
    <!--
    function reloadClaim(forma) {
        forma.action = "/WController/claimInterfaces/buildStructClaim.do?isLinkClaimDisable=<%=isLinkClaimDisable%>";
        forma.goTo.value = "reload";
        forma.submit();
    }

    function backInit(forma){
        forma.action = "/WController/claimInterfaces/totalize.do";
//        forma.action = "/WController/claimInterfaces/buildStructClaim.do";
      //  forma.goTo.value = "reload";
        // ruID=372595&
        // ioID=IOATMScotia-127650%28PlanATMScotia%29
        forma.submit();
    }
    //-->
</script>
<logic:equal value="true" name="windowClose"  scope="request" >
<table width="97%" align="center" cellpadding="1">
<tr>
    <td  class="TD_BGBLACK_C" ><%= messages.getString("totalizeClaim.title") %></td>
</tr>
<logic:notPresent name="error" scope="request">
<form name="formBack" action="">
    <tr>
        <td height="40" class="TD_GREY_C" >
            <%= messages.getString("totalizeClaim.successfulFinal") %>
        </td>
    </tr>
    <%
        String validationMessage = (String) request.getAttribute("validationMessage");
        System.out.println("validationMessage"+validationMessage);
        if(!StringUtil.isEmptyOrNullValue(validationMessage) && "true".equals(validationMessage)){
    %>
        <tr>
            <td height="40" class="TD_GREY_C" >
               <b><%= (String) request.getAttribute("messageAnnualAggregateLimit") %></b>
            </td>
        </tr>

    <%}%>
        <tr>
        <td class="TD_BGDBLUE_C" >
            <!--<input type=hidden name="command" value="updateClaimPayment"/>-->
            <!--<input type=hidden name="goTo" value="success"/>-->
            <input type="hidden" name="ruID" value="<%=request.getParameter("ruID")%>"/>
            <input type="hidden" name="ioID" value="<%=request.getParameter("ioID")%>"/>
            <input id="idb_0402006_totalizeclaim_01" type="button"  value='<%=messages.getString("btn.reload")%>' name="back"  class="BUTTON" onClick="javascript:backInit(this.form);" style="width: 100px"/>
        </td>
    </tr>
</form>
</logic:notPresent>
<logic:present name="error" scope="request">
<tr>
    <td height="40" class="TD_GREY_C" >
        <%if (StringUtil.isEmpty(messagesEx.getString((String) request.getAttribute("error")))){  %>
           <%=request.getAttribute("error")%>
       <% }else{ %>
          <%=messagesEx.getString((String)request.getAttribute("error"))%>
      <%  }%>

        <%
            request.removeAttribute("error");
        %>
    </td>
</tr>
<form name="formBack" action="">
    <tr>
        <td class="TD_BGDBLUE_C" >
            <input type="hidden" name="ruID" value="<%=request.getParameter("ruID")%>"/>
            <input type="hidden" name="ioID" value="<%=request.getParameter("ioID")%>"/>
            <input id="idb_0402006_totalizeclaim_02" type="button"  value='<%=messages.getString("backbutton")%>' name="back"  class="BUTTON" onClick="javascript:backInit(this.form);" style="width: 100px">
            <input id="idb_0402006_totalizeclaim_03" type="button"  value='<%=messages.getString("btn.reload")%>' name="back"  class="BUTTON" onClick="javascript:backInit(this.form)" style="width: 100px">
        </td>
    </tr>
    <input type="hidden" name="goTo" value=""/>
</form>
</logic:present>
<table>
</logic:equal>

<logic:notEqual value="true" name="windowClose"  scope="request" >
    <%
			String disabled = "";
		%>
<logic:present name="claimTotalize" scope="request">
</logic:present>
    <jsp:useBean id="claimTotalize" class="com.consisint.acsele.workflow.claimtool.bean.ClaimTotalizeOneByOne" scope="request" />

<logic:equal value="<%=String.valueOf(ClaimStatus.CLOSED.getValue())%>" name="dataClaim" property="claimState" >
    <%
			disabled = "disabled";
		%>
</logic:equal>
<logic:equal value="<%=String.valueOf(ClaimStatus.DENIED.getValue())%>" name="dataClaim" property="claimState" >
    <%
			disabled = "disabled";
		%>
</logic:equal>
    <%
        //TODO: hay hashtables comentados, por favor no borrar...[DFR - AszCLPaymentReversal]
//        Hashtable paymentsNormalReserve = null;
        Vector paymentsNormalReserve = null;
   		if(claimTotalize!=null){
    		paymentsNormalReserve = claimTotalize.getNormalReservePendingPayments();
   		}

   		int paymentsNormalSize = 0;
	    if (paymentsNormalReserve!=null) {
	    	paymentsNormalSize = paymentsNormalReserve.size();
		}
       	System.out.println("[paymentsNormalSize] = " + paymentsNormalSize);
//   		Hashtable paymentsConceptReserve = claimTotalize.getConceptReservePendingPayments();
   		Vector paymentsConceptReserve = claimTotalize.getConceptReservePendingPayments();
   		int paymentsConceptSize = 0;
	    if (paymentsConceptReserve!=null) {
			paymentsConceptSize = paymentsConceptReserve.size();
		}
//		Hashtable cancelledPaymentsNormalReserve = claimTotalize.getNormalReserveCancelledPayments();
		Vector cancelledPaymentsNormalReserve = claimTotalize.getNormalReserveCancelledPayments();
   		if(cancelledPaymentsNormalReserve!=null){
        }
   		int cancelledPaymentsNormalSize = 0;
	    if (paymentsNormalReserve!=null) {
			paymentsNormalSize = paymentsNormalReserve.size();
		}
       	System.out.println("[paymentsNormalSize] = " + paymentsNormalSize);
//   		Hashtable cancelledPaymentsConceptReserve = claimTotalize.getConceptReserveCancelledPayments();
   		Vector cancelledPaymentsConceptReserve = claimTotalize.getConceptReserveCancelledPayments();
  		int cancelledPaymentsConceptSize = 0;
	    if (paymentsConceptReserve!=null) {
			paymentsConceptSize = paymentsConceptReserve.size();
		}
   		Map currenciesList = new HashMap();

   		if (claimTotalize.getCurrencyList() != null) {
   		    currenciesList = claimTotalize.getCurrencyList();
   		}

   		String[] colorsLeft= { "TD_GREY_L","TD_WHITE_L" };
		String[] colorsCenter= { "TD_GREY_C","TD_WHITE_C" };
		String[] colorsRight= { "TD_GREY_R","TD_WHITE_R" };
		int precision = PrecisionUtil.getModulePrecision(PrecisionUtil.CLAIM);
        int precisionType = PrecisionUtil.getModulePrecisionType(PrecisionUtil.CLAIM);
        DecimalFormat numberFormatter = (DecimalFormat) NumberFormat.getInstance(UserInfo.getLocale());

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(UserInfo.getLocale());
        decimalFormatSymbols.setDecimalSeparator(AcseleConf.getProperty("decimalSeparator").charAt(0));
        decimalFormatSymbols.setGroupingSeparator(AcseleConf.getProperty("thousandsSeparator").charAt(0));

        numberFormatter.setDecimalFormatSymbols(decimalFormatSymbols);
        numberFormatter.setDecimalSeparatorAlwaysShown(true);
        numberFormatter.setMinimumFractionDigits(precision);
        numberFormatter.setMaximumFractionDigits(precision);
        String decimalSeparator= String.valueOf(numberFormatter.getDecimalFormatSymbols().getDecimalSeparator());
        Claim claimLetter = ((Claim) request.getSession().getAttribute("Claim"));
        String seleccioneCRM = messages.getString("CRM.selection");
        String groupingSeparator = String.valueOf(numberFormatter.getDecimalFormatSymbols().getGroupingSeparator());
  %>

<script LANGUAGE="JavaScript">
<!--
var callbackCRM;
var caracter = /[A-Za-z0-9]{1,12}/

function round(value, exp) {
    if (typeof exp === 'undefined' || +exp === 0)
        return Math.round(value);

    value = +value;
    exp = +exp;

    if (isNaN(value) || !(typeof exp === 'number' && exp % 1 === 0))
        return NaN;

    // Shift
    value = value.toString().split('e');
    value = Math.round(+(value[0] + 'e' + (value[1] ? (+value[1] + exp) : exp)));

    // Shift back
    value = value.toString().split('e');
    return +(value[0] + 'e' + (value[1] ? (+value[1] - exp) : -exp));
}

function TieneCaracter(caracter, cadena) {
    return caracter.test(cadena)
}

function actionUpdateCRM() {
    // llamara a validar seleccion del radio button
    callbackCRM = {
        success : function(o) {
            YAHOO.containerPanel.hide();
            applyPaymentUAA();
        },
        failure : function(o) {
            YAHOO.containerPanel.hide();
            //document.getElementsByTagName("body")[0].innerHTML = "<center>Error!!</center>";
            alert("Error");
        }
    }
    showContainer("/WController/claimInterfaces/clientCrm.do?typeAssociation=AP", 'Asociar Caso CRM', 500, 300, true, true, true, true, false, 'inner');
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

function applyPaymentUAA() {
    if (!validateCheck()){
        alert('<%= messages.getString("totalizeClaim.select")%>');


        return false;
    } else {

    <%--            document.formTotalize.action="/WController/servlet/ServletClaim";--%>

        //alert(<%=request.getAttribute("appliedOpenItems")%>);

        if('<%=request.getAttribute("appliedOpenItems")%>' != null) {


            if(<%=request.getAttribute("appliedOpenItems")%>){

            var r = confirm('<%= messages.getString("generateClaimPayment.deducedOpenItemChange") %>' + '<%=request.getAttribute("totalDiscount")%>');
            if (r == true) {

                document.formTotalize.action = "/WController/claimInterfaces/applyPayments.do";
                document.formTotalize.submit();
                return true;


            } else {

               // alert(document.getElementById("idb_0402006_structure_09"));

                document.getElementById("idb_0402006_structure_09").disabled = false;

                document.getElementById("idb_0402006_structure_09").click();


        }

            }else{

                document.formTotalize.action = "/WController/claimInterfaces/applyPayments.do";
                document.formTotalize.submit();
                return true;

            }



            }else{

            document.formTotalize.action = "/WController/claimInterfaces/applyPayments.do";
            document.formTotalize.submit();
            return true;

        }


    }
}

function validateCheck() {
    for (var i=1;i<formTotalize.length;i++) {
        if (formTotalize.elements[i].checked){
            return true;
        }
    }
    return false;
}


function hasNotChecked() {

    for (var i=1;i<formTotalize.length;i++) {
        if (!formTotalize.elements[i].checked){
            return true;
        }
    }
    return false;
}


function checkAll() {
    if (hasNotChecked()){
        for (var i=1;i<formTotalize.length;i++) {
            formTotalize.elements[i].checked = true;
        }
    } else {
        for (var i=1;i<formTotalize.length;i++) {
            formTotalize.elements[i].checked = false;
        }
    }
}



<%
        out.print("function hasNotCheckedNormal() {\n");
                        for(int i=0; i<paymentsNormalSize; i++) {
                            Payment p=(Payment)paymentsNormalReserve.get(i);
                            if(p.getAmount().doubleValue()>0.0){
        out.print("             var thirdPartyNameNormal = eval(formTotalize.thirdPartyNameNormal" + p.getPK() +");\n");
        out.print("             if(!thirdPartyNameNormal.checked) {\n");
        out.print("                 return true\n");
        out.print("             }\n");
                            }
                        }
        out.print("     return false;\n");
        out.print("}\n");
     %>

<%
        out.print("function hasNotCheckedConcept() {\n");
                        for(int i=0; i<paymentsConceptSize; i++) {
                            Payment p=(Payment)paymentsConceptReserve.get(i);
                            if(p.getAmount().doubleValue()>0.0){
        out.print("             var thirdPartyNameConcept = eval(formTotalize.thirdPartyNameConcept" + p.getPK() +");\n");
        out.print("             if(!thirdPartyNameConcept.checked) {\n");
        out.print("                 return true\n");
        out.print("             }\n");
                            }
                        }
        out.print("     return false;\n");
        out.print("}\n");
     %>

     <%
        out.print("function checkAllNormal() {\n");
        out.print("         if (hasNotCheckedNormal()){\n");
        out.print("             setValueForNormalPayment(true);\n");
        out.print("         }else{\n");
        out.print("             setValueForNormalPayment(false);\n");
        out.print("         }\n");
        out.print("}\n");
     %>
     <%
        out.print("function checkAllConcept() {\n");
        out.print("         if (hasNotCheckedConcept()){\n");
        out.print("             setValueForConceptPayment(true);\n");
        out.print("         }else{\n");
        out.print("             setValueForConceptPayment(false);\n");
        out.print("         }");
        out.print("}\n");
     %>

     <%
        out.print("function setValueForNormalPayment(checkValue) {\n");
                        for(int i=0; i<paymentsNormalSize; i++) {
                            Payment p=(Payment)paymentsNormalReserve.get(i);
                            if(p.getAmount().doubleValue()>0.0){
        out.print("             var thirdPartyNameNormal = eval(formTotalize.thirdPartyNameNormal" + p.getPK() +");\n");
        out.print("             thirdPartyNameNormal.checked = checkValue\n\n");
                            }
                        }
        out.print("}\n");
     %>

    <%
        out.print("function setValueForConceptPayment(checkValue) {\n");
                        for(int i=0; i<paymentsConceptSize; i++) {
                            Payment p=(Payment)paymentsConceptReserve.get(i);
                            if(p.getAmount().doubleValue()>0.0){
        out.print("             var thirdPartyNameConcept = eval(formTotalize.thirdPartyNameConcept" + p.getPK() +");\n");
        out.print("             thirdPartyNameConcept.checked = checkValue\n\n");
                            }
                        }
        out.print("}\n");
     %>


function setField(index, sufix) {
    var thirdPartyName = eval("formTotalize.thirdPartyName" + sufix + index);
    var partialPayment = eval("formTotalize.partialPayment" + sufix  + index);
    var pendingAmount = eval("formTotalize.pendingAmount" + sufix  + index);

    if (thirdPartyName.checked)
        partialPayment.value = pendingAmount.value;
    else
        partialPayment.value  = '';
}



function validNum(string) {
    if (!string  ||  string.length == 0) return false;
    var Chars = "0123456789.";

    for (var i = 0; i < string.length; i++) {
        if (Chars.indexOf(string.charAt(i)) == -1)
            return false;
    }
    return true;
}


function validParseFloat(desiredNumber) {
    var result = parseFloat(desiredNumber);

    if (isNaN(result)){
        return 0;
    } else {
        return result;
    }
}

function formatAmount(value) {
    var decimalSeparator_ = '.';
    var groupingSeparator_ = '<%=groupingSeparator%>';
    var decimalSep = '<%=decimalSeparator%>';
    var ocur = 0;
    var strInt;
    var valueTransformed = '';
    var valueParse = String(value);
    var index_decimal = valueParse.lastIndexOf(decimalSeparator_);

    if (index_decimal != -1) {
        strInt = valueParse.substring(0, valueParse.lastIndexOf(decimalSeparator_));
        strDecimal = valueParse.substring(valueParse.lastIndexOf(decimalSeparator_) + 1, valueParse.length);
    } else {
        strInt = valueParse;
        var decimalPrecision = validParseFloat(<%= precision %>);
        strDecimal = "";
        for (var i = 0 ; i < decimalPrecision ; i++){
            strDecimal += "0";
        }
    }
    for (var i = strInt.length - 1; i >= 0; i--) {
        if (ocur == 3) {
            valueTransformed = groupingSeparator_ + valueTransformed;
            valueTransformed = strInt.charAt(i) + valueTransformed;
            ocur = 0;
        } else {
            valueTransformed = strInt.charAt(i) + valueTransformed;
        }
        ocur++;
    }

    valueTransformed = valueTransformed + decimalSep + strDecimal;
    return valueTransformed;
}

function setCurrency(currency, row){

    <%
          int l = 0;
          Hashtable map = new Hashtable();
    %>
    var currencies = new Array;
    var currenciesRates = new Array;
    <%
    Set currenciesIds = null;
    if (currenciesList != null){
        currenciesIds = currenciesList.keySet();
        Iterator itKeys = currenciesIds.iterator();
       while (itKeys.hasNext()){
          String currencyId = (String)itKeys.next();
          map.put(currencyId, new Integer(l));
    %>
    currencies[<%=l%>] =  <%=currencyId%>;
    currenciesRates[<%=l%>]  =  new Array;
    <%      l++;
       }

        currenciesIds = currenciesList.keySet();
        itKeys = currenciesIds.iterator();
       while (itKeys.hasNext()){
          String currencyId = (String)itKeys.next();
          Map rates = claimTotalize.getCurrenciesRates().get(currencyId);
          if (rates == null)
           continue;

          Set keysRates = rates.keySet();
          Iterator itKeysRates = keysRates.iterator();
          Integer indx = (Integer)map.get(currencyId);

          while (itKeysRates.hasNext()){
            String currencyIdTmp = (String)itKeysRates.next();
            Integer indexTmp = (Integer)map.get(currencyIdTmp);
            String rateTmp = (String)rates.get(currencyIdTmp);
    %>
    currenciesRates[<%=indx%>][<%=indexTmp%>] = <%=rateTmp%>;
    <%       }
       }
       }
    %>
    var currencyFromId = eval('document.formTotalize.reserveCurrency'+row+'.defaultValue');
    var currencyToId = currency.value;
    var currencyFromIdIndex, currencyToIdIndex;

    var rate = 1.5;
    for (i = 0; i < currencies.length ; i++){
        if (currencies[i] == currencyFromId)
            currencyFromIdIndex = i;
        if (currencies[i] == currencyToId)
            currencyToIdIndex = i;
    }

    rate = currenciesRates[currencyFromIdIndex][currencyToIdIndex];

                    var amount = validParseFloat(eval('document.formTotalize.amount'+row+'.value'));
                    (eval('document.formTotalize.rate'+row)).value = formatAmount(round(rate, <%=Integer.valueOf(AcseleConf.getProperty("fourPrecisionDecimal"))%>).toFixed(<%=Integer.valueOf(AcseleConf.getProperty("fourPrecisionDecimal"))%>));
                    (eval('document.formTotalize.paidAmount1'+row)).value = formatAmount(round((rate*amount), <%=precision%>).toFixed(<%=precision%>));
                    (eval('document.formTotalize.paidAmount'+row)).value = toEnglishFormat((rate*amount),<%=precision%>,decimalSeparator,<%=precisionType%>);
                    //alert((eval('document.formTotalize.paidAmount1'+row)).value);
                }
                //-->
            </script>


</head>


<body background="/WController/webmenus/images/backpageNew.gif" >

<center>
<form  name="formTotalize" method="post">
<input type=hidden name="command" value="applyClaimPayment">
<input type=hidden name="changeState" value="<%="disabled".equalsIgnoreCase(disabled)?"false":"true"%>">
<input type="hidden" name="ruID" value="<%=request.getParameter("ruID")%>"/>
<input type="hidden" name="ioID" value="<%=request.getParameter("ioID")%>"/>
<input type=hidden name="url" value="<%=request.getRequestURL().toString()%>"/>
<table width="95%" align="center" cellpadding="1">
<tr>
    <td colspan="7" class="TD_BGBLACK_C" >
        <%= messages.getString("totalizeClaim.title") %>
    </td>
</tr>

<logic:notEqual name="claimTotalize" property="normalReservePaymentsSize" value='<%="0"%>' >
    <tr>
        <td colspan="7" class="TD_BGDBLUE_C" >
            <%= messages.getString("appletClaimTool.coverageReserve") %>
        </td>
    </tr>

    <tr class="TR_BGBLUE_C">
        <td width="5%"><img src="/WController/webmenus/images/check.gif" onClick="checkAllNormal();" title="<%= messages.getString("selectRiskUnitToClaim.massiveCheck")%>" ></td>
        <td width="30%"><%= messages.getString("totalizeClaim.beneficiary") %></td>
        <td width="10%"><%= messages.getString("appletClaimTool.state") %></td>
        <td width="25%"><%= messages.getString("totalizeClaim.totalAmount") %></td>
        <td width="15%"><%= messages.getString("totalizeClaim.currencyToPay") %></td>
        <td width="5%"><%=  messages.getString("totalizeClaim.todayExchangeRate")%></td>
        <td width="10%"><%= messages.getString("totalizeClaim.amountExchage") %></td>
    </tr>
</logic:notEqual>

<logic:notEqual name="claimTotalize" property="normalReservePaidPaymentsSize" value='<%="0"%>' >

    <logic:iterate id="beneficiary" collection="<%=claimTotalize.getNormalReservePaidPayments()%>" indexId="index"  type="com.consisint.acsele.workflow.claimapi.Payment" scope="request" >
        <%if(beneficiary.getAmount().doubleValue() != 0.0){
        double rate = beneficiary.getExchangeRate();
        double ammount = beneficiary.getPaidAmount();
            String total = numberFormatter.format(rate*ammount);
        %>
        <tr>
            <td class="<%=colorsCenter[index.intValue()%2]%>"><input type="checkbox" checked disabled></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><%=beneficiary.getThirdParty().getName()%></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><%=beneficiary.getPaymentStatus().toString()%></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text"  value='<%=numberFormatter.format(beneficiary.getAmount())%>'  readonly   size="15"> &nbsp <%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(beneficiary.getReserveCurrencyId())).getDescription()%> </td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(beneficiary.getPaidCurrencyId())).getDescription()%></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text" name="rate<%=((Payment)beneficiary).getPK()%>" value='<%=beneficiary.getExchangeRate()%>'  readonly   size="5"></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text" name="paidAmount1<%=((Payment)beneficiary).getPK()%>" value='<%=total%>'  readonly   size="15"></td>
        </tr>
        <%}%>

    </logic:iterate>

</logic:notEqual>


<logic:notEqual name="claimTotalize" property="normalReserveCancelledPaymentsSize" value='<%="0"%>' >

    <%
        //TODO:  no borrar los iterate comentados por favor... [DFR - AszCLPaymentReversal]
    %>

    <%--<logic:iterate id="beneficiaryCancelledPaymentsByCurrency" collection="<%=cancelledPaymentsNormalReserve.elements() %>" indexId="i"  type="java.util.Hashtable" >--%>

    <%--<logic:iterate id="CancelledPaymentsByCurrency" collection="<%=beneficiaryCancelledPaymentsByCurrency.elements()%>" indexId="w"  type="java.util.Collection" >--%>

    <logic:iterate id="payment" collection="<%=cancelledPaymentsNormalReserve.iterator()%>" indexId="j"  type="com.consisint.acsele.workflow.claimapi.Payment" >

        <%
            if(payment.getAmount().doubleValue() != 0.0){
                String pk = payment.getPK();
                System.out.println("pk = " + pk);
        %>
        <tr>
            <td class="<%=colorsCenter[j.intValue()%2]%>">
                <input type="checkbox"
                       name="thirdPartyNameNormalCancelled<%=payment.getPK()%>"
                       value="<%=pk%>"
                        <%=(payment.getPaymentStatus() != PaymentStatus.PENDING_STATE) ? "disabled" : ""%> <%=disabled%>>
            </td>
            <td class="<%=colorsLeft[j.intValue()%2]%>"><%=payment.getThirdParty().getName()%></td>
            <td class="<%=colorsLeft[j.intValue()%2]%>"><%=payment.getPaymentStatus().toString()%></td>
            <input type=hidden  name="amount<%=payment.getPK()%>" value='<%=payment.getAmount()%> ' >
            <td class="<%=colorsLeft[j.intValue()%2]%>">
                <input type="text"  name="amount1<%=payment.getPK()%>"
                       value='<%=NumberUtil.formatDouble(payment.getAmount(), precision)%>'
                       defaultValue='<%= payment.getReserveCurrencyId() %> '
                       readonly
                       size="15"> &nbsp <%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(payment.getReserveCurrencyId())).getDescription()%> </td>
            <input type=hidden  name="reserveCurrency<%=payment.getPK()%>" value='<%=payment.getReserveCurrencyId()%> ' >
            <td class="<%=colorsLeft[j.intValue()%2]%>">
                <select name="currency<%=pk%>" onChange ="setCurrency(this,'<%=pk%>')">
                    <logic:iterate id="currency" collection="<%=claimTotalize.getCurrencyList().values()%>" type="com.consisint.acsele.openapi.currency.Currency" scope="request">
                        <option value="<%=currency.getId() %>" <%= payment.getReserveCurrencyId().equals(String.valueOf(currency.getId()))  ? "selected" : "" %> >  <%= currency.getDescription() %></option>
                    </logic:iterate>
                </select>
            </td>
            <td class="<%=colorsLeft[j.intValue()%2]%>"><input type="text"  name="rate<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(Double.parseDouble("1"), Integer.valueOf(AcseleConf.getProperty("fourPrecisionDecimal")))%>'  readonly   size="8"></td>
            <input type=hidden  name="paidAmount<%=payment.getPK()%>" value='<%=payment.getAmount()%> ' >

            <td class="<%=colorsLeft[j.intValue()%2]%>"><input type="text"  name="paidAmount1<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(payment.getAmount(), precision)%>'  readonly   size="15"></td>
        </tr>
        <%}%>

        <%--</logic:iterate>--%>

        <%--</logic:iterate>--%>

    </logic:iterate>

</logic:notEqual>


<logic:notEqual name="claimTotalize" property="normalReservePendingPaymentsSize" value='<%="0"%>' >
    <%
        //TODO:  no borrar los iterate comentados por favor... [DFR - AszCLPaymentReversal]
    %>


    <%--<logic:iterate id="beneficiaryPaymentsByCurrency" collection="<%=paymentsNormalReserve.elements() %>" indexId="i"  type="java.util.Hashtable" >--%>

        <%--<logic:iterate id="PaymentsByCurrency" collection="<%=beneficiaryPaymentsByCurrency.elements()%>" indexId="w"  type="java.util.Collection" >--%>

            <logic:iterate id="payment" collection="<%=paymentsNormalReserve.iterator()%>" indexId="j"  type="com.consisint.acsele.workflow.claimapi.Payment" >

                <%if((payment.getAmount().doubleValue() != 0.0) && (payment.getPaymentOrder().getState() == PaymentOrderStatus.APPROVED_STATE.getValue())){%>
                <tr>
                    <td class="<%=colorsCenter[j.intValue()%2]%>">
                        <input type="checkbox" name="thirdPartyNameNormal<%=payment.getPK()%>" value="<%=payment.getPK()%>"<%=(payment.getPaymentStatus() != PaymentStatus.PENDING_STATE) ? "disabled": ""%> <%=disabled%>> </td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><%=payment.getThirdParty().getName()%></td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><%=PaymentOrderStatus.APPROVED_STATE.toString()%></td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><input type="text"  name="amount1<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(payment.getAmount(), precision)%>' defaultValue='<%= payment.getReserveCurrencyId() %> ' readonly   size="15"> &nbsp <%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(payment.getReserveCurrencyId())).getDescription()%> </td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>">
                        <select name="currency<%=payment.getPK()%>" onChange ="setCurrency(this,'<%=payment.getPK()%>')">
                            <logic:iterate id="currency" collection="<%=claimTotalize.getCurrencyList().values()%>" type="com.consisint.acsele.openapi.currency.Currency" scope="request">
                                <option value="<%=currency.getId() %>" <%= payment.getReserveCurrencyId().equals(String.valueOf(currency.getId()))  ? "selected" : "" %> >  <%= currency.getDescription() %></option>
                            </logic:iterate>
                        </select>
                    </td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><input type="text"  name="rate<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(Double.parseDouble("1"), Integer.valueOf(AcseleConf.getProperty("fourPrecisionDecimal")))%>'  readonly   size="8"></td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><input type="text"  name="paidAmount1<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(payment.getAmount(), precision)%>'  readonly   size="15"></td>
                    <input type=hidden  name="paidAmount<%=payment.getPK()%>" value='<%=payment.getAmount()%> ' >
                    <input type=hidden  name="reserveCurrency<%=payment.getPK()%>" value='<%=payment.getReserveCurrencyId()%> ' >
                    <input type=hidden  name="amount<%=payment.getPK()%>" value='<%=payment.getAmount()%> ' >
                </tr>
                <%}%>

            </logic:iterate>

        <%--</logic:iterate>--%>

    <%--</logic:iterate>--%>

</logic:notEqual>

<logic:notEqual name="claimTotalize" property="conceptReservePaymentsSize" value='<%="0"%>' >


    <tr>
        <td colspan="7" class="TD_BGDBLUE_C" >
            <%= messages.getString("appletClaimTool.reserveConcept") %>
        </td>
    </tr>


    <tr class="TR_BGBLUE_C">
        <td width="5%"><img src="/WController/webmenus/images/check.gif" onClick=";" title="<%= messages.getString("selectRiskUnitToClaim.massiveCheck")%>" ></td>
        <td width="30%"><%= messages.getString("totalizeClaim.beneficiary") %></td>
        <td width="10%"><%= messages.getString("appletClaimTool.state") %></td>
        <td width="25%"><%= messages.getString("totalizeClaim.totalAmount") %></td>
        <td width="15%"><%= messages.getString("totalizeClaim.currencyToPay") %></td>
        <td width="5%"><%=  messages.getString("totalizeClaim.todayExchangeRate")%></td>
        <td width="10%"><%= messages.getString("totalizeClaim.amountExchage") %></td>
    </tr>

</logic:notEqual>

<logic:notEqual name="claimTotalize" property="conceptReservePaidPaymentsSize" value='<%="0"%>' >

    <logic:iterate id="payment" collection="<%=claimTotalize.getConceptReservePaidPayments()%>" indexId="index"  type="com.consisint.acsele.workflow.claimapi.Payment" scope="request" >

        <%if(payment.getAmount().doubleValue() != 0.0){
            double rate = payment.getExchangeRate();
            double ammount = payment.getPaidAmount();
            String total = numberFormatter.format(rate*ammount);
        %>
        <tr>
            <td class="<%=colorsCenter[index.intValue()%2]%>"><input type="checkbox" checked disabled></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><%=payment.getThirdParty().getName()%></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><%=payment.getPaymentStatus().toString()%></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text"  name="amount1<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(payment.getAmount(), precision)%>' defaultValue='<%=payment.getReserveCurrencyId()%> ' readonly   size="15"> &nbsp <%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(payment.getReserveCurrencyId())).getDescription()%> </td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(payment.getPaidCurrencyId())).getDescription()%></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text"  value='<%=NumberUtil.formatDouble(payment.getExchangeRate(), Integer.valueOf(AcseleConf.getProperty("fourPrecisionDecimal")))%>'  readonly   size="5"></td>
            <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text"  value='<%=total%>'  readonly   size="15"></td>
        </tr>
        <%}%>

    </logic:iterate>

</logic:notEqual>


<logic:notEqual name="claimTotalize" property="conceptReserveCancelledPaymentsSize" value='<%="0"%>' >

    <%
        //TODO:  no borrar los iterate comentados por favor... [DFR - AszCLPaymentReversal]
    %>

    <%--<logic:iterate id="beneficiaryCancelledPaymentsByCurrency" collection="<%=cancelledPaymentsConceptReserve.elements() %>" indexId="i"  type="java.util.Hashtable" >--%>

        <%--<logic:iterate id="CancelledPaymentsByCurrency" collection="<%=beneficiaryCancelledPaymentsByCurrency.elements()%>" indexId="w"  type="java.util.Collection" >--%>

            <logic:iterate id="beneficiary" collection="<%=cancelledPaymentsConceptReserve.iterator()%>" indexId="index"  type="com.consisint.acsele.workflow.claimapi.Payment" scope="request" >

                <%if(beneficiary.getAmount().doubleValue() != 0.0){
                    double rate = beneficiary.getExchangeRate();
                    double ammount = beneficiary.getAmount();
                    String total = numberFormatter.format(rate*ammount);
                %>
                <tr>
                    <td class="<%=colorsCenter[index.intValue()%2]%>"><input type="checkbox" checked disabled></td>
                    <td class="<%=colorsLeft[index.intValue()%2]%>"><%=beneficiary.getThirdParty().getName()%></td>
                    <td class="<%=colorsLeft[index.intValue()%2]%>"><%=beneficiary.getPaymentStatus().toString()%></td>
                    <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text"  value='<%=numberFormatter.format(beneficiary.getAmount())%>'  readonly   size="15"> &nbsp <%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(beneficiary.getReserveCurrencyId())).getDescription()%> </td>
                    <td class="<%=colorsLeft[index.intValue()%2]%>"><%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(beneficiary.getPaidCurrencyId())).getDescription()%></td>
                    <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text"  value='<%=beneficiary.getExchangeRate()%>'  readonly   size="5"></td>
                    <td class="<%=colorsLeft[index.intValue()%2]%>"><input type="text"  value='<%=total%>'  readonly   size="15"></td>
                </tr>
                <%}%>

            <%--</logic:iterate>--%>

        <%--</logic:iterate>--%>

    </logic:iterate>

</logic:notEqual>


<logic:notEqual name="claimTotalize" property="conceptReservePendingPaymentsSize" value='<%="0"%>' >

    <%
        //TODO:  no borrar los iterate comentados por favor... [DFR - AszCLPaymentReversal]
    %>
    <%--<logic:iterate id="beneficiaryPaymentsByCurrency" collection="<%=paymentsConceptReserve.elements() %>" indexId="i"  type="java.util.Hashtable" >--%>

        <%--<logic:iterate id="PaymentsByCurrency" collection="<%=beneficiaryPaymentsByCurrency.elements()%>" indexId="w"  type="java.util.Collection" >--%>

            <logic:iterate id="payment" collection="<%=paymentsConceptReserve.iterator()%>" indexId="j"  type="com.consisint.acsele.workflow.claimapi.Payment" >

                <%if((payment.getAmount().doubleValue() != 0.0) && (payment.getPaymentOrder().getState() == PaymentOrderStatus.APPROVED_STATE.getValue())) {%>
                <tr>
                    <td class="<%=colorsCenter[j.intValue()%2]%>"><input type="checkbox" name="thirdPartyNameConcept<%=payment.getPK()%>" value="<%=payment.getPK()%>" <%=(payment.getPaymentStatus() != PaymentStatus.PENDING_STATE) ? "disabled" : ""%>></td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><%=payment.getThirdParty().getName()%></td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><%=PaymentOrderStatus.APPROVED_STATE.toString()%></td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><input type="text"  name="amount1<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(payment.getAmount(), precision)%>' defaultValue='<%=payment.getReserveCurrencyId()%> ' readonly   size="15"> &nbsp <%=((com.consisint.acsele.openapi.currency.Currency)currenciesList.get(payment.getReserveCurrencyId())).getDescription()%> </td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>">
                        <select name="currency<%=payment.getPK()%>" onChange ="setCurrency(this,'<%=payment.getPK()%>')">
                            <logic:iterate id="currency" collection="<%=claimTotalize.getCurrencyList().values()%>" type="com.consisint.acsele.openapi.currency.Currency" scope="request">
                                <option value="<%=currency.getId() %>" <%= payment.getReserveCurrencyId().equals(String.valueOf(currency.getId()))  ? "selected" : "" %> >  <%= currency.getDescription() %></option>
                            </logic:iterate>
                        </select>
                    </td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><input type="text"  name="rate<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(Double.parseDouble("1"), Integer.valueOf(AcseleConf.getProperty("fourPrecisionDecimal")))%>'  readonly   size="8"></td>
                    <td class="<%=colorsLeft[j.intValue()%2]%>"><input type="text"  name="paidAmount1<%=payment.getPK()%>" value='<%=NumberUtil.formatDouble(payment.getAmount(), precision)%>'  readonly   size="15"></td>
                    <input type=hidden  name="paidAmount<%=payment.getPK()%>" value='<%=payment.getAmount()%> ' >
                    <input type=hidden  name="reserveCurrency<%=payment.getPK()%>" value='<%=payment.getReserveCurrencyId()%> ' >
                    <input type=hidden  name="amount<%=payment.getPK()%>" value='<%=payment.getAmount()%> ' >
                </tr>
                <%}%>
            <%--</logic:iterate>--%>

        <%--</logic:iterate>--%>

    </logic:iterate>

</logic:notEqual>
<%

    String disabledSubmit = "";


    /*http://srvjira/browse/ACSELE-28204*/
    if(!UserInfo.canDo(Rights.CAN_PAYMENT_PAY)){
        disabledSubmit="disabled";
    }

    DataClaimForm dataClaim = (DataClaimForm) session.getAttribute("dataClaim");
    if (dataClaim.getClaimState() == ClaimStatus.CLOSED.getValue() || dataClaim.getClaimState() == ClaimStatus.DENIED.getValue()) {
        //if the status is closed but has outstanding payments, the button must be enabled
        disabledSubmit= !claimTotalize.getConceptReservePendingPayments().isEmpty() && UserInfo.canDo(Rights.CAN_PAYMENT_PAY)? "" : "disabled";
    }

%>
    <%--		<form name="prueba" action="">--%>
<tr>
    <td  colspan="7" class="TD_BGDBLUE_C">
        <%
            if (paymentsConceptReserve!=null && isLinkClaimDisable!=true) {
        %>
        <%
        if(isValidShowCRMDialog(claimLetter) && ClientInfo.isClientRunning("Interseguro")){
        %>
        <input id="idb_0402006_totalizeclaim_04" type="button"  value='<%=messages.getString("policiesclaim.submit")%>'
            <%=(paymentsConceptReserve.isEmpty() && paymentsNormalReserve.isEmpty()) ?
                         "disabled" : disabledSubmit %>  class="BUTTON"
               onclick="actionUpdateCRM()"
               style="width: 100px">
        <%
        } else {
        %>
        <input id="idb_0402006_totalizeclaim_04" type="button"  value='<%=messages.getString("policiesclaim.submit")%>'
                <%=paymentsConceptReserve.isEmpty() && paymentsNormalReserve.isEmpty() ?
                         "disabled" : disabledSubmit %>   class="BUTTON"
               onclick="applyPaymentUAA()"
               style="width: 100px">
        <%
            }
        %>

        <%
        } else {
        %>


        <input id="idb_0402006_totalizedclaim_05" type="button"  value='<%=messages.getString("policiesclaim.submit")%>'
               disabled  class="BUTTON" onclick="applyPaymentUAA()" style="width: 100px" >
        <% }%>
        <input type="button"  value='<%=messages.getString("btn.reload")%>' name="back"  class="BUTTON" onClick="javascript:reloadClaim(this.form);" style="width: 100px">
    </td>
</tr>
</table>
<input type="hidden" name="goTo" value=""/>
</form>
</center>
</body>
</logic:notEqual>
</html>
<%
    }
    catch(Exception e){
        System.out.println("There was an error generating the page totalizeclaim.jsp: "+e);
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