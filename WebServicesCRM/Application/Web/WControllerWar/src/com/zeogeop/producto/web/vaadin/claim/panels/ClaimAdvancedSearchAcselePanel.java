package com.consisint.acsele.web.vaadin.claim.panels;

import com.consisint.acsele.ClientInfo;
import com.consisint.acsele.UserInfo;
import com.consisint.acsele.claim.api.ClaimStatus;
import com.consisint.acsele.claim.bean.ClaimSearchParameters;
import com.consisint.acsele.claim.bean.ClaimSearchResult;
import com.consisint.acsele.claim.category.ClaimCollectionOrderByType;
import com.consisint.acsele.claim.category.EnumTypeClaimSearch;
import com.consisint.acsele.claim.category.EnumTypeResultViewClaimSearch;
import com.consisint.acsele.claim.engine.ClaimListImpl;
import com.consisint.acsele.claim.engine.search.SortClaim;
import com.consisint.acsele.claim.service.ClaimAdvancedSearchService;
import com.consisint.acsele.openapi.product.Product;
import com.consisint.acsele.openapi.product.ProductList;
import com.consisint.acsele.product.api.AgregInsObjectType;
import com.consisint.acsele.product.api.Plan;
import com.consisint.acsele.product.api.PlanList;
import com.consisint.acsele.product.claimcause.api.ClaimEvent;
import com.consisint.acsele.template.api.Template;
import com.consisint.acsele.template.api.TemplateList;
import com.consisint.acsele.template.api.TipoPresentador;
import com.consisint.acsele.template.api.Transformer;
import com.consisint.acsele.template.server.CotType;
import com.consisint.acsele.util.AcseleConf;
import com.consisint.acsele.util.BeanFactory;
import com.consisint.acsele.util.ListUtil;
import com.consisint.acsele.web.vaadin.AbstractAcselePanel;
import com.consisint.front.ui.PropertyComponent;
import com.consisint.front.ui.TemplateLayout;
import com.consisint.vaadin.services.claim.ClaimService;
import com.consisint.vaadin.util.StringUtil;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.tepi.filtertable.paged.PagedFilterTable;
import org.tepi.filtertable.paged.PagedTableChangeEvent;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Title: ClaimAdvancedSearchAcselePanel <br>
 * Copyright: (c) 2015 Consis International<br>
 * Company: Consis International<br>
 *
 * @author Consis International (CON)
 * @version Acsel-e v13.7
 *
 * Vaadin panel for claim advance search
 */

public class ClaimAdvancedSearchAcselePanel extends AbstractAcselePanel {
    private static ResourceBundle claimResourceBundle = ResourceBundle.getBundle("ClaimMessagesBundle", UserInfo.getLocale());
    private VerticalLayout layoutMain;
    private PagedFilterTable resultTable;
    private String action;
    private String isLinkClaimDisable;

    private int countLayoutTemplate;
    private Set<ClaimSearchResult> setClaimSearch;
    private EnumTypeClaimSearch enumTypeClaimSearch;
    private EnumTypeResultViewClaimSearch enumTypeResultViewClaimSearch;

    List<Product> listProducts;
    List<Template> listTemplatesThirdParty;
    List<Template> listTemplatesInsuranceObject;
    List<Template> listTemplatesRoles;


    Map<String,Product> mapProducts;
    Map<String,Template> mapTemplateThirdParties;
    Map<String,Template> mapTemplateInsuranceObjects;
    Map<String,Template> mapTemplateRoles;
    Map<String,ClaimEvent> mapClaimEvents;
    Map<String, List<AgregInsObjectType>> mapListInsuranceObjects;

    String currentProduct;
    Map<String,TemplateLayout> mapObjectTemplateLayout;

    private static final String ID_LAYOUT_TOP_MAIN = "layoutTopMain";
    private static final String ID_LAYOUT_LINE_BOTTOM = "layoutLineBottom";
    private static final String ID_LAYOUT_SIMPLE_SEARCH_CLAIM = "layoutSimpleSearchClaim";
    private static final String ID_LAYOUT_CLAIM = "layoutClaim";
    private static final String ID_LAYOUT_POLICY = "layoutPolicy";
    private static final String ID_LAYOUT_THIRDPARTY = "layoutThirdParty";
    private static final String ID_LAYOUT_RISK_UNIT = "layoutRiskUnit";
    private static final String ID_LAYOUT_INSURANCE_OBJECT = "layoutInsuranceObject";
    private static final String ID_LAYOUT_TABLE = "layoutTable";
    private static final String ID_LAYOUT_BUTTONS = "layoutButtons";
    private static final String ID_LAYOUT_CLAIM_TEMPLATE = "layoutClaimTemplate";
    private static final String ID_LAYOUT_POLICY_TEMPLATE = "layoutPolicyTemplate";
    private static final String ID_LAYOUT_THIRDPARTY_TEMPLATE = "layoutThirdPartyTemplate";
    private static final String ID_LAYOUT_ROL_TEMPLATE = "layoutRolTemplate";
    private static final String ID_LAYOUT_RISK_UNIT_TEMPLATE = "layoutRiskUnitTemplate";
    private static final String ID_LAYOUT_INSURANCE_OBJECT_TEMPLATE = "layoutInsuranceObjectTemplate";
    private static final String ID_LAYOUT_COMBO_PRODUCTO_ADVANCED_SEARCH = "layoutComboProductoAdvancedSearch";
    private static final String ID_LAYOUT_COMBO_EVENTO = "layoutComboEventoSiniestro";
    private static final String ID_LAYOUT_COMBO_THIRPARTY_TYPE = "layoutComboThirdPartyType";
    private static final String ID_LAYOUT_COMBO_ROL = "layoutComboRol";
    private static final String ID_LAYOUT_MORE_OPTIONS = "layoutMoreOptions";
    private static final String ID_LAYOUT_HIDE_OPTIONS = "layoutHideOptions";
    private static final String ID_LAYOUT_RESULT_TABLE = "layoutResultTable";
    private static final String ID_OBJECT_TEMPLATE_LAYOUT_CLAIM = "templateLayoutClaim";
    private static final String ID_OBJECT_TEMPLATE_LAYOUT_POLICY = "templateLayoutPolicy";
    private static final String ID_OBJECT_TEMPLATE_LAYOUT_THIRDPARTY = "templateLayoutThirdParty";
    private static final String ID_OBJECT_TEMPLATE_LAYOUT_ROL = "templateLayoutRol";
    private static final String ID_OBJECT_TEMPLATE_LAYOUT_INSURANCE_OBJECT = "templateLayoutInsuranceObject";
    private static final String ID_OBJECT_TEMPLATE_LAYOUT_RISK_UNIT = "templateLayoutRiskUnit";
    private static final String ID_LABEL_PRODUCTO_ADVANCED_SEARCH = "labelProductoAdvancedSearch";
    private static final String ID_LABEL_CRM_ADVANCED_SEARCH = "labelCRMAdvancedSearch";
    private static final String ID_LABEL_PRODUCTO_SIMPLE_SEARCH = "labelProductoSimpleSearch";
    private static final String ID_LABEL_EVENTO_SINIESTRO = "labelEventoSiniestro";
    private static final String ID_LABEL_THIRDPARTY_TYPE  = "labelThirdPartyType";
    private static final String ID_LABEL_ROL  = "labelRol";
    private static final String ID_LABEL_INSURANCE_OBJECT_TYPE = "labelInsuranceObjectType";
    private static final String ID_COMBO_THIRDPARTY_TYPE  = "comboThirdPartyType";
    private static final String ID_COMBO_PRODUCTO_ADVANCED_SEARCH = "comboProductoAdvancedSearch";
    private static final String ID_COMBO_PRODUCTO_SIMPLE_SEARCH = "comboProductoSimpleSearch";
    private static final String ID_COMBO_EVENTO_SINIESTRO = "comboEventoSiniestro";
    private static final String ID_COMBO_ROL = "comboRol";
    private static final String ID_COMBO_INSURANCE_OBJECT_TYPE = "comboInsuranceObjectType";
    private static final String ID_COMBO_ORDER_BY = "orderBy";
    private static final String ID_TEXTFIELD_CLAIM_NUMBER = "textFieldClaimNumbre";
    private static final String ID_DATEFIELD_OCCURRENCE_DATE = "dateFieldOccurrenceDate";
    private static final String ID_TEXTFIELD_POLICY_NUMBER = "textFieldPolicyNumber";
    private static final String ID_TEXTFIELD_CRM_NUMBER = "textFieldCRMNumber";
    private static final String ID_LABEL_CRM_SIMPLE_SEARCH = "labelCRMNumberSimpleSearch";

    private static final String STYLE_HEADER_BLUE = "header_blue";
    private static final String STYLE_LINK_BLUE = "link_blue";
    private static final String STYLE_BUTTON_BLUE = "button_blue";

    private static final String STANDARD_WIDTH = "175px";


    public ClaimAdvancedSearchAcselePanel(String action, String isLinkClaimDisable) {
        super();
        setWidth("40%");
        this.action = action;
        this.isLinkClaimDisable = isLinkClaimDisable;
        initTypeClaimSearch(action);
        initObjects();
        buildLayoutMain();
        setContent(layoutMain);
        setCaption(resourceBundle.getString("claimSearch"));
    }

