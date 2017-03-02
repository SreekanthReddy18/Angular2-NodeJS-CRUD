package com.consisint.acsele.util.evaluator;

/**
 * The collection of constants that are published to the evaluator
 * Title: EvaluationConstants.java <br>
 * Copyright: (c) 2003 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Consis International
 */

// TODO - Implementar el uso de esta clase en todas las clases que publiquen simbolos para evaluar o validar.
// TODO - Los simbolos de la poliza se est�n publicando en N sitios. Buscar POLICY_INITIAL_DATE y pasar a un solo sitio.

public class EvaluationConstants {

    // TODO: Some constants still have to be renamed
    // TODO: Traducir las constantes que est�n en espa�ol a ingl�s; Usar la convenci�n adecuada

    // Policy
    public static final String POLICY_PK = "POLICYPK";
    public static final String POLICY_NUMBER = "POLICYNUMBER";
    public static final String CURRENT_ID_POLICY = "currentIdPolicy";
    public static final String POLICY_EVENT = "PolicyEvent";
    public static final String LAST_POLICY_EVENT = "lastPolicyEvent";
    public static final String EVENT = "Event";
    public static final String POLICY_INITIAL_DATE = "PolicyInitialDate";
    public static final String POLICY_END_DATE = "PolicyEndDate";
    public static final String PARTICIPATION_IN_POLICY = "inPolicy";
    public static final String POLICY_STATE = "policyState";
    public static final String PAID_FREQUENCY = "FrecuenciaPago";
    public static final String POLICY_FINANCIAL_PLAN = "policyFP";
    public static final String NUMBER_PREMIUM_QUOTAS = "NumberOfPremiumQuotas";
    public static final String NUMBER_COMMISSION_QUOTAS = "NumberOfCommissionQuotas";
    public static final String NUMBER_PREMIUM_QUOTAS_WF = "NumberOfPremiumQuotasWF";
    public static final String NUMBER_COMMISSION_QUOTAS_WF = "NumberOfCommissionQuotasWF";
    public static final String FINANCIAL_PLAN_PERIODICITY = "FinancialPlanPeriodicity";
    public static final String FINANCIAL_PLAN_PERIODICITY_FACTOR = "FinancialPlanPeriodicityFactor";
    public static final String POLICY_COVS_IDS = "policyCovs";
    public static final String VIGENCY_TYPE = "TipVigencia";
    public static final String CURRENCY = "polCurrency";
    public static final String MONEDA = "Moneda"; // TODO Eliminar cuando los funcionales garanticen que no se usa.
    public static final String CURRENCY_FINANCIAL_PLAN = "financialPlanCurrency";
    public static final String CURRENCY_CODE = "CurrencyCode";
    public static final String USER_LOGIN = "UserLogin";
    public static final String USER_TYPE = "UserType";
    public static final String IS_COMMISSION = "IsCommission";
    public static final String IS_COLLECTION = "IsCollection";
    public static final String LAST_OP_INITIAL_DATE = "LastOpInitialDate";
    public static final String LAST_OP_FINISH_DATE = "LastOpFinishDate";
    public static final String FREE_TEXT = "CBFREETEXT";
    public static final String OPERATION_DATE = "OperationDate";
    public static final String POLICY_LEVEL2 = "PolicyLevel";
    public static final String RESERVE_HEIGHT = "height";
    public static final String RESERVE_MONTH = "month";
    public static final String AGEINSURED = "AgeInsured";
    public static final String GUARANTEEDYEARVALUE = "GuaranteedYearValue";
    public static final String POLICYDURATION = "PolicyDuration";
    public static final String ALL_PLANS = "allPlans";
    public static final String INCLUDED_PARTICIPATIONS = "includedParticipations";
    public static final String EXCLUDED_PARTICIPATIONS = "excludedParticipations";
    public static final String MODIFICATION_PARTICIPATIONS = "modificationParticipations";
    public static final String SQ_POL_PRODUCT = "sq_pol_product";
    public static final String SQ_CLAIM_PRODUCT = "sq_claim_product";
    public static final String CORRELATIVE_NUMBER = "PCorrelativo";
    public static final String POLICY_BRANCH_LOCATION = "policyBranchLocation";
    public static final String EXCLUDED_COVS = "excludedCoverages";
    public static final String INCLUDED_COVS = "includedCoverages";
    public static final String COLLECTOR = "collector";
    public static final String TEMPLATE_EVENT = "TemplateEvent";

    // Risk Unit
    public static final String RISK_UNIT_PK = "RISKUNITPK";
    public static final String RISK_UNIT_INITIAL_DATE = "RUInitialDate";
    public static final String RISK_UNIT_END_DATE = "RUEndDate";
    public static final String CURRENT_ID_RISK_UNIT = "currentIdRiskUnit";
    public static final String RISK_UNIT_EVENT = "RUEvent";
    public static final String PARTICIPATION_IN_RISK_UNIT = "inRU";
    public static final String RISK_UNIT_STATE = "RUState";
    public static final String RISK_UNIT_FINANCIAL_PLAN = "RUFP";
    public static final String RISK_UNIT_COVS_IDS = "RUCovs";
    public static final String RU_PREFIX = "RU";
    public static final String NUMBER_PREMIUM_QUOTAS_RU = "NumberOfPremiumQuotasRU";
    public static final String RISK_UNIT_MOVEMENT_DATE = "RUMovementDate";

    // Insurance Object
    public static final String IO_INITIAL_DATE = "IOInitialDate";
    public static final String IO_END_DATE = "IOEndDate";
    public static final String IO_EVENT = "IOEvent";
    public static final String IO_IDENTIFICATION = "IOIdentification";
    public static final String IO_STATE = "IOState";
    public static final String IO_COVS_IDS = "IOCovs";
    public static final String CURRENT_ID_INSURANCE_OBJECT = "currentIdInsuranceObject";
    public static final String PLAN = "Plan";
    public static final String NEW_INSURED_AMOUNT = "SumAsegNew";
    public static final String PLAN_ID = "PlanID";
    public static final String PARTICIPATION_IN_IO = "inIO";
    public static final String IO_CURRENTIDINSURANCEOBJECT = "IO_CURRENTIDINSURANCEOBJECT";
    public static final String IOPK = "IOPK";
    public static final String IO_PREFIX = "IO";


    // Coverage
    public static final String COVERAGE_SELECTED = "coverageSelected";
    public static final String CURRENT_ID_COVERAGE = "currentIdCoverage";
    public static final String CURRENT_ID_TITLE_COVERAGE = "currentIdTitleCoverage";
    public static final String CURRENT_ID_PACKAGE = "currentIdPackage";
    public static final String EC_INITIAL_DATE = "ECInitialDate";
    public static final String EC_END_DATE = "ECEndDate";
    public static final String COVERAGE_EVENT = "CoverageEvent";
    public static final String VAL_COVERAGE_CODE = "VALCoverageCode";
    public static final String COVERAGE_STATE = "CoverageState";
    public static final String LAST_ID_COVERAGE = "lastIdCoverage";
    public static final String LAST_COVERAGE = "lastCoverage";
    public static final String EXTENDED_DAYS = "extendedDays";
    public static final String EXTENDED_YEARS = "extendedYears";
    public static final String VALIDITY = "validityDesc";
    /*Reserve*/
    public static final String CONFIGURATED_COV_ID = "configuratedCovId";
    public static final String COVERAGE_RESERVE = "coverageCalcReserve";
    public static final String PLAN_RESERVE = "planCalcReserve";
    public static final String TERMINAL_RESERVE_AMOUNT = "terminalReserveAmount";
    public static final String BALANCE_RESERVE_AMOUNT = "balanceReserveAmount";
    public static final String MATH_RESERVE_AMOUNT = "mathReserveAmount";
    public static final String MODIFIED_RESERVE_AMOUNT = "modifiedReserveAmount";
    public static final String TYPE_RESERVE = "typeReserve";

