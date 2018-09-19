package com.sensedia.ecommerce.domain.data;

import com.sensedia.ecommerce.domain.data.Purchase.Cart.User;
import com.sensedia.ecommerce.domain.data.Purchase.CreditCard;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Payment {

  private String id;
  private CreditCard creditCard;
  private User user;
  private BigDecimal ammount;

}
