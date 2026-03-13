package org.xrpl.sdk.test

/**
 * Inline JSON fixture strings for common XRPL RPC responses.
 *
 * These fixtures match the JSON-RPC envelope format used by rippled nodes.
 * Use them with [MockXrplClient.respondWith] to set up canned responses in tests.
 *
 * All success responses follow the pattern:
 * ```json
 * {"result":{"status":"success", ...result fields...}}
 * ```
 *
 * All error responses follow the pattern:
 * ```json
 * {"result":{"status":"error","error":"...","error_code":N,"error_message":"..."}}
 * ```
 */
public object JsonFixtures {
    /**
     * A successful `account_info` response for the genesis account
     * ([TestAddresses.GENESIS]).
     */
    public val accountInfoResponse: String =
        """{"result":{"status":"success",""" +
            """"account_data":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh",""" +
            """"Balance":"100000000000","Sequence":1,"OwnerCount":0,"Flags":0},""" +
            """"ledger_index":1000,"validated":true}}"""

    /**
     * A successful `server_info` response with typical mainnet values.
     */
    public val serverInfoResponse: String =
        """{"result":{"status":"success",""" +
            """"info":{"build_version":"1.12.0","complete_ledgers":"32570-87000000",""" +
            """"hostid":"XRPL","io_latency_ms":1,"server_state":"full",""" +
            """"uptime":12345,"peers":10,"load_factor":1.0,"validation_quorum":4,""" +
            """"validated_ledger":{"age":3,"base_fee_xrp":0.00001,""" +
            """"hash":"4BC50C9B0D8515D3EAAE1E74B29A95804346C491EE1A95BF25E4AAB854A6A651",""" +
            """"reserve_base_xrp":10.0,"reserve_inc_xrp":2.0,"seq":87000000}}}}"""

    /**
     * A successful `submit` response indicating the transaction was accepted.
     */
    public val submitResponse: String =
        """{"result":{"status":"success",""" +
            """"engine_result":"tesSUCCESS","engine_result_code":0,""" +
            """"engine_result_message":"The transaction was applied.",""" +
            """"tx_blob":"1200002200000000240000000361D4838D7EA4C68000000000000000000000""" +
            """00000055534400000000004B4E9C06F24296074F7BC48F92A97916C6DC5EA9""" +
            """68400000000000000A732103AB40A0490F9B7ED8DF29D246BF2D6269820A0E""" +
            """E7742ACDD457BEA7C7D0931EF74473045022100D184EB4AE5956FF600E753""" +
            """6EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201CEA8009B55CEDB""" +
            """6B6B8F98E08731F8A7527949A8C3E1FFD3CD7B7AFC8DD3F668114B5F76279""" +
            """8A53D543A014CAF8B297CFF8F2F937E88314F667B0CA50CC7709A220B0561""" +
            """B85E53A48461FA8",""" +
            """"tx_json":{"Account":"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh","TransactionType":"Payment"}}}"""

    /**
     * A successful `fee` response with current network fee information.
     */
    public val feeResponse: String =
        """{"result":{"status":"success",""" +
            """"current_ledger_size":"56","current_queue_size":"0",""" +
            """"drops":{"base_fee":"10","median_fee":"11000",""" +
            """"minimum_fee":"10","open_ledger_fee":"10"},""" +
            """"expected_ledger_size":"55","ledger_current_index":87000001,""" +
            """"max_queue_size":"1120"}}"""

    /**
     * A successful `ledger` response returning the most recently validated ledger.
     */
    public val ledgerResponse: String =
        """{"result":{"status":"success",""" +
            """"ledger":{"ledger_hash":"4BC50C9B0D8515D3EAAE1E74B29A95804346C491EE1A95BF25E4AAB854A6A651",""" +
            """"ledger_index":87000000,"account_hash":"AA126EAF5E3A8EA1B3E3A299B1F9AC6A",""" +
            """"transaction_hash":"BB2634A0C4B71E8524C0A1B26598D77A","close_time":784111552,""" +
            """"close_time_human":"2024-Nov-01 12:00:00","total_coins":"99999000000000000",""" +
            """"closed":true},""" +
            """"ledger_hash":"4BC50C9B0D8515D3EAAE1E74B29A95804346C491EE1A95BF25E4AAB854A6A651",""" +
            """"ledger_index":87000000,"validated":true}}"""

    /**
     * A generic RPC error response for an unknown command.
     */
    public val errorResponse: String =
        """{"result":{"status":"error",""" +
            """"error":"unknownCmd","error_code":32,""" +
            """"error_message":"Unknown method."}}"""

    /**
     * An account-not-found RPC error response (actNotFound, code 19).
     */
    public val notFoundResponse: String =
        """{"result":{"status":"error",""" +
            """"error":"actNotFound","error_code":19,""" +
            """"error_message":"Account not found."}}"""
}
