package com.sensedia.ecommerce.domain.data;

import com.sensedia.ecommerce.domain.data.Purchase.Cart.User;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Cashback {

  private String transactionId;
  private User user;
  private BigDecimal points;

}
