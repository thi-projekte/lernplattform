package de.thi.mynd.subscription.dto;

import lombok.Builder;

@Builder
public final class PriceDto {
  public String id;
  public String interval;
  public double amount;
}
