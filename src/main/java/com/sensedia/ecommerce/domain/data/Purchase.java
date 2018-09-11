package com.sensedia.ecommerce.domain.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Purchase {

  private String _id;
  private CreditCard creditCard;
  private Cart cart;

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class CreditCard {

    private String id;
    private String issuer;
    private String number;
    private String name;
    private String adress;
    private String country;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime exp;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class Cart {

    private String id;
    private User user;
    private ShippingAddress shippingAddress;
    private BillingAddress billingAddress;
    private List<Items> items;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class User {

      private String id;
      private String email;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class ShippingAddress {

      private Address address;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class BillingAddress {

      private Address address;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Address {

      private String title;
      private String street;
      private String city;
      private String zipCode;
      private String country;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Items {

      private String number;
      private Product product;
      private BigDecimal price;

      @AllArgsConstructor
      @NoArgsConstructor
      @Data
      @Builder
      public static class Product {

        private String id;
        private String name;
        private String description;
      }
    }
  }
}
