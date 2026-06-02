package de.thi.mynd.subscription.service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.ProductSearchParams;
import com.stripe.param.checkout.SessionCreateParams;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.HandledStripeException;
import de.thi.mynd.subscription.exception.ProductNotFoundException;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class StripeServiceImpl implements StripeService {

  @ConfigProperty(name = "mynd.frontendUri")
  String frontendUri;

  @Inject StripeClient stripeClient;

  @Override
  public List<Product> getAllProductsWithPricesAndMetaData() {

    ProductListParams params =
        ProductListParams.builder().addExpand("data.prices").addExpand("data.metadata").build();

    try {
      return stripeClient.v1().products().list(params).getData();
    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new HandledStripeException("Cannot fetch all products");
    }
  }

  @Override
  public Product getFullProductById(String productId) {
    return null;
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

  private Product getProductByTierMetadataField(SubscriptionStatus subscriptionStatus) {
    String query =
        String.format(
            "metadata[\"tier\"]:\"%s\" AND active:\"true\"", subscriptionStatus.toString());

    ProductSearchParams params =
        ProductSearchParams.builder().setQuery(query).addExpand("data.default_price").build();

    try {
      StripeSearchResult<Product> result = stripeClient.v1().products().search(params);
      if (result.getData().isEmpty()) {
        throw new ProductNotFoundException("The product does not exist");
      }
      return result.getData().getFirst();

    } catch (StripeException e) {
      Log.error(e.getMessage());
      throw new ProductNotFoundException("Cannot fetch product by metadata");
    }
  }

  private String getSuccessUrl() {
    return String.format("%s/subscription?success=true", frontendUri);
  }

  private String getCancelUrl() {
    return String.format("%s/subscription?success=false", frontendUri);
  }
}