    private void initTypeClaimSearch(String action) {
        if(action != null && action.equals("ClaimDetailFront")) {
            this.enumTypeClaimSearch = EnumTypeClaimSearch.SIMPLE_SEARCH;
            this.enumTypeResultViewClaimSearch = EnumTypeResultViewClaimSearch.CLAIM_DETAIL_FRONT;
        }
        else if(action != null && action.equals("ClaimDetailJsp")) {
            this.enumTypeClaimSearch = EnumTypeClaimSearch.SIMPLE_SEARCH;
            this.enumTypeResultViewClaimSearch = EnumTypeResultViewClaimSearch.CLAIM_DETAIL_JSP;
        }
        else {
            this.enumTypeClaimSearch = EnumTypeClaimSearch.SIMPLE_SEARCH;
            this.enumTypeResultViewClaimSearch = EnumTypeResultViewClaimSearch.CLAIM_DETAIL_JSP;
        }
    }

    private void initObjects() {
        mapProducts = new HashMap<String,Product>();
        mapTemplateThirdParties = new HashMap<String,Template>();
        mapTemplateInsuranceObjects = new HashMap<String,Template>();
        mapTemplateRoles = new HashMap<String,Template>();
        mapClaimEvents = new HashMap<String,ClaimEvent>();

        mapObjectTemplateLayout = new HashMap<String,TemplateLayout>();

        listProducts = new ArrayList<Product>(ProductList.Impl.getProductsActives().getAll());
        Collections.sort(listProducts, new Comparator<Product>() {
            public int compare(Product o1, Product o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        for(Product product: listProducts) {
            mapProducts.put(product.getName(), product);
        }

        listTemplatesThirdParty = TemplateList.Impl.getByCategory(CotType.THIRDPARTY).getAll();
        Collections.sort(listTemplatesThirdParty, new Comparator<Template>() {
            @Override
            public int compare(Template o1, Template o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        for(Template template: listTemplatesThirdParty) {
            mapTemplateThirdParties.put(template.getName(), template);
        }

        listTemplatesInsuranceObject = TemplateList.Impl.getByCategory(CotType.INSURANCE_OBJECT).getAll();
        Collections.sort(listTemplatesInsuranceObject, new Comparator<Template>() {
            @Override
            public int compare(Template o1, Template o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        for(Template template: listTemplatesInsuranceObject) {
            mapTemplateInsuranceObjects.put(template.getName(), template);
        }

        listTemplatesRoles = TemplateList.Impl.getByCategory(CotType.ROLE).getAll();
        Collections.sort(listTemplatesRoles, new Comparator<Template>() {
            @Override
            public int compare(Template o1, Template o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        for(Template template: listTemplatesRoles) {
            mapTemplateRoles.put(template.getName(), template);
        }

        mapListInsuranceObjects = new HashMap<String, List<AgregInsObjectType>>();
        ProductList listProduct = ProductList.Impl.getProductsActives();
        for (Product product : listProduct) {
            PlanList planes = product.listPlans(true);
            List<AgregInsObjectType> newAgregInsObjectTypeList = new ArrayList<AgregInsObjectType>();
            for (Plan plan : planes) {
                boolean existe = false;
                for (AgregInsObjectType obj : plan.getAgregInsObjectTypeList().getAll()) {
                    for (int a = 0; a < newAgregInsObjectTypeList.size(); a++)
                    {
                        if (newAgregInsObjectTypeList.get(a).getDesc().equals(obj.getDesc()))
                        {
                            existe = true;
                            break;
                        }
                    }
                    if (!existe)
                        newAgregInsObjectTypeList.add(obj);
                }
            }
            mapListInsuranceObjects.put(product.getName(), newAgregInsObjectTypeList);
        }
    }

    private void buildLayoutMain(){
        layoutMain = createVerticalLayout("layoutMain", "100%", null, null);
        createLayoutTopMain();
        createLayoutLineBottom();
        createLayoutSimpleSearchClaim();
        createLayoutClaim();
        createLayoutPolicy();
        createLayoutThirdParty();
        createLayoutShowMoreOptions();
        createLayoutRiskUnit();
        createLayoutInsuranceObject();
        createLayoutHideOptionsOfSearch();
        createLayoutButtons();
        createLayoutTable();
        repaintAllLayout(true);
    }

    private void createLayoutTopMain() {
        HorizontalLayout layoutTopMain = createHorizontalLayout(ID_LAYOUT_TOP_MAIN,"100%","subpanel-top",new MarginInfo(true, true, false, true));

        //Agregando el layoutTemplateTop al layoutMain
        layoutMain.addComponent(layoutTopMain);
        layoutMain.setComponentAlignment(layoutTopMain, Alignment.MIDDLE_CENTER);

        if(enumTypeClaimSearch == EnumTypeClaimSearch.ADVANCED_SEARCH) {
            repaintLayoutTopMainAdvancedSearch();
        }
        else {
            repaintLayoutTopMainSimpleSearch();
        }
    }

    private void repaintLayoutTopMainAdvancedSearch() {
        Label labelAdvancedSearch = createLabel("labelAdvancedSearch", null, resourceBundle.getString("AdvanceSearch"), "titulo");

        Button buttonSimpleSearch = createButton("buttonSimpleSearch", resourceBundle.getString("SimpleSearch"), STYLE_LINK_BLUE);
        buttonSimpleSearch.addStyleName(BaseTheme.BUTTON_LINK);
        buttonSimpleSearch.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                mapHorizontalLayout.get(ID_LAYOUT_TOP_MAIN).removeAllComponents();
                enumTypeClaimSearch = EnumTypeClaimSearch.SIMPLE_SEARCH;
                repaintLayoutTopMainSimpleSearch();
                repaintAllLayout(true);
            }
        });

        Image imageAdvancedSearch = createImage("imageBack", "img/back_icon.png");

        repaintLayoutTopMainSearch(labelAdvancedSearch, buttonSimpleSearch, imageAdvancedSearch);
    }

    private void repaintLayoutTopMainSimpleSearch() {
        Label labelSimpleSearch = createLabel("labelSimpleSearch", null, resourceBundle.getString("SimpleSearch"), "titulo");

        Button buttonAdvancedSearch = createButton("buttonAdvancedSearch", resourceBundle.getString("AdvanceSearch"), STYLE_LINK_BLUE);
        buttonAdvancedSearch.addStyleName(BaseTheme.BUTTON_LINK);
        buttonAdvancedSearch.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                mapHorizontalLayout.get(ID_LAYOUT_TOP_MAIN).removeAllComponents();
                enumTypeClaimSearch = EnumTypeClaimSearch.ADVANCED_SEARCH;
                repaintLayoutTopMainAdvancedSearch();
                repaintAllLayout(true);
            }
        });

        Image imageSimpleSearch = createImage("imageBack", "img/icon_advanced_search.png");

        repaintLayoutTopMainSearch(labelSimpleSearch, buttonAdvancedSearch, imageSimpleSearch);
    }

    private void repaintLayoutTopMainSearch(Label labelSearch, Button buttonSearch, Image imageSearch) {
        //Creando los componentes del layoutSearchLeft
        Label labelIntroduzca = createLabel("labelIntroduzca", null, resourceBundle.getString("EnterData"), "subTitulo");

        VerticalLayout layoutSearchLeft = createVerticalLayout("layoutSearchLeft","100%", null,null);
        layoutSearchLeft.addComponent(labelSearch);
        layoutSearchLeft.setComponentAlignment(labelSearch, Alignment.TOP_LEFT);
        layoutSearchLeft.addComponent(labelIntroduzca);
        layoutSearchLeft.setComponentAlignment(labelIntroduzca, Alignment.BOTTOM_LEFT);

        //Creando los componentes del layoutComboOrdenar
        Label labelOrdenarPor = createLabel("labelOrdenarPor", null, resourceBundle.getString("OrderBy"),STYLE_HEADER_BLUE);
        ComboBox comboOrdenarPor = createCombo(ID_COMBO_ORDER_BY, STANDARD_WIDTH);
        HorizontalLayout layoutOrdenarPor = createHorizontalLayout("layoutOrdenarPor","80%",null,null);
        layoutOrdenarPor.addComponent(labelOrdenarPor);
        layoutOrdenarPor.addComponent(comboOrdenarPor);
        layoutOrdenarPor.setComponentAlignment(labelOrdenarPor,Alignment.TOP_LEFT);
        layoutOrdenarPor.setComponentAlignment(comboOrdenarPor,Alignment.TOP_LEFT);

        //Creando los componentes del layoutSearchRight
        Image imageFlecha = createImage("imageFlecha", "img/arrow1.png");

        HorizontalLayout layoutSearchRight = createHorizontalLayout("layoutSearchRight","65%", null,null);
        layoutSearchRight.addComponent(imageSearch);
        layoutSearchRight.setComponentAlignment(imageSearch,Alignment.TOP_RIGHT);
        layoutSearchRight.addComponent(buttonSearch);
        layoutSearchRight.setComponentAlignment(buttonSearch,Alignment.TOP_RIGHT);
        layoutSearchRight.addComponent(imageFlecha);
        layoutSearchRight.setComponentAlignment(imageFlecha,Alignment.TOP_RIGHT);

        //Agregando los componentes creados al layoutTemplateTop
        HorizontalLayout layoutTopMain = mapHorizontalLayout.get(ID_LAYOUT_TOP_MAIN);
        layoutTopMain.addComponent(layoutSearchLeft);
        layoutTopMain.addComponent(layoutOrdenarPor);
        layoutTopMain.addComponent(layoutSearchRight);

        layoutTopMain.setComponentAlignment(layoutSearchLeft, Alignment.MIDDLE_LEFT);
        layoutTopMain.setComponentAlignment(layoutOrdenarPor, Alignment.TOP_CENTER);
        layoutTopMain.setComponentAlignment(layoutSearchRight, Alignment.TOP_RIGHT);
    }

    private void createLayoutLineBottom() {
        //Agregando la linea separadora
        HorizontalLayout layoutLineBottom = createHorizontalLayout(ID_LAYOUT_LINE_BOTTOM,"100%", "subpanel-bottom", null);
        layoutLineBottom.setHeight("10px");
        layoutMain.addComponent(layoutLineBottom);
    }

    private void createLayoutSimpleSearchClaim() {
        Label labelClaimNumber = createLabel("labelClaimNumber", null, claimResourceBundle.getString("claim.claimNumber"), null);
        TextField textFieldClaimNumber = createTextField(ID_TEXTFIELD_CLAIM_NUMBER,STANDARD_WIDTH, null);

        Label labelOcurrenceDate = createLabel("labelOcurrenceDate", null, claimResourceBundle.getString("l_claims.ocurrencedate"), null);
        DateField dateFieldOccurrenceDate = createDateField(ID_DATEFIELD_OCCURRENCE_DATE,STANDARD_WIDTH,null);

        Label labelPolicyNumber = createLabel("labelPolicyNumber", null, resourceBundle.getString("NumberPolicy"), null);
        TextField textFieldPolicyNumber = createTextField(ID_TEXTFIELD_POLICY_NUMBER,STANDARD_WIDTH, null);

        Label labelCRMNumber = createLabel(ID_LABEL_CRM_SIMPLE_SEARCH, null, resourceBundle.getString("CRMNumberLabel"),null);
        TextField textFieldCRMNumber = createTextField(ID_TEXTFIELD_CRM_NUMBER,STANDARD_WIDTH, null);

        Label labelProducto = createLabel(ID_LABEL_PRODUCTO_SIMPLE_SEARCH, null, resourceBundle.getString("Product"),null);
        ComboBox comboProducto = createCombo(ID_COMBO_PRODUCTO_SIMPLE_SEARCH,STANDARD_WIDTH);

        GridLayout layoutFormSimpleSearch = createGridLayout("layoutFormSimpleSearch", "80%",null,new MarginInfo(true, true, false, false),3,5);

        layoutFormSimpleSearch.addComponent(labelClaimNumber);
        layoutFormSimpleSearch.addComponent(textFieldClaimNumber);
        layoutFormSimpleSearch.addComponent(createLayoutSpace("50px","30px"));
        layoutFormSimpleSearch.addComponent(labelOcurrenceDate);
        layoutFormSimpleSearch.addComponent(dateFieldOccurrenceDate);

        layoutFormSimpleSearch.addComponent(labelPolicyNumber);
        layoutFormSimpleSearch.addComponent(textFieldPolicyNumber);
        layoutFormSimpleSearch.addComponent(createLayoutSpace("50px","30px"));
        layoutFormSimpleSearch.addComponent(labelProducto);
        layoutFormSimpleSearch.addComponent(comboProducto);
        if(ClientInfo.isClientRunning("Interseguro")){
            layoutFormSimpleSearch.addComponent(labelCRMNumber);
            layoutFormSimpleSearch.addComponent(textFieldCRMNumber);
        }

        //Agregando al layout sus componentes
        VerticalLayout layoutSimpleSearchClaim = createVerticalLayout(ID_LAYOUT_SIMPLE_SEARCH_CLAIM, "100%", null, new MarginInfo(false, true, true, true));
        layoutSimpleSearchClaim.addComponent(layoutFormSimpleSearch);
        layoutSimpleSearchClaim.setComponentAlignment(layoutFormSimpleSearch, Alignment.MIDDLE_CENTER);

        layoutMain.addComponent(layoutSimpleSearchClaim);
        layoutMain.setComponentAlignment(layoutSimpleSearchClaim, Alignment.MIDDLE_CENTER);
    }

    private void createLayoutClaim() {
        Label labelProducto = createLabel(ID_LABEL_PRODUCTO_ADVANCED_SEARCH, null, resourceBundle.getString("Product"),null);
        ComboBox comboProducto = createCombo(ID_COMBO_PRODUCTO_ADVANCED_SEARCH,STANDARD_WIDTH);
        HorizontalLayout layoutComboProducto = createHorizontalLayout(ID_LAYOUT_COMBO_PRODUCTO_ADVANCED_SEARCH, "100%", null, new MarginInfo(true, false, false, false));
        layoutComboProducto.addComponent(labelProducto);
        layoutComboProducto.addComponent(comboProducto);
        layoutComboProducto.setExpandRatio(labelProducto, 1);
        layoutComboProducto.setExpandRatio(comboProducto, 5);
        layoutComboProducto.setComponentAlignment(labelProducto, Alignment.MIDDLE_LEFT);
        layoutComboProducto.setComponentAlignment(comboProducto, Alignment.MIDDLE_LEFT);

        Label labelEventoSiniestro = createLabel(ID_LABEL_EVENTO_SINIESTRO, null, resourceBundle.getString("claimEvent"),null);
        ComboBox comboEventoSiniestro = createCombo(ID_COMBO_EVENTO_SINIESTRO,STANDARD_WIDTH);
        HorizontalLayout layoutComboEventoSiniestro = createHorizontalLayout(ID_LAYOUT_COMBO_EVENTO, "100%", null, null);
        layoutComboEventoSiniestro.addComponent(labelEventoSiniestro);
        layoutComboEventoSiniestro.addComponent(comboEventoSiniestro);
        layoutComboEventoSiniestro.setExpandRatio(labelEventoSiniestro, 1);
        layoutComboEventoSiniestro.setExpandRatio(comboEventoSiniestro, 5);
        layoutComboEventoSiniestro.setComponentAlignment(labelEventoSiniestro, Alignment.MIDDLE_LEFT);
        layoutComboEventoSiniestro.setComponentAlignment(comboEventoSiniestro, Alignment.MIDDLE_LEFT);

        HorizontalLayout layoutClaimTemplate = createHorizontalLayout(ID_LAYOUT_CLAIM_TEMPLATE, "100%", null, null);

        //Agregando al layout sus componentes
        VerticalLayout layoutClaim = createLayoutContainer(ID_LAYOUT_CLAIM, resourceBundle.getString("claimInformation"), "75%");
        layoutClaim.addComponent(layoutComboProducto);
        layoutClaim.addComponent(layoutComboEventoSiniestro);
        layoutClaim.addComponent(layoutClaimTemplate);
        layoutClaim.setComponentAlignment(layoutComboProducto, Alignment.MIDDLE_LEFT);
        layoutClaim.setComponentAlignment(layoutComboEventoSiniestro, Alignment.MIDDLE_LEFT);
        layoutClaim.setComponentAlignment(layoutClaimTemplate, Alignment.MIDDLE_LEFT);

    }

    private void createLayoutPolicy() {
        HorizontalLayout layoutPolicyTemplate = createHorizontalLayout(ID_LAYOUT_POLICY_TEMPLATE, "100%", null, new MarginInfo(true, false, false, false));

        Template defaultTemplatePolicy = Template.Impl.getDefaultTemplate(CotType.POLICY);
        reloadLayoutTemplate(ID_LAYOUT_POLICY_TEMPLATE, defaultTemplatePolicy, ID_OBJECT_TEMPLATE_LAYOUT_POLICY);

        VerticalLayout layoutPolicy = createLayoutContainer(ID_LAYOUT_POLICY, resourceBundle.getString("frontEnd.policyInfo"), "70%");
        layoutPolicy.addComponent(layoutPolicyTemplate);
        layoutPolicy.setComponentAlignment(layoutPolicyTemplate, Alignment.MIDDLE_LEFT);
    }

    private void createLayoutThirdParty() {
        Label labelThirdPartyType = createLabel(ID_LABEL_THIRDPARTY_TYPE, null, resourceBundle.getString("frontEnd.tpType"),null);
        ComboBox comboThirPartyType = createCombo(ID_COMBO_THIRDPARTY_TYPE,STANDARD_WIDTH);

        HorizontalLayout layoutThirdPartyTemplate = createHorizontalLayout(ID_LAYOUT_THIRDPARTY_TEMPLATE, "100%", null, new MarginInfo(true, false, false, false));

        Template defaultTemplateThirdParty = Template.Impl.getDefaultTemplate(CotType.THIRDPARTY);
        reloadLayoutTemplate(ID_LAYOUT_THIRDPARTY_TEMPLATE, defaultTemplateThirdParty, ID_OBJECT_TEMPLATE_LAYOUT_THIRDPARTY);
        repaintComboThirdPartyType();

        HorizontalLayout layoutBorderTop = createHorizontalLayout("layoutBorderTop","100%","top-line",null);

        Label labelRol = createLabel(ID_LABEL_ROL, null, resourceBundle.getString("SearchRole"), null);
        ComboBox comboRol = createCombo(ID_COMBO_ROL,STANDARD_WIDTH);
        HorizontalLayout layoutComboRol = createHorizontalLayout(ID_LAYOUT_COMBO_ROL,"100%",null,null);
        layoutComboRol.addComponent(labelRol);
        layoutComboRol.addComponent(comboRol);
        layoutComboRol.setExpandRatio(labelRol, 1);
        layoutComboRol.setExpandRatio(comboRol, 7);
        HorizontalLayout layoutRolTemplate = createHorizontalLayout(ID_LAYOUT_ROL_TEMPLATE,"100%",null,null);

        VerticalLayout layoutThirdParty = createLayoutContainer(ID_LAYOUT_THIRDPARTY, resourceBundle.getString("frontEnd.tpInfo"), "75%");
        layoutThirdParty.addComponent(layoutThirdPartyTemplate);
        layoutThirdParty.addComponent(layoutBorderTop);
        layoutThirdParty.addComponent(layoutComboRol);
        layoutThirdParty.addComponent(layoutRolTemplate);
        layoutThirdParty.setComponentAlignment(layoutThirdPartyTemplate, Alignment.MIDDLE_LEFT);
        layoutThirdParty.setComponentAlignment(layoutBorderTop, Alignment.TOP_CENTER);
        layoutThirdParty.setComponentAlignment(layoutComboRol, Alignment.TOP_CENTER);
        layoutThirdParty.setComponentAlignment(layoutRolTemplate, Alignment.TOP_CENTER);
    }

    private void createLayoutRiskUnit() {
        HorizontalLayout layoutRiskUnitTemplate = createHorizontalLayout(ID_LAYOUT_RISK_UNIT_TEMPLATE, "100%", null, new MarginInfo(true, false, false, false));

        Template defaultTemplateRiskUnit = Template.Impl.getDefaultTemplate(CotType.RISK_UNIT);
        reloadLayoutTemplate(ID_LAYOUT_RISK_UNIT_TEMPLATE, defaultTemplateRiskUnit, ID_OBJECT_TEMPLATE_LAYOUT_RISK_UNIT);

        VerticalLayout layoutRiskUnit = createLayoutContainer(ID_LAYOUT_RISK_UNIT, resourceBundle.getString("SearchRUInformation"), "100%");
        layoutRiskUnit.addComponent(layoutRiskUnitTemplate);
        layoutRiskUnit.setComponentAlignment(layoutRiskUnitTemplate, Alignment.MIDDLE_LEFT);
        layoutRiskUnit.setVisible(false);
    }

    private void createLayoutInsuranceObject() {
        Label labelInsuranceObjectType = createLabel(ID_LABEL_INSURANCE_OBJECT_TYPE, null, resourceBundle.getString("IOType"),null);
        ComboBox comboInsuranceObjectType = createCombo(ID_COMBO_INSURANCE_OBJECT_TYPE,STANDARD_WIDTH);

        HorizontalLayout layoutInsuranceObjectTemplate = createHorizontalLayout(ID_LAYOUT_INSURANCE_OBJECT_TEMPLATE, "100%", null, new MarginInfo(true, false, false, false));

        Template defaultTemplateThirdParty = Template.Impl.getDefaultTemplate(CotType.INSURANCE_OBJECT);
        reloadLayoutTemplate(ID_LAYOUT_INSURANCE_OBJECT_TEMPLATE, defaultTemplateThirdParty, ID_OBJECT_TEMPLATE_LAYOUT_INSURANCE_OBJECT);
        repaintComboInsuranceObjectType();

        VerticalLayout layoutInsuranceObject = createLayoutContainer(ID_LAYOUT_INSURANCE_OBJECT, resourceBundle.getString("SearchIOInformation"), "100%");
        layoutInsuranceObject.addComponent(layoutInsuranceObjectTemplate);
        layoutInsuranceObject.setComponentAlignment(layoutInsuranceObjectTemplate, Alignment.MIDDLE_LEFT);
        layoutInsuranceObject.setVisible(false);
    }

    private void createLayoutShowMoreOptions() {
        Image imageMoreOptions = createImage("imageMoreOptions", "img/more_option_icon.png");
        Button buttonMoreOptions = createButton("buttonMoreOptions", resourceBundle.getString("SearchShowOptions"), STYLE_LINK_BLUE);
        buttonMoreOptions.addStyleName(BaseTheme.BUTTON_LINK);

        buttonMoreOptions.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                VerticalLayout layoutRiskUnit = mapVerticalLayout.get(ID_LAYOUT_RISK_UNIT);
                VerticalLayout layoutInsuranceObject = mapVerticalLayout.get(ID_LAYOUT_INSURANCE_OBJECT);
                HorizontalLayout layoutMoreOptions = mapHorizontalLayout.get(ID_LAYOUT_MORE_OPTIONS);
                HorizontalLayout layoutHideOptions = mapHorizontalLayout.get(ID_LAYOUT_HIDE_OPTIONS);
                layoutRiskUnit.setVisible(true);
                layoutInsuranceObject.setVisible(true);
                layoutMoreOptions.setVisible(false);
                layoutHideOptions.setVisible(true);
            }
        });

        HorizontalLayout layoutMoreOptions = createHorizontalLayout(ID_LAYOUT_MORE_OPTIONS,"45%",null,null);
        layoutMoreOptions.addComponent(imageMoreOptions);
        layoutMoreOptions.addComponent(buttonMoreOptions);
        layoutMoreOptions.setComponentAlignment(imageMoreOptions,Alignment.TOP_RIGHT);
        layoutMoreOptions.setComponentAlignment(buttonMoreOptions,Alignment.MIDDLE_LEFT);
        layoutMain.addComponent(layoutMoreOptions);
        layoutMain.setComponentAlignment(layoutMoreOptions, Alignment.TOP_RIGHT);
    }

    private void createLayoutHideOptionsOfSearch() {
        Image imageHideOptions = createImage("imageHideOptions", "img/less_option_icon.png");
        Button buttonHideOptions = createButton("buttonHideOptions", resourceBundle.getString("SearchHideOptions"), STYLE_LINK_BLUE);
        buttonHideOptions.addStyleName(BaseTheme.BUTTON_LINK);

        buttonHideOptions.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                VerticalLayout layoutRiskUnit = mapVerticalLayout.get(ID_LAYOUT_RISK_UNIT);
                VerticalLayout layoutInsuranceObject = mapVerticalLayout.get(ID_LAYOUT_INSURANCE_OBJECT);
                HorizontalLayout layoutMoreOptions = mapHorizontalLayout.get(ID_LAYOUT_MORE_OPTIONS);
                HorizontalLayout layoutHideOptions = mapHorizontalLayout.get(ID_LAYOUT_HIDE_OPTIONS);
                layoutRiskUnit.setVisible(false);
                layoutInsuranceObject.setVisible(false);
                layoutHideOptions.setVisible(false);
                layoutMoreOptions.setVisible(true);
            }
        });

        HorizontalLayout layoutHideOptions = createHorizontalLayout(ID_LAYOUT_HIDE_OPTIONS,"40%",null,null);
        layoutHideOptions.addComponent(imageHideOptions);
        layoutHideOptions.addComponent(buttonHideOptions);
        layoutHideOptions.setComponentAlignment(imageHideOptions,Alignment.TOP_RIGHT);
        layoutHideOptions.setComponentAlignment(buttonHideOptions,Alignment.MIDDLE_LEFT);
        layoutMain.addComponent(layoutHideOptions);
        layoutMain.setComponentAlignment(layoutHideOptions, Alignment.TOP_RIGHT);
        layoutHideOptions.setVisible(false);

    }

