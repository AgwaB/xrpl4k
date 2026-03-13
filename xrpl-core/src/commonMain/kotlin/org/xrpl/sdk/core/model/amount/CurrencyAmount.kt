package org.xrpl.sdk.core.model.amount

/**
 * The amount of currency in an XRPL transaction.
 *
 * Three subtypes represent the three currency types on the XRPL:
 * - [XrpAmount] for native XRP
 * - [IssuedAmount] for IOU tokens
 * - [MptAmount] for Multi-Purpose Tokens
 */
public sealed interface CurrencyAmount
