package org.dev.eclipse.enumarate;

/**
 * Tedarikçilerin gönderebileceği statik mesaj kodları.
 * Tedarikçi client'ı sadece bu enum değerlerinden birini seçer,
 * mesaj içeriği server tarafında bu enum'daki displayText'ten üretilir.
 *
 * Neden enum?
 * → Başlangıçta hızlı, type-safe, compile-time güvenli.
 * → İleride admin panelden yönetilecekse DB tablosuna taşınabilir.
 */
public enum StaticMessageCode {

    PROJE_SATISDA_MI("Bu proje hala satılık mı?"),
    TEKNIK_SARTNAME("Teknik şartname paylaşabilir misiniz?"),
    FIYAT_BILGISI("Fiyat bilgisi alabilir miyiz?"),
    TESLIM_TARIHI("Teslim tarihi nedir?"),
    BASVURU_ACIK_MI("Tedarikçi başvurusu açık mı?");

    private final String displayText;

    StaticMessageCode(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
