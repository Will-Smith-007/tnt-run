package de.will_smith_007.tntrun.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * In this enum are defined messages, which are often used.
 */
@Getter
@RequiredArgsConstructor
public enum Message {

    PREFIX("§f[§cTNT-Run§f] §7");

    private final String content;

    @Override
    public String toString() {
        return content;
    }
}