    private void createLayoutButtons() {
        //Creando los componentes del layout
        Button buttonBuscar = createButton("buttonBuscar", resourceBundle.getString("Search"), null);
        buttonBuscar.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ClaimSearchParameters searchParam = getSearchParam();

                if (searchParam == null) {
                    showSystemMessage(resourceBundle.getString("ErrorMessage"), resourceBundle.getString("DialogErrorSimpleSearch"));
                }
                else {
                    setClaimSearch = ((ClaimAdvancedSearchService) BeanFactory.getBean(ClaimAdvancedSearchService.class)).search(searchParam);
                    if(setClaimSearch.isEmpty()) {
                        showSystemMessage(resourceBundle.getString("message.table"), resourceBundle.getString("DialogDontFoundPoliciesSimpleSearch"));
                    }
                    else {
                        repaintResultTable(setClaimSearch);
                    }
                }
            }
        });

        Button buttonRestore = createButton("buttonRestore", resourceBundle.getString("reset"), STYLE_BUTTON_BLUE);
        buttonRestore.addStyleName(BaseTheme.BUTTON_LINK);

        buttonRestore.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                getUI().getPage().setLocation("/WController/vaadinservlet?view=claimAdvancedSearch&action=" + action);
            }
        });

        //Agregando al layout sus componentes
        HorizontalLayout layoutButtons = createHorizontalLayout(ID_LAYOUT_BUTTONS, "100%", null, new MarginInfo(false, false, true, false));
        layoutButtons.addComponent(buttonRestore);
        layoutButtons.addComponent(buttonBuscar);
        layoutButtons.setComponentAlignment(buttonRestore, Alignment.MIDDLE_RIGHT);
        layoutButtons.setComponentAlignment(buttonBuscar, Alignment.MIDDLE_LEFT);

        //Agregando el layoutButtons al layoutMain
        layoutMain.addComponent(layoutButtons);
        layoutMain.setComponentAlignment(layoutButtons, Alignment.MIDDLE_CENTER);

    }

    private void createLayoutTable() {

        Button buttonBackSearch = createButton("buttonBackSearch", resourceBundle.getString("BackSearch"), STYLE_LINK_BLUE);
        buttonBackSearch.addStyleName(BaseTheme.BUTTON_LINK);

        buttonBackSearch.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                repaintAllLayout(true);
            }
        });

        HorizontalLayout layoutButtonBackSearch = createHorizontalLayout("layoutButtonBackSearch", "90%", null,null);
        layoutButtonBackSearch.addComponent(buttonBackSearch);
        layoutButtonBackSearch.setComponentAlignment(buttonBackSearch,Alignment.MIDDLE_RIGHT);

        VerticalLayout layoutResultTable = this.createVerticalLayout(ID_LAYOUT_RESULT_TABLE, "100%", null, null);

        Button buttonOk = createButton("buttonOk", resourceBundle.getString("okbutton"), null);

        buttonOk.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if(resultTable.getValue() != null) {
                    ClaimSearchResult claimSearch = (ClaimSearchResult) resultTable.getValue();
                    if(enumTypeResultViewClaimSearch == EnumTypeResultViewClaimSearch.CLAIM_DETAIL_JSP) {
                        getUI().getPage().open("/WController/claimInterfaces/toModule.do?prefix=/claimInterfaces&way=vaadin&claimId=" + claimSearch.getClaimId() + "&page=/loadClaim.do" + "&isLinkClaimDisable=" + isLinkClaimDisable, "_blank");
                    }
                    else {
                        getUI().getPage().open("/WController/front?view=CLAIMDETAIL&claimId="
                                + claimSearch.getClaimId() + "&policyNumber="  + claimSearch.getPolicyNumber() + "&productName="  + claimSearch.getProductName()  + "&confidentialityMessage="  + getSession().getAttribute("confidentialityMessage"), "_blank");
                    }
                }
                else {
                    showSystemMessage(resourceBundle.getString("ErrorMessage"), resourceBundle.getString("selectedWarranty"));
                }
            }
        });

        Button buttonExport = createButton("buttonExport", resourceBundle.getString("Export"), null);

        buttonExport.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                ClaimService claimService = ClaimService.getInstance();

                Set<SortClaim> setSortClaim = new HashSet<SortClaim>();

                for(ClaimSearchResult claimSearch: setClaimSearch) {
                    SortClaim sortClaim = new SortClaim(claimSearch.getClaimId(), claimSearch.getClaimNumber(), claimSearch.getPolicyNumber(), claimSearch.getProductName(), claimSearch.getPolicyDate());
                    setSortClaim.add(sortClaim);
                }

                String fileName = claimService.exportClaims(new ClaimListImpl(setSortClaim));

                Link linkExcel = createLink("linkExcel", fileName, fileName, STYLE_LINK_BLUE);
                FileResource resource = new FileResource(new File(fileName));
                linkExcel.setResource(resource);
                showDownloadWindow(resourceBundle.getString("download"), resourceBundle.getString("SuccessfullExport"), linkExcel);
            }
        });

        HorizontalLayout layoutTableButtons = createHorizontalLayout("layoutTableButtons", "100%", null, new MarginInfo(false, false, true, false));
        layoutTableButtons.addComponent(buttonOk);
        layoutTableButtons.addComponent(buttonExport);
        layoutTableButtons.setComponentAlignment(buttonOk,Alignment.MIDDLE_RIGHT);
        layoutTableButtons.setComponentAlignment(buttonExport,Alignment.MIDDLE_LEFT);

        VerticalLayout layoutTable = createVerticalLayout(ID_LAYOUT_TABLE, "100%", null, new MarginInfo(true, false, false, false));
        layoutTable.addComponent(layoutButtonBackSearch);
        layoutTable.addComponent(layoutResultTable);
        layoutTable.addComponent(layoutTableButtons);
        layoutTable.setComponentAlignment(layoutButtonBackSearch, Alignment.MIDDLE_CENTER);
        layoutTable.setComponentAlignment(layoutResultTable, Alignment.MIDDLE_CENTER);
        layoutTable.setComponentAlignment(layoutTableButtons, Alignment.MIDDLE_CENTER);
        layoutTable.setVisible(false);

        //Agregando el layoutResultTable al layoutMain
        layoutMain.addComponent(layoutTable);
        layoutMain.setComponentAlignment(layoutTable, Alignment.MIDDLE_CENTER);

    }

    private void repaintResultTable(Set<ClaimSearchResult> setClaimSearch) {
        VerticalLayout layoutResultTable = mapVerticalLayout.get(ID_LAYOUT_RESULT_TABLE);
        layoutResultTable.removeAllComponents();
        resultTable = new PagedFilterTable();
        resultTable.setWidth("97%");
        resultTable.setImmediate(true);
        resultTable.setSelectable(true);
        resultTable.setFilterBarVisible(false);
        //resultTable.setSizeFull();

        String headerIdColumn = "Id";
        String headerClaimNumberColumn = resourceBundle.getString("claims.number");
        String headerProductColumn = resourceBundle.getString("Product");
        String headerUserColumn = resourceBundle.getString("User");
        String headerPolicyNumberColumn = resourceBundle.getString("NumberPolicy");
        String headerClaimStateColumn = resourceBundle.getString("claimState");

        resultTable.addContainerProperty(headerIdColumn, String.class, null);
        resultTable.addContainerProperty(headerClaimNumberColumn, String.class, null);
        resultTable.addContainerProperty(headerClaimStateColumn, String.class, null);
        resultTable.addContainerProperty(headerPolicyNumberColumn, String.class, null);
        resultTable.addContainerProperty(headerProductColumn, String.class, null);
        resultTable.addContainerProperty(headerUserColumn, String.class, null);

        resultTable.setColumnAlignment(headerIdColumn, CustomTable.Align.LEFT);
        resultTable.setColumnAlignment(headerClaimNumberColumn, CustomTable.Align.LEFT);
        resultTable.setColumnAlignment(headerClaimStateColumn, CustomTable.Align.LEFT);
        resultTable.setColumnAlignment(headerPolicyNumberColumn, CustomTable.Align.LEFT);
        resultTable.setColumnAlignment(headerProductColumn, CustomTable.Align.LEFT);
        resultTable.setColumnAlignment(headerUserColumn, CustomTable.Align.LEFT);

        for(ClaimSearchResult claimSearch: setClaimSearch) {

            resultTable.addItem(claimSearch);
            Item newRow = resultTable.getItem(claimSearch);

            //Agrego la data de la tabla
            newRow.getItemProperty(headerIdColumn).setValue(String.valueOf(claimSearch.getClaimId()));
            newRow.getItemProperty(headerClaimNumberColumn).setValue(claimSearch.getClaimNumber());
            newRow.getItemProperty(headerClaimStateColumn).setValue(ClaimStatus.getInstance(claimSearch.getStateIdClaim()).toString());
            newRow.getItemProperty(headerPolicyNumberColumn).setValue(claimSearch.getPolicyNumber());
            newRow.getItemProperty(headerProductColumn).setValue(claimSearch.getProductName());
            newRow.getItemProperty(headerUserColumn).setValue(claimSearch.getUserName());

        }

        resultTable.addListener(new PagedFilterTable.PageChangeListener() {
            @Override
            public void pageChanged(PagedTableChangeEvent event) {

            }
        });

        layoutResultTable.addComponent(resultTable);
        layoutResultTable.setComponentAlignment(resultTable, Alignment.MIDDLE_CENTER);
        layoutResultTable.addComponent(resultTable.createControls(controlConfigTable(resultTable)));
        layoutResultTable.setMargin(true);
        repaintAllLayout(false);
    }

    private void repaintAllLayout(boolean showFilterLayout) {
        //Comportamiento para de los layouts cuando la busqueda es avanzada
        if(enumTypeClaimSearch == EnumTypeClaimSearch.ADVANCED_SEARCH) {
            mapHorizontalLayout.get(ID_LAYOUT_TOP_MAIN).setVisible(showFilterLayout);
            mapHorizontalLayout.get(ID_LAYOUT_LINE_BOTTOM).setVisible(showFilterLayout);
            mapVerticalLayout.get(ID_LAYOUT_SIMPLE_SEARCH_CLAIM).setVisible(false);
            mapVerticalLayout.get(ID_LAYOUT_CLAIM).setVisible(showFilterLayout);
            mapVerticalLayout.get(ID_LAYOUT_POLICY).setVisible(showFilterLayout);
            mapVerticalLayout.get(ID_LAYOUT_THIRDPARTY).setVisible(showFilterLayout);
            mapHorizontalLayout.get(ID_LAYOUT_MORE_OPTIONS).setVisible(showFilterLayout);
            mapVerticalLayout.get(ID_LAYOUT_RISK_UNIT).setVisible(false);
            mapVerticalLayout.get(ID_LAYOUT_INSURANCE_OBJECT).setVisible(false);
            mapHorizontalLayout.get(ID_LAYOUT_HIDE_OPTIONS).setVisible(false);
            mapHorizontalLayout.get(ID_LAYOUT_BUTTONS).setVisible(showFilterLayout);
            mapVerticalLayout.get(ID_LAYOUT_TABLE).setVisible(!showFilterLayout);
            showLayoutNewLine(showFilterLayout);
        }
        else {
            //Comportamiento para de los layouts cuando la busqueda es simple
            mapHorizontalLayout.get(ID_LAYOUT_TOP_MAIN).setVisible(showFilterLayout);
            mapHorizontalLayout.get(ID_LAYOUT_LINE_BOTTOM).setVisible(showFilterLayout);
            mapVerticalLayout.get(ID_LAYOUT_SIMPLE_SEARCH_CLAIM).setVisible(showFilterLayout);
            mapVerticalLayout.get(ID_LAYOUT_CLAIM).setVisible(false);
            mapVerticalLayout.get(ID_LAYOUT_POLICY).setVisible(false);
            mapVerticalLayout.get(ID_LAYOUT_THIRDPARTY).setVisible(false);
            mapHorizontalLayout.get(ID_LAYOUT_MORE_OPTIONS).setVisible(false);
            mapVerticalLayout.get(ID_LAYOUT_RISK_UNIT).setVisible(false);
            mapVerticalLayout.get(ID_LAYOUT_INSURANCE_OBJECT).setVisible(false);
            mapHorizontalLayout.get(ID_LAYOUT_HIDE_OPTIONS).setVisible(false);
            mapHorizontalLayout.get(ID_LAYOUT_BUTTONS).setVisible(showFilterLayout);
            mapVerticalLayout.get(ID_LAYOUT_TABLE).setVisible(!showFilterLayout);
            showLayoutNewLine(false);
        }
    }

    private void showLayoutNewLine(boolean showLayout) {
        for(String key: mapHorizontalLayout.keySet()) {
            if(key.indexOf(ID_LAYOUT_NEWLINE) > -1) {
                mapHorizontalLayout.get(key).setVisible(showLayout);
            }
        }
    }

    private ClaimSearchParameters getSearchParam() {
        if(enumTypeClaimSearch == EnumTypeClaimSearch.ADVANCED_SEARCH) {
            return getSearchParamAdvancedSearch();
        }
        else {
            return getSearchParamSimpleSearch();
        }
    }

    private ClaimSearchParameters getSearchParamSimpleSearch() {
        String claimNumber = getValueOfTextField(ID_TEXTFIELD_CLAIM_NUMBER);
        String policyNumber = getValueOfTextField(ID_TEXTFIELD_POLICY_NUMBER);
        Date occurrenceDate = getValueOfDateField(ID_DATEFIELD_OCCURRENCE_DATE);
        String productName = getSelectedDescriptionComboBox(ID_COMBO_PRODUCTO_SIMPLE_SEARCH);
        String crmNumber = getValueOfTextField(ID_TEXTFIELD_CRM_NUMBER);

        ClaimCollectionOrderByType criteriaToOrderBySelected = getCriteriaToOrderBy();

        if (claimNumber == null && policyNumber == null && occurrenceDate == null && productName == null && crmNumber == null) {
            return null;
        }
        else {
            ClaimSearchParameters searchParam = new ClaimSearchParameters(EnumTypeClaimSearch.SIMPLE_SEARCH);
            searchParam.setClaimNumber(claimNumber);
            searchParam.setPolicyNumber(policyNumber);
            searchParam.setOccurrenceDate(occurrenceDate);
            searchParam.setProductName(productName);
            searchParam.setOccurrenceDate(occurrenceDate);
            searchParam.setCrmNumber(crmNumber);
            searchParam.setOrderBy(criteriaToOrderBySelected);
            return  searchParam;
        }
    }

    private ClaimSearchParameters getSearchParamAdvancedSearch() {
        Map<String, String> mapClaimEvent = getMapTemplateProperties(ID_OBJECT_TEMPLATE_LAYOUT_CLAIM);
        Map<String, String> mapPolicy = getMapTemplateProperties(ID_OBJECT_TEMPLATE_LAYOUT_POLICY);
        Map<String, String> mapThirdParty = getMapTemplateProperties(ID_OBJECT_TEMPLATE_LAYOUT_THIRDPARTY);
        Map<String, String> mapRole = getMapTemplateProperties(ID_OBJECT_TEMPLATE_LAYOUT_ROL);
        Map<String, String> mapUR = getMapTemplateProperties(ID_OBJECT_TEMPLATE_LAYOUT_RISK_UNIT);
        Map<String, String> mapIO = getMapTemplateProperties(ID_OBJECT_TEMPLATE_LAYOUT_INSURANCE_OBJECT);
        String productName = getSelectedDescriptionComboBox(ID_COMBO_PRODUCTO_ADVANCED_SEARCH);
        String nameTemplateClaimEvent = getNameTemplateClaimEvent();
        String nameTemplateThird = getNameTemplate(ID_COMBO_THIRDPARTY_TYPE, mapTemplateThirdParties);
        String nameTemplateRole = getNameTemplate(ID_COMBO_ROL, mapTemplateRoles);
        String nameTemplateRisk = (mapUR == null ? null : Template.Impl.getDefaultTemplate(CotType.RISK_UNIT).getName());
        String nameTemplateInsurance = getNameTemplate(ID_COMBO_INSURANCE_OBJECT_TYPE, mapTemplateInsuranceObjects);
        ClaimCollectionOrderByType criteriaToOrderBySelected = getCriteriaToOrderBy();

        if (productName == null && nameTemplateInsurance == null && nameTemplateThird == null && nameTemplateRole == null &&
                mapClaimEvent == null && mapPolicy == null && mapThirdParty == null && mapRole == null) {
            return null;
        }
        else {
            ClaimSearchParameters searchParam = new ClaimSearchParameters(EnumTypeClaimSearch.ADVANCED_SEARCH);
            searchParam.setProductName(productName);
            searchParam.setMapPolicyValues(mapPolicy);
            searchParam.setTemplateNameThirdParty(nameTemplateThird);
            searchParam.setMapThirdPartyValues(mapThirdParty);
            searchParam.setTemplateNameClaimEvent(nameTemplateClaimEvent);
            searchParam.setMapClaimEventValues(mapClaimEvent);
            searchParam.setTemplateNameRole(nameTemplateRole);
            searchParam.setMapRoleValues(mapRole);
            searchParam.setTemplateNameRU(nameTemplateRisk);
            searchParam.setMapRUValues(mapUR);
            searchParam.setTemplateNameIO(nameTemplateInsurance);
            searchParam.setMapIOValues(mapIO);
            searchParam.setOrderBy(criteriaToOrderBySelected);
            return searchParam;
        }
    }

    private Map<String, String> getMapTemplateProperties(String idObjectTemplateLayout) {
        Map<String, String> mapProperties = null;
        TemplateLayout objectTemplateLayout = mapObjectTemplateLayout.get(idObjectTemplateLayout);
        if(objectTemplateLayout != null) {
            List<PropertyComponent> listPropertyComponents = objectTemplateLayout.getPropertyComponents();
            for (PropertyComponent propertyComponent : listPropertyComponents) {
                String propertyDescription = propertyComponent.getProperty().getDescription();
                String propertyValue = null;

                if (propertyComponent.getComponent() instanceof VerticalLayout == false &&
                        propertyComponent.getComponent() instanceof Label == false) {

                    if (propertyComponent.getValue() instanceof Boolean) {
                        //Se asume que hay 2 transformadores: 1.0 es true y 0.0 es false. Esto se cumple a veces.
                        //No se encontró otra manera de hacerlo.
                        double value = ((Boolean) propertyComponent.getValue() ? 1.0 : 0.0);
                        for (Transformer transformer : propertyComponent.getProperty().getTransformerList()) {
                            if (transformer.getValue() == value) {
                                propertyValue = transformer.getInput();
                                break;
                            }
                        }
                    } else {
                        propertyValue = (String) propertyComponent.getValue();

                        if (propertyComponent.getProperty().isDate() && propertyValue != null && !propertyValue.equals("")) {
                            try {
                                DateFormat df = new SimpleDateFormat(AcseleConf.getProperty("dateFormatToShow"), getLocale());
                                Date dateValue = df.parse(propertyValue);
                                propertyValue = df.format(dateValue);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                if (propertyValue != null && !propertyValue.trim().equals("") && !propertyValue.equals(VALOR_POR_DEFECTO)) {
                    if(mapProperties == null) {
                        mapProperties = new Hashtable<String, String>();
                    }
                    mapProperties.put(propertyDescription, propertyValue);
                }
            }
        }

        return mapProperties;
    }

    private ClaimCollectionOrderByType getCriteriaToOrderBy() {
        String selectedDescription = getSelectedDescriptionComboBox(ID_COMBO_ORDER_BY);
        if(selectedDescription != null) {
            return ClaimCollectionOrderByType.descriptionOf(selectedDescription);
        }
        else {
            return ClaimCollectionOrderByType.NONE;
        }
    }

    private String getNameTemplateClaimEvent() {
        String nameTemplate = null;
        String selectedDescription = getSelectedDescriptionComboBox(ID_COMBO_EVENTO_SINIESTRO);
        if(selectedDescription != null) {
            ClaimEvent claimEvent = mapClaimEvents.get(selectedDescription);
            if(claimEvent != null) {
                Collection<Template> templateCollection = claimEvent.getClaimDamages();
                for (Template templateItem : templateCollection) {
                    return templateItem.getName();
                }
            }
        }
        return nameTemplate;
    }

    private String getNameTemplate(String idComboBox, Map<String,Template> mapTemplate) {
        String nameTemplate = null;
        String selectedDescription = getSelectedDescriptionComboBox(idComboBox);
        if(selectedDescription != null && mapTemplate != null) {
            Template template = mapTemplate.get(selectedDescription);
            nameTemplate = template.getName();
        }
        return nameTemplate;
    }

    private VerticalLayout createLayoutContainer(String idLayout, String templateTitle, String widthLayoutTitle) {
        //Creando los componentes del layout
        Label labelTitle = createLabel("labelTitle" + countLayoutTemplate, null, templateTitle,"titulo");
        labelTitle.setWidth(null);
        Image imageFlechaNaranja = createImage("imageFlechaNaranja" + countLayoutTemplate,"img/arrow_orange.png");
        Image imageFlechaGris = createImage("imageFlechaGris" + countLayoutTemplate, "img/arrow1.png");

        HorizontalLayout layoutTitle = createHorizontalLayout("layoutTitle" + countLayoutTemplate,widthLayoutTitle,null,null);
        layoutTitle.setHeight("15px");
        layoutTitle.addComponent(labelTitle);
        layoutTitle.addComponent(imageFlechaNaranja);
        layoutTitle.setComponentAlignment(labelTitle,Alignment.TOP_LEFT);
        layoutTitle.setComponentAlignment(imageFlechaNaranja,Alignment.MIDDLE_LEFT);

        HorizontalLayout layoutTemplateTop = createHorizontalLayout("layoutTemplateTop" + countLayoutTemplate,"100%",null,null);
        layoutTemplateTop.addComponent(layoutTitle);
        layoutTemplateTop.addComponent(imageFlechaGris);
        layoutTemplateTop.setComponentAlignment(layoutTitle,Alignment.TOP_LEFT);
        layoutTemplateTop.setComponentAlignment(imageFlechaGris,Alignment.TOP_RIGHT);

        //Agregando al layout sus componentes
        VerticalLayout layoutTemplate = createVerticalLayout(idLayout, "95%", "v-subPanel-content", new MarginInfo(true, true, true, true));
        layoutTemplate.addComponent(layoutTemplateTop);
        layoutTemplate.setComponentAlignment(layoutTemplateTop, Alignment.MIDDLE_CENTER);

        //Agregando el layoutTemplate layoutMain
        layoutMain.addComponent(layoutTemplate);
        layoutMain.addComponent(createLayoutNewLine());
        layoutMain.setComponentAlignment(layoutTemplate, Alignment.MIDDLE_CENTER);

        countLayoutTemplate++;
        return layoutTemplate;
    }

    @Override
    protected void loadCombo(ComboBox combo, final String id){
        combo.addItem(VALOR_POR_DEFECTO);

        if (id.equals(ID_COMBO_PRODUCTO_ADVANCED_SEARCH) || id.equals(ID_COMBO_PRODUCTO_SIMPLE_SEARCH)){
            for(Product product: listProducts) {
                combo.addItem(product.getName());
            }

            if(id.equals(ID_COMBO_PRODUCTO_ADVANCED_SEARCH)) {
                combo.addValueChangeListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                        String selectedDescriptionProduct = (String) valueChangeEvent.getProperty().getValue();
                        reloadComboBoxClaimEvents(selectedDescriptionProduct);
                        reloadLayoutPolicyTemplate(selectedDescriptionProduct);
                        reloadComboBoxInsuranceObject(selectedDescriptionProduct);
                        reloadComboBoxClaimPropertiesEvents(selectedDescriptionProduct);
                    }
                });
            }
        }

        if (id.equals(ID_COMBO_EVENTO_SINIESTRO)){
            combo.addValueChangeListener(new Property.ValueChangeListener(){
                @Override
                public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                    String selectedDescriptionClaimEvent = (String) valueChangeEvent.getProperty().getValue();
                    reloadLayoutClaimTemplate(selectedDescriptionClaimEvent);

                }
            });
        }

        if (id.equals(ID_COMBO_THIRDPARTY_TYPE)){
            for(Template template: listTemplatesThirdParty) {
                combo.addItem(template.getName());
            }
            combo.addValueChangeListener(new Property.ValueChangeListener(){
                @Override
                public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                    String selectedDescriptionThirdPartyType = (String) valueChangeEvent.getProperty().getValue();
                    reloadLayoutThirdPartyTemplate(selectedDescriptionThirdPartyType);

                }
            });
        }

        if (id.equals(ID_COMBO_ROL)){
            for(Template template: listTemplatesRoles) {
                combo.addItem(template.getName());
            }
            combo.addValueChangeListener(new Property.ValueChangeListener(){
                @Override
                public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                    String selectedDescriptionRol = (String) valueChangeEvent.getProperty().getValue();
                    reloadLayoutRolTemplate(selectedDescriptionRol);
                }
            });
        }

        if (id.equals(ID_COMBO_INSURANCE_OBJECT_TYPE)){
            for(Template template: listTemplatesInsuranceObject) {
                combo.addItem(template.getName());
            }
            combo.addValueChangeListener(new Property.ValueChangeListener(){
                @Override
                public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                    String selectedDescriptionInsuranceObjectType = (String) valueChangeEvent.getProperty().getValue();
                    reloadLayoutInsuranceObjectTemplate(selectedDescriptionInsuranceObjectType);

                }
            });
        }

        if (id.equals(ID_COMBO_ORDER_BY)){
            combo.addItem(resourceBundle.getString("claims.number"));
            combo.addItem(resourceBundle.getString("Product"));
            combo.addItem(resourceBundle.getString("OccurrenceDateLabel"));
            combo.addItem(resourceBundle.getString("claimStatus"));
        }

        combo.setValue(VALOR_POR_DEFECTO);
    }

    @Override
    protected void changePage(int pageNumber) {

    }

    private void reloadComboBoxInsuranceObject(String selectedDescriptionProduct) {
        List<AgregInsObjectType> list = mapListInsuranceObjects.get(selectedDescriptionProduct);
        List<String> listaValores = new ArrayList<String>();
        if(list != null) {
            for (AgregInsObjectType ioType : list) {
                listaValores.add(ioType.getDesc());
            }
        }
        else {
            for(Template template: listTemplatesInsuranceObject) {
                listaValores.add(template.getName());
            }
        }
        reloadComboBox(ID_COMBO_INSURANCE_OBJECT_TYPE,listaValores);
    }

    private void reloadComboBoxClaimEvents(String selectedDescriptionProduct) {
        Product selectedProduct = mapProducts.get(selectedDescriptionProduct);
        List<String> listaValores = new ArrayList<String>();
        if(selectedProduct != null) {
            currentProduct = selectedDescriptionProduct;
            Collection<ClaimEvent> claimEvents = selectedProduct.getClaimEvents();
            if (claimEvents != null && !claimEvents.isEmpty()) {
                mapClaimEvents.clear();
                for (ClaimEvent claimEvent : claimEvents) {
                    mapClaimEvents.put(claimEvent.getDesc(), claimEvent);
                    listaValores.add(claimEvent.getDesc());
                }
            }
        } else {
            currentProduct = null;
        }
        reloadComboBox(ID_COMBO_EVENTO_SINIESTRO,listaValores);
    }

    private void reloadComboBoxClaimPropertiesEvents(String selectedDescriptionProduct) {
        Product selectedProduct = mapProducts.get(selectedDescriptionProduct);
        List<com.consisint.acsele.template.api.Property> propertiList = new ArrayList<com.consisint.acsele.template.api.Property>();
        List<com.consisint.acsele.template.api.Property> temp;
        if (selectedProduct != null) {
            Collection<ClaimEvent> claimEvents = selectedProduct.getClaimEvents();
            if (ListUtil.isNotEmptyOrNull(claimEvents)) {
                ClaimEvent claimEv = claimEvents.iterator().next();//.toArray(new ClaimEvent[claimEvents.size()])[0];
                if (ListUtil.isNotEmptyOrNull(claimEv.getClaimDamages())) {
                    temp = claimEv.getClaimDamages().iterator().next().getPropertyList().getAll();
                    for (int i = 0; i < temp.size(); i++) {
                        int c = 0;
                        for (ClaimEvent claimEvent : claimEvents) {
                            for (Template template : claimEvent.getClaimDamages()) {
                                if (template.getPropertyList().contains(temp.get(i))) {
                                    c++;
                                }else
                                    break;
                            }
                            if (c == claimEvents.size()) {
                                propertiList.add(temp.get(i));
                            }
                        }
                    }
                }
            }
        }
        reloadLayoutTemplate(ID_LAYOUT_CLAIM_TEMPLATE, propertiList, ID_OBJECT_TEMPLATE_LAYOUT_CLAIM);
    }


    private void reloadLayoutClaimTemplate(String selectedDescriptionClaimEvent) {
        ClaimEvent claimEvent = mapClaimEvents.get(selectedDescriptionClaimEvent);
        Template template = null;
        if(claimEvent != null) {
            Collection<Template> templateCollection = claimEvent.getClaimDamages();
            for (Template templateItem : templateCollection) {
                template = templateItem;
                break;
            }
        }
        if (template == null) {
            reloadComboBoxClaimPropertiesEvents(currentProduct);
        } else {
            reloadLayoutTemplate(ID_LAYOUT_CLAIM_TEMPLATE, template, ID_OBJECT_TEMPLATE_LAYOUT_CLAIM);
        }
        repaintComboProductoYComboEvento();
    }

    private void reloadLayoutPolicyTemplate(String selectedDescriptionProduct) {
        Product product = mapProducts.get(selectedDescriptionProduct);
        Template template = null;
        if(product != null) {
            template = product.getPolicyTemplate();
        }
        else {
            template = Template.Impl.getDefaultTemplate(CotType.POLICY);
        }
        reloadLayoutTemplate(ID_LAYOUT_POLICY_TEMPLATE, template, ID_OBJECT_TEMPLATE_LAYOUT_POLICY);
    }

    private void reloadLayoutThirdPartyTemplate(String selectedDescriptionThirdPartyType) {
        Template template = mapTemplateThirdParties.get(selectedDescriptionThirdPartyType);
        if(template == null) {
            template = Template.Impl.getDefaultTemplate(CotType.THIRDPARTY);
        }

        reloadLayoutTemplate(ID_LAYOUT_THIRDPARTY_TEMPLATE, template, ID_OBJECT_TEMPLATE_LAYOUT_THIRDPARTY);
        repaintComboThirdPartyType();
    }

    private void reloadLayoutRolTemplate(String selectedDescriptionRol) {
        Template template = mapTemplateRoles.get(selectedDescriptionRol);

        reloadLayoutTemplate(ID_LAYOUT_ROL_TEMPLATE, template, ID_OBJECT_TEMPLATE_LAYOUT_ROL);
        repaintComboRol();
    }

    private void reloadLayoutInsuranceObjectTemplate(String selectedDescriptionInsuranceObjectType) {
        Template template = mapTemplateInsuranceObjects.get(selectedDescriptionInsuranceObjectType);
        if(template == null) {
            template = Template.Impl.getDefaultTemplate(CotType.INSURANCE_OBJECT);
        }

        reloadLayoutTemplate(ID_LAYOUT_INSURANCE_OBJECT_TEMPLATE, template, ID_OBJECT_TEMPLATE_LAYOUT_INSURANCE_OBJECT);
        repaintComboInsuranceObjectType();
    }

    private void reloadLayoutTemplate(String idLayoutTemplate, Template template, String idObjectTemplateLayout) {
        HorizontalLayout layoutTemplate = mapHorizontalLayout.get(idLayoutTemplate);

        if(layoutTemplate != null) {
            layoutTemplate.removeAllComponents();

            if (template != null) {
                List listComponent = new ArrayList<PropertyComponent>();
                List<com.consisint.acsele.template.api.Property> properties = template.getPropertyList().getAll();

                for (com.consisint.acsele.template.api.Property property : properties) {
                    if(property.isVisible() == true) {
                        PropertyComponent propertyComponent = new PropertyComponent(property, false);
                        if(countPropertyTap(properties) > 1) {
                            listComponent.add(propertyComponent);
                        }
                        else {
                            if(propertyComponent.getComponent() instanceof VerticalLayout == false) {
                                listComponent.add(propertyComponent);
                            }
                        }
                    }
                }

                TemplateLayout objectTemplateLayout = new TemplateLayout(listComponent, null, false);
                objectTemplateLayout.setWidth(100, Unit.PERCENTAGE);
                layoutTemplate.addComponent(objectTemplateLayout);
                mapObjectTemplateLayout.put(idObjectTemplateLayout, objectTemplateLayout);
            }
        }
    }

    private void reloadLayoutTemplate(String idLayoutTemplate, List<com.consisint.acsele.template.api.Property> properties, String idObjectTemplateLayout) {
        HorizontalLayout layoutTemplate = mapHorizontalLayout.get(idLayoutTemplate);
        if(layoutTemplate != null) {
            layoutTemplate.removeAllComponents();

            if (properties != null) {
                List listComponent = new ArrayList<PropertyComponent>();

                for (com.consisint.acsele.template.api.Property property : properties) {
                    if(property.isVisible()) {
                        PropertyComponent propertyComponent = new PropertyComponent(property, false);
                        if(countPropertyTap(properties) > 1) {
                            listComponent.add(propertyComponent);
                        }
                        else {
                            if(!(propertyComponent.getComponent() instanceof VerticalLayout)) {
                                listComponent.add(propertyComponent);
                            }
                        }
                    }
                }

                TemplateLayout objectTemplateLayout = new TemplateLayout(listComponent, null, false);
                objectTemplateLayout.setWidth(100, Unit.PERCENTAGE);
                layoutTemplate.addComponent(objectTemplateLayout);
                mapObjectTemplateLayout.put(idObjectTemplateLayout, objectTemplateLayout);
            }
        }
    }

    private void repaintComboProductoYComboEvento() {
        Label labelProducto = mapLabels.get(ID_LABEL_PRODUCTO_ADVANCED_SEARCH);
        Label labelEvento = mapLabels.get(ID_LABEL_EVENTO_SINIESTRO);
        ComboBox comboProducto = mapComboBoxes.get(ID_COMBO_PRODUCTO_ADVANCED_SEARCH);
        ComboBox comboEvento = mapComboBoxes.get(ID_COMBO_EVENTO_SINIESTRO);
        HorizontalLayout layoutComboProducto = mapHorizontalLayout.get(ID_LAYOUT_COMBO_PRODUCTO_ADVANCED_SEARCH);
        HorizontalLayout layoutComboEventoSiniestro = mapHorizontalLayout.get(ID_LAYOUT_COMBO_EVENTO);
        if(comboProducto != null && comboEvento != null && layoutComboEventoSiniestro != null && layoutComboEventoSiniestro != null) {
            if (comboEvento.getValue() == null || comboEvento.getValue() == null ||
                    comboProducto.getValue().equals(VALOR_POR_DEFECTO) || comboEvento.getValue().equals(VALOR_POR_DEFECTO)) {


                layoutComboProducto.addComponent(labelProducto);
                layoutComboProducto.addComponent(comboProducto);
                layoutComboEventoSiniestro.addComponent(labelEvento);
                layoutComboEventoSiniestro.addComponent(comboEvento);
                layoutComboProducto.setExpandRatio(labelProducto, 1);
                layoutComboProducto.setExpandRatio(comboProducto, 5);
                layoutComboEventoSiniestro.setExpandRatio(labelEvento, 1);
                layoutComboEventoSiniestro.setExpandRatio(comboEvento, 5);
            } else {
                TemplateLayout templateLayoutClaim = mapObjectTemplateLayout.get(ID_OBJECT_TEMPLATE_LAYOUT_CLAIM);
                List<Component> listComponentsEvents = new ArrayList<Component>();
                listComponentsEvents.add(labelEvento);
                listComponentsEvents.add(comboEvento);
                templateLayoutClaim.insertListComponentsInFirstGridLayout(listComponentsEvents);

                List<Component> listComponentsProducts = new ArrayList<Component>();
                listComponentsProducts.add(labelProducto);
                listComponentsProducts.add(comboProducto);
                templateLayoutClaim.insertListComponentsInFirstGridLayout(listComponentsProducts);
            }
        }
    }

    private void repaintComboThirdPartyType() {
        Label labelThirdPartyType = mapLabels.get(ID_LABEL_THIRDPARTY_TYPE);
        ComboBox comboThirdPartyType = mapComboBoxes.get(ID_COMBO_THIRDPARTY_TYPE);
        TemplateLayout templateLayoutThirdParty = mapObjectTemplateLayout.get(ID_OBJECT_TEMPLATE_LAYOUT_THIRDPARTY);

        if(comboThirdPartyType != null && templateLayoutThirdParty != null) {
            List<Component> listComponents = new ArrayList<Component>();
            listComponents.add(labelThirdPartyType);
            listComponents.add(comboThirdPartyType);
            templateLayoutThirdParty.insertListComponentsInFirstGridLayout(listComponents);

        }
    }

    private void repaintComboRol() {
        Label labelRol = mapLabels.get(ID_LABEL_ROL);
        ComboBox comboRol = mapComboBoxes.get(ID_COMBO_ROL);
        HorizontalLayout layoutComboRol = mapHorizontalLayout.get(ID_LAYOUT_COMBO_ROL);


        if(comboRol != null && layoutComboRol != null) {
            if (comboRol.getValue() == null || comboRol.getValue() == null ||
                    comboRol.getValue().equals(VALOR_POR_DEFECTO) || comboRol.getValue().equals(VALOR_POR_DEFECTO)) {

                layoutComboRol.addComponent(labelRol);
                layoutComboRol.addComponent(comboRol);
                layoutComboRol.setExpandRatio(labelRol, 1);
                layoutComboRol.setExpandRatio(comboRol, 7);
            }
            else {
                TemplateLayout objectTemplateLayoutRol = mapObjectTemplateLayout.get(ID_OBJECT_TEMPLATE_LAYOUT_ROL);
                List<Component> listComponents = new ArrayList<Component>();
                listComponents.add(labelRol);
                listComponents.add(comboRol);
                objectTemplateLayoutRol.insertListComponentsInFirstGridLayout(listComponents);
            }
        }
    }

    private void repaintComboInsuranceObjectType() {
        Label labelInsuranceObjectType = mapLabels.get(ID_LABEL_INSURANCE_OBJECT_TYPE);
        ComboBox comboInsuranceObjectType = mapComboBoxes.get(ID_COMBO_INSURANCE_OBJECT_TYPE);
        TemplateLayout templateLayoutInsuranceObject = mapObjectTemplateLayout.get(ID_OBJECT_TEMPLATE_LAYOUT_INSURANCE_OBJECT);

        if(comboInsuranceObjectType != null && templateLayoutInsuranceObject != null) {
            List<Component> listComponents = new ArrayList<Component>();
            listComponents.add(labelInsuranceObjectType);
            listComponents.add(comboInsuranceObjectType);
            templateLayoutInsuranceObject.insertListComponentsInFirstGridLayout(listComponents);
        }
    }

    private int countPropertyTap(List<com.consisint.acsele.template.api.Property> properties) {
        int count = 0;
        for(com.consisint.acsele.template.api.Property property: properties) {
            if(property.getTipoPresentador() == TipoPresentador.TAB) {
                count++;
            }
        }
        return count;
    }

}
