package com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.workers;

import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMCase;
import com.consisint.acsele.interseguro.interfaces.crm.entity.CRMOpenItem;
import com.consisint.acsele.interseguro.interfaces.crm.persister.LogCRMCasePersister;
import com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.EventNotificationWorker;
import com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.enums.StatusAssociationTypeCRM;
import com.consisint.acsele.interseguro.interfaces.intermedia.event.Notification.services.enums.StatusCRM;
import com.consisint.acsele.util.AcseleConf;
import com.consisint.acsele.util.HttpUtil;
import com.consisint.acsele.util.StringUtil;
import com.consisint.acsele.util.error.TechnicalException;
import com.consisint.acsele.util.logging.AcseleLogger;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * La clase <code>EventNotificationWorkerCRMClaim</code> es el encargado de enviar la notificacion de actualizacion al Sistema CRM
 */
public class EventNotificationWorkerCRMClaim extends EventNotificationWorker {
    private static final AcseleLogger log = AcseleLogger.getCurrentClassLogger();
    static String requestURL;
    private final StringBuilder errorOut;
    EventNotificacionCRMClaim parameter;
    public static String messageResponse;

    public EventNotificationWorkerCRMClaim(StringBuilder error) {
        super();
        this.errorOut = error;
    }

    public static String getMessageResponse() {
        return messageResponse;
    }

    public static void setMessageResponse(String messageResponse) {
        EventNotificationWorkerCRMClaim.messageResponse = messageResponse;
    }

    @Override
    public void run() {
        final Gson gson = new Gson();
        String jsonOut = gson.toJson(parameter);
        String jsonIn = null;
        StatusCRM statusCRM = StatusCRM.SENT_ERROR;
        EventNotificacionCRMClaimResponse responseJSON = null;
        try {
            responseJSON = EventNotificationCRMCallWeb.processEventNotification(jsonOut);
            isReceived = responseJSON.isReceived;
            if(isReceived){ statusCRM = StatusCRM.SENT_OK; msg = statusCRM.name(); }  else{ statusCRM = StatusCRM.SENT_ERROR; msg = statusCRM.name(); }
        } catch (Exception e) {
            log.error(e);
            isReceived = false;
            msg = ""+e.getMessage();
            responseJSON = new EventNotificacionCRMClaimResponse(false, msg);
        } finally {
            jsonIn = responseJSON != null ? gson.toJson(responseJSON, EventNotificacionCRMClaimResponse.class) : "{\"msg\":\"Esto es un error no Capturado verificar a EventNotificationWorkerCRMClaim.class \"}";
            setMessageResponse(msg); // Cuando se implemente al modulo de Notificaciones
            registerLogInner(parameter.numerocaso, new Date(System.currentTimeMillis()), statusCRM.getDescription(), jsonIn, jsonOut);
            if(!isReceived) errorOut.append(msg);
        }
    }

