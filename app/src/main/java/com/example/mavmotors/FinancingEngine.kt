package com.example.mavmotors

import kotlin.math.pow

data class FinanceDecision(
    val approved: Boolean,
    val apr: Double,
    val monthlyPayment: Double,
    val reason: String
)

object FinancingEngine
{
    fun evaluate(
        vehiclePrice: Double,
        downPayment: Double,
        monthlyIncome: Double,
        creditScore: Int,
        employmentMonths: Int,
        termMonths: Int
    ): FinanceDecision
    {
        val financedAmount = vehiclePrice - downPayment
        val downPercent = if (vehiclePrice > 0) downPayment / vehiclePrice else 0.0

        if (financedAmount <= 0.0)
        {
            return FinanceDecision(
                approved = true,
                apr = 0.0,
                monthlyPayment = 0.0,
                reason = "No financing needed"
            )
        }

        if (creditScore < 640)
        {
            return FinanceDecision(false, 0.0, 0.0, "Credit score too low")
        }

        if (monthlyIncome < 2500)
        {
            return FinanceDecision(false, 0.0, 0.0, "Income too low")
        }

        if (employmentMonths < 6)
        {
            return FinanceDecision(false, 0.0, 0.0, "Employment history too short")
        }

        val apr =
            when
            {
                creditScore >= 760 && downPercent >= 0.15 -> 5.9
                creditScore >= 700 && downPercent >= 0.10 -> 7.9
                creditScore >= 660 && downPercent >= 0.15 -> 10.9
                else -> return FinanceDecision(false, 0.0, 0.0, "Application did not meet lender requirements")
            }

        val monthlyPayment = calculateMonthlyPayment(
            principal = financedAmount,
            annualRate = apr,
            months = termMonths
        )

        if (monthlyPayment > monthlyIncome * 0.20)
        {
            return FinanceDecision(false, 0.0, 0.0, "Estimated payment too high for stated income")
        }

        return FinanceDecision(
            approved = true,
            apr = apr,
            monthlyPayment = monthlyPayment,
            reason = "Approved"
        )
    }

    private fun calculateMonthlyPayment(
        principal: Double,
        annualRate: Double,
        months: Int
    ): Double
    {
        if (annualRate == 0.0 || months == 0)
        {
            return if (months == 0) 0.0 else principal / months
        }

        val monthlyRate = annualRate / 100.0 / 12.0
        return principal * monthlyRate / (1 - (1 + monthlyRate).pow(-months))
    }
}