    // Reserva de Riesgo en Curso
    public static final String POLIZA_RRC = "polizaRRC";
    public static final String COMPANIA_RRC = "companiaRRC";
    public static final String SUCURSAL_RRC = "sucursalRRC";
    public static final String RAMO_RRC = "ramoRRC";
    public static final String FECHA_ENDOSO_RRC = "fechaEndosoRRC";
    public static final String FECHA_CORTE_RRC = "fechaCorteRRC";
    public static final String INTERIOR_EXTERIOR_RRC = "interiorExteriorRRC";
    public static final String PRODUCCION_VIGENTE_RRC = "produccionVigenteRRC";
    public static final String IDENTIFICADOR_RRC = "identificadorRRC";
    public static final String CONSTITUCION_BRUTA_RRC = "constitucionBrutaRRC";
    public static final String CONSTITUCION_INTERIOR_RRC = "constitucionInteriorRRC";
    public static final String CONSTITUCION_EXTERIOR_RRC = "constitucionExteriorRRC";
    public static final String LIBERACION_BRUTA_RRC = "liberacionBrutaRRC";
    public static final String LIBERACION_EXTERIOR_RRC = "liberacionExteriorRRC";
    public static final String LIBERACION_INTERIOR_RRC = "liberacionInteriorRRC";

    // Product
    public static final String PRODUCT = "product";
    public static final String PRODUCT_CODE = "prodcode";
    public static final String PRODUCT_NAME = "prodname";
    public static final String PRODUCT_ID = "productId";
    public static final String PRODUCT_TEMPLATE = "productLabel";
    public static final String PRODUCT_DESC = "ProducDesc";
    public static final String PRODUCT_TOLERANCE = "ProductTolerance";

    // System & Xtras
    public static final String SYSTEM_DATE = "sysDate";
    public static final String OPERATION_PK = "OPERATIONPK";
    public static final String AUTO_DATE = "propAutoDate";
    public static final String CONTRACT_ID = "CONTRACTID";
    public static final String HAS_FINANCIAL_PLAN = "HasFinPlan";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";

    //Day, Month and Year (SysDate)
    public static final String SYSDATE_DAY = "sysdateDay";
    public static final String SYSDATE_MONTH_NAME = "sysdateMonthName";
    public static final String SYSDATE_MONTH_NUMBER = "sysdateMonthNumber";
    public static final String SYSDATE_YEAR = "sysdateYear";

    public static final String CLIENTE_PK = "CLIENTEPK"; // TODO Traducir

    // Evaluation Engine
    public static final String DCO = "DCO";
    public static final String OLD_SYMBOL = "OLD";
    public static final String MIDNIGHT = "midnight";
    public static final String AFTER_EVALUATION = "INDEvaluacion"; // TODO Traducir
    public static final String EXPRESION_EVALUATOR = "expresionEvaluator";
    public static final String SYMBOL_TABLE = "SYMBOL_TABLE";

    // ThirdParty
    public static final String THIRDPARTY_ID = "ThirdPartyId";
    public static final String THIRDPARTY_CODE_IDENTIFIER = "ThirdPartyCodeIdentifier";
    public static final String ROLE_ID = "RoleId";
    public static final String ROLE_DESC = "Role";
    public static final String THIRDPARTY_COT = "PersonType";
    public static final String Asesor_PredeterminedName = "ThirdPartyName";
    public static final String THIRDPARTY_NAME = "ThirdPartyName";
    public static final String BENEFICIARY_NAME = "ThirdpartyName"; // TODO: Why???
    public static final String RFC = "RFC";
    public static final String ROLE_DCO = "roleDCO";
    public static final String ROLE_STATUS = "roleStatus";
    public static final String COUNT = "Count";
    public static final String IS_DECREMENT_DEPOSIT = "isDecrementDeposit";
    public static final String THIRDPARTY_COMPLETE_ADDRESS = "THIRDPARTY_COMPLETE_ADDRESS";
    public static final String INSURANCE_COMPLETE_ADDRESS = "INSURANCE_COMPLETE_ADDRESS";
    public static final String CLIENT_COMPLETE_ADDRESS = "CLIENT_COMPLETE_ADDRESS";
    public static final String THIRPARTY_PERSON_TYPE = "thirdPartyPersonType";
    public static final String THIRPARTY_ADDRESS = "FullAddress";
    public static final String THIRDPARTY_DPSID = "ThirdPartyDpsId";
    public static final String THIRDPARTY = "ThirdParty";
    public static final String THIRDPARTY_ENABLED = "ThirdPartyEnabled";
    public static final String EMAIL = "Email";

    //THIRDPARTYMOVEMENTPOLICY
    public static final String CONCEPT_MOVEMENT_POLICY = "conceptMovPol";
    public static final String AMOUNT_MOVEMENT_POLICY = "amountMovPol";
    public static final String DATEOPERATION_MOVEMENT_POLICY = "dateOperationMovPol";
    public static final String DATEUSERECIPENT_MOVEMENT_POLICY = "dateUseRecipentMovPol";
    public static final String DUEDATE_MOVEMENT_POLICY = "dueDateMovPol";

    //THIRDPARTYMOVEMENTRISKUNIT
    public static final String CONCEPT_MOVEMENT_RU = "conceptMovRU";
    public static final String AMOUNT_MOVEMENT_RU = "amountMovRU";
    public static final String DATEOPERATION_MOVEMENT_RU = "dateOperationMovRU";
    public static final String DATEUSERECIPENT_MOVEMENT_RU = "dateUseRecipentMovRU";
    public static final String DUEDATE_MOVEMENT_RU = "dueDateMovRU";

    // Credit Line
    public static final String CREDITLINE_PREF = "CreditLine";
    public static final String CREDITLINE_DESC = "Description";
    public static final String CREDITLINE_STARTDATE = "StartDate";
    public static final String CREDITLINE_ENDDATE = "EndDate";
    public static final String CREDITLINE_CURRENCY = "Currency";
    public static final String CREDITLINE_AMOUNT = "Amount";

    // financial information
    public static final String LEGAL_FINANCIAL_INFORMATION_PREF = "LegalFinancialInf";
    public static final String NATURAL_FINANCIAL_INFORMATION_PREF = "NaturalFinancialInf";

    // Financial Movements
    public static final String COMMISSION = "COMMISSION";
    public static final String PREMIUM = "PREMIUM";
    public static final String PAYMENT = "PAYMENT";
    public static final String PAYMENTMODE = "PAYMENTMODE";
    public static final String PREMIUM_COINSURANCE = "PREMIUM_COINSURANCE";
    public static final String COINSURANCE_TYPE = "coinsuranceType";
    public static final String PAYMENT_MODE_NAME = "PaymentModeName";
    public static final String PAYMENT_DUEDATE = "PaymentDueDate";

    // Commercial Network Symbols
    public static final String PREMIUM_AMOUNT = "premiumAmount";
    public static final String DOCTYPE = "DocType";
    public static final String ORIGINAL_THIRDPARTY = "OriginalThirdParty";

