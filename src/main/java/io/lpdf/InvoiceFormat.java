package io.lpdf;

import java.util.function.BiFunction;

/**
 * How the values of each type are written on the page, for {@link LCrossIndustryInvoice}: each function takes the value
 * in its normal form and the full name of its entry in the map, without the prefix or an item number
 * ({@code seller.country}, {@code lines.unit}), and returns the text to bind, never null: a value it leaves alone is
 * returned as it came. A type without a function is bound as the invoice has it.
 *
 * <pre>{@code
 * new InvoiceFormat().date((value, entryName) -> LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d.M.yyyy")))
 * }</pre>
 */
public final class InvoiceFormat {
    private BiFunction<String, String, String> date;
    private BiFunction<String, String, String> amount;
    private BiFunction<String, String, String> code;

    /** No functions: every value is bound as the invoice has it. */
    public InvoiceFormat() {
    }

    /** Writes a date, given as {@code 2026-10-31}, or {@code 2026-10} and {@code 2026-W44} for a month and a week. */
    public InvoiceFormat date(BiFunction<String, String, String> date) {
        this.date = date;
        return this;
    }

    /** Writes an amount, given as the invoice writes it: {@code 1250.10}. */
    public InvoiceFormat amount(BiFunction<String, String, String> amount) {
        this.amount = amount;
        return this;
    }

    /** Writes a code, given as the invoice writes it: {@code H87}, {@code S}, {@code 381}. Lpdf carries no words for codes. */
    public InvoiceFormat code(BiFunction<String, String, String> code) {
        this.code = code;
        return this;
    }

    /** The function of {@code type}, {@code date}, {@code amount} or {@code code}, or null. */
    BiFunction<String, String, String> of(String type) {
        return switch (type) {
            case "date" -> date;
            case "amount" -> amount;
            case "code" -> code;
            default -> null;
        };
    }
}
