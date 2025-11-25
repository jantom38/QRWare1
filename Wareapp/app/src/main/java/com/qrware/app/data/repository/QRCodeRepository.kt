package com.qrware.app.data.repository

import com.qrware.app.data.dto.*
import com.qrware.app.data.model.*
import com.qrware.app.data.remote.ApiService
import retrofit2.Response
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRCodeRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllQRCodes(
        page: Int = 0,
        size: Int = 20,
        sort: String = "id,desc"
    ): PaginatedResponse<QRCodeData> = apiService.getAllQRCodes(page, size, sort)

    suspend fun getQRCodeById(qrCodeId: Long): QRCodeData =
        apiService.getQRCodeById(qrCodeId)

    suspend fun scanQRCode(code: String): QRCodeData =
        apiService.scanQRCode(code)

    suspend fun getQRCodeByEntity(entityType: String, entityId: Long): QRCodeData =
        apiService.getQRCodeByEntity(entityType, entityId)

    suspend fun getActiveQRCodes(): List<QRCodeData> =
        apiService.getActiveQRCodes()

    suspend fun getQRCodesByType(type: QRCodeType): List<QRCodeData> =
        apiService.getQRCodesByType(type)

    suspend fun generateQRCode(request: GenerateQRRequest): QRCodeData =
        apiService.generateQRCode(request)

    suspend fun updateQRCode(qrCodeId: Long, request: UpdateQRRequest): QRCodeData =
        apiService.updateQRCode(qrCodeId, request)

    suspend fun deleteQRCode(qrCodeId: Long): Response<Unit> =
        apiService.deleteQRCode(qrCodeId)

    suspend fun toggleQRCodeActive(qrCodeId: Long): QRCodeData =
        apiService.toggleQRCodeActive(qrCodeId)

    suspend fun getQRStats(): QRStatsResponse =
        apiService.getQRStats()

    // ==================== INVENTORY INTEGRATION ====================

    /**
     * Pobiera pozycję magazynową po QR kodzie.
     * Pobiera DTO i konwertuje je na bezpieczny Model Domenowy.
     */
    suspend fun getInventoryByQRCode(qrCode: String): Result<InventoryItem> {
        return try {
            val dto = apiService.getInventoryByQRCode(qrCode)
            val item = dto.toDomainModel()
            Result.success(item)
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) {
                Result.failure(Exception("Nie znaleziono towaru dla kodu: $qrCode"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// =====================================================================
// FUNKCJE MAPUJĄCE (MAPPERS)
// =====================================================================

fun String?.toLocalDateOrNull(): LocalDate? {
    if (this.isNullOrEmpty()) return null
    return try {
        LocalDate.parse(this)
    } catch (e: Exception) {
        null
    }
}

// 1. InventoryItemDTO -> InventoryItem
fun InventoryItemDTO.toDomainModel(): InventoryItem {
    return InventoryItem(
        id = this.id,
        product = this.product.toDomain(),
        location = this.location.toDomain(),
        quantity = this.quantity,
        reservedQuantity = this.reservedQuantity,
        availableQuantity = this.availableQuantity,
        status = this.status,
        qrCode = this.qrCode ?: "BRAK-KODU",
        lotNumber = this.lotNumber,
        batchNumber = this.batchNumber,
        serialNumber = this.serialNumber,
        receivedDate = this.receivedDate.toLocalDateOrNull() ?: LocalDate.now(),
        expiryDate = this.expiryDate.toLocalDateOrNull(),
        manufactureDate = this.manufactureDate.toLocalDateOrNull(),
        lastCountedDate = this.lastCountedDate.toLocalDateOrNull(),
        lastMovedDate = this.lastMovedDate.toLocalDateOrNull(),
        unitCost = this.unitCost,
        totalCost = this.totalCost,
        supplierReference = this.supplierReference,
        purchaseOrderNumber = this.purchaseOrderNumber,
        notes = this.notes,
        temperature = this.temperature,
        humidity = this.humidity,
        conditionRating = this.conditionRating,
        quarantine = this.quarantine ?: false,
        quarantineReason = this.quarantineReason,
        hold = this.hold ?: false,
        holdReason = this.holdReason
    )
}

// 2. ProductDTO -> Product
fun ProductDTO.toDomain(): Product {
    return Product(
        id = this.id,
        sku = this.sku ?: "UNKNOWN",
        name = this.name,
        description = this.description,
        barcode = this.barcode,
        category = this.category?.toDomain() ?: createDummyCategory(),
        price = this.price,
        cost = this.cost,
        weight = this.weight,
        dimensionsLength = this.dimensionsLength,
        dimensionsWidth = this.dimensionsWidth,
        dimensionsHeight = this.dimensionsHeight,
        unitOfMeasure = this.unitOfMeasure ?: "szt.",
        minimumStock = this.minimumStock ?: 0,
        maximumStock = this.maximumStock,
        reorderPoint = this.reorderPoint,
        active = this.active ?: true,
        perishable = this.perishable ?: false,
        hazardous = this.hazardous ?: false,
        fragile = this.fragile ?: false,
        manufacturer = this.manufacturer,
        supplier = this.supplier,
        storageConditions = this.storageConditions
    )
}

// 3. CategoryDTO -> Category
fun CategoryDTO.toDomain(): Category {
    return Category(
        id = this.id,
        name = this.name,
        description = this.description,
        code = this.code,
        active = this.active,
        parentCategory = this.parent?.toDomain(),
        sortOrder = this.sortOrder,
        icon = this.icon,
        color = this.color,
        requiresSpecialHandling = this.requiresSpecialHandling,
        storageTemperatureMin = this.storageTemperatureMin,
        storageTemperatureMax = this.storageTemperatureMax,
        storageHumidityMin = this.storageHumidityMin,
        storageHumidityMax = this.storageHumidityMax,
        level = this.level,
        fullPath = this.fullPath
    )
}

// 4. LocationDTO -> Location
fun LocationDTO.toDomain(): Location {
    return Location(
        id = this.id,
        code = this.code ?: "LOC-???",
        name = this.name ?: "Nieznana lokalizacja",
        description = this.description,
        zone = this.zone.toDomain()
    )
}

// 5. ZoneDTO -> Zone
// Dodano '?' przy 'this.id', aby obsłużyć Long?, oraz pełną ścieżkę do ZoneType
fun ZoneDTO.toDomain(): Zone {
    // Używamy bezpiecznego operatora dla ID, jeśli ZoneDTO ma nullable ID
    val safeId: Long = this.id ?: 0L

    // Używamy pełnej ścieżki do ZoneType, aby uniknąć konfliktu nazw
    val safeType = this.type ?: com.qrware.app.data.model.ZoneType.STORAGE

    return Zone(
        id = safeId,
        code = this.code ?: "ZONE",
        name = this.name ?: "Unknown Zone",
        description = null,
        type = safeType
    )
}

// --- Funkcje pomocnicze ---

fun createDummyCategory(): Category {
    return Category(
        id = 0L, // POPRAWKA: Użyto 0L (Long), a nie 0 (Int)
        name = "Bez Kategorii",
        description = null,
        code = "UNCATEGORIZED",
        active = true,
        parentCategory = null,
        sortOrder = 0,
        icon = null,
        color = null,
        requiresSpecialHandling = false,
        storageTemperatureMin = null,
        storageTemperatureMax = null,
        storageHumidityMin = null,
        storageHumidityMax = null,
        level = 0,
        fullPath = null
    )
}