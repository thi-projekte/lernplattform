package de.thi.mynd.subscription.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.ProductSearchResult;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.ProductSearchParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import de.thi.mynd.subscription.entity.SubscriptionStatus;
import de.thi.mynd.subscription.exception.HandledStripeException;
import de.thi.mynd.subscription.exception.ProductNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class StripeServiceImpl implements StripeService {

    @ConfigProperty(name = "mynd.frontendUri")
    String frontendUri;

    @Override
    public Price obtainPriceForSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        Product product = getProductByTierMetadataField(subscriptionStatus);
        return product.getDefaultPriceObject();
    }

    @Override
    public Session createCheckoutSessionForSubscriptionPrice(Price price, String userId) {
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setSuccessUrl(getSuccessUrl())
                .setCancelUrl(getCancelUrl())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .addLineItem(SessionCreateParams.LineItem.builder().setPrice(price.getId()).setQuantity(1L).build());

        paramsBuilder.setCustomer(userId);

        try {
            return Session.create(paramsBuilder.build());
        } catch (StripeException e) {
            throw new HandledStripeException(e.getMessage());
        }
    }

    @Override
    public void cancelSubscriptionImmediately(String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.cancel();
        } catch (StripeException e) {
            throw new HandledStripeException(e.getMessage());
        }
    }

    @Override
    public void cancelSubscriptionAtPeriodEnd(String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();

            subscription.update(params);
        } catch (StripeException e) {
            throw new HandledStripeException(e.getMessage());
        }
    }

    private Product getProductByTierMetadataField(SubscriptionStatus subscriptionStatus) {
        String query = String.format("metadata['tier']:'%s' AND active:'true'", subscriptionStatus.toString());

        ProductSearchParams params = ProductSearchParams.builder()
                .setQuery(query)
                .build();

        try {
            ProductSearchResult result = Product.search(params);
            if (result.getData().isEmpty()) {
                throw new ProductNotFoundException("The product does not exist");
            }
            return result.getData().getFirst();

        } catch (StripeException e) {
            throw new ProductNotFoundException(e.getMessage());
        }
    }

    private String getSuccessUrl() {
        return String.format("%s/subscription?session_id={CHECKOUT_SESSION_ID}", frontendUri);
    }

    private String getCancelUrl() {
        return String.format("%s/subscription?cancel=true", frontendUri);
    }
}
