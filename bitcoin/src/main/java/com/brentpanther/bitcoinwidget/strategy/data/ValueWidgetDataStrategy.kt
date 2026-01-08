package com.brentpanther.bitcoinwidget.strategy.data

import com.brentpanther.bitcoinwidget.BlockchainExplorer
import com.brentpanther.bitcoinwidget.WidgetState
import com.brentpanther.bitcoinwidget.exchange.ExchangeHelper
import org.json.JSONObject

class ValueWidgetDataStrategy(widgetId: Int)  : PriceWidgetDataStrategy(widgetId) {

    override fun setData(value: String) {
        widget?.apply {
            val valueDouble = value.toDoubleOrNull() ?: 0.0
            val amountHeld = amountHeld
            val address = address
            lastValue = when {
                amountHeld != null -> (valueDouble * amountHeld).toString()
                address != null -> {
                    val balance = getAddressBalance(address, blockchainExplorer)
                    if (balance == null) {
                        state = WidgetState.INVALID_ADDRESS
                        lastValue
                    } else {
                        (valueDouble * balance).toString()
                    }
                }
                else -> lastValue
            }
            lastUpdated = System.currentTimeMillis()
        }
    }

    private fun getAddressBalance(address: String, explorer: BlockchainExplorer): Double? {
        return try {
            when (explorer) {
                BlockchainExplorer.MEMPOOL_SPACE -> {
                    val url = "https://mempool.space/api/address/$address"
                    val response = ExchangeHelper.getString(url)
                    val json = JSONObject(response)
                    val chainStats = json.getJSONObject("chain_stats")
                    val fundedTxoSum = chainStats.getLong("funded_txo_sum")
                    val spentTxoSum = chainStats.getLong("spent_txo_sum")
                    val balanceSatoshis = fundedTxoSum - spentTxoSum
                    balanceSatoshis * .00000001
                }
                BlockchainExplorer.BLOCKCHAIN_INFO -> {
                    val url = "https://blockchain.info/q/addressbalance/$address"
                    val response = ExchangeHelper.getString(url)
                    if ("error" in response) {
                        null
                    } else {
                        val balanceSatoshis = response.toDoubleOrNull() ?: return null
                        balanceSatoshis * .00000001
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}