    // Claim's Payments
    public static final String CLAIM_PAYMENT_ID = "ClaimPaymentId";//TOTALIZE PK
    public static final String CLAIM_PAYMENT_DATE = "ClaimPaymentDate";//paymentDate
    public static final String TAX_BENEFICIARY_ID = "TaxBenficiaryId";
    public static final String CLAIM_PK = "CLAIMPK";
    public static final String CLAIM_NUMBER = "CLAIMNUMBER";
    public static final String CLAIM_AMOUNT = "ClaimAmount";
    public static final String PLAN_NAME = "PlanName";
    public static final String INSURANCE_OBJECT_NAME = "InsuranceObjectName";
    public static final String COVERAGE_NAME = "CoverageName";
    public static final String OCURRENCE_DATE = "OcurrenceDate";
    public static final String CLAIM_OPEN_DATE = "claimOpenDate";
    public static final String RESERVE_COVERAGE = "ReserveCoverage";
    public static final String RESERVE_COVERAGE_MIN = "ReserveCoverageMin";
    public static final String COVERAGE_RESERVE_DEDUCTIBLE = "coverageReserveDeductible";
    public static final String CLAIM_BENEFICIARY = "ClaimBeneficiary";
    public static final String CLAIM_NORMAL_RESERVE_DESC = "ClaimNormalReserveDesc";
    public static final String CLAIM_REJECT_MOTIVE = "ClaimRejectMotive";
    public static final String CLAIM_STATUS = "claimStatus";
    public static final String CLAIM_RECIPIENT = "ClaimRecipient";
    public static final String CLAIM_RI_REVERSE = "RI_REVERSE";
    public static final String ISCONCEPTRESERVE = "ISCONCEPTRESERVE";
    public static final String EVENTCLAIMDESC = "EventClaimDesc";
    public static final String CLAIM_BRANCH = "claimBranch";
    public static final String CLAIM_INVOICE_AMOUNT = "claimInvoiceAmount";
    public static final String CLAIM_INVOICE_DATE = "claimInvoiceDate";
    public static final String INVOICE_TOTAL_AMOUNT = "invoiceTotalAmount";
    public static final String INVOICE_TOTAL_EXEMPT = "invoiceTotalExempt";
    public static final String INVOICE_EXCHANGERATE = "invoiceExchangeRate";
    public static final String CLAIM_TYPE_RESERVE = "typeReserve";
    public static final String CLAIM_NORMAL_RESERVE_DATE = "claimNormalReserveDate";
    public static final String CLAIM_RESERVE_STATUS = "claimReserveStatus";
    public static final String RESERVE_BY_CONCETP = "reserveByConcetp";
    public static final String AMOUNT_RESERVE_BY_CONCETP = "amountReserveByConcetp";
    public static final String BENEFIT_PAYMENT_AMOUNT = "BenefitPaymentAmount";
    public static final String IS_CLAIM_CLOSED_DENIED = "isClaimClosedDenied";
    public static final String CLAIM_RESERVE_TYPE = "ClaimReserveType";
    public static final String RESERVE_CONCEPT = "ReserveConcept";
    public static final String RESERVE_CONCEPT_DESC = "ReserveConceptDesc";
    public static final String CLAIM_RESERVE_DATE = "claimReserveDate";
    public static final String PAYMENT_TYPE_REVERSED = "paymenTypeReversed";
    public static final String REVERSED_AMOUNT = "reversedAmount";


    // Payment Orders
    public static final String PAYMENT_ORDER_CREATION_DATE = "PaymentOrderCreationDate";
    public static final String PAYMENT_ORDER_COMMITMENT_DATE = "PaymentOrderCommitmentDate";
    public static final String PAYMENT_ORDER_REASON = "PaymentOrderReason";
    public static final String PAYMENT_ORDER_TYPE = "PaymentOrderType";
    public static final String PAYMENT_ORDER_PK = "PaymentOrderPK";
    public static final String PAYMENT_ORDER_AMOUNT = "PaymentOrderAmount";
    public static final String PAYMENTAMOUNT = "PaymentAmount";
    public static final String PAYMENT_ORDER_DEDUCTIBLE_AMOUNT = "PaymentOrderDeductibleAmount"; // For the entrys, this symbol works for publish the accountant deductible from payment
    public static final String PAYMENT_ORDER_EDIT_DEDUCTIBLE_AMOUNT = "PaymentOrderEditDeductibleAmount"; // For the entrys, this symbol works for publish the edit deductible from payment
    public static final String USER_ACTION = "UserAction";// User Approve Payment
    public static final String PAYMENT_AMOUNT_BY_INSURED = "PaymentAmountInsured";
    public static final String PAYMENT_ORDER_NUMBER = "PaymentOrderNumber";
    public static final String PAYMENT_ORDERS_APPROVED = "CountPaymentOrderApr";
    public static final String PAYMENT_ORDERS_AMOUNT_APR = "AmountPaymentOrderApr";
    public static final String PAYMENT_ORDERS_BILL_DETAIL = "billDetail";
    public static final String PAYMENT_ORDERS_BILL_NUMBERS = "billNumbers";
    public static final String PAYMENT_RETENTION_AMOUNT = "RetentionAmount";
    public static final String PAYMENT_RETENTION_TYPE = "RetentionType";
    public static final String PAYMENT_ORDER_STATUS = "PaymentOrderStatus";
    public static final String PAYMENT_ORDER_PARTICIPATION_PERCENTAGE = "PaymentOrderParticipationPercentage";
    public static final String PAYMENT_ORDER_START_DATE = "PaymentOrderStartDate";
    public static final String PAYMENT_ORDER_END_DATE = "PaymentOrderEndDate";
    public static final String PAYMENT_ORDER_BRANCHOFFICEID = "PaymentOrderBranchOfficeId";
    public static final String PAYMENT_ORDER_BRANCHOFFICE = "PaymentOrderBranchOffice";
    public static final String PAYMENT_ORDER_ISFINAL = "PaymentOrderIsFinal";
    public static final String PAYMENT_ORDER_PENALTY = "PaymentOrderPenalty";
    public static final String PAYMENT_PARTICIPATION_PERCENTAGE = "ParticipationPercentage";

    //Payment forms
    public static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";
    public static final String BANK_NAME = "bankName";
    public static final String CREDIT_CARD_NUMBER = "creditCardNumber";
    public static final String CREDIT_CARD_TYPE = "creditCardType";
    public static final String CREDIT_CARD_DUE_DATE = "creditCardDueDate";
    public static final String DEPOSIT_DATE = "depositDate";
    public static final String CHECK_NUMBER = "checkNumber";
    public static final String CHECK_DATE = "checkDate";
    public static final String CONFORMATION_NUMBER = "conformationNumber";

    public static final String DAYS_ACCUMULATED = "daysAccumulated";

    // Users & Security
    public static final String USER = "User";
    public static final String USER_NAME = "UserName";
    public static final String GROUP = "Group";

    // UAA - Open Item
    public static final String OPEN_ITEM_ID = "openItemID";
    public static final String OPEN_ITEM_DATE = "openItemDate";
    public static final String DOCUMENT_DATE = "docDate";
    public static final String DATE_USE_RECIPENT = "dateUseRecipent";
    public static final String DUE_DATE = "dueDate";
    public static final String DETAIL_DUE_DATE = "detailDueDate";
    public static final String OPEN_ITEM_STATUS = "status";
    public static final String OPEN_ITEM_SUBSTATUS = "substatus";
    public static final String DOCUMENT_TYPE = "DocType";
    public static final String OPEN_ITEM_CURRENCY = "currency";
    public static final String OPEN_ITEM_BALANCE = "balance";
    public static final String OPEN_ITEM_REFERENCETYPE = "referenceType";
    public static final String WARNING_COLLECTION_ID = "warningCollectionID";
    public static final String AMOUNT_OPENITEM = "amountOpenItem";
    public static final String OPENITEM_QUOTE = "openItemQuote";
    public static final String AMOUNT_UAADETAIL = "amountUaaDetail";
    public static final String CONCEPT_UAADETAIL = "conceptUaaDetail";
    public static final String SYMBOL_UAADETAIL = "symbolUaaDetail";
    public static final String DETAIL_AMOUNT_OPENITEM = "detailAmountOpenItem";
    public static final String OPEN_ITEM_BASE_AMOUNT = "openItemBaseAmount";
    public static final String OPEN_ITEM_TOTAL_AMOUNT = "openItemTotalAmount";
    public static final String NUMBER_PAYMENTS_QUOTAS = "NumberOfQuotasPayments";

