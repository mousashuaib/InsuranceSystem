package com.insurancesystem.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Model.Entity.PriceList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Business Logic for Prescription Quantity Calculations
 * 
 * Rules:
 * - Doctors prescribe by dose, frequency per day, and duration (days) - never by package count
 * - System automatically calculates required quantity based on drug form
 * - Tablets/Capsules: quantity = dose_per_day × duration
 * - Injections: quantity = injections_per_day × duration
 * - Syrups/Drops: Calculate total ml based on dose and duration, then determine number of bottles
 * - Creams/Ointments: Dispense by duration-based policy (e.g., one tube per X days)
 */
@Component
@Slf4j
public class PrescriptionQuantityCalculator {

    private final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Extract drug form from PriceList serviceDetails JSON
     */
    public String extractDrugForm(PriceList priceList) {
        try {
            if (priceList == null || priceList.getServiceDetails() == null) {
                return null;
            }
            Map<String, Object> serviceDetails = jsonMapper.readValue(priceList.getServiceDetails(), Map.class);
            return (String) serviceDetails.get("form");
        } catch (Exception e) {
            log.warn("Failed to extract drug form from serviceDetails: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract package quantity from PriceList serviceDetails or quantity field
     */
    public Integer extractPackageQuantity(PriceList priceList) {
        // First try quantity field (direct field)
        if (priceList.getQuantity() != null && priceList.getQuantity() > 0) {
            return priceList.getQuantity();
        }

        // Fallback to serviceDetails JSON
        try {
            if (priceList.getServiceDetails() != null) {
                Map<String, Object> serviceDetails = jsonMapper.readValue(priceList.getServiceDetails(), Map.class);
                Object quantityObj = serviceDetails.get("quantity");
                if (quantityObj != null) {
                    if (quantityObj instanceof Integer) {
                        return (Integer) quantityObj;
                    } else if (quantityObj instanceof Number) {
                        return ((Number) quantityObj).intValue();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract quantity from serviceDetails: {}", e.getMessage());
        }

        // Default to 1 if not found
        return 1;
    }

    /**
     * Calculate required quantity based on prescription parameters and drug form
     * 
     * @param dosage - Dose per administration (e.g., 1 tablet, 5ml)
     * @param timesPerDay - Frequency per day
     * @param duration - Duration in days
     * @param drugForm - Drug form: Tablet, Syrup, Injection, Cream, Drops
     * @param packageQuantity - Quantity in one package (for syrups/creams calculation)
     * @return Calculated quantity
     */
    public Integer calculateRequiredQuantity(Integer dosage, Integer timesPerDay, Integer duration, 
                                             String drugForm, Integer packageQuantity) {
        if (duration == null || duration <= 0) {
            log.warn("Missing or invalid duration for quantity calculation");
            return 0;
        }

        String form = drugForm != null ? drugForm.toUpperCase() : "";
        
        // Validate parameters based on drug form
        switch (form) {
            case "TABLET":
            case "CAPSULE":
                // Tablets/Capsules: quantity = dose_per_day × duration
                // dosage = number of tablets per administration
                // timesPerDay = number of administrations per day
                // total = (dosage × timesPerDay) × duration
                if (dosage == null || timesPerDay == null || dosage <= 0 || timesPerDay <= 0) {
                    log.warn("Missing or invalid dosage/timesPerDay for tablets: dosage={}, timesPerDay={}", 
                             dosage, timesPerDay);
                    return 0;
                }
                return dosage * timesPerDay * duration;

            case "INJECTION":
                // Injections: quantity = dosage × duration
                // dosage = number of injections needed
                // duration = treatment duration in days
                // Note: timesPerDay is not used for injections - doctor only specifies number of injections and duration
                if (dosage <= 0 || duration <= 0) {
                    log.warn("Invalid injection parameters: dosage={}, duration={}", dosage, duration);
                    return 0;
                }
                return dosage * duration;

            case "SYRUP":
                // Syrups: نحسب عدد العبوات المطلوبة
                // Calculate number of bottles needed based on total ml required
                // الكمية المطلوبة بالمل = dosage × timesPerDay × duration
                // Required quantity in ml = dosage × timesPerDay × duration
                if (dosage == null || timesPerDay == null || dosage <= 0 || timesPerDay <= 0) {
                    log.warn("Missing or invalid dosage/timesPerDay for syrup: dosage={}, timesPerDay={}", 
                             dosage, timesPerDay);
                    return 0;
                }
                int totalMlNeeded = dosage * timesPerDay * duration;
                
                if (packageQuantity != null && packageQuantity > 0) {
                    // عدد العبوات = round up (totalMlNeeded / packageQuantity)
                    // Number of bottles = round up (totalMlNeeded / packageQuantity)
                    int numberOfBottles = (int) Math.ceil((double) totalMlNeeded / packageQuantity);
                    log.info("💧 [QUANTITY] Syrup - Total ml needed: {}, Package size: {}ml, Number of bottles: {}", 
                            totalMlNeeded, packageQuantity, numberOfBottles);
                    return numberOfBottles;
                } else {
                    // إذا لم توجد كمية للعلبة، نرجع 1 علبة
                    // If no package quantity, return 1 bottle
                    log.warn("⚠️ [QUANTITY] No package quantity for syrup, defaulting to 1 bottle");
                    return 1;
                }

            case "DROPS":
                // Drops: نحسب عدد العبوات المطلوبة مع تحويل drops إلى ml
                // Calculate number of bottles needed, converting drops to ml first
                // القاعدة: 1 ml ≈ 20 drops (للقطرات فقط)
                // Rule: 1 ml ≈ 20 drops (for drops only)
                // الكمية المطلوبة بالـ drops = dosage × timesPerDay × duration
                // Required quantity in drops = dosage × timesPerDay × duration
                if (dosage == null || timesPerDay == null || dosage <= 0 || timesPerDay <= 0) {
                    log.warn("Missing or invalid dosage/timesPerDay for drops: dosage={}, timesPerDay={}", 
                             dosage, timesPerDay);
                    return 0;
                }
                int totalDropsNeeded = dosage * timesPerDay * duration;
                
                // تحويل drops إلى ml: totalMlNeeded = totalDropsNeeded / 20
                // Convert drops to ml: totalMlNeeded = totalDropsNeeded / 20
                double totalMlNeededFromDrops = (double) totalDropsNeeded / 20.0;
                
                if (packageQuantity != null && packageQuantity > 0) {
                    // عدد العبوات = round up (totalMlNeeded / packageQuantity)
                    // Number of bottles = round up (totalMlNeeded / packageQuantity)
                    int numberOfBottles = (int) Math.ceil(totalMlNeededFromDrops / packageQuantity);
                    log.info("💧 [QUANTITY] Drops - Total drops needed: {}, Total ml needed: {}ml ({} drops ÷ 20), Package size: {}ml, Number of bottles: {}", 
                            totalDropsNeeded, totalMlNeededFromDrops, totalDropsNeeded, packageQuantity, numberOfBottles);
                    return numberOfBottles;
                } else {
                    // إذا لم توجد كمية للعلبة، نرجع 1 علبة
                    // If no package quantity, return 1 bottle
                    log.warn("⚠️ [QUANTITY] No package quantity for drops, defaulting to 1 bottle");
                    return 1;
                }

            case "CREAM":
            case "OINTMENT":
                // Creams/Ointments: نحسب عدد الأنابيب بناءً على المدة وعدد المرات في اليوم (سياسة زمنية)
                // Calculate number of tubes based on duration and times per day (time-based policy)
                // القاعدة: 1 أنبوب ≈ 7 أيام (للاستخدام مرة واحدة في اليوم)
                // Rule: 1 tube ≈ 7 days (for once per day usage)
                // إذا كان timesPerDay = 2، نحتاج ضعف الكمية
                // If timesPerDay = 2, we need double the quantity
                // عدد الأنابيب = round up ((duration × timesPerDay) / 7)
                // Number of tubes = round up ((duration × timesPerDay) / 7)
                if (timesPerDay == null || timesPerDay <= 0) {
                    log.warn("Missing or invalid timesPerDay for cream/ointment: timesPerDay={}", timesPerDay);
                    return 0;
                }
                // dosage is not used for creams/ointments, only timesPerDay and duration
                int totalUsages = duration * timesPerDay;
                int numberOfTubes = (int) Math.ceil((double) totalUsages / 7.0);
                
                log.info("🧴 [QUANTITY] Cream/Ointment - Duration: {} days, Times per day: {}, Total usages: {}, Number of tubes: {} (1 tube ≈ 7 usages)", 
                        duration, timesPerDay, totalUsages, numberOfTubes);
                return numberOfTubes;

            default:
                // Default calculation: dosage × timesPerDay × duration
                log.warn("Unknown drug form '{}', using default calculation", drugForm);
                return dosage * timesPerDay * duration;
        }
    }

    /**
     * Calculate unit price from package price
     * 
     * @param packagePrice - Price per package
     * @param packageQuantity - Quantity in package
     * @return Price per unit
     */
    public Double calculateUnitPrice(Double packagePrice, Integer packageQuantity) {
        if (packagePrice == null || packagePrice <= 0) {
            return 0.0;
        }
        if (packageQuantity == null || packageQuantity <= 0) {
            return packagePrice; // If no quantity, assume price is already per unit
        }
        return packagePrice / packageQuantity;
    }

    /**
     * Calculate covered quantity (min of calculated and dispensed)
     * Insurance covers only the calculated quantity, even if more was dispensed
     * 
     * @param calculatedQuantity - Required quantity based on prescription
     * @param dispensedQuantity - Actual quantity dispensed by pharmacy
     * @return Covered quantity
     */
    public Integer calculateCoveredQuantity(Integer calculatedQuantity, Integer dispensedQuantity) {
        if (calculatedQuantity == null || calculatedQuantity <= 0) {
            return 0;
        }
        if (dispensedQuantity == null || dispensedQuantity <= 0) {
            return 0;
        }
        // Insurance covers only the calculated quantity
        return Math.min(calculatedQuantity, dispensedQuantity);
    }

    /**
     * Calculate final claim amount
     * Formula: min(union_price_per_unit, pharmacy_price_per_unit) × covered_quantity
     * 
     * @param unionPricePerUnit - Union price per unit
     * @param pharmacistPricePerUnit - Pharmacy price per unit
     * @param coveredQuantity - Covered quantity
     * @return Final claim amount
     */
    public Double calculateClaimAmount(Double unionPricePerUnit, Double pharmacistPricePerUnit, Integer coveredQuantity) {
        if (coveredQuantity == null || coveredQuantity <= 0) {
            return 0.0;
        }
        if (unionPricePerUnit == null) unionPricePerUnit = 0.0;
        if (pharmacistPricePerUnit == null) pharmacistPricePerUnit = 0.0;
        
        // Use the lower price between union and pharmacy price
        Double unitPrice = Math.min(unionPricePerUnit, pharmacistPricePerUnit);
        
        return unitPrice * coveredQuantity;
    }
}

