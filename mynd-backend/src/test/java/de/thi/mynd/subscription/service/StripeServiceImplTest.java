package de.thi.mynd.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.service.*;
import com.stripe.service.checkout.SessionService;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.HandledStripeException;
import de.thi.mynd.subscription.exception.ProductNotFoundException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class StripeServiceImplTest {

  @Inject StripeService stripeService;

  @InjectMock StripeClient stripeClient;

  // Stripe service chain mocks
  private V1Services v1Services;
  private ProductService productService;
  private CustomerService customerService;
  private CheckoutService checkoutService;
  private SessionService checkoutSessionService;
  private BillingPortalService billingPortalService;
  private com.stripe.service.billingportal.SessionService billingPortalSessionService;

  private static final String FRONTEND_URI = "https://app.mynd.de";
  private static final String CUSTOMER_ID = "cus_abc123";
  private static final String USERNAME = "testuser";
  private static final String PRODUCT_ID = "prod_xyz";
  private static final String PRICE_ID = "price_xyz";

  @BeforeEach
  void setUp() {
    v1Services = mock(V1Services.class);
    productService = mock(ProductService.class);
    customerService = mock(CustomerService.class);
    checkoutService = mock(CheckoutService.class);
    checkoutSessionService = mock(SessionService.class);
    billingPortalService = mock(BillingPortalService.class);
    billingPortalSessionService = mock(com.stripe.service.billingportal.SessionService.class);

    when(stripeClient.v1()).thenReturn(v1Services);
    when(v1Services.products()).thenReturn(productService);
    when(v1Services.customers()).thenReturn(customerService);
    when(v1Services.checkout()).thenReturn(checkoutService);
    when(checkoutService.sessions()).thenReturn(checkoutSessionService);
    when(v1Services.billingPortal()).thenReturn(billingPortalService);
    when(billingPortalService.sessions()).thenReturn(billingPortalSessionService);
  }

  // --- obtainPriceForSubscriptionStatus ---

  @Test
  void obtainPriceForSubscriptionStatus_withMatchingProduct_returnsDefaultPrice()
      throws StripeException {
    Price price = mock(Price.class);
    Product product = mock(Product.class);
    when(product.getDefaultPriceObject()).thenReturn(price);

    StripeSearchResult<Product> searchResult = mockSearchResult(List.of(product));
    when(productService.search(any(ProductSearchParams.class))).thenReturn(searchResult);

    Price result = stripeService.obtainPriceForSubscriptionStatus(SubscriptionStatus.PREMIUM);

    assertNotNull(result);
    assertSame(price, result);
  }

  @Test
  void obtainPriceForSubscriptionStatus_withNoMatchingProduct_throwsProductNotFoundException()
      throws StripeException {
    StripeSearchResult<Product> emptyResult = mockSearchResult(Collections.emptyList());
    when(productService.search(any(ProductSearchParams.class))).thenReturn(emptyResult);

    ProductNotFoundException ex =
        assertThrows(
            ProductNotFoundException.class,
            () -> stripeService.obtainPriceForSubscriptionStatus(SubscriptionStatus.PREMIUM));

    assertEquals("The product does not exist", ex.getMessage());
  }

  @Test
  void obtainPriceForSubscriptionStatus_whenStripeThrows_throwsProductNotFoundException()
      throws StripeException {
    when(productService.search(any(ProductSearchParams.class)))
        .thenThrow(mock(StripeException.class));

    assertThrows(
        ProductNotFoundException.class,
        () -> stripeService.obtainPriceForSubscriptionStatus(SubscriptionStatus.PREMIUM));
  }

  @Test
  void obtainPriceForSubscriptionStatus_buildsQueryWithTierAndActiveFilter()
      throws StripeException {
    Product product = mock(Product.class);
    when(product.getDefaultPriceObject()).thenReturn(mock(Price.class));

    StripeSearchResult<Product> searchResult = mockSearchResult(List.of(product));
    ArgumentCaptor<ProductSearchParams> captor = ArgumentCaptor.forClass(ProductSearchParams.class);
    when(productService.search(captor.capture())).thenReturn(searchResult);

    stripeService.obtainPriceForSubscriptionStatus(SubscriptionStatus.PREMIUM);

    String query = captor.getValue().toMap().get("query").toString();
    assertTrue(query.contains("PRO"));
    assertTrue(query.contains("active:\"true\""));
  }

  // --- getFullProductById ---

  @Test
  void getFullProductById_withValidId_returnsProduct() throws StripeException {
    Product product = mock(Product.class);
    when(productService.retrieve(PRODUCT_ID)).thenReturn(product);

    Product result = stripeService.getFullProductById(PRODUCT_ID);

    assertSame(product, result);
  }

  @Test
  void getFullProductById_whenStripeThrows_throwsHandledStripeException() throws StripeException {
    StripeException stripeException = mock(StripeException.class);
    when(stripeException.getMessage()).thenReturn("Not found");
    when(productService.retrieve(PRODUCT_ID)).thenThrow(stripeException);

    HandledStripeException ex =
        assertThrows(
            HandledStripeException.class, () -> stripeService.getFullProductById(PRODUCT_ID));

    assertTrue(ex.getMessage().contains("Could not retrieve product"));
  }

  // --- createCheckoutSessionForSubscriptionPrice ---

  @Test
  void createCheckoutSessionForSubscriptionPrice_withValidInputs_returnsSession()
      throws StripeException {
    Price price = mock(Price.class);
    when(price.getId()).thenReturn(PRICE_ID);

    Session session = mock(Session.class);
    when(checkoutSessionService.create(any(SessionCreateParams.class))).thenReturn(session);

    Session result = stripeService.createCheckoutSessionForSubscriptionPrice(price, CUSTOMER_ID);

    assertNotNull(result);
    assertSame(session, result);
  }

  @Test
  void createCheckoutSessionForSubscriptionPrice_setsCustomerAndPriceAndMode()
      throws StripeException {
    Price price = mock(Price.class);
    when(price.getId()).thenReturn(PRICE_ID);

    ArgumentCaptor<SessionCreateParams> captor = ArgumentCaptor.forClass(SessionCreateParams.class);
    when(checkoutSessionService.create(captor.capture())).thenReturn(mock(Session.class));

    stripeService.createCheckoutSessionForSubscriptionPrice(price, CUSTOMER_ID);

    SessionCreateParams params = captor.getValue();
    assertEquals(CUSTOMER_ID, params.getCustomer());
    assertEquals(SessionCreateParams.Mode.SUBSCRIPTION, params.getMode());
    assertEquals(1, params.getLineItems().size());
    assertEquals(PRICE_ID, params.getLineItems().get(0).getPrice());
    assertEquals(1L, params.getLineItems().get(0).getQuantity());
  }

  @Test
  void createCheckoutSessionForSubscriptionPrice_setsSuccessAndCancelUrls() throws StripeException {
    Price price = mock(Price.class);
    when(price.getId()).thenReturn(PRICE_ID);

    ArgumentCaptor<SessionCreateParams> captor = ArgumentCaptor.forClass(SessionCreateParams.class);
    when(checkoutSessionService.create(captor.capture())).thenReturn(mock(Session.class));

    stripeService.createCheckoutSessionForSubscriptionPrice(price, CUSTOMER_ID);

    SessionCreateParams params = captor.getValue();
    assertTrue(params.getSuccessUrl().contains("success=true"));
    assertTrue(params.getCancelUrl().contains("success=false"));
  }

  @Test
  void createCheckoutSessionForSubscriptionPrice_whenStripeThrows_throwsHandledStripeException()
      throws StripeException {
    Price price = mock(Price.class);
    when(price.getId()).thenReturn(PRICE_ID);

    StripeException stripeException = mock(StripeException.class);
    when(stripeException.getMessage()).thenReturn("Stripe error");
    when(checkoutSessionService.create(any(SessionCreateParams.class))).thenThrow(stripeException);

    assertThrows(
        HandledStripeException.class,
        () -> stripeService.createCheckoutSessionForSubscriptionPrice(price, CUSTOMER_ID));
  }

  // --- createBillingPortalSession ---

  @Test
  void createBillingPortalSession_withValidCustomerId_returnsSession() throws StripeException {
    com.stripe.model.billingportal.Session session =
        mock(com.stripe.model.billingportal.Session.class);
    when(billingPortalSessionService.create(
            any(com.stripe.param.billingportal.SessionCreateParams.class)))
        .thenReturn(session);

    com.stripe.model.billingportal.Session result =
        stripeService.createBillingPortalSession(CUSTOMER_ID);

    assertNotNull(result);
    assertSame(session, result);
  }

  @Test
  void createBillingPortalSession_setsCustomerAndReturnUrl() throws StripeException {
    ArgumentCaptor<com.stripe.param.billingportal.SessionCreateParams> captor =
        ArgumentCaptor.forClass(com.stripe.param.billingportal.SessionCreateParams.class);
    when(billingPortalSessionService.create(captor.capture()))
        .thenReturn(mock(com.stripe.model.billingportal.Session.class));

    stripeService.createBillingPortalSession(CUSTOMER_ID);

    com.stripe.param.billingportal.SessionCreateParams params = captor.getValue();
    assertEquals(CUSTOMER_ID, params.getCustomer());
    assertTrue(params.getReturnUrl().contains("/subscription"));
  }

  @Test
  void createBillingPortalSession_whenStripeThrows_throwsHandledStripeException()
      throws StripeException {
    StripeException stripeException = mock(StripeException.class);
    when(billingPortalSessionService.create(
            any(com.stripe.param.billingportal.SessionCreateParams.class)))
        .thenThrow(stripeException);

    assertThrows(
        HandledStripeException.class, () -> stripeService.createBillingPortalSession(CUSTOMER_ID));
  }

  // --- getOrCreateCustomer ---

  @Test
  void getOrCreateCustomer_whenCustomerExists_returnsExistingCustomer() throws StripeException {
    Customer existingCustomer = mock(Customer.class);
    StripeSearchResult<Customer> searchResult = mockSearchResult(List.of(existingCustomer));
    when(customerService.search(any(CustomerSearchParams.class))).thenReturn(searchResult);

    Customer result = stripeService.getOrCreateCustomer(USERNAME);

    assertSame(existingCustomer, result);
    verify(customerService, never()).create(any(CustomerCreateParams.class));
  }

  @Test
  void getOrCreateCustomer_whenNoCustomerExists_createsAndReturnsNewCustomer()
      throws StripeException {
    StripeSearchResult<Customer> emptyResult = mockSearchResult(Collections.emptyList());
    when(customerService.search(any(CustomerSearchParams.class))).thenReturn(emptyResult);

    Customer newCustomer = mock(Customer.class);
    when(customerService.create(any(CustomerCreateParams.class))).thenReturn(newCustomer);

    Customer result = stripeService.getOrCreateCustomer(USERNAME);

    assertSame(newCustomer, result);
    verify(customerService).create(any(CustomerCreateParams.class));
  }

  @Test
  void getOrCreateCustomer_searchQueryContainsUsername() throws StripeException {
    StripeSearchResult<Customer> emptyResult = mockSearchResult(Collections.emptyList());
    ArgumentCaptor<CustomerSearchParams> captor =
        ArgumentCaptor.forClass(CustomerSearchParams.class);
    when(customerService.search(captor.capture())).thenReturn(emptyResult);
    when(customerService.create(any(CustomerCreateParams.class))).thenReturn(mock(Customer.class));

    stripeService.getOrCreateCustomer(USERNAME);

    String query = captor.getValue().toMap().get("query").toString();
    assertTrue(query.contains(USERNAME));
  }

  @Test
  void getOrCreateCustomer_createParamsContainUsername() throws StripeException {
    StripeSearchResult<Customer> emptyResult = mockSearchResult(Collections.emptyList());
    when(customerService.search(any(CustomerSearchParams.class))).thenReturn(emptyResult);

    ArgumentCaptor<CustomerCreateParams> captor =
        ArgumentCaptor.forClass(CustomerCreateParams.class);
    when(customerService.create(captor.capture())).thenReturn(mock(Customer.class));

    stripeService.getOrCreateCustomer(USERNAME);

    assertEquals(USERNAME, captor.getValue().getName());
  }

  @Test
  void getOrCreateCustomer_whenStripeThrows_throwsHandledStripeException() throws StripeException {
    when(customerService.search(any(CustomerSearchParams.class)))
        .thenThrow(mock(StripeException.class));

    assertThrows(HandledStripeException.class, () -> stripeService.getOrCreateCustomer(USERNAME));
  }

  // --- Helpers ---

  @SuppressWarnings("unchecked")
  private <T extends StripeObjectInterface> StripeSearchResult<T> mockSearchResult(List<T> data) {
    StripeSearchResult<T> result = mock(StripeSearchResult.class);
    when(result.getData()).thenReturn(data);
    return result;
  }
}