    public static final String IS_RESERVAL = "isReversal";
    public static final String TOTAL_AMOUNT = "TotAmount";
    public static final String AMOUNT = "Amount";
    public static final String PAYMENT_TRANSACTION_TYPE = "PaymentTransactionType";
    public static final String TOTAL_CLIENT = "TotCliente";
    public static final String PREMIUM_DEPOSIT_TYPE = "PremiumDepositType";
    public static final String IS_DECREMENT_TYPE = "isDecrementDeposit";
    public static final String CURRENCY_PAID = "MonedaCobranza";
    public static final String CURRENCY_PAID_DESC = "MonedaCobranzaDesc";
    public static final String PAID_AMOUNT = "MontoPagado";

    //UAA - Currency Rate
    public static final String CURRENCY_ORIGIN = "currencyOrigin";
    public static final String CURRENCY_DESTINATION = "currencyDestination";
    public static final String CURRENCY_RATE = "currencyRate";
    public static final String SYSDATE_CURRENCY_RATE = "sysDateCurrencyRate";
    public static final String CURRENCY_RATE_DATE = "currencyRateDate";

    // OpenItemReceipts
    public static final String OPEN_ITEM_RECEIPT_CCY_RATE = "openItemReceiptCcyRate";
    public static final String OPEN_ITEM_RECEIPT_AMOUNT = "openItemReceiptAmount";
    public static final String OPEN_ITEM_RESERVE_AMOUNT = "freeReserveDacAmount";


    // Benefits
    public static final String BENEFIT_PAYMENTS = "BENEFITPAYMENTS";
    public static final String BENEFIT_PERIODICITY = "BENEFITPERIODICITY";
    public static final String BENEFIT_START_DATE = "BENEFITSTARTDATE";
    public static final String ACCOUNTING_OPERATION = "AccountingOperation";

    // Funds
    public static final String FUND_AMOUNT = "FundAmount";
    public static final String FUND_PARTICIPATION = "FundParticipation";

    //Evaluation Levels
    static public final String POLICY_LEVEL = "POL";
    static public final String PRODUCT_LEVEL = "PRO";
    static public final String RISK_UNIT_LEVEL = "RU";
    static public final String IO_LEVEL = "IO";
    static public final String COV_LEVEL = "COV";
    static public final String FIRST_COV_LEVEL = "FIRST_COV";
    static public final String COV_OPT_LEVEL = "COV_OPT";
    static public final String FIRST_COV_OPT_LEVEL = "FIRST_COV_OPT";
    static public final String COV_MAND_LEVEL = "COV_MAND";
    static public final String PLAN_LEVEL = "PLA";
    static public final String MOVEMENTS_LEVEL = "MOV";
    static public final String MOVEMENTS_LEVEL_PO = "MOV_POL";
    static public final String MOVEMENTS_LEVEL_RU = "MOV_RU";
    static public final String MOVEMENTS_UAA_LEVEL = "UAA";
    static public final String LOANS_LEVEL = "LOAN";
    static public final String GVT_LEVEL = "GVT";
    static public final String IO_LEVEL_INFO = "IO_INFO";
    static public final String REQ_LEVEL = "REQ";
    static public final String POR_APR_LEVEL = "POR_APR"; //Approved Payment Orders
    static public final String RU_LEVEL_ACCUM = "RU_ACCUM";
    static public final String CLAU_LEVEL = "CLAU";
    static public final String BENEFICIARY_PRINCIPAL_LEVEL = "BEN_PRI";
    static public final String BENEFICIARY_SECONDARY_LEVEL = "BEN_SEC";
    static public final String PENDING_REQ_LEVEL = "PENDING_REQ";
    static public final String WILDCARDQUOTA_INITIAL_LEVEL = "WILDCARD_QUOTA_INITIAL";
    static public final String WILDCARDQUOTA_UPDATED_LEVEL = "WILDCARD_QUOTA_UPDATED";
    static public final String POLICY_DIFFERENCE_LEVEL = "POL_DIFF";
    static public final String SURCHARGE_LEVEL = "SURCHARGE_LEVEL";
    static public final String RESERVE_CONCEPTS = "RES_CONS";
    static public final String LEVEL = "LEVEL";


    // Coinsurance
    static public final String COIN_ID = "CoinsuranceID";
    static public final String COIN_DESC = "CoinsuranceDescription";

    //Agreements
    static public final String AGREEMENT_ID = "AgreementId";
    static public final String AGREEMENT_NAME = "agreementName";
    static public final String AGREE_INITIAL_DATE = "agreeInitialDate";
    static public final String AGREE_END_DATE = "agreeEndDate";
    static public final String AGREEMENT_DESC = "agreementDesc";
    static public final String AGREEMENT_VERSION = "agreementVersion";
    static public final String AGREEMENT_LEVEL = "AGREE";
    static public final String AGREEMENT_NUMBER = "agreementNumber";
    static public final String AGREEMENT_COTID = "agreementCotId";
    static public final String AGREEMENT_IDDCO = "agreementIdDco";
    static public final String AGREEMENT_STATUS = "agreementStatus";
    static public final String AGREEMENT_VERSION_ID = "agreementVersionId";
    static public final String AGREEMENT_PK = "agreementPk";


    //Inspections
    static public final String INSPECTION_ID = "inspectionId";
    static public final String INSPECTION_VERSION = "inspectionVersion";
    static public final String INSPECTION_CREATED_DATE = "inspectionCreatedDate";
    static public final String INSPECTION_EXPIRED_DATE = "inspectionExpiredDate";
    static public final String INSPECTION_STATUS = "inspectionStatus";
    static public final String INSPECTION_INSPECTOR = "inspectionInspector";
    static public final String INSPECTION_USERCLIENT = "inspectionUser";
    static public final String INSPECTION_USERSECRETARY = "inspectionSecretary";
    static public final String INSPECTION_BRANCH = "inspectionBranch";
    static public final String INSPECTION_GROUPS = "inspectionGroups";
    static public final String INSPECTION_SECONDARY_INSPECTORS = "secondaryInspectors";
    static public final String INSPECTION_TYPES = "inspectionTypes";
    static public final String INSPECTION_INSINFOVERSION = "InspectionInfoVersion";

    // Due Date
    static public final String MOV_STARTDATE="MOVStartDate"; //correspondiente a la fecha de inicio de vigencia del movimiento
    static public final String MOV_ENDDATE="MOVEndDate"; //correspondiente a la fecha de fin de vigencia del movimiento
    static public final String MOV_PAYMENTNUMBER="MOVNumber"; //correspondiente al numero de pago que se esta generando.

    //DPS
    public static final String START_DATE_DPS = "startDateDps";
    public static final String RECEIVED_REQUIREMENTS = "requirementsDPS";

