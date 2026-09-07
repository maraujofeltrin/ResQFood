<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!-- Cancel Auction Confirmation Modal -->
<div id="cancelAuctionModal" class="hidden fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" onclick="closeCancelAuctionModal()">
    </div>
    <div class="relative bg-surface rounded-2xl p-8 max-w-md w-full shadow-2xl flex flex-col gap-4 transform transition-all scale-95 opacity-0"
        id="cancelAuctionModalContent">
        <div class="flex items-center gap-4 text-error mb-2">
            <span class="material-symbols-outlined text-4xl">warning</span>
            <h3 class="text-2xl font-headline font-bold text-on-surface">
                <spring:message code="commerce.auction.cancel.title" />
            </h3>
        </div>
        <p class="text-secondary font-body">
            <spring:message code="commerce.auction.cancel.description" />
        </p>
        <div class="flex items-center justify-end gap-3 mt-4">
            <button type="button" onclick="closeCancelAuctionModal()"
                class="px-6 py-2 rounded-full font-bold text-secondary hover:bg-surface-variant transition-colors">
                <spring:message code="commerce.auction.cancel.cancel" />
            </button>
            <form id="cancelAuctionForm" method="POST" action="">
                <button type="submit"
                    class="bg-error text-on-error px-6 py-2 rounded-full font-bold shadow-md hover:scale-105 transition-transform flex items-center gap-2">
                    <span class="material-symbols-outlined text-[20px]">cancel</span>
                    <spring:message code="commerce.auction.cancel.confirm" />
                </button>
            </form>
        </div>
    </div>
</div>

<script>
    function openCancelAuctionModal(auctionId) {
        var modal = document.getElementById('cancelAuctionModal');
        var content = document.getElementById('cancelAuctionModalContent');
        var form = document.getElementById('cancelAuctionForm');

        form.action = '${pageContext.request.contextPath}/commerce/auctions/' + auctionId + '/cancel';

        modal.classList.remove('hidden');
        // Trigger reflow for animation
        void modal.offsetWidth;
        content.classList.remove('scale-95', 'opacity-0');
        content.classList.add('scale-100', 'opacity-100');
    }

    function closeCancelAuctionModal() {
        var modal = document.getElementById('cancelAuctionModal');
        var content = document.getElementById('cancelAuctionModalContent');

        content.classList.remove('scale-100', 'opacity-100');
        content.classList.add('scale-95', 'opacity-0');

        setTimeout(function() {
            modal.classList.add('hidden');
        }, 200); // Wait for transition
    }
</script>
