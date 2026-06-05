package de.thi.mynd.subscription.service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.model.entitlements.ActiveEntitlement;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import de.thi.mynd.subscription.exception.HandledStripeException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class StripeServiceImpl implements StripeService {

  @ConfigProperty(name = "mynd.frontendUri")
  String frontendUri;

  @ConfigProperty(name = "mynd.trialDays")
  long trialDays;

  @Inject StripeClient stripeClient;

  @Override
  public List<Product> getAllProductsWithPricesAndMetaData() {

    ProductListParams params = ProductListParams.builder()
            .setActive(true)
            .addExpand("data.marketing_features")
            .build();

    try {
      return stripeClient.v1().products().list(params).getData();
    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Cannot fetch all products");
    }
  }

  @Override
  public Product getFullProductById(String productId) {
    try {
      return stripeClient.v1().products().retrieve(productId);
    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Could not retrieve product");
    }
  }

  @Override
  public List<Price> getAllPricesForProduct(String productId) {
    PriceListParams params =
        PriceListParams.builder().setProduct(productId).setActive(true).build();

    try {
      StripeCollection<Price> prices = stripeClient.v1().prices().list(params);
      return prices.getData();
    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Cannot fetch prices for product");
    }
  }

  @Override
  public Session createCheckoutSessionForSubscriptionPrice(String priceId, String userId) {
    SessionCreateParams.Builder paramsBuilder =
        SessionCreateParams.builder()
            .setSuccessUrl(getSuccessUrl())
            .setCancelUrl(getCancelUrl())
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .addLineItem(
                SessionCreateParams.LineItem.builder().setPrice(priceId).setQuantity(1L).build());

    paramsBuilder.setCustomer(userId);

    try {
      return stripeClient.v1().checkout().sessions().create(paramsBuilder.build());
    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Cannot create checkout session");
    }
  }

  @Override
  public com.stripe.model.billingportal.Session createBillingPortalSession(String customerId) {
    com.stripe.param.billingportal.SessionCreateParams params =
        com.stripe.param.billingportal.SessionCreateParams.builder()
            .setCustomer(customerId)
            .setReturnUrl(frontendUri + "/subscription")
            .build();

    try {
      return stripeClient.v1().billingPortal().sessions().create(params);
    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Cannot create billing portal session");
    }
  }

  @Override
  public Customer getOrCreateCustomer(String username) {
    String query = String.format("name:\"%s\"", username);
    CustomerSearchParams searchParams = CustomerSearchParams.builder().setQuery(query).build();

    try {
      StripeSearchResult<Customer> result = stripeClient.v1().customers().search(searchParams);

      if (!result.getData().isEmpty()) {
        return result.getData().getFirst();
      }

      CustomerCreateParams createParams = CustomerCreateParams.builder().setName(username).build();

      return stripeClient.v1().customers().create(createParams);

    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Cannot get or create the customer");
    }
  }

  @Override
  public Subscription createTrialForPriceId(String priceId, String customerId) {
    SubscriptionCreateParams params =
        SubscriptionCreateParams.builder()
            .setCustomer(customerId)
            .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
            .setTrialPeriodDays(trialDays)
            .setCancelAtPeriodEnd(true)
            .build();

    try {
      return stripeClient.v1().subscriptions().create(params);
    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Cannot create trial");
    }
  }

  @Override
  public List<ProductFeature> getProductFeatures(String productId) {
    try {
      return stripeClient.v1().products().features().list(productId).getData();
    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Cannot fetch stripe product features");
    }
  }

  private String getSuccessUrl() {
    return String.format("%s/subscription?success=true", frontendUri);
  }

  private String getCancelUrl() {
    return String.format("%s/subscription?success=false", frontendUri);
  }
}