    //Document Reinsurance
    public static final String REINSURANCE_TOTAL_AMOUNT = "totalAmount";
    public static final String REINSURER_NAME = "reinsurerName";
    public static final String START_DATE_LIQUIDATION = "startDateLiquidation";
    public static final String END_DATE_LIQUIDATION = "endDateLiquidation";
    public static final String CREATED_DATE_LIQUIDATION = "createdDateLiquidation";
    public static final String BROKER_NAME = "brokerName";
    public static final String CORRELATIVE_ID = "correlative";
    public static final String TYPE_OPERATION = "typeOperation"; //Es Debito o Credito
    public static final String CONCEPTS_DETAILS = "conceptsDetails";

    public static final String YEARRANSOM = "yearRansom";
    public static final String YEAR_RETIREMENT = "yearRetirement";
    public static final String AMOUNT_FORM = "amountForm";
    public static final String MINPARTIALRESCUEFORM = "minPartialRescueForm";
    public static final String MAXPARTIALRESCUEFORM = "maxPartialRescueForm";
    public static final String TOTALRESCUEFORM = "totalRescueForm";
    public static final String RESCUECOSTFORM = "rescueCostForm";

    public static final String PREMIUM_AMOUNT_TO_GONNE = "premiumAmountToGonne";
    public static final String AMOUNT_OVERDUE_PREMIUMS_ISSUED = "amountOverduePremiumsIssued";
    public static final String EXPENSE_AMOUNT_RECEIVABLE = "expenseAmountReceivable";
    public static final String AMOUNT_FOR_INTEREST_LOANS = "amountForInterestLoans";
    public static final String AMOUNT_LOANS = "amountLoans";
    public static final String AMOUNT_OVERDUE_PREMIUMS = "amountOverduePremiums";
    public static final String ENTRY_DATE = "Fecha";
    public static final String TOTAL_DEDUCTIONS = "totalDeduction";
    public static final String ENTRY_AMOUNT = "Monto";
    public static final String ENTRY_AUXILIAR = "Auxiliar";
    public static final String ENTRY_AUXILIAR_CREDIT_VALUE = "Credito";
    public static final String ENTRY_AUXILIAR_DEBIT_VALUE = "Debito";

    public static final String DELAYINTEREST = "delayInterest";
    public static final String PENDINGPREMIUN = "pendingPremiun";

    public static final String GROUP_REINSURANCE = "groupReinsurance";
    public static final String TECH_ACC_LABEL = "componentDescription";
    public static final String TECH_ACC_AMOUNT = "componentAmount";
    public static final String TECH_ACC_TABLE = "technical_accounts";
    public static final String RESINSURANCE_DEPOSIT = "reinsuranceDeposit";

    //new symbols reinsurance liquidation SUM
    public static final String PROPORTIONAL_CONTRACT_SUM = "proportionalContractSum";
    public static final String NOPROPORTIONAL_CONTRACT_SUM = "noProportionalContractSum";
    public static final String FACULTATIVE_MANDATORY_CONTRACT_SUM = "facultativeMandatoryContractSum";

    //new symbols reinsurance type contract
    public static final String RI_PROPORTIONAL_CONTRACT_TYPE = "RIProportionalContractType";
    public static final String RI_FACULTATIVE_CONTRACT_TYPE = "RIFacultativeContractType";
    public static final String RI_NOPROPORTIONAL_CONTRACT_TYPE = "RINoProportionalContractType";


    // suma total de contractos proporcionales,facultativo, no proportionales
    public static final String TOTAL_SUM_ALL_CONTRACTS = "totalAllContracts";
    public static final String QUARTER_REINSURANCE_LIQUIDATION = "quarterReinsuranceLiquidation";
    public static final String YEAR_REINSURANCE_LIQUIDATION = "yearReinsuranceLiquidation";


    // Reaseguro - Contabilización de Valores Garantizados
    public static final String SA_TOTAL = "SATOTAL"; //(Suma Aseguradas total)
    public static final String SA_COURTESY = "SACOURTESY"; //(Suma Asegurada Cedida)
    public static final String SA_HELD = "SAHELD"; //(Suma Asegurada Retenida)
    public static final String SA_REINSURANCE_GROUP = "SAREINSURANCEGROUP";

    // Pending Document Requirements/Requisitos no entregados o pendientes
    public static final String PCOMPANIA = "pcompania";
    public static final String NITCOMPANIA = "nitcompania";
    public static final String PUSUARIO = "pusuario";
    public static final String RI_CURRENCY_ISOCODE = "currency_isocode";
    public static final String RI_CURRENCY_DESC = "currency_desc";



    // Pending Document Requirements/Requisitos no entregados o pendientes
    public static final String PENDING_REQUIREMENTS = "PendingRequirements";

    //Entrys for Reinsurance
    public static final String RI_AMOUNT = "RIAmount";
    public static final String RI_TOTAL_AMOUNT = "RITotalAmount";
    public static final String RI_RESERVE_AMOUNT = "RIReserveAmount";
    public static final String RI_RESERVE_TOTAL_AMOUNT = "RIReserveTotalAmount";
    public static final String RI_PAYMENT_AMOUNT = "RIPaymentAmout";
    public static final String RI_PAYMENT_TOTAL_AMOUNT = "RIPaymentTotalAmout";
    public static final String RI_RECOVERY_AMOUNT = "RIRecoveryAmout";
    public static final String RI_RECOVERY_TOTAL_AMOUNT = "RIRecoveryTotalAmout";
    public static final String CONTRACT_COMPONENT_CAPACITY = "ContractComponentCapacity";
    public static final String CONTRACT_COMPONENT_TYPE = "ContractComponentType";
    public static final String RI_PREMIUM_ASSIGNMENT_ADJUSTMENT = "RIAmountPremiumAdjustment";
    public static final String AMOUNT_PAID_BY_REINSURER = "AmountPaidByReinsurer";


    //Life
    public static final String INSDUR = "InsDur";

    // Math Reserve
    public static final String RESERVE_TYPE_BENEFIT = "ReserveTypeBenefit";
    public static final String RESERVE_TYPE_EXPENSE = "ReserveTypeExpense";
    public static final String LIFE_RESERVE_TYPE_TERMINAL = "LifeReserveTypeTerminal";
    public static final String LIFE_RESERVE_TYPE_BALANCE = "LifeReserveTypeBalance";
    public static final String LIFE_RESERVE_TYPE_MATH = "LifeReserveTypeMath";
    public static final String LIFE_RESERVE_TYPE_MODIFIED = "LifeReserveTypeModified";
    public static final String RESERVE_METHOD_TYPE_COMMUTATIVE = "ReserveMethodTypeCommutative";
    public static final String RESERVE_METHOD_TYPE_FLOW = "ReserveMethodTypeFlow";
    public static final String RESERVE_PAY_AMOUNT = "payAmount";
    public static final String RESERVE_PAY_REASON = "payReason";

    //Function lookupDurationExtendedTerm
    public static final String MORTALITY_COMUT_RATE = "Mortality_Rate";
    public static final String MORTALITY_SEX = "Mortality_Sex";
    public static final String MORTALITY_IS_SMOKER = "Mortality_Is_Smoker";
    public static final String MORTALITY_AGE = "Mortality_Age";
    public static final String MORTALITY_TABLENAME = "Mortality_TableName";

    //Function used in entries creation for risk reserves
    public static final String RRC_CANAVENTA = "RRC_CANAVENTA";
    public static final String RRC_RISKGROUPID = "RRC_RISKGROUPID";
    public static final String RRC_PRODUCTID = "RRC_PRODUCTID";
    public static final String RRC_CURRENCYID = "RRC_CURRENCYID";
    public static final String RRC_CALCULATEDATE = "RRC_CALCULATEDATE";
    public static final String RRC_PNGR = "RRC_PNGr";
    public static final String RRC_PCGR = "RRC_PCGr";
    public static final String RRC_RETGR = "RRC_RETGr";
    public static final String RRC_RESERVASEGUROS = "RRC_RESERVASEGUROS";
    public static final String RRC_RESERVASCEDIDAS = "RRC_RESERVASCEDIDAS";
    public static final String RRC_RESERVANETA = "RRC_RESERVANETA";
    public static final String RRC_UNEARNEDAMOUNT = "RRC_UNEARNEDAMOUNT";
    public static final String RRC_AMOUNT = "AmountRRC";

