package com.sensedia.ecommerce.domain.data;

import com.sensedia.ecommerce.domain.data.Purchase.Cart.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Order {

  private String id;
  private String paymentId;
  private User user;
  private String store;
  private Double points;

}
