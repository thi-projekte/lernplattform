package de.thi.mynd.subscription.request;

import jakarta.validation.constraints.NotBlank;

public final class SubscribeRequest {

  @NotBlank public String priceId;
}
