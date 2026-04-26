package ar.edu.itba.paw.services.reservation;

import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.reservation.Reservation;
import ar.edu.itba.paw.persistence.PackDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReservationMailServiceImplTest {

    static class InMemoryPackDao implements PackDao {
        private final Pack pack;
        InMemoryPackDao(Pack pack) { this.pack = pack; }

        @Override public Pack createPack(Long c, String t, String d, Double op, Double fp, Integer s, List<ar.edu.itba.paw.models.pack.PackTag> tags, byte[] id, String ic) { throw new UnsupportedOperationException(); }
        @Override public Optional<Pack> findById(Long id) { return id.equals(pack.getId()) ? Optional.of(pack) : Optional.empty(); }
        @Override public List<Pack> findAll() { return List.of(pack); }
        @Override public List<Pack> findByCommerceId(Long commerceId) { return pack.getCommerceId().equals(commerceId) ? List.of(pack) : Collections.emptyList(); }
        @Override public Pack update(Pack p) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { }
        @Override public void setActive(Long id, boolean active) { }
        @Override public Optional<Pack> findImageByPackId(Long id) { return Optional.empty(); }
        @Override public void updateImage(Long pId, byte[] id, String ic) { }
        @Override public boolean decrementStock(long packId, int quantity) { return true; }
        @Override public boolean incrementStock(long packId, int quantity) { return true; }
        @Override public java.util.List<Pack> filterPacks(String query, java.util.List<ar.edu.itba.paw.models.pack.PackTag> tags, String city, java.util.List<String> timeRanges, PackSortOption sort, int page, int pageSize) { return Collections.emptyList(); }
        @Override public int countFilteredPacks(String query, java.util.List<ar.edu.itba.paw.models.pack.PackTag> tags, String city, java.util.List<String> timeRanges) { return 0; }
    }

    static class FakeMailSender implements JavaMailSender {
        final List<MimeMessage> sent = new ArrayList<>();
        @Override public MimeMessage createMimeMessage() { return new MimeMessage((javax.mail.Session) null); }
        @Override public MimeMessage createMimeMessage(java.io.InputStream is) { try { return new MimeMessage((javax.mail.Session) null, is); } catch (javax.mail.MessagingException e) { throw new RuntimeException(e); } }
        @Override public void send(MimeMessage m) throws org.springframework.mail.MailException { sent.add(m); }
        @Override public void send(MimeMessage... ms) throws org.springframework.mail.MailException { for (MimeMessage m : ms) send(m); }
        @Override public void send(org.springframework.mail.SimpleMailMessage sm) throws org.springframework.mail.MailException { }
        @Override public void send(org.springframework.mail.SimpleMailMessage... sms) throws org.springframework.mail.MailException { }
        @Override public void send(org.springframework.mail.javamail.MimeMessagePreparator p) throws org.springframework.mail.MailException {
            try { MimeMessage m = createMimeMessage(); p.prepare(m); send(m); } catch (Exception e) { throw new org.springframework.mail.MailSendException("Failed", e); }
        }
        @Override public void send(org.springframework.mail.javamail.MimeMessagePreparator... ps) throws org.springframework.mail.MailException { for (var p : ps) send(p); }
    }

    private FakeMailSender mailSender;
    private ReservationMailServiceImpl svc;
    private InMemoryClientService clientService;

    @BeforeEach
    public void setUp() {
        mailSender = new FakeMailSender();
        clientService = new InMemoryClientService();
    }

    static class InMemoryClientService implements ar.edu.itba.paw.services.user.ClientService {
        @Override
        public ar.edu.itba.paw.models.user.Client createClient(final Long userId, final String name, final String lastName,
                final Boolean notificationsVisibilityPreferences) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.Optional<ar.edu.itba.paw.models.user.Client> findByUserId(final Long userId) {
            return java.util.Optional.of(new ar.edu.itba.paw.models.user.Client(userId, "ClientName", "Surname", true));
        }

        @Override
        public ar.edu.itba.paw.models.user.Client update(final ar.edu.itba.paw.models.user.Client client) {
            throw new UnsupportedOperationException();
        }
    }

    private String extractTextFromMime(MimeMessage msg) throws MessagingException, IOException {
        final Object content = msg.getContent();
        return extractFromObject(content);
    }

    private String extractFromObject(Object content) throws MessagingException, IOException {
        if (content == null)
            return "";
        if (content instanceof String)
            return (String) content;
        if (content instanceof java.io.InputStream) {
            final var is = (java.io.InputStream) content;
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        if (content instanceof Multipart) {
            final Multipart mp = (Multipart) content;
            for (int i = 0; i < mp.getCount(); i++) {
                final BodyPart bp = mp.getBodyPart(i);
                final Object partContent = bp.getContent();
                final String extracted = extractFromObject(partContent);
                if (extracted != null && !extracted.isEmpty())
                    return extracted;
            }
            return "";
        }
        // fallback to toString
        return content.toString();
    }

    @Test
    public void sendReservationRequestToCommerce_sendsMail_withProvidedTokens() throws Exception {
        final Reservation reservation = new Reservation(1L, 2L, 3L, LocalDateTime.now(), 5.0,
                Reservation.Status.RESERVED, "code123", null, 1, "pw");
        final Pack pack = new Pack(3L, 2L, "Delicious", "desc", 10.0, 5.0, 1, true, false, List.of(), null, null);
        final String acceptToken = "accept-token-123";
        final String rejectToken = "reject-token-456";

        final PackDao packDao = new InMemoryPackDao(pack);
        svc = new ReservationMailServiceImpl(mailSender, packDao, clientService,
                "noreply@example.org", "ResQFood", ZoneId.of("America/Argentina/Buenos_Aires"));

        svc.sendReservationRequestToCommerce(reservation, "commerce@example.org", "http://app/",
            acceptToken, rejectToken, "10/04/2026", Locale.forLanguageTag("es"));

        // one mail sent
        assertEquals(1, mailSender.sent.size());

        final MimeMessage msg = mailSender.sent.get(0);
        // verify recipients and from
        assertEquals("commerce@example.org", msg.getAllRecipients()[0].toString());
        assertEquals("ResQFood <noreply@example.org>", msg.getFrom()[0].toString());
        assertEquals("Solicitud de reserva #" + reservation.getId() + " de ClientName Surname", msg.getSubject());
        final String body = extractTextFromMime(msg);
        assertTrue(body.contains("reservations/accept?token=" + acceptToken));
        assertTrue(body.contains("reservations/reject?token=" + rejectToken));
        assertTrue(body.contains("Delicious"));
    }

    @Test
    public void sendReservationCodeToClient_sendsMail_withCodeAndPackLabel() throws Exception {
        final Reservation reservation = new Reservation(7L, 2L, 11L, LocalDateTime.now(), 9.99,
                Reservation.Status.RESERVED, "PICKUPCODE", null, 1, "pw");
        final Pack pack = new Pack(11L, 2L, "Morning Bread", "desc", 10.0, 5.0, 1, true, false, List.of(), null, null);
        final PackDao packDao = new InMemoryPackDao(pack);
        svc = new ReservationMailServiceImpl(mailSender, packDao, clientService,
                "noreply@example.org", "ResQFood", ZoneId.of("America/Argentina/Buenos_Aires"));

        svc.sendReservationCodeToClient(reservation, "client@example.org", "10/04/2026",
            Locale.forLanguageTag("es"));

        assertEquals(1, mailSender.sent.size());
        final MimeMessage msg = mailSender.sent.get(0);
        assertEquals("client@example.org", msg.getAllRecipients()[0].toString());
        assertEquals("ResQFood <noreply@example.org>", msg.getFrom()[0].toString());
        final String subject = msg.getSubject();
        assertTrue(subject.contains("Tu reserva"));
        final String body = extractTextFromMime(msg);
        assertTrue(body.contains("PICKUPCODE"));
        assertTrue(body.contains("Morning Bread"));
    }

    @Test
    public void sendAuctionWinnerCodeToClient_sendsMail_withAuctionWinnerTextAndCode() throws Exception {
        final Reservation reservation = new Reservation(8L, 2L, 12L, LocalDateTime.now(), 14.5,
                Reservation.Status.RESERVED, "WIN123", null, 1, "pw");
        final Pack pack = new Pack(12L, 2L, "Evening Combo", "desc", 20.0, 14.5, 1, true, List.of());
        final PackDao packDao = new InMemoryPackDao(pack);
        svc = new ReservationMailServiceImpl(mailSender, packDao, clientService,
                "noreply@example.org", "ResQFood", ZoneId.of("America/Argentina/Buenos_Aires"));

        svc.sendAuctionWinnerCodeToClient(reservation, "winner@example.org", "11/04/2026",
            Locale.forLanguageTag("es"));

        assertEquals(1, mailSender.sent.size());
        final MimeMessage msg = mailSender.sent.get(0);
        assertEquals("winner@example.org", msg.getAllRecipients()[0].toString());
        assertTrue(msg.getSubject().contains("Ganaste la subasta"));
        final String body = extractTextFromMime(msg);
        assertTrue(body.contains("SUBASTA GANADA"));
        assertTrue(body.contains("WIN123"));
        assertTrue(body.contains("Evening Combo"));
    }

    @Test
    public void sendAuctionWinnerCodeToCommerce_sendsMail_withWinnerAndWithoutCode() throws Exception {
        final Reservation reservation = new Reservation(9L, 2L, 13L, LocalDateTime.now(), 18.0,
                Reservation.Status.RESERVED, "C0DE9", null, 1, "pw");
        final Pack pack = new Pack(13L, 2L, "Late Night Pack", "desc", 25.0, 18.0, 1, true, List.of());
        final PackDao packDao = new InMemoryPackDao(pack);
        svc = new ReservationMailServiceImpl(mailSender, packDao, clientService,
                "noreply@example.org", "ResQFood", ZoneId.of("America/Argentina/Buenos_Aires"));

        svc.sendAuctionWinnerCodeToCommerce(reservation, "commerce@example.org", "12/04/2026",
            Locale.forLanguageTag("es"));

        assertEquals(1, mailSender.sent.size());
        final MimeMessage msg = mailSender.sent.get(0);
        assertEquals("commerce@example.org", msg.getAllRecipients()[0].toString());
        assertTrue(msg.getSubject().contains("ya tiene ganador"));
        final String body = extractTextFromMime(msg);
        assertTrue(body.contains("SUBASTA FINALIZADA"));
        assertTrue(body.contains("Subasta finalizada con ganador"));
        assertTrue(body.contains("Revisá los datos de la reserva"));
        assertTrue(!body.contains("C0DE9"));
        assertTrue(body.contains("Ganador"));
        assertTrue(body.contains("ClientName Surname"));
        assertTrue(body.contains("Late Night Pack"));
    }
}
