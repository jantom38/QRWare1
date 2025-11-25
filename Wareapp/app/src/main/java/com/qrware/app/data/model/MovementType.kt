package com.qrware.app.data.model

enum class MovementType(
    val displayName: String,
    val description: String,
    val increasesQuantity: Boolean = false,
    val decreasesQuantity: Boolean = false,
    val requiresApproval: Boolean = false
) {
    RECEIPT("Przyjęcie", "Towar przyjęty do magazynu", increasesQuantity = true),
    ISSUE("Wydanie", "Towar wydany z magazynu", decreasesQuantity = true),
    TRANSFER("Transfer", "Towar przeniesiony między lokalizacjami"),
    MOVE("Przemieszczenie", "Towar przemieszczony do innej lokalizacji"),
    ADJUSTMENT("Korekta", "Korekta ilości towaru", requiresApproval = true),
    CYCLE_COUNT("Liczenie cykliczne", "Korekta z liczenia cyklicznego"),
    PHYSICAL_COUNT("Inwentaryzacja", "Korekta z inwentaryzacji fizycznej"),
    RESERVE("Rezerwacja", "Towar zarezerwowany pod zamówienie"),
    UNRESERVE("Zwolnienie rezerwacji", "Rezerwacja towaru zwolniona"),
    PICK("Kompletacja", "Towar skompletowany pod zamówienie"),
    PACK("Pakowanie", "Towar spakowany do wysyłki"),
    SHIP("Wysyłka", "Towar wysłany", decreasesQuantity = true),
    RETURN("Zwrot", "Towar zwrócony do magazynu", increasesQuantity = true),
    PUTAWAY("Odłożenie", "Towar odłożony na miejsce składowania"),
    REPLENISHMENT("Uzupełnienie", "Uzupełnienie towaru z lokalizacji masowej"),
    ALLOCATION("Alokacja", "Towar przydzielony do konkretnego celu"),
    DEALLOCATION("Dealokacja", "Alokacja towaru usunięta"),
    QUARANTINE("Kwarantanna", "Towar przeniesiony do kwarantanny"),
    RELEASE("Zwolnienie", "Towar zwolniony z kwarantanny"),
    HOLD("Wstrzymanie", "Towar wstrzymany"),
    UNHOLD("Zwolnienie wstrzymania", "Wstrzymanie towaru usunięte"),
    DAMAGE("Uszkodzenie", "Towar oznaczony jako uszkodzony", requiresApproval = true),
    DISPOSAL("Utylizacja", "Towar zutylizowany", decreasesQuantity = true, requiresApproval = true),
    LOSS("Utrata", "Towar oznaczony jako utracony", decreasesQuantity = true, requiresApproval = true),
    FOUND("Odnalezienie", "Utracony towar odnaleziony", increasesQuantity = true),
    EXPIRY("Wygaśnięcie", "Towar przeterminowany"),
    RECALL("Wycofanie", "Towar wycofany"),
    STAGING("Sortowanie", "Towar przeniesiony do strefy sortowania"),
    CROSSDOCK("Crossdocking", "Towar przekierowany przez crossdock"),
    CONSOLIDATION("Konsolidacja", "Towary skonsolidowane"),
    SPLIT("Podział", "Ilość towaru podzielona"),
    MERGE("Łączenie", "Towary połączone"),
    CONVERSION("Konwersja", "Konwersja jednostek"),
    PRODUCTION("Produkcja", "Towar wyprodukowany", increasesQuantity = true),
    CONSUMPTION("Zużycie", "Towar zużyty w produkcji", decreasesQuantity = true),
    SCRAP("Złomowanie", "Towar złomowany", decreasesQuantity = true, requiresApproval = true),
    REWORK("Przeróbka", "Towar wysłany do przeróbki"),
    SAMPLE("Próbka", "Pobranie próbki", decreasesQuantity = true),
    LOAN("Wypożyczenie", "Towar wypożyczony", decreasesQuantity = true),
    LOAN_RETURN("Zwrot wypożyczenia", "Wypożyczony towar zwrócony", increasesQuantity = true);

    fun isInbound(): Boolean = increasesQuantity

    fun isOutbound(): Boolean = decreasesQuantity

    fun isMovement(): Boolean = this in listOf(
        TRANSFER, MOVE, PUTAWAY, REPLENISHMENT, STAGING, CROSSDOCK
    )

    fun isStatusChange(): Boolean = this in listOf(
        RESERVE, UNRESERVE, QUARANTINE, HOLD, DAMAGE, EXPIRY, RECALL
    )

    fun isAdjustment(): Boolean = this in listOf(
        ADJUSTMENT, CYCLE_COUNT, PHYSICAL_COUNT
    )

    fun isOrderRelated(): Boolean = this in listOf(
        RESERVE, PICK, PACK, SHIP, ALLOCATION, DEALLOCATION
    )

    fun isQualityRelated(): Boolean = this in listOf(
        QUARANTINE, RELEASE, DAMAGE, EXPIRY, RECALL, REWORK, SAMPLE
    )

    fun isProductionRelated(): Boolean = this in listOf(
        PRODUCTION, CONSUMPTION, SCRAP, REWORK, CONVERSION
    )

    companion object {
        fun getInboundTypes(): List<MovementType> = values().filter { it.isInbound() }
        fun getOutboundTypes(): List<MovementType> = values().filter { it.isOutbound() }
        fun getMovementTypes(): List<MovementType> = values().filter { it.isMovement() }
        fun getAdjustmentTypes(): List<MovementType> = values().filter { it.isAdjustment() }
        fun getOrderRelatedTypes(): List<MovementType> = values().filter { it.isOrderRelated() }
        fun getQualityRelatedTypes(): List<MovementType> = values().filter { it.isQualityRelated() }
        fun getProductionRelatedTypes(): List<MovementType> = values().filter { it.isProductionRelated() }
        fun getApprovalRequiredTypes(): List<MovementType> = values().filter { it.requiresApproval }
    }
}