    //Function used in entries creation for policy flow tables
    public static final String PFT_HEIGHT = "H";
    public static final String PFT_MONTH = "M";
    public static final String PFT_ROW = "row";

    //TVG Table values (Tabla de valores garantizados)
    public static final String TVG_YEAR = "Tvg_Year";
    public static final String TVG_MONTH = "Tvg_Month";
    public static final String TVG_AGE = "Tvg_Age";
    public static final String TVG_MATHRESERVE = "Tvg_Mathreserve";
    public static final String TVG_FUND = "TvgFund";
    public static final String TVG_TOTALSURRENDER = "Tvg_Totalsurrender";
    public static final String TVG_REDUCEPAID = "Tvg_Reducepaid";
    public static final String TVG_EXTENDEDTERM = "Tvg_Extendedterm";
    public static final String PHDEXTENDEDYEARS = "PHDExtenderYears";
    public static final String TVG_EXTENDEDDAYS = "Tvg_Extendeddays";
    public static final String PHDSURVIVALBENEFIT = "PHDSurvivalbenefit";
    public static final String IS_PROJECTION = "IsProjection";
    public static final String DAMAGE_TEMPLATE = "damageTemplate";
    public static final String IO_AFFECTED = "ioAffected";
    public static final String IS_QUOTATION = "IsQuotation";
    public static final String IS_DISABILITY_COVERAGE = "IsDisabilityCoverage";


    //Loan values
    public static final String LOAN_QUOTE = "loanQuote";
    public static final String LOAN_TOTAL_QUOTE = "loanTotalQuote";
    public static final String LOAN_NUMBER = "loanNumber";
    public static final String LOAN_INTEREST = "loanInterest";
    public static final String LOAN_AMORTIZATION = "loanAmortization";
    public static final String LOAN_AMOUNT = "loanAmount";
    public static final String LOAN_DATE = "loanDate";
    public static final String LOAN_FREQPAY = "loanFreqPay";
    public static final String LOAN_AMOUNT_BALANCE = "loanAmountBalance";
    public static final String LOAN_BALANCE_WARRANTED_VALUE = "balanceWarrantedValue";
    public static final String LOAN_MAX_AMOUNT = "loanMaxAmount";
    public static final String LOAN_MIN_AMOUNT = "loanMinAmount";
    public static final String LOAN_REQUESTED_AMOUNT = "loanRequestedAmount";
    public static final String LOAN_ADMIN_EXPENSES = "loanAdminExpenses";
    public static final String LOAN_MEDIUM = "loanMedium";
    public static final String LOAN_RATE = "loanRate";
    public static final String LOAN_FEE = "loanFee";
    public static final String LOAN_INTEREST_AMOUNT = "loanInterestAmount";
    public static final String LOAN_AMORT_AMOUNT = "loanAmortAmount";
    public static final String TOTAL_LOAN_AMOUNT = "totalLoanAmount";
    public static final String LOAN_BALANCE = "loanBalance";
    public static final String RESCUE_AMOUNT = "rescueAmount";
    public static final String OPEN_ITEM_ID_RESCUE = "openItemIdRescue";
    public static final String RESCUE_DATE = "rescueDate";
    public static final String LOAN_CURRENCY = "loanCurrency";
    public static final String PREMIUMS_OVERDUE = "premiumsOverDue";
    public static final String BILLING_COST = "billingCost";
    public static final String LOAN_INTEREST_DEDUCTION = "loanInterestDeduction";
    public static final String LOAN_AMOUNT_DEDUCTION = "loanAmountDeduction";
    public static final String RATE_RESCUE = "rateRescue";
    public static final String PAID_NET_PREMIUMS_AMOUNT_NOT_EARNED = "allPaidNetPremiumsAmountNotEarned";
    public static final String EMISSION_YEAR = "emissionYear";

    //Receipt constants
    public static final String RECEIPT_CASHIER_OPERATION_ID = "cashierOperationId";
    public static final String RECEIPT_NUMBER = "receiptNumber";
    public static final String RECEIPT_CODE = "receiptCode";
    public static final String RECEIPT_DOSAGE_ID = "receiptDosageId";
    public static final String RECEIPT_AMOUNT = "receiptAmount";
    public static final String RECEIPT_AMOUNTTEXT = "receiptAmountText";
    public static final String RECEIPT_DATE = "receiptDate";
    public static final String RECEIPT_DATE_HMS = "receiptDateHMS";
    public static final String RECEIPT_CURRENCY_DESCRIPTION = "receiptCurrencyDescription";
    public static final String RECEIPT_CURRENCY_ISOCODE = "receiptCurrencyIsoCode";
    public static final String RECEIPT_CASHIER_DESCRIPTION = "receiptCashierDescription";
    public static final String RECEIPT_DOSAGE_LIMIT_DATE = "receiptDosageLimitDate";
    public static final String RECEIPT_NIT_THIRD = "receiptNitThird";
    public static final String RECEIPT_BRANCH_LOCATION = "receiptBranchLocation";
    public static final String IS_BILL = "isBill"; //used in entries generation
    public static final String RECEIPT_NUMBER_GENERIC = "receiptNumberGeneric";
    public static final String RECEIPT_DOSAGE_ID_GENERIC = "receiptDosageIdGeneric";
    public static final String PARTIALRECOVERY = "PartialRecovery";
    public static final String RECEIPTTYPE = "receipttype";

    //CashReceipt constants
    public static final String CASH_RECEIPT_NUMBER = "cashReceiptNumber";
    public static final String CASH_RECEIPT_PAYER_NAME = "cashReceiptPayerName";
    public static final String CASH_RECEIPT_INSURED_NAME = "cashReceiptInsuredName";
    public static final String CASH_RECEIPT_INSURED_IDENTIFIER = "cashReceiptInsuredIdentifier";
    public static final String CASH_RECEIPT_PRODUCT_APS_BRANCH = "cashReceiptProductAPSBranch";
    public static final String CASH_RECEIPT_LIQUIDATION_NUMBER = "cashReceiptLiquidationNumber";
    public static final String CASH_RECEIPT_LIQUIDATION_QUOTA_NUMBER = "cashReceiptLiquidationQuotaNumber";
    public static final String CASH_RECEIPT_DEFAULT_CURRENCY_ISOCODE = "cashReceiptDefaultCurrencyIsoCode";
    public static final String CASH_RECEIPT_DEFAULT_CURRENCY_DESCRIPTION = "cashReceiptDefaultCurrencyDescription";
    public static final String CASH_RECEIPT_SECONDARY_CURRENCY_ISOCODE = "cashReceiptSecondaryCurrencyIsoCode";
    public static final String CASH_RECEIPT_SECONDARY_CURRENCY_DESCRIPTION = "cashReceiptSecondaryCurrencyDescription";
    public static final String CASH_RECEIPT_CURRENCY_RATE = "cashReceiptCurrencyRate";
    public static final String CASH_RECEIPT_DEFAULT_AMOUNT = "cashReceiptDefaultAmount";
    public static final String CASH_RECEIPT_SECONDARY_AMOUNT = "cashReceiptSecondaryAmount";
    public static final String CASH_RECEIPT_TOTAL_AMOUNT = "cashReceiptTotalAmount";
    public static final String CASH_RECEIPT_AMOUNT_TEXT = "cashReceiptAmountText";
    public static final String CASH_RECEIPT_CURRENCY = "cashReceiptCurrency";
    public static final String CASH_RECEIPT_BRANCH_LOCATION = "cashReceiptBranchLocation";
    public static final String CASH_RECEIPT_BRANCH_ADDRESS = "cashReceiptBranchAddress";
    public static final String CASH_RECEIPT_BRANCH_PHONE = "cashReceiptBranchPhone";
    public static final String CASH_RECEIPT_BRANCH_EMAIL = "cashReceiptBranchEmail";
    public static final String CASH_RECEIPT_BRANCH_DESCRIPTION = "cashReceiptBranchDescription";
    public static final String CASH_RECEIPT_LIQUIDATION_DUEDATE = "cashReceiptLiquidationDueDate";
    public static final String CASH_RECEIPT_DATE = "cashReceiptDate";
    public static final String CASH_RECEIPT_DATE_HMS = "cashReceiptDateHMS";
    public static final String CASH_RECEIPT_AMOUNT = "cashReceiptAmount";
    public static final String CASH_RECEIPT_PAYMENT_TYPE = "cashReceiptPaymentType";

