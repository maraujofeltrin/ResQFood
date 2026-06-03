package ar.edu.itba.paw.services.pack;

import ar.edu.itba.paw.models.auction.Auction;
import ar.edu.itba.paw.models.image.Image;
import ar.edu.itba.paw.models.pack.Municipality;
import ar.edu.itba.paw.models.pack.Pack;
import ar.edu.itba.paw.models.pack.PackDirectEditException;
import ar.edu.itba.paw.models.pack.PackSortOption;
import ar.edu.itba.paw.models.pack.PackTag;
import ar.edu.itba.paw.models.user.Client;
import ar.edu.itba.paw.models.user.Commerce;
import ar.edu.itba.paw.persistence.ImageDao;
import ar.edu.itba.paw.persistence.PackDao;
import ar.edu.itba.paw.services.auction.AuctionService;
import ar.edu.itba.paw.services.reservation.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackServiceImplTest {

    @Mock
    private PackDao packDao;

    @Mock
    private ImageDao imageDao;

    @Mock
    private AuctionService auctionService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private PackServiceImpl packService;

    private static Commerce commerceRef(final long userId) {
        return new Commerce(userId, "Comm", Commerce.Category.BAKERY, "St", 1, Municipality.AVELLANEDA, "P", "1000",
                "08:00", "20:00");
    }

    private static Image imageRef(final long id) {
        return new Image(id, new byte[0], "image/png");
    }

    private static Pack newPack(final Long id, final Long commerceId, final String title, final String description,
            final Double originalPrice, final Double finalPrice, final Integer stock, final Boolean active,
            final List<PackTag> tags) {
        return new Pack(id, commerceRef(commerceId), title, description, originalPrice, finalPrice, stock, active,
                tags);
    }

    private static Pack newPack(final Long id, final Long commerceId, final String title, final String description,
            final Double originalPrice, final Double finalPrice, final Integer stock, final Boolean active,
            final Boolean deleted, final List<PackTag> tags, final Long imageId) {
        return new Pack(id, commerceRef(commerceId), title, description, originalPrice, finalPrice, stock, active,
                deleted, tags, imageId != null ? imageRef(imageId) : null);
    }

    @Test
    void testCreatePackWhenDaoPersistsReturnsPackWithIdAndTitle() {
        // 1. Setup
        final Pack persisted = newPack(1L, 5L, "T", "D", 10.0, 7.0, 3, true, false, Collections.emptyList(), null);
        when(packDao.createPack(eq(5L), eq("T"), eq("D"), eq(10.0), eq(7.0), eq(3), eq(Collections.emptyList()),
                isNull())).thenReturn(persisted);

        // 2. Ejercicio
        final Pack created = packService.createPack(5L, "T", "D", 10.0, 7.0, 3, Collections.emptyList(), null);

        // 3. Asserts
        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertEquals("T", created.getTitle());
    }

    @Test
    void testFindByIdWhenPackExistsReturnsPackFromDao() {
        // 1. Setup
        final Pack persisted = newPack(1L, 5L, "T", "D", 10.0, 7.0, 3, true, false, Collections.emptyList(), null);
        when(packDao.findById(1L)).thenReturn(Optional.of(persisted));

        // 2. Ejercicio
        final Optional<Pack> found = packService.findById(1L);

        // 3. Asserts
        assertTrue(found.isPresent());
        assertEquals("T", found.get().getTitle());
    }

    @Test
    void testDeletePackWhenPackExistsMarksDeletedViaSoftDelete() {
        // 1. Setup
        final Pack pack = newPack(1L, 1L, "a", "b", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        doAnswer(invocation -> {
            pack.setDeleted(true);
            return null;
        }).when(packDao).softDelete(1L);

        when(packDao.findById(1L)).thenReturn(Optional.of(pack));
        when(auctionService.findByPackId(1L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        packService.deletePack(1L);

        // 3. Asserts
        assertTrue(pack.getDeleted());
    }

    @Test
    void testUpdateWhenDaoReturnsPackReflectsMutatedFields() {
        // 1. Setup
        final Pack pack = newPack(2L, 2L, "old", "d", 2.0, 1.0, 2, true, false, Collections.emptyList(), null);
        pack.setTitle("new");
        pack.setStock(5);
        when(packDao.update(pack)).thenReturn(pack);

        // 2. Ejercicio
        final Pack result = packService.update(pack);

        // 3. Asserts
        assertEquals("new", result.getTitle());
        assertEquals(5, result.getStock());
    }

    @Test
    void testFindVisibleForDetailWhenPackActiveReturnsPackForAnonymousViewer() {
        // 1. Setup
        final Pack pack = newPack(7L, 10L, "active", "d", 2.0, 1.0, 2, true, false, Collections.emptyList(), null);
        when(packDao.findById(7L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(7L, null);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(7L, result.get().getId());
    }

    @Test
    void testFindVisibleForDetailWhenPackInactiveReturnsPackForOwnerCommerce() {
        // 1. Setup
        final Pack pack = newPack(8L, 11L, "inactive", "d", 2.0, 1.0, 2, false, false, Collections.emptyList(), null);
        when(packDao.findById(8L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(8L, 11L);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(8L, result.get().getId());
    }

    @Test
    void testFindVisibleForDetailWhenPackInactiveReturnsEmptyForNonOwner() {
        // 1. Setup
        final Pack pack = newPack(9L, 12L, "inactive", "d", 2.0, 1.0, 2, false, false, Collections.emptyList(), null);
        when(packDao.findById(9L)).thenReturn(Optional.of(pack));
        when(reservationService.hasActiveReservation(9L, 99L)).thenReturn(false);
        when(auctionService.findByPackId(9L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(9L, 99L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindVisibleForDetailWhenPackInactiveReturnsPackForAuctionParticipant() {
        // 1. Setup
        final Pack pack = newPack(11L, 12L, "auction", "d", 2.0, 1.0, 0, false, false, Collections.emptyList(), null);
        final Auction auction = new Auction(20L, pack, 1.0, 1.0, 5.0, 99L,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(1), Auction.Status.FINISHED, LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
        when(packDao.findById(11L)).thenReturn(Optional.of(pack));
        when(reservationService.hasActiveReservation(11L, 99L)).thenReturn(false);
        when(auctionService.findByPackId(11L)).thenReturn(Optional.of(auction));
        when(auctionService.hasClientBidOnAuction(20L, 99L)).thenReturn(true);

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(11L, 99L);

        // 3. Asserts
        assertTrue(result.isPresent());
        assertEquals(11L, result.get().getId());
    }

    @Test
    void testFindVisibleForDetailWhenPackMissingReturnsEmpty() {
        // 1. Setup
        when(packDao.findById(999L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(999L, 99L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindVisibleForDetailWhenPackDeletedReturnsEmpty() {
        // 1. Setup
        final Pack pack = newPack(10L, 1L, "gone", "d", 1.0, 1.0, 1, true, true, Collections.emptyList(), null);
        when(packDao.findById(10L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final Optional<Pack> result = packService.findVisibleForDetail(10L, 1L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByIdWhenPackDoesNotExistReturnsEmpty() {
        // 1. Setup
        when(packDao.findById(404L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Optional<Pack> result = packService.findById(404L);

        // 3. Asserts
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllWhenDaoReturnsListReturnsSameContent() {
        // 1. Setup
        final Pack a = newPack(1L, 1L, "A", "d", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        final Pack b = newPack(2L, 1L, "B", "d", 2.0, 2.0, 2, true, false, Collections.emptyList(), null);
        final List<Pack> fromDao = Arrays.asList(a, b);
        when(packDao.findAll()).thenReturn(fromDao);

        // 2. Ejercicio
        final List<Pack> result = packService.findAll();

        // 3. Asserts
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getTitle());
        assertEquals("B", result.get(1).getTitle());
    }

    @Test
    void testFindByCommerceIdWhenDaoReturnsListReturnsSameContent() {
        // 1. Setup
        final long commerceId = 88L;
        final Pack only = newPack(9L, commerceId, "C", "d", 3.0, 3.0, 1, true, false, Collections.emptyList(),
                null);
        when(packDao.findByCommerceId(commerceId)).thenReturn(Collections.singletonList(only));

        // 2. Ejercicio
        final List<Pack> result = packService.findByCommerceId(commerceId);

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).getId());
        assertEquals(commerceId, result.get(0).getCommerceId());
    }

    @Test
    void testFilterPacksWhenDaoReturnsListReturnsSameContent() {
        // 1. Setup
        final String query = "pan";
        final List<PackTag> tags = Collections.singletonList(PackTag.VEGAN);
        final String city = "Rosario";
        final List<String> timeRanges = Collections.singletonList("morning");
        final PackSortOption sort = PackSortOption.PRICE_ASC;
        final int page = 1;
        final int pageSize = 20;
        final Pack filtered = newPack(30L, 2L, "Vegan", "d", 10.0, 8.0, 4, true, false, tags, null);
        when(packDao.filterPacks(eq(query), eq(tags), eq(city), eq(timeRanges), eq(sort), eq(page), eq(pageSize),
                eq(true), isNull())).thenReturn(Collections.singletonList(filtered));

        // 2. Ejercicio
        final List<Pack> result =
                packService.filterPacks(query, tags, city, timeRanges, sort, page, pageSize, true, null);

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(30L, result.get(0).getId());
        assertEquals("Vegan", result.get(0).getTitle());
    }

    @Test
    void testCountFilteredPacksWhenDaoReturnsCountReturnsValue() {
        // 1. Setup
        when(packDao.countFilteredPacks(eq("q"), eq(Collections.emptyList()), isNull(), isNull(), eq(false), isNull()))
                .thenReturn(42);

        // 2. Ejercicio
        final int count = packService.countFilteredPacks("q", Collections.emptyList(), null, null, false, null);

        // 3. Asserts
        assertEquals(42, count);
    }

    @Test
    void testFilterCommercePacksWhenDaoReturnsListReturnsSameContent() {
        // 1. Setup
        final Pack row = newPack(40L, 7L, "Mine", "d", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        when(packDao.filterCommercePacks(7L, Boolean.FALSE, 0, 15)).thenReturn(Collections.singletonList(row));

        // 2. Ejercicio
        final List<Pack> result = packService.filterCommercePacks(7L, false, 0, 15);

        // 3. Asserts
        assertEquals(1, result.size());
        assertEquals(40L, result.get(0).getId());
    }

    @Test
    void testCountCommercePacksWhenDaoReturnsCountReturnsValue() {
        // 1. Setup
        when(packDao.countCommercePacks(7L, null)).thenReturn(3);

        // 2. Ejercicio
        final int count = packService.countCommercePacks(7L, null);

        // 3. Asserts
        assertEquals(3, count);
    }

    @Test
    void testUpdatePackWhenPackNotFoundThrowsPackDirectEditException() {
        // 1. Setup
        when(packDao.findById(999L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final PackDirectEditException exception = assertThrows(PackDirectEditException.class,
                () -> packService.updatePack(999L, "t", "d", 1.0, 1.0, 1, Collections.emptyList(), null));

        // 3. Asserts
        assertEquals(PackDirectEditException.Reason.NOT_FOUND, exception.getReason());
    }

    @Test
    void testUpdatePackWhenPackExistsUpdatesFieldsAndReturnsUpdatedPack() {
        // 1. Setup
        final Pack existing =
                newPack(3L, 5L, "old", "oldD", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        when(packDao.findById(3L)).thenReturn(Optional.of(existing));
        when(imageDao.getImage(77L)).thenReturn(Optional.of(imageRef(77L)));
        when(packDao.update(any(Pack.class))).thenAnswer(invocation -> invocation.getArgument(0));
        final List<PackTag> tags = Collections.singletonList(PackTag.VEGAN);

        // 2. Ejercicio
        final Pack result = packService.updatePack(3L, "NewTitle", "NewDesc", 10.0, 8.0, 12, tags, 77L);

        // 3. Asserts
        assertEquals("NewTitle", result.getTitle());
        assertEquals("NewDesc", result.getDescription());
        assertEquals(12, result.getStock());
        assertEquals(tags, result.getTags());
        assertEquals(77L, result.getImageId());
    }

    @Test
    void testUpdatePackWhenTagsNullUsesEmptyList() {
        // 1. Setup
        final Pack existing =
                newPack(4L, 2L, "x", "y", 1.0, 1.0, 2, true, false, Collections.singletonList(PackTag.VEGAN), null);
        when(packDao.findById(4L)).thenReturn(Optional.of(existing));
        when(packDao.update(any(Pack.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Ejercicio
        final Pack result = packService.updatePack(4L, "x", "y", 1.0, 1.0, 2, null, null);

        // 3. Asserts
        assertTrue(result.getTags().isEmpty());
    }

    @Test
    void testUpdatePackWhenImageIdNullDoesNotSetImageId() {
        // 1. Setup
        final Pack existing =
                newPack(5L, 2L, "x", "y", 1.0, 1.0, 2, true, false, Collections.emptyList(), 99L);
        when(packDao.findById(5L)).thenReturn(Optional.of(existing));
        when(packDao.update(any(Pack.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Ejercicio
        final Pack result = packService.updatePack(5L, "x2", "y2", 2.0, 2.0, 3, Collections.emptyList(), null);

        // 3. Asserts
        assertEquals(99L, result.getImageId());
    }

    @Test
    void testResolvePackForDirectEditWhenPackMissingThrowsNotFound() {
        // 1. Setup
        when(packDao.findById(1L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final PackDirectEditException exception = assertThrows(PackDirectEditException.class,
                () -> packService.resolvePackForDirectEdit(1L));

        // 3. Asserts
        assertEquals(PackDirectEditException.Reason.NOT_FOUND, exception.getReason());
    }

    @Test
    void testResolvePackForDirectEditWhenPackDeletedThrowsNotFound() {
        // 1. Setup
        final Pack pack = newPack(7L, 100L, "p", "d", 1.0, 1.0, 1, true, true, Collections.emptyList(), null);
        when(packDao.findById(7L)).thenReturn(Optional.of(pack));

        // 2. Ejercicio
        final PackDirectEditException exception = assertThrows(PackDirectEditException.class,
                () -> packService.resolvePackForDirectEdit(7L));

        // 3. Asserts
        assertEquals(PackDirectEditException.Reason.NOT_FOUND, exception.getReason());
    }

    @Test
    void testResolvePackForDirectEditWhenAuctionExistsThrowsForbiddenAuction() {
        // 1. Setup
        final Pack pack = newPack(8L, 200L, "a", "d", 1.0, 1.0, 1, true, false, Collections.emptyList(), null);
        when(packDao.findById(8L)).thenReturn(Optional.of(pack));
        final Auction auction =
                new Auction(1L, pack, 1.0, 0.5, 1.0, null, LocalDateTime.now(ZoneOffset.UTC).plusDays(1),
                        Auction.Status.ACTIVE, LocalDateTime.now(ZoneOffset.UTC));
        when(auctionService.findByPackId(8L)).thenReturn(Optional.of(auction));

        // 2. Ejercicio
        final PackDirectEditException exception = assertThrows(PackDirectEditException.class,
                () -> packService.resolvePackForDirectEdit(8L));

        // 3. Asserts
        assertEquals(PackDirectEditException.Reason.FORBIDDEN_AUCTION, exception.getReason());
        assertEquals(PackDirectEditException.ForbiddenAction.EDIT, exception.getForbiddenAction());
    }

    @Test
    void testResolvePackForDirectEditWhenPackValidAndNoAuctionReturnsPack() {
        // 1. Setup
        final Pack pack = newPack(11L, 300L, "ok", "d", 2.0, 1.0, 3, true, false, Collections.emptyList(), null);
        when(packDao.findById(11L)).thenReturn(Optional.of(pack));
        when(auctionService.findByPackId(11L)).thenReturn(Optional.empty());

        // 2. Ejercicio
        final Pack result = packService.resolvePackForDirectEdit(11L);

        // 3. Asserts
        assertEquals(11L, result.getId());
    }
}
