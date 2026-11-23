package bg.uni_plovdiv.fmi.tapetransfer.data.repository

import bg.uni_plovdiv.fmi.tapetransfer.data.local.dao.PriceDao
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.DiscountEntity
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.MediaType
import bg.uni_plovdiv.fmi.tapetransfer.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepository @Inject constructor(
    private val priceDao: PriceDao,
) {

    val prices: Flow<List<PriceEntity>> = priceDao.observePrices().map { list ->
        list.sortedBy { it.mediaType.ordinal }
    }

    val discounts: Flow<List<DiscountEntity>> = priceDao.observeDiscounts()

    suspend fun updatePrice(price: PriceEntity) {
        priceDao.updatePrice(price)
    }

    suspend fun updateDiscount(discount: DiscountEntity) {
        priceDao.updateDiscount(discount)
    }

    suspend fun seedDefaultsIfNeeded() {
        if (priceDao.countPrices() > 0) return

        priceDao.insertPrices(
            listOf(
                PriceEntity(MediaType.VHS, 8.0),
                PriceEntity(MediaType.S_VHS, 10.0),
                PriceEntity(MediaType.VHS_C, 8.0),
                PriceEntity(MediaType.VIDEO8, 10.0),
                PriceEntity(MediaType.HI8, 10.0),
                PriceEntity(MediaType.DIGITAL8, 12.0),
                PriceEntity(MediaType.MINI_DV, 12.0),
                PriceEntity(MediaType.AUDIO, 5.0),
            ),
        )
        priceDao.insertDiscounts(
            listOf(
                DiscountEntity(minQuantity = 5, percent = 10),
                DiscountEntity(minQuantity = 10, percent = 15),
            ),
        )
    }
}
