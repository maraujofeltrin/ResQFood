<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="paw" uri="http://itba.edu.ar/paw/tags" %>
<%@ attribute name="availableSorts" required="true" type="ar.edu.itba.paw.models.pack.PackSortOption[]" %>
<%@ attribute name="currentSort" required="true" type="ar.edu.itba.paw.models.pack.PackSortOption" %>
<%@ attribute name="classes" required="false" type="java.lang.String" %>

<paw:inlineFormSelectDropdown
        selectName="sort"
        options="${availableSorts}"
        selectedValue="${currentSort.name()}"
        optionMessageCodePrefix="pack.sort."
        classes="${classes != null ? classes : 'relative inline-flex items-center max-w-full'}" />
