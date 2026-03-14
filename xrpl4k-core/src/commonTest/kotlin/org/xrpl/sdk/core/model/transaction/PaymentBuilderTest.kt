package org.xrpl.sdk.core.model.transaction

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.xrpl.sdk.core.model.amount.IssuedAmount
import org.xrpl.sdk.core.model.amount.XrpAmount
import org.xrpl.sdk.core.type.Address
import org.xrpl.sdk.core.type.CurrencyCode
import org.xrpl.sdk.core.type.XrpDrops

class PaymentBuilderTest : FunSpec({

    val sender = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh")
    val receiver = Address("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe")
    val xrpAmount = XrpAmount(XrpDrops(1_000_000L))
    val usdAmount =
        IssuedAmount(
            currency = CurrencyCode("USD"),
            issuer = Address("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
            value = "1.5",
        )

    context("payment DSL - minimal required fields") {
        test("builds with account, destination, amount") {
            val tx =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                }
            tx.transactionType shouldBe TransactionType.Payment
            tx.account shouldBe sender
            val fields = tx.fields as PaymentFields
            fields.destination shouldBe receiver
            fields.amount shouldBe xrpAmount
            fields.sendMax shouldBe null
            fields.deliverMin shouldBe null
            fields.destinationTag shouldBe null
            fields.invoiceId shouldBe null
            fields.paths shouldBe null
            tx.memos shouldBe emptyList()
            tx.sourceTag shouldBe null
        }
    }

    context("payment DSL - all optional fields") {
        test("builds with all fields set") {
            val tx =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                    sendMax = usdAmount
                    deliverMin = xrpAmount
                    destinationTag = 42u
                    invoiceId = "AABBCCDD"
                    sourceTag = 99u
                }
            val fields = tx.fields as PaymentFields
            fields.sendMax shouldBe usdAmount
            fields.deliverMin shouldBe xrpAmount
            fields.destinationTag shouldBe 42u
            fields.invoiceId shouldBe "AABBCCDD"
            tx.sourceTag shouldBe 99u
        }
    }

    context("payment DSL - memo helpers") {
        test("memo {} DSL appends a memo") {
            val tx =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                    memo {
                        memoData = "AABB"
                        memoType = "746578742F706C61696E"
                    }
                }
            tx.memos.size shouldBe 1
            tx.memos[0].memoData shouldBe "AABB"
            tx.memos[0].memoType shouldBe "746578742F706C61696E"
        }

        test("multiple memo {} blocks accumulate") {
            val tx =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                    memo { memoData = "AABB" }
                    memo { memoData = "CCDD" }
                }
            tx.memos.size shouldBe 2
        }

        test("memos list property used when no memo {} blocks") {
            val m = Memo(memoData = "FFEE")
            val tx =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                    memos = listOf(m)
                }
            tx.memos shouldBe listOf(m)
        }

        test("memo {} blocks take precedence over memos list") {
            val m = Memo(memoData = "FFEE")
            val tx =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                    memos = listOf(m)
                    memo { memoData = "AABB" }
                }
            tx.memos.size shouldBe 1
            tx.memos[0].memoData shouldBe "AABB"
        }
    }

    context("payment DSL - validation errors") {
        test("throws when account is missing") {
            shouldThrow<IllegalArgumentException> {
                payment {
                    destination = receiver
                    amount = xrpAmount
                }
            }.message shouldBe "account is required for Payment"
        }

        test("throws when destination is missing") {
            shouldThrow<IllegalArgumentException> {
                payment {
                    account = sender
                    amount = xrpAmount
                }
            }.message shouldBe "destination is required for Payment"
        }

        test("throws when amount is missing") {
            shouldThrow<IllegalArgumentException> {
                payment {
                    account = sender
                    destination = receiver
                }
            }.message shouldBe "amount is required for Payment"
        }
    }

    context("PaymentFields - equals and hashCode") {
        test("equal instances are equal") {
            val a = PaymentFields(destination = receiver, amount = xrpAmount)
            val b = PaymentFields(destination = receiver, amount = xrpAmount)
            a shouldBe b
        }

        test("equal instances have equal hashCodes") {
            val a = PaymentFields(destination = receiver, amount = xrpAmount)
            val b = PaymentFields(destination = receiver, amount = xrpAmount)
            a.hashCode() shouldBe b.hashCode()
        }

        test("different destination means not equal") {
            val a = PaymentFields(destination = receiver, amount = xrpAmount)
            val b = PaymentFields(destination = sender, amount = xrpAmount)
            a shouldNotBe b
        }

        test("different amount means not equal") {
            val a = PaymentFields(destination = receiver, amount = xrpAmount)
            val b = PaymentFields(destination = receiver, amount = usdAmount)
            a shouldNotBe b
        }

        test("not equal to null") {
            val a = PaymentFields(destination = receiver, amount = xrpAmount)
            (a.equals(null)) shouldBe false
        }

        test("not equal to different type") {
            val a = PaymentFields(destination = receiver, amount = xrpAmount)
            (a.equals("string")) shouldBe false
        }

        test("same reference is equal") {
            val a = PaymentFields(destination = receiver, amount = xrpAmount)
            (a.equals(a)) shouldBe true
        }

        test("optional fields participate in equality") {
            val a = PaymentFields(destination = receiver, amount = xrpAmount, destinationTag = 1u)
            val b = PaymentFields(destination = receiver, amount = xrpAmount, destinationTag = 2u)
            a shouldNotBe b
        }
    }

    context("PaymentFields - toString") {
        test("toString includes all field names") {
            val f = PaymentFields(destination = receiver, amount = xrpAmount, destinationTag = 5u)
            val s = f.toString()
            s.contains("PaymentFields") shouldBe true
            s.contains("destination=") shouldBe true
            s.contains("amount=") shouldBe true
            s.contains("destinationTag=5") shouldBe true
        }

        test("toString shows null for unset optionals") {
            val f = PaymentFields(destination = receiver, amount = xrpAmount)
            f.toString().contains("sendMax=null") shouldBe true
        }
    }

    context("PathStep") {
        test("constructs with all fields") {
            val step =
                PathStep(
                    account = sender,
                    currency = "USD",
                    issuer = receiver,
                )
            step.account shouldBe sender
            step.currency shouldBe "USD"
            step.issuer shouldBe receiver
        }

        test("equal PathSteps are equal") {
            val a = PathStep(account = sender)
            val b = PathStep(account = sender)
            a shouldBe b
        }

        test("equal PathSteps have equal hashCodes") {
            val a = PathStep(account = sender)
            val b = PathStep(account = sender)
            a.hashCode() shouldBe b.hashCode()
        }

        test("different PathSteps are not equal") {
            val a = PathStep(account = sender)
            val b = PathStep(account = receiver)
            a shouldNotBe b
        }

        test("toString includes all fields") {
            val step = PathStep(account = sender, currency = "USD")
            step.toString().contains("PathStep") shouldBe true
        }
    }

    context("payment DSL - paths field") {
        test("builds with paths") {
            val path = listOf(PathStep(account = receiver))
            val tx =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                    paths = listOf(path)
                }
            val fields = tx.fields as PaymentFields
            fields.paths shouldBe listOf(path)
        }
    }

    context("payment result type") {
        test("returns XrplTransaction.Unsigned") {
            val tx: XrplTransaction.Unsigned =
                payment {
                    account = sender
                    destination = receiver
                    amount = xrpAmount
                }
            tx.transactionType shouldBe TransactionType.Payment
        }
    }
})
