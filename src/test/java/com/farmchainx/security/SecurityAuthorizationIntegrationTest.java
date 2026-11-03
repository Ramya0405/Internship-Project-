package com.farmchainx.security;

import com.farmchainx.entity.Farm;
import com.farmchainx.entity.Product;
import com.farmchainx.entity.ProductBatch;
import com.farmchainx.entity.QualityInspection;
import com.farmchainx.entity.Transportation;
import com.farmchainx.entity.User;
import com.farmchainx.entity.Warehouse;
import com.farmchainx.dto.RegistrationRequest;
import com.farmchainx.enums.BatchStatus;
import com.farmchainx.enums.QualityGrade;
import com.farmchainx.enums.TransportationStatus;
import com.farmchainx.enums.UserRole;
import com.farmchainx.repository.FarmRepository;
import com.farmchainx.repository.ProductBatchRepository;
import com.farmchainx.repository.ProductRepository;
import com.farmchainx.repository.QualityInspectionRepository;
import com.farmchainx.repository.TransportationRepository;
import com.farmchainx.repository.UserRepository;
import com.farmchainx.repository.WarehouseRepository;
import com.farmchainx.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductBatchRepository batchRepository;

    @Autowired
    private TransportationRepository transportationRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private QualityInspectionRepository inspectionRepository;

    private User admin;
    private User farmer;
    private User otherFarmer;
    private User distributor;
    private User otherDistributor;
    private User warehouseManager;
    private User otherWarehouseManager;
    private User retailer;
    private User consumer;
    private Farm farm;
    private Product product;
    private ProductBatch batch;
    private Transportation transportation;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        admin = createUser(UserRole.ADMIN);
        farmer = createUser(UserRole.FARMER);
        otherFarmer = createUser(UserRole.FARMER);
        distributor = createUser(UserRole.DISTRIBUTOR);
        otherDistributor = createUser(UserRole.DISTRIBUTOR);
        warehouseManager = createUser(UserRole.WAREHOUSE_MANAGER);
        otherWarehouseManager = createUser(UserRole.WAREHOUSE_MANAGER);
        retailer = createUser(UserRole.RETAILER);
        consumer = createUser(UserRole.CONSUMER);

        farm = new Farm();
        farm.setFarmName("Owned Farm");
        farm.setLocation("Punjab");
        farm.setOwner(farmer);
        farm = farmRepository.saveAndFlush(farm);

        product = new Product();
        product.setProductName("Test Rice");
        product.setCategory("Grain");
        product = productRepository.saveAndFlush(product);

        batch = newBatch(farm, product, farmer, BatchStatus.IN_WAREHOUSE, "FCX-TEST-00000001");
        batch = batchRepository.saveAndFlush(batch);

        transportation = new Transportation();
        transportation.setBatch(batch);
        transportation.setDistributor(distributor);
        transportation.setSourceLocation("Farm");
        transportation.setDestinationLocation("Warehouse");
        transportation.setTransportationStatus(TransportationStatus.SCHEDULED);
        transportation = transportationRepository.saveAndFlush(transportation);

        warehouse = new Warehouse();
        warehouse.setWarehouseName("Owned Warehouse");
        warehouse.setLocation("Punjab");
        warehouse.setManager(warehouseManager);
        warehouse = warehouseRepository.saveAndFlush(warehouse);

        QualityInspection inspection = new QualityInspection();
        inspection.setBatch(batch);
        inspection.setInspectedBy(warehouseManager);
        inspection.setQualityGrade(QualityGrade.A);
        inspection.setInspectionDate(LocalDate.now());
        inspectionRepository.saveAndFlush(inspection);
    }

    @Test
    void adminEndpointAndProductCatalogueFollowRoleMatrix() throws Exception {
        List<User> actors = List.of(
                admin, farmer, distributor, warehouseManager, retailer, consumer);
        for (User actor : actors) {
            ResultActions adminRequest = mockMvc.perform(get("/api/admin/stats")
                    .with(as(actor)).accept(MediaType.APPLICATION_JSON));
            if (actor.getRole() == UserRole.ADMIN) {
                adminRequest.andExpect(status().isOk());
            } else {
                adminRequest.andExpect(status().isForbidden());
            }

            mockMvc.perform(get("/api/products")
                            .with(as(actor)).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/admin/stats").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void farmReadsEnforceOwnerAndAdminAccess() throws Exception {
        mockMvc.perform(get("/api/farms/{id}", farm.getId())
                        .with(as(farmer)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/farms/{id}", farm.getId())
                        .with(as(otherFarmer)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/farms/{id}", farm.getId())
                        .with(as(admin)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/farms/{id}", farm.getId())
                        .with(as(consumer)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/farms/{id}", farm.getId())
                        .with(as(otherFarmer))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"farmName\":\"Hijacked Farm\",\"location\":\"Elsewhere\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void farmerCanRecordSupplyChainEventFromBatchHistoryForm() throws Exception {
        mockMvc.perform(post("/events/batch/{batchId}/add", batch.getId())
                        .with(as(farmer))
                        .with(csrf())
                        .param("batchId", batch.getId().toString())
                        .param("eventType", "HARVESTED")
                        .param("location", "Owned Farm")
                        .param("description", "Harvest recorded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/events/batch/" + batch.getId()));

        mockMvc.perform(get("/api/batches/{batchId}/history", batch.getId())
                        .with(as(farmer))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HARVESTED")));
    }

    @Test
    void rendersEveryControllerBackedThymeleafTemplate() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
        assertRenderedView("/access-denied", admin, "error/access-denied");

        assertRenderedView("/admin/dashboard", admin, "admin/dashboard");
        assertRenderedView("/admin/users", admin, "admin/users");
        assertRenderedView("/admin/users/new", admin, "admin/user-form");
        assertRenderedView("/admin/batches", admin, "admin/batches");
        assertRenderedView("/admin/farms", admin, "admin/farms");

        assertRenderedView("/farmer/dashboard", farmer, "farmer/dashboard");
        assertRenderedView("/farmer/farms", farmer, "farmer/farms");
        assertRenderedView("/farmer/farms/new", farmer, "farmer/farm-form");
        assertRenderedView("/farmer/farms/" + farm.getId() + "/edit", farmer, "farmer/farm-form");
        assertRenderedView("/farmer/batches", farmer, "farmer/batches");
        assertRenderedView("/farmer/batches/new", farmer, "farmer/batch-form");
        assertRenderedView("/farmer/batches/" + batch.getId(), farmer, "farmer/batch-detail");

        assertRenderedView("/distributor/dashboard", distributor, "distributor/dashboard");
        assertRenderedView("/distributor/batches", distributor, "distributor/batches");
        assertRenderedView("/distributor/transportation", distributor, "distributor/transportation-list");
        assertRenderedView("/distributor/transportation/new", distributor,
                "distributor/transportation-form");
        assertRenderedView("/distributor/transportation/" + transportation.getId() + "/edit",
                distributor, "distributor/transportation-form");

        assertRenderedView("/warehouse/dashboard", warehouseManager, "warehouse/dashboard");
        assertRenderedView("/warehouse/batches", warehouseManager, "warehouse/batches");
        assertRenderedView("/warehouse/manage", warehouseManager, "warehouse/list");
        assertRenderedView("/warehouse/manage/new", warehouseManager, "warehouse/form");
        assertRenderedView("/warehouse/manage/" + warehouse.getId() + "/edit",
                warehouseManager, "warehouse/form");

        assertRenderedView("/retailer/dashboard", retailer, "retailer/dashboard");
        assertRenderedView("/retailer/batches", retailer, "retailer/batches");
        assertRenderedView("/consumer/track", consumer, "consumer/track");
        assertRenderedView("/products", admin, "product/list");
        assertRenderedView("/products/new", admin, "product/form");
        assertRenderedView("/products/" + product.getId() + "/edit", admin, "product/form");
        assertRenderedView("/quality/new", warehouseManager, "quality/form");
        assertRenderedView("/quality/batch/" + batch.getId(), warehouseManager, "quality/list");
        assertRenderedView("/events/batch/" + batch.getId(), farmer, "events/history");
        assertRenderedView("/ai/dashboard", admin, "ai/dashboard");
        assertRenderedView("/ai/batch/" + batch.getId(), admin, "ai/batch-analysis");
        assertRenderedView("/notifications", admin, "notifications/list");

        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        WebContext webContext = new WebContext(
                JakartaServletWebApplication.buildApplication(servletContext)
                        .buildExchange(request, response));
        assertThat(templateEngine.process("error/404", webContext))
                .contains("404 – Page Not Found");
        assertThat(templateEngine.process("error/500", webContext))
                .contains("500 – Server Error");
    }

    @Test
    void dashboardAuthorizationMatchesEachRole() throws Exception {
        List<User> actors = List.of(admin, farmer, distributor, warehouseManager, retailer, consumer);
        List<String> dashboards = List.of(
                "/admin/dashboard",
                "/farmer/dashboard",
                "/distributor/dashboard",
                "/warehouse/dashboard",
                "/retailer/dashboard");

        for (String path : dashboards) {
            for (User actor : actors) {
                boolean permitted = switch (path) {
                    case "/admin/dashboard" -> actor.getRole() == UserRole.ADMIN;
                    case "/farmer/dashboard" -> actor.getRole() == UserRole.FARMER;
                    case "/distributor/dashboard" -> actor.getRole() == UserRole.DISTRIBUTOR;
                    case "/warehouse/dashboard" -> actor.getRole() == UserRole.WAREHOUSE_MANAGER;
                    case "/retailer/dashboard" -> actor.getRole() == UserRole.RETAILER;
                    default -> false;
                };
                ResultActions result = mockMvc.perform(get(path)
                        .with(as(actor))
                        .accept(MediaType.APPLICATION_JSON));
                result.andExpect(permitted ? status().isOk() : status().isForbidden());
            }
        }
    }

    @Test
    void dashboardNavigationShowsOnlyTheAuthenticatedRoleSection() throws Exception {
        assertSidebarSection("/admin/dashboard", admin, "Admin", "Farmer");
        assertSidebarSection("/farmer/dashboard", farmer, "Farmer", "Admin");
        assertSidebarSection("/distributor/dashboard", distributor, "Distributor", "Warehouse");
        assertSidebarSection("/warehouse/dashboard", warehouseManager, "Warehouse", "Retailer");
        assertSidebarSection("/retailer/dashboard", retailer, "Retailer", "Distributor");
    }

    @Test
    void farmToConsumerTraceabilityWorkflowPersistsAcrossRoleTransitions() throws Exception {
        MvcResult farmResponse = mockMvc.perform(post("/api/farms")
                        .with(as(farmer))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"farmName\":\"Workflow Farm " + System.nanoTime()
                                + "\",\"location\":\"Punjab\",\"farmSize\":8.5}"))
                .andExpect(status().isCreated())
                .andReturn();
        long workflowFarmId = responseId(farmResponse);

        MvcResult productResponse = mockMvc.perform(post("/api/products")
                        .with(as(farmer))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Workflow Rice " + System.nanoTime()
                                + "\",\"category\":\"Grain\",\"description\":\"Traceability test\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long workflowProductId = responseId(productResponse);

        MvcResult batchResponse = mockMvc.perform(post("/api/batches")
                        .with(as(farmer))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + workflowProductId
                                + ",\"farmId\":" + workflowFarmId
                                + ",\"quantity\":42.0,\"harvestDate\":\"2026-11-03\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode createdBatch = objectMapper.readTree(batchResponse.getResponse().getContentAsString())
                .path("data");
        long workflowBatchId = createdBatch.path("id").asLong();
        String trackingId = createdBatch.path("trackingId").asText();
        assertThat(workflowBatchId).isPositive();
        assertThat(trackingId).startsWith("FCX");

        updateBatchStatus(workflowBatchId, BatchStatus.IN_TRANSIT, farmer);

        mockMvc.perform(post("/api/transportation")
                        .with(as(distributor))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"batchId\":" + workflowBatchId
                                + ",\"sourceLocation\":\"Workflow Farm\","
                                + "\"destinationLocation\":\"Workflow Warehouse\","
                                + "\"vehicleNumber\":\"FCX-TEST-02\"}"))
                .andExpect(status().isCreated());
        updateBatchStatus(workflowBatchId, BatchStatus.IN_WAREHOUSE, distributor);

        mockMvc.perform(post("/api/quality-inspections")
                        .with(as(warehouseManager))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"batchId\":" + workflowBatchId
                                + ",\"qualityGrade\":\"A\","
                                + "\"inspectionDate\":\"2026-11-03\","
                                + "\"temperature\":18.0,\"humidity\":55.0}"))
                .andExpect(status().isCreated());
        updateBatchStatus(workflowBatchId, BatchStatus.QUALITY_INSPECTED, warehouseManager);
        updateBatchStatus(workflowBatchId, BatchStatus.AT_RETAILER, retailer);
        updateBatchStatus(workflowBatchId, BatchStatus.SOLD, retailer);

        mockMvc.perform(get("/api/public/track/{trackingId}", trackingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"currentStatus\":\"SOLD\"")))
                .andExpect(content().string(containsString("\"eventType\":\"DISPATCHED\"")))
                .andExpect(content().string(containsString("\"eventType\":\"QUALITY_CHECK\"")));
    }

    private long responseId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }

    private void updateBatchStatus(long batchId, BatchStatus status, User actor) throws Exception {
        mockMvc.perform(put("/api/batches/{id}/status", batchId)
                        .param("status", status.name())
                        .with(as(actor))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private void assertRenderedView(String path, User actor, String expectedView) throws Exception {
        mockMvc.perform(get(path).with(as(actor)))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedView))
                .andExpect(content().string(containsString("<html")));
    }

    private void assertSidebarSection(String path, User actor, String visibleRole, String hiddenRole)
            throws Exception {
        String html = mockMvc.perform(get(path).with(as(actor)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(html).contains("fcx-sidebar-section\">" + visibleRole);
        assertThat(html).doesNotContain("fcx-sidebar-section\">" + hiddenRole);
    }

    @Test
    void batchReadsRequireOwnerOrRoleAndWorkflowPermission() throws Exception {
        assertBatchRead(farmer, true);
        assertBatchRead(otherFarmer, false);
        assertBatchRead(distributor, true);
        assertBatchRead(warehouseManager, true);
        assertBatchRead(retailer, false);
        assertBatchRead(consumer, false);
        assertBatchRead(admin, true);

        mockMvc.perform(put("/api/batches/{id}/status", batch.getId())
                        .param("status", "RECALLED")
                        .with(as(otherFarmer))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void transportationAccessIsOwnerScopedAndBatchListsHideOtherDistributors() throws Exception {
        mockMvc.perform(get("/api/transportation/{id}", transportation.getId())
                        .with(as(distributor)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transportation/{id}", transportation.getId())
                        .with(as(otherDistributor)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/transportation/{id}", transportation.getId())
                        .with(as(admin)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/transportation/batch/{id}", batch.getId())
                        .with(as(otherDistributor)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"data\":[]")));

        mockMvc.perform(put("/api/transportation/{id}", transportation.getId())
                        .with(as(otherDistributor))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"unauthorized update\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void warehouseAndInspectionReadsRejectUnrelatedOwners() throws Exception {
        mockMvc.perform(get("/warehouse/manage/{id}/edit", warehouse.getId())
                        .with(as(warehouseManager)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/warehouse/manage/{id}/edit", warehouse.getId())
                        .with(as(otherWarehouseManager)).accept(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/warehouse/manage/{id}/edit", warehouse.getId())
                        .with(as(admin)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/warehouse/manage/{id}/edit", warehouse.getId())
                        .with(as(otherWarehouseManager))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("warehouseName", "Changed Warehouse")
                        .param("location", "Elsewhere"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/quality-inspections/{id}", batch.getId())
                        .with(as(farmer)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/quality-inspections/{id}", batch.getId())
                        .with(as(otherFarmer)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/quality-inspections/{id}", batch.getId())
                        .with(as(consumer)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicRegistrationAndApiWritesRequireCsrfAndCannotEscalateRoles() throws Exception {
        String baseRegistration = """
                {
                  "fullName":"Public Registration",
                  "password":"StrongPass123",
                  "confirmPassword":"StrongPass123"
                }
                """;
        String omittedRoleRegistration = """
                {
                  "fullName":"Consumer Default",
                  "email":"consumer-default@example.test",
                  "password":"StrongPass123",
                  "confirmPassword":"StrongPass123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(omittedRoleRegistration))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(omittedRoleRegistration))
                .andExpect(status().isCreated());

        assertPublicRegistrationRole("consumer-default@example.test");
        for (UserRole requestedRole : List.of(
                UserRole.ADMIN,
                UserRole.FARMER,
                UserRole.DISTRIBUTOR,
                UserRole.WAREHOUSE_MANAGER,
                UserRole.RETAILER)) {
            String email = "role-attempt-" + requestedRole.name().toLowerCase() + "@example.test";
            String registration = baseRegistration.replace(
                    "\"password\":\"StrongPass123\"",
                    "\"email\":\"" + email + "\",\"password\":\"StrongPass123\"")
                    .replace("\"confirmPassword\":\"StrongPass123\"",
                            "\"confirmPassword\":\"StrongPass123\",\"role\":\""
                                    + requestedRole.name() + "\"");

            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(registration))
                    .andExpect(status().isCreated());
            assertPublicRegistrationRole(email);
        }

        String farmRequest = """
                {"farmName":"CSRF Farm","location":"Punjab","farmSize":2.5}
                """;
        mockMvc.perform(post("/api/farms")
                        .with(as(farmer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(farmRequest))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/farms")
                        .with(as(farmer))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(farmRequest))
                .andExpect(status().isCreated());
    }

    private void assertPublicRegistrationRole(String email) {
        org.assertj.core.api.Assertions.assertThat(userRepository.findByEmail(email))
                .get()
                .extracting(User::getRole)
                .isEqualTo(UserRole.CONSUMER);
    }

    @Test
    void writeOperationsAreRestrictedToTheirDeclaredRoles() throws Exception {
        ProductBatch transitBatch = batchRepository.saveAndFlush(newBatch(
                farm, product, farmer, BatchStatus.IN_TRANSIT, "FCX-TEST-00000002"));

        List<User> actors = List.of(
                admin, farmer, distributor, warehouseManager, retailer, consumer);
        for (User actor : actors) {
            boolean farmerOrAdmin = actor.getRole() == UserRole.FARMER
                    || actor.getRole() == UserRole.ADMIN;
            boolean distributorOrAdmin = actor.getRole() == UserRole.DISTRIBUTOR
                    || actor.getRole() == UserRole.ADMIN;
            boolean warehouseOrAdmin = actor.getRole() == UserRole.WAREHOUSE_MANAGER
                    || actor.getRole() == UserRole.ADMIN;
            String suffix = Long.toString(System.nanoTime());

            assertJsonPostRole("/api/farms", actor, farmerOrAdmin,
                    "{\"farmName\":\"Farm " + suffix + "\",\"location\":\"Punjab\"}");
            assertJsonPostRole("/api/products", actor, farmerOrAdmin,
                    "{\"productName\":\"Product " + suffix + "\",\"category\":\"Grain\"}");
            assertJsonPostRole("/api/batches", actor, farmerOrAdmin,
                    "{\"productId\":" + product.getId()
                            + ",\"farmId\":" + farm.getId()
                            + ",\"quantity\":10,\"harvestDate\":\"" + LocalDate.now() + "\"}");
            assertJsonPostRole("/api/transportation", actor, distributorOrAdmin,
                    "{\"batchId\":" + transitBatch.getId()
                            + ",\"sourceLocation\":\"Farm\",\"destinationLocation\":\"Hub\"}");
            assertJsonPostRole("/api/quality-inspections", actor, warehouseOrAdmin,
                    "{\"batchId\":" + batch.getId()
                            + ",\"qualityGrade\":\"A\",\"inspectionDate\":\"" + LocalDate.now() + "\"}");

            ResultActions warehouseCreate = mockMvc.perform(post("/warehouse/manage/new")
                    .with(as(actor))
                    .with(csrf())
                    .accept(MediaType.APPLICATION_JSON)
                    .param("warehouseName", "Warehouse " + suffix)
                    .param("location", "Punjab"));
            if (warehouseOrAdmin) {
                warehouseCreate.andExpect(status().is3xxRedirection());
            } else {
                warehouseCreate.andExpect(status().isForbidden());
            }
        }
    }

    @Test
    void adminAllowedOnRoutesWhoseControllerAnnotationsPermitAdmin() throws Exception {
        mockMvc.perform(get("/farmer/batches").with(as(admin)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/warehouse/manage").with(as(admin)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/distributor/transportation").with(as(admin)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/warehouse/manage").with(as(retailer))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/distributor/transportation").with(as(retailer))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateRoleSpecificUsersWithBCryptPasswords() throws Exception {
        String email = "created-" + System.nanoTime() + "@example.test";
        String rawPassword = "ValidPass123";

        mockMvc.perform(get("/admin/users/new").with(as(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Account")))
                .andExpect(content().string(containsString("name=\"role\"")));

        mockMvc.perform(post("/admin/users/new")
                        .with(as(admin))
                        .with(csrf())
                        .param("fullName", "New Farm Owner")
                        .param("email", email)
                        .param("phoneNumber", "+15550001234")
                        .param("role", UserRole.FARMER.name())
                        .param("password", rawPassword)
                        .param("confirmPassword", rawPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/admin/users"));

        User created = userRepository.findByEmail(email).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(created.getRole()).isEqualTo(UserRole.FARMER);
        org.assertj.core.api.Assertions.assertThat(created.getPassword()).isNotEqualTo(rawPassword);
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches(rawPassword, created.getPassword()))
                .isTrue();
    }

    @Test
    void nonAdminCannotCreateUsersOrEscalateRolesThroughAdminForm() throws Exception {
        List<User> actors = List.of(farmer, distributor, warehouseManager, retailer, consumer);
        for (User actor : actors) {
            mockMvc.perform(get("/admin/users/new").with(as(actor)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                            .redirectedUrl("/access-denied"));

            String attemptEmail = "escalation-" + actor.getId() + "@example.test";
            mockMvc.perform(post("/admin/users/new")
                            .with(as(actor))
                            .with(csrf())
                            .param("fullName", "Escalation Attempt")
                            .param("email", attemptEmail)
                            .param("role", UserRole.ADMIN.name())
                            .param("password", "ValidPass123")
                            .param("confirmPassword", "ValidPass123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                            .redirectedUrl("/access-denied"));
            org.assertj.core.api.Assertions.assertThat(userRepository.existsByEmail(attemptEmail)).isFalse();
        }
    }

    @Test
    void adminUserFormRejectsInvalidOrMismatchedInput() throws Exception {
        String email = "invalid-" + System.nanoTime() + "@example.test";

        mockMvc.perform(post("/admin/users/new")
                        .with(as(admin))
                        .with(csrf())
                        .param("fullName", "Valid Name")
                        .param("email", email)
                        .param("role", UserRole.RETAILER.name())
                        .param("password", "ValidPass123")
                        .param("confirmPassword", "DifferentPass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user-form"))
                .andExpect(content().string(containsString("Passwords do not match")));

        org.assertj.core.api.Assertions.assertThat(userRepository.existsByEmail(email)).isFalse();
    }

    @Test
    @WithMockUser(roles = "CONSUMER")
    void nonAdminCannotAssignPrivilegedRoleThroughUserService() {
        RegistrationRequest request = new RegistrationRequest();
        request.setFullName("Unauthorized Admin");
        request.setEmail("service-escalation-" + System.nanoTime() + "@example.test");
        request.setPassword("ValidPass123");
        request.setConfirmPassword("ValidPass123");

        assertThatThrownBy(() -> userService.createUser(request, UserRole.ADMIN))
                .isInstanceOf(AccessDeniedException.class);
        org.assertj.core.api.Assertions.assertThat(userRepository.existsByEmail(request.getEmail())).isFalse();
    }

    @Test
    void transportationEditRendersAnUpdateFormForTheSelectedRecord() throws Exception {
        mockMvc.perform(get("/distributor/transportation/{id}/edit", transportation.getId())
                        .with(as(distributor)))
                .andExpect(status().isOk())
                .andExpect(view().name("distributor/transportation-form"))
                .andExpect(content().string(containsString(
                        "action=\"/distributor/transportation/" + transportation.getId() + "/edit\"")))
                .andExpect(content().string(containsString("name=\"transportationStatus\"")))
                .andExpect(content().string(containsString("name=\"actualArrival\"")));
    }

    @Test
    void farmerBatchDetailDoesNotRenderAnEmptyStatusUpdateForm() throws Exception {
        mockMvc.perform(get("/farmer/batches/{id}", batch.getId()).with(as(farmer)))
                .andExpect(status().isOk())
                .andExpect(view().name("farmer/batch-detail"))
                .andExpect(content().string(containsString(
                        "No status changes are available for this batch.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString(
                        "action=\"/farmer/batches/" + batch.getId() + "/status\""))));
    }

    @Test
    void publicTrackingRemainsAvailableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/track/{trackingId}", batch.getTrackingId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private void assertBatchRead(User actor, boolean permitted) throws Exception {
        ResultActions result = mockMvc.perform(get("/api/batches/{id}", batch.getId())
                .with(as(actor)).accept(MediaType.APPLICATION_JSON));
        if (permitted) {
            result.andExpect(status().isOk());
        } else {
            result.andExpect(status().isForbidden());
        }
    }

    private void assertJsonPostRole(String path, User actor, boolean permitted, String body)
            throws Exception {
        ResultActions result = mockMvc.perform(post(path)
                .with(as(actor))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body));
        if (permitted) {
            result.andExpect(status().isCreated());
        } else {
            result.andExpect(status().isForbidden());
        }
    }

    private User createUser(UserRole role) {
        User user = new User();
        user.setFullName("Test " + role + " " + System.nanoTime());
        user.setEmail(role.name().toLowerCase() + "-" + System.nanoTime() + "@example.test");
        user.setPassword("unused-test-password");
        user.setRole(role);
        return userRepository.saveAndFlush(user);
    }

    private ProductBatch newBatch(Farm farm, Product product, User owner,
                                  BatchStatus status, String trackingId) {
        ProductBatch batch = new ProductBatch();
        batch.setTrackingId(trackingId);
        batch.setProduct(product);
        batch.setFarm(farm);
        batch.setFarmer(owner);
        batch.setQuantity(25.0);
        batch.setHarvestDate(LocalDate.now().minusDays(1));
        batch.setCurrentStatus(status);
        return batch;
    }

    private RequestPostProcessor as(User actor) {
        return user(new CustomUserDetails(actor));
    }
}
