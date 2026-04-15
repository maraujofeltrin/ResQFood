package ar.edu.itba.paw.services;

import ar.edu.itba.paw.models.Pack;
import ar.edu.itba.paw.models.PackSortOption;
import ar.edu.itba.paw.models.Reservation;
import ar.edu.itba.paw.models.ReservationToken;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.persistence.ReservationTokenDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReservationMailServiceImplTest {

    static class InMemoryReservationTokenDao implements ReservationTokenDao {
        final Map<String, ReservationToken> store = new HashMap<>();

        @Override
        public ReservationToken create(String token, Long reservationId, ReservationToken.Action action,
                LocalDateTime createdAt, LocalDateTime expiresAt) {
            final ReservationToken rt = new ReservationToken(token, reservationId, action, false, createdAt, expiresAt);
            store.put(token, rt);
            return rt;
        }

        @Override
        public Optional<ReservationToken> findByToken(String token) {
            return Optional.ofNullable(store.get(token));
        }

        @Override
        public void markAsUsed(String token) {
            final ReservationToken old = store.get(token);
            if (old != null) {
                final ReservationToken nw = new ReservationToken(old.getToken(), old.getReservationId(),
                        old.getAction(), true, old.getCreatedAt(), old.getExpiresAt());
                store.put(token, nw);
            }
        }
    }

    static class InMemoryPackDao implements PackDao {
        private final Pack pack;
        InMemoryPackDao(Pack pack) { this.pack = pack; }

        @Override public Pack createPack(Long c, String t, String d, Double op, Double fp, Integer s, List<ar.edu.itba.paw.models.PackTag> tags, byte[] id, String ic) { throw new UnsupportedOperationException(); }
        @Override public Optional<Pack> findById(Long id) { return id.equals(pack.getId()) ? Optional.of(pack) : Optional.empty(); }
        @Override public List<Pack> findAll() { return List.of(pack); }
        @Override public List<Pack> findByCommerceId(Long commerceId) { return pack.getCommerceId().equals(commerceId) ? List.of(pack) : Collections.emptyList(); }
        @Override public List<Pack> findActive() { return List.of(pack); }
        @Override public List<Pack> searchPacks(String q) { return List.of(pack); }
        @Override public Pack update(Pack p) { throw new UnsupportedOperationException(); }
        @Override public void softDelete(Long id) { }
        @Override public Optional<Pack> findImageByPackId(Long id) { return Optional.empty(); }
        @Override public void updateImage(Long pId, byte[] id, String ic) { }
        @Override public boolean decrementStock(long pId, int q) { return true; }
        @Override public List<Pack> findActiveByTags(List<ar.edu.itba.paw.models.PackTag> tags) { return Collections.emptyList(); }
        @Override public List<Pack> searchPacksWithTags(String q, List<ar.edu.itba.paw.models.PackTag> tags) { return Collections.emptyList(); }
        @Override public List<Pack> findActive(PackSortOption s) { return findActive(); }
        @Override public List<Pack> searchPacks(String q, PackSortOption s) { return searchPacks(q); }
        @Override public List<Pack> findActiveByTags(List<ar.edu.itba.paw.models.PackTag> tags, PackSortOption s) { return findActiveByTags(tags); }
        @Override public List<Pack> searchPacksWithTags(String q, List<ar.edu.itba.paw.models.PackTag> tags, PackSortOption s) { return searchPacksWithTags(q, tags); }
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

    private InMemoryReservationTokenDao tokenDao;
    private FakeMailSender mailSender;
    private ReservationMailServiceImpl svc;
    private InMemoryClientService clientService;

    @BeforeEach
    public void setUp() {
        tokenDao = new InMemoryReservationTokenDao();
        mailSender = new FakeMailSender();
        clientService = new InMemoryClientService();
    }

    static class InMemoryClientService implements ar.edu.itba.paw.services.ClientService {
        @Override
        public ar.edu.itba.paw.models.Client createClient(final Long userId, final String name, final String lastName,
                final Boolean notificationsVisibilityPreferences) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.Optional<ar.edu.itba.paw.models.Client> findByUserId(final Long userId) {
            return java.util.Optional.of(new ar.edu.itba.paw.models.Client(userId, "ClientName", "Surname", true));
        }

        @Override
        public ar.edu.itba.paw.models.Client update(final ar.edu.itba.paw.models.Client client) {
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
    public void sendReservationRequestToCommerce_createsTokens_and_sendsMail() throws Exception {
        final Reservation reservation = new Reservation(1L, 2L, 3L, LocalDateTime.now(), 5.0,
                Reservation.Status.RESERVED, "code123", null, 1, "pw");
        final Pack pack = new Pack(3L, 2L, "Delicious", "desc", 10.0, 5.0, 1, true, false, List.of(), null, null);

        final PackDao packDao = new InMemoryPackDao(pack);
        svc = new ReservationMailServiceImpl(mailSender, tokenDao, packDao, clientService,
                "noreply@example.org", "ResQFood", "America/Argentina/Buenos_Aires");

        svc.sendReservationRequestToCommerce(reservation, "commerce@example.org", "http://app/", "10/04/2026");

        // two tokens created (accept + reject)
        assertEquals(2, tokenDao.store.size());
        // one mail sent
        assertEquals(1, mailSender.sent.size());

        final MimeMessage msg = mailSender.sent.get(0);
        // verify recipients and from
        assertEquals("commerce@example.org", msg.getAllRecipients()[0].toString());
        assertEquals("ResQFood <noreply@example.org>", msg.getFrom()[0].toString());
        assertEquals("Solicitud de reserva #" + reservation.getId() + " de ClientName Surname", msg.getSubject());
        final String body = extractTextFromMime(msg);
        assertTrue(body.contains("reservations/accept?token="));
        assertTrue(body.contains("reservations/reject?token="));
        assertTrue(body.contains("Delicious"));

        // extract tokens from URLs and ensure they were stored in tokenDao
        final java.util.regex.Pattern p = java.util.regex.Pattern
                .compile("reservations/(?:accept|reject)\\?token=([a-zA-Z0-9\\-]+)");
        final java.util.regex.Matcher m = p.matcher(body);
        final java.util.Set<String> found = new java.util.HashSet<>();
        while (m.find()) {
            found.add(m.group(1));
        }
        // we expect two tokens
        assertEquals(2, found.size());
        for (final String t : found) {
            assertTrue(tokenDao.store.containsKey(t));
        }
    }

    @Test
    public void sendReservationCodeToClient_sendsMail_withCodeAndPackLabel() throws Exception {
        final Reservation reservation = new Reservation(7L, 2L, 11L, LocalDateTime.now(), 9.99,
                Reservation.Status.RESERVED, "PICKUPCODE", null, 1, "pw");
        final Pack pack = new Pack(11L, 2L, "Morning Bread", "desc", 10.0, 5.0, 1, true, false, List.of(), null, null);
        final PackDao packDao = new InMemoryPackDao(pack);
        svc = new ReservationMailServiceImpl(mailSender, tokenDao, packDao, clientService,
                "noreply@example.org", "ResQFood", "America/Argentina/Buenos_Aires");

        svc.sendReservationCodeToClient(reservation, "client@example.org", "10/04/2026");

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
}
