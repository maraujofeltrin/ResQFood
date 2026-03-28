package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Commerce;

public interface CommerceService {
    Commerce getOrCreateCommerce(final String email, final String password, final String name, 
                                 final String commercialName, final Commerce.Category category, 
                                 final String street, final Integer streetNumber, 
                                 final String city, final String province, 
                                 final String postalCode, final String openingTime, final String closingTime);
}