    //Global Agency
    public static final String GLOBAL_AGENCY_DESCRIPTION = "globalAgencyDescription";
    public static final String GLOBAL_AGENCY_LOCATION = "globalAgencyLocation";
    public static final String GLOBAL_AGENCY_SYA_NUMBER = "globalAgencySyaNumber";
    public static final String GLOBAL_AGENCY_ADDRESS = "globalAgencyAddress";
    public static final String GLOBAL_AGENCY_PHONE = "globalAgencyPhone";
    public static final String GLOBAL_AGENCY_EMAIL = "globalAgencyEmail";
    public static final String GLOBAL_AGENCY_TYPE = "globalAgencyType";
    public static final String GLOBAL_AGENCY_PRIMARY_BRANCH_PARENT_ID = "globalAgencyPrimaryBranchParentId";

    //CoinsuranceConstants
    public static final String CoinsuranceType = "CoinsuranceType";
    public static final String CoinsuranceLeaderPerc = "CoinsuranceLeaderPerc";

    //Participation Utility
    public static final String PU_CURRENCY = "PUCurrency";
    public static final String PU_INITIAL_DATE = "PUInitialDate";
    public static final String PU_END_DATE = "PUEndDate";
    public static final String PU_PREMIUM_CEDED = "PUPremiumCeded";
    public static final String PU_CLAIM_CEDED = "PUClaimCeded";
    public static final String PU_CLAIM_RESERVE_PENDING_AFTER = "PUClaimReservePendingAfter";
    public static final String PU_CLAIM_RESERVE_PENDING_BEFORE = "PUClaimReservePendingBefore";
    public static final String PU_RESERVE_RISK_BEFORE = "PUReserveRiskBefore";
    public static final String PU_RESERVE_RISK_AFTER = "PUReserveRiskAfter";
    public static final String PU_COMMISSION_TAX = "PUCommissionTax";
    public static final String PU_EXPENSES = "PUExpenses";
    public static final String PU_CONTINGENCY = "PUContingency";
    public static final String PU_LOSSES_CONTINGENCY_PREVIOUS = "PULossesContingencyPrevious";
    public static final String PU_RESULT = "PUResult";
    public static final String PU_PARTICIPATION_UTILITY_RESULT = "PUtilityResult";

    //Claim Recovery
    public static final String RECOVERY_ID = "recoveryId";
    public static final String RECOVERY_DATE = "recoveryDate";
    public static final String RECOVERY_NUMBER = "recoveryNumber";
    public static final String RECOVERY_OBSERVATION = "recoveryObservation";
    public static final String RECOVERY_STATE = "recoveryState";
    public static final String RECOVERY_DCOID = "recoveryDcoId";
    public static final String RECOVERY_ROLE_ID = "recoveryRoleId";
    public static final String RECOVERY_COTTYPEID = "recoveryCotTypeId";
    public static final String RECOVERY_CURRENCY_ID = "recoveryCurrencyId";
    public static final String RECOVERY_THIRDPARTY_ID = "recoveryThirdPartyId";
    public static final String RECOVERY_AFFECTEDOBJECT_ID = "recoveryAffectedObjectId";
    public static final String RECOVERY_ROLE_DESCRIP = "recoveryRoleId";
    public static final String RECOVERY_CURRENCY_DESCRIP = "recoveryCurrencyId";
    public static final String RECOVERY_THIRDPARTY_DESCRIP = "recoveryThirdPartyId";
    public static final String RECOVERY_AFFECTEDOBJECT_DESCRIP = "recoveryAffectedObjectId";
    public static final String RECOVERY_AMOUNT = "recoveryAmount";

    //Detail Claim Recovery
    public static final String RECOVERYDETAIL_ID = "recoveryDetailId";
    public static final String RECOVERYDETAIL_CODE = "recoveryDetailCode";
    public static final String RECOVERYDETAIL_DESCRIP = "recoveryDetailDescription";
    public static final String RECOVERYDETAIL_QUANTITY = "recoveryDetailQuantity";
    public static final String RECOVERYDETAIL_PRICE = "recoveryDetailPrice";
    public static final String RECOVERYDETAIL_DCOID = "recoveryDetailDcoId";

    //Contract Detail
    public static final String REINSURED_GROUP = "ReinsuredGroup";
    public static final String REINSURED_CONTRACT_TYPE = "ContractType";
    public static final String REINSURED_PK = "ReinsuredPk";
    public static final String REINSURED_PERCENTAGE = "ReinsuredPercentage";
    public static final String REINSURED_CONTRACT_ID = "ContractId";
    public static final String REINSURED_CONTRACT_CURRENCY_ID = "ContractCurrency";

    // Facultative Contract Detail
    public static final String F_CONTRACT_ID = "fContractId";
    public static final String F_CONTRACT_INITIAL_DATE = "fContractInitialDate";
    public static final String F_CONTRACT_FINAL_DATE = "fContractFinalDate";
    public static final String F_CONTRACT_CAPACITY = "fContractCapacity";
    public static final String F_CONTRACT_TC_DATE = "fContractTcDate";
    public static final String F_CONTRACT_CURRENCY_ID = "fContractCurrency";
    public static final String F_CONTRACT_DIST_CURRENCY_TYPE = "fContractDistCurrencyType";
    public static final String F_CONTRACT_REINSURED_PK = "fContractReinsuredPk";
    public static final String F_CONTRACT_REINSURED_TYPE = "fContractReinsuredType";
    public static final String F_CONTRACT_REINSURED_GROUP_PK = "fContractReinsuredGroupPk";
    public static final String F_CONTRACT_REINSURED_GROUP_DESC = "fContractReinsuredGroupDesc";
    public static final String F_CONTRACT_REINSURED_GROUP_CODE = "fContractReinsuredGroupCode";
    public static final String F_CONTRACT_EXPENSES_DIST = "fContractExpensesDist";
    public static final String F_CONTRACT_USER = "fContractUser";
    public static final String F_AMOUNT_NOT_REINSURANCE = "fAmountNotReinsured";


    //Notification
    public static final String NOTIFICATION_USER = "notificationUser";
    public static final String NOTIFICATION_COVERAGEDESCRIP = "notificationCoverageDescription";
    public static final String NOTIFICATION_RESERVEAMOUNT = "notificationReserveAmount";
    public static final String NOTIFICATION_OCURRENCEDATE = "notificationOcurrenceDate";
    public static final String NOTIFICATION_PRODUCTNAME = "notificationProductName";
    public static final String NOTIFICATION_EMAIL = "notificationEmail";
    public static final String NOTIFICATION_VALIDATION_MESSAGE = "notificationValidationMessage";

