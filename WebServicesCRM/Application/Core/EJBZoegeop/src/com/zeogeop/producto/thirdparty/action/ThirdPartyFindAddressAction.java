package com.consisint.acsele.thirdparty.action;

import com.consisint.acsele.*;
import com.consisint.acsele.claim.communicationhistory.model.CommunicationHistoryModel;
import com.consisint.acsele.claim.communicationhistory.service.CommunicationHistoryService;
import com.consisint.acsele.custserv.engine.ClientCommunicationRecord;
import com.consisint.acsele.custserv.engine.StateCommunicationEnum;
import com.consisint.acsele.document.DocumentEngine;
import com.consisint.acsele.document.letter.Letter;
import com.consisint.acsele.letters.strategy.LetterGenerator;
import com.consisint.acsele.letters.strategy.LetterStrategy;
import com.consisint.acsele.letters.util.Converter;
import com.consisint.acsele.management.maintainer.User;
import com.consisint.acsele.policy.server.AgregatedPolicy;
import com.consisint.acsele.policy.server.EvaluatedCoverage;
import com.consisint.acsele.product.server.AssociateDocumentsRole;
import com.consisint.acsele.product.server.ConfiguratedCoverage;
import com.consisint.acsele.product.server.HistoryDocumentGenerated;
import com.consisint.acsele.product.server.Product;
import com.consisint.acsele.reports.EMailSender;
import com.consisint.acsele.template.server.Categorias;
import com.consisint.acsele.template.server.ConfigurableObjectType;
import com.consisint.acsele.template.server.CotType;
import com.consisint.acsele.template.server.PropiedadImpl;
import com.consisint.acsele.thirdparty.ThirdPartyUtil;
import com.consisint.acsele.thirdparty.api.Address;
import com.consisint.acsele.thirdparty.api.AddressList;
import com.consisint.acsele.thirdparty.beans.impls.AddressListImpl;
import com.consisint.acsele.thirdparty.beans.impls.persister.AddressPersister;
import com.consisint.acsele.thirdparty.persistent.AddressBook;
import com.consisint.acsele.thirdparty.persistent.ThirdParty;
import com.consisint.acsele.uaa.api.Role;
import com.consisint.acsele.util.*;
import com.consisint.acsele.util.context.CRMInternalServices;
import com.consisint.acsele.util.error.ApplicationException;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.evaluator.EvaluationConstants;
import com.consisint.acsele.util.evaluator.ExpresionEvaluator;
import com.consisint.acsele.util.evaluator.SymbolAttributes;
import com.consisint.acsele.util.evaluator.TablaSimbolos;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.consisint.acsele.util.session.SessionConstants;
import com.consisint.acsele.workflow.claimInterfaces.forms.ClaimRequisitesForm;
import com.consisint.acsele.workflow.claimapi.*;
import com.consisint.acsele.workflow.webboxcontroller.NoteAdderBean;
import com.consisint.acsele.workflow.workflowcontroller.Note;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * Search address associated to the thirdParty.
 * Title: ThirdPartyFindAdressAction<br/>
 * Copyright: (c) 2008 Consis International<br/>
 * Company: Consis International<br/>
 * @author Consis International (CON)
 */

public class ThirdPartyFindAddressAction extends Action {

    private static final AcseleLogger log = AcseleLogger.getLogger(ThirdPartyAddressInsertAction.class);
    private static ThirdPartyUtil util = new ThirdPartyUtil();
    public static final String ROLE_DESCRIPTION = "roleDescription";
    public static final String REQUISITE = "requisite";
    public static final String ALL_REQUISITES = "allRequisites";
    public static final String RECEIVER_PREFIX = "RECEIVER";
    private CommunicationHistoryService serviceHistory;

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        ParameterFinder finder = new ParameterFinder(request);
        String freeText = finder.getParameter("freeText");
        String claimID = finder.getParameter("claimId");

        String command = finder.getParameter("commandSendLetter");
        String from = finder.getParameter("from");

