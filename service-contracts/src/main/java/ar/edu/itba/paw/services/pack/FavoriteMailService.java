package ar.edu.itba.paw.services.pack;

import java.util.Locale;

public interface FavoriteMailService {

    void sendFavoritePackRestockedToClient(String clientEmail, String packTitle, String commerceName,
            Locale locale);

    void sendFavoriteCommerceNewPackToClient(String clientEmail, String packTitle, String commerceName,
            Locale locale);
}
