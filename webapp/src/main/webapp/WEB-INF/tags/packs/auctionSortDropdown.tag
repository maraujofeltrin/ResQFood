<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ attribute name="availableSorts" required="true" type="ar.edu.itba.paw.models.auction.AuctionSortOption[]" %>
<%@ attribute name="currentSort" required="true" type="ar.edu.itba.paw.models.auction.AuctionSortOption" %>
<%@ attribute name="baseUrl" required="true" type="java.lang.String" %>
<%@ attribute name="searchQuery" required="false" type="java.lang.String" %>
<%@ attribute name="selectedTags" required="false" type="java.util.List" %>
<%@ attribute name="selectedTypes" required="false" type="java.util.List" %>
<%@ attribute name="classes" required="false" type="java.lang.String" %>

<paw:inlineFormSelectDropdown
        selectName="auctionSort"
        options="${availableSorts}"
        selectedValue="${currentSort.name()}"
        optionMessageCodePrefix="auction.sort."
        icon="hourglass_top"
        classes="${classes != null ? classes : 'relative inline-flex items-center max-w-full'}" />