    //Requeriments
    public static final String REQUIREMENT_DESC = "requirementDescrp";
    public static final String REQUIREMENT_MANDATORY = "requirementMand";
    public static final String UNRECEIVED_REQUIREMENT = "unreceivedRequirement";

    //Surcharges
    public static final String SURCHARGES_TYPE_DESC = "surchargesTypeDescription";
    public static final String SURCHARGES_TYPE_CODE = "surchargesTypeCode";
    public static final String SURCHARGES_TYPE_ISREQUIREDMODE = "surchargesTypeIsRequiredMode";
    public static final String SURCHARGES_TYPE_ISREQUIREDAMOUNT = "surchargesTypeIsRequiredAmount";
    public static final String SURCHARGES_TYPE_ISREQUIREDTIME = "surchargesTypeIsRequiredTime";
    public static final String SURCHARGESDCO_MODE = "surchargesDcoMode";
    public static final String SURCHARGESDCO_AMOUNT = "surchargesDcoAmount";
    public static final String SURCHARGESDCO_TIME = "surchargesDcoTime";
    public static final String SURCHARGES_REASON_DESC = "surchargesReasonDesc";
    public static final String SURCHARGES_REASON_CODE = "surchargesReasonCode";
    public static final String SURCHARGES_REASON_TEXT = "surchargesReasonText";
    public static final String SURCHARGESDCO_COVERAGE_TITLE_DESC = "surchargesDcoCoverageTitleDesc";
    public static final String EXIST_SURCHARGES = "existSurcharges";


    // Cashier Register
    public static final String CASH_REGISTER_NAME = "cashRegisterName";
    public static final String CASH_AMOUNT = "cashAmount";

    //Cashier closing
    public static final String CASHIER_CLOSING_AMOUNT = "cashierClosingAmount";

    //Clauses
    public static final String CLAUSES_DESC = "clausesDescrp";

    //Beneficiary Types
    public static final String BENEFICIARY_PRINCIPAL_DESC = "beneficiaryPrincipalDesc";
    public static final String BENEFICIARY_SECONDARY_DESC = "beneficiarySecondaryDesc";

    //Documented loans symbols
    public static final String DOC_LOAN_TOTAL_RESCUE = "RescueBalance";
    public static final String DOC_LOAN_ACTIVE_LOANS = "LoanBalance";

    //WildcardQuota
    public static final String QUOTA_OPTION = "quotaOption";
    public static final String QUOTA_IDENTIFYING_WILD_CARD = "coutaIdentifyingWildcard";
    public static final String QUOTA_SCHEDULED_WILDCARD = "quotaScheduledWildCard";
    public static final String PROGRAMMING_TYPE_WILDCARD_QUOTA = "programmingTypeWildCardQuota";
    public static final String DATE_WILDCARD_QUOTA_APPLIED = "dateWildcardQuotaApplied";
    public static final String STATE_QUOTA_WILDCARD = "stateQuotaWildcard";
    public static final String QUOTA_WILDCARD_ITERATOR = "iteratorOfWildCardQuota";

    //Property
    public static final String PROPERTY_NAME = "propertyName";
    public static final String PROPERTY_VALUE = "propertyValue";
    public static final String PROPERTY_LEVEL = "propertyLevel";
    public static final String PROPERTY_TEMPLATE = "propertyTemplate";

    public static final String RECIPIENT_PREFIX = "RECIPIENT";
    public static final String BENEFICIARY_PREFIX = "BENEFICIARY";
    public static final String REINSURER_PREFIX = "REINSURER";
    public static final String REINSURER_ROL_PREFIX = "REINSURER_ROL";
    public static final String DAMAGE_CLAIM_PREFIX = "DamageClaim";
    //CoverageInvoiceDetail
    public static final String COVERAGE_INVOICE_DETAIL_COV = "coverageInvoiceDetailCov";
    public static final String COVERAGE_INVOICE_DETAIL_COVQUANTITY = "coverageInvoiceDetailCovQuantity";
    public static final String COVERAGE_INVOICE_DETAIL_COVTAX = "coverageInvoiceDetailCovTax";
    public static final String COVERAGE_INVOICE_DETAIL_COVCONCEPT = "coverageInvoiceDetailCovConcept";
    public static final String COVERAGE_INVOICE_DETAIL_COVUNITPRICE = "coverageInvoiceDetailCovUnitPrice";
    public static final String COVERAGE_INVOICE_DETAIL_COVAMOUNT = "coverageInvoiceDetailCovAmount";
    public static final String COVERAGE_INVOICE_DETAIL_COVEXEMPT = "coverageInvoiceDetailCovExempt";

    //Primas en Deposito
    public static final String MINIMUM_PREMIUM_DEPOSIT = "minimumPremiumDeposit";
    public static final String MINIMUM_PREMIUM_DEPOSIT_DETAIL = "minimumPremiumDepositDetail";

    public static final String AMOUNT_LIQ = "amountLiq";
    public static final String TAX_AMOUNT = "taxAmount";
    public static final String INSURANCE_BRANCH = "InsuranceBranch";

    //Invoice Refund Claim
    public static final String INVOICE_REFUND_ID = "InvoiceRefundId";
    public static final String COMPANY = "COMPANY";
    public static final String GROUP_RECEIPT_BY = "groupReceiptBy";
    public static final String INSURANCE_OBJECT_ID = "InsuranceObjectId";
    public static final String COVERAGE_ID = "CoverageId";
    public static final String THIRDPARTY_RECEIVER_ID = "ThirdPartyReceiverId";
    public static final String THIRDPARTY_RECEIVER_NAME = "ThirdPartyReceiverName";
    public static final String THIRDPARTY_PROVIDER_ID = "ThirdPartyProviderId";
    public static final String THIRDPARTY_PROVIDER_NAME = "ThirdPartyProviderName";
    public static final String INVOICE_REFUND_DETAIL_ID = "InvoiceRefundDetailId";
    public static final String INVOICE_REFUND_TAX = "InvoiceRefundTax";


    /*Pre-liquidations*/
    public static final String PRELIQUIDATION_DOCUMENT_USERLOGIN="USERLOGIN";
    public static final String PRELIQUIDATION_DOCUMENT_CREATIONDATE="CREATIONDATE";
    public static final String PRELIQUIDATION_DOCUMENT_STATUS="STATUS";
    public static final String PRELIQUIDATION_DOCUMENT_MOVEMENT_DATE="PRELIQUIDATIONMOVEMENT_DATE";
    public static final String PRELIQUIDATION_DOCUMENT_MOVEMENT_ID="PRELIQUIDATIONMOVEMENT_ID";
    public static final String PRELIQUIDATION_DOCUMENT_GROSS_AMOUNT="GROSSAM";
    public static final String PRELIQUIDATION_DOCUMENT_AMOUNT="AMOUNT";
    public static final String PRELIQUIDATION_DOCUMENT_DEDUCTION="PRELIQUIDATIONDEDUCTION";

    // Function used in entries creation for premium reserves pending
    public static final String RPPC_CURRENCY = "CurrencyRPPC";
    public static final String RPPC_AMOUNT = "AmountRPPC";
    public static final String RPPC_BRANCHACCOUNT = "BranchRPPC";

    public static final String COMMISION_AMOUNT = "commissionAmount";
    public static final String PAYMENT_TYPE = "paymentType";

    //CRM
    public static final String CRMCase = "CRMCASE";
}