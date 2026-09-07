package ar.edu.itba.paw.services.auction;

import java.util.Locale;

public interface AuctionMailService {

    void sendAuctionOutbidToClient(String clientEmail, String packTitle, String commerceName,
            double newAmount, Locale locale);

    void sendAuctionFinishedLostToClient(String clientEmail, String packTitle, String commerceName,
            Locale locale);
}