    private void registerLogInner(String nroCaso, Date date, String movementStatus, String jsonIn, String jsonOut) {
        try {
            LogCRMCasePersister.Impl.createNew(nroCaso, date, movementStatus, jsonIn, jsonOut);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // super.registerLog(); Esto cuando se implemente con el modulo de Notificaciones y la tabla anterior no sera necesaria
        //if(log.isDebugEnabled()) log.debug("[EventNotificationWorkerCRMClaim] Ejecutado Notificacion "+notificacionEvent.toString()+", "+this.toString());    // Esto cuando se implemente con el modulo de Notificaciones y la tabla anterior no sera necesaria y se tenga una notificacion configurada
    }

    @Override
    public void setObjectToSendOther(Object objectInfo) {
        if(objectInfo instanceof CRMCase){
            createDataToSend((CRMCase) objectInfo);
        }
    }
/*
//    @Override
    public void setObjectToSendPol(CasoCRMData objectToSendCRM) {
        try {
//            super.setObjectToSendPol(objectToSendPol);
            createDataToSend(objectToSendCRM);
            if(log.isDebugEnabled()) log.debug("[EventNotificationWorkerCRMClaim] Se enviara a Ejecutar la Notificacion "+notificacionEvent.toString()+", "+this.toString());
            getRequestURL();
        } catch (Exception e) {
            msg = "No se pudo enviar la Notificacion hacia el CRM evento: " + this.notificacionEvent.getEventName();
            super.registerLog();
            log.error(e);
        }
    }
*/

    private void createDataToSend(CRMCase objectToSendCRM) {
        final String numerocaso = objectToSendCRM.getCrmNumber();
        final Long siniestro = objectToSendCRM.getClaimId();
        final Long ccvId = objectToSendCRM.getCcvId();
        final String ccvName = StringUtil.defaultIfEmpty(objectToSendCRM.getCcvName(), StringUtil.EMPTY_STRING);
        final Long idCarta = objectToSendCRM.getHltId();
        final String numerocarta = StringUtil.defaultIfEmpty(objectToSendCRM.getLetterName(), StringUtil.EMPTY_STRING);
        Integer associationType = objectToSendCRM.getAssociationType();
        if(associationType == null) throw new TechnicalException("Objeto CRMCase es invalido debe poseer un tipo de Asociacion!!!!!!!!!!!!");
        String tipoAsociacion = StatusAssociationTypeCRM.getIntanceByValue(associationType).getDescription();
        final Set<CRMOpenItem> crmOpenItems = objectToSendCRM.getCrmOpenItems();
        List<Long> ioIds = new ArrayList<Long>();
        if(crmOpenItems !=null && !crmOpenItems.isEmpty()){
            for (CRMOpenItem crmCaseMovement : crmOpenItems) ioIds.add(crmCaseMovement.getPk().getOpmId());
        }
        String fechacarta = "";
        String fechacartaaprob = "";
        String numeromemo = "";
        String montomemo = "";
        parameter = new EventNotificacionCRMClaim(numerocaso, ioIds, siniestro, ccvName, tipoAsociacion, numerocarta, fechacarta, fechacartaaprob, numeromemo, montomemo);
        this.mutsBeRun = true;
    }


    public static String getRequestURL(){
        if(requestURL == null){ requestURL = AcseleConf.getPropertyOrDefault("url.notificaCRM","http://130.30.11.62:8089/wsMasterCRM/ServiceMasterCRM.svc/rest/ActualizarCasoSiniestroCRM"); }
        return requestURL;
    }

    @Override
    public String toString() {
        StringBuilder retorno = new StringBuilder("EventNotificationWorkerCRMClaim{" + "class: "+this.getClass().getSimpleName()+ ", isReceived: " + isReceived + (StringUtil.isEmpty(msg)?"":", msg: '" + msg + '\''));
        if(parameter != null) retorno.append(", parametro: ").append(parameter.toString());
        return retorno.append("}").toString();
    }

/*********************************************************************************************************************/
    static class EventNotificacionCRMClaim {
        @SerializedName("numerocaso")
        public String numerocaso;           //Es el número de caso del CRM enviado en el Servicio A (campo Número Caso CRM)
        @SerializedName("openitem")
        public List<Long> openitem;       //Es el número del openitem asociado al pago
        @SerializedName("siniestro")
        public Long siniestro;              //Es el número de reclamo que se está asociando al número de caso del CRM
        @SerializedName("cobertura")
        public String cobertura;              //Nombre de la Cobertura a la que se realizará el pago
        @SerializedName("tipocarta")
        public String tipocarta;              /*Origen desde donde se llama al servicio: AP: Aprobar Pago (al seleccionar la cobertura y dar click en Aceptar) RS: Rechazar Siniestro (al seleccionar el objeto afectado es el botón Rechazar del menú) RC: Rechazar Cobertura (al seleccionar la cobertura es el botón Rechazar del menú) RR: Rechazar Caso por Carta de Requisito (al seleccionar la cobertura es el botón Requisitos del menú)*/
        @SerializedName("numerocarta")
        public String numerocarta;              //Numero de carta
        @SerializedName("fechacarta")
        public String fechacarta;              //Datos de la carta de rechazo (aplica para origen RS, RC y RR)
        @SerializedName("fechacartaaprob")
        public String fechacartaaprob;              //Enviar en blanco. No aplica para masivos.
        @SerializedName("numeromemo")
        public String numeromemo;              //Enviar en blanco. No aplica para masivos.
        @SerializedName("montomemo")
        public String montomemo;              //Datos del memorándum de pago (aplica para origen AP).

        public EventNotificacionCRMClaim(String numerocaso ,
                                         List<Long> openitem ,
                                         Long siniestro ,
                                         String cobertura ,
                                         String tipocarta ,
                                         String numerocarta ,
                                         String fechacarta ,
                                         String fechacartaaprob ,
                                         String numeromemo ,
                                         String montomemo) {
            this.numerocaso = numerocaso;
            this.openitem = openitem;
            this.siniestro = siniestro;
            this.cobertura = cobertura;
            this.tipocarta = tipocarta;
            this.numerocarta = numerocarta;
            this.fechacarta = fechacarta;
            this.fechacartaaprob = fechacartaaprob;
            this.numeromemo = numeromemo;
            this.montomemo = montomemo;
        }

        @Override
        public String toString() {
            return "\"EventNotificacionCRMClaim\":"+new Gson().toJson(this);
        }
    }

    static class EventNotificacionCRMClaimResponse{
        boolean isReceived;
        String msg;

        public EventNotificacionCRMClaimResponse(boolean isReceived, String msg) {
            this.isReceived = isReceived;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "EventNotificacionCRMClaimResponse{" +
                    "isReceived=" + isReceived +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    static class EventNotificationCRMCallWeb{
        private final static String JSON_NAMEOUT = "jsonActualizarCasoSiniestroCRM";
        private final static String JSON_NAMEIN = "responseJSON";
        private final static String REQ_METHOD = "POST";
        private static final String POSTFIX_TEST_ = "Object";


        public static EventNotificacionCRMClaimResponse processEventNotification(String jsonOut) throws Exception {
            EventNotificacionCRMClaimResponse response = sendToWebService(jsonOut, MediaType.APPLICATION_JSON);
            return response;
        }

        private static EventNotificacionCRMClaimResponse sendToWebService(String outSend, String mediatype) throws Exception {
            String inJSON = sendRequestRaw(outSend);
            final EventNotificacionCRMClaimResponse response = new Gson().fromJson(inJSON, EventNotificacionCRMClaimResponse.class);
            return response;
        }

        private static String sendRequestRaw(String jsonOut) throws Exception {
            Map<String, String> outMap = new HashMap<String, String>(); outMap.put(JSON_NAMEOUT, jsonOut);
            Map<String, String> headerMap = new HashMap<String, String>();
            headerMap.put("Content-Type", "application/json;charset=utf-8");
            headerMap.put("Accept", "application/json");
            return HttpUtil.executePostURLJSONRawContent(EventNotificationWorkerCRMClaim.getRequestURL()/*+POSTFIX_TEST_*/, headerMap, outMap);
        }
    }
}
