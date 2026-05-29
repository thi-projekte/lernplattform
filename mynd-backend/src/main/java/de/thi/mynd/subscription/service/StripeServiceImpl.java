package de.thi.mynd.subscription.service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.ProductSearchParams;
import com.stripe.param.checkout.SessionCreateParams;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.HandledStripeException;
import de.thi.mynd.subscription.exception.ProductNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class StripeServiceImpl implements StripeService {

  @ConfigProperty(name = "mynd.frontendUri")
  String frontendUri;

  @Inject StripeClient stripeClient;

  @Override
  public Price obtainPriceForSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
    Product product = getProductByTierMetadataField(subscriptionStatus);
    return product.getDefaultPriceObject();
  }

  @Override
  public Session createCheckoutSessionForSubscriptionPrice(Price price, String userId) {
    SessionCreateParams.Builder paramsBuilder =
        SessionCreateParams.builder()
            .setSuccessUrl(getSuccessUrl())
            .setCancelUrl(getCancelUrl())
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(price.getId())
                    .setQuantity(1L)
                    .build());

    paramsBuilder.setCustomer(userId);

    try {
      return stripeClient.checkout().sessions().create(paramsBuilder.build());
    } catch (StripeException e) {
      throw new HandledStripeException(e.getMessage());
    }
  }

  @Override
  public com.stripe.model.billingportal.Session createBillingPortalSession(String customerId) {
    com.stripe.param.billingportal.SessionCreateParams params = com.stripe.param.billingportal.SessionCreateParams.builder()
            .setCustomer(customerId)
            .setReturnUrl(frontendUri + "/subscription")
            .build();

    try {
      return stripeClient.billingPortal().sessions().create(params);
    } catch (StripeException e) {
      throw new HandledStripeException(e.getMessage());
    }
  }

  @Override
  public Customer createCustomer(String username) {
    CustomerCreateParams params = CustomerCreateParams.builder().setName(username).build();

    try {
      return stripeClient.customers().create(params);
    } catch (StripeException e) {
      throw new HandledStripeException(e.getMessage());
    }
  }

  private Product getProductByTierMetadataField(SubscriptionStatus subscriptionStatus) {
    String query =
        String.format("metadata['tier']:'%s' AND active:'true'", subscriptionStatus.toString());

    ProductSearchParams params =
        ProductSearchParams.builder().setQuery(query).addExpand("data.default_price").build();

    try {
      StripeSearchResult<Product> result = stripeClient.products().search(params);
      if (result.getData().isEmpty()) {
        throw new ProductNotFoundException("The product does not exist");
      }
      return result.getData().getFirst();

    } catch (StripeException e) {
      throw new ProductNotFoundException(e.getMessage());
    }
  }

  private String getSuccessUrl() {
    return String.format("%s/subscription?success=true", frontendUri);
  }

  private String getCancelUrl() {
    return String.format("%s/subscription?success=false", frontendUri);
  }
}