        if (StringUtil.isEmptyOrNullValue(command)) {
            ClaimComposer composer = ClaimSessionUtil.getSession(request.getSession(), ThirdPartyFindAddressAction.class.getName());

            String thirdPartyId = request.getParameter("ThirdPartyID");
            String idRequisite = request.getParameter("IdRequisite");
            String rolId = request.getParameter("roleId");
            String coverageId = request.getParameter("coverageId");
            long configuratedCovId = 0;

            AgregatedPolicy policy = composer.getAgregatedPolicy();
            Product product = policy.getProduct();
            Collection claimNormalReserves = composer.getClaimNormalReserves();

            for (Object claimNormalReserve : claimNormalReserves) {
                ClaimNormalReserve reserve = (ClaimNormalReserve) claimNormalReserve;

                if (reserve.getPk().equals(coverageId)) {
                    EvaluatedCoverage ev = reserve.getEvaluatedCoverage();
                    configuratedCovId = ev.getConfiguratedCoverageOA().getId();
                    break;
                }
            }

            AssociateDocumentsRole associateDocumentsRole = new AssociateDocumentsRole();
            associateDocumentsRole.setCoverageID(configuratedCovId);
            associateDocumentsRole.setProduct(Long.parseLong(product.getPk()));
            associateDocumentsRole.setRoleId(Long.parseLong(rolId));

            Collection<DocumentEngine> documents = DocumentEngine.loadDocumentsByRolAndCov(associateDocumentsRole);
            ThirdParty thirdParty = ThirdParty.getInstance(Long.valueOf(thirdPartyId));
            Hashtable addressAndTags = util.searchAddressToThirdParty(Long.parseLong(thirdPartyId));
            Role role = Role.Impl.load(Long.valueOf(rolId));

            request.setAttribute("freeText", freeText);
            request.setAttribute("claimId", claimID);
            request.setAttribute("letters", documents);
            request.setAttribute("thirdPartyAddressBookList", addressAndTags.get("address"));
            request.setAttribute("thirdPartyAddressBookPropertyTags", addressAndTags.get("tags"));
            request.setAttribute("thirdPartyData", thirdParty);
            request.setAttribute("requisite", idRequisite);
            request.setAttribute("configuratedCovId", configuratedCovId);
            request.setAttribute("role", role);

            return mapping.findForward("success");

        } else {

            // llama al servicio para asociarlo
            CRMInternalServices crmServices = CRMInternalServices.Impl.getInstance();
            String crmNumber = "";
            if(crmServices != null) crmNumber = crmServices.processThirdParty(claimID);

            String selectAddress = request.getParameter("selectAddress");
            String[] selectLetter = request.getParameterValues("selectLetter");
            long thirdPartyID = Long.parseLong(request.getParameter("ThirdPartyID"));
            String requisite = request.getParameter("requisite");
            String roleDesc = request.getParameter("roleDesc");
            String sendMail = request.getParameter("sendMail");
            long configuratedCovId = Long.parseLong(request.getParameter("configuratedCovId"));
            long roleId = Long.parseLong(request.getParameter("roleId"));

            TablaSimbolos table = new TablaSimbolos();
            ThirdParty thirdParty = ThirdParty.getInstance(thirdPartyID);

            ClaimComposer composer = ClaimSessionUtil.getSession(request.getSession(), ThirdPartyFindAddressAction.class.getName());
            TablaSimbolos symbolsTable = composer.fillSymbolTableClaim(composer.getClaim().getPk(), table);
            ExpresionEvaluator evaluator = ExpresionEvaluator.createEvaluator();

            /*(HES): http://srvjira/browse/ACSELE-22584*/
            String urlFileNotification=null;
            String notificationComment=null;
            serviceHistory = (CommunicationHistoryService) BeanFactory.getBean(CommunicationHistoryService.class);

            AddressBook addressBook = AddressBook.getInstance(Long.parseLong(selectAddress));
            if (freeText != null) {
                SymbolAttributes symbolAttributes = new SymbolAttributes(freeText, NumberUtil.ZERO_DOUBLE, false);
                symbolsTable.put(EvaluationConstants.FREE_TEXT, symbolAttributes);
            }
            evaluator.setTablaSimbolos(symbolsTable);
            evaluator.addSymbol(EvaluationConstants.CRMCase, crmNumber, 0.0, true);
            evaluator.addSymbol(ROLE_DESCRIPTION, roleDesc, 0.0, true);

            try {
                com.consisint.acsele.product.api.ConfiguratedCoverage configuratedCoverage = com.consisint.acsele.product.api.ConfiguratedCoverage.Impl.load(configuratedCovId);
                evaluator.addSymbol(EvaluationConstants.CLAIM_NORMAL_RESERVE_DESC, configuratedCoverage.getDesc(), 0d, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            evaluator.addSymbols(thirdParty.getDynamic().getDCO(), thirdParty.getDynamic().getDCO().getCOT().getCotType().getLevel());
            evaluator.addSymbols(addressBook.getDynamic().getDCO(), addressBook.getDynamic().getDCO().getCOT().getCotType().getLevel());
            evaluator.addSymbols(addressBook.getDynamic().getDCO(), addressBook.getDynamic().getDCO().getCOT().getCotType().getLevel());

            String destinatary = thirdParty.getName().replaceAll(" ", "");
            List resultLetter= null;
            ArrayList lettersWithErrors = new ArrayList();
            ArrayList lettersWithoutErrors = new ArrayList();

            com.consisint.acsele.thirdparty.api.ThirdParty thirdPartyApi = com.consisint.acsele.thirdparty.api.ThirdParty.Impl.load(thirdPartyID);
            AddressList addressList = getAddressList(thirdPartyApi);
            String thirdPartyCompleteAddress = "";

            if (!StringUtil.isEmptyOrNullValue(selectAddress) && addressList != null && addressList.size() > 0) {
                for (Address address : addressList.getAll()) {
                    if (address != null && Long.parseLong(selectAddress) == address.getId()) {
                        thirdPartyCompleteAddress = address.getAddressLine();
                        break;
                    }
                }
            }

            evaluator.addSymbol(EvaluationConstants.THIRDPARTY_COMPLETE_ADDRESS, thirdPartyCompleteAddress.toUpperCase(), 0.0, false);
            evaluator.addSymbol(EvaluationConstants.THIRDPARTY_COT, thirdParty.getDynamic().getTemplate(), 0.0, false);
            evaluator.addSymbols(thirdParty.getDynamic().getDCO(), RECEIVER_PREFIX);

            try {
                String languageByCustomer = thirdParty.getConfiguratedLanguageByCustomer();
                String languageByDefault = thirdParty.getConfiguratedLanguageByDefault();

                if (StringUtil.isEmptyOrNullValue(languageByCustomer)) {
                    languageByCustomer = "";
                }

                if (StringUtil.isEmptyOrNullValue(languageByDefault)) {
                    languageByDefault = "";
                }

                destinatary = destinatary.replaceAll("/", "_");
                requisite = requisite.replaceAll("/", "_");
                String timeStamp = DateUtil.sdfl.format(Calendar.getInstance().getTime());

                Locale locale = (Locale) request.getSession().getAttribute(SessionConstants.LOCALE);
                ResourceBundle rb = ResourceBundle.getBundle("ThirdPartyMessagesBundle", locale);

                for (int i = 0; i < selectLetter.length; i++) {
                    Letter letterToSend = Letter.load(Long.valueOf(selectLetter[i]));
                    if (letterToSend.getStrategy().equals(DocumentEngine.advancedIteratorsStrategy)){
                        letterToSend.setStrategy(DocumentEngine.advancedStrategy);
                    }
                    LetterStrategy strategy = (LetterStrategy) BeanFactory.getBean(letterToSend.getStrategy());
                    LetterGenerator letterGenerator = LetterGenerator.getInstance(strategy);

                    Map<String,List<Properties>> notReceivedRequisitesMap = new HashMap<String, List<Properties>>();
                    List<Properties> propertiesToIterate = new ArrayList<Properties>();
                    if (StringUtil.isEmptyOrNullValue(requisite)) {
                        Vector requisites = composer.getRequisites();
                         for (Object claimRequisitesForm : requisites) {
                            ClaimRequisitesForm requisiteForm = (ClaimRequisitesForm) claimRequisitesForm;
                            if (!requisiteForm.isChecked()) {
                                TablaSimbolos tablaSimbolos = new TablaSimbolos();
                                tablaSimbolos.put(EvaluationConstants.UNRECEIVED_REQUIREMENT, requisiteForm.getLabel(), 0.0);
                                tablaSimbolos.addTableSimbol(evaluator.getTablaSimbolos());
                                propertiesToIterate.add(Converter.loadProperties(letterToSend, tablaSimbolos));
                            }
                        }
                        Collection<ClaimRequisite> claimRequisites =  composer.getClaim().getRequisiteCheckList();
                        for (Object claimRequisite : claimRequisites) {
                            ClaimRequisite requisiteClaim = (ClaimRequisite) claimRequisite;
                            if (!requisiteClaim.isChecked()) {
                                String symbol = requisiteClaim.getDescription();
                                PropiedadImpl property = PropiedadImpl.getInstanceTemplate(symbol);
                                TablaSimbolos tablaSimbolos = new TablaSimbolos();
                                if (property == null) {
                                    tablaSimbolos.put(EvaluationConstants.UNRECEIVED_REQUIREMENT, requisiteClaim.getDescription(), 0.0);
                                } else {
                                    tablaSimbolos.put(EvaluationConstants.UNRECEIVED_REQUIREMENT, property.getEtiqueta(), 0.0);
                                }
                                tablaSimbolos.addTableSimbol(evaluator.getTablaSimbolos());
                                propertiesToIterate.add(Converter.loadProperties(letterToSend, tablaSimbolos));
                            }
                        }
                        notReceivedRequisitesMap.put(EvaluationConstants.REQ_LEVEL, propertiesToIterate);

                        String requisiteEnv;
                        List<String> requisitesList = new ArrayList<String>();
                        for (Object claimRequisitesForm : composer.getRequisites()) {
                            ClaimRequisitesForm requisiteForm = (ClaimRequisitesForm) claimRequisitesForm;
                            if(requisiteForm.isMandatory() && !requisiteForm.isChecked()){
                                requisitesList.add(requisiteForm.getLabel());
                            }
                        }
                        requisiteEnv = StringUtil.unsplit(requisitesList," </w:t> <w:br/> <w:t> ");
                        evaluator.addSymbol(ALL_REQUISITES, requisiteEnv, 0D, true);
                    } else {
                        evaluator.addSymbol(REQUISITE, requisite, 0.0, true);
                    }

                    resultLetter = letterGenerator.generateAutomaticLetterClaim(evaluator.getTablaSimbolos(), claimID,
                            letterToSend, destinatary, LetterGenerator.FROM_CLAIM, requisite + i + "_" + timeStamp,
                            languageByCustomer, languageByDefault, LetterGenerator.FROM_CLAIM, null, notReceivedRequisitesMap);

                    if (resultLetter == null || resultLetter.size()==0) {
                        lettersWithErrors.add(letterToSend);
                    } else {
                        lettersWithoutErrors.add(letterToSend);

                        if (sendMail != null) {
                            String emailAddress = evaluator.getTablaSimbolos().getSymbol(AcseleConf.getProperty("ThirdEmail")).getInput();
                            urlFileNotification = ((File) resultLetter.get(0)).getAbsolutePath();

                            if (freeText == null || freeText.equals("")) {
                                notificationComment = rb.getString("letter.thirdpartyCorrespondence.subject");
                            } else
                                notificationComment = freeText;

                            ArrayList fileToSend = new ArrayList();
                            fileToSend.add((File) resultLetter.get(0));

                            EMailSender emailSender = new EMailSender();

                            boolean emailSent = emailSender.sendMailCommunication(new String[]{emailAddress},
                                    rb.getString("letter.thirdpartyCorrespondence.subject"), notificationComment, fileToSend, "text/plain");

                            /*(HES): http://srvjira/browse/ACSELE-22584*/
                            if (from != null && from.equals("claim")) {

                                ClientCommunicationRecord clienteCommunication = new ClientCommunicationRecord();
                                clienteCommunication.setClaimId(Long.valueOf(claimID));
                                clienteCommunication.setTrackingDate(new Date());
                                clienteCommunication.setUserId(Long.parseLong(UserInfo.getUserID()));
                                clienteCommunication.setItem(null);
                                clienteCommunication.setTptId(thirdParty.getStatic());

                                String customAuditRequirementLetter = AcseleConf.getProperty("customAuditRequirementLetter");
                                if (customAuditRequirementLetter != null && !customAuditRequirementLetter.equals(""))
                                    clienteCommunication.setItem(Long.valueOf(customAuditRequirementLetter));
                                else
                                    clienteCommunication.setItem(null);

                                clienteCommunication.setComment(notificationComment);
                                clienteCommunication.setIsMailContact(true);

                                if (emailSent) {
                                    clienteCommunication.setState(StateCommunicationEnum.STATE_COMMUNICATION_SENT.getValue());
                                } else {
                                    clienteCommunication.setState(StateCommunicationEnum.STATE_COMMUNICATION_PENDING.getValue());
                                }

                                clienteCommunication.setAttachedFile(urlFileNotification);
                                clienteCommunication.insertar();

                                CommunicationHistoryModel communicationHistory = new CommunicationHistoryModel();
                                communicationHistory.setChangeDate(new Date());
                                communicationHistory.setState(clienteCommunication.getState());
                                communicationHistory.setCommRecord(clienteCommunication);
                                serviceHistory.save(communicationHistory);
                            }
                        }

                        HistoryDocumentGenerated.saveHistoryGeneratedLetters(configuratedCovId, roleId, letterToSend.getPk(),
                                thirdPartyID, Long.parseLong(UserInfo.getUserID()), !StringUtil.isEmptyOrNullValue(sendMail), new Date());

                        generateComment(Integer.parseInt(UserInfo.getUserID()), configuratedCovId, composer.getClaim(), letterToSend);

                        // llama al servicio para asociarlo
                        if(crmServices != null) crmServices.processThirdPartyPublish(claimID,letterToSend);
                    }
                }
            } catch (Exception e) {

                /*(HES): http://srvjira/browse/ACSELE-22584*/
                if(from!=null && from.equals("claim") && sendMail!=null ){

                    ClientCommunicationRecord clienteCommunication= new ClientCommunicationRecord();
                    clienteCommunication.setClaimId(Long.valueOf(claimID));
                    clienteCommunication.setTrackingDate(new Date());
                    clienteCommunication.setUserId(Long.parseLong(UserInfo.getUserID()));
                    clienteCommunication.setItem(null);
                    clienteCommunication.setTptId(thirdParty.getStatic());

                    String customAuditRequirementLetter = AcseleConf.getProperty("customAuditRequirementLetter");
                    if(customAuditRequirementLetter!=null && !customAuditRequirementLetter.equals(""))
                        clienteCommunication.setItem(Long.valueOf(customAuditRequirementLetter));
                    else
                        clienteCommunication.setItem(null);

                    clienteCommunication.setComment(notificationComment);
                    clienteCommunication.setIsMailContact(true);
                    clienteCommunication.setState(StateCommunicationEnum.STATE_COMMUNICATION_PENDING.getValue());
                    clienteCommunication.setAttachedFile(urlFileNotification);
                    clienteCommunication.insertar();

                    CommunicationHistoryModel communicationHistory = new CommunicationHistoryModel();
                    communicationHistory.setChangeDate(new Date());
                    communicationHistory.setState(StateCommunicationEnum.STATE_COMMUNICATION_PENDING.getValue());
                    communicationHistory.setCommRecord(clienteCommunication);
                    serviceHistory.save(communicationHistory);
                }
                e.printStackTrace();
                log.error("Error generating letter", e);
            }

            request.setAttribute("lettersWithErrors", lettersWithErrors);
            request.setAttribute("lettersWithoutErrors", lettersWithoutErrors);
            return mapping.findForward("resultLetterGeneration");
        }
    }

    public AddressList getAddressList(com.consisint.acsele.thirdparty.api.ThirdParty thirdParty) throws ApplicationException, TechnicalException {
        AddressPersister loader = AddressPersister.Impl.getInstance();
        return new AddressListImpl(loader.getByThirdParty(thirdParty));
    }

    private void generateComment(int userId, long configuratedCovId, Claim claim, Letter letter) {
        try {
            ResourceBundle claimMessagesBundle = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
            User user = new User(userId);
            user.loadById();

            ConfiguratedCoverage confCov = new ConfiguratedCoverage(String.valueOf(configuratedCovId));
            confCov.load();
            NoteAdderBean addNoterBean = new NoteAdderBean();

            ConfigurableObjectType cot = Categorias.getFirstTemplate(CotType.COMMENT);
            DefaultConfigurableObject dco = DefaultConfigurableObject.create(cot);
            dco.save();

            StringBuilder mensaje = new StringBuilder();
            mensaje.append(claimMessagesBundle.getString("letter"))
                    .append(" ").append(letter.getName())
                    .append(" ").append(claimMessagesBundle.getString("letterGeneration.comment1"))
                    .append(": ").append(confCov.getDesc());

            addNoterBean.addNote(user.getLogin(), mensaje.toString(), claim.getPk(),
                    claim.getPolicyId(), Note.CL, String.valueOf(dco.getId()), cot.getPk(), null);
        } catch (Exception e) {
            log.error("Error in comment generation: " + e);
        }
    }
}
