package com.kontenery.validator

import com.kontenery.data.Payment
import com.kontenery.data.utils.errors.PaymentError
import com.kontenery.data.utils.errors.ValidationErrorType
import com.kontenery.repository.PaymentRepo

class PaymentValidator(
    private val paymentRepo: PaymentRepo
) {
    suspend fun validatePayment(newPayment: Payment, errors: MutableList<PaymentError>): Boolean {
        if (newPayment.referenceNumber.isNullOrBlank()) {
            val isDuplicated = paymentRepo.isDuplicate(newPayment)
            if (isDuplicated) {
                errors.add(
                    PaymentError(
                        ValidationErrorType.DUPLICATED.name,
                        "Payment with same parameters already exists",
                        newPayment
                    )
                )
                return false
            }
        } else if (paymentRepo.isPaymentWithReferenceNr(newPayment.referenceNumber)) {
            errors.add(
                PaymentError(
                    ValidationErrorType.DUPLICATED.name,
                    "Payment with REFERENCE nr already exists",
                    newPayment
                )
            )
            return false
        }
        return true
    }

    suspend fun validatePaymentByParams(newPayment: Payment): Boolean {
        return !paymentRepo.isDuplicate(newPayment)
    }
}
